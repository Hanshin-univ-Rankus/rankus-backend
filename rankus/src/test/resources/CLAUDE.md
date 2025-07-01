# Test Resources 가이드

> 테스트 실행에 필요한 설정 파일과 테스트 데이터를 관리하는 디렉토리

## 📁 Test Resources 개요

### 핵심 역할

- **테스트 환경 설정**: 프로덕션과 격리된 테스트 전용 설정
- **테스트 데이터 격리**: 테스트용 데이터베이스 및 데이터 관리
- **테스트 리소스**: 테스트에 필요한 정적 파일 제공
- **Mock 설정**: 외부 시스템 대체를 위한 설정

### 디렉토리 구조

```
src/test/resources/
├── CLAUDE.md                    # 이 파일
├── application.yml              # 테스트 환경 기본 설정
├── application-integration.yml  # 통합 테스트 설정 (향후 확장)
├── data/                        # 테스트 데이터 파일 (향후 확장)
│   ├── users.json
│   ├── labs.json
│   └── applications.json
├── sql/                         # 테스트용 SQL 스크립트 (향후 확장)
│   ├── test-schema.sql
│   └── test-data.sql
├── static/                      # 테스트용 정적 파일 (향후 확장)
│   └── test-images/
└── mock/                        # Mock 응답 데이터 (향후 확장)
    ├── external-api-responses.json
    └── jwt-keys.json
```

## ⚙️ 테스트 설정 파일

### application.yml (테스트 기본 설정)

```yaml
spring:
  profiles: test
  
  datasource:
    url: jdbc:mysql://localhost:3308/rankus_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
    username: rankus_test_user
    password: test_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop  # 테스트마다 스키마 재생성
    database-platform: org.hibernate.dialect.MySQLDialect
    show-sql: false  # 테스트 로그 최소화
    
  sql:
    init:
      mode: never  # data.sql 사용하지 않음
```

**주요 특징**:

- **격리된 데이터베이스**: 별도 포트(3308) 및 DB 사용
- **자동 스키마 관리**: `create-drop`으로 테스트 격리
- **로깅 최소화**: 테스트 성능 최적화
- **데이터 초기화 비활성화**: 테스트 코드에서 직접 관리

### 프로덕션과의 차이점

| 설정 항목       | 프로덕션 (main)                | 테스트                        |
|-------------|----------------------------|----------------------------|
| **데이터베이스**  | localhost:3306/rankus      | localhost:3308/rankus_test |
| **DDL 모드**  | create (개발), validate (운영) | create-drop                |
| **데이터 초기화** | data.sql 사용                | 테스트 코드에서 관리                |
| **로깅**      | 상세 로깅                      | 최소 로깅                      |
| **보안**      | 환경에 따라 다름                  | 테스트용 고정값                   |

## 🗃️ 테스트 데이터 관리 전략

### 1. 테스트 데이터베이스 격리

```yaml
# 테스트 전용 데이터베이스 설정
datasource:
  url: jdbc:mysql://localhost:3308/rankus_test
  # 3308 포트로 완전 격리
```

**격리 장점**:

- 개발용 데이터와 충돌 방지
- 테스트 데이터 일관성 보장
- 병렬 테스트 실행 가능
- 테스트 실패가 개발 환경에 영향 없음

### 2. 테스트 데이터 생성 전략

```java
// Factory 패턴으로 테스트 데이터 생성
@Test
@Transactional
void 사용자_생성_테스트() {
    // given - 팩토리로 테스트 데이터 생성
    User user = DomainUserFactory.createStudent();
    Lab lab = DomainLabFactory.createAiLab();
    
    // when & then
    // 테스트 로직
}
```

### 3. 데이터 정리 전략

```java
// @Transactional로 자동 롤백
@Test
@Transactional
void 테스트_메서드() {
    // 테스트 종료 후 자동으로 롤백됨
}

// @Sql로 특정 상태 설정
@Test
@Sql("/sql/test-data.sql")
void 특정_데이터로_테스트() {
    // SQL 스크립트로 데이터 설정
}
```

## 🔧 테스트 프로파일 관리

### 기본 테스트 프로파일

```yaml
spring:
  profiles:
    active: test
```

### 통합 테스트 프로파일 (향후 확장)

```yaml
# application-integration.yml
spring:
  profiles: integration
  
  # 실제 외부 시스템과 연동하는 통합 테스트 설정
  datasource:
    url: jdbc:mysql://localhost:3309/rankus_integration
    
  # 외부 API 연동 설정
  external-api:
    base-url: http://localhost:8080
    timeout: 5000
```

### E2E 테스트 프로파일 (향후 확장)

```yaml
# application-e2e.yml
spring:
  profiles: e2e
  
  # 프로덕션과 유사한 환경 설정
  jpa:
    hibernate:
      ddl-auto: validate
      
  # 실제 JWT 시크릿 사용
  security:
    jwt:
      secret: ${TEST_JWT_SECRET}
```

