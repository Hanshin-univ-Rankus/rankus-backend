package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.VisibilityLevel;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DomainScoreSubmissionFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 ScoreSubmission 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainScoreSubmissionFactory {

    private DomainScoreSubmissionFactory() {
    }

    public static ScoreSubmission buildValidSubmission() {
        User user = DomainUserFactory.buildValidUserWithId(1L);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);

        return new ScoreSubmission(
                user,
                lab,
                ScoreCategory.RESEARCH_SCI_PAPER,
                "SCI 논문 게재 성과",
                LocalDate.now().minusDays(30),
                "https://example.com/proof.pdf",
                "연구 성과 인정 신청",
                "https://example.com/paper-link",
                VisibilityLevel.PUBLIC
        );
    }

    public static ScoreSubmission buildValidSubmissionWithId(Long id) {
        ScoreSubmission submission = buildValidSubmission();
        ReflectionTestUtils.setField(submission, "id", id);
        return submission;
    }

    public static ScoreSubmission buildSubmissionWithCategory(ScoreCategory category) {
        User user = DomainUserFactory.buildValidUserWithId(2L);
        Lab lab = DomainLabFactory.buildValidLabWithId(2L);

        return new ScoreSubmission(
                user,
                lab,
                category,
                category.getDisplayName() + " 성과",
                LocalDate.now().minusDays(10),
                "https://example.com/proof.pdf",
                "점수 신청",
                null,
                VisibilityLevel.PUBLIC
        );
    }

    public static ScoreSubmission buildSubmissionWithVisibility(VisibilityLevel visibility) {
        User user = DomainUserFactory.buildValidUserWithId(3L);
        Lab lab = DomainLabFactory.buildValidLabWithId(3L);

        return new ScoreSubmission(
                user,
                lab,
                ScoreCategory.CONTEST_EXTERNAL_WINNER,
                "교외 대회 수상",
                LocalDate.now().minusDays(15),
                "https://example.com/proof.pdf",
                "대회 수상 인정 신청",
                "https://example.com/contest-result",
                visibility
        );
    }

    public static ScoreSubmission buildSubmissionWithUserAndLab(User user, Lab lab) {
        // 사용자에게 랩실 할당 (권한 검사를 위해)
        if (user != null && lab != null) {
            try {
                user.assignLab(lab);
            } catch (Exception e) {
                // 이미 할당된 경우는 무시
            }
        }
        return new ScoreSubmission(
                user,
                lab,
                ScoreCategory.CERTIFICATION_NATIONAL,
                "국가 자격증 취득",
                LocalDate.now().minusDays(20),
                "https://example.com/cert.pdf",
                "자격증 취득 인정 신청",
                null,
                VisibilityLevel.LAB_ONLY
        );
    }

    public static ScoreSubmission buildApprovedSubmission() {
        ScoreSubmission submission = buildValidSubmission();
        ReflectionTestUtils.setField(submission, "status", SubmissionStatus.APPROVED);
        ReflectionTestUtils.setField(submission, "approvedBy", 999L);
        ReflectionTestUtils.setField(submission, "approvedAt", LocalDateTime.now());
        return submission;
    }

    public static ScoreSubmission buildRejectedSubmission() {
        ScoreSubmission submission = buildValidSubmission();
        ReflectionTestUtils.setField(submission, "status", SubmissionStatus.REJECTED);
        ReflectionTestUtils.setField(submission, "approvedBy", 999L);
        ReflectionTestUtils.setField(submission, "approvedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(submission, "rejectionReason", "증빙서류 부족");
        return submission;
    }

    public static ScoreSubmission buildExpiredSubmission() {
        User user = DomainUserFactory.buildValidUserWithId(4L);
        Lab lab = DomainLabFactory.buildValidLabWithId(4L);

        ScoreSubmission submission = new ScoreSubmission(
                user,
                lab,
                ScoreCategory.EDUCATION_COMPLETION,
                "교육 과정 수료",
                LocalDate.now().minusDays(200),
                "https://example.com/proof.pdf",
                "교육 수료 인정 신청",
                null,
                VisibilityLevel.PRIVATE
        );

        // 만료 시간을 과거로 설정
        ReflectionTestUtils.setField(submission, "expiresAt", LocalDateTime.now().minusDays(1));
        return submission;
    }

    public static ScoreSubmission buildCorrectedSubmission() {
        ScoreSubmission submission = buildApprovedSubmission();
        ReflectionTestUtils.setField(submission, "correctionUsed", true);
        ReflectionTestUtils.setField(submission, "correctionCount", 1);
        return submission;
    }

    public static ScoreSubmission buildSubmissionWithFutureDate() {
        User user = DomainUserFactory.buildValidUserWithId(5L);
        Lab lab = DomainLabFactory.buildValidLabWithId(5L);

        // 미래 날짜를 사용해서 검증 오류 유발용
        return new ScoreSubmission(
                user,
                lab,
                ScoreCategory.SEMINAR_PARTICIPATION,
                "세미나 참여",
                LocalDate.now().plusDays(1), // 미래 날짜
                "https://example.com/proof.pdf",
                null,
                null,
                VisibilityLevel.PUBLIC
        );
    }

    // 빌더 패턴
    public static ScoreSubmissionBuilder builder() {
        return new ScoreSubmissionBuilder();
    }

    public static class ScoreSubmissionBuilder {
        private User user = DomainUserFactory.buildValidUserWithId(10L);
        private Lab lab = DomainLabFactory.buildValidLabWithId(10L);
        private ScoreCategory category = ScoreCategory.ACADEMIC_ACHIEVEMENT;
        private String achievementDescription = "기본 성과 내용";
        private LocalDate achievementDate = LocalDate.now().minusDays(30);
        private String proofFileUrl = "https://example.com/proof.pdf";
        private String applicationReason = "점수 신청";
        private String relatedLink = null;
        private VisibilityLevel visibility = VisibilityLevel.PUBLIC;
        private Long id;
        private SubmissionStatus status;
        private Long approvedBy;
        private LocalDateTime approvedAt;
        private String rejectionReason;
        private boolean correctionUsed = false;
        private int correctionCount = 0;

        public ScoreSubmissionBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ScoreSubmissionBuilder lab(Lab lab) {
            this.lab = lab;
            return this;
        }

        public ScoreSubmissionBuilder category(ScoreCategory category) {
            this.category = category;
            return this;
        }

        public ScoreSubmissionBuilder achievementDescription(String description) {
            this.achievementDescription = description;
            return this;
        }

        public ScoreSubmissionBuilder achievementDate(LocalDate date) {
            this.achievementDate = date;
            return this;
        }

        public ScoreSubmissionBuilder proofFileUrl(String url) {
            this.proofFileUrl = url;
            return this;
        }

        public ScoreSubmissionBuilder applicationReason(String reason) {
            this.applicationReason = reason;
            return this;
        }

        public ScoreSubmissionBuilder relatedLink(String link) {
            this.relatedLink = link;
            return this;
        }

        public ScoreSubmissionBuilder visibility(VisibilityLevel visibility) {
            this.visibility = visibility;
            return this;
        }

        public ScoreSubmissionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ScoreSubmissionBuilder status(SubmissionStatus status) {
            this.status = status;
            return this;
        }

        public ScoreSubmissionBuilder approvedBy(Long approvedBy) {
            this.approvedBy = approvedBy;
            return this;
        }

        public ScoreSubmissionBuilder rejectionReason(String reason) {
            this.rejectionReason = reason;
            return this;
        }

        public ScoreSubmissionBuilder correctionUsed(boolean used) {
            this.correctionUsed = used;
            return this;
        }

        public ScoreSubmissionBuilder correctionCount(int count) {
            this.correctionCount = count;
            return this;
        }

        public ScoreSubmission build() {
            // 사용자에게 랩실 할당 (권한 검사를 위해)
            if (user != null && lab != null) {
                try {
                    user.assignLab(lab);
                } catch (Exception e) {
                    // 이미 할당된 경우는 무시
                }
            }

            ScoreSubmission submission = new ScoreSubmission(
                    user, lab, category, achievementDescription, achievementDate,
                    proofFileUrl, applicationReason, relatedLink, visibility
            );

            // Reflection을 사용해서 추가 필드 설정
            if (id != null) {
                ReflectionTestUtils.setField(submission, "id", id);
            }
            if (status != null) {
                ReflectionTestUtils.setField(submission, "status", status);
            }
            if (approvedBy != null) {
                ReflectionTestUtils.setField(submission, "approvedBy", approvedBy);
                ReflectionTestUtils.setField(submission, "approvedAt",
                        approvedAt != null ? approvedAt : LocalDateTime.now());
            }
            if (rejectionReason != null) {
                ReflectionTestUtils.setField(submission, "rejectionReason", rejectionReason);
            }
            if (correctionUsed) {
                ReflectionTestUtils.setField(submission, "correctionUsed", correctionUsed);
                ReflectionTestUtils.setField(submission, "correctionCount", correctionCount);
            }

            return submission;
        }
    }

    // 특정 시나리오용 헬퍼 메서드들
    public static ScoreSubmission buildContestWinnerSubmission() {
        return builder()
                .category(ScoreCategory.CONTEST_EXTERNAL_WINNER)
                .achievementDescription("국제 프로그래밍 대회 우승")
                .achievementDate(LocalDate.now().minusDays(45))
                .applicationReason("대회 우승 성과 인정 신청")
                .relatedLink("https://contest.example.com/results")
                .visibility(VisibilityLevel.PUBLIC)
                .build();
    }

    public static ScoreSubmission buildResearchPaperSubmission() {
        return builder()
                .category(ScoreCategory.RESEARCH_SCI_PAPER)
                .achievementDescription("머신러닝 관련 SCI 논문 게재")
                .achievementDate(LocalDate.now().minusDays(60))
                .applicationReason("연구 성과 인정 신청")
                .relatedLink("https://journal.example.com/paper123")
                .visibility(VisibilityLevel.LAB_ONLY)
                .build();
    }

    public static ScoreSubmission buildCertificationSubmission() {
        return builder()
                .category(ScoreCategory.CERTIFICATION_INTERNATIONAL)
                .achievementDescription("AWS Solutions Architect 자격증 취득")
                .achievementDate(LocalDate.now().minusDays(90))
                .applicationReason("국제 자격증 취득 인정 신청")
                .relatedLink("https://aws.amazon.com/certification/")
                .visibility(VisibilityLevel.PUBLIC)
                .build();
    }

    public static ScoreSubmission buildPrivateSubmission() {
        return builder()
                .category(ScoreCategory.VOLUNTEER_ACTIVITY)
                .achievementDescription("지역 사회 봉사 활동")
                .achievementDate(LocalDate.now().minusDays(14))
                .applicationReason("봉사 활동 인정 신청")
                .visibility(VisibilityLevel.PRIVATE)
                .build();
    }

    // 다양한 카테고리별 테스트 데이터
    public static ScoreSubmission buildWithRandomCategory() {
        ScoreCategory[] categories = ScoreCategory.values();
        ScoreCategory randomCategory = categories[(int) (Math.random() * categories.length)];

        return builder()
                .category(randomCategory)
                .achievementDescription(randomCategory.getDisplayName() + " 달성")
                .achievementDate(LocalDate.now().minusDays((int) (Math.random() * 365)))
                .build();
    }

    // 유효성 검증 실패용 데이터
    public static ScoreSubmission buildInvalidSubmission_EmptyDescription() {
        User user = DomainUserFactory.buildValidUserWithId(6L);
        Lab lab = DomainLabFactory.buildValidLabWithId(6L);

        return new ScoreSubmission(
                user,
                lab,
                ScoreCategory.ACADEMIC_ACHIEVEMENT,
                "", // 빈 문자열
                LocalDate.now().minusDays(10),
                "https://example.com/proof.pdf",
                null,
                null,
                VisibilityLevel.PUBLIC
        );
    }

    public static ScoreSubmission buildInvalidSubmission_TooLongDescription() {
        User user = DomainUserFactory.buildValidUserWithId(7L);
        Lab lab = DomainLabFactory.buildValidLabWithId(7L);

        return new ScoreSubmission(
                user,
                lab,
                ScoreCategory.ACADEMIC_ACHIEVEMENT,
                "가".repeat(501), // 500자 초과
                LocalDate.now().minusDays(10),
                "https://example.com/proof.pdf",
                null,
                null,
                VisibilityLevel.PUBLIC
        );
    }

    public static ScoreSubmission buildInvalidSubmission_InvalidUrl() {
        User user = DomainUserFactory.buildValidUserWithId(8L);
        Lab lab = DomainLabFactory.buildValidLabWithId(8L);

        return new ScoreSubmission(
                user,
                lab,
                ScoreCategory.ACADEMIC_ACHIEVEMENT,
                "성과 내용",
                LocalDate.now().minusDays(10),
                "https://example.com/proof.pdf",
                null,
                "invalid-url", // 잘못된 URL
                VisibilityLevel.PUBLIC
        );
    }

    /**
     * 특정 사용자와 점수로 ScoreSubmission 생성
     */
    public static ScoreSubmission buildSubmissionWithUserAndScore(User user, int score) {
        Lab lab = DomainLabFactory.buildValidLabWithId(9L);
        ScoreCategory category = findCategoryByScore(score);

        return new ScoreSubmission(
                user,
                lab,
                category,
                category.getDisplayName() + " 성과",
                LocalDate.now().minusDays(30),
                "https://example.com/proof.pdf",
                "점수 신청",
                null,
                VisibilityLevel.PUBLIC
        );
    }

    /**
     * 점수에 맞는 ScoreCategory 찾기
     */
    private static ScoreCategory findCategoryByScore(int score) {
        // 정확히 일치하는 점수를 찾기
        for (ScoreCategory category : ScoreCategory.values()) {
            if (category.getDefaultScore() == score) {
                return category;
            }
        }

        // 점수별 매핑 (테스트용)
        return switch (score) {
            case 100 -> ScoreCategory.RESEARCH_SCI_PAPER; // 100점
            case 80 -> ScoreCategory.CONTEST_EXTERNAL_RUNNER_UP; // 80점 -> 35점 카테고리 (매핑 불가)
            case 50 -> ScoreCategory.RESEARCH_GENERAL_PAPER; // 50점
            case 30 -> ScoreCategory.CONTEST_INTERNAL_WINNER; // 30점
            case 20 -> ScoreCategory.CERTIFICATION_NATIONAL; // 20점
            case 10 -> ScoreCategory.GPA_MAINTENANCE; // 10점
            case 5 -> ScoreCategory.ACADEMIC_ACHIEVEMENT; // 5점
            default -> ScoreCategory.ACADEMIC_ACHIEVEMENT; // 기본값
        };
    }
}