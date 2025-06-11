package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.lab.ImageType;
import org.univ.rankus.domain.model.lab.LabImage;

import java.util.List;

/**
 * 순수 JPA 기반 LabImage 저장소 인터페이스
 * - JpaRepository<LabImage, Long>을 상속하면 기본 CRUD 메서드가 모두 제공됩니다.
 * - 추가로 LabImage 조회를 위한 커스텀 메서드를 선언해 둡니다.
 */
@Repository
public interface SpringDataLabImageRepository extends JpaRepository<LabImage, Long> {

    /**
     * 랩실 ID로 LabImage 리스트를 조회합니다.
     */
    List<LabImage> findByLabId(Long labId);

    /**
     * 이미지 타입(ImageType)으로 LabImage 리스트를 조회합니다.
     */
    List<LabImage> findByType(ImageType imageType);
}