## 📊 테스트 데이터 파일 (향후 확장)

### JSON 테스트 데이터

```json
// data/users.json
[
  {
    "id": 1,
    "name": "테스트사용자1",
    "email": "test1@example.com",
    "role": "STUDENT"
  },
  {
    "id": 2,
    "name": "테스트랩장1",
    "email": "leader1@example.com",
    "role": "LAB_LEADER"
  }
]
```

```json
// data/labs.json
[
  {
    "id": 1,
    "name": "테스트AI랩",
    "category": "AI",
    "description": "테스트용 AI 랩실"
  }
]
```

### 테스트 데이터 로더

```java
@Component
@Profile("test")
public class TestDataLoader {
    
    public List<User> loadTestUsers() {
        // JSON 파일에서 테스트 사용자 데이터 로드
        InputStream inputStream = getClass()
            .getResourceAsStream("/data/users.json");
        return objectMapper.readValue(inputStream, 
            new TypeReference<List<User>>() {});
    }
}
```

## 🎭 Mock 데이터 관리 (향후 확장)

### 외부 API Mock 응답

```json
// mock/external-api-responses.json
{
  "userProfile": {
    "success": {
      "status": 200,
      "data": {
        "id": "ext_123",
        "name": "외부사용자",
        "email": "external@example.com"
      }
    },
    "notFound": {
      "status": 404,
      "error": "User not found"
    }
  }
}
```

### JWT 테스트 키

```json
// mock/jwt-keys.json
{
  "test": {
    "secret": "test-secret-key-for-unit-tests-only",
    "expiration": 3600000
  },
  "integration": {
    "secret": "integration-test-secret-key",
    "expiration": 1800000
  }
}
```

## 🔒 테스트 보안 설정

### 테스트용 JWT 설정

```yaml
security:
  jwt:
    secret: test-secret-key-32-characters-long
    expiration-ms: 3600000  # 1시간 (테스트용 짧은 시간)
```

### 테스트용 사용자 인증

```java
@TestConfiguration
public class TestSecurityConfig {
    
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        return username -> {
            // 테스트용 고정 사용자 반환
            User testUser = DomainUserFactory.createStudent();
            return new CustomUserDetails(testUser);
        };
    }
}
```

## 📈 테스트 성능 최적화

### 테스트 실행 속도 향상

```yaml
# 테스트 성능 최적화 설정
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50  # 배치 처리로 성능 향상
        order_inserts: true
        order_updates: true
        
  datasource:
    hikari:
      maximum-pool-size: 5  # 테스트용 최소 커넥션 풀
      minimum-idle: 1
```

### 메모리 사용량 최적화

```yaml
logging:
  level:
    org.hibernate.SQL: WARN  # SQL 로깅 최소화
    org.springframework.web: WARN
    org.univ.rankus: INFO
    
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

## 🧪 테스트 환경 설정 가이드

### Docker를 활용한 테스트 DB (권장)

```yaml
# docker-compose.test.yml
version: '3.8'
services:
  mysql-test:
    image: mysql:8.0
    ports:
      - "3308:3306"
    environment:
      MYSQL_ROOT_PASSWORD: test_root_password
      MYSQL_DATABASE: rankus_test
      MYSQL_USER: rankus_test_user
      MYSQL_PASSWORD: test_password
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
```

### 테스트 실행 명령어

```bash
# 테스트 DB 시작
docker-compose -f docker-compose.test.yml up -d

# 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests UserCommandServiceTest

# 테스트 DB 정리
docker-compose -f docker-compose.test.yml down
```

## 🎯 테스트 리소스 베스트 프랙티스

### 1. 설정 파일 관리

- **환경 분리**: 테스트 전용 설정으로 격리
- **성능 최적화**: 테스트 실행 속도 우선
- **안정성**: 테스트 간 격리 보장

### 2. 테스트 데이터 관리

- **Factory 패턴**: 일관된 테스트 데이터 생성
- **최소 데이터**: 테스트에 필요한 최소한의 데이터만 생성
- **자동 정리**: @Transactional로 자동 롤백

### 3. 외부 의존성 처리

- **Mock 활용**: 외부 API는 Mock으로 대체
- **테스트 더블**: 실제 구현체 대신 테스트용 구현체 사용
- **격리**: 외부 시스템 장애가 테스트에 영향 없도록

### 4. 리소스 정리

- **메모리 관리**: 대용량 테스트 데이터는 스트림 처리
- **커넥션 관리**: 테스트 후 DB 커넥션 정리
- **임시 파일**: 테스트용 임시 파일 자동 삭제

## 🔗 관련 설정

- **메인 리소스**: `@main/resources/CLAUDE.md`
- **테스트 전략**: `@test/CLAUDE.md`
- **도메인 테스트**: `@domain/CLAUDE.md`
- **어댑터 테스트**: `@adapter/CLAUDE.md`