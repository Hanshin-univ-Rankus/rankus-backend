package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.adapter.in.web.dto.response.AuthTokens;

/**
 * 로그인 성공 시 JWT 토큰을 반환할 때 사용하는 DTO
 */
@Getter
@Builder
@Schema(description = "인증 응답 정보")
public class AuthResponseDto {

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    @Schema(description = "JWT 리프레시 토큰", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String refreshToken;

    @Schema(description = "사용자 정보")
    private final UserResponseDto user;

    public static AuthResponseDto from(AuthTokens tokens) {
        return AuthResponseDto.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .user(tokens.getUser())
                .build();
    }
}