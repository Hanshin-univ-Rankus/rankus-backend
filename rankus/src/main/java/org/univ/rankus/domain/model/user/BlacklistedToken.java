package org.univ.rankus.domain.model.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserValidationException;

import java.time.LocalDateTime;

/**
 * 블랙리스트된 토큰 엔티티
 * - 로그아웃된 토큰이나 무효화된 토큰 관리
 * - 토큰 재사용 방지를 위한 블랙리스트
 */
@Getter
@Entity
@Table(
        name = "blacklisted_tokens",
        indexes = {
                @Index(name = "idx_blacklisted_token_id", columnList = "token_id"),
                @Index(name = "idx_blacklisted_token_expires", columnList = "expires_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistedToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_id", nullable = false, length = 100, unique = true)
    private String tokenId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "reason", length = 50)
    private String reason;

    /**
     * 새로운 블랙리스트 토큰 생성
     */
    public static BlacklistedToken create(String tokenId, LocalDateTime expiresAt, String reason) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new UserValidationException(UserErrorCode.TOKEN_INVALID);
        }
        if (expiresAt == null) {
            throw new UserValidationException(UserErrorCode.TOKEN_EXPIRATION_INVALID);
        }

        BlacklistedToken token = new BlacklistedToken();
        token.tokenId = tokenId.trim();
        token.expiresAt = expiresAt;
        token.reason = reason != null ? reason.trim() : "LOGOUT";

        return token;
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}