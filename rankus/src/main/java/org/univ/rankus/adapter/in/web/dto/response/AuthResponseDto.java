package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 성공 시 JWT 토큰을 반환할 때 사용하는 DTO
 */

@Getter
@Builder
public class AuthResponseDto {
    private final String token;
    private final UserResponseDto user;

    public static AuthResponseDto from(String token, UserResponseDto user) {
        return AuthResponseDto.builder()
                .token(token)
                .user(user)
                .build();
    }
}