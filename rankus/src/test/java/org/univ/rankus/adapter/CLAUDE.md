# Adapter Layer 테스트 가이드

> 외부 시스템과의 통합 지점을 검증하는 Adapter Layer 테스트 전략

## 🔌 Adapter Layer 테스트 개요

### 테스트 목표
- **Controller 테스트**: REST API 엔드포인트 동작 검증
- **Repository 테스트**: 데이터 영속성 계층 검증
- **보안 통합 테스트**: 인증/인가 로직 검증
- **DTO 변환 테스트**: 데이터 직렬화/역직렬화 검증

### 테스트 범위
- HTTP 요청/응답 처리
- JSON 직렬화/역직렬화
- 데이터베이스 연동
- 보안 설정 동작
- 예외 처리 및 에러 응답

## 📁 Adapter 테스트 구조

### 현재 구현된 테스트 파일
```
src/test/java/org/univ/rankus/adapter/
├── CLAUDE.md                           # 이 파일
├── in/web/controller/                  # Controller 테스트
│   ├── AuthControllerTest.java         # 인증 API 테스트
│   ├── ControllerSecuritySmokeTests.java # 보안 연기 테스트
│   ├── LabApplicationControllerTest.java # 지원서 API 테스트
│   ├── LabApplicationControllerAuthorizationTest.java # 인가 테스트
│   ├── LabApplicationControllerSecurityTest.java # 보안 테스트
│   ├── LabImageControllerTest.java     # 이미지 API 테스트
│   ├── LabPromotionControllerTest.java # 랩실 홍보 API 테스트
│   ├── UserControllerTest.java         # 사용자 API 테스트
│   └── security/                       # 보안 통합 테스트
│       └── JwtIntegrationTest.java     # JWT 통합 테스트
└── out/persistence/                    # Repository 테스트
    └── jpa/
        ├── SpringDataLabApplicationRepositoryTest.java
        ├── SpringDataLabImageRepositoryTest.java
        ├── SpringDataLabRepositoryTest.java
        └── SpringDataUserRepositoryTest.java
```

## 🎮 Controller 테스트 전략

### 1. @WebMvcTest 활용
**목표**: Controller 레이어만 격리하여 테스트

```java
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)  // 보안 설정 임포트
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserQueryUseCase userQueryUseCase;
    
    @MockBean
    private UserCommandUseCase userCommandUseCase;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void 사용자_목록_조회_성공() throws Exception {
        // given
        List<UserResponseDto> users = Arrays.asList(
            UserResponseDto.builder().id(1L).name("홍길동").build()
        );
        when(userQueryUseCase.findAllUsers()).thenReturn(users);
        
        // when & then
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].name").value("홍길동"));
        
        verify(userQueryUseCase).findAllUsers();
    }
}
```

**특징**:
- Spring Context의 Web Layer만 로드
- UseCase는 @MockBean으로 대체
- HTTP 요청/응답 검증 중심
- 빠른 실행 속도

### 2. 보안 테스트 패턴
```java
@Test
void 인증_없이_보호된_엔드포인트_접근시_401_반환() throws Exception {
    mockMvc.perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized());
}

@Test
@WithMockUser(roles = "STUDENT")
void 권한_없는_사용자가_관리자_기능_접근시_403_반환() throws Exception {
    mockMvc.perform(post("/api/users"))
        .andExpect(status().isForbidden());
}

@Test
@WithMockUser(username = "leader@test.com", roles = "LAB_LEADER")
void 랩장이_자신의_랩실_지원서_조회_성공() throws Exception {
    // given
    Long labId = 1L;
    when(userQueryUseCase.findCurrentUser())
        .thenReturn(createLabLeaderResponse(labId));
    
    // when & then
    mockMvc.perform(get("/api/labs/{labId}/applications", labId))
        .andExpect(status().isOk());
}
```

### 3. 요청/응답 검증 패턴
```java
@Test
@WithMockUser(roles = "STUDENT")
void 지원서_제출_성공() throws Exception {
    // given
    Long labId = 1L;
    LabApplicationRequestDto request = LabApplicationRequestDto.builder()
        .interviewTime(LocalDateTime.now().plusDays(1))
        .build();
    
    LabApplicationResponseDto response = LabApplicationResponseDto.builder()
        .id(1L)
        .status(ApplicationStatus.PENDING)
        .build();
    
    when(labApplicationCommandUseCase.applyToLab(eq(labId), any()))
        .thenReturn(response);
    
    // when & then
    mockMvc.perform(post("/api/labs/{labId}/applications", labId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(201))
        .andExpect(jsonPath("$.data.status").value("PENDING"));
        
    verify(labApplicationCommandUseCase).applyToLab(eq(labId), any());
}
```

