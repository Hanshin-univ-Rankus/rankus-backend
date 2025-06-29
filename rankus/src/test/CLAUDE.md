# Test 디렉토리 가이드

> 헥사고날 아키텍처 기반의 종합적인 테스트 전략과 계층별 테스트 방법론

## 🧪 테스트 아키텍처 개요

### 핵심 원칙
- **테스트 피라미드**: Unit Tests (70%) → Integration Tests (20%) → E2E Tests (10%)
- **계층별 격리**: 각 계층의 책임에 맞는 독립적인 테스트
- **포트/어댑터 테스트**: 인터페이스 기반의 Mock 활용
- **의존성 역전**: 실제 구현체 대신 Mock/Stub 사용

### 테스트 전략 매핑
```
┌─────────────────────────────────────────┐
│             Adapter Layer               │ ← @WebMvcTest, @DataJpaTest
│  ┌─────────────────────────────────┐    │
│  │        Application Layer        │    │ ← @ExtendWith(MockitoExtension)  
│  │   ┌─────────────────────────┐   │    │
│  │   │      Domain Layer       │   │    │ ← Pure Unit Tests
│  │   └─────────────────────────┘   │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

## 📁 테스트 디렉토리 구조

### 현재 구현된 테스트 구조
```
src/test/
├── CLAUDE.md                     # 이 파일
├── java/org/univ/rankus/
│   ├── adapter/                  # Adapter Layer 테스트
│   │   ├── CLAUDE.md            # Adapter 테스트 가이드
│   │   ├── in/web/controller/   # Controller 테스트
│   │   │   ├── AuthControllerTest.java
│   │   │   ├── LabApplicationControllerTest.java
│   │   │   ├── LabImageControllerTest.java
│   │   │   ├── LabPromotionControllerTest.java
│   │   │   ├── UserControllerTest.java
│   │   │   └── security/        # 보안 통합 테스트
│   │   │       ├── JwtIntegrationTest.java
│   │   │       └── LabApplicationControllerSecurityTest.java
│   │   └── out/persistence/     # Repository 테스트
│   │       └── jpa/
│   │           ├── SpringDataLabApplicationRepositoryTest.java
│   │           ├── SpringDataLabImageRepositoryTest.java
│   │           ├── SpringDataLabRepositoryTest.java
│   │           └── SpringDataUserRepositoryTest.java
│   ├── application/             # Application Layer 테스트
│   │   ├── CLAUDE.md           # Application 테스트 가이드
│   │   └── service/
│   │       ├── auth/
│   │       │   └── AuthServiceTest.java
│   │       ├── command/
│   │       │   ├── LabApplicationCommandServiceTest.java
│   │       │   ├── LabImageCommandServiceTest.java
│   │       │   └── UserCommandServiceTest.java
│   │       └── query/
│   │           ├── LabApplicationQueryServiceTest.java
│   │           ├── LabImageQueryServiceTest.java
│   │           ├── LabPromotionQueryServiceTest.java
│   │           └── UserQueryServiceTest.java
│   ├── domain/                  # Domain Layer 테스트
│   │   ├── CLAUDE.md           # Domain 테스트 가이드
│   │   └── model/
│   │       ├── lab/
│   │       │   ├── LabApplicationTest.java
│   │       │   ├── LabImageTest.java
│   │       │   └── LabTest.java
│   │       └── user/
│   │           ├── PasswordTest.java
│   │           └── UserTest.java
│   ├── common/                  # Common Layer 테스트
│   │   ├── CLAUDE.md           # Common 테스트 가이드
│   │   └── security/
│   │       └── permission/
│   │           └── LabApplicationPermissionHandlerTest.java
│   └── testutil/               # 테스트 유틸리티
│       ├── CLAUDE.md          # TestUtil 가이드
│       ├── config/            # 테스트 설정
│       │   ├── BaseRepositoryTest.java
│       │   ├── BaseServiceTest.java
│       │   └── BaseWebTest.java
│       ├── factory/           # 테스트 데이터 팩토리
│       │   ├── domain/        # 도메인 객체 팩토리
│       │   │   ├── DomainLabApplicationFactory.java
│       │   │   ├── DomainLabFactory.java
│       │   │   ├── DomainLabImageFactory.java
│       │   │   └── DomainUserFactory.java
│       │   └── integration/   # 통합 테스트 팩토리
│       │       ├── IntegrationLabApplicationFactory.java
│       │       ├── IntegrationLabFactory.java
│       │       ├── IntegrationLabImageFactory.java
│       │       └── IntegrationUserFactory.java
│       └── mock/              # Mock 유틸리티
│           ├── AuthMockUtil.java
│           └── QueryMockUtil.java
└── resources/
    ├── CLAUDE.md              # 테스트 리소스 가이드
    └── application.yml        # 테스트 환경 설정
