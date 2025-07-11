package org.univ.rankus.application.service.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.ScoreSubmissionRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.ranking.VisibilityLevel;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;
import org.univ.rankus.domain.model.ranking.policy.ScoreSubmissionPolicy;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainScoreSubmissionFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ScoreSubmissionCommandService 테스트")
class ScoreSubmissionCommandServiceTest {

    @Mock
    private ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private ScoreSubmissionPolicy scoreSubmissionPolicy;

    @InjectMocks
    private ScoreSubmissionCommandService scoreSubmissionCommandService;

    @Nested
    @DisplayName("점수 신청 생성 시")
    class SubmitScoreTests {

        @Test
        @DisplayName("유효한 정보로 점수 신청을 생성할 수 있다")
        void submitScore_ValidInfo_Success() {
            // given
            Long userId = 1L;
            Long labId = 1L;
            ScoreCategory category = ScoreCategory.RESEARCH_SCI_PAPER;
            String description = "SCI 논문 게재";
            LocalDate achievementDate = LocalDate.now().minusDays(30);
            String proofFileUrl = "https://example.com/proof.pdf";
            String applicationReason = "연구 성과 인정";
            String relatedLink = "https://example.com/paper";
            VisibilityLevel visibility = VisibilityLevel.PUBLIC;

            User user = DomainUserFactory.buildValidUserWithId(userId);
            Lab lab = DomainLabFactory.buildValidLabWithId(labId);
            ScoreSubmission expectedSubmission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(1L);

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
            when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab));
            when(scoreSubmissionRepositoryPort.save(any(ScoreSubmission.class))).thenReturn(expectedSubmission);

            // when
            ScoreSubmission result = scoreSubmissionCommandService.submitScore(
                    userId, labId, category, description, achievementDate,
                    proofFileUrl, applicationReason, relatedLink, visibility
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);

