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
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceRecordQueryService 테스트")
class AttendanceRecordQueryServiceTest {

    @Mock
    private AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;

    @Mock
    private AttendanceSessionRepositoryPort attendanceSessionRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private AttendanceRecordQueryService service;

    // ——————————————————————————————————————————————————————————
    // 헬퍼 메서드: 반복되는 모킹 로직을 한곳에 모았습니다.
    // ——————————————————————————————————————————————————————————

    private User givenExistingUser(Long userId) {
        User user = DomainUserFactory.buildValidUserWithRole(org.univ.rankus.domain.model.user.Role.ADMIN); // 모든 랩실 관리 권한
        // ID 설정을 위해 reflection 사용
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(userRepositoryPort.findById(userId))
                .thenReturn(Optional.of(user));
        return user;
    }

    private AttendanceRecord givenExistingRecord(Long recordId) {
        AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
        when(attendanceRecordRepositoryPort.findById(recordId))
                .thenReturn(Optional.of(record));
        return record;
    }

    private void givenNotExistingRecord(Long recordId) {
        when(attendanceRecordRepositoryPort.findById(recordId))
                .thenReturn(Optional.empty());
    }

    private void givenNotExistingUser(Long userId) {
        when(userRepositoryPort.findById(userId))
                .thenReturn(Optional.empty());
    }

    private org.univ.rankus.domain.model.lab.core.Lab givenExistingLab(Long labId) {
        org.univ.rankus.domain.model.lab.core.Lab lab = org.univ.rankus.testutil.factory.domain.DomainLabFactory.buildValidLab();
        when(labRepositoryPort.findById(labId))
                .thenReturn(Optional.of(lab));
        return lab;
    }