```

## 🏗️ 계층별 테스트 전략

### 1. Domain Layer 테스트 (70% - Unit Tests)
**목표**: 순수한 비즈니스 로직 검증

```java
// 예시: 도메인 로직 테스트
@Test
void 지원서_승인시_상태가_APPROVED로_변경된다() {
    // given
    LabApplication application = DomainLabApplicationFactory.createPending();
    
    // when
    application.approve();
    
    // then
    assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
}
```

**특징**:
- 외부 의존성 없음 (순수 Java 객체)
- 빠른 실행 속도
- 비즈니스 규칙 검증 중심
- Given-When-Then 패턴 활용

### 2. Application Layer 테스트 (20% - Integration Tests)
**목표**: 유스케이스 구현 검증

```java
// 예시: 서비스 레이어 테스트
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {
    
    @Mock private UserRepositoryPort userRepositoryPort;
    @InjectMocks private UserCommandService userCommandService;
    
    @Test
    void 유효한_정보로_사용자를_생성할_수_있다() {
        // given
        UserCreateRequestDto request = UserCreateRequestDto.builder()...build();
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        
        // when
        UserResponseDto result = userCommandService.createUser(request);
        
        // then
        assertThat(result.getName()).isEqualTo(request.getName());
        verify(userRepositoryPort).save(any(User.class));
    }
}
```

**특징**:
- @Mock으로 포트 인터페이스 대체
- 트랜잭션 경계 테스트
- 예외 처리 시나리오 검증
- 포트 호출 검증 (verify)

### 3. Adapter Layer 테스트 (10% - Integration/E2E Tests)
**목표**: 외부 시스템과의 통합 검증

```java
// 예시: Controller 테스트
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @MockBean private UserQueryUseCase userQueryUseCase;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void 사용자_목록을_조회할_수_있다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }
}
```

**특징**:
- Spring Context 로딩
- HTTP 요청/응답 테스트
- 보안 설정 검증
- JSON 직렬화/역직렬화 검증

## 🎭 테스트 어노테이션 가이드

### Spring Boot Test 어노테이션
| 어노테이션 | 용도 | 로딩되는 컴포넌트 | 사용 계층 |
|------------|------|-------------------|-----------|
| `@SpringBootTest` | 전체 통합 테스트 | 모든 Bean | E2E 테스트 |
| `@WebMvcTest` | Controller 테스트 | Web Layer | Adapter/Controller |
| `@DataJpaTest` | Repository 테스트 | JPA 관련 | Adapter/Repository |
| `@JsonTest` | JSON 직렬화 테스트 | Jackson | DTO 테스트 |
| `@ExtendWith(MockitoExtension.class)` | Mock 테스트 | 없음 | Application/Domain |

### 보안 테스트 어노테이션
```java
@WithMockUser(roles = "ADMIN")              // 관리자 권한
@WithMockUser(username = "user@test.com")   // 특정 사용자
@WithAnonymousUser                          // 익명 사용자
@WithUserDetails("user@test.com")           // UserDetailsService 활용
```

## 📊 테스트 데이터 관리 전략

### 1. Factory 패턴 활용
**도메인 팩토리** (domain/factory/):
```java
public class DomainUserFactory {
    public static User createStudent() {
        return User.create("홍길동", "hong@test.com", "password123!", Role.STUDENT);
    }
    
    public static User createLabLeader() {
        return User.create("김팀장", "leader@test.com", "password123!", Role.LAB_LEADER);
    }
}
```

**통합 팩토리** (integration/factory/):
```java
@Component
public class IntegrationUserFactory {
    
    @Autowired private UserRepository userRepository;
    
