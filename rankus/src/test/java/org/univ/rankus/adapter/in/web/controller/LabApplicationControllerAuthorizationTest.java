package org.univ.rankus.adapter.in.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.univ.rankus.application.port.in.command.LabApplicationCommandUseCase;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.exception.LabApplicationErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabApplicationValidationException;
import org.univ.rankus.domain.model.user.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * LabApplicationController 단위 테스트
 * - 각 컨트롤러 메서드의 기능 검증
 * - HTTP 응답 및 상태 코드 검증
 */
@ExtendWith(MockitoExtension.class)
class LabApplicationControllerAuthorizationTest {

    @Mock
    private LabApplicationQueryUseCase queryUseCase;

    @Mock
    private LabApplicationCommandUseCase commandUseCase;

    @InjectMocks
    private LabApplicationController controller;

    private static final Long LAB_ID = 1L;
    private static final Long APP_ID = 100L;
    private static final Long USER_ID = 200L;
    private static final Long OTHER_USER_ID = 300L;
    private static final Long ADMIN_USER_ID = 999L;

    @Nested
    @DisplayName("DELETE /api/labs/{labId}/applications/{appId} - 가입 신청 취소")
    class CancelApplication {

        @Test
        @DisplayName("가입 신청을 성공적으로 취소")
        void cancelApplicationSuccess() {
            // Given
            CustomUserDetails mockUserDetails = mock(CustomUserDetails.class);
            when(mockUserDetails.getUserId()).thenReturn(USER_ID);
            doNothing().when(commandUseCase).cancelApplication(APP_ID, USER_ID);

            // When
            ResponseEntity<Void> response = controller.cancelApplication(LAB_ID, APP_ID, mockUserDetails);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).cancelApplication(APP_ID, USER_ID);
        }

