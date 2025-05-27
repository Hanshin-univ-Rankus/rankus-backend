package org.univ.rankus.application.port.in;

import org.univ.rankus.domain.model.user.User;

/**
 * 애플리케이션의 회원 관련 Use Case 정의
 */
public interface UserUseCase {
    /**
     * 회원가입
     *
     * @param name        사용자 이름
     * @param email       이메일 (중복 불가)
     * @param rawPassword 평문 비밀번호
     * @return 생성된 User 엔티티
     */
    User signUp(String name, String email, String rawPassword);

    // 추가: 로그인
    /**
     * 로그인
     *
     * @param email       이메일
     * @param rawPassword 평문 비밀번호
     * @return 발급된 JWT 토큰
     */
    String login(String email, String rawPassword);
}
