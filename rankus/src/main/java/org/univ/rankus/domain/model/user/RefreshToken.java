package org.univ.rankus.domain.model.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserValidationException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JWT 리프레시 토큰 엔티티
 * - 액세스 토큰 갱신을 위한 장기 보관 토큰
 * - 사용자당 하나의 활성 토큰만 유지
 */
@Getter
@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token_email", columnList = "user_email"),
                @Index(name = "idx_refresh_token_active", columnList = "is_active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

    @Id
    @Column(name = "token_id", length = 36)
    private String tokenId;

    @Column(name = "user_email", nullable = false, length = 100)
    private String userEmail;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    /**
     * 새로운 리프레시 토큰 생성
     */
    public static RefreshToken create(String userEmail, long expirationMs) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new UserValidationException(UserErrorCode.EMAIL_REQUIRED);
        }
        if (expirationMs <= 0) {
            throw new UserValidationException(UserErrorCode.TOKEN_EXPIRATION_INVALID);
        }

        RefreshToken token = new RefreshToken();
        token.tokenId = UUID.randomUUID().toString();
        token.userEmail = userEmail.trim();
        token.expiresAt = LocalDateTime.now().plusSeconds(expirationMs / 1000);
        token.isActive = true;

        return token;
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean isValid() {
        return isActive && !isExpired();
    }

    /**
     * 토큰 비활성화 (로그아웃 시 사용)
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 토큰 재활성화 (새로운 리프레시 토큰 발급 시)
     */
    public void reactivate(long expirationMs) {
        this.tokenId = UUID.randomUUID().toString();
        this.expiresAt = LocalDateTime.now().plusSeconds(expirationMs / 1000);
        this.isActive = true;
    }
}