### 4. 예외 처리 테스트
```java
@Test
@WithMockUser(roles = "STUDENT")
void 존재하지_않는_랩실_지원시_404_반환() throws Exception {
    // given
    Long nonExistentLabId = 999L;
    when(labApplicationCommandUseCase.applyToLab(eq(nonExistentLabId), any()))
        .thenThrow(new LabNotFoundException());
    
    // when & then
    mockMvc.perform(post("/api/labs/{labId}/applications", nonExistentLabId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.code").value("LAB_NOT_FOUND"));
}
```

## 💾 Repository 테스트 전략

### 1. @DataJpaTest 활용
**목표**: JPA Repository 동작 검증

```java
@DataJpaTest
@Import(TestDataConfig.class)  // 테스트 설정 임포트
class SpringDataUserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private SpringDataUserRepository userRepository;
    
    @Test
    void 이메일로_사용자_조회_성공() {
        // given
        User user = User.create("홍길동", "hong@test.com", "password123!", Role.STUDENT);
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> found = userRepository.findByEmail("hong@test.com");
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("홍길동");
    }
    
    @Test
    void 이메일_중복_체크_성공() {
        // given
        User user = User.create("홍길동", "hong@test.com", "password123!", Role.STUDENT);
        entityManager.persistAndFlush(user);
        
        // when
        boolean exists = userRepository.existsByEmail("hong@test.com");
        
        // then
        assertThat(exists).isTrue();
    }
}
```

**특징**:
- JPA 관련 구성요소만 로드
- 자동으로 H2 인메모리 DB 사용 (또는 테스트 DB)
- @Transactional로 자동 롤백
- TestEntityManager로 테스트 데이터 관리

### 2. 복합 쿼리 테스트
```java
@Test
void 랩실별_승인된_지원서_조회() {
    // given
    Lab lab = createAndSaveLab("AI랩");
    User user1 = createAndSaveUser("사용자1");
    User user2 = createAndSaveUser("사용자2");
    
    LabApplication application1 = createAndSaveApplication(lab, user1, ApplicationStatus.APPROVED);
    LabApplication application2 = createAndSaveApplication(lab, user2, ApplicationStatus.PENDING);
    
    // when
    List<LabApplication> approvedApplications = 
        labApplicationRepository.findByLabIdAndStatus(lab.getId(), ApplicationStatus.APPROVED);
    
    // then
    assertThat(approvedApplications).hasSize(1);
    assertThat(approvedApplications.get(0).getUser().getName()).isEqualTo("사용자1");
}
```

### 3. 페이징 테스트
```java
@Test
void 사용자_목록_페이징_조회() {
    // given
    for (int i = 1; i <= 15; i++) {
        User user = User.create("사용자" + i, "user" + i + "@test.com", "password123!", Role.STUDENT);
        entityManager.persist(user);
    }
    entityManager.flush();
    
    Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
    
    // when
    Page<User> userPage = userRepository.findAll(pageable);
    
    // then
    assertThat(userPage.getContent()).hasSize(10);
    assertThat(userPage.getTotalElements()).isEqualTo(15);
    assertThat(userPage.getTotalPages()).isEqualTo(2);
}
```

## 🔐 보안 통합 테스트

### 1. JWT 통합 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Test
    void 유효한_JWT_토큰으로_인증된_요청_처리() {
        // given
        String token = jwtTokenProvider.createToken("test@example.com", Role.STUDENT);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        // when
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
            "/api/users/me",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ApiResponse.class
        );
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    @Test
    void 만료된_JWT_토큰으로_요청시_401_반환() {
        // given
        String expiredToken = createExpiredToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(expiredToken);
        
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
            "/api/users/me",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ErrorResponse.class
        );
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

