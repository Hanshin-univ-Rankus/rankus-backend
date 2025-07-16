package org.univ.rankus.application.service.query;

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
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.SessionStatus;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceSessionQueryService 테스트")
class AttendanceSessionQueryServiceTest {

    @Mock
    private AttendanceSessionRepositoryPort attendanceSessionRepositoryPort;

    @Mock
    private AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @InjectMocks
    private AttendanceSessionQueryService service;

    // ——————————————————————————————————————————————————————————
    // 헬퍼 메서드: 반복되는 모킹 로직을 한곳에 모았습니다.
    // ——————————————————————————————————————————————————————————

    private User givenExistingUser(Long userId) {
        User user = DomainUserFactory.buildValidUserWithRole(org.univ.rankus.domain.model.user.Role.ADMIN); // 모든 랩실 관리 권한
        when(userRepositoryPort.findById(userId))
                .thenReturn(Optional.of(user));
        return user;
    }

    private AttendanceSession givenExistingSession(Long sessionId) {
        AttendanceSession session = DomainAttendanceFactory.buildValidSessionWithId(sessionId);
        when(attendanceSessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));
        return session;
    }

    private Lab givenExistingLab(Long labId) {
        Lab lab = DomainLabFactory.buildValidLab();
        when(labRepositoryPort.findById(labId))
                .thenReturn(Optional.of(lab));
        return lab;
    }

    // ——————————————————————————————————————————————————————————
    // 1) findSessionById 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("findSessionById 메서드는")
    class FindSessionByIdTests {

        @Test
        @DisplayName("존재하는 세션 조회 성공")
        void findSessionById_success() {
            // given
            Long sessionId = 1L;
            Long userId = 2L;

            AttendanceSession session = givenExistingSession(sessionId);
            givenExistingUser(userId);
            givenExistingLab(session.getLabId()); // Lab 모킹 추가

            // when
            AttendanceSession result = service.findSessionById(sessionId, userId);

            // then
            assertThat(result).isNotNull();

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
            verify(labRepositoryPort).findById(session.getLabId());
        }

        @Test
        @DisplayName("존재하지 않는 세션 조회시 AttendanceValidationException 발생")
        void findSessionById_sessionNotFound() {
            // given
            Long sessionId = 999L;
            Long userId = 2L;

            when(attendanceSessionRepositoryPort.findById(sessionId))
                    .thenReturn(Optional.empty());
            givenExistingUser(userId);

            // when & then
            assertThatThrownBy(() -> service.findSessionById(sessionId, userId))
                    .isInstanceOf(AttendanceValidationException.class)
                    .satisfies(ex -> {
                        AttendanceValidationException e = (AttendanceValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            // userRepositoryPort.findById()는 세션이 없으면 호출되지 않음
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 세션 조회시 UserNotFoundException 발생")
        void findSessionById_userNotFound() {
            // given
            Long sessionId = 1L;
            Long userId = 999L;

            givenExistingSession(sessionId);
            when(userRepositoryPort.findById(userId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findSessionById(sessionId, userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 2) findActiveSessionsByLabId 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("findActiveSessionsByLabId 메서드는")
    class FindActiveSessionsByLabIdTests {

        @Test
        @DisplayName("랩실의 활성 세션 목록 조회 성공")
        void findActiveSessionsByLabId_success() {
            // given
            Long labId = 1L;
            Long userId = 2L;

            List<AttendanceSession> sessions = Arrays.asList(
                    DomainAttendanceFactory.buildValidSession(),
                    DomainAttendanceFactory.buildValidSession()
            );

            givenExistingUser(userId);
            givenExistingLab(labId); // Lab 모킹 추가
            when(attendanceSessionRepositoryPort.findByLabIdAndStatus(labId, SessionStatus.ACTIVE))
                    .thenReturn(sessions);

            // when
            List<AttendanceSession> result = service.findActiveSessionsByLabId(labId, userId);

            // then
            assertThat(result).hasSize(2);

            verify(userRepositoryPort).findById(userId);
            verify(labRepositoryPort).findById(labId);
            verify(attendanceSessionRepositoryPort).findByLabIdAndStatus(labId, SessionStatus.ACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 활성 세션 조회시 UserNotFoundException 발생")
        void findActiveSessionsByLabId_userNotFound() {
            // given
            Long labId = 1L;
            Long userId = 999L;

            when(userRepositoryPort.findById(userId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findActiveSessionsByLabId(labId, userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verifyNoMoreInteractions(attendanceSessionRepositoryPort);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 3) findSessionsByLabId 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("findSessionsByLabId 메서드는")
    class FindSessionsByLabIdTests {

        @Test
        @DisplayName("랩실의 모든 세션 목록 페이징 조회 성공")
        void findSessionsByLabId_success() {
            // given
            Long labId = 1L;
            Long userId = 2L;
            int page = 0;
            int size = 10;

            List<AttendanceSession> sessions = Arrays.asList(
                    DomainAttendanceFactory.buildValidSession(),
                    DomainAttendanceFactory.buildValidSession(),
                    DomainAttendanceFactory.buildValidSession()
            );

            givenExistingUser(userId);
            givenExistingLab(labId); // Lab 모킹 추가
            when(attendanceSessionRepositoryPort.findByLabIdWithPaging(labId, page, size))
                    .thenReturn(sessions);

            // when
            List<AttendanceSession> result = service.findSessionsByLabId(labId, userId, page, size);

            // then
            assertThat(result).hasSize(3);

            verify(userRepositoryPort).findById(userId);
            verify(labRepositoryPort).findById(labId);
            verify(attendanceSessionRepositoryPort).findByLabIdWithPaging(labId, page, size);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 세션 목록 조회시 UserNotFoundException 발생")
        void findSessionsByLabId_userNotFound() {
            // given
            Long labId = 1L;
            Long userId = 999L;

            when(userRepositoryPort.findById(userId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findSessionsByLabId(labId, userId, 0, 10))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verifyNoMoreInteractions(attendanceSessionRepositoryPort);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 4) findSessionsByUserId 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("findSessionsByUserId 메서드는")
    class FindSessionsByUserIdTests {

        @Test
        @DisplayName("사용자가 참여한 세션 목록 조회 성공")
        void findSessionsByUserId_success() {
            // given
            Long userId = 2L;
            int page = 0;
            int size = 10;

            List<AttendanceSession> sessions = Arrays.asList(
                    DomainAttendanceFactory.buildValidSession(),
                    DomainAttendanceFactory.buildValidSession()
            );

            givenExistingUser(userId);
            when(attendanceSessionRepositoryPort.findSessionsByUserIdWithPaging(userId, page, size))
                    .thenReturn(sessions);

            // when
            List<AttendanceSession> result = service.findSessionsByUserId(userId, page, size);

            // then
            assertThat(result).hasSize(2);

            verify(userRepositoryPort).findById(userId);
            verify(attendanceSessionRepositoryPort).findSessionsByUserIdWithPaging(userId, page, size);
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 세션 목록 조회시 UserNotFoundException 발생")
        void findSessionsByUserId_userNotFound() {
            // given
            Long userId = 999L;

            when(userRepositoryPort.findById(userId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findSessionsByUserId(userId, 0, 10))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verifyNoMoreInteractions(attendanceSessionRepositoryPort);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 5) getSessionStatistics 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("getSessionStatistics 메서드는")
    class GetSessionStatisticsTests {

        @Test
        @DisplayName("세션 통계 조회 성공")
        void getSessionStatistics_success() {
            // given
            Long sessionId = 1L;
            Long userId = 2L;

            AttendanceSession session = givenExistingSession(sessionId);
            givenExistingUser(userId);
            givenExistingLab(session.getLabId()); // Lab 모킹 추가

            // Mock 통계 데이터 - 실제 통계 리포지토리 응답을 시뮬레이션
            AttendanceRecordRepositoryPort.AttendanceStatistics mockStatistics =
                    new AttendanceRecordRepositoryPort.AttendanceStatistics(10L, 8L, 1L, 1L);

            when(attendanceRecordRepositoryPort.findStatisticsBySessionId(sessionId))
                    .thenReturn(mockStatistics);

            // when
            AttendanceSession.AttendanceStatistics result = service.getSessionStatistics(sessionId, userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalMembers()).isEqualTo(10);
            assertThat(result.getPresentCount()).isEqualTo(8);

            // 간단한 검증으로 시작
            verify(attendanceRecordRepositoryPort).findStatisticsBySessionId(sessionId);
        }

        @Test
        @DisplayName("존재하지 않는 세션의 통계 조회시 AttendanceValidationException 발생")
        void getSessionStatistics_sessionNotFound() {
            // given
            Long sessionId = 999L;
            Long userId = 2L;

            when(attendanceSessionRepositoryPort.findById(sessionId))
                    .thenReturn(Optional.empty());
            givenExistingUser(userId);

            // when & then
            assertThatThrownBy(() -> service.getSessionStatistics(sessionId, userId))
                    .isInstanceOf(AttendanceValidationException.class)
                    .satisfies(ex -> {
                        AttendanceValidationException e = (AttendanceValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            // userRepositoryPort.findById()는 세션이 없으면 호출되지 않음
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 통계 조회시 UserNotFoundException 발생")
        void getSessionStatistics_userNotFound() {
            // given
            Long sessionId = 1L;
            Long userId = 999L;

            givenExistingSession(sessionId);
            when(userRepositoryPort.findById(userId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getSessionStatistics(sessionId, userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
        }
    }
}