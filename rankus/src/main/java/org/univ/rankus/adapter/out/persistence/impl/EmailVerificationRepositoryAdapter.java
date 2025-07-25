package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataEmailVerificationRepository;
import org.univ.rankus.application.port.out.EmailVerificationRepositoryPort;
import org.univ.rankus.domain.model.user.EmailVerification;

import java.util.Optional;

/**
 * 이메일 인증 리포지토리 어댑터 구현체
 * - EmailVerificationRepositoryPort의 구현체
 * - Spring Data JPA를 통한 데이터 영속성 처리
 * - 헥사고날 아키텍처의 Adapter 계층
 */
@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryAdapter implements EmailVerificationRepositoryPort {

    private final SpringDataEmailVerificationRepository springDataRepository;

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return springDataRepository.save(emailVerification);
    }

    @Override
    public Optional<EmailVerification> findLatestByEmail(String email) {
        return springDataRepository.findLatestByEmail(email);
    }

    @Override
    public Optional<EmailVerification> findById(Long id) {
        return springDataRepository.findById(id);
    }

    @Override
    public boolean existsVerifiedByEmail(String email) {
        return springDataRepository.existsByEmailAndVerifiedTrue(email);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public void deleteAllByEmail(String email) {
        springDataRepository.deleteAllByEmail(email);
    }
}