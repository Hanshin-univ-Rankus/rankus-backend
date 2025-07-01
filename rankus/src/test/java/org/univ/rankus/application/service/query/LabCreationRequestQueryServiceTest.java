package org.univ.rankus.application.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.LabCreationRequestRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.LabCreationRequest;
import org.univ.rankus.domain.model.lab.LabCreationStatus;
import org.univ.rankus.domain.model.lab.exception.LabCreationRequestNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainLabCreationRequestFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LabCreationRequestQueryService 테스트")
class LabCreationRequestQueryServiceTest {

    @Mock
    private LabCreationRequestRepositoryPort labCreationRequestRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private LabCreationRequestQueryService labCreationRequestQueryService;

    @Nested
    @DisplayName("getAllLabCreationRequests 메서드는")
    class GetAllLabCreationRequestsTests {

        @Test
        @DisplayName("모든 랩실 생성 신청서를 생성일 내림차순으로 조회한다")
        void getAllLabCreationRequests_success() {
            // given
            List<LabCreationRequest> requests = Arrays.asList(
                    DomainLabCreationRequestFactory.buildValidPendingRequest(),
                    DomainLabCreationRequestFactory.buildApprovedRequestWithApprover(),
                    DomainLabCreationRequestFactory.buildRejectedRequestWithRejector()
            );

            when(labCreationRequestRepositoryPort.findAllByCreatedAtDesc()).thenReturn(requests);

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getAllLabCreationRequests();

            // then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyElementsOf(requests);

            verify(labCreationRequestRepositoryPort).findAllByCreatedAtDesc();
        }

        @Test
        @DisplayName("신청서가 없을 때 빈 리스트를 반환한다")
        void getAllLabCreationRequests_emptyList() {
            // given
            when(labCreationRequestRepositoryPort.findAllByCreatedAtDesc()).thenReturn(Collections.emptyList());

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getAllLabCreationRequests();

            // then
            assertThat(result).isEmpty();

            verify(labCreationRequestRepositoryPort).findAllByCreatedAtDesc();
        }
    }

    @Nested
    @DisplayName("getLabCreationRequestsByStatus 메서드는")
    class GetLabCreationRequestsByStatusTests {

        @Test
        @DisplayName("PENDING 상태의 신청서들을 생성일 내림차순으로 조회한다")
        void getLabCreationRequestsByStatus_pending_success() {
            // given
            LabCreationStatus status = LabCreationStatus.PENDING;
            List<LabCreationRequest> pendingRequests = Arrays.asList(
                    DomainLabCreationRequestFactory.buildValidPendingRequest(),
                    DomainLabCreationRequestFactory.buildRequestWithCategory(org.univ.rankus.domain.model.lab.LabCategory.DB)
            );

            when(labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(status)).thenReturn(pendingRequests);

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getLabCreationRequestsByStatus(status);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(pendingRequests);

            verify(labCreationRequestRepositoryPort).findByStatusOrderByCreatedAtDesc(status);
        }

        @Test
        @DisplayName("APPROVED 상태의 신청서들을 조회한다")
        void getLabCreationRequestsByStatus_approved_success() {
            // given
            LabCreationStatus status = LabCreationStatus.APPROVED;
            List<LabCreationRequest> approvedRequests = Arrays.asList(
                    DomainLabCreationRequestFactory.buildApprovedRequestWithApprover()
            );

            when(labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(status)).thenReturn(approvedRequests);

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getLabCreationRequestsByStatus(status);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactlyElementsOf(approvedRequests);

            verify(labCreationRequestRepositoryPort).findByStatusOrderByCreatedAtDesc(status);
        }

        @Test
        @DisplayName("REJECTED 상태의 신청서들을 조회한다")
        void getLabCreationRequestsByStatus_rejected_success() {
            // given
            LabCreationStatus status = LabCreationStatus.REJECTED;
            List<LabCreationRequest> rejectedRequests = Arrays.asList(
                    DomainLabCreationRequestFactory.buildRejectedRequestWithRejector()
            );

            when(labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(status)).thenReturn(rejectedRequests);

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getLabCreationRequestsByStatus(status);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactlyElementsOf(rejectedRequests);

            verify(labCreationRequestRepositoryPort).findByStatusOrderByCreatedAtDesc(status);
        }

