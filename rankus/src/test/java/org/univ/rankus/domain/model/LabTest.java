package org.univ.rankus.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class LabTest {

    @Test
    @DisplayName("정상적인 Lab 생성")
    void createLab_success() {
        String name = "AI 연구실";
        String description = "인공지능 알고리즘 연구";
        String department = "컴퓨터공학과";
        LabCategory category = LabCategory.AI;

        Lab lab = new Lab(name, description, department, category);

        assertAll(
                () -> assertEquals(name, lab.getName()),
                () -> assertEquals(description, lab.getDescription()),
                () -> assertEquals(department, lab.getDepartment()),
                () -> assertEquals(category, lab.getCategory()),
                () -> assertEquals(0, lab.getRanking())
        );
    }

    @Test
    @DisplayName("필수 파라미터 누락 시 예외 발생")
    void createLab_requiredFieldsNull_throws() {
        // name 누락
        assertThrows(NullPointerException.class, () -> {
            new Lab(null, "desc", "dept", LabCategory.DB);
        });

        // department 누락
        assertThrows(NullPointerException.class, () -> {
            new Lab("Name", "desc", null, LabCategory.DB);
        });

        // category 누락
        assertThrows(NullPointerException.class, () -> {
            new Lab("Name", "desc", "dept", null);
        });
    }

    @Test
    @DisplayName("랭킹 업데이트 검증")
    void updateRanking_successAndFailure() {
        Lab lab = new Lab("AI", "", "CS", LabCategory.AI);

        // 정상 업데이트
        lab.updateRanking(5);
        assertEquals(5, lab.getRanking());

        // 음수 랭킹 설정 시 예외
        assertThrows(IllegalArgumentException.class, () -> {
            lab.updateRanking(-1);
        });
    }
}
