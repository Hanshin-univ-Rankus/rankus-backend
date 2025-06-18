package org.univ.rankus.application.service.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.LabApplicationRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.*;
import org.univ.rankus.domain.model.lab.exception.*;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabApplicationCommandServiceTest {

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LabApplicationRepositoryPort labApplicationRepositoryPort;

    @InjectMocks
    private LabApplicationCommandService service;

    // ——————————————————————————————————————————————————————————
    // 헬퍼 메서드: 반복되는 모킹 로직을 한곳에 모았습니다.
    // ——————————————————————————————————————————————————————————

    private Lab givenExistingLab(Long labId) {
        Lab lab = mock(Lab.class);
        when(labRepositoryPort.findById(labId))
                .thenReturn(Optional.of(lab));
        return lab;
    }

    private User givenExistingUser(Long userId) {
        User user = mock(User.class);
        when(userRepositoryPort.findById(userId))
                .thenReturn(Optional.of(user));
        return user;
    }

    private LabApplication givenExistingApplication(Long appId) {
        LabApplication app = mock(LabApplication.class);
        when(labApplicationRepositoryPort.findById(appId))
                .thenReturn(Optional.of(app));
        return app;
    }

    // ——————————————————————————————————————————————————————————
    // 1) applyToLab 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("applyToLab 메서드는")
    class ApplyToLabTests {

        @Test
        @DisplayName("정상 입력 시 저장된 LabApplication을 반환한다")
        void applySuccess() {
            Long labId = 1L, userId = 2L;
            Lab lab = givenExistingLab(labId);
            User user = givenExistingUser(userId);
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

            // save() 호출 시 파라미터를 그대로 반환
            when(labApplicationRepositoryPort.save(any(LabApplication.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            LabApplication result = service.applyToLab(labId, userId, futureTime);

            assertThat(result).isNotNull();
            assertThat(result.getLab()).isSameAs(lab);
            assertThat(result.getUser()).isSameAs(user);
            assertThat(result.getInterviewTime()).isEqualTo(futureTime);
            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);

            verify(labRepositoryPort).findById(labId);
            verify(userRepositoryPort).findById(userId);
            verify(labApplicationRepositoryPort).existsByLabIdAndUserId(labId, userId);
            verify(labApplicationRepositoryPort).save(any(LabApplication.class));
        }

        @Test
        @DisplayName("랩실이 존재하지 않으면 LabNotFoundException을 던진다")
        void applyLabNotFound() {
            Long labId = 10L;
            when(labRepositoryPort.findById(labId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.applyToLab(labId, 1L, LocalDateTime.now().plusDays(1)))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException e = (LabNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(labRepositoryPort).findById(labId);
            verifyNoMoreInteractions(userRepositoryPort, labApplicationRepositoryPort);
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 UserNotFoundException을 던진다")
        void applyUserNotFound() {
            Long labId = 1L, userId = 99L;
            givenExistingLab(labId);
            when(userRepositoryPort.findById(userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.applyToLab(labId, userId, LocalDateTime.now().plusDays(1)))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(labRepositoryPort).findById(labId);
            verify(userRepositoryPort).findById(userId);
            verifyNoMoreInteractions(labApplicationRepositoryPort);
        }

        @Test
        @DisplayName("중복 신청 시 LabApplicationValidationException(DUPLICATE_APPLICATION)을 던진다")
        void applyDuplicate() {
            Long labId = 1L, userId = 2L;
            givenExistingLab(labId);
            givenExistingUser(userId);
            when(labApplicationRepositoryPort.existsByLabIdAndUserId(labId, userId))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.applyToLab(labId, userId, LocalDateTime.now().plusDays(1)))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e =
                                (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.DUPLICATE_APPLICATION);
                    });

            verify(labRepositoryPort).findById(labId);
            verify(userRepositoryPort).findById(userId);
            verify(labApplicationRepositoryPort).existsByLabIdAndUserId(labId, userId);
            verify(labApplicationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("과거 면접 시간 입력 시 INVALID_INTERVIEW_TIME 예외를 던진다")
        void applyInvalidInterviewTime() {
            Long labId = 1L, userId = 2L;
            givenExistingLab(labId);
            givenExistingUser(userId);
            LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

            assertThatThrownBy(() -> service.applyToLab(labId, userId, pastTime))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e =
                                (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.INVALID_INTERVIEW_TIME);
                    });

            verify(labRepositoryPort).findById(labId);
            verify(userRepositoryPort).findById(userId);
            verify(labApplicationRepositoryPort).existsByLabIdAndUserId(labId, userId);
            verify(labApplicationRepositoryPort, never()).save(any());
        }
    }


    // ——————————————————————————————————————————————————————————
    // 2) approveApplication 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("approveApplication 메서드는")
    class ApproveTests {

        @Test
        @DisplayName("정상 승인 시 app.approve()를 호출한다")
        void approveSuccess() {
            Long appId = 100L;
            LabApplication app = givenExistingApplication(appId);

            service.approveApplication(appId);

            verify(labApplicationRepositoryPort).findById(appId);
            verify(app).approve();
        }

        @Test
        @DisplayName("신청서가 없으면 LabApplicationNotFoundException을 던진다")
        void approveNotFound() {
            Long appId = 200L;
            when(labApplicationRepositoryPort.findById(appId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.approveApplication(appId))
                    .isInstanceOf(LabApplicationNotFoundException.class)
                    .satisfies(ex -> {
                        LabApplicationNotFoundException e =
                                (LabApplicationNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.APPLICATION_NOT_FOUND);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
            verifyNoMoreInteractions(labApplicationRepositoryPort);
        }

        @Test
        @DisplayName("이미 처리된 신청을 승인하면 ALREADY_PROCESSED 예외를 던진다")
        void approveAlreadyProcessed() {
            Long appId = 300L;
            LabApplication app = givenExistingApplication(appId);
            // 이미 APPROVED 또는 REJECTED 상태라고 가정
            doThrow(new LabApplicationValidationException(LabApplicationErrorCode.ALREADY_PROCESSED))
                    .when(app).approve();

            assertThatThrownBy(() -> service.approveApplication(appId))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e =
                                (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.ALREADY_PROCESSED);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
            verify(app).approve();
        }
    }


    // ——————————————————————————————————————————————————————————
    // 3) rejectApplication 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("rejectApplication 메서드는")
    class RejectTests {

        @Test
        @DisplayName("정상 거절 시 app.reject()를 호출한다")
        void rejectSuccess() {
            Long appId = 400L;
            LabApplication app = givenExistingApplication(appId);

            service.rejectApplication(appId);

            verify(labApplicationRepositoryPort).findById(appId);
            verify(app).reject();
        }

        @Test
        @DisplayName("신청서가 없으면 LabApplicationNotFoundException을 던진다")
        void rejectNotFound() {
            Long appId = 500L;
            when(labApplicationRepositoryPort.findById(appId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.rejectApplication(appId))
                    .isInstanceOf(LabApplicationNotFoundException.class)
                    .satisfies(ex -> {
                        LabApplicationNotFoundException e =
                                (LabApplicationNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.APPLICATION_NOT_FOUND);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
            verifyNoMoreInteractions(labApplicationRepositoryPort);
        }

        @Test
        @DisplayName("이미 처리된 신청을 거절하면 ALREADY_PROCESSED 예외를 던진다")
        void rejectAlreadyProcessed() {
            Long appId = 600L;
            LabApplication app = givenExistingApplication(appId);
            doThrow(new LabApplicationValidationException(LabApplicationErrorCode.ALREADY_PROCESSED))
                    .when(app).reject();

            assertThatThrownBy(() -> service.rejectApplication(appId))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e =
                                (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.ALREADY_PROCESSED);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
            verify(app).reject();
        }
    }


    // ——————————————————————————————————————————————————————————
    // 4) cancelApplication 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("cancelApplication 메서드는")
    class CancelTests {

        @Test
        @DisplayName("정상 취소 시 delete()를 호출한다")
        void cancelSuccess() {
            Long appId = 700L, userId = 700L;
            LabApplication app = givenExistingApplication(appId);
            // 소유자 검증용
            when(app.isOwnedBy(userId)).thenReturn(true);

            service.cancelApplication(appId, userId);

            verify(labApplicationRepositoryPort).findById(appId);
            verify(app).isOwnedBy(userId);
            verify(labApplicationRepositoryPort).delete(app);
        }

        @Test
        @DisplayName("신청서가 없으면 LabApplicationNotFoundException을 던진다")
        void cancelNotFound() {
            Long appId = 800L;
            when(labApplicationRepositoryPort.findById(appId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancelApplication(appId, 1L))
                    .isInstanceOf(LabApplicationNotFoundException.class)
                    .satisfies(ex -> {
                        LabApplicationNotFoundException e =
                                (LabApplicationNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.APPLICATION_NOT_FOUND);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
            verify(labApplicationRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("소유자가 아니면 UNAUTHORIZED_CANCEL_ATTEMPT 예외를 던진다")
        void cancelUnauthorized() {
            Long appId = 900L, userId = 123L;
            LabApplication app = givenExistingApplication(appId);
            when(app.isOwnedBy(userId)).thenReturn(false);

            assertThatThrownBy(() -> service.cancelApplication(appId, userId))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e =
                                (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.UNAUTHORIZED_CANCEL_ATTEMPT);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
            verify(app).isOwnedBy(userId);
            verify(labApplicationRepositoryPort, never()).delete(any());
        }
    }
}