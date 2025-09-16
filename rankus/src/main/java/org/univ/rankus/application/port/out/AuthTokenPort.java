package org.univ.rankus.application.port.out;

import org.univ.rankus.adapter.in.web.dto.response.AuthTokens;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;

public interface AuthTokenPort {
    /**
     * 액세스 토큰 생성 (레거시 호환용)
     *
     * @param user 인증된 사용자
     * @return 발급된 JWT 액세스 토큰 문자열
     */
    String generateToken(User user);

    /**
     * 액세스 토큰과 리프레시 토큰 쌍 생성
     *
     * @param user 인증된 사용자
     * @return 액세스 토큰과 리프레시 토큰 정보
     */
    AuthTokens generateTokens(User user);

    /**
     * 리프레시 토큰으로 새로운 액세스 토큰과 리프레시 토큰을 발급 (토큰 순환)
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰과 리프레시 토큰 정보
     */
    AuthTokens refreshAccessToken(String refreshToken);

    /**
     * 리프레시 토큰 유효성 검증
     *
     * @param refreshToken 리프레시 토큰
     * @return 유효성 여부
     */
    boolean validateRefreshToken(String refreshToken);

    /**
     * 리프레시 토큰 무효화 (로그아웃 시 사용)
     *
     * @param refreshToken 무효화할 리프레시 토큰
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * 액세스 토큰 만료 시간 조회
     *
     * @param accessToken 액세스 토큰
     * @return 만료 시간
     */
    LocalDateTime getAccessTokenExpiryTime(String accessToken);

    /**
     * 토큰에서 사용자 이메일 추출
     *
     * @param token JWT 토큰
     * @return 사용자 이메일
     */
    String extractEmailFromToken(String token);

    /**
     * 토큰에서 JTI(토큰 ID) 추출
     *
     * @param token JWT 토큰
     * @return 토큰 ID (JTI)
     */
    String extractTokenId(String token);

    /**
     * 토큰 블랙리스트 등록
     *
     * @param tokenId   토큰 ID (JTI)
     * @param expiresAt 토큰 만료 시간
     */
    void blacklistToken(String tokenId, LocalDateTime expiresAt);

    /**
     * 토큰 블랙리스트 확인
     *
     * @param tokenId 토큰 ID (JTI)
     * @return 블랙리스트 등록 여부
     */
    boolean isTokenBlacklisted(String tokenId);
}
