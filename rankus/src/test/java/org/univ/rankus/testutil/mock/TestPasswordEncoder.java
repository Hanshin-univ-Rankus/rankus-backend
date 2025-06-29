package org.univ.rankus.testutil.mock;

import org.univ.rankus.domain.model.user.PasswordEncoder;

/**
 * 테스트용 PasswordEncoder 구현체
 * 실제 암호화 없이 평문을 그대로 저장하여 테스트 성능 향상
 */
public class TestPasswordEncoder implements PasswordEncoder {
    
    private static final String PREFIX = "encoded:";
    
    @Override
    public String encode(String rawPassword) {
        // 테스트용으로 간단히 prefix만 추가
        return PREFIX + rawPassword;
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        // encoded: prefix를 제거하고 비교
        if (encodedPassword.startsWith(PREFIX)) {
            String decodedPassword = encodedPassword.substring(PREFIX.length());
            return rawPassword.equals(decodedPassword);
        }
        return false;
    }
}