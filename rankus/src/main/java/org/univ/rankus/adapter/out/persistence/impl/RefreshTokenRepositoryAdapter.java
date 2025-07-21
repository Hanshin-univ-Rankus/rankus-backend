package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataRefreshTokenRepository;
import org.univ.rankus.application.port.out.RefreshTokenRepositoryPort;
import org.univ.rankus.domain.model.user.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final SpringDataRefreshTokenRepository repository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return repository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenId(String tokenId) {
        return repository.findByTokenId(tokenId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByUserEmailAndIsActiveTrue(String userEmail) {
        return repository.findByUserEmailAndIsActiveTrue(userEmail);
    }

    @Override
    public void deactivateAllByUserEmail(String userEmail) {
        repository.deactivateAllByUserEmail(userEmail);
    }

    @Override
    public void deleteExpiredTokens() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        repository.delete(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTokenId(String tokenId) {
        return repository.existsByTokenId(tokenId);
    }
}