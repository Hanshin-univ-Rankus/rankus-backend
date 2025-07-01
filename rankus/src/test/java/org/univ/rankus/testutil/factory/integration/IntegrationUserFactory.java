package org.univ.rankus.testutil.factory.integration;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

/**
 * IntegrationUserFactory - Repository/통합 테스트 전용 User 테스트 팩토리
 * - DomainUserFactory.build*() 를 호출하여 도메인 객체 생성 후, 저장(persist) 기능만 제공합니다.
 */
public final class IntegrationUserFactory {
    private IntegrationUserFactory() {
    }

    public static User persistValidUser(UserRepositoryPort repo) {
        User u = DomainUserFactory.buildValidUser();
        return repo.save(u);
    }

    public static User persistValidUser(TestEntityManager em) {
        User u = DomainUserFactory.buildValidUser();
        em.persist(u);
        em.flush();
        return u;
    }

    public static User persistCustomUser(UserRepositoryPort repo, String name, String email, String rawPassword) {
        User u = DomainUserFactory.buildCustomUser(name, email, rawPassword);
        return repo.save(u);
    }

    public static User persistCustomUser(TestEntityManager em, String name, String email, String rawPassword) {
        User u = DomainUserFactory.buildCustomUser(name, email, rawPassword);
        em.persist(u);
        em.flush();
        return u;
    }

    public static User persistUserWithRole(UserRepositoryPort repo, org.univ.rankus.domain.model.user.Role role) {
        User u = DomainUserFactory.buildValidUserWithRole(role);
        return repo.save(u);
    }

    public static User persistUserWithRole(TestEntityManager em, org.univ.rankus.domain.model.user.Role role) {
        User u = DomainUserFactory.buildValidUserWithRole(role);
        em.persist(u);
        em.flush();
        return u;
    }
}
