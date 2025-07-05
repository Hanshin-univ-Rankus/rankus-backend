# Application Layer 테스트 가이드

> 유스케이스 구현과 비즈니스 플로우를 검증하는 Application Layer 테스트 전략

## 🎯 Application Layer 테스트 개요

### 테스트 목표

- **유스케이스 구현 검증**: 비즈니스 요구사항의 정확한 구현
- **포트 상호작용 검증**: 의존성 주입된 포트들의 올바른 호출
- **트랜잭션 경계 검증**: 데이터 일관성과 원자성 보장
- **예외 처리 검증**: 비즈니스 예외의 적절한 처리

### 테스트 범위

- Service 클래스의 비즈니스 로직
- UseCase 인터페이스 구현 검증
- 포트 인터페이스 호출 및 응답 처리
- 도메인 객체 조합 및 변환
- 트랜잭션 경계 동작

## 📁 Application 테스트 구조

### 현재 구현된 테스트 파일

```
src/test/java/org/univ/rankus/application/
├── CLAUDE.md                          # 이 파일
└── service/
    ├── auth/                          # 인증 서비스 테스트
    │   └── AuthServiceTest.java       # 로그인, 회원가입 테스트
    ├── command/                       # Command 서비스 테스트
    │   ├── LabApplicationCommandServiceTest.java
    │   ├── LabImageCommandServiceTest.java
    │   └── UserCommandServiceTest.java
    └── query/                         # Query 서비스 테스트
        ├── LabApplicationQueryServiceTest.java
        ├── LabImageQueryServiceTest.java
        ├── LabPromotionQueryServiceTest.java
        └── UserQueryServiceTest.java
```

## 🧪 Service 테스트 전략

### 1. @ExtendWith(MockitoExtension.class) 패턴

**목표**: 순수한 서비스 로직 검증, 의존성 격리

```java

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    // 의존성 Mock 처리
    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    // 테스트 대상 Service 주입
    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    void 유효한_정보로_사용자_생성_성공() {
        // given
        UserCreateRequestDto request = UserCreateRequestDto.builder()
                .name("홍길동")
                .email("hong@test.com")
                .password("password123!")
                .role(Role.STUDENT)
                .build();

        User savedUser = User.create("홍길동", "hong@test.com", "encodedPassword", Role.STUDENT);
        savedUser.setId(1L);

        when(userRepositoryPort.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        // when
        UserResponseDto result = userCommandService.createUser(request);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getEmail()).isEqualTo("hong@test.com");

        // 의존성 호출 검증
        verify(userRepositoryPort).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepositoryPort).save(any(User.class));
    }
}
```

**특징**:

- 모든 외부 의존성을 Mock으로 대체
- 서비스의 순수한 비즈니스 로직에 집중
- 빠른 실행 속도
- 의존성 호출 검증 (verify)

### 2. 예외 시나리오 테스트

```java

@Test
void 중복된_이메일로_사용자_생성시_예외_발생() {
    // given
    UserCreateRequestDto request = UserCreateRequestDto.builder()
            .email("duplicate@test.com")
            .build();

    when(userRepositoryPort.existsByEmail(request.getEmail())).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> userCommandService.createUser(request))
            .isInstanceOf(UserValidationException.class)
            .hasMessage("이미 사용 중인 이메일입니다");

    // 저장 호출되지 않았는지 검증
    verify(userRepositoryPort, never()).save(any(User.class));
}
```

### 3. 복잡한 비즈니스 로직 테스트

```java

@Test
void 지원서_승인시_사용자_랩실_할당_및_알림_발송() {
    // given
    Long applicationId = 1L;

    User applicant = DomainUserFactory.createStudent();
    Lab lab = DomainLabFactory.createAiLab();
    LabApplication application = DomainLabApplicationFactory.createPending(lab, applicant);

    when(labApplicationRepositoryPort.findById(applicationId))
            .thenReturn(Optional.of(application));
    when(labApplicationRepositoryPort.save(any(LabApplication.class)))
            .thenReturn(application);

    // when
    LabApplicationResponseDto result = labApplicationCommandService.approveApplication(applicationId);

    // then
    assertThat(result.getStatus()).isEqualTo(ApplicationStatus.APPROVED);

    // 복잡한 비즈니스 플로우 검증
    verify(labApplicationRepositoryPort).findById(applicationId);
    verify(labApplicationRepositoryPort).save(application);
    // 향후 확장: 알림 서비스 호출 검증
    // verify(notificationService).sendApprovalNotification(applicant);
}
```

## 🔄 Command vs Query 서비스 테스트

### Command Service 테스트 특징

**목표**: 상태 변경 로직 및 트랜잭션 경계 검증

