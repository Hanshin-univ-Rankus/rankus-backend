package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 성공 시 JWT 토큰을 반환할 때 사용하는 DTO
 */

@Getter
@Builder
@Schema(description = "인증 응답 정보")
public class AuthResponseDto {
    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String token;

    @Schema(description = "사용자 정보")
    private final UserResponseDto user;

    public static AuthResponseDto from(String token, UserResponseDto user) {
        return AuthResponseDto.builder()
                .token(token)
                .user(user)
                .build();
    }
}