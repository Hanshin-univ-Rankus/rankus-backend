package org.univ.rankus.common.security;


public final class SecurityConstants {
    private SecurityConstants() {
    }

    /**
     * 모든 HTTP 메서드에서 인증 없이 접근을 허용할 URL 패턴
     */
    public static final String[] ALWAYS_PUBLIC_URLS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**"
    };

    /**
     * GET 메서드에서만 인증 없이 접근을 허용할 URL 패턴
     */
    public static final String[] PUBLIC_GET_URLS = {
            "/api/labs",              // 랩실 목록 조회만 GET 허용
            "/api/labs/*/images/**",  // 랩실 이미지 조회(GET)
            "/api/labs/*/promotions"  // 랩실 홍보 정보 조회(GET)
    };

    /**
     * 기존 호환: 항상 공개 URL과 동일하게 유지 (점진적 마이그레이션용)
     */
    public static final String[] PUBLIC_URLS = ALWAYS_PUBLIC_URLS;
}