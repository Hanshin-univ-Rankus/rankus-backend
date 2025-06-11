package org.univ.rankus.testutil.factory.integration;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;

/**
 * IntegrationLabFactory - Repository/통합 테스트 전용 Lab 테스트 팩토리
 * - DomainLabFactory.build*() 를 호출하여 Lab 생성 후, 영속화 기능(persist)만 제공합니다.
 */
public final class IntegrationLabFactory {
    private IntegrationLabFactory() {}

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

    public static Lab persistCustomLab(LabRepositoryPort repo, String name, org.univ.rankus.domain.model.lab.LabCategory category, String desc, String prof) {
        Lab lab = DomainLabFactory.buildCustomLab(name, category, desc, prof);
        return repo.save(lab);
    }

    public static Lab persistCustomLab(TestEntityManager em, String name, org.univ.rankus.domain.model.lab.LabCategory category, String desc, String prof) {
        Lab lab = DomainLabFactory.buildCustomLab(name, category, desc, prof);
        em.persist(lab);
        em.flush();
        return lab;
    }
}