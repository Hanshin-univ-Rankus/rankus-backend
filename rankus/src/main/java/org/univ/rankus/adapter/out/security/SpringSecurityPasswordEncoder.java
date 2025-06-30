package org.univ.rankus.adapter.out.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.univ.rankus.domain.model.user.PasswordEncoder ;
/**
 * Spring Security의 PasswordEncoder를 도메인 인터페이스로 어댑팅하는 구현체
 *
 * 도메인 계층이 Spring Security에 직접 의존하지 않도록
 * 어댑터 패턴을 적용한 구현체입니다.
 */
@Component
@RequiredArgsConstructor
public class SpringSecurityPasswordEncoder implements PasswordEncoder {

    private final org.springframework.security.crypto.password.PasswordEncoder springPasswordEncoder;

    @Override
    public String encode(String rawPassword) {
        return springPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return springPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}
