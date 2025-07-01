package org.univ.rankus.application.service.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.LabCreationRequestRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.lab.LabCreationRequest;
import org.univ.rankus.domain.model.lab.LabCreationStatus;
import org.univ.rankus.domain.model.lab.exception.LabCreationRequestErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabCreationRequestNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabCreationRequestValidationException;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainLabCreationRequestFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LabCreationRequestCommandService 테스트")
class LabCreationRequestCommandServiceTest {

    @Mock
    private LabCreationRequestRepositoryPort labCreationRequestRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @InjectMocks
    private LabCreationRequestCommandService labCreationRequestCommandService;

    @Nested
    @DisplayName("createLabCreationRequest 메서드는")
    class CreateLabCreationRequestTests {

        @Test
        @DisplayName("유효한 정보로 랩실 생성 신청서를 생성한다")
        void createLabCreationRequest_validInput_success() {
            // given
            String requestedLabName = "AI Lab";
            LabCategory requestedCategory = LabCategory.AI;
            String requestedDescription = "AI research lab";
            Long requesterId = 1L;

            User requester = DomainUserFactory.buildStudentUser();
            LabCreationRequest savedRequest = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);

            when(userRepositoryPort.findById(requesterId)).thenReturn(Optional.of(requester));
            when(labCreationRequestRepositoryPort.existsByRequesterAndRequestedLabNameAndStatus(
                    requester, requestedLabName, LabCreationStatus.PENDING)).thenReturn(false);
            when(labCreationRequestRepositoryPort.existsByRequestedLabNameAndStatus(
                    requestedLabName, LabCreationStatus.PENDING)).thenReturn(false);
            when(labCreationRequestRepositoryPort.save(any(LabCreationRequest.class))).thenReturn(savedRequest);

            // when
            LabCreationRequest result = labCreationRequestCommandService.createLabCreationRequest(
                    requestedLabName, requestedCategory, requestedDescription, requesterId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(LabCreationStatus.PENDING);

            // 저장된 요청 검증
            ArgumentCaptor<LabCreationRequest> requestCaptor = ArgumentCaptor.forClass(LabCreationRequest.class);
            verify(labCreationRequestRepositoryPort).save(requestCaptor.capture());

            LabCreationRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.getRequestedLabName()).isEqualTo(requestedLabName);
            assertThat(capturedRequest.getRequestedCategory()).isEqualTo(requestedCategory);
            assertThat(capturedRequest.getRequestedDescription()).isEqualTo(requestedDescription);
            assertThat(capturedRequest.getRequester()).isEqualTo(requester);

            verify(userRepositoryPort).findById(requesterId);
            verify(labCreationRequestRepositoryPort).existsByRequesterAndRequestedLabNameAndStatus(
                    requester, requestedLabName, LabCreationStatus.PENDING);
            verify(labCreationRequestRepositoryPort).existsByRequestedLabNameAndStatus(
                    requestedLabName, LabCreationStatus.PENDING);
        }

