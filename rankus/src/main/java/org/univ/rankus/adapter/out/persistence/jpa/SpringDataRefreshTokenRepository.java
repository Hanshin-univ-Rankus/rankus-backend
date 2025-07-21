package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.univ.rankus.domain.model.user.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenId(String tokenId);

    Optional<RefreshToken> findByUserEmailAndIsActiveTrue(String userEmail);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.userEmail = :userEmail")
    void deactivateAllByUserEmail(@Param("userEmail") String userEmail);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

    boolean existsByTokenId(String tokenId);
}