            verify(userRepositoryPort).findById(userId);
            verify(labRepositoryPort).findById(labId);
            verify(scoreSubmissionRepositoryPort).save(any(ScoreSubmission.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 점수 신청 시 예외가 발생한다")
        void submitScore_UserNotFound_ThrowsException() {
            // given
            Long userId = 999L;
            Long labId = 1L;

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionCommandService.submitScore(
                    userId, labId, ScoreCategory.RESEARCH_SCI_PAPER, "논문 게재",
                    LocalDate.now().minusDays(30), "https://example.com/proof.pdf",
                    "연구 성과", null, VisibilityLevel.PUBLIC
            ))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException exception = (UserNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verify(labRepositoryPort, never()).findById(any());
            verify(scoreSubmissionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 랩실로 점수 신청 시 예외가 발생한다")
        void submitScore_LabNotFound_ThrowsException() {
            // given
            Long userId = 1L;
            Long labId = 999L;

            User user = DomainUserFactory.buildValidUserWithId(userId);
            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
            when(labRepositoryPort.findById(labId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionCommandService.submitScore(
                    userId, labId, ScoreCategory.RESEARCH_SCI_PAPER, "논문 게재",
                    LocalDate.now().minusDays(30), "https://example.com/proof.pdf",
                    "연구 성과", null, VisibilityLevel.PUBLIC
            ))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException exception = (LabNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verify(labRepositoryPort).findById(labId);
            verify(scoreSubmissionRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("점수 신청 승인 시")
    class ApproveSubmissionTests {

        @Test
        @DisplayName("유효한 승인자로 점수 신청을 승인할 수 있다")
        void approveSubmission_ValidApprover_Success() {
            // given
            Long submissionId = 1L;
            Long approverId = 2L;

            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(submissionId);
            User approver = DomainUserFactory.buildValidUserWithId(approverId);

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));
            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.of(approver));
            when(scoreSubmissionPolicy.canApprove(approver, submission)).thenReturn(true);
            when(scoreSubmissionRepositoryPort.save(any(ScoreSubmission.class))).thenReturn(submission);

            // when
            scoreSubmissionCommandService.approveSubmission(submissionId, approverId);

            // then
            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(userRepositoryPort).findById(approverId);
            verify(scoreSubmissionPolicy).canApprove(approver, submission);
            verify(scoreSubmissionRepositoryPort).save(submission);
        }

        @Test
        @DisplayName("존재하지 않는 점수 신청 승인 시 예외가 발생한다")
        void approveSubmission_SubmissionNotFound_ThrowsException() {
            // given
            Long submissionId = 999L;
            Long approverId = 2L;

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionCommandService.approveSubmission(submissionId, approverId))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.SUBMISSION_NOT_FOUND);
                    });

            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(userRepositoryPort, never()).findById(any());
            verify(scoreSubmissionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("승인 권한이 없는 사용자가 승인 시 예외가 발생한다")
        void approveSubmission_InsufficientPermission_ThrowsException() {
            // given
            Long submissionId = 1L;
            Long approverId = 2L;

            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(submissionId);
            User approver = DomainUserFactory.buildValidUserWithId(approverId);

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));
            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.of(approver));
            when(scoreSubmissionPolicy.canApprove(approver, submission)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> scoreSubmissionCommandService.approveSubmission(submissionId, approverId))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.INSUFFICIENT_PERMISSION);
                    });

            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(userRepositoryPort).findById(approverId);
            verify(scoreSubmissionPolicy).canApprove(approver, submission);
            verify(scoreSubmissionRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("점수 신청 거부 시")
    class RejectSubmissionTests {

        @Test
        @DisplayName("유효한 승인자로 점수 신청을 거부할 수 있다")
        void rejectSubmission_ValidApprover_Success() {
            // given
            Long submissionId = 1L;
            Long approverId = 2L;
            String rejectionReason = "증빙서류 부족";

            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(submissionId);
            User approver = DomainUserFactory.buildValidUserWithId(approverId);

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));
            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.of(approver));
            when(scoreSubmissionPolicy.canApprove(approver, submission)).thenReturn(true);
            when(scoreSubmissionRepositoryPort.save(any(ScoreSubmission.class))).thenReturn(submission);

            // when
            scoreSubmissionCommandService.rejectSubmission(submissionId, approverId, rejectionReason);

            // then
            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(userRepositoryPort).findById(approverId);
            verify(scoreSubmissionPolicy).canApprove(approver, submission);
            verify(scoreSubmissionRepositoryPort).save(submission);
        }

        @Test
        @DisplayName("존재하지 않는 승인자로 거부 시 예외가 발생한다")
        void rejectSubmission_ApproverNotFound_ThrowsException() {
            // given
            Long submissionId = 1L;
            Long approverId = 999L;
            String rejectionReason = "증빙서류 부족";

            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(submissionId);

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));
            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scoreSubmissionCommandService.rejectSubmission(submissionId, approverId, rejectionReason))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException exception = (UserNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(userRepositoryPort).findById(approverId);
            verify(scoreSubmissionRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("점수 신청 정정 시")
    class CorrectSubmissionStatusTests {

        @Test
        @DisplayName("유효한 정정자로 점수 신청을 정정할 수 있다")
        void correctSubmissionStatus_ValidCorrecter_Success() {
            // given
            Long submissionId = 1L;
            Long correcterId = 2L;

            ScoreSubmission submission = DomainScoreSubmissionFactory.buildApprovedSubmission();
            submission = DomainScoreSubmissionFactory.builder()
                    .id(submissionId)
                    .status(SubmissionStatus.APPROVED)
                    .build();
            User correcter = DomainUserFactory.buildValidUserWithId(correcterId);

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));
            when(userRepositoryPort.findById(correcterId)).thenReturn(Optional.of(correcter));
            when(scoreSubmissionPolicy.canApprove(correcter, submission)).thenReturn(true);
            when(scoreSubmissionPolicy.canCorrect(submission)).thenReturn(true);
            when(scoreSubmissionRepositoryPort.save(any(ScoreSubmission.class))).thenReturn(submission);

            // when
            scoreSubmissionCommandService.correctSubmissionStatus(submissionId, correcterId);

            // then
            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(userRepositoryPort).findById(correcterId);
            verify(scoreSubmissionPolicy).canApprove(correcter, submission);
            verify(scoreSubmissionPolicy).canCorrect(submission);
            verify(scoreSubmissionRepositoryPort).save(submission);
        }

        @Test
        @DisplayName("정정 권한이 없는 사용자가 정정 시 예외가 발생한다")
        void correctSubmissionStatus_InsufficientPermission_ThrowsException() {
            // given
            Long submissionId = 1L;
            Long correcterId = 2L;

            ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmissionWithId(submissionId);
            User correcter = DomainUserFactory.buildValidUserWithId(correcterId);

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));
            when(userRepositoryPort.findById(correcterId)).thenReturn(Optional.of(correcter));
            when(scoreSubmissionPolicy.canApprove(correcter, submission)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> scoreSubmissionCommandService.correctSubmissionStatus(submissionId, correcterId))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.INSUFFICIENT_PERMISSION);
                    });

            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(userRepositoryPort).findById(correcterId);
            verify(scoreSubmissionPolicy).canApprove(correcter, submission);
            verify(scoreSubmissionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("정정 불가능한 상태에서 정정 시 예외가 발생한다")
        void correctSubmissionStatus_CannotCorrect_ThrowsException() {
            // given
            Long submissionId = 1L;
            Long correcterId = 2L;

            ScoreSubmission submission = DomainScoreSubmissionFactory.buildCorrectedSubmission();
            submission = DomainScoreSubmissionFactory.builder()
                    .id(submissionId)
                    .correctionCount(1)
                    .build();
            User correcter = DomainUserFactory.buildValidUserWithId(correcterId);

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));
            when(userRepositoryPort.findById(correcterId)).thenReturn(Optional.of(correcter));
            when(scoreSubmissionPolicy.canApprove(correcter, submission)).thenReturn(true);
            when(scoreSubmissionPolicy.canCorrect(submission)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> scoreSubmissionCommandService.correctSubmissionStatus(submissionId, correcterId))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.CORRECTION_LIMIT_EXCEEDED);
                    });

            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(userRepositoryPort).findById(correcterId);
            verify(scoreSubmissionPolicy).canApprove(correcter, submission);
            verify(scoreSubmissionPolicy).canCorrect(submission);
            verify(scoreSubmissionRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("점수 신청 삭제 시")
    class DeleteSubmissionTests {

        @Test
        @DisplayName("본인이 신청한 PENDING 상태의 점수를 삭제할 수 있다")
        void deleteSubmission_OwnPendingSubmission_Success() {
            // given
            Long submissionId = 1L;
            Long userId = 1L;

            User user = DomainUserFactory.buildValidUserWithId(userId);
            Lab lab = DomainLabFactory.buildValidLabWithId(1L);
            ScoreSubmission submission = DomainScoreSubmissionFactory.builder()
                    .id(submissionId)
                    .user(user)
                    .lab(lab)
                    .status(SubmissionStatus.PENDING)
                    .build();

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));

            // when
            scoreSubmissionCommandService.deleteSubmission(submissionId, userId);

            // then
            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(scoreSubmissionRepositoryPort).delete(submission);
        }

        @Test
        @DisplayName("본인이 아닌 사용자가 점수 삭제 시 예외가 발생한다")
        void deleteSubmission_NotOwner_ThrowsException() {
            // given
            Long submissionId = 1L;
            Long userId = 2L; // 다른 사용자

            User owner = DomainUserFactory.buildValidUserWithId(1L);
            Lab lab = DomainLabFactory.buildValidLabWithId(1L);
            ScoreSubmission submission = DomainScoreSubmissionFactory.builder()
                    .id(submissionId)
                    .user(owner)
                    .lab(lab)
                    .status(SubmissionStatus.PENDING)
                    .build();

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));

            // when & then
            assertThatThrownBy(() -> scoreSubmissionCommandService.deleteSubmission(submissionId, userId))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.NOT_OWNER);
                    });

            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(scoreSubmissionRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("이미 처리된 점수 삭제 시 예외가 발생한다")
        void deleteSubmission_AlreadyProcessed_ThrowsException() {
            // given
            Long submissionId = 1L;
            Long userId = 1L;

            User user = DomainUserFactory.buildValidUserWithId(userId);
            Lab lab = DomainLabFactory.buildValidLabWithId(1L);
            ScoreSubmission submission = DomainScoreSubmissionFactory.builder()
                    .id(submissionId)
                    .user(user)
                    .lab(lab)
                    .status(SubmissionStatus.APPROVED)
                    .build();

            when(scoreSubmissionRepositoryPort.findById(submissionId)).thenReturn(Optional.of(submission));

            // when & then
            assertThatThrownBy(() -> scoreSubmissionCommandService.deleteSubmission(submissionId, userId))
                    .isInstanceOf(RankingValidationException.class)
                    .satisfies(ex -> {
                        RankingValidationException exception = (RankingValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.ALREADY_PROCESSED);
                    });

            verify(scoreSubmissionRepositoryPort).findById(submissionId);
            verify(scoreSubmissionRepositoryPort, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("만료된 점수 신청 처리 시")
    class ProcessExpiredSubmissionsTests {

        @Test
        @DisplayName("만료된 점수 신청들을 자동으로 거부할 수 있다")
        void processExpiredSubmissions_AutoRejectExpired_Success() {
            // given
            ScoreSubmission expiredSubmission1 = DomainScoreSubmissionFactory.buildExpiredSubmission();
            ScoreSubmission expiredSubmission2 = DomainScoreSubmissionFactory.buildExpiredSubmission();
            List<ScoreSubmission> expiredSubmissions = List.of(expiredSubmission1, expiredSubmission2);

            when(scoreSubmissionRepositoryPort.findByStatusAndExpiresAtBefore(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class)))
                    .thenReturn(expiredSubmissions);
            when(scoreSubmissionPolicy.shouldAutoReject(any(ScoreSubmission.class))).thenReturn(true);
            when(scoreSubmissionRepositoryPort.save(any(ScoreSubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            scoreSubmissionCommandService.processExpiredSubmissions();

            // then
            verify(scoreSubmissionRepositoryPort).findByStatusAndExpiresAtBefore(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class));
            verify(scoreSubmissionPolicy, times(2)).shouldAutoReject(any(ScoreSubmission.class));
            verify(scoreSubmissionRepositoryPort, times(2)).save(any(ScoreSubmission.class));
        }

        @Test
        @DisplayName("만료되었지만 자동 거부 대상이 아닌 점수는 처리하지 않는다")
        void processExpiredSubmissions_NotAutoRejectTarget_DoesNotProcess() {
            // given
            ScoreSubmission expiredSubmission = DomainScoreSubmissionFactory.buildExpiredSubmission();
            List<ScoreSubmission> expiredSubmissions = List.of(expiredSubmission);

            when(scoreSubmissionRepositoryPort.findByStatusAndExpiresAtBefore(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class)))
                    .thenReturn(expiredSubmissions);
            when(scoreSubmissionPolicy.shouldAutoReject(expiredSubmission)).thenReturn(false);

            // when
            scoreSubmissionCommandService.processExpiredSubmissions();

            // then
            verify(scoreSubmissionRepositoryPort).findByStatusAndExpiresAtBefore(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class));
            verify(scoreSubmissionPolicy).shouldAutoReject(expiredSubmission);
            verify(scoreSubmissionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("만료된 점수 신청이 없을 때 처리하지 않는다")
        void processExpiredSubmissions_NoExpiredSubmissions_DoesNotProcess() {
            // given
            when(scoreSubmissionRepositoryPort.findByStatusAndExpiresAtBefore(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class)))
                    .thenReturn(List.of());

            // when
            scoreSubmissionCommandService.processExpiredSubmissions();

            // then
            verify(scoreSubmissionRepositoryPort).findByStatusAndExpiresAtBefore(
                    eq(SubmissionStatus.PENDING), any(LocalDateTime.class));
            verify(scoreSubmissionPolicy, never()).shouldAutoReject(any());
            verify(scoreSubmissionRepositoryPort, never()).save(any());
        }
    }
}