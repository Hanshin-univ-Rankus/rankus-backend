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

@DisplayName("BlacklistedToken 도메인 단위 테스트")
class BlacklistedTokenTest {

    @Nested
    @DisplayName("생성자 검증")
    class CreateTests {

        @Test
        @DisplayName("유효한 정보로 블랙리스트 토큰 생성 성공")
        void create_validInput_success() {
            // given
            String tokenId = "test-token-123";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
            String reason = "LOGOUT";

            // when
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, expiresAt, reason);

            // then
            assertThat(blacklistedToken.getTokenId()).isEqualTo(tokenId);
            assertThat(blacklistedToken.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(blacklistedToken.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("reason이 null일 때 기본값 LOGOUT으로 설정")
        void create_nullReason_defaultsToLogout() {
            // given
            String tokenId = "test-token-123";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
            String reason = null;

            // when
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, expiresAt, reason);

            // then
            assertThat(blacklistedToken.getReason()).isEqualTo("LOGOUT");
        }

        @Test
        @DisplayName("reason이 공백일 때 트림 처리")
        void create_whitespaceReason_trimmed() {
            // given
            String tokenId = "test-token-123";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
            String reason = "  SECURITY_VIOLATION  ";

            // when
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, expiresAt, reason);

            // then
            assertThat(blacklistedToken.getReason()).isEqualTo("SECURITY_VIOLATION");
        }

        @ParameterizedTest
        @DisplayName("토큰 ID가 null이거나 공백일 때 TOKEN_INVALID 예외 발생")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        void create_invalidTokenId_throwsTokenInvalid(String invalidTokenId) {
            // given
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
            String reason = "LOGOUT";

            // when & then
            assertThatThrownBy(() -> BlacklistedToken.create(invalidTokenId, expiresAt, reason))
                    .isInstanceOf(UserValidationException.class)
                    .satisfies(ex -> {
                        UserValidationException e = (UserValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.TOKEN_INVALID);
                    });
        }

        @Test
        @DisplayName("만료 시간이 null일 때 TOKEN_EXPIRATION_INVALID 예외 발생")
        void create_nullExpiresAt_throwsTokenExpirationInvalid() {
            // given
            String tokenId = "test-token-123";
            LocalDateTime expiresAt = null;
            String reason = "LOGOUT";

            // when & then
            assertThatThrownBy(() -> BlacklistedToken.create(tokenId, expiresAt, reason))
                    .isInstanceOf(UserValidationException.class)
                    .satisfies(ex -> {
                        UserValidationException e = (UserValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.TOKEN_EXPIRATION_INVALID);
                    });
        }

        @Test
        @DisplayName("토큰 ID가 공백으로만 구성될 때 트림 후 빈 문자열이 되어 예외 발생")
        void create_tokenIdWithSpacesOnly_throwsException() {
            // given
            String tokenId = "    ";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
            String reason = "LOGOUT";

            // when & then
            assertThatThrownBy(() -> BlacklistedToken.create(tokenId, expiresAt, reason))
                    .isInstanceOf(UserValidationException.class)
                    .satisfies(ex -> {
                        UserValidationException e = (UserValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(UserErrorCode.TOKEN_INVALID);
                    });
        }

        @Test
        @DisplayName("토큰 ID 트림 처리")
        void create_tokenIdWithSpaces_trimmed() {
            // given
            String tokenId = "  test-token-123  ";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
            String reason = "LOGOUT";

            // when
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, expiresAt, reason);

            // then
            assertThat(blacklistedToken.getTokenId()).isEqualTo("test-token-123");
        }
    }

    @Nested
    @DisplayName("만료 여부 검증")
    class ExpirationTests {

        @Test
        @DisplayName("미래 만료 시간을 가진 토큰은 만료되지 않음")
        void isExpired_futureExpiration_returnsFalse() {
            // given
            String tokenId = "test-token-123";
            LocalDateTime futureExpiration = LocalDateTime.now().plusDays(1);
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, futureExpiration, "LOGOUT");

            // when & then
            assertThat(blacklistedToken.isExpired()).isFalse();
        }

        @Test
        @DisplayName("과거 만료 시간을 가진 토큰은 만료됨")
        void isExpired_pastExpiration_returnsTrue() {
            // given
            String tokenId = "test-token-123";
            LocalDateTime pastExpiration = LocalDateTime.now().minusDays(1);
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, pastExpiration, "LOGOUT");

