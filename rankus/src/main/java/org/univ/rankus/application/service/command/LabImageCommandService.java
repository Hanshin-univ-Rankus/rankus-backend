package org.univ.rankus.application.service.command;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.univ.rankus.application.port.in.command.LabImageCommandUseCase;
import org.univ.rankus.application.port.out.LabImageRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.ImageType;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.lab.exception.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional  // 쓰기 트랜잭션
public class LabImageCommandService implements LabImageCommandUseCase {

    private final LabRepositoryPort labRepositoryPort;
    private final LabImageRepositoryPort labImageRepositoryPort;

    /**
     * 새 이미지 등록 (랩 리더/매니저만 가능)
     */
    @Override
    public LabImage addImage(Long labId, String imageUrl, ImageType type) {
        // 1) 랩실 존재 여부 확인 (없으면 예외)
        var lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 2) 도메인 생성자 내부에서 유효성 검증 수행
        LabImage image;
        try {
            image = new LabImage(lab, imageUrl, type);
        } catch (LabImageValidationException ex) {
            throw ex; // validation 실패 시 상위로 전달 (400/409 처리)
        }

        // 3) 저장 후 반환
        return labImageRepositoryPort.save(image);
    }

    /**
     * 이미지 삭제 (소유자 또는 랩 권한자)
     */
    @Override
    public void deleteImage(Long imageId, Long userId) {
        // 1) 이미지 존재 여부 확인
        var image = labImageRepositoryPort.findById(imageId)
                .orElseThrow(() ->
                        new LabImageNotFoundException(LabImageErrorCode.IMAGE_NOT_FOUND)
                );

        // 2) TODO: userId 기반 권한 검증 로직 추가
        //    예: image.getOwnerId() == userId 또는 랩 권한자 여부 체크

        // 3) 삭제
        try {
            labImageRepositoryPort.delete(image);
        } catch (Exception ex) {
            // 삭제 불가 시 적절한 예외로 변환
            throw new LabImageValidationException(LabImageErrorCode.IMAGE_DELETE_FAILED);
        }
    }
}