    private org.univ.rankus.domain.model.attendance.AttendanceSession givenExistingSession(Long sessionId) {
        org.univ.rankus.domain.model.attendance.AttendanceSession session = DomainAttendanceFactory.buildValidSessionWithId(sessionId);
        when(attendanceSessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));
        return session;
    }

    private void givenNotExistingSession(Long sessionId) {
        when(attendanceSessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.empty());
    }

    // ——————————————————————————————————————————————————————————
    // 1) findRecordById 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("findRecordById 메서드는")
    class FindRecordByIdTests {

        @Test
        @DisplayName("존재하는 출석 기록 조회 성공")
        void findRecordById_success() {
            // given
            Long recordId = 1L;
            Long userId = 2L;

            AttendanceRecord record = givenExistingRecord(recordId);
            AttendanceSession session = givenExistingSession(record.getSessionId());
            givenExistingUser(userId);
            givenExistingLab(1L); // Lab 모킹 추가 (기본 labId 사용)

            // when
            AttendanceRecord result = service.findRecordById(recordId, userId);

            // then
            assertThat(result).isNotNull();

            verify(attendanceRecordRepositoryPort).findById(recordId);
            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 출석 기록 조회시 AttendanceNotFoundException 발생")
        void findRecordById_recordNotFound() {
            // given
            Long recordId = 999L;
            Long userId = 2L;

            givenNotExistingRecord(recordId);

            // when & then
            assertThatThrownBy(() -> service.findRecordById(recordId, userId))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.RECORD_NOT_FOUND);
                    });

            verify(attendanceRecordRepositoryPort).findById(recordId);
            verify(userRepositoryPort, never()).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 출석 기록 조회시 UserNotFoundException 발생")
        void findRecordById_userNotFound() {
            // given
            Long recordId = 1L;
            Long userId = 999L;

            AttendanceRecord record = givenExistingRecord(recordId);
            AttendanceSession session = givenExistingSession(record.getSessionId());
            givenNotExistingUser(userId);

            // when & then
            assertThatThrownBy(() -> service.findRecordById(recordId, userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(attendanceRecordRepositoryPort).findById(recordId);
            verify(attendanceSessionRepositoryPort).findById(record.getSessionId());
            verify(userRepositoryPort).findById(userId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 2) findRecordsBySessionId 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("findRecordsBySessionId 메서드는")
    class FindRecordsBySessionIdTests {

        @Test
        @DisplayName("세션의 모든 출석 기록 조회 성공")
        void findRecordsBySessionId_success() {
            // given
            Long sessionId = 1L;
            Long userId = 2L;

            List<AttendanceRecord> records = Arrays.asList(
                    DomainAttendanceFactory.buildValidRecord(),
                    DomainAttendanceFactory.buildValidRecord()
            );

            AttendanceSession session = givenExistingSession(sessionId);
            givenExistingUser(userId);
            givenExistingLab(session.getLabId()); // Lab 모킹 추가
            when(attendanceRecordRepositoryPort.findBySessionId(sessionId))
                    .thenReturn(records);

            // when
            List<AttendanceRecord> result = service.findRecordsBySessionId(sessionId, userId);

            // then
            assertThat(result).hasSize(2);

            verify(userRepositoryPort).findById(userId);
            verify(attendanceRecordRepositoryPort).findBySessionId(sessionId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 세션 출석 기록 조회시 UserNotFoundException 발생")
        void findRecordsBySessionId_userNotFound() {
            // given
            Long sessionId = 1L;
            Long userId = 999L;

            AttendanceSession session = givenExistingSession(sessionId);
            givenNotExistingUser(userId);

            // when & then
            assertThatThrownBy(() -> service.findRecordsBySessionId(sessionId, userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(userId);
            verifyNoMoreInteractions(attendanceRecordRepositoryPort);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 3) findRecordBySessionIdAndUserId 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("findRecordBySessionIdAndUserId 메서드는")
    class FindRecordBySessionIdAndUserIdTests {

        @Test
        @DisplayName("특정 세션의 특정 사용자 출석 기록 조회 성공")
        void findRecordBySessionIdAndUserId_success() {
            // given
            Long sessionId = 1L;
            Long targetUserId = 2L;
            Long requesterId = 3L;

            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
            AttendanceSession session = givenExistingSession(sessionId);
            
            givenExistingUser(requesterId);
            givenExistingUser(targetUserId); // 대상 사용자 모킹 추가
            givenExistingLab(session.getLabId()); // Lab 모킹 추가
            when(attendanceRecordRepositoryPort.findBySessionIdAndUserId(sessionId, targetUserId))
                    .thenReturn(Optional.of(record));

            // when
            AttendanceRecord result = service.findRecordBySessionIdAndUserId(sessionId, targetUserId, requesterId);

            // then
            assertThat(result).isNotNull();

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(requesterId);
            verify(userRepositoryPort).findById(targetUserId);
            verify(labRepositoryPort).findById(session.getLabId());
            verify(attendanceRecordRepositoryPort).findBySessionIdAndUserId(sessionId, targetUserId);
        }

        @Test
        @DisplayName("존재하지 않는 출석 기록 조회시 null 반환")
        void findRecordBySessionIdAndUserId_recordNotFound() {
            // given
            Long sessionId = 1L;
            Long targetUserId = 2L;
            Long requesterId = 3L;

            AttendanceSession session = givenExistingSession(sessionId);
            givenExistingUser(requesterId);
            givenExistingUser(targetUserId);
            givenExistingLab(session.getLabId());
            when(attendanceRecordRepositoryPort.findBySessionIdAndUserId(sessionId, targetUserId))
                    .thenReturn(Optional.empty());

            // when
            AttendanceRecord result = service.findRecordBySessionIdAndUserId(sessionId, targetUserId, requesterId);

            // then
            assertThat(result).isNull();

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(requesterId);
            verify(userRepositoryPort).findById(targetUserId);
            verify(labRepositoryPort).findById(session.getLabId());
            verify(attendanceRecordRepositoryPort).findBySessionIdAndUserId(sessionId, targetUserId);
        }

        @Test
        @DisplayName("존재하지 않는 요청자로 출석 기록 조회시 UserNotFoundException 발생")
        void findRecordBySessionIdAndUserId_requesterNotFound() {
            // given
            Long sessionId = 1L;
            Long targetUserId = 2L;
            Long requesterId = 999L;

            AttendanceSession session = givenExistingSession(sessionId);
            givenNotExistingUser(requesterId);

            // when & then
            assertThatThrownBy(() -> service.findRecordBySessionIdAndUserId(sessionId, targetUserId, requesterId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(attendanceSessionRepositoryPort).findById(sessionId);
            verify(userRepositoryPort).findById(requesterId);
            verifyNoMoreInteractions(attendanceRecordRepositoryPort);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 4) findRecordsByUserId 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("findRecordsByUserId 메서드는")
    class FindRecordsByUserIdTests {

        @Test
        @DisplayName("사용자의 모든 출석 기록 페이징 조회 성공")
        void findRecordsByUserId_success() {
            // given
            Long targetUserId = 2L;
            Long requesterId = 3L;
            int page = 0;
            int size = 10;

            List<AttendanceRecord> records = Arrays.asList(
                    DomainAttendanceFactory.buildValidRecord(),
                    DomainAttendanceFactory.buildValidRecord(),
                    DomainAttendanceFactory.buildValidRecord()
            );

            givenExistingUser(requesterId);
            givenExistingUser(targetUserId);
            when(attendanceRecordRepositoryPort.findByUserIdWithPaging(targetUserId, page, size))
                    .thenReturn(records);

            // when
            List<AttendanceRecord> result = service.findRecordsByUserId(targetUserId, requesterId, page, size);

            // then
            assertThat(result).hasSize(3);

            verify(userRepositoryPort).findById(requesterId);
            verify(userRepositoryPort).findById(targetUserId);
            verify(attendanceRecordRepositoryPort).findByUserIdWithPaging(targetUserId, page, size);
        }

        @Test
        @DisplayName("존재하지 않는 요청자로 사용자 출석 기록 조회시 UserNotFoundException 발생")
        void findRecordsByUserId_requesterNotFound() {
            // given
            Long targetUserId = 2L;
            Long requesterId = 999L;

            givenNotExistingUser(requesterId);

            // when & then
            assertThatThrownBy(() -> service.findRecordsByUserId(targetUserId, requesterId, 0, 10))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(requesterId);
            verifyNoMoreInteractions(attendanceRecordRepositoryPort);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 5) findRecordsByLabId 메서드 테스트
    // ——————————————————————————————————————————————————————————
    @Nested
    @DisplayName("findRecordsByLabId 메서드는")
    class FindRecordsByLabIdTests {

        @Test
        @DisplayName("랩실의 모든 출석 기록 페이징 조회 성공")
        void findRecordsByLabId_success() {
            // given
            Long labId = 1L;
            Long userId = 2L;
            int page = 0;
            int size = 10;

            List<AttendanceRecord> records = Arrays.asList(
                    DomainAttendanceFactory.buildValidRecord(),
                    DomainAttendanceFactory.buildValidRecord(),
                    DomainAttendanceFactory.buildValidRecord(),
                    DomainAttendanceFactory.buildValidRecord()
            );

            givenExistingUser(userId);
            givenExistingLab(labId);
            when(attendanceRecordRepositoryPort.findByLabIdWithPaging(labId, page, size))
                    .thenReturn(records);

            // when
            List<AttendanceRecord> result = service.findRecordsByLabId(labId, userId, page, size);

            // then
            assertThat(result).hasSize(4);

            verify(userRepositoryPort).findById(userId);
            verify(attendanceRecordRepositoryPort).findByLabIdWithPaging(labId, page, size);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 랩실 출석 기록 조회시 UserNotFoundException 발생")
        void findRecordsByLabId_userNotFound() {
            // given
            Long labId = 1L;
            Long userId = 999L;

            when(userRepositoryPort.findById(userId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findRecordsByLabId(labId, userId, 0, 10))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepositoryPort).findById(userId);
            verifyNoMoreInteractions(attendanceRecordRepositoryPort);
        }
    }
}