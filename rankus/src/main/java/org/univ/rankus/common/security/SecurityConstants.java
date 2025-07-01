package org.univ.rankus.common.security;


public final class SecurityConstants {
    private SecurityConstants() {
    }

    /**
     * 인증 없이 접근을 허용할 URL 패턴 (SecurityConfig와 JwtAuthenticationFilter 공통 사용)
     */
    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/labs/**", // GET /api/labs 만 허용하려면, POST/PUT 엔드포인트는 별도 인증 필요
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };
}