        @Test
        @DisplayName("해당 상태의 신청서가 없을 때 빈 리스트를 반환한다")
        void getLabCreationRequestsByStatus_emptyList() {
            // given
            LabCreationStatus status = LabCreationStatus.PENDING;
            when(labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(status)).thenReturn(Collections.emptyList());

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getLabCreationRequestsByStatus(status);

            // then
            assertThat(result).isEmpty();

            verify(labCreationRequestRepositoryPort).findByStatusOrderByCreatedAtDesc(status);
        }
    }

    @Nested
    @DisplayName("getLabCreationRequestsByRequester 메서드는")
    class GetLabCreationRequestsByRequesterTests {

        @Test
        @DisplayName("특정 신청자의 모든 신청서를 생성일 내림차순으로 조회한다")
        void getLabCreationRequestsByRequester_success() {
            // given
            Long requesterId = 1L;
            User requester = DomainUserFactory.buildValidUserWithId(requesterId);
            List<LabCreationRequest> requesterRequests = Arrays.asList(
                    DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester),
                    DomainLabCreationRequestFactory.buildApprovedRequest(DomainUserFactory.buildProfessorUser())
            );

            when(userRepositoryPort.findById(requesterId)).thenReturn(Optional.of(requester));
            when(labCreationRequestRepositoryPort.findByRequesterOrderByCreatedAtDesc(requester)).thenReturn(requesterRequests);

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getLabCreationRequestsByRequester(requesterId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(requesterRequests);

            verify(userRepositoryPort).findById(requesterId);
            verify(labCreationRequestRepositoryPort).findByRequesterOrderByCreatedAtDesc(requester);
        }

