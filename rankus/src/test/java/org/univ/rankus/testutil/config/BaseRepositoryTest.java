package org.univ.rankus.testutil.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.univ.rankus.config.DomainConfig;

/**
 * Repository 계층 통합 테스트 공통 설정
 * - MySQL 테스트 DB 사용
 * - JPA Auditing 활성화
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DomainConfig.class, org.univ.rankus.adapter.out.persistence.impl.CalendarEventRepositoryAdapter.class})
public abstract class BaseRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;
}