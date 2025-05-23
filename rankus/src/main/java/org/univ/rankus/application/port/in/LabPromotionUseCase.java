package org.univ.rankus.application.port.in;


import org.univ.rankus.domain.model.lab.Lab;

import java.util.List;

public interface LabPromotionUseCase {

    /**
     * 전체 랩실 리스트를 랭킹 순(높은 점수 → 낮은 점수)으로 반환합니다.
     */
    List<Lab> listLabs();

    /**
     * ID로 특정 랩실의 상세 정보를 조회합니다.
     * @throws java.util.NoSuchElementException 조회할 수 없는 ID인 경우
     */
    Lab getLabById(Long labId);
}