package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.core.LabCategory;

/**
 * DomainLabFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 Lab 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainLabFactory {
    private DomainLabFactory() {
    }

    public static Lab buildValidLab() {
        Lab lab = new Lab("TestLab", LabCategory.AI, "Description", "ProfX");
        return lab;
    }

    public static Lab buildValidLabWithId(Long id) {
        Lab lab = buildValidLab();
        ReflectionTestUtils.setField(lab, "id", id);
        return lab;
    }

    public static Lab buildInvalidLab_NoName() {
        return new Lab("", LabCategory.AI, "Desc", "ProfX");
    }

    public static Lab buildInvalidLab_NoCategory() {
        return new Lab("TestLab", null, "Desc", "ProfX");
    }

    public static Lab buildInvalidLab_LongDescription() {
        StringBuilder sb = new StringBuilder();
        while (sb.length() <= 1001) sb.append('a');
        return new Lab("TestLab", LabCategory.AI, sb.toString(), "ProfX");
    }

    public static Lab buildLab_NoProfessor() {
        return new Lab("TestLab", LabCategory.AI, "Desc", null);
    }

    public static Lab buildCustomLab(String name, LabCategory cat, String desc, String prof) {
        return new Lab(name, cat, desc, prof);
    }

    public static Lab buildLabWithProfessor(String professorName) {
        return new Lab("TestLab", LabCategory.AI, "Description", professorName);
    }

    public static Lab buildLabWithCategory(LabCategory category) {
        return new Lab("TestLab", category, "Description", "ProfX");
    }

    public static Lab buildAiLab() {
        Lab lab = new Lab("AI Lab", LabCategory.AI, "Artificial Intelligence Research Lab", "Dr. Smith");
        return lab;
    }

    public static Lab buildDbLab() {
        Lab lab = new Lab("DB Lab", LabCategory.DB, "Database Research Lab", "Dr. Jones");
        return lab;
    }

    public static Lab buildSecurityLab() {
        Lab lab = new Lab("Security Lab", LabCategory.SECURITY, "Security Research Lab", "Dr. Brown");
        return lab;
    }
}
