package org.univ.rankus.common.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.univ.rankus.adapter.in.web.dto.response.AuthTokens;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.application.port.out.BlacklistedTokenRepositoryPort;
import org.univ.rankus.application.port.out.RefreshTokenRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.BlacklistedToken;
import org.univ.rankus.domain.model.user.RefreshToken;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.domain.model.user.exception.UserValidationException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements AuthTokenPort {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final BlacklistedTokenRepositoryPort blacklistedTokenRepository;
    private final UserRepositoryPort userRepository;

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.access-expiration-ms:900000}") // 15분 기본값
    private long accessExpirationMs;

    @Value("${security.jwt.refresh-expiration-ms:604800000}") // 7일 기본값
    private long refreshExpirationMs;

    @Value("${security.jwt.expiration-ms:3600000}") // 레거시 호환용
    private long legacyExpirationMs;

    @PostConstruct
    public void logSecretKeyDetails() {
        try {
            log.info("Initializing JwtTokenProvider...");
            log.info("JWT Secret Key Length: {}", secretKey.length());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            log.info("JWT Secret Key SHA-256 Hash: {}", hexString.toString());
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not initialize SHA-256 MessageDigest", e);
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(User user) {
        return generateAccessToken(user);
    }

    @Override
    public AuthTokens generateTokens(User user) {
        // 기존 리프레시 토큰 비활성화
        refreshTokenRepository.deactivateAllByUserEmail(user.getEmail());

        // 새로운 액세스 토큰 생성
        String accessToken = generateAccessToken(user);
        LocalDateTime accessExpiryTime = LocalDateTime.now().plusSeconds(accessExpirationMs / 1000);

        // 새로운 리프레시 토큰 생성
        RefreshToken refreshToken = RefreshToken.create(user.getEmail(), refreshExpirationMs);
        refreshTokenRepository.save(refreshToken);

        UserResponseDto userDto = UserResponseDto.from(user);

        return AuthTokens.of(
                accessToken,
                refreshToken.getTokenId(),
                accessExpiryTime,
                refreshToken.getExpiresAt(),
                userDto
        );
    }

    private String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpirationMs);
        String tokenId = UUID.randomUUID().toString();

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name()) // 역할 정보를 클레임에 추가
                .setId(tokenId) // JTI (JWT ID) 추가
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(60) // 시계 불일치 허용 (진단용)
                .build()
                .parseClaimsJws(token);
    }

    public void validateToken(String token) {
        try {
            Jws<Claims> claims = parseClaims(token);
            String tokenId = claims.getBody().getId();

            if (tokenId != null && blacklistedTokenRepository.existsByTokenId(tokenId)) {
                throw new JwtAuthenticationException(JwtErrorCode.TOKEN_BLACKLISTED);
            }

        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_SIGNATURE_INVALID);
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_MALFORMED);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_UNSUPPORTED);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_EXPIRED);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException(JwtErrorCode.TOKEN_ILLEGAL_ARGUMENT);
        }
    }

    @Override
    public AuthTokens refreshAccessToken(String refreshTokenId) {
        // 1. 리프레시 토큰 조회 및 검증
        RefreshToken refreshToken = refreshTokenRepository.findByTokenId(refreshTokenId)
                .orElseThrow(() -> new UserValidationException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 2. 토큰 유효성(만료, 비활성화) 검사
        if (!refreshToken.isValid()) {
            // 비정상적인 토큰이므로 삭제하여 재사용 방지
            refreshTokenRepository.delete(refreshToken);
            if (refreshToken.isExpired()) {
                throw new UserValidationException(UserErrorCode.REFRESH_TOKEN_EXPIRED);
            }
            // 만료되지 않았지만 비활성화된 토큰 -> 탈취 후 사용 시도일 수 있음
            throw new UserValidationException(UserErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 3. 사용자 정보 조회
        User user = userRepository.findByEmail(refreshToken.getUserEmail())
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 4. 새로운 토큰 쌍 생성 (generateTokens가 기존 모든 토큰 비활성화 및 새 토큰 생성을 담당)
        // 이것이 바로 "Refresh Token Rotation"의 핵심입니다.
        return generateTokens(user);
    }

    @Override
    public boolean validateRefreshToken(String refreshTokenId) {
        return refreshTokenRepository.findByTokenId(refreshTokenId)
                .map(RefreshToken::isValid)
                .orElse(false);
    }

    @Override
    public void revokeRefreshToken(String refreshTokenId) {
        refreshTokenRepository.findByTokenId(refreshTokenId)
                .ifPresent(token -> {
                    token.deactivate();
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public LocalDateTime getAccessTokenExpiryTime(String accessToken) {
        try {
            Jws<Claims> claims = parseClaims(accessToken);
            Date expiration = claims.getBody().getExpiration();
            return LocalDateTime.ofInstant(expiration.toInstant(), ZoneOffset.UTC);
        } catch (JwtException e) {
            throw new UserValidationException(UserErrorCode.TOKEN_INVALID);
        }
    }

    @Override
    public String extractEmailFromToken(String token) {
        try {
            return parseClaims(token).getBody().getSubject();
        } catch (JwtException e) {
            throw new UserValidationException(UserErrorCode.TOKEN_INVALID);
        }
    }

    @Override
    public void blacklistToken(String tokenId, LocalDateTime expiresAt) {
        BlacklistedToken blacklistedToken = BlacklistedToken.create(tokenId, expiresAt, "LOGOUT");
        blacklistedTokenRepository.save(blacklistedToken);
    }

    @Override
    public boolean isTokenBlacklisted(String tokenId) {
        return blacklistedTokenRepository.existsByTokenId(tokenId);
    }

    public Authentication getAuthentication(
            String token,
            UserDetailsService userDetailsService
    ) {
        Jws<Claims> claimsJws = parseClaims(token);
        Claims claims = claimsJws.getBody();
        String email = claims.getSubject();
        String roleFromToken = claims.get("role", String.class);

        // UserDetails는 DB에서 계속 조회하여 전체 Principal 정보를 유지합니다.
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 하지만, 권한은 토큰에 명시된 것을 기준으로 새로 생성하여 사용합니다.
        // 이를 통해 DB 복제 지연(Replication Lag)이 발생해도 토큰에 담긴 권한을 신뢰하여 일관성을 보장합니다.
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleFromToken));

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
        );
    }
}
