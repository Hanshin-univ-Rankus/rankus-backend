package org.univ.rankus.testutil.factory.domain;

import org.univ.rankus.application.port.in.query.RankingQueryUseCase.LabRankingResult;
import org.univ.rankus.application.port.in.query.RankingQueryUseCase.UserContribution;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DomainRankingFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 랭킹 관련 객체(LabRankingResult, UserContribution) 생성 메서드를 제공합니다.
 */
public final class DomainRankingFactory {

    private DomainRankingFactory() {
    }

    // LabRankingResult 팩토리 메서드들
    public static LabRankingResult buildValidLabRankingResult() {
        Lab lab = DomainLabFactory.buildAiLab();
        List<ScoreSubmission> recentSubmissions = Arrays.asList(
                DomainScoreSubmissionFactory.buildApprovedSubmission(),
                DomainScoreSubmissionFactory.buildApprovedSubmission()
        );
        List<UserContribution> topContributors = Arrays.asList(
                buildValidUserContribution(),
                buildUserContributionWithScore(80)
        );

        return new LabRankingResult(
                lab.getId(),
                lab.getName(),
                250, // totalScore
                1,   // rank
                recentSubmissions,
                topContributors
        );
    }

    public static LabRankingResult buildLabRankingResultWithScore(int totalScore) {
        Lab lab = DomainLabFactory.buildAiLab();
        return new LabRankingResult(
                lab.getId(),
                lab.getName(),
                totalScore,
                calculateRankFromScore(totalScore),
                Arrays.asList(DomainScoreSubmissionFactory.buildApprovedSubmission()),
                Arrays.asList(buildValidUserContribution())
        );
    }

    public static LabRankingResult buildLabRankingResultWithRank(int rank) {
        Lab lab = DomainLabFactory.buildAiLab();
        int totalScore = calculateScoreFromRank(rank);

        return new LabRankingResult(
                lab.getId(),
                lab.getName(),
                totalScore,
                rank,
                Arrays.asList(DomainScoreSubmissionFactory.buildApprovedSubmission()),
                Arrays.asList(buildValidUserContribution())
        );
    }

    public static LabRankingResult buildLabRankingResultWithLab(Lab lab) {
        List<ScoreSubmission> submissions = Arrays.asList(
                DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(
                        DomainUserFactory.buildValidUser(), lab),
                DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(
                        DomainUserFactory.buildValidUser(), lab)
        );

        return new LabRankingResult(
                lab.getId(),
                lab.getName(),
                180,
                2,
                submissions,
                Arrays.asList(buildValidUserContribution())
        );
    }

    public static LabRankingResult buildEmptyLabRankingResult() {
        Lab lab = DomainLabFactory.buildValidLab();
        return new LabRankingResult(
                lab.getId(),
                lab.getName(),
                0, // 점수 없음
                999, // 최하위 순위
                new ArrayList<>(), // 빈 제출 목록
                new ArrayList<>()  // 빈 기여자 목록
        );
    }

    public static LabRankingResult buildTopRankingResult() {
        Lab lab = DomainLabFactory.buildAiLab();
        List<ScoreSubmission> topSubmissions = Arrays.asList(
                DomainScoreSubmissionFactory.buildResearchPaperSubmission(),
                DomainScoreSubmissionFactory.buildContestWinnerSubmission(),
                DomainScoreSubmissionFactory.buildCertificationSubmission()
        );
        List<UserContribution> topContributors = Arrays.asList(
                buildUserContributionWithScore(150),
                buildUserContributionWithScore(120),
                buildUserContributionWithScore(100)
        );

        return new LabRankingResult(
                lab.getId(),
                lab.getName(),
                500, // 최고 점수
                1,   // 1등
                topSubmissions,
                topContributors
        );
    }

    // UserContribution 팩토리 메서드들
    public static UserContribution buildValidUserContribution() {
        User user = DomainUserFactory.buildValidUser();
        return new UserContribution(
                user.getId(),
                user.getName(),
                100, // contributionScore
                5    // submissionCount
        );
    }

    public static UserContribution buildUserContributionWithScore(int score) {
        User user = DomainUserFactory.buildValidUser();
        return new UserContribution(
                user.getId(),
                user.getName(),
                score,
                calculateSubmissionCountFromScore(score)
        );
    }

    public static UserContribution buildUserContributionWithUser(User user) {
        return new UserContribution(
                user.getId(),
                user.getName(),
                120,
                6
        );
    }

    public static UserContribution buildUserContributionWithSubmissions(int submissionCount) {
        User user = DomainUserFactory.buildValidUser();
        return new UserContribution(
                user.getId(),
                user.getName(),
                submissionCount * 20, // 제출당 평균 20점 가정
                submissionCount
        );
    }

    public static UserContribution buildTopUserContribution() {
        User user = DomainUserFactory.buildLabLeaderUser();
        return new UserContribution(
                user.getId(),
                user.getName(),
                200, // 높은 점수
                10   // 많은 제출
        );
    }

    public static UserContribution buildZeroContribution() {
        User user = DomainUserFactory.buildStudentUser();
        return new UserContribution(
                user.getId(),
                user.getName(),
                0,   // 점수 없음
                0    // 제출 없음
        );
    }

