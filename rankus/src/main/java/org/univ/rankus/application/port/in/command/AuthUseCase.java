package org.univ.rankus.application.port.in.command;

import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.domain.model.user.User;

public interface AuthUseCase {
    /**
     * 로그인 처리
     *
     * @return JWT 토큰과 사용자 정보를 포함한 AuthResponseDto
     */
    AuthResponseDto login(UserLoginRequestDto request);

    /**
     * 회원 가입 처리
     *
     * @return 가입된 User 엔티티
     */
    User signUp(UserRegisterRequestDto request);
}