    public User createAndSaveStudent() {
        User user = DomainUserFactory.createStudent();
        return userRepository.save(user);
    }
}
```

### 2. 테스트 데이터 격리
- **@Transactional**: 테스트 메서드마다 자동 롤백
- **@Sql**: 특정 SQL 스크립트 실행
- **@DirtiesContext**: Spring Context 재시작

### 3. Mock 데이터 관리
```java
public class AuthMockUtil {
    public static Authentication createMockAuthentication(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
    }
}
```

## 🔒 보안 테스트 전략

### 1. 인증 테스트
```java
@Test
void 인증_없이_보호된_엔드포인트_접근시_401_반환() throws Exception {
    mockMvc.perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized());
}
```

### 2. 인가 테스트
```java
@Test
@WithMockUser(roles = "STUDENT")
void 학생은_다른_사용자_지원서를_조회할_수_없다() throws Exception {
    mockMvc.perform(get("/api/labs/1/applications"))
        .andExpect(status().isForbidden());
}
```

### 3. JWT 토큰 테스트
```java
@Test
void 유효한_JWT_토큰으로_인증된_요청_처리() {
    // JWT 토큰 생성 및 검증 로직
}
```

## 📈 테스트 성능 최적화

### 1. 테스트 실행 속도 향상
- **슬라이스 테스트**: 필요한 컴포넌트만 로딩
- **@MockBean vs @Mock**: 적절한 Mock 전략 선택
- **테스트 병렬 실행**: `@Execution(ExecutionMode.CONCURRENT)`

### 2. 메모리 최적화
- **@DirtiesContext 최소화**: Context 재시작 비용 고려
- **대용량 데이터 테스트**: 페이징 처리 검증
- **Connection Pool**: 테스트용 최소 설정

### 3. 플레이키 테스트 방지
- **시간 의존성 제거**: `Clock` 인터페이스 활용
- **비동기 처리 테스트**: `@Async` 메서드 검증
- **외부 의존성 격리**: WireMock, TestContainers 활용

## 🎯 테스트 베스트 프랙티스

### 1. 테스트 명명 규칙
```java
// Given-When-Then 패턴
void 유효한_정보로_사용자_생성시_사용자가_반환된다()
void 존재하지_않는_사용자_조회시_예외가_발생한다()
void 권한이_없는_사용자가_지원서_승인시_403_반환된다()
```

### 2. 테스트 구조
```java
@Test
void 테스트_메서드명() {
    // given (준비)
    User user = DomainUserFactory.createStudent();
    
    // when (실행)
    UserResponseDto result = userService.createUser(request);
    
    // then (검증)
    assertThat(result.getName()).isEqualTo("홍길동");
    verify(userRepository).save(any(User.class));
}
```

### 3. Assertion 전략
```java
// AssertJ 활용
assertThat(users)
    .hasSize(2)
    .extracting(User::getName)
    .containsExactly("홍길동", "김철수");

// 예외 검증
assertThatThrownBy(() -> userService.findById(999L))
    .isInstanceOf(UserNotFoundException.class)
    .hasMessage("사용자를 찾을 수 없습니다");
```

## 🔗 관련 테스트 가이드

각 계층별 상세한 테스트 방법은 다음 파일을 참조하세요:

- **Adapter Layer**: `@adapter/CLAUDE.md`
- **Application Layer**: `@application/CLAUDE.md`
- **Domain Layer**: `@domain/CLAUDE.md`
- **Common Layer**: `@common/CLAUDE.md`
- **Test Utilities**: `@testutil/CLAUDE.md`
- **Test Resources**: `@resources/CLAUDE.md`

## 🚀 CI/CD 통합

### GitHub Actions 테스트 파이프라인
```yaml
- name: Run Tests
  run: ./gradlew test
  
- name: Generate Test Report
  uses: dorny/test-reporter@v1
  if: success() || failure()
  with:
    name: Gradle Tests
    path: build/test-results/test/*.xml
    reporter: java-junit
```

### 테스트 커버리지 목표
- **전체 코버리지**: 80% 이상
- **Domain Layer**: 90% 이상 (핵심 비즈니스 로직)
- **Application Layer**: 85% 이상 (유스케이스 구현)
- **Adapter Layer**: 70% 이상 (통합 지점)