package org.univ.rankus.testutil.factory.integration;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.univ.rankus.application.port.out.LabImageRepositoryPort;
import org.univ.rankus.domain.model.lab.ImageType;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.testutil.factory.domain.DomainLabImageFactory;

/**
 * IntegrationLabImageFactory - Repository/통합 테스트 전용 LabImage 테스트 팩토리
 * - DomainLabImageFactory.build*() 를 호출하여 LabImage 생성 후, 영속화 기능(persist)만 제공합니다.
 */
public final class IntegrationLabImageFactory {
    private IntegrationLabImageFactory() {
    }

    public static LabImage persistValidLabImage(LabImageRepositoryPort repo, Lab lab, String url, ImageType type) {
        LabImage img = DomainLabImageFactory.buildValidLabImage(lab, url, type);
        return repo.save(img);
    }

    public static LabImage persistValidLabImage(TestEntityManager em, Lab lab, String url, ImageType type) {
        LabImage img = DomainLabImageFactory.buildValidLabImage(lab, url, type);
        em.persist(img);
        em.flush();
        return img;
    }

    public static LabImage persistCustomLabImage(LabImageRepositoryPort repo, Lab lab, String url, ImageType type) {
        LabImage img = DomainLabImageFactory.buildCustomLabImage(lab, url, type);
        return repo.save(img);
    }

    public static LabImage persistCustomLabImage(TestEntityManager em, Lab lab, String url, ImageType type) {
        LabImage img = DomainLabImageFactory.buildCustomLabImage(lab, url, type);
        em.persist(img);
        em.flush();
        return img;
    }
}