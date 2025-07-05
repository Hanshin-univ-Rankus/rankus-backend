package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataLabImageRepository;
import org.univ.rankus.application.port.out.LabImageRepositoryPort;
import org.univ.rankus.domain.model.lab.core.ImageType;
import org.univ.rankus.domain.model.lab.core.LabImage;

import java.util.List;
import java.util.Optional;

/**
 * LabImageRepositoryPort 어댑터 구현체
 * - 내부에서 SpringDataLabImageRepository를 호출하여 실제 DB 저장·조회·삭제 기능을 위임합니다.
 */
@Repository
@RequiredArgsConstructor
public class LabImageRepositoryAdapter implements LabImageRepositoryPort {

    private final SpringDataLabImageRepository springDataLabImageRepository;

    @Override
    public LabImage save(LabImage image) {
        return springDataLabImageRepository.save(image);
    }

    @Override
    public Optional<LabImage> findById(Long id) {
        return springDataLabImageRepository.findById(id);
    }

    @Override
    public List<LabImage> findByLabId(Long labId) {
        return springDataLabImageRepository.findByLabId(labId);
    }

    @Override
    public List<LabImage> findByType(ImageType type) {
        return springDataLabImageRepository.findByType(type);
    }

    @Override
    public void delete(LabImage image) {
        springDataLabImageRepository.delete(image);
    }
}