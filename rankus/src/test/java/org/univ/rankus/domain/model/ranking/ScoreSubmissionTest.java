package org.univ.rankus.domain.model.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainScoreSubmissionFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ScoreSubmission 도메인 테스트")
class ScoreSubmissionTest {

    @Nested
    @DisplayName("점수 신청 생성 시")
    class CreateScoreSubmissionTests {

        @Test
        @DisplayName("유효한 정보로 점수 신청을 생성할 수 있다")
        void createScoreSubmission_ValidInfo_Success() {
            // given
            User user = DomainUserFactory.buildValidUser();
            Lab lab = DomainLabFactory.buildValidLab();

            // when
            ScoreSubmission submission = new ScoreSubmission(
                    user, lab, ScoreCategory.RESEARCH_SCI_PAPER,
                    "SCI 논문 게재", LocalDate.now().minusDays(30),
                    "https://example.com/proof.pdf", "연구 성과",
                    "https://example.com/paper", VisibilityLevel.PUBLIC
            );

            // then
            assertThat(submission.getUser()).isEqualTo(user);
            assertThat(submission.getLab()).isEqualTo(lab);
            assertThat(submission.getCategory()).isEqualTo(ScoreCategory.RESEARCH_SCI_PAPER);
            assertThat(submission.getAchievementDescription()).isEqualTo("SCI 논문 게재");
            assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.PENDING);
            assertThat(submission.getVisibility()).isEqualTo(VisibilityLevel.PUBLIC);
            assertThat(submission.getScore()).isEqualTo(100); // SCI 논문은 100점
            assertThat(submission.isCorrectionUsed()).isFalse();
            assertThat(submission.getCorrectionCount()).isZero();
        }

        @Test
        @DisplayName("null 사용자로 점수 신청 생성 시 예외가 발생한다")
        void createScoreSubmission_NullUser_ThrowsException() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();

