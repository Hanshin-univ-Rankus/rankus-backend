package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataLabRepository;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;

import java.util.List;
import java.util.Optional;

/**
 * LabRepositoryPort 어댑터 구현체
 * - 내부에서 SpringDataLabRepository를 호출하여 실제 DB 저장·조회 기능을 위임합니다.
 */
@Repository
@RequiredArgsConstructor
public class LabRepositoryAdapter implements LabRepositoryPort {

    private final SpringDataLabRepository springDataLabRepository;

    @Override
    public Lab save(Lab lab) {
        return springDataLabRepository.save(lab);
    }

    @Override
    public Optional<Lab> findById(Long id) {
        return springDataLabRepository.findById(id);
    }

    @Override
    public List<Lab> findAll() {
        return springDataLabRepository.findAll();
    }

    @Override
    public List<Lab> findAllByRankingDesc() {
        // SpringDataLabRepository에 default 메서드가 정의되어 있으므로, 그대로 호출
        return springDataLabRepository.findAllByRankingDesc();
    }

    @Override
    public void delete(Lab lab) {
        springDataLabRepository.delete(lab);
    }

    @Override
    public boolean existsByName(String name) {
        return springDataLabRepository.existsByName(name);
    }
}