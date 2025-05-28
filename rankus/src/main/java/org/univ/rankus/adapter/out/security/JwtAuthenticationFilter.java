package org.univ.rankus.adapter.out.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 인증용 필터
 * - Authorization 헤더에서 Bearer 토큰을 파싱
 * - 토큰 유효성 확인 후 SecurityContext에 Authentication 설정
 * - roles 클레임을 읽어 권한(GrantedAuthority) 세팅
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = parseBearerToken(bearer);
        String uri = request.getRequestURI();
        boolean protectedPath = isProtectedPath(uri);

        // 인증이 필요한 경로에서 토큰 누락 또는 잘못된 형식
        if (protectedPath) {
            if (!StringUtils.hasText(bearer) || token == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
            if (!bearer.startsWith("Bearer ")) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }

        // 토큰 유효성 및 권한 파싱
        if (protectedPath && token != null) {
            if (!tokenProvider.validateToken(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
            try {
                Claims claims = tokenProvider.parseClaims(token);
                String email = claims.getSubject();

                // roles 클레임 읽기
                List<String> roles = claims.get("roles", List.class);
                var authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());

                var auth = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        authorities
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }

        // 다음 필터 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Bearer 토큰만 추출
     */
    private String parseBearerToken(String bearer) {
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    /**
     * 인증이 필요한 보호된 경로인지 판단
     */
    private boolean isProtectedPath(String uri) {
        // 인증 예외 경로
        if (uri.startsWith("/api/auth") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")) {
            return false;
        }
        // 랩실 정보 GET은 공개
        if (uri.startsWith("/api/labs")) {
            return uri.contains("/applications");
        }
        // 나머지 모두 보호
        return true;
    }
}