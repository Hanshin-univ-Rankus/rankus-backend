package org.univ.rankus.adapter.in.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.univ.rankus.application.port.in.command.LabApplicationCommandUseCase;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Nested
    @DisplayName("DELETE /api/labs/{labId}/applications/{appId} - 가입 신청 취소")
    class CancelApplication {

        private CustomUserDetails mockUserDetails;

        @BeforeEach
        void setUp() {
            // cancelApplication 테스트에서만 사용되는 인증 설정
            mockUserDetails = mock(CustomUserDetails.class);
            when(mockUserDetails.getUserId()).thenReturn(USER_ID);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(mockUserDetails, null, Collections.emptyList())
            );
        }

        @Test
        @DisplayName("가입 신청을 성공적으로 취소")
        void cancelApplicationSuccess() {
            // Given
            doNothing().when(commandUseCase).cancelApplication(APP_ID, USER_ID);

            // When
            ResponseEntity<Void> response = controller.cancelApplication(LAB_ID, APP_ID, mockUserDetails);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(commandUseCase).cancelApplication(APP_ID, USER_ID);
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
