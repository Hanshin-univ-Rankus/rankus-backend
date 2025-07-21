package org.univ.rankus.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserValidationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RefreshToken 도메인 단위 테스트")
class RefreshTokenTest {

    @Nested
    @DisplayName("생성자 검증")
    class CreateTests {

        @Test
        @DisplayName("유효한 정보로 리프레시 토큰 생성 성공")
        void create_validInput_success() {
            // given
            String userEmail = "test@example.com";
            long expirationMs = 7 * 24 * 60 * 60 * 1000L; // 7일

            // when
            RefreshToken refreshToken = RefreshToken.create(userEmail, expirationMs);

            // then
            assertThat(refreshToken.getTokenId()).isNotBlank();
            assertThat(refreshToken.getTokenId()).hasSize(36); // UUID 길이
            assertThat(refreshToken.getUserEmail()).isEqualTo(userEmail);
            assertThat(refreshToken.getExpiresAt()).isAfter(LocalDateTime.now());
            assertThat(refreshToken.isActive()).isTrue();
        }

        @ParameterizedTest
        @DisplayName("사용자 이메일이 null이거나 공백일 때 EMAIL_REQUIRED 예외 발생")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        void create_invalidEmail_throwsEmailRequired(String invalidEmail) {
            // given
            long expirationMs = 7 * 24 * 60 * 60 * 1000L;

            // when & then
            assertThatThrownBy(() -> RefreshToken.create(invalidEmail, expirationMs))
                    .isInstanceOf(UserValidationException.class)
                    .satisfies(ex -> {
                        UserValidationException e = (UserValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.EMAIL_REQUIRED);
                    });
        }

        @ParameterizedTest
        @DisplayName("만료 시간이 유효하지 않을 때 TOKEN_EXPIRATION_INVALID 예외 발생")
        @ValueSource(longs = {0L, -1L, -1000L})
        void create_invalidExpiration_throwsTokenExpirationInvalid(long invalidExpirationMs) {
            // given
            String userEmail = "test@example.com";

            // when & then
            assertThatThrownBy(() -> RefreshToken.create(userEmail, invalidExpirationMs))
                    .isInstanceOf(UserValidationException.class)
                    .satisfies(ex -> {
                        UserValidationException e = (UserValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.TOKEN_EXPIRATION_INVALID);
                    });
        }

