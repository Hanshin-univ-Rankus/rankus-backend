package org.univ.rankus.adapter.out.persistence.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.univ.rankus.adapter.out.persistence.impl.LabRepositoryAdapter;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.testutil.config.BaseRepositoryTest;
import org.univ.rankus.testutil.factory.integration.IntegrationLabFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(LabRepositoryAdapter.class)
@DisplayName("Spring Data JPA LabRepository 통합 테스트")
class SpringDataLabRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private LabRepositoryPort labRepo;

    @Test
    @DisplayName("save: Lab 저장 후 ID 자동 생성")
    void save_generatesId() {
        Lab saved = IntegrationLabFactory.persistValidLab(labRepo);
        assertNotNull(saved.getId(), "저장된 Lab은 ID가 자동 생성되어야 한다");
        assertTrue(saved.getId() > 0, "생성된 ID는 양수여야 한다");
    }

    @Test
    @DisplayName("findAllByRankingDesc: 랩 여러 개 저장 시 ranking 내림차순 정렬")
    void findAllByRankingDesc_returnsSortedList() {
        // given
        Lab lab1 = IntegrationLabFactory.persistCustomLab(labRepo, "LabA", LabCategory.AI, "desc1", "Prof1");
        Lab lab2 = IntegrationLabFactory.persistCustomLab(labRepo, "LabB", LabCategory.IOT, "desc2", "Prof2");
        Lab lab3 = IntegrationLabFactory.persistCustomLab(labRepo, "LabC", LabCategory.CV, "desc3", "Prof3");
        // ranking 직접 수정
        lab1.setRanking(20);
        lab2.setRanking(50);
        lab3.setRanking(10);
        labRepo.save(lab1);
        labRepo.save(lab2);
        labRepo.save(lab3);

        // when
        List<Lab> labs = labRepo.findAllByRankingDesc();

        // then
        assertEquals(3, labs.size(), "저장된 랩 수 만큼 반환되어야 한다");
        assertEquals(50, labs.get(0).getRanking(), "가장 높은 ranking이 첫 번째여야 한다");
        assertEquals(20, labs.get(1).getRanking());
        assertEquals(10, labs.get(2).getRanking());
    }

    @Test
    @DisplayName("findAllByRankingDesc: 랩이 없을 때 빈 리스트 반환")
    void findAllByRankingDesc_empty() {
        List<Lab> labs = labRepo.findAllByRankingDesc();
        assertTrue(labs.isEmpty(), "저장된 랩이 없으면 빈 리스트를 반환해야 한다");
    }

    @Test
    @DisplayName("save: null 전달 시 IllegalArgumentException 예외 발생")
    void save_null_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> labRepo.save(null),
                "null을 저장 시도하면 IllegalArgumentException을 던져야 한다");
    }

    @Test
    @DisplayName("save: 기존 Lab 수정 시 ID 동일 및 수정 내용 반영")
    void save_updateFields_persistsChanges() {
        // given
        Lab saved = IntegrationLabFactory.persistValidLab(labRepo);
        // when
        saved.setProfessorName("NewProf");
        Lab updated = labRepo.save(saved);
        // then
        assertEquals(saved.getId(), updated.getId(), "수정 후에도 ID는 유지되어야 한다");
        assertEquals("NewProf", updated.getProfessorName(), "수정된 professorName이 반영되어야 한다");
    }
}