package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.ScoreSubmissionQueryUseCase;
import org.univ.rankus.application.port.out.ScoreSubmissionRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;
import org.univ.rankus.domain.model.ranking.policy.DuplicateCheckPolicy;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 읽기 전용 트랜잭션
public class ScoreSubmissionQueryService implements ScoreSubmissionQueryUseCase {

    private final ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final DuplicateCheckPolicy duplicateCheckPolicy;

    @Override
    public ScoreSubmission findSubmissionById(Long submissionId) {
        return scoreSubmissionRepositoryPort.findById(submissionId)
                .orElseThrow(() -> new RankingValidationException(RankingErrorCode.SUBMISSION_NOT_FOUND));
    }

    @Override
    public Page<ScoreSubmission> findSubmissionsByUserId(Long userId, Pageable pageable) {
        // 사용자 존재 확인
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        return scoreSubmissionRepositoryPort.findByUserId(userId, pageable);
    }

    @Override
    public Page<ScoreSubmission> findSubmissionsByLabId(Long labId, Pageable pageable) {
        return scoreSubmissionRepositoryPort.findByLabId(labId, pageable);
    }

    @Override
    public Page<ScoreSubmission> findSubmissionsByStatus(SubmissionStatus status, Pageable pageable) {
        return scoreSubmissionRepositoryPort.findByStatus(status, pageable);
    }

    @Override
    public Page<ScoreSubmission> findSubmissionsByLabIdAndStatus(Long labId, SubmissionStatus status, Pageable pageable) {
        return scoreSubmissionRepositoryPort.findByLabIdAndStatus(labId, status, pageable);
    }

    @Override
    public Page<ScoreSubmission> findPendingSubmissionsForApprover(Long approverId, Pageable pageable) {
        // 승인자 존재 확인
        User approver = userRepositoryPort.findById(approverId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 승인자의 권한에 따라 다른 로직 적용
        if (approver.isAdmin() || approver.getRole().name().equals("PROFESSOR")) {
            // ADMIN과 PROFESSOR는 모든 PENDING 상태의 점수 신청을 볼 수 있음
            return scoreSubmissionRepositoryPort.findByStatus(SubmissionStatus.PENDING, pageable);
        } else {
            // LAB_LEADER, LAB_MANAGER는 자신의 랩실만
            if (approver.getLab() != null) {
                return scoreSubmissionRepositoryPort.findByLabIdAndStatus(
                        approver.getLab().getId(), SubmissionStatus.PENDING, pageable);
            } else {
                // 랩실 소속이 없으면 빈 페이지 반환
                return Page.empty(pageable);
            }
        }
    }

    @Override
    public DuplicateCheckPolicy.DuplicateCheckResult checkDuplicates(Long userId, LocalDate achievementDate,
                                                                     ScoreCategory category) {
        // 사용자 존재 확인
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 기존 점수 신청 조회
        List<ScoreSubmission> existingSubmissions = scoreSubmissionRepositoryPort
                .findByUserIdAndAchievementDate(userId, achievementDate);

        // 중복 검사 실행
        return duplicateCheckPolicy.checkDuplicates(userId, achievementDate, category, existingSubmissions);
    }

    @Override
    public List<ScoreSubmission> findSubmissionsExpiringWithin(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusDays(days);

        return scoreSubmissionRepositoryPort.findByStatusAndExpiresAtBetween(
                SubmissionStatus.PENDING, now, futureTime);
    }

    @Override
    public List<ScoreSubmission> findExpiredSubmissions() {
        return scoreSubmissionRepositoryPort.findByStatusAndExpiresAtBefore(
                SubmissionStatus.PENDING, LocalDateTime.now());
    }

    @Override
    public int calculateUserScoreInLab(Long userId, Long labId) {
        // 사용자 존재 확인
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        Integer score = scoreSubmissionRepositoryPort.sumScoresByUserIdAndLabIdAndStatus(
                userId, labId, SubmissionStatus.APPROVED);
        return score != null ? score : 0;
    }

    @Override
    public int calculateUserTotalScore(Long userId) {
        // 사용자 존재 확인
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        Integer score = scoreSubmissionRepositoryPort.sumScoresByUserIdAndStatus(
                userId, SubmissionStatus.APPROVED);
        return score != null ? score : 0;
    }
}