```java

@ExtendWith(MockitoExtension.class)
class LabApplicationCommandServiceTest {

    @Mock
    private LabApplicationRepositoryPort labApplicationRepositoryPort;
    @Mock
    private LabRepositoryPort labRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private LabApplicationCommandService labApplicationCommandService;

    @Test
    void 랩실_지원_성공() {
        // given
        Long labId = 1L;
        LabApplicationRequestDto request = LabApplicationRequestDto.builder()
                .interviewTime(LocalDateTime.now().plusDays(1))
                .build();

        Lab lab = DomainLabFactory.createAiLab();
        User user = DomainUserFactory.createStudent();
        LabApplication savedApplication = DomainLabApplicationFactory.createPending(lab, user);

        when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab));
        when(labApplicationRepositoryPort.existsByLabIdAndUserId(labId, user.getId()))
                .thenReturn(false);
        when(labApplicationRepositoryPort.save(any(LabApplication.class)))
                .thenReturn(savedApplication);

        // when
        LabApplicationResponseDto result = labApplicationCommandService.applyToLab(labId, request);

        // then
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);

        // 상태 변경 검증
        ArgumentCaptor<LabApplication> applicationCaptor =
                ArgumentCaptor.forClass(LabApplication.class);
        verify(labApplicationRepositoryPort).save(applicationCaptor.capture());

        LabApplication capturedApplication = applicationCaptor.getValue();
        assertThat(capturedApplication.getLab()).isEqualTo(lab);
        assertThat(capturedApplication.getUser()).isEqualTo(user);
    }
}
```

### Query Service 테스트 특징

**목표**: 조회 로직 및 데이터 변환 검증

```java

@ExtendWith(MockitoExtension.class)
class LabPromotionQueryServiceTest {

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @Mock
    private LabImageRepositoryPort labImageRepositoryPort;

    @InjectMocks
    private LabPromotionQueryService labPromotionQueryService;

    @Test
    void 모든_랩실_목록_조회_성공() {
        // given
        List<Lab> labs = Arrays.asList(
                DomainLabFactory.createAiLab(),
                DomainLabFactory.createDbLab()
        );
        when(labRepositoryPort.findAll()).thenReturn(labs);

        // when
        List<LabResponseDto> result = labPromotionQueryService.findAllLabs();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("AI랩");
        assertThat(result.get(1).getName()).isEqualTo("DB랩");

        verify(labRepositoryPort).findAll();
    }

    @Test
    void 랩실_상세_조회_이미지_포함() {
        // given
        Long labId = 1L;
        Lab lab = DomainLabFactory.createAiLab();
        List<LabImage> images = Arrays.asList(
                DomainLabImageFactory.createRepresentative(lab),
                DomainLabImageFactory.createAdditional(lab)
        );

        when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab));
        when(labImageRepositoryPort.findByLabId(labId)).thenReturn(images);

        // when
        LabResponseDto result = labPromotionQueryService.findLabById(labId);

        // then
        assertThat(result.getName()).isEqualTo("AI랩");
        assertThat(result.getImages()).hasSize(2);

        verify(labRepositoryPort).findById(labId);
        verify(labImageRepositoryPort).findByLabId(labId);
    }
}
```

## 🔐 인증 서비스 테스트

### AuthService 테스트 특별 고려사항

```java

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void 유효한_자격증명으로_로그인_성공() {
        // given
        UserLoginRequestDto request = UserLoginRequestDto.builder()
                .email("user@test.com")
                .password("password123!")
                .build();

        User user = User.create("홍길동", "user@test.com", "encodedPassword", Role.STUDENT);
        String expectedToken = "jwt.token.string";

        when(userRepositoryPort.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword().getValue()))
                .thenReturn(true);
        when(jwtTokenProvider.createToken(user.getEmail(), user.getRole()))
                .thenReturn(expectedToken);

        // when
        AuthResponseDto result = authService.login(request);

        // then
        assertThat(result.getAccessToken()).isEqualTo(expectedToken);
        assertThat(result.getUser().getName()).isEqualTo("홍길동");

        verify(userRepositoryPort).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword().getValue());
        verify(jwtTokenProvider).createToken(user.getEmail(), user.getRole());
    }

    @Test
    void 잘못된_비밀번호로_로그인시_예외_발생() {
        // given
        UserLoginRequestDto request = UserLoginRequestDto.builder()
                .email("user@test.com")
                .password("wrongPassword")
                .build();

        User user = User.create("홍길동", "user@test.com", "encodedPassword", Role.STUDENT);

        when(userRepositoryPort.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword().getValue()))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserValidationException.class)
                .hasMessage("비밀번호가 올바르지 않습니다");

        // 토큰 생성되지 않았는지 검증
        verify(jwtTokenProvider, never()).createToken(any(), any());
    }
}
```

## 🧩 Mock 활용 전략

### MockitoExtension 설정 (중요)

```java

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // ✅ 필수: UnnecessaryStubbingException 방지
class UserCommandServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private UserCommandService userCommandService;

    // 테스트 메서드들...
}
```

**주의사항**:

- `@MockitoSettings(strictness = Strictness.LENIENT)` 어노테이션 필수
- 사용되지 않는 Mock stubbing으로 인한 UnnecessaryStubbingException 방지
- 테스트 안정성 향상

### 1. Mock 객체 설정 패턴

```java

@BeforeEach
void setUp() {
    // 공통 Mock 설정
    when(userRepositoryPort.findById(any(Long.class)))
            .thenAnswer(invocation -> {
                Long id = invocation.getArgument(0);
                return Optional.of(DomainUserFactory.createWithId(id));
            });
}
```