        @Test
        @DisplayName("존재하지 않는 신청자 ID로 조회 시 UserNotFoundException을 던진다")
        void getLabCreationRequestsByRequester_nonExistentUser_throwsUserNotFoundException() {
            // given
            Long nonExistentRequesterId = 999L;
            when(userRepositoryPort.findById(nonExistentRequesterId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> labCreationRequestQueryService.getLabCreationRequestsByRequester(nonExistentRequesterId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException exception = (UserNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(nonExistentRequesterId);
            verify(labCreationRequestRepositoryPort, never()).findByRequesterOrderByCreatedAtDesc(any());
        }

        @Test
        @DisplayName("신청자가 신청한 신청서가 없을 때 빈 리스트를 반환한다")
        void getLabCreationRequestsByRequester_emptyList() {
            // given
            Long requesterId = 1L;
            User requester = DomainUserFactory.buildValidUserWithId(requesterId);

            when(userRepositoryPort.findById(requesterId)).thenReturn(Optional.of(requester));
            when(labCreationRequestRepositoryPort.findByRequesterOrderByCreatedAtDesc(requester)).thenReturn(Collections.emptyList());

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getLabCreationRequestsByRequester(requesterId);

            // then
            assertThat(result).isEmpty();

            verify(userRepositoryPort).findById(requesterId);
            verify(labCreationRequestRepositoryPort).findByRequesterOrderByCreatedAtDesc(requester);
        }
    }

    @Nested
    @DisplayName("getLabCreationRequestById 메서드는")
    class GetLabCreationRequestByIdTests {

        @Test
        @DisplayName("유효한 ID로 신청서를 조회한다")
        void getLabCreationRequestById_success() {
            // given
            Long requestId = 1L;
            LabCreationRequest request = DomainLabCreationRequestFactory.buildValidPendingRequestWithId(requestId);

            when(labCreationRequestRepositoryPort.findById(requestId)).thenReturn(Optional.of(request));

            // when
            LabCreationRequest result = labCreationRequestQueryService.getLabCreationRequestById(requestId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(request);

            verify(labCreationRequestRepositoryPort).findById(requestId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 LabCreationRequestNotFoundException을 던진다")
        void getLabCreationRequestById_nonExistentId_throwsNotFoundException() {
            // given
            Long nonExistentRequestId = 999L;
            when(labCreationRequestRepositoryPort.findById(nonExistentRequestId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> labCreationRequestQueryService.getLabCreationRequestById(nonExistentRequestId))
                    .isInstanceOf(LabCreationRequestNotFoundException.class);

            verify(labCreationRequestRepositoryPort).findById(nonExistentRequestId);
        }
    }

    @Nested
    @DisplayName("getPendingLabCreationRequests 메서드는")
    class GetPendingLabCreationRequestsTests {

        @Test
        @DisplayName("대기 중인 신청서들을 생성일 내림차순으로 조회한다")
        void getPendingLabCreationRequests_success() {
            // given
            List<LabCreationRequest> pendingRequests = Arrays.asList(
                    DomainLabCreationRequestFactory.buildValidPendingRequest(),
                    DomainLabCreationRequestFactory.buildRequestWithCategory(org.univ.rankus.domain.model.lab.LabCategory.SECURITY),
                    DomainLabCreationRequestFactory.buildAiLabRequest()
            );

            when(labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(LabCreationStatus.PENDING)).thenReturn(pendingRequests);

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getPendingLabCreationRequests();

            // then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyElementsOf(pendingRequests);

            // 모든 신청서가 PENDING 상태인지 확인
            assertThat(result).allSatisfy(request ->
                    assertThat(request.getStatus()).isEqualTo(LabCreationStatus.PENDING)
            );

            verify(labCreationRequestRepositoryPort).findByStatusOrderByCreatedAtDesc(LabCreationStatus.PENDING);
        }

        @Test
        @DisplayName("대기 중인 신청서가 없을 때 빈 리스트를 반환한다")
        void getPendingLabCreationRequests_emptyList() {
            // given
            when(labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(LabCreationStatus.PENDING)).thenReturn(Collections.emptyList());

            // when
            List<LabCreationRequest> result = labCreationRequestQueryService.getPendingLabCreationRequests();

            // then
            assertThat(result).isEmpty();

            verify(labCreationRequestRepositoryPort).findByStatusOrderByCreatedAtDesc(LabCreationStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("조회 메서드 통합 테스트")
    class IntegrationTests {

        @Test
        @DisplayName("다양한 상태의 신청서들에 대한 조회가 올바르게 동작한다")
        void multipleStatusQueries_success() {
            // given
            User requester1 = DomainUserFactory.buildValidUserWithId(1L);
            User requester2 = DomainUserFactory.buildValidUserWithId(2L);
            User approver = DomainUserFactory.buildProfessorUser();

            LabCreationRequest pendingRequest1 = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester1);
            LabCreationRequest pendingRequest2 = DomainLabCreationRequestFactory.buildValidPendingRequestWithRequester(requester2);
            LabCreationRequest approvedRequest = DomainLabCreationRequestFactory.buildApprovedRequest(approver);

            List<LabCreationRequest> allRequests = Arrays.asList(pendingRequest1, pendingRequest2, approvedRequest);
            List<LabCreationRequest> pendingRequests = Arrays.asList(pendingRequest1, pendingRequest2);
            List<LabCreationRequest> approvedRequests = Arrays.asList(approvedRequest);
            List<LabCreationRequest> requester1Requests = Arrays.asList(pendingRequest1);

            // Mock 설정
            when(labCreationRequestRepositoryPort.findAllByCreatedAtDesc()).thenReturn(allRequests);
            when(labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(LabCreationStatus.PENDING)).thenReturn(pendingRequests);
            when(labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(LabCreationStatus.APPROVED)).thenReturn(approvedRequests);
            when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(requester1));
            when(labCreationRequestRepositoryPort.findByRequesterOrderByCreatedAtDesc(requester1)).thenReturn(requester1Requests);

            // when & then
            List<LabCreationRequest> allResult = labCreationRequestQueryService.getAllLabCreationRequests();
            assertThat(allResult).hasSize(3);

            List<LabCreationRequest> pendingResult = labCreationRequestQueryService.getLabCreationRequestsByStatus(LabCreationStatus.PENDING);
            assertThat(pendingResult).hasSize(2);

            List<LabCreationRequest> approvedResult = labCreationRequestQueryService.getLabCreationRequestsByStatus(LabCreationStatus.APPROVED);
            assertThat(approvedResult).hasSize(1);

            List<LabCreationRequest> requester1Result = labCreationRequestQueryService.getLabCreationRequestsByRequester(1L);
            assertThat(requester1Result).hasSize(1);

            // Mock 호출 검증
            verify(labCreationRequestRepositoryPort).findAllByCreatedAtDesc();
            verify(labCreationRequestRepositoryPort, times(2)).findByStatusOrderByCreatedAtDesc(any());
            verify(userRepositoryPort).findById(1L);
            verify(labCreationRequestRepositoryPort).findByRequesterOrderByCreatedAtDesc(requester1);
        }
    }
}