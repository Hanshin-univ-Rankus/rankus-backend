package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.univ.rankus.domain.model.user.BlacklistedToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SpringDataBlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    Optional<BlacklistedToken> findByTokenId(String tokenId);

    boolean existsByTokenId(String tokenId);

    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiresAt < :now")
    void deleteByExpiresAtBefore(@Param("now") LocalDateTime now);
}