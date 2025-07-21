package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataBlacklistedTokenRepository;
import org.univ.rankus.application.port.out.BlacklistedTokenRepositoryPort;
import org.univ.rankus.domain.model.user.BlacklistedToken;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class BlacklistedTokenRepositoryAdapter implements BlacklistedTokenRepositoryPort {

    private final SpringDataBlacklistedTokenRepository repository;

    @Override
    public BlacklistedToken save(BlacklistedToken blacklistedToken) {
        return repository.save(blacklistedToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlacklistedToken> findByTokenId(String tokenId) {
        return repository.findByTokenId(tokenId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTokenId(String tokenId) {
        return repository.existsByTokenId(tokenId);
    }

    @Override
    public void deleteExpiredTokens() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    @Override
    public void delete(BlacklistedToken blacklistedToken) {
        repository.delete(blacklistedToken);
    }
}