package org.univ.rankus.common.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.univ.rankus.common.security.SecurityConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.univ.rankus.common.exception.ErrorResponse;
import org.univ.rankus.common.exception.GlobalErrorCode;

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
    // 인증 실패 시 일관된 에러 응답을 위한 EntryPoint (선택)
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    // URL 패턴 매칭을 위한 PathMatcher
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * JwtAuthenticationFilter 생성자 (기존 호환용)
     */
    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            UserDetailsService userDetailsService
    ) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = null;
    }

    /**
     * JwtAuthenticationFilter 생성자 (EntryPoint 주입)
     */
    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            UserDetailsService userDetailsService,
            JwtAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
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
        String requestURI = request.getRequestURI();
        boolean isPublicUrl = isPublicUrl(requestURI, request.getMethod());

        // Authorization 헤더에서 Bearer 토큰 추출
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            // 토큰 유효성 검사
            if (tokenProvider.validateToken(token)) {
                Authentication auth = tokenProvider.getAuthentication(token, userDetailsService);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else if (!isPublicUrl) {
                // 보호 URL에서 잘못된 토큰이면 일관된 에러 포맷 반환
                SecurityContextHolder.clearContext();
                if (authenticationEntryPoint != null) {
                    authenticationEntryPoint.commence(
                            request,
                            response,
                            new BadCredentialsException("Invalid JWT token")
                    );
                } else {
                    // EntryPoint 미주입 시에도 ErrorResponse 포맷으로 반환
                    ErrorResponse error = ErrorResponse.of(
                            GlobalErrorCode.UNAUTHORIZED,
                            request.getRequestURI(),
                            null
                    );
                    response.setStatus(GlobalErrorCode.UNAUTHORIZED.getStatus().value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getWriter(), error);
                }
                return;
            }
        }
        // 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }

    /**
     * 요청 URI가 공개 URL인지 확인합니다. (메서드 고려)
     */
    private boolean isPublicUrl(String requestURI, String method) {
        // 항상 공개 URL
        for (String pattern : SecurityConstants.ALWAYS_PUBLIC_URLS) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }
        // GET 전용 공개 URL
        if (HttpMethod.GET.matches(method)) {
            for (String pattern : SecurityConstants.PUBLIC_GET_URLS) {
                if (pathMatcher.match(pattern, requestURI)) {
                    return true;
                }
            }
        }
        return false;
    }
}
