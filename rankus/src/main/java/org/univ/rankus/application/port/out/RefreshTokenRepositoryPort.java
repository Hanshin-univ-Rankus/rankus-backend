package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.user.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepositoryPort {

    /**
     * 리프레시 토큰 저장
     */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * 토큰 ID로 리프레시 토큰 조회
     */
    Optional<RefreshToken> findByTokenId(String tokenId);

    /**
     * 사용자 이메일로 활성 리프레시 토큰 조회
     */
    Optional<RefreshToken> findByUserEmailAndIsActiveTrue(String userEmail);

    /**
     * 사용자의 모든 리프레시 토큰 비활성화
     */
    void deactivateAllByUserEmail(String userEmail);

    /**
     * 만료된 토큰 삭제
     */
    void deleteExpiredTokens();

    /**
     * 리프레시 토큰 삭제
     */
    void delete(RefreshToken refreshToken);

    /**
     * 토큰 ID 존재 여부 확인
     */
    boolean existsByTokenId(String tokenId);
}