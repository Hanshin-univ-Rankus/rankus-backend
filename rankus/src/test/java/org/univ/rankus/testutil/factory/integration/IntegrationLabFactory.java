package org.univ.rankus.testutil.factory.integration;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;

/**
 * IntegrationLabFactory - Repository/통합 테스트 전용 Lab 테스트 팩토리
 * - DomainLabFactory.build*() 를 호출하여 Lab 생성 후, 영속화 기능(persist)만 제공합니다.
 */
public final class IntegrationLabFactory {
    private IntegrationLabFactory() {
    }

    public static Lab persistValidLab(LabRepositoryPort repo) {
        Lab lab = DomainLabFactory.buildValidLab();
        return repo.save(lab);
    }

    public static Lab persistValidLab(TestEntityManager em) {
        Lab lab = DomainLabFactory.buildValidLab();
        em.persist(lab);
        em.flush();
        return lab;
    }

    public static Lab persistCustomLab(LabRepositoryPort repo, String name, org.univ.rankus.domain.model.lab.core.LabCategory category, String desc, String prof) {
        Lab lab = DomainLabFactory.buildCustomLab(name, category, desc, prof);
        return repo.save(lab);
    }

    public static Lab persistCustomLab(TestEntityManager em, String name, org.univ.rankus.domain.model.lab.core.LabCategory category, String desc, String prof) {
        Lab lab = DomainLabFactory.buildCustomLab(name, category, desc, prof);
        em.persist(lab);
        em.flush();
        return lab;
    }

    // IntegrationScoreSubmissionFactory에서 필요한 메서드들 추가
    public static Lab createAndSaveAiLab(LabRepositoryPort repo) {
        Lab lab = DomainLabFactory.buildCustomLab("AI 연구실", LabCategory.AI, "인공지능 연구실", "AI 교수");
        return repo.save(lab);
    }

    public static Lab createAndSaveDbLab(LabRepositoryPort repo) {
        Lab lab = DomainLabFactory.buildCustomLab("DB 연구실", LabCategory.DB, "데이터베이스 연구실", "DB 교수");
        return repo.save(lab);
    }

    public static Lab createAndSaveSecurityLab(LabRepositoryPort repo) {
        Lab lab = DomainLabFactory.buildCustomLab("보안 연구실", LabCategory.SECURITY, "정보보안 연구실", "보안 교수");
        return repo.save(lab);
    }
}