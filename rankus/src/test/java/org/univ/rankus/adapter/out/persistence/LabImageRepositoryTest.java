package org.univ.rankus.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.univ.rankus.config.DomainConfig;            // @EnableJpaAuditing
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.lab.ImageType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DomainConfig.class)  // Auditing 활성화
@DisplayName("LabImage 리포지토리 테스트")
class LabImageRepositoryTest {

    @Autowired
    private SpringDataLabRepository labRepo;

    @Autowired
    private SpringDataLabImageRepository imageRepo;

    @Test
    @DisplayName("LabImage 저장 후 조회 시 올바르게 반환되어야 한다")
    void saveAndFindById() {
        // given: Lab 엔티티 저장
        Lab lab = new Lab("TestLab", "설명", "컴퓨터과", LabCategory.AI);
        Lab savedLab = labRepo.save(lab);

        // when: LabImage 저장
        LabImage img = new LabImage(savedLab,
                "https://example.com/img.png",
                ImageType.REPRESENTATIVE);
        LabImage savedImg = imageRepo.save(img);

        // then: 조회 검증
        Optional<LabImage> foundOpt = imageRepo.findById(savedImg.getId());
        assertTrue(foundOpt.isPresent(), "저장한 LabImage를 조회할 수 있어야 한다");
        LabImage found = foundOpt.get();
        assertAll("LabImage 필드 검증",
                () -> assertEquals(savedLab.getId(), found.getLab().getId(), "Lab 연관관계가 설정되어야 한다"),
                () -> assertEquals("https://example.com/img.png", found.getImageUrl()),
                () -> assertEquals(ImageType.REPRESENTATIVE, found.getType())
        );

        // Auditing 검증
        assertNotNull(found.getCreatedAt(), "createdAt이 자동 설정되어야 한다");
        assertNotNull(found.getUpdatedAt(), "updatedAt이 자동 설정되어야 한다");
    }
}
