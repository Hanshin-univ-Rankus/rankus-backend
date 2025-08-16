package org.univ.rankus.common.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            UserDetailsService userDetailsService,
            JwtAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            tokenProvider.validateToken(token); // 유효성 검증, 실패 시 예외 발생
            Authentication auth = tokenProvider.getAuthentication(token, userDetailsService);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (AuthenticationException e) {
            // 토큰 유효성 검증 실패 시, EntryPoint로 처리 위임
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, e);
            return; // 필터 체인 중단
        }

        chain.doFilter(request, response);
    }
}