        @Test
        @DisplayName("존재하지 않는 신청자 ID로 요청 시 UserNotFoundException을 던진다")
        void createLabCreationRequest_nonExistentRequester_throwsUserNotFoundException() {
            // given
            Long nonExistentRequesterId = 999L;
            when(userRepositoryPort.findById(nonExistentRequesterId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.createLabCreationRequest(
                    "TestLab", LabCategory.AI, "Description", nonExistentRequesterId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException exception = (UserNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(nonExistentRequesterId);
            verify(labCreationRequestRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("동일한 신청자가 동일한 랩실 이름으로 대기 중인 신청이 있을 때 DUPLICATE_LAB_NAME_REQUEST 예외를 던진다")
        void createLabCreationRequest_duplicateByRequester_throwsDuplicateException() {
            // given
            String requestedLabName = "AI Lab";
            Long requesterId = 1L;
            User requester = DomainUserFactory.buildStudentUser();

            when(userRepositoryPort.findById(requesterId)).thenReturn(Optional.of(requester));
            when(labCreationRequestRepositoryPort.existsByRequesterAndRequestedLabNameAndStatus(
                    requester, requestedLabName, LabCreationStatus.PENDING)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.createLabCreationRequest(
                    requestedLabName, LabCategory.AI, "Description", requesterId))
                    .isInstanceOf(LabCreationRequestValidationException.class)
                    .satisfies(ex -> {
                        LabCreationRequestValidationException exception = (LabCreationRequestValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabCreationRequestErrorCode.DUPLICATE_LAB_NAME_REQUEST);
                    });

            verify(userRepositoryPort).findById(requesterId);
            verify(labCreationRequestRepositoryPort).existsByRequesterAndRequestedLabNameAndStatus(
                    requester, requestedLabName, LabCreationStatus.PENDING);
            verify(labCreationRequestRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("동일한 랩실 이름으로 다른 신청자의 대기 중인 신청이 있을 때 DUPLICATE_LAB_NAME_REQUEST 예외를 던진다")
        void createLabCreationRequest_duplicateLabName_throwsDuplicateException() {
            // given
            String requestedLabName = "AI Lab";
            Long requesterId = 1L;
            User requester = DomainUserFactory.buildStudentUser();

            when(userRepositoryPort.findById(requesterId)).thenReturn(Optional.of(requester));
            when(labCreationRequestRepositoryPort.existsByRequesterAndRequestedLabNameAndStatus(
                    requester, requestedLabName, LabCreationStatus.PENDING)).thenReturn(false);
            when(labCreationRequestRepositoryPort.existsByRequestedLabNameAndStatus(
                    requestedLabName, LabCreationStatus.PENDING)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.createLabCreationRequest(
                    requestedLabName, LabCategory.AI, "Description", requesterId))
                    .isInstanceOf(LabCreationRequestValidationException.class)
                    .satisfies(ex -> {
                        LabCreationRequestValidationException exception = (LabCreationRequestValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabCreationRequestErrorCode.DUPLICATE_LAB_NAME_REQUEST);
                    });

            verify(userRepositoryPort).findById(requesterId);
            verify(labCreationRequestRepositoryPort).existsByRequesterAndRequestedLabNameAndStatus(
                    requester, requestedLabName, LabCreationStatus.PENDING);
            verify(labCreationRequestRepositoryPort).existsByRequestedLabNameAndStatus(
                    requestedLabName, LabCreationStatus.PENDING);
            verify(labCreationRequestRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("approveLabCreationRequest 메서드는")
    class ApproveLabCreationRequestTests {

        @Test
        @DisplayName("교수가 신청서를 승인하면 Lab을 생성하고 신청자를 LAB_LEADER로 설정한다")
        void approveLabCreationRequest_professorApprover_createLabAndAssignLeader() {
            // given
            Long requestId = 1L;
            Long approverId = 2L;

            User requester = DomainUserFactory.buildStudentUser();
            User approver = DomainUserFactory.buildProfessorUser();
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
            Lab savedLab = DomainLabFactory.buildValidLabWithId(1L);

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));
            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.of(approver));
            when(labRepositoryPort.save(any(Lab.class))).thenReturn(savedLab);
            when(userRepositoryPort.save(any(User.class))).thenReturn(requester);
            when(labCreationRequestRepositoryPort.save(any(LabCreationRequest.class))).thenReturn(request);

            // when
            labCreationRequestCommandService.approveLabCreationRequest(requestId, approverId);

            // then
            // Lab 생성 검증
            ArgumentCaptor<Lab> labCaptor = ArgumentCaptor.forClass(Lab.class);
            verify(labRepositoryPort).save(labCaptor.capture());

            Lab capturedLab = labCaptor.getValue();
            assertThat(capturedLab.getName()).isEqualTo(request.getRequestedLabName());
            assertThat(capturedLab.getCategory()).isEqualTo(request.getRequestedCategory());
            assertThat(capturedLab.getDescription()).isEqualTo(request.getRequestedDescription());

            // 신청자 역할 변경 및 랩실 배정 검증
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepositoryPort).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getRole()).isEqualTo(Role.LAB_LEADER);

            // 신청서 상태 저장 검증
            verify(labCreationRequestRepositoryPort).save(request);
            verify(labCreationRequestRepositoryPort).findById(requestId);
            verify(userRepositoryPort).findById(approverId);
        }

        @Test
        @DisplayName("관리자가 신청서를 승인할 수 있다")
        void approveLabCreationRequest_adminApprover_success() {
            // given
            Long requestId = 1L;
            Long approverId = 2L;

            User requester = DomainUserFactory.buildStudentUser();
            User admin = DomainUserFactory.buildValidUserWithRole(Role.ADMIN);
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
            Lab savedLab = DomainLabFactory.buildValidLabWithId(1L);

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));
            when(userRepositoryPort.findById(approverId)).thenReturn(Optional.of(admin));
            when(labRepositoryPort.save(any(Lab.class))).thenReturn(savedLab);
            when(userRepositoryPort.save(any(User.class))).thenReturn(requester);
            when(labCreationRequestRepositoryPort.save(any(LabCreationRequest.class))).thenReturn(request);

            // when
            labCreationRequestCommandService.approveLabCreationRequest(requestId, approverId);

            // then
            verify(labRepositoryPort).save(any(Lab.class));
            verify(userRepositoryPort).save(any(User.class));
            verify(labCreationRequestRepositoryPort).save(request);
        }

        @Test
        @DisplayName("존재하지 않는 신청서 ID로 승인 시 LabCreationRequestNotFoundException을 던진다")
        void approveLabCreationRequest_nonExistentRequest_throwsNotFoundException() {
            // given
            Long nonExistentRequestId = 999L;
            Long approverId = 1L;

            when(labCreationRequestRepositoryPort.findById(nonExistentRequestId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.approveLabCreationRequest(
                    nonExistentRequestId, approverId))
                    .isInstanceOf(LabCreationRequestNotFoundException.class);

            verify(labCreationRequestRepositoryPort).findById(nonExistentRequestId);
            verify(userRepositoryPort, never()).findById(any());
            verify(labRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 승인자 ID로 승인 시 UserNotFoundException을 던진다")
        void approveLabCreationRequest_nonExistentApprover_throwsUserNotFoundException() {
            // given
            Long requestId = 1L;
            Long nonExistentApproverId = 999L;

            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));
            when(userRepositoryPort.findById(nonExistentApproverId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.approveLabCreationRequest(
                    requestId, nonExistentApproverId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException exception = (UserNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(labCreationRequestRepositoryPort).findById(requestId);
            verify(userRepositoryPort).findById(nonExistentApproverId);
            verify(labRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("rejectLabCreationRequest 메서드는")
    class RejectLabCreationRequestTests {

        @Test
        @DisplayName("교수가 신청서를 거절하면 REJECTED 상태로 변경된다")
        void rejectLabCreationRequest_professorRejector_success() {
            // given
            Long requestId = 1L;
            Long rejectorId = 2L;
            String rejectionReason = "연구 방향이 맞지 않음";

            User rejector = DomainUserFactory.buildProfessorUser();
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));
            when(userRepositoryPort.findById(rejectorId)).thenReturn(Optional.of(rejector));
            when(labCreationRequestRepositoryPort.save(any(LabCreationRequest.class))).thenReturn(request);

            // when
            labCreationRequestCommandService.rejectLabCreationRequest(requestId, rejectorId, rejectionReason);

            // then
            verify(labCreationRequestRepositoryPort).findById(requestId);
            verify(userRepositoryPort).findById(rejectorId);
            verify(labCreationRequestRepositoryPort).save(request);
        }

        @Test
        @DisplayName("관리자가 신청서를 거절할 수 있다")
        void rejectLabCreationRequest_adminRejector_success() {
            // given
            Long requestId = 1L;
            Long rejectorId = 2L;
            String rejectionReason = "정책 위반";

            User admin = DomainUserFactory.buildValidUserWithRole(Role.ADMIN);
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));
            when(userRepositoryPort.findById(rejectorId)).thenReturn(Optional.of(admin));
            when(labCreationRequestRepositoryPort.save(any(LabCreationRequest.class))).thenReturn(request);

            // when
            labCreationRequestCommandService.rejectLabCreationRequest(requestId, rejectorId, rejectionReason);

            // then
            verify(labCreationRequestRepositoryPort).findById(requestId);
            verify(userRepositoryPort).findById(rejectorId);
            verify(labCreationRequestRepositoryPort).save(request);
        }

        @Test
        @DisplayName("존재하지 않는 신청서 ID로 거절 시 LabCreationRequestNotFoundException을 던진다")
        void rejectLabCreationRequest_nonExistentRequest_throwsNotFoundException() {
            // given
            Long nonExistentRequestId = 999L;
            Long rejectorId = 1L;
            String rejectionReason = "사유";

            when(labCreationRequestRepositoryPort.findById(nonExistentRequestId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.rejectLabCreationRequest(
                    nonExistentRequestId, rejectorId, rejectionReason))
                    .isInstanceOf(LabCreationRequestNotFoundException.class);

            verify(labCreationRequestRepositoryPort).findById(nonExistentRequestId);
            verify(userRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("존재하지 않는 거절자 ID로 거절 시 UserNotFoundException을 던진다")
        void rejectLabCreationRequest_nonExistentRejector_throwsUserNotFoundException() {
            // given
            Long requestId = 1L;
            Long nonExistentRejectorId = 999L;
            String rejectionReason = "사유";

            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));
            when(userRepositoryPort.findById(nonExistentRejectorId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.rejectLabCreationRequest(
                    requestId, nonExistentRejectorId, rejectionReason))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException exception = (UserNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(labCreationRequestRepositoryPort).findById(requestId);
            verify(userRepositoryPort).findById(nonExistentRejectorId);
        }
    }

    @Nested
    @DisplayName("cancelLabCreationRequest 메서드는")
    class CancelLabCreationRequestTests {

        @Test
        @DisplayName("신청자가 PENDING 상태의 신청서를 취소할 수 있다")
        void cancelLabCreationRequest_ownerPendingRequest_success() {
            // given
            Long requestId = 1L;
            Long requesterId = 2L;

            User requester = DomainUserFactory.buildValidUserWithId(requesterId);
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));
            when(userRepositoryPort.findById(requesterId)).thenReturn(Optional.of(requester));

            // when
            labCreationRequestCommandService.cancelLabCreationRequest(requestId, requesterId);

            // then
            verify(labCreationRequestRepositoryPort).findById(requestId);
            verify(userRepositoryPort).findById(requesterId);
            verify(labCreationRequestRepositoryPort).delete(request);
        }

        @Test
        @DisplayName("존재하지 않는 신청서 ID로 취소 시 LabCreationRequestNotFoundException을 던진다")
        void cancelLabCreationRequest_nonExistentRequest_throwsNotFoundException() {
            // given
            Long nonExistentRequestId = 999L;
            Long requesterId = 1L;

            when(labCreationRequestRepositoryPort.findById(nonExistentRequestId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.cancelLabCreationRequest(
                    nonExistentRequestId, requesterId))
                    .isInstanceOf(LabCreationRequestNotFoundException.class);

            verify(labCreationRequestRepositoryPort).findById(nonExistentRequestId);
            verify(userRepositoryPort, never()).findById(any());
            verify(labCreationRequestRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("존재하지 않는 신청자 ID로 취소 시 UserNotFoundException을 던진다")
        void cancelLabCreationRequest_nonExistentRequester_throwsUserNotFoundException() {
            // given
            Long requestId = 1L;
            Long nonExistentRequesterId = 999L;

            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequest();

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));
            when(userRepositoryPort.findById(nonExistentRequesterId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.cancelLabCreationRequest(
                    requestId, nonExistentRequesterId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException exception = (UserNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(labCreationRequestRepositoryPort).findById(requestId);
            verify(userRepositoryPort).findById(nonExistentRequesterId);
            verify(labCreationRequestRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("신청서 소유자가 아닌 사용자가 취소 시도 시 INSUFFICIENT_PERMISSION_FOR_APPROVAL 예외를 던진다")
        void cancelLabCreationRequest_notOwner_throwsInsufficientPermissionException() {
            // given
            Long requestId = 1L;
            Long requesterId = 2L;
            Long otherUserId = 3L;

            User requester = DomainUserFactory.buildValidUserWithId(requesterId);
            User otherUser = DomainUserFactory.buildValidUserWithId(otherUserId);
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));
            when(userRepositoryPort.findById(otherUserId)).thenReturn(Optional.of(otherUser));

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.cancelLabCreationRequest(
                    requestId, otherUserId))
                    .isInstanceOf(LabCreationRequestValidationException.class)
                    .satisfies(ex -> {
                        LabCreationRequestValidationException exception = (LabCreationRequestValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabCreationRequestErrorCode.INSUFFICIENT_PERMISSION_FOR_APPROVAL);
                    });

            verify(labCreationRequestRepositoryPort).findById(requestId);
            verify(userRepositoryPort).findById(otherUserId);
            verify(labCreationRequestRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("PENDING이 아닌 상태의 신청서 취소 시도 시 CANNOT_CHANGE_STATUS_AFTER_DECISION 예외를 던진다")
        void cancelLabCreationRequest_nonPendingStatus_throwsCannotChangeStatusException() {
            // given
            Long requestId = 1L;
            Long requesterId = 2L;

            User requester = DomainUserFactory.buildValidUserWithId(requesterId);
            LabCreationRequest approvedRequest = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester);
            User professor = DomainUserFactory.buildProfessorUser();
            approvedRequest.approve(professor);
            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(approvedRequest));
            when(userRepositoryPort.findById(requesterId)).thenReturn(Optional.of(requester));

            // when & then
            assertThatThrownBy(() -> labCreationRequestCommandService.cancelLabCreationRequest(
                    requestId, requesterId))
                    .isInstanceOf(LabCreationRequestValidationException.class)
                    .satisfies(ex -> {
                        LabCreationRequestValidationException exception = (LabCreationRequestValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabCreationRequestErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION);
                    });

            verify(labCreationRequestRepositoryPort).findById(requestId);
            verify(userRepositoryPort).findById(requesterId);
            verify(labCreationRequestRepositoryPort, never()).delete(any());
        }
    }
}