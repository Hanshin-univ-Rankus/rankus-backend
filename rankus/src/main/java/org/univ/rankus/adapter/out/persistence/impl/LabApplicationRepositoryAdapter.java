package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataLabApplicationRepository;
import org.univ.rankus.application.port.out.LabApplicationRepositoryPort;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.user.User;

import java.util.List;
import java.util.Optional;

/**
 * LabApplicationRepositoryPort 어댑터 구현체
 * - 내부에서 SpringDataLabApplicationRepository를 호출하여 실제 DB 저장·조회·삭제 기능을 위임합니다.
 */
@Repository
@RequiredArgsConstructor
public class LabApplicationRepositoryAdapter implements LabApplicationRepositoryPort {

    private final SpringDataLabApplicationRepository springDataLabApplicationRepository;

    @Override
    public LabApplication save(LabApplication application) {
        return springDataLabApplicationRepository.save(application);
    }

    @Override
    public Optional<LabApplication> findById(Long id) {
        return springDataLabApplicationRepository.findById(id);
    }

    @Override
    public List<LabApplication> findByLabId(Long labId) {
        return springDataLabApplicationRepository.findByLabId(labId);
    }

    @Override
    public boolean existsByLabIdAndUser(Long labId, User user) {
        return springDataLabApplicationRepository.existsByLabIdAndUser(labId, user);
    }

    @Override
    public void delete(LabApplication application) {
        springDataLabApplicationRepository.delete(application);
    }

    @Override
    public boolean existsByLabIdAndUserId(Long labId, Long userId) {
        return springDataLabApplicationRepository.existsByLabIdAndUserId(labId, userId);
    }
}