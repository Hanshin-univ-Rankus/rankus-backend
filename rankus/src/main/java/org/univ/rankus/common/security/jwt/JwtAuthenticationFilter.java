package org.univ.rankus.common.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.univ.rankus.common.security.SecurityConstants;

import java.io.IOException;

/**
 * JWT 인증을 처리하는 Spring Security 필터입니다.
 * HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출하고,
 * 토큰이 유효하면 인증 정보를 SecurityContext에 저장합니다.
 * 특정 경로는 필터링을 건너뛸 수 있습니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 토큰 관련 기능을 제공하는 Provider
    private final JwtTokenProvider tokenProvider;
    // 사용자 정보를 로드하는 서비스
    private final UserDetailsService userDetailsService;
    // URL 패턴 매칭을 위한 PathMatcher
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * JwtAuthenticationFilter 생성자
     *
     * @param tokenProvider      JWT 토큰 Provider
     * @param userDetailsService 사용자 정보 서비스
     */
    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            UserDetailsService userDetailsService
    ) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * HTTP 요청을 필터링하는 메서드입니다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        // 공개 URL인지 확인
        String requestURI = request.getRequestURI();
        boolean isPublicUrl = isPublicUrl(requestURI);

        // Authorization 헤더에서 Bearer 토큰 추출
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            // 토큰 유효성 검사
            if (tokenProvider.validateToken(token)) {
                // 토큰에서 인증 정보 추출 및 SecurityContext에 저장
                Authentication auth = tokenProvider.getAuthentication(token, userDetailsService);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else if (!isPublicUrl) {
                // 토큰이 유효하지 않고 공개 URL이 아닌 경우에만 오류 응답
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Invalid JWT token\"}");
                response.getWriter().flush();
                return; // 필터 체인 중단
            }
        }
        // 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }

    /**
     * 요청 URI가 공개 URL인지 확인합니다.
     */
    private boolean isPublicUrl(String requestURI) {
        for (String publicPattern : SecurityConstants.PUBLIC_URLS) {
            if (pathMatcher.match(publicPattern, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
