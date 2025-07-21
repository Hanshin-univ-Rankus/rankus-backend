package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.user.BlacklistedToken;

import java.util.Optional;

public interface BlacklistedTokenRepositoryPort {

    /**
     * 블랙리스트 토큰 저장
     */
    BlacklistedToken save(BlacklistedToken blacklistedToken);

    /**
     * 토큰 ID로 블랙리스트 토큰 조회
     */
    Optional<BlacklistedToken> findByTokenId(String tokenId);

    /**
     * 토큰 ID가 블랙리스트에 있는지 확인
     */
    boolean existsByTokenId(String tokenId);

    /**
     * 만료된 블랙리스트 토큰 삭제
     */
    void deleteExpiredTokens();

    /**
     * 블랙리스트 토큰 삭제
     */
    void delete(BlacklistedToken blacklistedToken);
}