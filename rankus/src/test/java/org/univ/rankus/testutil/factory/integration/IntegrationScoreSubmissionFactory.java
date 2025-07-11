package org.univ.rankus.testutil.factory.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.ScoreSubmissionRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.VisibilityLevel;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainScoreSubmissionFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * IntegrationScoreSubmissionFactory - 실제 데이터베이스와 연동하는 통합 테스트용 팩토리
 * Repository를 통해 ScoreSubmission 및 관련 엔티티들을 실제로 저장하고 조회합니다.
 */
@Component
public class IntegrationScoreSubmissionFactory {

    @Autowired
    private ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private LabRepositoryPort labRepositoryPort;

    @Autowired
    private IntegrationUserFactory userFactory;

    @Autowired
    private IntegrationLabFactory labFactory;

    public ScoreSubmission createAndSaveSubmission() {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);

        ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(user, lab);
        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveSubmissionWithStatus(SubmissionStatus status) {
        ScoreSubmission submission = createAndSaveSubmission();

        switch (status) {
            case APPROVED -> {
                User approver = userFactory.createAndSaveWithRole(userRepositoryPort, org.univ.rankus.domain.model.user.Role.PROFESSOR);
                submission.approve(approver.getId());
            }
            case REJECTED -> {
                User approver = userFactory.createAndSaveWithRole(userRepositoryPort, org.univ.rankus.domain.model.user.Role.PROFESSOR);
                submission.reject(approver.getId(), "테스트 거부 사유");
            }
            // PENDING는 기본 상태이므로 변경 없음
        }

        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveApprovedSubmission() {
        return createAndSaveSubmissionWithStatus(SubmissionStatus.APPROVED);
    }

    public ScoreSubmission createAndSaveRejectedSubmission() {
        return createAndSaveSubmissionWithStatus(SubmissionStatus.REJECTED);
    }

    public ScoreSubmission createAndSavePendingSubmission() {
        return createAndSaveSubmissionWithStatus(SubmissionStatus.PENDING);
    }

    public ScoreSubmission createAndSaveSubmissionWithCategory(ScoreCategory category) {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);

        ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithCategory(category);
        submission = new ScoreSubmission(
                user, lab, category,
                category.getDisplayName() + " 성과",
                LocalDate.now().minusDays(30),
                "https://example.com/proof.pdf",
                "점수 신청",
                null,
                VisibilityLevel.PUBLIC
        );

        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveSubmissionWithVisibility(VisibilityLevel visibility) {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);

        ScoreSubmission submission = new ScoreSubmission(
                user, lab,
                ScoreCategory.CONTEST_EXTERNAL_WINNER,
                "교외 대회 수상",
                LocalDate.now().minusDays(15),
                "https://example.com/proof.pdf",
                "대회 수상 인정 신청",
                "https://example.com/contest-result",
                visibility
        );

        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveSubmissionForUser(User user) {
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);

        ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(user, lab);
        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveSubmissionForLab(Lab lab) {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);

        ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(user, lab);
        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveSubmissionForUserAndLab(User user, Lab lab) {
        ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(user, lab);
        return scoreSubmissionRepositoryPort.save(submission);
    }

    public List<ScoreSubmission> createAndSaveMultipleSubmissions(int count) {
        List<ScoreSubmission> submissions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ScoreSubmission submission = createAndSaveSubmission();
            submissions.add(submission);
        }

        return submissions;
    }

    public List<ScoreSubmission> createAndSaveMultipleSubmissionsForLab(Lab lab, int count) {
        List<ScoreSubmission> submissions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User user = userFactory.createAndSaveLabMember(userRepositoryPort, lab);
            ScoreSubmission submission = createAndSaveSubmissionForUserAndLab(user, lab);
            submissions.add(submission);
        }

