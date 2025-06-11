package org.univ.rankus.common.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 토큰을 생성/검증하는 provider
    private final JwtTokenProvider tokenProvider;
    // 인증이 필요 없는 경로 목록
    private final List<String> excludedPaths = List.of(
            "/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/labs/**"
    );
    // 경로 패턴 매칭을 위한 객체
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // JwtTokenProvider를 주입받는 생성자
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * JWT 인증 필터의 핵심 로직
     * 요청이 인증이 필요한 경로라면 JWT 토큰을 검증하고,
     * 인증이 필요 없는 경로라면 필터를 통과시킨다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Authorization 헤더에서 Bearer 토큰 추출
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = parseBearerToken(bearer);
        String uri = request.getRequestURI();

        // 인증이 필요 없는 경로인지 확인
        boolean isExcluded = excludedPaths.stream().anyMatch(p -> pathMatcher.match(p, uri));

        // 인증이 필요한 경로인데 토큰이 없는 경우
        if (!isExcluded && token == null) {
            log.warn("🔒 토큰 누락: 요청 URI = {}", uri);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // 인증이 필요한 경로인데 Bearer 형식이 아닌 경우
        if (!isExcluded && bearer != null && !bearer.startsWith("Bearer ")) {
            log.warn("🔒 토큰 형식 오류: 요청 URI = {}", uri);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // 인증이 필요한 경로이고, 토큰이 유효한 경우
        if (!isExcluded && token != null && tokenProvider.validateToken(token)) {
            try {
                // 토큰에서 클레임(정보) 추출
                Claims claims = tokenProvider.parseClaims(token);
                String email = claims.getSubject();

                // 인증 객체 생성 및 SecurityContext에 저장
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                log.warn("🔒 클레임 파싱 실패: {}", e.getMessage());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * Bearer 토큰 문자열에서 실제 토큰 값만 추출
     * @param bearer Authorization 헤더 값
     * @return 토큰 값 또는 null
     */
    private String parseBearerToken(String bearer) {
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
