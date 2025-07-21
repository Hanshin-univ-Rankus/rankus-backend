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
     * 사용자 인증 (토큰 생성 없이 사용자 객체만 반환)
     *
     * @param request 로그인 요청 정보
     * @return 인증된 사용자 객체
     */
    User authenticate(UserLoginRequestDto request);

    /**
     * 회원 가입 처리
     *
     * @return 가입된 User 엔티티
     */
    User signUp(UserRegisterRequestDto request);

    /**
     * 로그아웃 처리
     *
     * @param userEmail   사용자 이메일
     * @param accessToken 현재 액세스 토큰
     */
    void logout(String userEmail, String accessToken);
}