        @Test
        @DisplayName("토큰 ID는 매번 다른 UUID로 생성됨")
        void create_generatesDifferentUUIDs() {
            // given
            String userEmail = "test@example.com";
            long expirationMs = 7 * 24 * 60 * 60 * 1000L;

            // when
            RefreshToken token1 = RefreshToken.create(userEmail, expirationMs);
            RefreshToken token2 = RefreshToken.create(userEmail, expirationMs);

            // then
            assertThat(token1.getTokenId()).isNotEqualTo(token2.getTokenId());
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class ValidationTests {

        @Test
        @DisplayName("활성 상태이고 만료되지 않은 토큰은 유효함")
        void isValid_activeAndNotExpired_returnsTrue() {
            // given
            String userEmail = "test@example.com";
            long expirationMs = 7 * 24 * 60 * 60 * 1000L; // 7일
            RefreshToken refreshToken = RefreshToken.create(userEmail, expirationMs);

            // when & then
            assertThat(refreshToken.isValid()).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰은 유효하지 않음")
        void isValid_expired_returnsFalse() {
            // given - 매우 짧은 만료 시간으로 토큰 생성
            String userEmail = "test@example.com";
            long expirationMs = 1L; // 1ms 후 만료
            RefreshToken refreshToken = RefreshToken.create(userEmail, expirationMs);

            // when - 토큰이 만료될 때까지 대기
            try {
                Thread.sleep(5); // 5ms 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // then - 만료된 토큰은 유효하지 않음
            assertThat(refreshToken.isValid()).isFalse();
            assertThat(refreshToken.isExpired()).isTrue();
        }

        @Test
        @DisplayName("비활성화된 토큰은 유효하지 않음")
        void isValid_inactive_returnsFalse() {
            // given
            String userEmail = "test@example.com";
            long expirationMs = 7 * 24 * 60 * 60 * 1000L;
            RefreshToken refreshToken = RefreshToken.create(userEmail, expirationMs);

            // when
            refreshToken.deactivate();

            // then
            assertThat(refreshToken.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰 관리")
    class TokenManagementTests {

        @Test
        @DisplayName("토큰 비활성화 성공")
        void deactivate_activeToken_success() {
            // given
            String userEmail = "test@example.com";
            long expirationMs = 7 * 24 * 60 * 60 * 1000L;
            RefreshToken refreshToken = RefreshToken.create(userEmail, expirationMs);

            // when
            refreshToken.deactivate();

            // then
            assertThat(refreshToken.isActive()).isFalse();
            assertThat(refreshToken.isValid()).isFalse();
        }

        @Test
        @DisplayName("만료 여부 확인")
        void isExpired_checkExpirationStatus() {
            // given - 1초 후 만료되는 토큰과 장기간 유효한 토큰
            String userEmail = "test@example.com";
            long shortExpirationMs = 1L; // 1ms 후 만료
            long longExpirationMs = 7 * 24 * 60 * 60 * 1000L; // 7일

            RefreshToken shortToken = RefreshToken.create(userEmail, shortExpirationMs);
            RefreshToken longToken = RefreshToken.create(userEmail, longExpirationMs);

            // when & then - 장기 토큰은 만료되지 않음
            assertThat(longToken.isExpired()).isFalse();

            // when - 짧은 토큰이 만료될 때까지 대기
            try {
                Thread.sleep(5); // 5ms 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // then - 짧은 토큰은 만료됨
            assertThat(shortToken.isExpired()).isTrue();
        }

        @Test
        @DisplayName("토큰 정보 검증")
        void tokenProperties_checkAllFields() {
            // given
            String userEmail = "user@test.com";
            long expirationMs = 3 * 24 * 60 * 60 * 1000L; // 3일

            // when
            RefreshToken refreshToken = RefreshToken.create(userEmail, expirationMs);

            // then
            assertThat(refreshToken.getTokenId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
            assertThat(refreshToken.getUserEmail()).isEqualTo(userEmail);
            assertThat(refreshToken.getExpiresAt()).isAfter(LocalDateTime.now());
            assertThat(refreshToken.getExpiresAt()).isBefore(LocalDateTime.now().plusDays(4));
            assertThat(refreshToken.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("비즈니스 로직")
    class BusinessLogicTests {

        @Test
        @DisplayName("같은 사용자의 토큰도 각각 고유한 ID를 가짐")
        void create_sameUser_differentTokenIds() {
            // given
            String userEmail = "same@example.com";
            long expirationMs = 7 * 24 * 60 * 60 * 1000L;

            // when
            RefreshToken token1 = RefreshToken.create(userEmail, expirationMs);
            RefreshToken token2 = RefreshToken.create(userEmail, expirationMs);
            RefreshToken token3 = RefreshToken.create(userEmail, expirationMs);

            // then
            assertThat(token1.getTokenId()).isNotEqualTo(token2.getTokenId());
            assertThat(token2.getTokenId()).isNotEqualTo(token3.getTokenId());
            assertThat(token1.getTokenId()).isNotEqualTo(token3.getTokenId());
        }

        @Test
        @DisplayName("토큰 생성 시간이 만료 시간보다 이전임")
        void create_creationTimeBeforeExpiration() {
            // given
            String userEmail = "test@example.com";
            long expirationMs = 1 * 60 * 60 * 1000L; // 1시간
            LocalDateTime beforeCreation = LocalDateTime.now();

            // when
            RefreshToken refreshToken = RefreshToken.create(userEmail, expirationMs);
            LocalDateTime afterCreation = LocalDateTime.now();

            // then
            assertThat(refreshToken.getExpiresAt()).isAfter(beforeCreation);
            assertThat(refreshToken.getExpiresAt()).isAfter(afterCreation);
        }

        @Test
        @DisplayName("토큰 재활성화 성공")
        void reactivate_deactivatedToken_success() {
            // given
            String userEmail = "test@example.com";
            long expirationMs = 7 * 24 * 60 * 60 * 1000L;
            RefreshToken refreshToken = RefreshToken.create(userEmail, expirationMs);
            String originalTokenId = refreshToken.getTokenId();

            // 토큰을 비활성화
            refreshToken.deactivate();
            assertThat(refreshToken.isValid()).isFalse();

            // when - 재활성화
            long newExpirationMs = 14 * 24 * 60 * 60 * 1000L; // 14일
            refreshToken.reactivate(newExpirationMs);

            // then
            assertThat(refreshToken.isActive()).isTrue();
            assertThat(refreshToken.isValid()).isTrue();
            assertThat(refreshToken.getTokenId()).isNotEqualTo(originalTokenId); // 새로운 UUID
            assertThat(refreshToken.getExpiresAt()).isAfter(LocalDateTime.now());
        }
    }
}