### 2. ArgumentCaptor 활용

```java

@Test
void 지원서_저장시_올바른_데이터_전달_검증() {
    // given
    // ... 테스트 데이터 준비

    // when
    labApplicationCommandService.applyToLab(labId, request);

    // then
    ArgumentCaptor<LabApplication> captor = ArgumentCaptor.forClass(LabApplication.class);
    verify(labApplicationRepositoryPort).save(captor.capture());

    LabApplication savedApplication = captor.getValue();
    assertThat(savedApplication.getInterviewTime()).isEqualTo(request.getInterviewTime());
    assertThat(savedApplication.getStatus()).isEqualTo(ApplicationStatus.PENDING);
}
```

### 3. Mock 응답 체이닝

```java

@Test
void 복잡한_조회_로직_체이닝_테스트() {
    // given
    Long labId = 1L;
    Lab lab = DomainLabFactory.createAiLab();
    List<LabApplication> applications = Arrays.asList(/* 지원서 목록 */);

    when(labRepositoryPort.findById(labId))
            .thenReturn(Optional.of(lab));
    when(labApplicationRepositoryPort.findByLabId(labId))
            .thenReturn(applications);
    when(userRepositoryPort.findByLabId(labId))
            .thenReturn(Arrays.asList(/* 멤버 목록 */));

    // when
    LabDetailResponseDto result = labQueryService.findLabDetail(labId);

    // then
    // 복합 조회 결과 검증
}
```

## 🎯 Application 테스트 베스트 프랙티스

### 1. 테스트 구조 원칙

```java
// Given-When-Then 패턴 엄격 적용
@Test
void 메서드명은_한국어로_명확한_시나리오_표현() {
    // given - 테스트 준비
    // 필요한 모든 Mock 설정
    // 테스트 데이터 준비

    // when - 실제 메서드 호출
    // 테스트 대상 메서드 단일 호출

    // then - 결과 검증
    // 반환값 검증
    // Mock 호출 검증
    // 상태 변경 검증
}
```

### 2. Mock 검증 수준

```java
// 1단계: 반환값 검증
assertThat(result).

isNotNull();

assertThat(result.getId()).

isEqualTo(expectedId);

// 2단계: 의존성 호출 검증
verify(repositoryPort).

save(any(Domain.class));

verify(repositoryPort, times(1)).

findById(id);

// 3단계: 호출 순서 검증 (필요시)
InOrder inOrder = inOrder(repositoryPort, notificationService);
inOrder.

verify(repositoryPort).

save(any());
        inOrder.

verify(notificationService).

send(any());

// 4단계: 호출되지 않음 검증
verify(repositoryPort, never()).

delete(any());
```

### 3. 예외 테스트 패턴

```java

@Test
void 비즈니스_규칙_위반시_적절한_예외_발생() {
    // given
    // 예외 발생 조건 설정

    // when & then
    assertThatThrownBy(() -> service.businessMethod(invalidInput))
            .isInstanceOf(SpecificException.class)
            .hasMessage("예상되는 에러 메시지")
            .satisfies(exception -> {
                // 추가 예외 속성 검증
                assertThat(exception.getErrorCode()).isEqualTo(EXPECTED_ERROR_CODE);
            });
}
```

### 4. 테스트 데이터 관리

```java
// 테스트 클래스별 공통 데이터 관리
class UserCommandServiceTest {

    private UserCreateRequestDto validRequest;
    private User savedUser;

    @BeforeEach
    void setUpTestData() {
        validRequest = UserCreateRequestDto.builder()
                .name("홍길동")
                .email("hong@test.com")
                .password("password123!")
                .role(Role.STUDENT)
                .build();

        savedUser = DomainUserFactory.createFromRequest(validRequest);
        savedUser.setId(1L);
    }

    // 각 테스트에서 공통 데이터 활용
}
```

## 📊 테스트 메트릭 및 품질

### 커버리지 목표

- **Command Service**: 90% 이상 (상태 변경 로직 중요)
- **Query Service**: 85% 이상 (조회 로직 및 변환)
- **Auth Service**: 95% 이상 (보안 관련 핵심 로직)

### 테스트 성능 목표

- **단위 테스트**: 평균 10ms 이하
- **Mock 설정**: 테스트당 최대 5개 Mock
- **테스트 격리**: 각 테스트 독립 실행 가능

### 품질 체크리스트

- [ ] 모든 public 메서드 테스트 커버
- [ ] 예외 시나리오 테스트 포함
- [ ] Mock 호출 검증 포함
- [ ] Given-When-Then 패턴 준수
- [ ] 테스트 메서드명 명확성
- [ ] 테스트 데이터 격리

## 🔗 관련 테스트 가이드

- **테스트 전략**: `@test/CLAUDE.md`
- **Domain 테스트**: `@domain/CLAUDE.md`
- **Adapter 테스트**: `@adapter/CLAUDE.md`
- **테스트 유틸리티**: `@testutil/CLAUDE.md`
- **테스트 팩토리**: `@testutil/factory/CLAUDE.md`