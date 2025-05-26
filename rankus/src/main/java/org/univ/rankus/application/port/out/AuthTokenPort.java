package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.user.User;

public interface AuthTokenPort {
    /**
     * @param user 인증된 사용자
     * @return 발급된 JWT 토큰 문자열
     */
    String generateToken(User user);
}