        return submissions;
    }

    public List<ScoreSubmission> createAndSaveApprovedSubmissionsForLab(Lab lab, int count) {
        List<ScoreSubmission> submissions = new ArrayList<>();
        User approver = userFactory.createAndSaveWithRole(userRepositoryPort, org.univ.rankus.domain.model.user.Role.PROFESSOR);

        for (int i = 0; i < count; i++) {
            User user = userFactory.createAndSaveLabMember(userRepositoryPort, lab);
            ScoreSubmission submission = createAndSaveSubmissionForUserAndLab(user, lab);
            submission.approve(approver.getId());
            submission = scoreSubmissionRepositoryPort.save(submission);
            submissions.add(submission);
        }

        return submissions;
    }

    public List<ScoreSubmission> createAndSaveSubmissionsWithDifferentCategories(int count) {
        List<ScoreSubmission> submissions = new ArrayList<>();
        ScoreCategory[] categories = ScoreCategory.values();

        for (int i = 0; i < count; i++) {
            ScoreCategory category = categories[i % categories.length];
            ScoreSubmission submission = createAndSaveSubmissionWithCategory(category);
            submissions.add(submission);
        }

        return submissions;
    }

    public ScoreSubmission createAndSaveHighScoreSubmission() {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);
        User approver = userFactory.createAndSaveWithRole(userRepositoryPort, org.univ.rankus.domain.model.user.Role.PROFESSOR);

        ScoreSubmission submission = new ScoreSubmission(
                user, lab,
                ScoreCategory.RESEARCH_SCI_PAPER, // 100점
                "SCI 논문 게재 성과",
                LocalDate.now().minusDays(30),
                "https://example.com/proof.pdf",
                "연구 성과 인정 신청",
                "https://example.com/paper-link",
                VisibilityLevel.PUBLIC
        );

        submission = scoreSubmissionRepositoryPort.save(submission);
        submission.approve(approver.getId());
        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveLowScoreSubmission() {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);
        User approver = userFactory.createAndSaveWithRole(userRepositoryPort, org.univ.rankus.domain.model.user.Role.PROFESSOR);

        ScoreSubmission submission = new ScoreSubmission(
                user, lab,
                ScoreCategory.SEMINAR_PARTICIPATION, // 5점
                "세미나 참여",
                LocalDate.now().minusDays(10),
                "https://example.com/proof.pdf",
                "세미나 참여 인정 신청",
                null,
                VisibilityLevel.PUBLIC
        );

        submission = scoreSubmissionRepositoryPort.save(submission);
        submission.approve(approver.getId());
        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveContestWinnerSubmission() {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);
        User approver = userFactory.createAndSaveWithRole(userRepositoryPort, org.univ.rankus.domain.model.user.Role.PROFESSOR);

        ScoreSubmission submission = new ScoreSubmission(
                user, lab,
                ScoreCategory.CONTEST_EXTERNAL_WINNER, // 50점
                "국제 프로그래밍 대회 우승",
                LocalDate.now().minusDays(45),
                "https://example.com/proof.pdf",
                "대회 우승 성과 인정 신청",
                "https://contest.example.com/results",
                VisibilityLevel.PUBLIC
        );

        submission = scoreSubmissionRepositoryPort.save(submission);
        submission.approve(approver.getId());
        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveCertificationSubmission() {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);
        User approver = userFactory.createAndSaveWithRole(userRepositoryPort, org.univ.rankus.domain.model.user.Role.PROFESSOR);

        ScoreSubmission submission = new ScoreSubmission(
                user, lab,
                ScoreCategory.CERTIFICATION_INTERNATIONAL, // 25점
                "AWS Solutions Architect 자격증 취득",
                LocalDate.now().minusDays(90),
                "https://example.com/proof.pdf",
                "국제 자격증 취득 인정 신청",
                "https://aws.amazon.com/certification/",
                VisibilityLevel.LAB_ONLY
        );

        submission = scoreSubmissionRepositoryPort.save(submission);
        submission.approve(approver.getId());
        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveExpiredSubmission() {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);

        ScoreSubmission submission = DomainScoreSubmissionFactory.buildExpiredSubmission();
        submission = new ScoreSubmission(
                user, lab,
                ScoreCategory.EDUCATION_COMPLETION,
                "교육 과정 수료",
                LocalDate.now().minusDays(200),
                "https://example.com/proof.pdf",
                "교육 수료 인정 신청",
                null,
                VisibilityLevel.PRIVATE
        );

        return scoreSubmissionRepositoryPort.save(submission);
    }

    public ScoreSubmission createAndSaveCorrectedSubmission() {
        ScoreSubmission submission = createAndSaveApprovedSubmission();
        User correcter = userFactory.createAndSaveWithRole(userRepositoryPort, org.univ.rankus.domain.model.user.Role.ADMIN);

        // 정정 처리 (승인 -> 거부)
        submission.correctStatus(SubmissionStatus.REJECTED, correcter.getId());
        return scoreSubmissionRepositoryPort.save(submission);
    }

    // 랭킹 테스트를 위한 시나리오 데이터
    public List<ScoreSubmission> createRankingTestScenario() {
        List<ScoreSubmission> submissions = new ArrayList<>();

        // 1. AI Lab에 여러 승인된 점수 생성
        Lab aiLab = labFactory.createAndSaveAiLab(labRepositoryPort);
        submissions.addAll(createAndSaveApprovedSubmissionsForLab(aiLab, 5));

        // 2. DB Lab에 적은 수의 점수 생성  
        Lab dbLab = labFactory.createAndSaveDbLab(labRepositoryPort);
        submissions.addAll(createAndSaveApprovedSubmissionsForLab(dbLab, 2));

        // 3. Security Lab에 중간 정도의 점수 생성
        Lab securityLab = labFactory.createAndSaveSecurityLab(labRepositoryPort);
        submissions.addAll(createAndSaveApprovedSubmissionsForLab(securityLab, 3));

        return submissions;
    }

    // 사용자별 기여도 테스트를 위한 시나리오 데이터
    public List<ScoreSubmission> createUserContributionTestScenario(Lab lab) {
        List<ScoreSubmission> submissions = new ArrayList<>();
        User approver = userFactory.createAndSaveWithRole(userRepositoryPort, org.univ.rankus.domain.model.user.Role.PROFESSOR);

        // 1. 고기여 사용자 (연구 논문)
        User topUser = userFactory.createAndSaveLabMember(userRepositoryPort, lab);
        ScoreSubmission highScore = createAndSaveSubmissionForUserAndLab(topUser, lab);
        highScore.approve(approver.getId());
        submissions.add(scoreSubmissionRepositoryPort.save(highScore));

        // 2. 중기여 사용자 (대회 수상)
        User midUser = userFactory.createAndSaveLabMember(userRepositoryPort, lab);
        ScoreSubmission midScore = createAndSaveSubmissionForUserAndLab(midUser, lab);
        midScore.approve(approver.getId());
        submissions.add(scoreSubmissionRepositoryPort.save(midScore));

        // 3. 저기여 사용자 (세미나 참여)
        User lowUser = userFactory.createAndSaveLabMember(userRepositoryPort, lab);
        ScoreSubmission lowScore = createAndSaveSubmissionForUserAndLab(lowUser, lab);
        lowScore.approve(approver.getId());
        submissions.add(scoreSubmissionRepositoryPort.save(lowScore));

        return submissions;
    }

    // 다양한 상태의 점수 신청 생성
    public List<ScoreSubmission> createMixedStatusSubmissions(int pendingCount, int approvedCount, int rejectedCount) {
        List<ScoreSubmission> submissions = new ArrayList<>();

        // PENDING 상태
        for (int i = 0; i < pendingCount; i++) {
            submissions.add(createAndSavePendingSubmission());
        }

        // APPROVED 상태
        for (int i = 0; i < approvedCount; i++) {
            submissions.add(createAndSaveApprovedSubmission());
        }

        // REJECTED 상태
        for (int i = 0; i < rejectedCount; i++) {
            submissions.add(createAndSaveRejectedSubmission());
        }

        return submissions;
    }

    // 다양한 공개 범위의 점수 신청 생성
    public List<ScoreSubmission> createMixedVisibilitySubmissions() {
        List<ScoreSubmission> submissions = new ArrayList<>();

        submissions.add(createAndSaveSubmissionWithVisibility(VisibilityLevel.PUBLIC));
        submissions.add(createAndSaveSubmissionWithVisibility(VisibilityLevel.LAB_ONLY));
        submissions.add(createAndSaveSubmissionWithVisibility(VisibilityLevel.PRIVATE));

        return submissions;
    }
}