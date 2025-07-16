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
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceRecordCommandService 테스트")
class AttendanceRecordCommandServiceTest {

    @Mock
    private AttendanceRecordRepositoryPort attendanceRecordRepository;

    @Mock
    private AttendanceSessionRepositoryPort attendanceSessionRepository;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private LabRepositoryPort labRepository;

    @InjectMocks
    private AttendanceRecordCommandService service;

    /**
     * 완전한 의존성 체인을 설정하는 헬퍼 메서드
     */
    private void givenCompleteAttendanceChain(Long recordId, Long managerId) {
        givenCompleteAttendanceChain(recordId, managerId, AttendanceStatus.PRESENT);
    }

    /**
     * 특정 초기 상태로 완전한 의존성 체인을 설정하는 헬퍼 메서드
     */
    private void givenCompleteAttendanceChain(Long recordId, Long managerId, AttendanceStatus initialStatus) {
        // 1. Lab 설정 (먼저 생성, ID 필수)
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);
        when(labRepository.findById(any(Long.class))).thenReturn(Optional.of(lab));

        // 2. 해당 Lab에 할당된 User 설정 (권한 확보)
        User user = DomainUserFactory.buildLabManagerWithLab(lab);
        when(userRepository.findById(managerId)).thenReturn(Optional.of(user));

        // 3. AttendanceSession 설정 (동일한 lab 사용)
        AttendanceSession session = DomainAttendanceFactory.buildSessionWithLab(lab);
        when(attendanceSessionRepository.findById(any(Long.class))).thenReturn(Optional.of(session));

        // 4. AttendanceRecord 설정 (session 사용, 초기 상태 조정)
        AttendanceRecord record = DomainAttendanceFactory.buildRecordWithSession(session);
        // 초기 상태가 PRESENT가 아닌 경우 상태 조정
        if (initialStatus != AttendanceStatus.PRESENT) {
            // ReflectionTestUtils로 상태 강제 변경 (테스트용)
            org.springframework.test.util.ReflectionTestUtils.setField(record, "status", initialStatus);
            org.springframework.test.util.ReflectionTestUtils.setField(record, "isManuallyAdjusted", false);
        }
        when(attendanceRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(attendanceRecordRepository.save(any(AttendanceRecord.class))).thenReturn(record);
    }

    @Nested
    @DisplayName("markAsAbsent 메서드는")
    class MarkAsAbsentTests {

        @Test
        @DisplayName("출석 기록을 결석으로 변경 성공")
        void markAsAbsent_success() {
            // given
            Long recordId = 1L;
            Long managerId = 2L;
            String reason = "연락 없이 불참";

            givenCompleteAttendanceChain(recordId, managerId);

            // when
            AttendanceRecord result = service.markAsAbsent(recordId, managerId, reason);

            // then
            assertThat(result).isNotNull();
            verify(attendanceRecordRepository).findById(recordId);
            verify(userRepository).findById(managerId);
            verify(attendanceRecordRepository).save(any(AttendanceRecord.class));
        }

        @Test
        @DisplayName("존재하지 않는 출석 기록을 결석 처리시 AttendanceNotFoundException 발생")
        void markAsAbsent_recordNotFound() {
            // given
            Long recordId = 999L;
            when(attendanceRecordRepository.findById(recordId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.markAsAbsent(recordId, 1L, "사유"))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.RECORD_NOT_FOUND);
                    });

            verify(attendanceRecordRepository).findById(recordId);
            verifyNoInteractions(userRepository, labRepository, attendanceSessionRepository);
        }
    }

    @Nested
    @DisplayName("markAsLate 메서드는")
    class MarkAsLateTests {

        @Test
        @DisplayName("출석 기록을 지각으로 변경 성공")
        void markAsLate_success() {
            // given
            Long recordId = 1L;
            Long managerId = 2L;
            String reason = "교통 체증으로 지각";

            givenCompleteAttendanceChain(recordId, managerId);

            // when
            AttendanceRecord result = service.markAsLate(recordId, managerId, reason);

            // then
            assertThat(result).isNotNull();
            verify(attendanceRecordRepository).findById(recordId);
            verify(attendanceRecordRepository).save(any(AttendanceRecord.class));
        }

        @Test
        @DisplayName("존재하지 않는 출석 기록을 지각 처리시 AttendanceNotFoundException 발생")
        void markAsLate_recordNotFound() {
            // given
            Long recordId = 999L;
            when(attendanceRecordRepository.findById(recordId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.markAsLate(recordId, 1L, "사유"))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.RECORD_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("markAsPresent 메서드는")
    class MarkAsPresentTests {

        @Test
        @DisplayName("출석 기록을 출석으로 변경 성공")
        void markAsPresent_success() {
            // given
            Long recordId = 1L;
            Long managerId = 2L;
            String reason = "출석 인정";

            // ABSENT 상태에서 시작해야 PRESENT로 변경 가능
            givenCompleteAttendanceChain(recordId, managerId, AttendanceStatus.ABSENT);

            // when
            AttendanceRecord result = service.markAsPresent(recordId, managerId, reason);

            // then
            assertThat(result).isNotNull();
            verify(attendanceRecordRepository).findById(recordId);
            verify(attendanceRecordRepository).save(any(AttendanceRecord.class));
        }

        @Test
        @DisplayName("존재하지 않는 출석 기록을 출석 처리시 AttendanceNotFoundException 발생")
        void markAsPresent_recordNotFound() {
            // given
            Long recordId = 999L;
            when(attendanceRecordRepository.findById(recordId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.markAsPresent(recordId, 1L, "사유"))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.RECORD_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("updateAttendanceStatus 메서드는")
    class UpdateAttendanceStatusTests {

        @Test
        @DisplayName("출석 상태를 직접 변경 성공")
        void updateAttendanceStatus_success() {
            // given
            Long recordId = 1L;
            AttendanceStatus newStatus = AttendanceStatus.LATE;
            Long managerId = 2L;
            String reason = "상태 변경";

            givenCompleteAttendanceChain(recordId, managerId);

            // when
            AttendanceRecord result = service.updateAttendanceStatus(recordId, newStatus, managerId, reason);

            // then
            assertThat(result).isNotNull();
            verify(attendanceRecordRepository).findById(recordId);
            verify(attendanceRecordRepository).save(any(AttendanceRecord.class));
        }

        @Test
        @DisplayName("존재하지 않는 출석 기록의 상태 변경시 AttendanceNotFoundException 발생")
        void updateAttendanceStatus_recordNotFound() {
            // given
            Long recordId = 999L;
            when(attendanceRecordRepository.findById(recordId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.updateAttendanceStatus(recordId, AttendanceStatus.LATE, 1L, "사유"))
                    .isInstanceOf(AttendanceNotFoundException.class)
                    .satisfies(ex -> {
                        AttendanceNotFoundException e = (AttendanceNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(AttendanceErrorCode.RECORD_NOT_FOUND);
                    });
        }
    }
}