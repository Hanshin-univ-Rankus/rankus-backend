package org.univ.rankus.domain.model.user;

/**
 * 도메인 계층의 비밀번호 암호화 인터페이스
 * <p>
 * 이 인터페이스는 도메인 계층이 외부 기술(Spring Security)에 의존하지 않도록
 * 의존성 역전 원칙을 적용한 도메인 인터페이스입니다.
 */
public interface PasswordEncoder {

    /**
     * 평문 비밀번호를 암호화합니다.
     *
     * @param rawPassword 평문 비밀번호
     * @return 암호화된 비밀번호
     */
    String encode(String rawPassword);

    /**
     * 평문 비밀번호와 암호화된 비밀번호가 일치하는지 확인합니다.
     *
     * @param rawPassword     평문 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 일치 여부
     */
    boolean matches(String rawPassword, String encodedPassword);
}