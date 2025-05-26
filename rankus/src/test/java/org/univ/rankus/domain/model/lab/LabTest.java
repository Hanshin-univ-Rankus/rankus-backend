package org.univ.rankus.domain.model.lab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lab 도메인 단위 테스트")
@ActiveProfiles("test")
class LabTest {

    @Test
    @DisplayName("Lab 객체를 올바른 값으로 생성하면 모든 필드가 정상적으로 초기화된다.")
    void createLab_success() {
        // given
        String name        = "AI 연구실";
        String description = "인공지능 알고리즘 연구";
        String department  = "컴퓨터공학과";
        LabCategory category = LabCategory.AI;

        // when
        Lab lab = new Lab(name, description, department, category);

        // then
        assertAll("Lab 생성 기본 검증",
                () -> assertEquals(name, lab.getName(),        "name이 설정되어야 한다"),
                () -> assertEquals(description, lab.getDescription(), "description이 설정되어야 한다"),
                () -> assertEquals(department, lab.getDepartment(),   "department가 설정되어야 한다"),
                () -> assertEquals(category, lab.getCategory(),       "category가 설정되어야 한다"),
                () -> assertEquals(0, lab.getRanking(),               "초기 ranking은 0이어야 한다")
        );
    }

    @Test
    @DisplayName("name 필드가 null일 때 Lab 객체를 생성하면 NullPointerException이 발생한다.")
    void createLab_nameNull_throws() {
        // given
        String name        = null;
        String description = "desc";
        String department  = "dept";
        LabCategory category = LabCategory.DB;

        // when & then
        assertThrows(NullPointerException.class, () ->
                        new Lab(name, description, department, category),
                "name이 null이면 NullPointerException이 발생해야 한다"
        );
    }

    @Test
    @DisplayName("department 필드가 null일 때 Lab 객체를 생성하면 NullPointerException이 발생한다.")
    void createLab_departmentNull_throws() {
        // given
        String name        = "Name";
        String description = "desc";
        String department  = null;
        LabCategory category = LabCategory.DB;

        // when & then
        assertThrows(NullPointerException.class, () ->
                        new Lab(name, description, department, category),
                "department가 null이면 NullPointerException이 발생해야 한다"
        );
    }

    @Test
    @DisplayName("category 필드가 null일 때 Lab 객체를 생성하면 NullPointerException이 발생한다.")
    void createLab_categoryNull_throws() {
        // given
        String name        = "Name";
        String description = "desc";
        String department  = "dept";
        LabCategory category = null;

        // when & then
        assertThrows(NullPointerException.class, () ->
                        new Lab(name, description, department, category),
                "category가 null이면 NullPointerException이 발생해야 한다"
        );
    }

    @Test
    @DisplayName("updateRanking을 양수로 호출하면 랭킹 값이 정상적으로 변경된다.")
    void updateRanking_success() {
        // given
        Lab lab = new Lab("AI", "desc", "CS", LabCategory.AI);

        // when
        lab.updateRanking(5);

        // then
        assertEquals(5, lab.getRanking(), "랭킹이 5로 변경되어야 한다");
    }

    @Test
    @DisplayName("updateRanking을 음수로 호출하면 IllegalArgumentException이 발생한다.")
    void updateRanking_negative_throws() {
        // given
        Lab lab = new Lab("AI", "desc", "CS", LabCategory.AI);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                        lab.updateRanking(-1),
                "음수 랭킹 설정 시 IllegalArgumentException이 발생해야 한다"
        );
    }
}