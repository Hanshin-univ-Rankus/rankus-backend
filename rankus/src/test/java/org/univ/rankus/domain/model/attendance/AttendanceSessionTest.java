package org.univ.rankus.domain.model.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AttendanceSession 도메인 단위 테스트")
class AttendanceSessionTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 정보로 출석 세션 생성 성공")
        void constructor_validInput_success() {
            // given
            Long labId = 1L;
            Long createdBy = 1L;
            String title = "테스트 출석";
            Integer qrValidityMinutes = 5;

            // when
            AttendanceSession session = AttendanceSession.create(labId, createdBy, title, qrValidityMinutes);

            // then
            assertThat(session.getLabId()).isEqualTo(labId);
            assertThat(session.getCreatedBy()).isEqualTo(createdBy);
            assertThat(session.getTitle()).isEqualTo(title);
            assertThat(session.getQrValidityMinutes()).isEqualTo(qrValidityMinutes);
            assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(session.getStartTime()).isNotNull();
            assertThat(session.getEndTime()).isNull();
        }

        @ParameterizedTest(name = "[{index}] title='{0}' → TITLE_REQUIRED 예외")
        @NullSource
        @DisplayName("제목 누락시 TITLE_REQUIRED 예외 발생")
        void constructor_nullTitle_throwsTitleRequired(String title) {
            // given
            Long labId = 1L;
            Long createdBy = 1L;
            Integer qrValidityMinutes = 5;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> AttendanceSession.create(labId, createdBy, title, qrValidityMinutes));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.TITLE_REQUIRED);
        }

        @Test
        @DisplayName("제목 길이 초과시 TITLE_TOO_LONG 예외 발생")
        void constructor_titleTooLong_throwsTitleTooLong() {
            // given
            Long labId = 1L;
            Long createdBy = 1L;
            String title = "a".repeat(101); // 101자
            Integer qrValidityMinutes = 5;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> AttendanceSession.create(labId, createdBy, title, qrValidityMinutes));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.TITLE_TOO_LONG);
        }

        @Test
        @DisplayName("QR 유효시간 누락시 QR_VALIDITY_REQUIRED 예외 발생")
        void constructor_nullQrValidityMinutes_throwsQrValidityRequired() {
            // given
            Long labId = 1L;
            Long createdBy = 1L;
            String title = "테스트 출석";
            Integer qrValidityMinutes = null;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> AttendanceSession.create(labId, createdBy, title, qrValidityMinutes));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.QR_VALIDITY_REQUIRED);
        }

        @ParameterizedTest(name = "[{index}] qrValidityMinutes={0} → QR_VALIDITY_INVALID 예외")
        @ValueSource(ints = {0, 11})
        @DisplayName("QR 유효시간 범위 초과시 QR_VALIDITY_INVALID 예외 발생")
        void constructor_invalidQrValidityMinutes_throwsQrValidityInvalid(Integer qrValidityMinutes) {
            // given
            Long labId = 1L;
            Long createdBy = 1L;
            String title = "테스트 출석";

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> AttendanceSession.create(labId, createdBy, title, qrValidityMinutes));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.QR_VALIDITY_INVALID);
        }

        @Test
        @DisplayName("랩실 ID 누락시 LAB_ID_REQUIRED 예외 발생")
        void constructor_nullLabId_throwsLabIdRequired() {
            // given
            Long labId = null;
            Long createdBy = 1L;
            String title = "테스트 출석";
            Integer qrValidityMinutes = 5;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> AttendanceSession.create(labId, createdBy, title, qrValidityMinutes));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.LAB_ID_REQUIRED);
        }

        @Test
        @DisplayName("생성자 ID 누락시 CREATOR_ID_REQUIRED 예외 발생")
        void constructor_nullCreatedBy_throwsCreatorIdRequired() {
            // given
            Long labId = 1L;
            Long createdBy = null;
            String title = "테스트 출석";
            Integer qrValidityMinutes = 5;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> AttendanceSession.create(labId, createdBy, title, qrValidityMinutes));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.CREATOR_ID_REQUIRED);
        }
    }

    @Nested
    @DisplayName("QR 토큰 생성 검증")
    class QRTokenGenerationTests {

        @Test
        @DisplayName("QR 토큰 생성 성공")
        void generateQRToken_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSessionWithId(1L);

            // when
            QRToken token = session.generateQRToken();

            // then
            assertThat(token).isNotNull();
            assertThat(token.getToken()).isNotBlank();
            assertThat(token.getSessionId()).isEqualTo(session.getSessionId());
            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("비활성 세션에서 QR 생성시 SESSION_NOT_ACTIVE 예외 발생")
        void generateQRToken_inactiveSession_throwsSessionNotActive() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            session.endSession(); // 세션 종료

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> session.generateQRToken());
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_ACTIVE);
        }
    }

    @Nested
    @DisplayName("출석 체크 검증")
    class AttendanceCheckTests {

        @Test
        @DisplayName("출석 체크 성공")
        void checkAttendance_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            Long userId = 2L;
            LocalDateTime checkedAt = LocalDateTime.now();

            // when
            AttendanceRecord record = session.checkAttendance(userId, checkedAt);

            // then
            assertThat(record).isNotNull();
            assertThat(record.getUserId()).isEqualTo(userId);
            assertThat(record.getCheckedAt()).isEqualTo(checkedAt);
            assertThat(record.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
            assertThat(session.getAttendanceRecords()).hasSize(1);
        }

        @Test
        @DisplayName("중복 출석 체크시 ALREADY_CHECKED_IN 예외 발생")
        void checkAttendance_duplicateCheck_throwsAlreadyCheckedIn() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            Long userId = 2L;
            LocalDateTime checkedAt = LocalDateTime.now();

            // 첫 번째 출석 체크
            session.checkAttendance(userId, checkedAt);

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> session.checkAttendance(userId, checkedAt));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.ALREADY_CHECKED_IN);
        }

        @Test
        @DisplayName("비활성 세션에서 출석 체크시 SESSION_NOT_ACTIVE 예외 발생")
        void checkAttendance_inactiveSession_throwsSessionNotActive() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            session.endSession(); // 세션 종료

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> session.checkAttendance(2L, LocalDateTime.now()));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_ACTIVE);
        }
    }

    @Nested
    @DisplayName("세션 상태 관리 검증")
    class SessionStatusTests {

        @Test
        @DisplayName("세션 종료 성공")
        void endSession_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();

            // when
            session.endSession();

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(session.getEndTime()).isNotNull();
        }

        @Test
        @DisplayName("세션 취소 성공")
        void cancelSession_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();

            // when
            session.cancelSession();

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.CANCELLED);
            assertThat(session.getEndTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("세션 관리 기능 검증")
    class SessionManagementTests {

        @Test
        @DisplayName("세션 소유자 확인 성공")
        void isOwnedBy_success() {
            // given
            Long createdBy = 1L;
            AttendanceSession session = DomainAttendanceFactory.buildSessionWithCreator(createdBy);

            // when & then
            assertThat(session.isOwnedBy(createdBy)).isTrue();
            assertThat(session.isOwnedBy(2L)).isFalse();
        }

        @Test
        @DisplayName("제목 수정 성공")
        void updateTitle_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            String newTitle = "수정된 출석";

            // when
            session.updateTitle(newTitle);

            // then
            assertThat(session.getTitle()).isEqualTo(newTitle);
        }

        @Test
        @DisplayName("QR 유효시간 수정 성공")
        void updateQRValidityMinutes_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            Integer newValidityMinutes = 10;

            // when
            session.updateQRValidityMinutes(newValidityMinutes);

            // then
            assertThat(session.getQrValidityMinutes()).isEqualTo(newValidityMinutes);
        }
    }

    @Nested
    @DisplayName("통계 및 조회 검증")
    class StatisticsAndQueryTests {

        @Test
        @DisplayName("출석 통계 계산 성공")
        void calculateStatistics_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            LocalDateTime now = LocalDateTime.now();

            // 출석 기록 생성
            AttendanceRecord record1 = session.checkAttendance(2L, now);
            AttendanceRecord record2 = session.checkAttendance(3L, now);
            AttendanceRecord record3 = session.checkAttendance(4L, now);

            // 한 명을 지각으로 변경
            record2.markAsLate(1L, "지각 처리");

            // when
            AttendanceSession.AttendanceStatistics stats = session.calculateStatistics();

            // then
            assertThat(stats.getTotalMembers()).isEqualTo(3);
            assertThat(stats.getPresentCount()).isEqualTo(2);
            assertThat(stats.getAbsentCount()).isEqualTo(0);
            assertThat(stats.getLateCount()).isEqualTo(1);
            assertThat(stats.getAttendanceRate()).isCloseTo(66.66666666666667, offset(0.00000001));
        }

        @Test
        @DisplayName("특정 사용자 출석 기록 조회 성공")
        void getAttendanceRecord_success() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();
            Long userId = 2L;
            session.checkAttendance(userId, LocalDateTime.now());

            // when
            AttendanceRecord record = session.getAttendanceRecord(userId);

            // then
            assertThat(record).isNotNull();
            assertThat(record.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 출석 기록 조회시 null 반환")
        void getAttendanceRecord_notFound_returnsNull() {
            // given
            AttendanceSession session = DomainAttendanceFactory.buildValidSession();

            // when
            AttendanceRecord record = session.getAttendanceRecord(999L);

            // then
            assertThat(record).isNull();
        }
    }
}



