package org.univ.rankus.application.port.in;

import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.lab.ImageType;

import java.util.List;

public interface LabImageUseCase {

    /**
     * 주어진 랩실 ID에 이미지 URL과 타입을 저장하고, 저장된 LabImage를 반환한다.
     */
    LabImage registerImage(Long labId, String imageUrl, ImageType type);

    /**
     * 주어진 랩실 ID에 등록된 모든 이미지를 조회한다.
     */
    List<LabImage> listImages(Long labId);
}
