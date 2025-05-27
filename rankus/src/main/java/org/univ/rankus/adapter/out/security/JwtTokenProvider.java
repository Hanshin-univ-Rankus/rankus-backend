package org.univ.rankus.adapter.out.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.domain.model.user.User;

import java.security.Key;
import java.util.Date;


/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider
 * AuthTokenPort를 구현하여 애플리케이션 계층에 토큰 발급 기능을 제공
 */
@Component
public class JwtTokenProvider implements AuthTokenPort {

    // 비밀키(환경변수 또는 application.yml에 설정)
    private final Key key;
    // 토큰 만료시간(ms)
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secretKey,
            @Value("${security.jwt.expiration-ms}") long validityInMilliseconds
    ) {
        // Base64로 인코딩된 secretKey를 디코딩하여 Key 객체 생성
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.validityInMilliseconds = validityInMilliseconds;
    }

    /**
     * AuthTokenPort 인터페이스 구현
     * @param user 인증된 사용자 엔티티
     * @return JWT 토큰 문자열
     */
    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(user.getEmail())            // 토큰 제목: 사용자 이메일
                .claim("name", user.getName())        // 추가 클레임: 사용자 이름
                .claim("labId", user.getLab() != null ? user.getLab().getId() : null)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 클레임(정보) 파싱
     */
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰 유효성 확인 (만료 여부)
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
