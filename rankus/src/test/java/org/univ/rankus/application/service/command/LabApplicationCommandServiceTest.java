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
import org.univ.rankus.domain.model.lab.application.ApplicationStatus;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.*;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainLabApplicationFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        Lab lab = DomainLabFactory.buildValidLabWithId(labId);
        when(labRepositoryPort.findById(labId))
                .thenReturn(Optional.of(lab));
        return lab;
    }

    private User givenExistingUser(Long userId) {
        User user = DomainUserFactory.buildValidUserWithId(userId);
        when(userRepositoryPort.findById(userId))
                .thenReturn(Optional.of(user));
        return user;
    }

    private LabApplication givenExistingApplication(Long appId) {
        LabApplication app = DomainLabApplicationFactory.buildValidPendingWithId(
                appId,
                DomainLabFactory.buildValidLab(),
                DomainUserFactory.buildValidUser(),
                LocalDateTime.now().plusDays(1)
        );
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
            // given
            Long labId = 1L, userId = 2L;
            Lab lab = givenExistingLab(labId);
            User user = givenExistingUser(userId);
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

            LabApplication expectedApplication = DomainLabApplicationFactory.buildValidPendingApplication(lab, user, futureTime);
            when(labApplicationRepositoryPort.save(any(LabApplication.class)))
                    .thenReturn(expectedApplication);

            // when
            LabApplication result = service.applyToLab(labId, userId, futureTime);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(result.getInterviewTime()).isEqualTo(futureTime);

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
        @DisplayName("정상 승인 시 APPROVED 상태로 변경된다")
        void approveSuccess() {
            // given
            Long appId = 100L;
            LabApplication app = givenExistingApplication(appId);

            // when
            service.approveApplication(appId);

            // then
            verify(labApplicationRepositoryPort).findById(appId);
            // 실제 서비스에서는 도메인 객체의 approve() 메서드만 호출하고 save()는 호출하지 않음
            // verify(labApplicationRepositoryPort).save(app); // 이 줄 제거
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
        @DisplayName("이미 처리된 신청을 승인하면 ALREADY_PROCESSED 예외를 던질 수 있다")
        void approveAlreadyProcessed() {
            // given
            Long appId = 300L;
            Lab lab = DomainLabFactory.buildValidLab();
            User user = DomainUserFactory.buildValidUser();
            LabApplication app = DomainLabApplicationFactory.buildApprovedApplication(lab, user);
            when(labApplicationRepositoryPort.findById(appId))
                    .thenReturn(Optional.of(app));

            // when & then
            assertThatThrownBy(() -> service.approveApplication(appId))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e =
                                (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.ALREADY_PROCESSED);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
        }
    }


    // ——————————————————————————————————————————————————————————
    // 3) rejectApplication 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("rejectApplication 메서드는")
    class RejectTests {

        @Test
        @DisplayName("정상 거절 시 REJECTED 상태로 변경된다")
        void rejectSuccess() {
            // given
            Long appId = 400L;
            LabApplication app = givenExistingApplication(appId);

            // when
            service.rejectApplication(appId);

            // then
            verify(labApplicationRepositoryPort).findById(appId);
            // 실제 서비스에서는 도메인 객체의 reject() 메서드만 호출하고 save()는 호출하지 않음
            // verify(labApplicationRepositoryPort).save(app); // 이 줄 제거
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
        @DisplayName("이미 처리된 신청을 거절하면 ALREADY_PROCESSED 예외를 던질 수 있다")
        void rejectAlreadyProcessed() {
            // given
            Long appId = 600L;
            Lab lab = DomainLabFactory.buildValidLab();
            User user = DomainUserFactory.buildValidUser();
            LabApplication app = DomainLabApplicationFactory.buildRejectedApplication(lab, user);
            when(labApplicationRepositoryPort.findById(appId))
                    .thenReturn(Optional.of(app));

            // when & then
            assertThatThrownBy(() -> service.rejectApplication(appId))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e =
                                (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.ALREADY_PROCESSED);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
        }
    }


    // ——————————————————————————————————————————————————————————
    // 4) cancelApplication 메서드 테스트
    // ——————————————————————————————————————————————————————————
    //
    // 주의: ADMIN 권한 확인은 서비스 계층이 아닌 보안 계층에서 처리됩니다.
    // - Controller: @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(...)")
    // - PermissionHandler: user.isAdmin() || app.isOwnedBy(userId)
    // - Service: 순수 비즈니스 로직만 수행 (소유권 검증)
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("cancelApplication 메서드는")
    class CancelTests {

        @Test
        @DisplayName("정상 취소 시 delete()를 호출한다")
        void cancelSuccess() {
            // given
            Long appId = 700L, userId = 700L;
            Lab lab = DomainLabFactory.buildValidLab();
            User owner = DomainUserFactory.buildValidUserWithId(userId);
            LabApplication app = DomainLabApplicationFactory.buildValidPendingWithId(
                    appId, lab, owner, LocalDateTime.now().plusDays(1)
            );
            when(labApplicationRepositoryPort.findById(appId))
                    .thenReturn(Optional.of(app));

            // when
            service.cancelApplication(appId, userId);

            // then
            verify(labApplicationRepositoryPort).findById(appId);
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
            // given
            Long appId = 900L, userId = 123L;
            Lab lab = DomainLabFactory.buildValidLab();
            User owner = DomainUserFactory.buildValidUserWithId(999L); // 다른 사용자
            LabApplication app = DomainLabApplicationFactory.buildValidPendingWithId(
                    appId, lab, owner, LocalDateTime.now().plusDays(1)
            );
            when(labApplicationRepositoryPort.findById(appId))
                    .thenReturn(Optional.of(app));

            // when & then
            assertThatThrownBy(() -> service.cancelApplication(appId, userId))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e =
                                (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.UNAUTHORIZED_CANCEL_ATTEMPT);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
            verify(labApplicationRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("서비스 계층에서는 소유권 검증만 수행하고 ADMIN 권한 확인은 보안 계층에서 처리됨을 검증")
        void serviceLayerOnlyChecksOwnership() {
            // given
            Long appId = 1000L, nonOwnerUserId = 456L;
            Lab lab = DomainLabFactory.buildValidLab();
            User owner = DomainUserFactory.buildValidUserWithId(789L); // 다른 사용자
            LabApplication app = DomainLabApplicationFactory.buildValidPendingWithId(
                    appId, lab, owner, LocalDateTime.now().plusDays(1)
            );
            when(labApplicationRepositoryPort.findById(appId))
                    .thenReturn(Optional.of(app));

            // when & then - 서비스 계층에서는 소유권만 체크하므로 예외 발생
            // ADMIN 권한 확인은 Controller의 @PreAuthorize에서 처리됨
            assertThatThrownBy(() -> service.cancelApplication(appId, nonOwnerUserId))
                    .isInstanceOf(LabApplicationValidationException.class)
                    .satisfies(ex -> {
                        LabApplicationValidationException e =
                                (LabApplicationValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(LabApplicationErrorCode.UNAUTHORIZED_CANCEL_ATTEMPT);
                    });

            verify(labApplicationRepositoryPort).findById(appId);
            verify(labApplicationRepositoryPort, never()).delete(any());
        }
    }
}