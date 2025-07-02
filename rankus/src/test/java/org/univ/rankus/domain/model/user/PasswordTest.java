package org.univ.rankus.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.user.exception.PasswordErrorCode;
import org.univ.rankus.domain.model.user.exception.PasswordValidationException;
import org.univ.rankus.testutil.mock.TestPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password 값 객체 단위 테스트")
class PasswordTest {

    private final org.univ.rankus.domain.model.user.PasswordEncoder encoder = new TestPasswordEncoder();

    @Nested
    @DisplayName("fromRaw() 예외 검증")
    class FromRawExceptionTests {

        @ParameterizedTest(name = "[{index}] raw=''{0}'' → PASSWORD_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("raw가 null 또는 blank일 경우 PASSWORD_REQUIRED 예외 발생")
        void rawNullOrBlank_throwsRequired(String raw) {
            // when & then
            PasswordValidationException ex = assertThrows(
                    PasswordValidationException.class,
                    () -> Password.fromRaw(raw, encoder)
            );
            // then
            assertEquals(PasswordErrorCode.PASSWORD_REQUIRED, ex.getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] raw.length={0} (<8) → PASSWORD_TOO_SHORT 예외")
        @ValueSource(strings = {"1234567", "abcdefg"})
        @DisplayName("raw 길이가 8 미만일 경우 PASSWORD_TOO_SHORT 예외 발생")
        void rawTooShort_throwsTooShort(String raw) {
            // when & then
            PasswordValidationException ex = assertThrows(
                    PasswordValidationException.class,
                    () -> Password.fromRaw(raw, encoder)
            );
            assertEquals(PasswordErrorCode.PASSWORD_TOO_SHORT, ex.getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] raw.length={0} (>255) → PASSWORD_TOO_LONG 예외")
        @ValueSource(ints = {256, 300})
        @DisplayName("raw 길이가 255 초과일 경우 PASSWORD_TOO_LONG 예외 발생")
        void rawTooLong_throwsTooLong(int length) {
            // given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) sb.append('a');
            String raw = sb.toString();

            // when & then
            PasswordValidationException ex = assertThrows(
                    PasswordValidationException.class,
                    () -> Password.fromRaw(raw, encoder)
            );
            assertEquals(PasswordErrorCode.PASSWORD_TOO_LONG, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("fromRaw() 정상 흐름")
    class FromRawSuccessTests {

        @ParameterizedTest(name = "[{index}] raw=''{0}'' → 정상 생성 및 matches 확인")
        @ValueSource(strings = {"Password!23", "Abcdefgh", "12345678"})
        @DisplayName("유효한 raw로부터 Password 객체 생성 및 matches() 검증")
        void validRaw_createsPasswordAndMatches(String raw) {
            // when
            Password pw = Password.fromRaw(raw, encoder);

            // then
            assertNotNull(pw, "Password 객체는 null이 아니어야 한다");
            assertNotNull(pw.getHashed(), "hashed 필드는 null이 아니어야 한다");
            assertTrue(pw.matches(raw, encoder), "matches(raw) 메서드는 원본과 일치해야 한다");
        }
    }

    @Nested
    @DisplayName("matches() 예외 및 검증")
    class MatchesTests {

        @ParameterizedTest(name = "[{index}] raw=''{0}'' → false 반환")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("raw가 null 또는 blank일 경우 false 반환")
        void nullOrBlankRaw_returnsFalse(String raw) {
            // given
            Password pw = Password.fromRaw("validPWD1", encoder);

            // when
            boolean result = pw.matches(raw, encoder);

            // then
            assertFalse(result, "raw가 null 또는 blank이면 false여야 한다");
        }

        @ParameterizedTest(name = "[{index}] raw=''{0}'' → true 반환")
        @ValueSource(strings = {"validPWD1", "1234Abcd"})
        @DisplayName("일치하는 raw일 경우 true 반환")
        void matchingRaw_returnsTrue(String raw) {
            // given
            Password pw = Password.fromRaw(raw, encoder);

            // when
            boolean result = pw.matches(raw, encoder);

            // then
            assertTrue(result, "일치하는 raw에 대해 true를 반환해야 한다");
        }

        @ParameterizedTest(name = "[{index}] raw=''{0}'' → false 반환")
        @ValueSource(strings = {"wrong1", "Password23"})
        @DisplayName("불일치 raw일 경우 false 반환")
        void nonMatchingRaw_returnsFalse(String wrong) {
            // given
            Password pw = Password.fromRaw("validPWD1", encoder);

            // when
            boolean result = pw.matches(wrong, encoder);

            // then
            assertFalse(result, "불일치 raw에 대해 false를 반환해야 한다");
        }
    }
}