            // when & then
            assertThatThrownBy(() -> new ScoreSubmission(
                    null, lab, ScoreCategory.ACADEMIC_ACHIEVEMENT,
                    "성과 내용", LocalDate.now().minusDays(10),
                    "https://example.com/proof.pdf", null, null, VisibilityLevel.PUBLIC
            ))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.USER_REQUIRED);
                    });
        }

        @Test
        @DisplayName("null 랩실로 점수 신청 생성 시 예외가 발생한다")
        void createScoreSubmission_NullLab_ThrowsException() {
            // given
            User user = DomainUserFactory.buildValidUser();

            // when & then
            assertThatThrownBy(() -> new ScoreSubmission(
                    user, null, ScoreCategory.ACADEMIC_ACHIEVEMENT,
                    "성과 내용", LocalDate.now().minusDays(10),
                    "https://example.com/proof.pdf", null, null, VisibilityLevel.PUBLIC
            ))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.LAB_REQUIRED);
                    });
        }

        @Test
        @DisplayName("빈 성과 내용으로 점수 신청 생성 시 예외가 발생한다")
        void createScoreSubmission_EmptyDescription_ThrowsException() {
            // given
            User user = DomainUserFactory.buildValidUser();
            Lab lab = DomainLabFactory.buildValidLab();

            // when & then
            assertThatThrownBy(() -> new ScoreSubmission(
                    user, lab, ScoreCategory.ACADEMIC_ACHIEVEMENT,
                    "", LocalDate.now().minusDays(10),
                    "https://example.com/proof.pdf", null, null, VisibilityLevel.PUBLIC
            ))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.ACHIEVEMENT_DESCRIPTION_REQUIRED);
                    });
        }

        @Test
        @DisplayName("500자 초과 성과 내용으로 점수 신청 생성 시 예외가 발생한다")
        void createScoreSubmission_TooLongDescription_ThrowsException() {
            // given
            User user = DomainUserFactory.buildValidUser();
            Lab lab = DomainLabFactory.buildValidLab();
            String longDescription = "가".repeat(501);

            // when & then
            assertThatThrownBy(() -> new ScoreSubmission(
                    user, lab, ScoreCategory.ACADEMIC_ACHIEVEMENT,
                    longDescription, LocalDate.now().minusDays(10),
                    "https://example.com/proof.pdf", null, null, VisibilityLevel.PUBLIC
            ))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.ACHIEVEMENT_DESCRIPTION_TOO_LONG);
                    });
        }

        @Test
        @DisplayName("미래 날짜로 점수 신청 생성 시 예외가 발생한다")
        void createScoreSubmission_FutureDate_ThrowsException() {
            // given
            User user = DomainUserFactory.buildValidUser();
            Lab lab = DomainLabFactory.buildValidLab();

            // when & then
            assertThatThrownBy(() -> new ScoreSubmission(
                    user, lab, ScoreCategory.ACADEMIC_ACHIEVEMENT,
                    "성과 내용", LocalDate.now().plusDays(1),
                    "https://example.com/proof.pdf", null, null, VisibilityLevel.PUBLIC
            ))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.ACHIEVEMENT_DATE_FUTURE);
                    });
        }

        @Test
        @DisplayName("잘못된 관련 링크로 점수 신청 생성 시 예외가 발생한다")
        void createScoreSubmission_InvalidRelatedLink_ThrowsException() {
            // given
            User user = DomainUserFactory.buildValidUser();
            Lab lab = DomainLabFactory.buildValidLab();

            // when & then
            assertThatThrownBy(() -> new ScoreSubmission(
                    user, lab, ScoreCategory.ACADEMIC_ACHIEVEMENT,
                    "성과 내용", LocalDate.now().minusDays(10),
                    "https://example.com/proof.pdf", null, "invalid-url", VisibilityLevel.PUBLIC
            ))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.RELATED_LINK_INVALID);
                    });
        }
    }

    @Nested
    @DisplayName("점수 신청 승인 시")
    class ApproveSubmissionTests {

        @Test
        @DisplayName("유효한 승인자로 점수 신청을 승인할 수 있다")
        void approveSubmission_ValidApprover_Success() {
            // given
            User submitter = DomainUserFactory.buildStudentUser();
            Lab lab = DomainLabFactory.buildValidLab();
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(submitter, lab);
            Long approverId = 999L;

            // when
            submission.approve(approverId);

            // then
            assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.APPROVED);
            assertThat(submission.getApprovedBy()).isEqualTo(approverId);
            assertThat(submission.getApprovedAt()).isNotNull();
            assertThat(submission.getRejectionReason()).isNull();
        }

        @Test
        @DisplayName("null 승인자로 승인 시 예외가 발생한다")
        void approveSubmission_NullApprover_ThrowsException() {
            // given
            User submitter = DomainUserFactory.buildStudentUser();
            Lab lab = DomainLabFactory.buildValidLab();
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(submitter, lab);

            // when & then
            assertThatThrownBy(() -> submission.approve(null))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.INSUFFICIENT_PERMISSION);
                    });
        }

        @Test
        @DisplayName("본인이 신청한 점수를 승인 시 예외가 발생한다")
        void approveSubmission_SelfApproval_ThrowsException() {
            // given
            User user = DomainUserFactory.buildValidUserWithId(1L);
            Lab lab = DomainLabFactory.buildValidLab();
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(user, lab);

            // when & then
            assertThatThrownBy(() -> submission.approve(1L)) // 동일한 사용자 ID
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.CANNOT_APPROVE_OWN_SUBMISSION);
                    });
        }

        @Test
        @DisplayName("이미 승인된 점수를 재승인 시 예외가 발생한다")
        void approveSubmission_AlreadyApproved_ThrowsException() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildApprovedSubmission();

            // when & then
            assertThatThrownBy(() -> submission.approve(999L))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.INVALID_STATUS_TRANSITION);
                    });
        }
    }

    @Nested
    @DisplayName("점수 신청 거부 시")
    class RejectSubmissionTests {

        @Test
        @DisplayName("유효한 승인자로 점수 신청을 거부할 수 있다")
        void rejectSubmission_ValidApprover_Success() {
            // given
            User submitter = DomainUserFactory.buildStudentUser();
            Lab lab = DomainLabFactory.buildValidLab();
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(submitter, lab);
            Long approverId = 999L;
            String reason = "증빙서류 부족";

            // when
            submission.reject(approverId, reason);

            // then
            assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.REJECTED);
            assertThat(submission.getApprovedBy()).isEqualTo(approverId);
            assertThat(submission.getApprovedAt()).isNotNull();
            assertThat(submission.getRejectionReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("이미 거부된 점수를 재거부 시 예외가 발생한다")
        void rejectSubmission_AlreadyRejected_ThrowsException() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildRejectedSubmission();

            // when & then
            assertThatThrownBy(() -> submission.reject(999L, "거부 사유"))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.INVALID_STATUS_TRANSITION);
                    });
        }
    }

    @Nested
    @DisplayName("점수 신청 정정 시")
    class CorrectSubmissionTests {

        @Test
        @DisplayName("승인된 점수를 거부로 정정할 수 있다")
        void correctSubmission_ApprovedToRejected_Success() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildApprovedSubmission();
            Long correcterId = 999L;

            // when
            submission.correctStatus(SubmissionStatus.REJECTED, correcterId);

            // then
            assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.REJECTED);
            assertThat(submission.getApprovedBy()).isEqualTo(correcterId);
            assertThat(submission.isCorrectionUsed()).isTrue();
            assertThat(submission.getCorrectionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("거부된 점수를 승인으로 정정할 수 있다")
        void correctSubmission_RejectedToApproved_Success() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildRejectedSubmission();
            Long correcterId = 999L;

            // when
            submission.correctStatus(SubmissionStatus.APPROVED, correcterId);

            // then
            assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.APPROVED);
            assertThat(submission.getApprovedBy()).isEqualTo(correcterId);
            assertThat(submission.isCorrectionUsed()).isTrue();
            assertThat(submission.getCorrectionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("정정 횟수 초과 시 예외가 발생한다")
        void correctSubmission_ExceedLimit_ThrowsException() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildCorrectedSubmission();

            // when & then
            assertThatThrownBy(() -> submission.correctStatus(SubmissionStatus.REJECTED, 999L))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.CORRECTION_LIMIT_EXCEEDED);
                    });
        }

        @Test
        @DisplayName("이미 정정된 거부 점수를 승인으로 재정정 시 예외가 발생한다")
        void correctSubmission_AlreadyCorrectedRejectedToApproved_ThrowsException() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.builder()
                    .status(SubmissionStatus.REJECTED)
                    .correctionUsed(true)
                    .correctionCount(0) // correctionCount를 0으로 설정하여 첫 번째 검증 통과
                    .build();

            // when & then
            assertThatThrownBy(() -> submission.correctStatus(SubmissionStatus.APPROVED, 999L))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.CORRECTION_NOT_ALLOWED);
                    });
        }
    }

    @Nested
    @DisplayName("점수 신청 상태 확인 시")
    class StatusCheckTests {

        @Test
        @DisplayName("만료되지 않은 PENDING 점수는 승인 가능하다")
        void canBeApproved_PendingNotExpired_True() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();

            // when & then
            assertThat(submission.canBeApproved()).isTrue();
        }

        @Test
        @DisplayName("만료된 점수는 승인 불가하다")
        void canBeApproved_Expired_False() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildExpiredSubmission();

            // when & then
            assertThat(submission.canBeApproved()).isFalse();
            assertThat(submission.isExpired()).isTrue();
        }

        @Test
        @DisplayName("이미 승인된 점수는 승인 불가하다")
        void canBeApproved_AlreadyApproved_False() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildApprovedSubmission();

            // when & then
            assertThat(submission.canBeApproved()).isFalse();
        }
    }

    @Nested
    @DisplayName("권한 확인 시")
    class PermissionCheckTests {

        @Test
        @DisplayName("ADMIN은 모든 점수를 승인할 수 있다")
        void canUserApprove_Admin_True() {
            // given
            User submitter = DomainUserFactory.buildStudentUser();
            Lab lab = DomainLabFactory.buildValidLab();
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(submitter, lab);
            User admin = DomainUserFactory.buildAdminUser();

            // when & then
            assertThat(submission.canUserApprove(admin)).isTrue();
        }

        @Test
        @DisplayName("PROFESSOR는 모든 점수를 승인할 수 있다")
        void canUserApprove_Professor_True() {
            // given
            User submitter = DomainUserFactory.buildStudentUser();
            Lab lab = DomainLabFactory.buildValidLab();
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(submitter, lab);
            User professor = DomainUserFactory.buildProfessorUser();

            // when & then
            assertThat(submission.canUserApprove(professor)).isTrue();
        }

        @Test
        @DisplayName("같은 랩실의 LAB_LEADER는 점수를 승인할 수 있다")
        void canUserApprove_SameLabLeader_True() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            User submitter = DomainUserFactory.buildLabMemberWithLab(lab);
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(submitter, lab);

            User labLeader = DomainUserFactory.buildLabLeaderWithLab(lab);

            // when & then
            assertThat(submission.canUserApprove(labLeader)).isTrue();
        }

        @Test
        @DisplayName("다른 랩실의 LAB_LEADER는 점수를 승인할 수 없다")
        void canUserApprove_DifferentLabLeader_False() {
            // given
            Lab lab1 = DomainLabFactory.buildAiLab();
            Lab lab2 = DomainLabFactory.buildDbLab();

            User submitter = DomainUserFactory.buildLabMemberWithLab(lab1);
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(submitter, lab1);

            User labLeader = DomainUserFactory.buildLabLeaderWithLab(lab2);

            // when & then
            assertThat(submission.canUserApprove(labLeader)).isFalse();
        }

        @Test
        @DisplayName("본인이 신청한 점수는 승인할 수 없다")
        void canUserApprove_SelfSubmission_False() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            User submitter = DomainUserFactory.buildLabLeaderWithLab(lab);
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(submitter, lab);

            // when & then (본인이 승인자가 되는 경우)
            assertThat(submission.canUserApprove(submitter)).isFalse();
        }

        @Test
        @DisplayName("일반 학생은 점수를 승인할 수 없다")
        void canUserApprove_Student_False() {
            // given
            User submitter = DomainUserFactory.buildStudentUser();
            Lab lab = DomainLabFactory.buildValidLab();
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(submitter, lab);
            User student = DomainUserFactory.buildStudentUser();

            // when & then
            assertThat(submission.canUserApprove(student)).isFalse();
        }
    }

    @Nested
    @DisplayName("소유권 확인 시")
    class OwnershipTests {

        @Test
        @DisplayName("본인이 신청한 점수에 대해 소유권을 확인할 수 있다")
        void isOwnedBy_Owner_True() {
            // given
            User owner = DomainUserFactory.buildValidUserWithId(1L);
            Lab lab = DomainLabFactory.buildValidLab();
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(owner, lab);

            // when & then
            assertThat(submission.isOwnedBy(1L)).isTrue();
        }

        @Test
        @DisplayName("다른 사용자가 신청한 점수에 대해 소유권이 없다")
        void isOwnedBy_NotOwner_False() {
            // given
            User owner = DomainUserFactory.buildValidUserWithId(1L);
            Lab lab = DomainLabFactory.buildValidLab();
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUserAndLab(owner, lab);

            // when & then
            assertThat(submission.isOwnedBy(2L)).isFalse();
        }
    }

    @Nested
    @DisplayName("점수 계산 시")
    class ScoreCalculationTests {

        @Test
        @DisplayName("SCI 논문은 100점을 반환한다")
        void getScore_SciPaper_Returns100() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithCategory(ScoreCategory.RESEARCH_SCI_PAPER);

            // when & then
            assertThat(submission.getScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("교외 대회 우승은 40점을 반환한다")
        void getScore_ExternalContestWinner_Returns40() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithCategory(ScoreCategory.CONTEST_EXTERNAL_WINNER);

            // when & then
            assertThat(submission.getScore()).isEqualTo(40);
        }

        @Test
        @DisplayName("세미나 참여는 5점을 반환한다")
        void getScore_SeminarParticipation_Returns5() {
            // given
            ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithCategory(ScoreCategory.SEMINAR_PARTICIPATION);

            // when & then
            assertThat(submission.getScore()).isEqualTo(5);
        }
    }
}