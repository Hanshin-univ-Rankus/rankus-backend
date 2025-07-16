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
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.QRToken;
import org.univ.rankus.domain.model.attendance.SessionStatus;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;
import org.univ.rankus.testutil.mock.AttendanceMockUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceSessionCommandService 테스트")
class AttendanceSessionCommandServiceTest {

    @Mock
    private AttendanceSessionRepositoryPort attendanceSessionRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;

    @InjectMocks
    private AttendanceSessionCommandService service;

    /**
     * 완전한 의존성 체인을 설정하는 헬퍼 메서드
     * AttendanceSessionCommandService의 모든 메서드에서 필요한 Lab, User, Session 관계를 설정
     */
    private void givenCompleteSessionChain(Long sessionId, Long userId) {
        // AttendanceMockUtil 활용한 통합 Mock 설정
        AttendanceMockUtil.MockChainResult mockChain = AttendanceMockUtil.mockFullAttendanceChain(
                attendanceRecordRepositoryPort,
                attendanceSessionRepositoryPort,
                userRepositoryPort,
                labRepositoryPort
        );

        // Mock 설정 - ID는 Mock에서 관리
        when(attendanceSessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(mockChain.session));
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(mockChain.user));
    }

    /**
     * Session 미존재 상황을 모킹
     */
    private void givenSessionNotFound(Long sessionId) {
        when(attendanceSessionRepositoryPort.findById(sessionId)).thenReturn(Optional.empty());
    }

    /**
     * User 미존재 상황을 모킹
     */
    private void givenUserNotFound(Long userId) {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());
    }

    /**
     * Lab 미존재 상황을 모킹
     */
    private void givenLabNotFound(Long labId) {
        when(labRepositoryPort.findById(labId)).thenReturn(Optional.empty());
    }

    // ——————————————————————————————————————————————————————————
    // 1) createSession 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("createSession 메서드는")
    class CreateSessionTests {

        @Test
        @DisplayName("유효한 정보로 출석 세션 생성 성공")
        void createSession_success() {
            // given
            Long labId = 1L;
            String title = "오전 출석";
            Integer qrValidityMinutes = 5;
            Long createdBy = 2L;

            givenCompleteSessionChain(1L, createdBy);

            AttendanceSession expectedSession = DomainAttendanceFactory.buildValidSessionWithCreator(createdBy);
            when(attendanceSessionRepositoryPort.save(any(AttendanceSession.class)))
                    .thenReturn(expectedSession);

            // when
            AttendanceSession result = service.createSession(labId, title, qrValidityMinutes, createdBy);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(SessionStatus.ACTIVE);

            verify(labRepositoryPort).findById(labId);
            verify(userRepositoryPort).findById(createdBy);
            verify(attendanceSessionRepositoryPort).save(any(AttendanceSession.class));
        }

        @Test
        @DisplayName("존재하지 않는 랩실로 세션 생성시 LabNotFoundException 발생")
        void createSession_labNotFound() {
            // given
            Long labId = 999L;
            Long createdBy = 1L;

            // User는 존재하지만 Lab은 없는 상황
            User creator = DomainUserFactory.buildValidUserWithId(createdBy);
            when(userRepositoryPort.findById(createdBy)).thenReturn(Optional.of(creator));
            givenLabNotFound(labId);

            // when & then
            assertThatThrownBy(() -> service.createSession(labId, "테스트", 5, createdBy))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException e = (LabNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(createdBy);
            verify(labRepositoryPort).findById(labId);
            verifyNoInteractions(attendanceSessionRepositoryPort);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 세션 생성시 UserNotFoundException 발생")
        void createSession_userNotFound() {
            // given
            Long labId = 1L;
            Long createdBy = 999L;

            // Service에서 User를 먼저 찾으므로 User not found 예외 발생
            givenUserNotFound(createdBy);

            // when & then
            assertThatThrownBy(() -> service.createSession(labId, "테스트", 5, createdBy))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(createdBy);
            // User가 없으면 Lab은 찾지 않음
            verifyNoInteractions(labRepositoryPort);
            verifyNoInteractions(attendanceSessionRepositoryPort);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 2) endSession 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("endSession 메서드는")
    class EndSessionTests {

        @Test
        @DisplayName("활성 세션 종료 성공")
        void endSession_success() {
            // given
            Long sessionId = 1L;
            Long userId = 2L;

            givenCompleteSessionChain(sessionId, userId);

            // when
            AttendanceSession result = service.endSession(sessionId, userId);

            // then
            assertThat(result).isNotNull();
            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 세션 종료시 AttendanceNotFoundException 발생")
        void endSession_sessionNotFound() {
            // given
            Long sessionId = 999L;
            givenSessionNotFound(sessionId);

            // when & then
            assertThatThrownBy(() -> service.endSession(sessionId, 1L))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 3) cancelSession 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("cancelSession 메서드는")
    class CancelSessionTests {

        @Test
        @DisplayName("활성 세션 취소 성공")
        void cancelSession_success() {
            // given
            Long sessionId = 1L;
            Long userId = 2L;

            givenCompleteSessionChain(sessionId, userId);

            // when
            AttendanceSession result = service.cancelSession(sessionId, userId);

            // then
            assertThat(result).isNotNull();
            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 세션 취소시 AttendanceNotFoundException 발생")
        void cancelSession_sessionNotFound() {
            // given
            Long sessionId = 999L;
            givenSessionNotFound(sessionId);

            // when & then
            assertThatThrownBy(() -> service.cancelSession(sessionId, 1L))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 4) updateSessionTitle 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("updateSessionTitle 메서드는")
    class UpdateSessionTitleTests {

        @Test
        @DisplayName("세션 제목 수정 성공")
        void updateSessionTitle_success() {
            // given
            Long sessionId = 1L;
            String newTitle = "수정된 출석";
            Long userId = 2L;

            givenCompleteSessionChain(sessionId, userId);

            // when
            AttendanceSession result = service.updateSessionTitle(sessionId, newTitle, userId);

            // then
            assertThat(result).isNotNull();
            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 세션 제목 수정시 AttendanceNotFoundException 발생")
        void updateSessionTitle_sessionNotFound() {
            // given
            Long sessionId = 999L;
            givenSessionNotFound(sessionId);

            // when & then
            assertThatThrownBy(() -> service.updateSessionTitle(sessionId, "새 제목", 1L))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 5) updateQRValidityMinutes 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("updateQRValidityMinutes 메서드는")
    class UpdateQRValidityMinutesTests {

        @Test
        @DisplayName("QR 유효시간 수정 성공")
        void updateQRValidityMinutes_success() {
            // given
            Long sessionId = 1L;
            Integer newValidityMinutes = 10;
            Long userId = 2L;

            givenCompleteSessionChain(sessionId, userId);

            // when
            AttendanceSession result = service.updateQRValidityMinutes(sessionId, newValidityMinutes, userId);

            // then
            assertThat(result).isNotNull();
            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 세션 QR 유효시간 수정시 AttendanceNotFoundException 발생")
        void updateQRValidityMinutes_sessionNotFound() {
            // given
            Long sessionId = 999L;
            givenSessionNotFound(sessionId);

            // when & then
            assertThatThrownBy(() -> service.updateQRValidityMinutes(sessionId, 10, 1L))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 6) generateQRCode 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("generateQRCode 메서드는")
    class GenerateQRCodeTests {

        @Test
        @DisplayName("활성 세션에서 QR 코드 생성 성공")
        void generateQRCode_success() {
            // given
            Long sessionId = 1L;
            Long userId = 2L;

            // ID가 설정된 AttendanceSession 생성
            AttendanceSession session = DomainAttendanceFactory.buildValidSessionWithId(sessionId);
            Lab lab = DomainLabFactory.buildValidLabWithId(session.getLabId());
            User user = DomainUserFactory.buildLabManagerWithLab(lab);
            org.springframework.test.util.ReflectionTestUtils.setField(user, "id", userId);

            when(attendanceSessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));
            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
            when(labRepositoryPort.findById(session.getLabId())).thenReturn(Optional.of(lab));

            // when
            QRToken result = service.generateQRCode(sessionId, userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isNotBlank();
            assertThat(result.getSessionId()).isEqualTo(sessionId);

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 세션에서 QR 생성시 AttendanceNotFoundException 발생")
        void generateQRCode_sessionNotFound() {
            // given
            Long sessionId = 999L;
            givenSessionNotFound(sessionId);

            // when & then
            assertThatThrownBy(() -> service.generateQRCode(sessionId, 1L))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 7) checkAttendance 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("checkAttendance 메서드는")
    class CheckAttendanceTests {

        @Test
        @DisplayName("유효한 QR 토큰으로 출석 체크 성공")
        void checkAttendance_success() {
            // given
            // 1분 전 시간으로 토큰 생성 (5분 유효시간으로 4분 남음)
            long oneMinuteAgo = LocalDateTime.now().minusMinutes(1).toEpochSecond(java.time.ZoneOffset.UTC);
            String qrToken = "1-1-" + oneMinuteAgo;
            Long userId = 2L;

            // QR 토큰에서 세션 ID 추출 (두 번째 부분)
            Long sessionId = Long.parseLong(qrToken.split("-")[1]);

            // Lab과 User를 먼저 생성하고 관계 설정
            Lab lab = DomainLabFactory.buildValidLabWithId(1L);
            when(labRepositoryPort.findById(any(Long.class))).thenReturn(Optional.of(lab));

            // LAB_MEMBER User 생성 및 랩실 할당 (해당 랩실 출석 체크 권한 확보)
            User user = DomainUserFactory.buildLabMemberWithLab(lab);
            org.springframework.test.util.ReflectionTestUtils.setField(user, "id", userId);
            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

            // AttendanceSession 설정
            AttendanceSession session = DomainAttendanceFactory.buildSessionWithLab(lab);
            org.springframework.test.util.ReflectionTestUtils.setField(session, "sessionId", sessionId);
            when(attendanceSessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

            // AttendanceRecord 관련 Mock 설정
            when(attendanceRecordRepositoryPort.existsBySessionIdAndUserId(sessionId, userId)).thenReturn(false);
            when(attendanceRecordRepositoryPort.save(any(AttendanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            AttendanceRecord result = service.checkAttendance(qrToken, userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("잘못된 QR 토큰으로 출석 체크시 AttendanceValidationException 발생")
        void checkAttendance_invalidToken() {
            // given
            String invalidToken = "invalid-token";
            Long userId = 2L;

            // 잘못된 토큰은 QRToken.fromString()에서 예외 발생하므로 Mock 설정 불필요

            // when & then
            assertThatThrownBy(() -> service.checkAttendance(invalidToken, userId))
                    .isInstanceOf(AttendanceValidationException.class)
                    .satisfies(ex -> {
                        AttendanceValidationException e = (AttendanceValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.QR_TOKEN_INVALID);
                    });

            // 토큰 파싱에서 예외 발생하므로 Repository 호출 안됨
            verifyNoInteractions(userRepositoryPort);
            verifyNoInteractions(attendanceSessionRepositoryPort);
        }

        @Test
        @DisplayName("존재하지 않는 세션의 QR 토큰으로 출석 체크시 AttendanceNotFoundException 발생")
        void checkAttendance_sessionNotFound() {
            // given
            long oneMinuteAgo = LocalDateTime.now().minusMinutes(1).toEpochSecond(java.time.ZoneOffset.UTC);
            String qrToken = "999-999-" + oneMinuteAgo;
            Long userId = 2L;

            // 세션이 없으므로 세션 조회에서 예외 발생, 사용자 조회는 도달하지 않음
            givenSessionNotFound(999L);

            // when & then
            assertThatThrownBy(() -> service.checkAttendance(qrToken, userId))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_FOUND);
                    });

            // 세션 조회만 시도하고 사용자 조회는 도달하지 않음
            verify(attendanceSessionRepositoryPort).findById(999L);
            verifyNoInteractions(userRepositoryPort);
        }

        @Test
        @DisplayName("존재하지 않는 사용자가 출석 체크시 AttendanceNotFoundException 발생")
        void checkAttendance_userNotFound() {
            // given
            long oneMinuteAgo = LocalDateTime.now().minusMinutes(1).toEpochSecond(java.time.ZoneOffset.UTC);
            String qrToken = "1-1-" + oneMinuteAgo;
            Long userId = 999L;
            Long sessionId = 1L;

            // 세션은 존재하지만 사용자는 없는 상황
            Lab lab = DomainLabFactory.buildValidLabWithId(1L);
            AttendanceSession session = DomainAttendanceFactory.buildSessionWithLab(lab);
            org.springframework.test.util.ReflectionTestUtils.setField(session, "sessionId", sessionId);
            when(attendanceSessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

            givenUserNotFound(userId);

            // when & then
            assertThatThrownBy(() -> service.checkAttendance(qrToken, userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
        }
    }
}