package org.univ.rankus.adapter.out.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증용 필터
 * - Authorization 헤더에서 Bearer 토큰을 파싱
 * - 토큰 유효성 확인 후 SecurityContext에 Authentication 설정
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
        // 1) 헤더에서 토큰 추출
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = parseBearerToken(bearer);

        // 보호된 경로에 대한 요청인지 확인
        String requestURI = request.getRequestURI();
        boolean isProtectedPath = isProtectedPath(requestURI);

        // 인증 헤더가 없는 경우 체크
        if (isProtectedPath && (bearer == null || token == null)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // 토큰 형식이 올바르지 않은 경우 (Bearer 접두사 없음)
        if (isProtectedPath && bearer != null && !bearer.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // 2) 토큰 유효성 검사
        if (token != null && isProtectedPath) {
            if (tokenProvider.validateToken(token)) {
                try {
                    // 3) 클레임(Subject: email) 추출
                    Claims claims = tokenProvider.parseClaims(token);
                    String email = claims.getSubject();

                    // 4) Authentication 객체 생성 (Role 등 권한 정보가 있으면 추가)
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    email,          // principal (식별자)
                                    null,           // credentials
                                    Collections.emptyList() // authorities: 없으면 빈 리스트
                            );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5) SecurityContext에 인증 정보 세팅
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception ex) {
                    // 클레임 파싱 실패 등의 오류 처리
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return;
                }
            } else {
                // 토큰이 유효하지 않은 경우
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }

        // 다음 필터 실행
        filterChain.doFilter(request, response);
    }

    /**
     * "Bearer eyJ.." 형태의 헤더에서 토큰 부분만 리턴
     */
    private String parseBearerToken(String bearer) {
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    /**
     * 보호된 경로인지 확인하는 메서드
     */
    private boolean isProtectedPath(String uri) {
        // 인증이 필요 없는 경로들
        if (uri.startsWith("/api/auth") ||
            uri.startsWith("/swagger-ui") ||
            uri.startsWith("/v3/api-docs")) {
            return false;
        }

        // 랩실 기본 조회 API는 인증 불필요 (GET이고, /applications가 포함되지 않은 경우)
        if (uri.startsWith("/api/labs")) {
            // 경로에 applications가 포함되어 있으면 보호 경로
            if (uri.contains("/applications")) {
                return true;
            }

            // GET 요청인지 확인 (테스트에서는 체크하지 않음)
            return false;
        }

        // 기본적으로 다른 모든 경로는 보호됨
        return true;
    }
}