            // when & then
            assertThat(blacklistedToken.isExpired()).isTrue();
        }

        @Test
        @DisplayName("매우 짧은 만료 시간 후 토큰 만료 확인")
        void isExpired_shortExpiration_returnsTrue() {
            // given - 1ms 후 만료되는 토큰
            String tokenId = "test-token-123";
            LocalDateTime shortExpiration = LocalDateTime.now().plusNanos(1_000_000); // 1ms
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, shortExpiration, "LOGOUT");

            // when - 토큰이 만료될 때까지 대기
            try {
                Thread.sleep(5); // 5ms 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // then - 만료된 토큰
            assertThat(blacklistedToken.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("토큰 정보 검증")
    class TokenInfoTests {

        @Test
        @DisplayName("토큰 정보 모든 필드 확인")
        void tokenProperties_checkAllFields() {
            // given
            String tokenId = "jwt-token-abc123";
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
            String reason = "SECURITY_VIOLATION";

            // when
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, expiresAt, reason);

            // then
            assertThat(blacklistedToken.getTokenId()).isEqualTo(tokenId);
            assertThat(blacklistedToken.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(blacklistedToken.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("ID 필드는 생성 후 null 상태")
        void getId_afterCreation_isNull() {
            // given
            String tokenId = "test-token-123";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, expiresAt, "LOGOUT");

            // when & then
            assertThat(blacklistedToken.getId()).isNull();
        }

        @Test
        @DisplayName("다양한 reason 값 처리")
        void create_variousReasons_success() {
            // given
            String tokenId = "test-token-123";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

            // when & then
            BlacklistedToken logoutToken = BlacklistedToken.create(tokenId + "1", expiresAt, "LOGOUT");
            assertThat(logoutToken.getReason()).isEqualTo("LOGOUT");

            BlacklistedToken securityToken = BlacklistedToken.create(tokenId + "2", expiresAt, "SECURITY_VIOLATION");
            assertThat(securityToken.getReason()).isEqualTo("SECURITY_VIOLATION");

            BlacklistedToken expiredToken = BlacklistedToken.create(tokenId + "3", expiresAt, "TOKEN_REFRESH");
            assertThat(expiredToken.getReason()).isEqualTo("TOKEN_REFRESH");

            BlacklistedToken nullReasonToken = BlacklistedToken.create(tokenId + "4", expiresAt, null);
            assertThat(nullReasonToken.getReason()).isEqualTo("LOGOUT");
        }
    }

    @Nested
    @DisplayName("비즈니스 로직")
    class BusinessLogicTests {

        @Test
        @DisplayName("같은 토큰 ID로 여러 번 생성 가능")
        void create_sameTokenId_allowsMultipleCreation() {
            // given
            String tokenId = "duplicate-token-123";
            LocalDateTime expiresAt1 = LocalDateTime.now().plusDays(1);
            LocalDateTime expiresAt2 = LocalDateTime.now().plusDays(2);

            // when
            BlacklistedToken token1 = BlacklistedToken.create(tokenId, expiresAt1, "LOGOUT");
            BlacklistedToken token2 = BlacklistedToken.create(tokenId, expiresAt2, "SECURITY_VIOLATION");

            // then - 도메인 객체 레벨에서는 중복 허용, DB 제약조건은 별개
            assertThat(token1.getTokenId()).isEqualTo(token2.getTokenId());
            assertThat(token1.getExpiresAt()).isNotEqualTo(token2.getExpiresAt());
            assertThat(token1.getReason()).isNotEqualTo(token2.getReason());
        }

        @Test
        @DisplayName("만료 시간 경계값 테스트")
        void isExpired_boundaryTest() {
            // given
            String tokenId = "boundary-test-token";
            LocalDateTime exactExpiryTime = LocalDateTime.now().plusSeconds(1);
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, exactExpiryTime, "LOGOUT");

            // when & then - 만료 시간과 정확히 같은 시점
            try {
                Thread.sleep(1100); // 1.1초 대기 (1초 + 여유시간)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertThat(blacklistedToken.isExpired()).isTrue();
        }

        @Test
        @DisplayName("토큰 생성 시간이 만료 시간보다 이전임")
        void create_creationTimeBeforeExpiration() {
            // given
            String tokenId = "time-test-token";
            LocalDateTime beforeCreation = LocalDateTime.now();
            LocalDateTime futureExpiration = LocalDateTime.now().plusHours(1);

            // when
            BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, futureExpiration, "LOGOUT");
            LocalDateTime afterCreation = LocalDateTime.now();

            // then
            assertThat(blacklistedToken.getExpiresAt()).isAfter(beforeCreation);
            assertThat(blacklistedToken.getExpiresAt()).isAfter(afterCreation);
        }
    }
}