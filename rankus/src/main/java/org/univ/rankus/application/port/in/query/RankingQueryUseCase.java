package org.univ.rankus.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;

import java.util.List;

public interface RankingQueryUseCase {

    /**
     * 랭킹 정보를 담는 결과 DTO
     */
    class LabRankingResult {
        private final Long labId;
        private final String labName;
        private final int totalScore;
        private final int rank;
        private final List<ScoreSubmission> recentSubmissions;
        private final List<UserContribution> topContributors;

        public LabRankingResult(Long labId, String labName, int totalScore, int rank,
                                List<ScoreSubmission> recentSubmissions, List<UserContribution> topContributors) {
            this.labId = labId;
            this.labName = labName;
            this.totalScore = totalScore;
            this.rank = rank;
            this.recentSubmissions = recentSubmissions;
            this.topContributors = topContributors;
        }

        public Long getLabId() {
            return labId;
        }

        public String getLabName() {
            return labName;
        }

        public int getTotalScore() {
            return totalScore;
        }

        public int getRank() {
            return rank;
        }

        public List<ScoreSubmission> getRecentSubmissions() {
            return recentSubmissions;
        }

        public List<UserContribution> getTopContributors() {
            return topContributors;
        }
    }

    /**
     * 사용자 기여도 정보
     */
    class UserContribution {
        private final Long userId;
        private final String userName;
        private final int contributionScore;
        private final int approvedSubmissionCount;

        public UserContribution(Long userId, String userName, int contributionScore, int approvedSubmissionCount) {
            this.userId = userId;
            this.userName = userName;
            this.contributionScore = contributionScore;
            this.approvedSubmissionCount = approvedSubmissionCount;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public int getContributionScore() {
            return contributionScore;
        }

        public int getApprovedSubmissionCount() {
            return approvedSubmissionCount;
        }
    }

    /**
     * 전체 랩실 랭킹 조회
     */
    Page<LabRankingResult> getLabRankings(Pageable pageable);

    /**
     * 특정 랩실의 랭킹 정보 조회
     */
    LabRankingResult getLabRanking(Long labId);

    /**
     * 랩실의 상위 기여자 조회 (최대 5명)
     */
    List<UserContribution> getTopContributors(Long labId, int limit);

    /**
     * 사용자가 속한 랩실들의 랭킹 정보 조회
     */
    List<LabRankingResult> getUserLabRankings(Long userId);

    /**
     * 랩실 총 점수 계산
     */
    int calculateLabTotalScore(Long labId);

    /**
     * 사용자의 랩실 내 기여도 계산
     */
    int calculateUserContributionInLab(Long userId, Long labId);

    /**
     * 특정 점수 범위의 랩실 수 조회
     */
    long countLabsInScoreRange(int minScore, int maxScore);

    /**
     * 랭킹 업데이트가 필요한 랩실 목록 조회
     */
    List<Lab> findLabsNeedingRankingUpdate();
}