### 2. 권한 검증 통합 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class LabApplicationControllerAuthorizationTest {
    
    @Test
    @Order(1)
    void 랩장이_자신의_랩실_지원서_승인_성공() {
        // given
        String token = createLabLeaderToken(1L);  // 1번 랩실 랩장
        
        // when
        ResponseEntity<ApiResponse> response = 
            performApproveApplication(1L, 1L, token);  // 1번 랩실, 1번 지원서
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    @Test
    @Order(2)
    void 다른_랩실_랩장이_지원서_승인시_403_반환() {
        // given
        String token = createLabLeaderToken(2L);  // 2번 랩실 랩장
        
        // when
        ResponseEntity<ErrorResponse> response = 
            performApproveApplicationExpectingError(1L, 1L, token);  // 1번 랩실 지원서 승인 시도
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
```

## 📊 DTO 테스트 전략

### 1. JSON 직렬화/역직렬화 테스트
```java
@JsonTest
class UserResponseDtoJsonTest {
    
    @Autowired
    private JacksonTester<UserResponseDto> json;
    
    @Test
    void JSON_직렬화_테스트() throws Exception {
        // given
        UserResponseDto dto = UserResponseDto.builder()
            .id(1L)
            .name("홍길동")
            .email("hong@test.com")
            .role(Role.STUDENT)
            .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
            .build();
        
        // when & then
        assertThat(json.write(dto))
            .hasJsonPath("$.id")
            .hasJsonPath("$.name")
            .hasJsonPathValue("$.name", "홍길동")
            .hasJsonPathValue("$.role", "STUDENT");
    }
    
    @Test
    void JSON_역직렬화_테스트() throws Exception {
        // given
        String jsonContent = """
            {
                "id": 1,
                "name": "홍길동",
                "email": "hong@test.com",
                "role": "STUDENT"
            }
            """;
        
        // when
        UserResponseDto dto = json.parseObject(jsonContent);
        
        // then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("홍길동");
        assertThat(dto.getRole()).isEqualTo(Role.STUDENT);
    }
}
```

### 2. 검증 어노테이션 테스트
```java
@ExtendWith(MockitoExtension.class)
class LabApplicationRequestDtoValidationTest {
    
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    @Test
    void 유효한_요청_데이터_검증_성공() {
        // given
        LabApplicationRequestDto request = LabApplicationRequestDto.builder()
            .interviewTime(LocalDateTime.now().plusDays(1))
            .build();
        
        // when
        Set<ConstraintViolation<LabApplicationRequestDto>> violations = 
            validator.validate(request);
        
        // then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void 과거_면접시간_검증_실패() {
        // given
        LabApplicationRequestDto request = LabApplicationRequestDto.builder()
            .interviewTime(LocalDateTime.now().minusDays(1))  // 과거 시간
            .build();
        
        // when
        Set<ConstraintViolation<LabApplicationRequestDto>> violations = 
            validator.validate(request);
        
        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("미래 시점이어야 합니다");
    }
}
```

## 🎯 Adapter 테스트 베스트 프랙티스

### 1. Controller 테스트 원칙
- **단일 책임**: 하나의 엔드포인트당 하나의 테스트
- **모든 HTTP 상태**: 성공, 실패 시나리오 모두 테스트
- **보안 검증**: 인증/인가 요구사항 검증
- **데이터 검증**: 요청/응답 데이터 형식 검증

### 2. Repository 테스트 원칙
- **쿼리 로직**: 복잡한 쿼리는 반드시 테스트
- **제약 조건**: DB 제약 조건 위반 시나리오 테스트
- **성능**: 대용량 데이터에 대한 쿼리 성능 검증
- **트랜잭션**: 트랜잭션 경계 동작 검증

### 3. 통합 테스트 원칙
- **End-to-End**: 실제 HTTP 요청부터 DB까지 전체 플로우
- **보안 통합**: JWT, 권한 검증 등 보안 기능 통합
- **환경 격리**: 테스트 환경과 개발 환경 완전 분리
- **데이터 정리**: 테스트 후 데이터 자동 정리

### 4. 테스트 데이터 관리
```java
// 테스트 베이스 클래스 활용
@TestMethodOrder(OrderAnnotation.class)
abstract class BaseControllerTest {
    
    protected String createTestUserToken(Role role) {
        User testUser = DomainUserFactory.createWithRole(role);
        return jwtTokenProvider.createToken(testUser.getEmail(), testUser.getRole());
    }
    
    protected void setupTestData() {
        // 공통 테스트 데이터 설정
    }
    
    @AfterEach
    void cleanupTestData() {
        // 테스트 데이터 정리
    }
}
```

## 🔗 관련 테스트 가이드

- **테스트 전략**: `@test/CLAUDE.md`
- **Application 테스트**: `@application/CLAUDE.md`
- **Domain 테스트**: `@domain/CLAUDE.md`
- **테스트 유틸리티**: `@testutil/CLAUDE.md`
- **보안 테스트**: `@common/CLAUDE.md`

## 📈 성능 및 품질 지표

### 테스트 커버리지 목표
- **Controller**: 85% 이상 (모든 엔드포인트 커버)
- **Repository**: 90% 이상 (모든 쿼리 메서드 검증)
- **DTO**: 70% 이상 (핵심 변환 로직)

### 테스트 실행 성능
- **단위 테스트**: 평균 50ms 이하
- **통합 테스트**: 평균 500ms 이하
- **E2E 테스트**: 평균 2초 이하