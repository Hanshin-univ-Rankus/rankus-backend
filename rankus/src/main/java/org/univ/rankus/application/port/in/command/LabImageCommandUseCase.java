package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.lab.core.ImageType;
import org.univ.rankus.domain.model.lab.core.LabImage;

/**
 * LabImage 도메인에 대한 애플리케이션 서비스 인터페이스
 */

public interface LabImageCommandUseCase {
    /**
     * 새 이미지 등록 (랩 리더/매니저만 가능)
     */
    LabImage addImage(Long labId, String imageUrl, ImageType type);

    /**
     * 이미지 삭제 (소유자 또는 랩 권한자)
     */
    void deleteImage(Long imageId, Long userId);
}