        @Test
        @DisplayName("ADMIN이 다른 사용자의 신청서를 취소할 수 있다")
        void adminCanCancelOthersApplication() {
            // Given
            CustomUserDetails adminUserDetails = mock(CustomUserDetails.class);
            when(adminUserDetails.getUserId()).thenReturn(ADMIN_USER_ID);
            doNothing().when(commandUseCase).cancelApplication(APP_ID, ADMIN_USER_ID);

            // When
            ResponseEntity<Void> response = controller.cancelApplication(LAB_ID, APP_ID, adminUserDetails);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).cancelApplication(APP_ID, ADMIN_USER_ID);
        }

        @Test
        @DisplayName("일반 사용자가 다른 사용자의 신청서를 취소하려 하면 권한 오류 발생")
        void nonAdminCannotCancelOthersApplication() {
            // Given
            CustomUserDetails otherUserDetails = mock(CustomUserDetails.class);
            when(otherUserDetails.getUserId()).thenReturn(OTHER_USER_ID);
            doThrow(new LabApplicationValidationException(LabApplicationErrorCode.UNAUTHORIZED_CANCEL_ATTEMPT))
                    .when(commandUseCase).cancelApplication(APP_ID, OTHER_USER_ID);

            // When & Then
            assertThatThrownBy(() -> controller.cancelApplication(LAB_ID, APP_ID, otherUserDetails))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e = (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(LabApplicationErrorCode.UNAUTHORIZED_CANCEL_ATTEMPT);
                    });

            verify(commandUseCase).cancelApplication(APP_ID, OTHER_USER_ID);
        }
    }

    @Nested
    @DisplayName("PUT /api/labs/{labId}/applications/{appId}/approve - 가입 신청 승인")
    class ApproveApplication {
        @Test
        @DisplayName("가입 신청을 성공적으로 승인")
        void approveApplicationSuccess() {
            // Given
            doNothing().when(commandUseCase).approveApplication(APP_ID);

            // When
            ResponseEntity<Void> response = controller.approveApplication(LAB_ID, APP_ID);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).approveApplication(APP_ID);
        }
    }

    @Nested
    @DisplayName("권한 기반 DELETE 테스트 - 종합 시나리오")
    class DeletePermissionTests {

        @Test
        @DisplayName("ADMIN은 모든 사용자의 지원서를 삭제할 수 있다")
        void adminCanDeleteAnyApplication() {
            // Given
            CustomUserDetails adminDetails = createMockUserDetails(ADMIN_USER_ID, Role.ADMIN);
            doNothing().when(commandUseCase).cancelApplication(APP_ID, ADMIN_USER_ID);

            // When
            ResponseEntity<Void> response = controller.cancelApplication(LAB_ID, APP_ID, adminDetails);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).cancelApplication(APP_ID, ADMIN_USER_ID);
        }

        @Test
        @DisplayName("STUDENT는 자신의 지원서만 삭제할 수 있다")
        void studentCanOnlyDeleteOwnApplication() {
            // Given
            CustomUserDetails studentDetails = createMockUserDetails(USER_ID, Role.STUDENT);
            doNothing().when(commandUseCase).cancelApplication(APP_ID, USER_ID);

            // When
            ResponseEntity<Void> response = controller.cancelApplication(LAB_ID, APP_ID, studentDetails);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).cancelApplication(APP_ID, USER_ID);
        }

        @Test
        @DisplayName("LAB_MEMBER는 자신의 지원서만 삭제할 수 있다")
        void labMemberCanOnlyDeleteOwnApplication() {
            // Given
            CustomUserDetails labMemberDetails = createMockUserDetails(USER_ID, Role.LAB_MEMBER);
            doNothing().when(commandUseCase).cancelApplication(APP_ID, USER_ID);

            // When
            ResponseEntity<Void> response = controller.cancelApplication(LAB_ID, APP_ID, labMemberDetails);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).cancelApplication(APP_ID, USER_ID);
        }

        @Test
        @DisplayName("LAB_MANAGER는 자신의 지원서만 삭제할 수 있다")
        void labManagerCanOnlyDeleteOwnApplication() {
            // Given
            CustomUserDetails labManagerDetails = createMockUserDetails(USER_ID, Role.LAB_MANAGER);
            doNothing().when(commandUseCase).cancelApplication(APP_ID, USER_ID);

            // When
            ResponseEntity<Void> response = controller.cancelApplication(LAB_ID, APP_ID, labManagerDetails);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).cancelApplication(APP_ID, USER_ID);
        }

        @Test
        @DisplayName("LAB_LEADER는 자신의 지원서만 삭제할 수 있다")
        void labLeaderCanOnlyDeleteOwnApplication() {
            // Given
            CustomUserDetails labLeaderDetails = createMockUserDetails(USER_ID, Role.LAB_LEADER);
            doNothing().when(commandUseCase).cancelApplication(APP_ID, USER_ID);

            // When
            ResponseEntity<Void> response = controller.cancelApplication(LAB_ID, APP_ID, labLeaderDetails);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).cancelApplication(APP_ID, USER_ID);
        }

        @Test
        @DisplayName("PROFESSOR는 자신의 지원서만 삭제할 수 있다")
        void professorCanOnlyDeleteOwnApplication() {
            // Given
            CustomUserDetails professorDetails = createMockUserDetails(USER_ID, Role.PROFESSOR);
            doNothing().when(commandUseCase).cancelApplication(APP_ID, USER_ID);

            // When
            ResponseEntity<Void> response = controller.cancelApplication(LAB_ID, APP_ID, professorDetails);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).cancelApplication(APP_ID, USER_ID);
        }

        @Test
        @DisplayName("일반 사용자가 다른 사용자의 지원서 삭제 시도시 예외 발생")
        void nonAdminCannotDeleteOthersApplicationThrowsException() {
            // Given
            CustomUserDetails studentDetails = createMockUserDetails(OTHER_USER_ID, Role.STUDENT);
            doThrow(new LabApplicationValidationException(LabApplicationErrorCode.UNAUTHORIZED_CANCEL_ATTEMPT))
                    .when(commandUseCase).cancelApplication(APP_ID, OTHER_USER_ID);

            // When & Then
            assertThatThrownBy(() -> controller.cancelApplication(LAB_ID, APP_ID, studentDetails))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e = (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(LabApplicationErrorCode.UNAUTHORIZED_CANCEL_ATTEMPT);
                    });

            verify(commandUseCase).cancelApplication(APP_ID, OTHER_USER_ID);
        }

        private CustomUserDetails createMockUserDetails(Long userId, Role role) {
            CustomUserDetails userDetails = mock(CustomUserDetails.class);
            when(userDetails.getUserId()).thenReturn(userId);
            return userDetails;
        }
    }

    @Nested
    @DisplayName("PUT /api/labs/{labId}/applications/{appId}/reject - 가입 신청 거절")
    class RejectApplication {
        @Test
        @DisplayName("가입 신청을 성공적으로 거절")
        void rejectApplicationSuccess() {
            // Given
            doNothing().when(commandUseCase).rejectApplication(APP_ID);

            // When
            ResponseEntity<Void> response = controller.rejectApplication(LAB_ID, APP_ID);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).rejectApplication(APP_ID);
        }
    }
}
