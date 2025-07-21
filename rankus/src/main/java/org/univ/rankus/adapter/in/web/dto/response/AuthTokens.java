package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * JWT 액세스 토큰과 리프레시 토큰 쌍을 반환할 때 사용하는 DTO
 */
@Getter
@Builder
@Schema(description = "인증 토큰 정보")
public class AuthTokens {

    @Schema(description = "JWT 액세스 토큰 (15분 유효)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    @Schema(description = "JWT 리프레시 토큰 (7일 유효)", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String refreshToken;

    @Schema(description = "액세스 토큰 만료 시간", example = "2024-01-15T10:45:00")
    private final LocalDateTime accessTokenExpiresAt;

    @Schema(description = "리프레시 토큰 만료 시간", example = "2024-01-22T10:30:00")
    private final LocalDateTime refreshTokenExpiresAt;

    @Schema(description = "사용자 정보")
    private final UserResponseDto user;

    public static AuthTokens of(String accessToken, String refreshToken,
                                LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt,
                                UserResponseDto user) {
        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .user(user)
                .build();
    }
}