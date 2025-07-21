package org.univ.rankus.common.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements AuthTokenPort {

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
                .setId(tokenId) // JTI (JWT ID) 추가
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = parseClaims(token);
            String tokenId = claims.getBody().getId();

            // 블랙리스트 확인
            if (tokenId != null && blacklistedTokenRepository.existsByTokenId(tokenId)) {
                return false;
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String refreshAccessToken(String refreshTokenId) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenId(refreshTokenId)
                .orElseThrow(() -> new UserValidationException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            if (refreshToken.isExpired()) {
                throw new UserValidationException(UserErrorCode.REFRESH_TOKEN_EXPIRED);
            } else {
                throw new UserValidationException(UserErrorCode.REFRESH_TOKEN_INVALID);
            }
        }

        User user = userRepository.findByEmail(refreshToken.getUserEmail())
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        return generateAccessToken(user);
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
        String email = parseClaims(token).getBody().getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}