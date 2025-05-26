package org.univ.rankus.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.config.DomainConfig;
import org.univ.rankus.domain.model.lab.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DomainConfig.class)
@DisplayName("LabImageRepository JPA 테스트")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LabImageRepositoryTest {

    @Autowired
    private SpringDataLabRepository labRepo;

    @Autowired
    private SpringDataLabImageRepository imageRepo;

    @Nested
    @DisplayName("LabImage 저장 및 조회")
    class SaveAndQueryTest {

        @Test
        @DisplayName("LabImage 저장 후 ID로 조회하면 동일한 엔티티가 반환된다")
        void 저장_후_ID조회() {
            // given
            Lab lab = new Lab("TestLab", "설명", "컴퓨터과", LabCategory.AI);
            Lab savedLab = labRepo.save(lab);

            LabImage img = new LabImage(savedLab, "https://example.com/img.png", ImageType.REPRESENTATIVE);
            LabImage savedImg = imageRepo.save(img);

            // when
            Optional<LabImage> foundOpt = imageRepo.findById(savedImg.getId());

            // then
            assertTrue(foundOpt.isPresent(), "저장한 LabImage를 조회할 수 있어야 한다");
            LabImage found = foundOpt.get();
            assertAll("LabImage 필드 검증",
                    () -> assertEquals(savedLab.getId(), found.getLab().getId(), "Lab 연관관계가 설정되어야 한다"),
                    () -> assertEquals("https://example.com/img.png", found.getImageUrl()),
                    () -> assertEquals(ImageType.REPRESENTATIVE, found.getType())
            );
            // Auditing 필드 검증
            assertNotNull(found.getCreatedAt(), "createdAt이 자동 설정되어야 한다");
            assertNotNull(found.getUpdatedAt(), "updatedAt이 자동 설정되어야 한다");
        }

        @Test
        @DisplayName("LabImage를 여러 개 저장하면 랩별, 타입별로 조회할 수 있다")
        void 랩별_타입별_조회() {
            // given
            Lab labA = labRepo.save(new Lab("A랩", "설명", "컴공", LabCategory.AI));
            Lab labB = labRepo.save(new Lab("B랩", "설명", "소프트", LabCategory.DB));
            LabImage imgA1 = imageRepo.save(new LabImage(labA, "u1", ImageType.REPRESENTATIVE));
            LabImage imgA2 = imageRepo.save(new LabImage(labA, "u2", ImageType.ADDITIONAL));
            LabImage imgB1 = imageRepo.save(new LabImage(labB, "u3", ImageType.REPRESENTATIVE));

            // when
            List<LabImage> aImages = imageRepo.findAllByLabId(labA.getId());
            List<LabImage> repImages = imageRepo.findAllByType(ImageType.REPRESENTATIVE);

            // then
            assertEquals(2, aImages.size(), "A랩의 이미지가 2개여야 한다");
            assertEquals(2, repImages.size(), "REPRESENTATIVE 타입이 2개여야 한다");
            assertTrue(aImages.stream().anyMatch(i -> i.getImageUrl().equals("u1")));
            assertTrue(aImages.stream().anyMatch(i -> i.getImageUrl().equals("u2")));
        }
    }

    @Nested
    @DisplayName("LabImage 삭제 및 연관관계 테스트")
    class DeleteTest {

        @Test
        @DisplayName("랩 삭제 시 LabImage도 함께 삭제(연관관계 cascade 동작)되어야 한다")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void 랩_삭제_이미지_자동삭제() {
            // given: Lab과 LabImage를 모두 즉시 DB에 반영
            Lab lab = labRepo.saveAndFlush(new Lab("DelLab", "설명", "전자", LabCategory.AI));
            LabImage img = imageRepo.saveAndFlush(new LabImage(lab, "u", ImageType.REPRESENTATIVE));
            Long imageId = img.getId();
            Long labId = lab.getId(); // Lab ID도 저장

            // when: Lab 삭제
            labRepo.deleteById(labId); // ID로 삭제
            labRepo.flush();

            // then: LabImage가 DB에서 사라져야 한다
            Optional<LabImage> found = imageRepo.findById(imageId);
            assertTrue(found.isEmpty(), "랩을 삭제하면 이미지도 함께 삭제되어야 한다");
        }
    }

    @Nested
    @DisplayName("제약조건 및 예외")
    class ConstraintTest {

        @Test
        @DisplayName("Lab이 null일 경우 저장 시 예외 발생")
        void 랩_null_예외() {
            // given & when & then
            assertThrows(Exception.class,
                    () -> imageRepo.save(new LabImage(null, "url", ImageType.REPRESENTATIVE)),
                    "Lab이 null이면 예외가 발생해야 한다");
        }

        @Test
        @DisplayName("imageUrl이 null이면 저장 시 예외 발생")
        void imageUrl_null_예외() {
            Lab lab = labRepo.save(new Lab("TestLab", "설명", "전기", LabCategory.AI));
            assertThrows(Exception.class,
                    () -> imageRepo.save(new LabImage(lab, null, ImageType.REPRESENTATIVE)),
                    "imageUrl이 null이면 예외가 발생해야 한다");
        }
    }
}
