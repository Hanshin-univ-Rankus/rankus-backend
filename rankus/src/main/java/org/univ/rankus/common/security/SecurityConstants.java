package org.univ.rankus.common.security;


public final class SecurityConstants {
    private SecurityConstants() {
    }

    /**
     * 인증 없이 접근을 허용할 URL 패턴 (SecurityConfig와 JwtAuthenticationFilter 공통 사용)
     */
    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/labs",              // GET /api/labs (랩실 목록 조회만)
            "/api/labs/*/images/**",  // 랩실 이미지 조회
            "/api/labs/*/promotions", // 랩실 홍보 정보 조회
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**"
    };
}