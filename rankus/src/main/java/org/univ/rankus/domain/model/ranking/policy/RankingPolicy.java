package org.univ.rankus.domain.model.ranking.policy;

import org.springframework.stereotype.Component;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;

import java.util.List;

/**
 * 랭킹 계산 정책을 정의하는 도메인 서비스
 */
@Component
public class RankingPolicy {

    /**
     * 랩실의 총 점수를 계산
     * 승인된 점수 신청의 합계
     */
    public int calculateLabTotalScore(List<ScoreSubmission> submissions) {
        return submissions.stream()
                .filter(submission -> submission.getStatus() == SubmissionStatus.APPROVED)
                .mapToInt(ScoreSubmission::getScore)
                .sum();
    }

    /**
     * 사용자의 기여도를 계산
     * 해당 랩실에서 승인된 점수의 합계
     */
    public int calculateUserContribution(Long userId, Long labId, List<ScoreSubmission> submissions) {
        return submissions.stream()
                .filter(submission -> submission.getStatus() == SubmissionStatus.APPROVED)
                .filter(submission -> submission.getUser().getId().equals(userId))
                .filter(submission -> submission.getLab().getId().equals(labId))
                .mapToInt(ScoreSubmission::getScore)
                .sum();
    }

    /**
     * 동점 처리 로직
     * 동일한 점수의 랩실들은 같은 순위로 처리
     */
    public int calculateRankWithTies(int totalScore, List<Integer> sortedScores) {
        int rank = 1;
        for (int score : sortedScores) {
            if (score > totalScore) {
                rank++;
            } else {
                break;
            }
        }
        return rank;
    }

    /**
     * 랭킹 유효성 검증
     */
    public boolean isValidRankingScore(int score) {
        return score >= 0;
    }

    /**
     * 다중 랩실 소속 시 점수 적용 정책
     * 모든 소속 랩실에 동일한 점수 적용
     */
    public boolean shouldApplyToAllLabs() {
        return true;
    }
}