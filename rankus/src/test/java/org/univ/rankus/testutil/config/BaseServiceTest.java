package org.univ.rankus.testutil.config;


import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 서비스 계층 단위 테스트 공통 설정
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// UserRepositoryAdapter를 스캔 대상에 포함시켜 Port로 주입 가능하게 함
public abstract class BaseServiceTest {
}