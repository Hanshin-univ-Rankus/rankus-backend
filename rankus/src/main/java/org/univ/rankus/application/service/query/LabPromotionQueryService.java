package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.util.List;

/**
 * LabPromotionService 구현체 (포트–어댑터 패턴 적용 버전)
 * - LabRepositoryPort만 바라보고, 내부에서 JPA 구현체는 Adapter가 처리
 * - 조회 메서드에 @Transactional(readOnly = true), 쓰기 메서드에 @Transactional
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 읽기 전용 최적화
public class LabPromotionQueryService implements LabPromotionQueryUseCase {

    private final LabRepositoryPort labRepositoryPort;

    /**
     * 모든 랩실을 랭킹 내림차순으로 조회합니다.
     */
    @Override
    public List<Lab> listLabs() {
        return labRepositoryPort.findAllByRankingDesc();
    }

    /**
     * 특정 랩실을 ID로 조회합니다.
     *
     * @param labId 조회할 랩실 ID
     * @return Lab 엔티티
     * @throws LabNotFoundException (랩실이 존재하지 않으면 404)
     */
    @Override
    public Lab getLabById(Long labId) {
        return labRepositoryPort.findById(labId)
                .orElseThrow(() ->
                        new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND)
                );
    }
}