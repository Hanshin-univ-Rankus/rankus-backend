package org.univ.rankus.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.univ.rankus.config.DomainConfig;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabApplication;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.lab.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DomainConfig.class)
@DisplayName("LabApplication 리포지토리 테스트")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LabApplicationRepositoryTest {

    @Autowired
    private SpringDataLabRepository labRepo;

    @Autowired
    private SpringDataLabApplicationRepository appRepo;

    @Nested
    @DisplayName("신청 저장 및 기본 조회")
    class SaveAndFindById {

        @Test
        @DisplayName("LabApplication을 저장하면 ID가 생성되고 저장된 엔티티가 조회된다.")
        void saveAndFindById_success() {
            // given
            Lab lab = labRepo.saveAndFlush(
                    new Lab("TestLab", "설명", "컴퓨터공학과", LabCategory.AI)
            );

            LabApplication app = new LabApplication(
                    lab,
                    42L,
                    LocalDateTime.of(2025, 6, 1, 14, 30)
            );

            // when
            LabApplication savedApp = appRepo.save(app);
            Optional<LabApplication> foundOpt = appRepo.findById(savedApp.getId());

            // then
            assertTrue(foundOpt.isPresent(), "저장된 LabApplication을 조회할 수 있어야 한다");
            LabApplication found = foundOpt.get();
            assertAll("저장된 LabApplication 필드 검증",
                    () -> assertEquals(lab.getId(),    found.getLab().getId(),      "랩실 연관관계가 유지되어야 한다"),
                    () -> assertEquals(42L,            found.getUserId(),            "userId가 일치해야 한다"),
                    () -> assertEquals(ApplicationStatus.PENDING, found.getStatus(), "초기 상태는 PENDING이어야 한다"),
                    () -> assertNotNull(found.getCreatedAt(),      "createdAt이 설정되어야 한다"),
                    () -> assertNotNull(found.getUpdatedAt(),      "updatedAt이 설정되어야 한다")
            );
        }
    }

    @Nested
    @DisplayName("랩실별 신청 목록 조회")
    class FindByLabIdTests {

        @Test
        @DisplayName("랩실 ID로 조회하면 해당 랩실의 모든 신청이 반환된다.")
        void findByLabId_success() {
            // given
            Lab lab1 = labRepo.save(new Lab("Lab1", "설명1", "컴퓨터공학과", LabCategory.DB));
            Lab lab2 = labRepo.save(new Lab("Lab2", "설명2", "전자공학과", LabCategory.WEB));

            LabApplication app1 = new LabApplication(lab1, 1L, LocalDateTime.now());
            LabApplication app2 = new LabApplication(lab1, 2L, LocalDateTime.now().plusHours(1));
            LabApplication app3 = new LabApplication(lab2, 3L, LocalDateTime.now().plusHours(2));
            appRepo.saveAll(List.of(app1, app2, app3));

            // when
            List<LabApplication> list = appRepo.findByLabId(lab1.getId());

            // then
            assertEquals(2, list.size(), "해당 랩실의 신청만 조회되어야 한다");
            assertTrue(list.stream().allMatch(a -> a.getLab().getId().equals(lab1.getId())),
                    "조회된 모든 신청이 올바른 랩실 ID여야 한다");
        }

        @Test
        @DisplayName("신청이 하나도 없으면 빈 리스트를 반환한다.")
        void findByLabId_empty() {
            // given: 아무 신청도 없는 상태
            Lab lab = labRepo.saveAndFlush(
                    new Lab("EmptyLab", "desc", "CS", LabCategory.WEB)
            );

            // when
            List<?> result = appRepo.findByLabId(lab.getId());

            // then
            assertNotNull(result,      "null이 아닌 리스트여야 한다");
            assertTrue(result.isEmpty(), "리스트가 비어 있어야 한다");
        }
    }

    @DisplayName("LabApplication을 삭제하면 다시 조회해도 결과가 없어야 한다")
    void deleteApplication_success() {
        // given
        Lab lab = labRepo.saveAndFlush(new Lab("삭제랩", "desc", "CS", LabCategory.AI));
        LabApplication app = new LabApplication(lab, 100L, LocalDateTime.now());
        LabApplication saved = appRepo.saveAndFlush(app);
        Long id = saved.getId();

        // when
        appRepo.deleteById(id);

        // then
        assertTrue(appRepo.findById(id).isEmpty(), "삭제 후에는 findById가 empty여야 한다");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 삭제해도 예외가 발생하지 않는다")
    void deleteApplication_nonexistentId() {
        // when & then: 예외 없음
        assertDoesNotThrow(() -> appRepo.deleteById(999999L));
    }

    @Test
    @DisplayName("LabApplication의 상태를 변경한 후 저장하면, DB에서도 값이 반영된다")
    void updateStatus_persisted() {
        // given
        Lab lab = labRepo.saveAndFlush(new Lab("수정랩", "desc", "CS", LabCategory.AI));
        LabApplication app = new LabApplication(lab, 200L, LocalDateTime.now());
        LabApplication saved = appRepo.saveAndFlush(app);

        // when
        saved.approve(); // 상태를 APPROVED로 변경
        appRepo.saveAndFlush(saved);

        // then
        LabApplication reloaded = appRepo.findById(saved.getId()).orElseThrow();
        assertEquals(ApplicationStatus.APPROVED, reloaded.getStatus(), "상태 변경이 DB에 반영되어야 한다");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 findById 호출 시 Optional.empty()가 반환된다")
    void findById_nonexistent() {
        assertTrue(appRepo.findById(999999L).isEmpty(), "존재하지 않는 ID면 empty여야 한다");
    }

    @Test
    @DisplayName("null 값을 가진 랩실로 LabApplication을 저장하면 IllegalArgumentException이 발생한다")
    void save_nullLab_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new LabApplication(null, 123L, LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("userId가 null인 LabApplication을 생성하면 IllegalArgumentException이 발생한다")
    void save_nullUserId_throws() {
        Lab lab = labRepo.saveAndFlush(new Lab("null유저랩", "desc", "CS", LabCategory.DB));
        assertThrows(IllegalArgumentException.class, () ->
                new LabApplication(lab, null, LocalDateTime.now())
        );
    }
}