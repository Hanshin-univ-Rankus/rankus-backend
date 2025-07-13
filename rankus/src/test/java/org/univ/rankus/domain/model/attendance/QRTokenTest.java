package org.univ.rankus.domain.model.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("QRToken 도메인 단위 테스트")
class QRTokenTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 정보로 QR 토큰 생성 성공")
        void constructor_validInput_success() {
            // given
            Long labId = 1L;
            Long sessionId = 1L;
            Integer validityMinutes = 5;

            // when
            QRToken token = QRToken.create(labId, sessionId, validityMinutes);

            // then
            assertThat(token.getToken()).isNotBlank();
            assertThat(token.getSessionId()).isEqualTo(sessionId);
            assertThat(token.getGeneratedAt()).isNotNull();
            assertThat(token.getExpiresAt()).isNotNull();
            assertThat(token.getLabId()).isEqualTo(labId);
        }

        @Test
        @DisplayName("랩실 ID 누락시 LAB_ID_REQUIRED 예외 발생")
        void constructor_nullLabId_throwsLabIdRequired() {
            // given
            Long labId = null;
            Long sessionId = 1L;
            Integer validityMinutes = 5;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> QRToken.create(labId, sessionId, validityMinutes));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.LAB_ID_REQUIRED);
        }

        @Test
        @DisplayName("세션 ID 누락시 SESSION_ID_REQUIRED 예외 발생")
        void constructor_nullSessionId_throwsSessionIdRequired() {
            // given
            Long labId = 1L;
            Long sessionId = null;
            Integer validityMinutes = 5;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> QRToken.create(labId, sessionId, validityMinutes));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_ID_REQUIRED);
        }

        @ParameterizedTest(name = "[{index}] validityMinutes={0} → QR_VALIDITY_INVALID 예외")
        @ValueSource(ints = {0, 11})
        @DisplayName("유효시간 범위 초과시 QR_VALIDITY_INVALID 예외 발생")
        void constructor_invalidValidityMinutes_throwsQrValidityInvalid(Integer validityMinutes) {
            // given
            Long labId = 1L;
            Long sessionId = 1L;

            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> QRToken.create(labId, sessionId, validityMinutes));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.QR_VALIDITY_INVALID);
        }
    }

    @Nested
    @DisplayName("토큰 문자열 복원 검증")
    class TokenStringRestorationTests {

        @Test
        @DisplayName("토큰 문자열 복원 성공")
        void fromString_validToken_success() {
            // given
            QRToken originalToken = DomainAttendanceFactory.buildValidToken();

            // when
            QRToken restoredToken = QRToken.fromString(originalToken.getToken());

            // then
            assertThat(restoredToken.getToken()).isEqualTo(originalToken.getToken());
            assertThat(restoredToken.getSessionId()).isEqualTo(originalToken.getSessionId());
            assertThat(restoredToken.getLabId()).isEqualTo(originalToken.getLabId());
        }

        @ParameterizedTest(name = "[{index}] tokenString='{0}' → QR_TOKEN_INVALID 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("비어있거나 null 토큰 문자열 복원시 QR_TOKEN_INVALID 예외 발생")
        void fromString_nullOrEmptyToken_throwsQrTokenInvalid(String tokenString) {
            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> QRToken.fromString(tokenString));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.QR_TOKEN_INVALID);
        }

        @ParameterizedTest(name = "[{index}] tokenString='{0}' → QR_TOKEN_INVALID 예외")
        @ValueSource(strings = {"invalid-token", "abc-def-ghi"})
        @DisplayName("잘못된 형식 토큰 복원시 QR_TOKEN_INVALID 예외 발생")
        void fromString_invalidFormatToken_throwsQrTokenInvalid(String tokenString) {
            // when & then
            AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
                    () -> QRToken.fromString(tokenString));
            assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.QR_TOKEN_INVALID);
        }
    }

    @Nested
    @DisplayName("토큰 유효성 검증")
    class TokenValidityTests {

        @Test
        @DisplayName("토큰 만료 확인 성공")
        void isExpired_success() {
            // given
            QRToken token = DomainAttendanceFactory.buildValidToken();

            // when & then
            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("토큰 유효성 확인 성공")
        void isValid_success() {
            // given
            QRToken token = DomainAttendanceFactory.buildValidToken();
            LocalDateTime currentTime = LocalDateTime.now();

            // when & then
            assertThat(token.isValid(currentTime)).isTrue();
            assertThat(token.isValid(currentTime.plusMinutes(6))).isFalse();
        }

        @Test
        @DisplayName("만료 시간 경계값 테스트")
        void isValid_boundaryTest() {
            // given
            QRToken token = DomainAttendanceFactory.buildValidToken();
            
            // when & then
            LocalDateTime exactExpiryTime = token.getExpiresAt();
            assertThat(token.isValid(exactExpiryTime)).isTrue();
            assertThat(token.isValid(exactExpiryTime.plusSeconds(1))).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰 정보 추출 검증")
    class TokenInfoExtractionTests {

        @Test
        @DisplayName("토큰에서 랩실 ID 추출 성공")
        void getLabId_success() {
            // given
            Long labId = 123L;
            QRToken token = DomainAttendanceFactory.buildTokenWithLabId(labId);

            // when
            Long extractedLabId = token.getLabId();

            // then
            assertThat(extractedLabId).isEqualTo(labId);
        }

        @Test
        @DisplayName("토큰 형식 확인 성공")
        void getToken_formatCheck() {
            // given
            Long labId = 1L;
            Long sessionId = 2L;
            QRToken token = DomainAttendanceFactory.buildTokenWithLabAndSession(labId, sessionId);

            // when
            String tokenString = token.getToken();

            // then
            assertThat(tokenString).matches("\\d+-\\d+-\\d+");
            assertThat(tokenString).startsWith("1-2-");
        }

        @Test
        @DisplayName("토큰 시간 정보 확인 성공")
        void getTimeInfo_success() {
            // given
            LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);
            
            // when
            QRToken token = DomainAttendanceFactory.buildTokenWithValidityMinutes(5);
            
            // then
            LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);
            assertThat(token.getGeneratedAt()).isAfter(beforeCreate);
            assertThat(token.getGeneratedAt()).isBefore(afterCreate);
            assertThat(token.getExpiresAt()).isAfter(token.getGeneratedAt());
            assertThat(token.getExpiresAt()).isEqualTo(token.getGeneratedAt().plusMinutes(5));
        }
    }
}



