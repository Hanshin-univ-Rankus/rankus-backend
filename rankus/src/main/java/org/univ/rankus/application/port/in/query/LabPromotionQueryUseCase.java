package org.univ.rankus.application.port.in.query;


import org.univ.rankus.domain.model.lab.Lab;

import java.util.List;

public interface LabPromotionQueryUseCase {

    /**
     * 전체 랩실 리스트를 랭킹 순(높은 점수 → 낮은 점수)으로 반환합니다.
     */
    List<Lab> listLabs();

    /**
     * ID로 특정 랩실의 상세 정보를 조회합니다.
     *
     * @throws org.univ.rankus.domain.model.lab.exception.LabNotFoundException
     */
    Lab getLabById(Long labId);
}