    // 리스트 생성 헬퍼 메서드들
    public static List<LabRankingResult> buildLabRankingResultList(int count) {
        List<LabRankingResult> results = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            results.add(buildLabRankingResultWithRank(i));
        }
        return results;
    }

    public static List<UserContribution> buildUserContributionList(int count) {
        List<UserContribution> contributions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int score = 150 - (i * 20); // 점수 내림차순
            contributions.add(buildUserContributionWithScore(Math.max(score, 0)));
        }
        return contributions;
    }

    public static List<LabRankingResult> buildTiedRankingResults() {
        // 동점 상황 시뮬레이션
        Lab lab1 = DomainLabFactory.buildAiLab();
        Lab lab2 = DomainLabFactory.buildDbLab();

        LabRankingResult result1 = new LabRankingResult(
                lab1.getId(), lab1.getName(), 100, 2, // 동점
                Arrays.asList(DomainScoreSubmissionFactory.buildApprovedSubmission()),
                Arrays.asList(buildValidUserContribution())
        );

        LabRankingResult result2 = new LabRankingResult(
                lab2.getId(), lab2.getName(), 100, 2, // 동점
                Arrays.asList(DomainScoreSubmissionFactory.buildApprovedSubmission()),
                Arrays.asList(buildValidUserContribution())
        );

        return Arrays.asList(result1, result2);
    }

    public static List<UserContribution> buildDiverseContributions() {
        return Arrays.asList(
                buildTopUserContribution(),      // 고기여자
                buildValidUserContribution(),   // 일반 기여자
                buildZeroContribution()         // 무기여자
        );
    }

    // 빌더 패턴
    public static LabRankingResultBuilder labRankingBuilder() {
        return new LabRankingResultBuilder();
    }

    public static UserContributionBuilder userContributionBuilder() {
        return new UserContributionBuilder();
    }

    public static class LabRankingResultBuilder {
        private Long labId = 1L;
        private String labName = "Test Lab";
        private int totalScore = 100;
        private int rank = 1;
        private List<ScoreSubmission> recentSubmissions = new ArrayList<>();
        private List<UserContribution> topContributors = new ArrayList<>();

        public LabRankingResultBuilder labId(Long labId) {
            this.labId = labId;
            return this;
        }

        public LabRankingResultBuilder labName(String labName) {
            this.labName = labName;
            return this;
        }

        public LabRankingResultBuilder totalScore(int totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        public LabRankingResultBuilder rank(int rank) {
            this.rank = rank;
            return this;
        }

        public LabRankingResultBuilder recentSubmissions(List<ScoreSubmission> submissions) {
            this.recentSubmissions = submissions;
            return this;
        }

        public LabRankingResultBuilder topContributors(List<UserContribution> contributors) {
            this.topContributors = contributors;
            return this;
        }

        public LabRankingResult build() {
            return new LabRankingResult(labId, labName, totalScore, rank, recentSubmissions, topContributors);
        }
    }

    public static class UserContributionBuilder {
        private Long userId = 1L;
        private String userName = "Test User";
        private int contributionScore = 50;
        private int submissionCount = 3;

        public UserContributionBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public UserContributionBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public UserContributionBuilder contributionScore(int score) {
            this.contributionScore = score;
            return this;
        }

        public UserContributionBuilder submissionCount(int count) {
            this.submissionCount = count;
            return this;
        }

        public UserContribution build() {
            return new UserContribution(userId, userName, contributionScore, submissionCount);
        }
    }

    // 내부 헬퍼 메서드들
    private static int calculateRankFromScore(int totalScore) {
        if (totalScore >= 300) return 1;
        if (totalScore >= 200) return 2;
        if (totalScore >= 100) return 3;
        if (totalScore >= 50) return 4;
        return 5;
    }

    private static int calculateScoreFromRank(int rank) {
        return switch (rank) {
            case 1 -> 350;
            case 2 -> 250;
            case 3 -> 150;
            case 4 -> 75;
            default -> 25;
        };
    }

    private static int calculateSubmissionCountFromScore(int score) {
        // 평균 20점/제출 가정
        return Math.max(score / 20, 1);
    }

    // 특정 시나리오용 헬퍼들
    public static List<LabRankingResult> buildCompetitiveRankingScenario() {
        // 치열한 경쟁 상황 시뮬레이션
        return Arrays.asList(
                labRankingBuilder().labName("AI Lab").totalScore(300).rank(1).build(),
                labRankingBuilder().labName("DB Lab").totalScore(295).rank(2).build(),
                labRankingBuilder().labName("Security Lab").totalScore(290).rank(3).build()
        );
    }

    public static List<UserContribution> buildTeamContributionScenario() {
        // 팀 기여도 분산 시뮬레이션
        return Arrays.asList(
                userContributionBuilder().userName("팀장").contributionScore(150).submissionCount(8).build(),
                userContributionBuilder().userName("부팀장").contributionScore(100).submissionCount(5).build(),
                userContributionBuilder().userName("팀원1").contributionScore(80).submissionCount(4).build(),
                userContributionBuilder().userName("팀원2").contributionScore(70).submissionCount(3).build(),
                userContributionBuilder().userName("신입").contributionScore(20).submissionCount(1).build()
        );
    }
}