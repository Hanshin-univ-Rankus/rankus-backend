package org.univ.rankus.application.port.in.query;

import org.univ.rankus.domain.model.lab.LabImage;

import java.util.List;

public interface LabImageQueryUseCase {
    /**
     * 특정 랩실의 이미지 목록 조회 (인증 사용자)
     */
    List<LabImage> listImagesByLab(Long labId);

    /**
     * 단일 이미지 조회 (소유자 또는 랩 권한자)
     */
    LabImage getImageById(Long imageId);
}