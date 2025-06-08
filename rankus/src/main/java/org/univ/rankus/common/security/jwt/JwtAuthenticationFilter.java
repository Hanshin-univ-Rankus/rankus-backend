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

    private final JwtTokenProvider tokenProvider;
    private final List<String> excludedPaths = List.of(
            "/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/labs/**"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

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

        boolean isExcluded = excludedPaths.stream().anyMatch(p -> pathMatcher.match(p, uri));

        if (!isExcluded && token == null) {
            log.warn("🔒 토큰 누락: 요청 URI = {}", uri);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        if (!isExcluded && bearer != null && !bearer.startsWith("Bearer ")) {
            log.warn("🔒 토큰 형식 오류: 요청 URI = {}", uri);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        if (!isExcluded && token != null && tokenProvider.validateToken(token)) {
            try {
                Claims claims = tokenProvider.parseClaims(token);
                String email = claims.getSubject();

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

        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(String bearer) {
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}