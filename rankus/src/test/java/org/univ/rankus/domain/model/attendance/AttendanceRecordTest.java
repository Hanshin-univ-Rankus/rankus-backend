package org.univ.rankus.domain.model.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AttendanceRecord 도메인 단위 테스트")
class AttendanceRecordTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 정보로 출석 기록 생성 성공")
        void constructor_validInput_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            Long userId = 2L;
            LocalDateTime checkedAt = LocalDateTime.now();

            // when
            AttendanceRecord record = AttendanceRecord.create(session, userId, checkedAt);

            // then
            assertThat(record.getAttendanceSession()).isEqualTo(session);
            assertThat(record.getUserId()).isEqualTo(userId);
            assertThat(record.getCheckedAt()).isEqualTo(checkedAt);
            assertThat(record.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
            assertThat(record.getIsManuallyAdjusted()).isFalse();
        }

        @Test
        @DisplayName("세션 누락시 SESSION_NOT_FOUND 예외 발생")
        void constructor_nullSession_throwsSessionNotFound() {
            // given
            AttendanceSession session = null;
            Long userId = 2L;
            LocalDateTime checkedAt = LocalDateTime.now();

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> AttendanceRecord.create(session, userId, checkedAt));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_FOUND);
        }

        @ParameterizedTest(name = "[{index}] userId='{0}' → USER_ID_REQUIRED 예외")
        @NullSource
        @DisplayName("사용자 ID 누락시 USER_ID_REQUIRED 예외 발생")
        void constructor_nullUserId_throwsUserIdRequired(Long userId) {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            LocalDateTime checkedAt = LocalDateTime.now();

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> AttendanceRecord.create(session, userId, checkedAt));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.USER_ID_REQUIRED);
        }

        @Test
        @DisplayName("체크인 시간 누락시 CHECKED_AT_REQUIRED 예외 발생")
        void constructor_nullCheckedAt_throwsCheckedAtRequired() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            Long userId = 2L;
            LocalDateTime checkedAt = null;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> AttendanceRecord.create(session, userId, checkedAt));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.CHECKED_AT_REQUIRED);
        }
    }

    @Nested
    @DisplayName("상태 변경 검증")
    class StatusUpdateTests {

        @Test
        @DisplayName("결석 처리 성공")
        void markAsAbsent_success() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
            Long adjustedBy = 1L;
            String reason = "무단 결석";

            // when
            record.markAsAbsent(adjustedBy, reason);

            // then
            assertThat(record.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
            assertThat(record.getIsManuallyAdjusted()).isTrue();
            assertThat(record.getAdjustmentReason()).isEqualTo(reason);
            assertThat(record.getAdjustedBy()).isEqualTo(adjustedBy);
            assertThat(record.getAdjustedAt()).isNotNull();
        }

        @Test
        @DisplayName("지각 처리 성공")
        void markAsLate_success() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
            Long adjustedBy = 1L;
            String reason = "지각 처리";

            // when
            record.markAsLate(adjustedBy, reason);

            // then
            assertThat(record.getStatus()).isEqualTo(AttendanceStatus.LATE);
            assertThat(record.getIsManuallyAdjusted()).isTrue();
            assertThat(record.getAdjustmentReason()).isEqualTo(reason);
            assertThat(record.getAdjustedBy()).isEqualTo(adjustedBy);
            assertThat(record.getAdjustedAt()).isNotNull();
        }

        @Test
        @DisplayName("출석 처리 성공")
        void markAsPresent_success() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
            Long adjustedBy = 1L;
            String reason = "출석 복원";

            // 먼저 결석으로 변경
            record.markAsAbsent(adjustedBy, "결석 처리");

            // when
            record.markAsPresent(adjustedBy, reason);

            // then
            assertThat(record.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
            assertThat(record.getIsManuallyAdjusted()).isTrue();
            assertThat(record.getAdjustmentReason()).isEqualTo(reason);
            assertThat(record.getAdjustedBy()).isEqualTo(adjustedBy);
            assertThat(record.getAdjustedAt()).isNotNull();
        }

        @Test
        @DisplayName("수정자 ID 누락시 ADJUSTED_BY_REQUIRED 예외 발생")
        void markAsAbsent_nullAdjustedBy_throwsAdjustedByRequired() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
            Long adjustedBy = null;
            String reason = "결석 처리";

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> record.markAsAbsent(adjustedBy, reason));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.ADJUSTED_BY_REQUIRED);
        }

        @Test
        @DisplayName("수정 사유 누락시 ADJUSTMENT_REASON_REQUIRED 예외 발생")
        void markAsAbsent_nullReason_throwsAdjustmentReasonRequired() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
            Long adjustedBy = 1L;
            String reason = null;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> record.markAsAbsent(adjustedBy, reason));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.ADJUSTMENT_REASON_REQUIRED);
        }

        @Test
        @DisplayName("수정 사유 길이 초과시 ADJUSTMENT_REASON_TOO_LONG 예외 발생")
        void markAsAbsent_reasonTooLong_throwsAdjustmentReasonTooLong() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
            Long adjustedBy = 1L;
            String reason = "a".repeat(201); // 201자

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> record.markAsAbsent(adjustedBy, reason));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.ADJUSTMENT_REASON_TOO_LONG);
        }

        @Test
        @DisplayName("동일 상태로 변경시 INVALID_STATUS_TRANSITION 예외 발생")
        void updateStatus_sameStatus_throwsInvalidTransition() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
            Long adjustedBy = 1L;
            String reason = "출석 처리";

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> record.updateStatus(AttendanceStatus.PRESENT, adjustedBy, reason));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    @Nested
    @DisplayName("소유권 및 상태 확인 검증")
    class OwnershipAndStatusTests {

        @Test
        @DisplayName("기록 소유자 확인 성공")
        void isOwnedBy_success() {
            // given
            Long userId = 2L;
            AttendanceRecord record = DomainAttendanceFactory.buildRecordWithUser(userId);

            // when & then
            assertThat(record.isOwnedBy(userId)).isTrue();
            assertThat(record.isOwnedBy(3L)).isFalse();
        }

        @Test
        @DisplayName("세션 소속 확인 성공")
        void belongsToSession_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSessionWithId(1L);
            AttendanceRecord record = DomainAttendanceFactory.buildRecordWithSession(session);

            // when & then
            assertThat(record.belongsToSession(session.getSessionId())).isTrue();
            assertThat(record.belongsToSession(999L)).isFalse();
        }

        @Test
        @DisplayName("출석 상태 확인 메서드 성공")
        void statusCheck_success() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();

            // when & then
            assertThat(record.isPresent()).isTrue();
            assertThat(record.isAbsent()).isFalse();
            assertThat(record.isLate()).isFalse();

            // 지각으로 변경
            record.markAsLate(1L, "지각 처리");
            assertThat(record.isPresent()).isFalse();
            assertThat(record.isAbsent()).isFalse();
            assertThat(record.isLate()).isTrue();
        }

        @Test
        @DisplayName("지각 체크인 확인 성공")
        void isCheckedInLate_true() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();

            // 세션 시작 후 10분 뒤에 체크인
            LocalDateTime lateCheckedAt = session.getStartTime().plusMinutes(10);
            AttendanceRecord record = AttendanceRecord.create(session, 2L, lateCheckedAt);

            // when & then
            assertThat(record.isCheckedInLate()).isTrue();
        }

        @Test
        @DisplayName("정상 체크인 확인 성공")
        void isCheckedInLate_false() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();

            // 세션 시작 후 3분 뒤에 체크인
            LocalDateTime normalCheckedAt = session.getStartTime().plusMinutes(3);
            AttendanceRecord record = AttendanceRecord.create(session, 2L, normalCheckedAt);

            // when & then
            assertThat(record.isCheckedInLate()).isFalse();
        }

        @Test
        @DisplayName("출석 기록 요약 정보 반환 성공")
        void getSummary_success() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();
            record.markAsLate(1L, "지각 처리");

            // when
            AttendanceRecord.AttendanceRecordSummary summary = record.getSummary();

            // then
            assertThat(summary.getUserId()).isEqualTo(2L);
            assertThat(summary.getStatus()).isEqualTo(AttendanceStatus.LATE);
            assertThat(summary.getIsManuallyAdjusted()).isTrue();
            assertThat(summary.getAdjustmentReason()).isEqualTo("지각 처리");
            assertThat(summary.getAdjustedBy()).isEqualTo(1L);
            assertThat(summary.getAdjustedAt()).isNotNull();
        }

        @Test
        @DisplayName("수동 수정 여부 확인 성공")
        void isManuallyAdjusted_success() {
            // given
            AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();

            // when & then
            assertThat(record.isManuallyAdjusted()).isFalse();

            // 수정 후
            record.markAsLate(1L, "지각 처리");
            assertThat(record.isManuallyAdjusted()).isTrue();
        }
    }
}