package org.univ.rankus.adapter.out.persistence.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.univ.rankus.adapter.out.persistence.impl.LabImageRepositoryAdapter;
import org.univ.rankus.adapter.out.persistence.impl.LabRepositoryAdapter;
import org.univ.rankus.application.port.out.LabImageRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.ImageType;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.testutil.config.BaseRepositoryTest;
import org.univ.rankus.testutil.factory.integration.IntegrationLabFactory;
import org.univ.rankus.testutil.factory.integration.IntegrationLabImageFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({LabImageRepositoryAdapter.class, LabRepositoryAdapter.class})
@DisplayName("Spring Data JPA LabImageRepository 통합 테스트")
class SpringDataLabImageRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private LabImageRepositoryPort imgRepo;

    // Adapter가 포트를 구현하므로 LabRepositoryPort 타입으로 주입
    @Autowired
    private LabRepositoryPort labRepo;

    @Test
    @DisplayName("save: LabImage 저장 후 ID 자동 생성")
    void save_generatesId() {
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        LabImage saved = IntegrationLabImageFactory.persistValidLabImage(imgRepo, lab, "https://ex.com/img1.png", ImageType.ADDITIONAL);
        assertNotNull(saved.getId(), "저장된 LabImage는 ID 자동 생성되어야 한다");
        assertTrue(saved.getId() > 0, "생성된 ID는 양수여야 한다");
    }

    @Test
    @DisplayName("findByLabId: 동일한 Lab ID의 이미지만 조회")
    void findByLabId_returnsOnlyThatLabImages() {
        Lab lab1 = IntegrationLabFactory.persistValidLab(labRepo);
        Lab lab2 = IntegrationLabFactory.persistCustomLab(labRepo, "OtherLab", lab1.getCategory(), "desc", "ProfX");

        IntegrationLabImageFactory.persistValidLabImage(imgRepo, lab1, "https://ex.com/a.png", ImageType.REPRESENTATIVE);
        IntegrationLabImageFactory.persistValidLabImage(imgRepo, lab1, "https://ex.com/b.png", ImageType.ADDITIONAL);
        IntegrationLabImageFactory.persistValidLabImage(imgRepo, lab2, "https://ex.com/c.png", ImageType.ADDITIONAL);

        List<LabImage> images1 = imgRepo.findByLabId(lab1.getId());
        assertEquals(2, images1.size(), "lab1에 속한 이미지 2개만 반환되어야 한다");
        images1.forEach(img -> assertEquals(lab1.getId(), img.getLab().getId()));

        List<LabImage> images2 = imgRepo.findByLabId(lab2.getId());
        assertEquals(1, images2.size(), "lab2에 속한 이미지 1개만 반환되어야 한다");
        assertEquals("https://ex.com/c.png", images2.get(0).getImageUrl());
    }

    @Test
    @DisplayName("findByLabId: 없는 Lab ID 조회 시 빈 리스트 반환")
    void findByLabId_empty() {
        List<LabImage> images = imgRepo.findByLabId(9999L);
        assertTrue(images.isEmpty(), "존재하지 않는 Lab ID 조회 시 빈 리스트를 반환해야 한다");
    }

    @Test
    @DisplayName("findByType: 특정 타입의 이미지만 조회")
    void findByType_returnsOnlyThatType() {
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        IntegrationLabImageFactory.persistValidLabImage(imgRepo, lab, "https://u1.png", ImageType.ADDITIONAL);
        IntegrationLabImageFactory.persistValidLabImage(imgRepo, lab, "https://u2.png", ImageType.ADDITIONAL);
        IntegrationLabImageFactory.persistValidLabImage(imgRepo, lab, "https://r1.png", ImageType.REPRESENTATIVE);

        List<LabImage> additional = imgRepo.findByType(ImageType.ADDITIONAL);
        assertEquals(2, additional.size(), "ADDITIONAL 타입 이미지 2개만 반환되어야 한다");
        additional.forEach(img -> assertEquals(ImageType.ADDITIONAL, img.getType()));

        List<LabImage> repr = imgRepo.findByType(ImageType.REPRESENTATIVE);
        assertEquals(1, repr.size(), "REPRESENTATIVE 타입 이미지 1개만 반환되어야 한다");
        assertEquals("https://r1.png", repr.get(0).getImageUrl());
    }

    @Test
    @DisplayName("findByType: 없는 타입 조회 시 빈 리스트 반환")
    void findByType_empty() {
        List<LabImage> images = imgRepo.findByType(null);
        assertTrue(images.isEmpty(), "null 타입 조회 시 빈 리스트 반환");
    }

    @Test
    @DisplayName("save: LabImage URL 수정 시 ID 유지 및 URL 변경")
    void save_updateFields_persistsChanges() {
        Lab lab = IntegrationLabFactory.persistValidLab(labRepo);
        LabImage saved = IntegrationLabImageFactory.persistValidLabImage(imgRepo, lab, "https://before.png", ImageType.ADDITIONAL);
        // update
        saved.setImageUrl("https://after.png");
        LabImage updated = imgRepo.save(saved);
        assertEquals(saved.getId(), updated.getId(), "수정해도 ID 유지");
        assertEquals("https://after.png", updated.getImageUrl(), "imageUrl 수정 반영");
    }
}