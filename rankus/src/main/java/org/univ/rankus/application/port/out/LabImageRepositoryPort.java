package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.lab.core.ImageType;
import org.univ.rankus.domain.model.lab.core.LabImage;

import java.util.List;
import java.util.Optional;

/**
 * LabImage 도메인 퍼시스턴스 포트 인터페이스
 * - 서비스 계층은 이 인터페이스를 통해서만 LabImage를 저장·조회·삭제합니다.
 */
public interface LabImageRepositoryPort {

    /**
     * 새로운 LabImage를 저장합니다.
     *
     * @param image 저장할 LabImage 엔티티
     * @return 영속화된 LabImage(저장 후 ID 포함)
     */
    LabImage save(LabImage image);

    /**
     * ID로 LabImage를 조회합니다.
     *
     * @param id 조회할 LabImage ID
     * @return Optional.of(LabImage) 또는 Optional.empty()
     */
    Optional<LabImage> findById(Long id);

    /**
     * 특정 랩실 ID에 속한 모든 LabImage를 조회합니다.
     *
     * @param labId 조회할 랩실 ID
     * @return 해당 랩실에 속한 LabImage 리스트 (빈 리스트 가능)
     */
    List<LabImage> findByLabId(Long labId);

    /**
     * 특정 이미지 타입(ImageType)에 해당하는 모든 LabImage를 조회합니다.
     *
     * @param type 조회할 ImageType (예: REPRESENTATIVE, THUMBNAIL 등)
     * @return 해당 타입에 속한 LabImage 리스트 (빈 리스트 가능)
     */
    List<LabImage> findByType(ImageType type);

    /**
     * LabImage를 삭제합니다.
     *
     * @param image 삭제할 LabImage 엔티티
     */
    void delete(LabImage image);
}