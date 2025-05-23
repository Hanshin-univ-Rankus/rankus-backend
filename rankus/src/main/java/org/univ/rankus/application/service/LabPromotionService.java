package org.univ.rankus.application.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;
import org.univ.rankus.application.port.in.LabPromotionUseCase;
import org.univ.rankus.domain.model.lab.Lab;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LabPromotionService implements LabPromotionUseCase {

    private final SpringDataLabRepository repository;

    /**
     * 전체 랩실을 랭킹(높은 점수→낮은 점수) 순으로 조회한다.
     */
    @Override
    public List<Lab> listLabs() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "ranking"));
    }

    /**
     * ID로 특정 랩실을 조회한다.
     * @throws NoSuchElementException 존재하지 않는 ID 요청 시
     */
    @Override
    public Lab getLabById(Long labId) {
        return repository.findById(labId)
                .orElseThrow(() -> new NoSuchElementException(
                        "해당 ID의 랩실을 찾을 수 없습니다: " + labId));
    }
}
