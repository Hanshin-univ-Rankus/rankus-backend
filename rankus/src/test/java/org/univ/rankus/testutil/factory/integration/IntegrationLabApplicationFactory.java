package org.univ.rankus.testutil.factory.integration;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.univ.rankus.application.port.out.LabApplicationRepositoryPort;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabApplicationFactory;

import java.time.LocalDateTime;

/**
 * IntegrationLabApplicationFactory - Repository/통합 테스트 전용 LabApplication 테스트 팩토리
 * - DomainLabApplicationFactory.build*() 를 호출하여 LabApplication 생성 후, 영속화(persist) 기능만 제공합니다.
 */
public final class IntegrationLabApplicationFactory {
    private IntegrationLabApplicationFactory() {
    }

    public static LabApplication persistValidPendingApplication(LabApplicationRepositoryPort repo, Lab lab, User user, LocalDateTime time) {
        LabApplication app = DomainLabApplicationFactory.buildValidPendingApplication(lab, user, time);
        return repo.save(app);
    }

    public static LabApplication persistValidPendingApplication(TestEntityManager em, Lab lab, User user, LocalDateTime time) {
        LabApplication app = DomainLabApplicationFactory.buildValidPendingApplication(lab, user, time);
        em.persist(app);
        em.flush();
        return app;
    }

    public static LabApplication persistWithStatus(LabApplicationRepositoryPort repo, Lab lab, User user, LocalDateTime time, org.univ.rankus.domain.model.lab.application.ApplicationStatus status) {
        LabApplication app = DomainLabApplicationFactory.buildWithStatus(lab, user, status);
        return repo.save(app);
    }

    public static LabApplication persistWithStatus(TestEntityManager em, Lab lab, User user, LocalDateTime time, org.univ.rankus.domain.model.lab.application.ApplicationStatus status) {
        LabApplication app = DomainLabApplicationFactory.buildWithStatus(lab, user, status);
        em.persist(app);
        em.flush();
        return app;
    }
}