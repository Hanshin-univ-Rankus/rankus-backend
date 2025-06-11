package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.user.User;

public interface AuthUseCase {
    /**
     * 로그인 처리
     * @return JWT 토큰 또는 세션 식별자
     */
    String login(String email, String rawPassword);

    /**
     * 회원 가입 처리
     * @return 가입된 User 엔티티
     */
    User signUp(String name, String email, String rawPassword);
}