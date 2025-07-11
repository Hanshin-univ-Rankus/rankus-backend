package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.RankingQueryUseCase;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.ScoreSubmissionRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.policy.RankingPolicy;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 읽기 전용 트랜잭션
public class RankingQueryService implements RankingQueryUseCase {

    private final ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;
    private final RankingPolicy rankingPolicy;

    @Override
    public Page<LabRankingResult> getLabRankings(Pageable pageable) {
        // 1) 모든 랩실 조회
        List<Lab> allLabs = labRepositoryPort.findAll();

        // 2) 각 랩실의 점수 계산 및 정렬
        List<LabRankingResult> rankings = allLabs.stream()
                .map(this::calculateLabRankingResult)
                .sorted((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()))
                .collect(Collectors.toList());

        // 3) 순위 계산
        List<Integer> sortedScores = rankings.stream()
                .map(LabRankingResult::getTotalScore)
                .collect(Collectors.toList());

        List<LabRankingResult> rankedResults = rankings.stream()
                .map(result -> new LabRankingResult(
                        result.getLabId(),
                        result.getLabName(),
                        result.getTotalScore(),
                        rankingPolicy.calculateRankWithTies(result.getTotalScore(), sortedScores),
                        result.getRecentSubmissions(),
                        result.getTopContributors()
                ))
                .collect(Collectors.toList());

        // 4) 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), rankedResults.size());
        List<LabRankingResult> pageContent = rankedResults.subList(start, end);

        return new PageImpl<>(pageContent, pageable, rankedResults.size());
    }

    @Override
    public LabRankingResult getLabRanking(Long labId) {
        // 1) 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 2) 랩실 랭킹 결과 계산
        LabRankingResult result = calculateLabRankingResult(lab);

        // 3) 전체 랭킹에서의 순위 계산
        List<Lab> allLabs = labRepositoryPort.findAll();
        List<Integer> allScores = allLabs.stream()
                .map(this::calculateLabTotalScore)
                .sorted((a, b) -> Integer.compare(b, a))
                .collect(Collectors.toList());

        int rank = rankingPolicy.calculateRankWithTies(result.getTotalScore(), allScores);

        return new LabRankingResult(
                result.getLabId(),
                result.getLabName(),
                result.getTotalScore(),
                rank,
                result.getRecentSubmissions(),
                result.getTopContributors()
        );
    }

    @Override
    public List<UserContribution> getTopContributors(Long labId, int limit) {
        // 1) 랩실 존재 확인
        labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 2) 랩실의 승인된 점수 신청 조회
        List<ScoreSubmission> approvedSubmissions = scoreSubmissionRepositoryPort
                .findByLabIdAndStatus(labId, SubmissionStatus.APPROVED);

        // 3) 사용자별 기여도 계산
        Map<User, Integer> userContributions = approvedSubmissions.stream()
                .collect(Collectors.groupingBy(
                        ScoreSubmission::getUser,
                        Collectors.summingInt(ScoreSubmission::getScore)
                ));

        // 4) 상위 기여자 추출
        return userContributions.entrySet().stream()
                .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    User user = entry.getKey();
                    int contributionScore = entry.getValue();
                    int submissionCount = (int) approvedSubmissions.stream()
                            .filter(s -> s.getUser().equals(user))
                            .count();
                    return new UserContribution(user.getId(), user.getName(), contributionScore, submissionCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<LabRankingResult> getUserLabRankings(Long userId) {
        // 1) 사용자 존재 확인
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2) 사용자가 속한 랩실들 조회 (현재는 하나의 랩실만 소속)
        if (user.getLab() == null) {
            return List.of();
        }

        // 3) 해당 랩실의 랭킹 정보 반환
        return List.of(getLabRanking(user.getLab().getId()));
    }

    @Override
    public int calculateLabTotalScore(Long labId) {
        // 1) 랩실 존재 확인
        labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 2) 랩실의 승인된 점수 신청 조회
        List<ScoreSubmission> approvedSubmissions = scoreSubmissionRepositoryPort
                .findByLabIdAndStatus(labId, SubmissionStatus.APPROVED);

        // 3) 총 점수 계산
        return rankingPolicy.calculateLabTotalScore(approvedSubmissions);
    }

    @Override
    public int calculateUserContributionInLab(Long userId, Long labId) {
        // 1) 사용자 존재 확인
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2) 랩실 존재 확인
        labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 3) 사용자의 해당 랩실 승인된 점수 신청 조회
        List<ScoreSubmission> userApprovedSubmissions = scoreSubmissionRepositoryPort
                .findByUserIdAndLabIdAndStatus(userId, labId, SubmissionStatus.APPROVED);

        // 4) 기여도 계산
        return rankingPolicy.calculateUserContribution(userId, labId, userApprovedSubmissions);
    }

    @Override
    public long countLabsInScoreRange(int minScore, int maxScore) {
        // 1) 모든 랩실 조회
        List<Lab> allLabs = labRepositoryPort.findAll();

        // 2) 각 랩실의 점수 계산 후 범위 내 랩실 수 계산
        return allLabs.stream()
                .mapToInt(this::calculateLabTotalScore)
                .filter(score -> score >= minScore && score <= maxScore)
                .count();
    }

    @Override
    public List<Lab> findLabsNeedingRankingUpdate() {
        // 모든 랩실 반환 (실제로는 랭킹 업데이트가 필요한 랩실만 필터링)
        return labRepositoryPort.findAll();
    }

    /**
     * 랩실의 총 점수 계산 (내부 헬퍼 메서드)
     */
    private int calculateLabTotalScore(Lab lab) {
        List<ScoreSubmission> approvedSubmissions = scoreSubmissionRepositoryPort
                .findByLabIdAndStatus(lab.getId(), SubmissionStatus.APPROVED);
        return rankingPolicy.calculateLabTotalScore(approvedSubmissions);
    }

    /**
     * 랩실 랭킹 결과 계산 (내부 헬퍼 메서드)
     */
    private LabRankingResult calculateLabRankingResult(Lab lab) {
        // 1) 총 점수 계산
        int totalScore = calculateLabTotalScore(lab);

        // 2) 최근 점수 신청 조회 (상위 10개)
        List<ScoreSubmission> recentSubmissions = scoreSubmissionRepositoryPort
                .findByLabIdAndStatus(lab.getId(), SubmissionStatus.APPROVED)
                .stream()
                .sorted((a, b) -> b.getApprovedAt().compareTo(a.getApprovedAt()))
                .limit(10)
                .collect(Collectors.toList());

        // 3) 상위 기여자 조회 (상위 5명)
        List<UserContribution> topContributors = getTopContributors(lab.getId(), 5);

        return new LabRankingResult(
                lab.getId(),
                lab.getName(),
                totalScore,
                0, // 순위는 호출하는 곳에서 계산
                recentSubmissions,
                topContributors
        );
    }
}