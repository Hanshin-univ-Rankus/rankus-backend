package org.univ.rankus.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@DisplayName("SpringDataLabRepository 기본 동작 테스트")
class SpringDataLabRepositoryTest {

    @Autowired
    private SpringDataLabRepository repository;

    @Test
    @DisplayName("랩실 엔티티를 저장하고 조회하면 동일한 데이터가 반환되어야 한다")
    void saveAndFindById() {
        // given
        Lab lab = new Lab("Test Lab", "테스트 설명", "테스트학과", LabCategory.ETC);

        // when
        Lab saved = repository.save(lab);
        Optional<Lab> foundOpt = repository.findById(saved.getId());

        // then
        assertTrue(foundOpt.isPresent(), "저장한 랩실을 조회할 수 있어야 한다");
        Lab found = foundOpt.get();
        assertAll("저장된 랩실 필드 검증",
                () -> assertEquals(saved.getId(), found.getId(),                "ID가 일치해야 한다"),
                () -> assertEquals("Test Lab", found.getName(),                "이름이 일치해야 한다"),
                () -> assertEquals("테스트 설명", found.getDescription(),        "설명이 일치해야 한다"),
                () -> assertEquals("테스트학과", found.getDepartment(),          "학과가 일치해야 한다"),
                () -> assertEquals(LabCategory.ETC, found.getCategory(),        "카테고리가 일치해야 한다")
        );
    }

    @Test
    @DisplayName("여러 랩실 엔티티를 저장하고 findAll을 호출하면 저장한 수만큼 반환되어야 한다")
    void saveMultipleAndFindAll() {
        // given
        Lab lab1 = new Lab("Lab1", "설명1", "학과1", LabCategory.AI);
        Lab lab2 = new Lab("Lab2", "설명2", "학과2", LabCategory.DB);

        // when
        repository.save(lab1);
        repository.save(lab2);
        List<Lab> labs = repository.findAll();

        // then
        assertEquals(2, labs.size(), "저장된 랩실 수와 조회된 수가 동일해야 한다");
    }

    @Test
    @DisplayName("저장된 랩실을 삭제하면 findById 조회 시 결과가 비어 있어야 한다")
    void deleteById() {
        // given
        Lab lab = new Lab("ToDelete", "설명", "학과", LabCategory.WEB);
        Lab saved = repository.save(lab);

        // when
        repository.deleteById(saved.getId());
        Optional<Lab> foundOpt = repository.findById(saved.getId());

        // then
        assertFalse(foundOpt.isPresent(), "삭제된 랩실은 조회되지 않아야 한다");
    }
}
