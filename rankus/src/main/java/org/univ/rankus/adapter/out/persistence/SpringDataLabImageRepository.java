package org.univ.rankus.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.univ.rankus.domain.model.lab.ImageType;
import org.univ.rankus.domain.model.lab.LabImage;

import java.util.List;

public interface SpringDataLabImageRepository extends JpaRepository<LabImage, Long> {
    // 필요하다면 findByLabId(Long labId) 같은 커스텀 메서드 추가
    List<LabImage> findByLabId(Long labId);

    List<LabImage> findAllByLabId(Long id);

    List<LabImage> findAllByType(ImageType imageType);
}