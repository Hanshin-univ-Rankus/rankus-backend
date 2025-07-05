package org.univ.rankus.application.service.query;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.LabImageQueryUseCase;
import org.univ.rankus.application.port.out.LabImageRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.core.LabImage;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabImageErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabImageNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 읽기 전용 최적화
public class LabImageQueryService implements LabImageQueryUseCase {

    private final LabRepositoryPort labRepositoryPort;
    private final LabImageRepositoryPort labImageRepositoryPort;

    /**
     * 특정 랩실의 이미지 목록 조회 (인증 사용자)
     */
    @Override
    public List<LabImage> listImagesByLab(Long labId) {
        // 랩실 존재 여부 확인
        labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // LabImage 리스트 반환
        return labImageRepositoryPort.findByLabId(labId);
    }

    /**
     * 단일 이미지 조회 (소유자 또는 랩 권한자)
     */
    @Override
    public LabImage getImageById(Long imageId) {
        return labImageRepositoryPort.findById(imageId)
                .orElseThrow(() ->
                        new LabImageNotFoundException(LabImageErrorCode.IMAGE_NOT_FOUND)
                );
    }
}