package org.univ.rankus.application.port.in.command;

import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

public interface AuthUseCase {
    /**
     * 로그인 처리
     * @return JWT 토큰과 사용자 정보를 포함한 AuthResponseDto
     */
    AuthResponseDto login(String email, String rawPassword);

    /**
     * 회원 가입 처리
     * @return 가입된 User 엔티티
     */
    User signUp(String name, String email, String rawPassword, Role role);
}