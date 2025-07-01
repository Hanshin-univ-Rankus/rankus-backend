# Common Layer 테스트 가이드

> 공통 컴포넌트와 보안 기능을 검증하는 Common Layer 테스트 전략

## 🛠️ Common Layer 테스트 개요

### 테스트 목표

- **보안 컴포넌트 검증**: JWT, 인증/인가 로직의 정확한 동작
- **예외 처리 검증**: 전역 예외 처리기의 올바른 에러 응답
- **공통 유틸리티 검증**: 모든 계층에서 사용되는 공통 기능
- **설정 검증**: 보안 설정 및 권한 평가 로직

### 테스트 범위

- JWT 토큰 생성, 검증, 만료 처리
- 권한 평가 로직 (Permission Evaluator)
- 전역 예외 처리기 동작
- 보안 필터 체인 동작
- 커스텀 UserDetails 구현

## 📁 Common 테스트 구조

### 현재 구현된 테스트 파일

```
src/test/java/org/univ/rankus/common/
├── CLAUDE.md                              # 이 파일
└── security/
    └── permission/
        └── LabApplicationPermissionHandlerTest.java
```

### 확장 가능한 테스트 구조 (향후 구현)

```
src/test/java/org/univ/rankus/common/
├── CLAUDE.md
├── exception/                             # 예외 처리 테스트
│   ├── GlobalExceptionHandlerTest.java
│   └── ErrorResponseTest.java
├── security/                              # 보안 컴포넌트 테스트
│   ├── jwt/
│   │   ├── JwtTokenProviderTest.java
│   │   ├── JwtAuthenticationFilterTest.java
│   │   └── JwtAuthenticationEntryPointTest.java
│   ├── customUser/
│   │   ├── CustomUserDetailsTest.java
│   │   └── CustomUserDetailsServiceTest.java
│   └── permission/
│       ├── UnifiedPermissionEvaluatorTest.java
│       ├── LabApplicationPermissionHandlerTest.java
│       └── LabImagePermissionHandlerTest.java
└── util/                                  # 공통 유틸리티 테스트
    └── ValidationUtilTest.java
```

## 🔐 JWT 보안 테스트 전략

### 1. JwtTokenProvider 테스트

```java
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;
    
    @BeforeEach
    void setUp() {
        // 테스트용 시크릿 키 설정
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", 
            "test-secret-key-32-characters-long");
        ReflectionTestUtils.setField(jwtTokenProvider, "tokenValidityInMilliseconds", 
            3600000L); // 1시간
    }
    
    @Test
    void 유효한_정보로_JWT_토큰_생성_성공() {
        // given
        String email = "test@example.com";
        Role role = Role.STUDENT;
        
        // when
        String token = jwtTokenProvider.createToken(email, role);
        
        // then
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT 구조: header.payload.signature
    }
    
    @Test
    void 생성된_토큰에서_이메일_추출_성공() {
        // given
        String email = "test@example.com";
        Role role = Role.STUDENT;
        String token = jwtTokenProvider.createToken(email, role);
        
        // when
        String extractedEmail = jwtTokenProvider.getEmail(token);
        
        // then
        assertThat(extractedEmail).isEqualTo(email);
    }
    
    @Test
    void 유효한_토큰_검증_성공() {
        // given
        String token = jwtTokenProvider.createToken("test@example.com", Role.STUDENT);
        
        // when
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void 잘못된_형식의_토큰_검증_실패() {
        // given
        String invalidToken = "invalid.jwt.token";
        
        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        // then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void 만료된_토큰_검증_실패() {
        // given
        // 토큰 유효시간을 1ms로 설정
        ReflectionTestUtils.setField(jwtTokenProvider, "tokenValidityInMilliseconds", 1L);
        String token = jwtTokenProvider.createToken("test@example.com", Role.STUDENT);
        
        // when
        // 토큰 만료를 위해 잠시 대기
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // then
        assertThat(isValid).isFalse();
    }
}
```

### 2. JwtAuthenticationFilter 테스트

```java
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private CustomUserDetailsService userDetailsService;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Test
    void 유효한_JWT_토큰으로_인증_설정_성공() throws Exception {
        // given
        String token = "valid.jwt.token";
        String email = "test@example.com";
        User user = DomainUserFactory.createStudent();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        assertThat(authentication.isAuthenticated()).isTrue();
        
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    void JWT_토큰이_없는_경우_인증_설정하지_않음() throws Exception {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(any());
    }
    
    @Test
    void 잘못된_토큰_형식인_경우_인증_설정하지_않음() throws Exception {
        // given
        when(request.getHeader("Authorization")).thenReturn("Invalid token");
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(any());
    }
}
```

## 🔍 권한 평가 테스트 전략

### 1. UnifiedPermissionEvaluator 테스트

```java
@ExtendWith(MockitoExtension.class)
class UnifiedPermissionEvaluatorTest {
    
    @Mock
    private LabApplicationPermissionHandler labApplicationPermissionHandler;
    
    @Mock
    private LabImagePermissionHandler labImagePermissionHandler;
    
    @InjectMocks
    private UnifiedPermissionEvaluator unifiedPermissionEvaluator;
    
    @Test
    void LabApplication_권한_평가_위임_성공() {
        // given
        Authentication authentication = createMockAuthentication();
        Long targetId = 1L;
        String targetType = "LabApplication";
        String permission = "VIEW";
        
        when(labApplicationPermissionHandler.hasPermission(authentication, targetId, permission))
            .thenReturn(true);
        
        // when
        boolean result = unifiedPermissionEvaluator.hasPermission(
            authentication, targetId, targetType, permission);
        
        // then
        assertThat(result).isTrue();
        verify(labApplicationPermissionHandler).hasPermission(authentication, targetId, permission);
        verify(labImagePermissionHandler, never()).hasPermission(any(), any(), any());
    }
    
    @Test
    void 인증되지_않은_사용자_권한_평가_실패() {
        // given
        Authentication unauthenticated = createUnauthenticatedAuthentication();
        
        // when
        boolean result = unifiedPermissionEvaluator.hasPermission(
            unauthenticated, 1L, "LabApplication", "VIEW");
        
        // then
        assertThat(result).isFalse();
        verify(labApplicationPermissionHandler, never()).hasPermission(any(), any(), any());
    }
    
    @Test
    void 지원되지_않는_타겟_타입_권한_평가_실패() {
        // given
        Authentication authentication = createMockAuthentication();
        
        // when
        boolean result = unifiedPermissionEvaluator.hasPermission(
            authentication, 1L, "UnsupportedType", "VIEW");
        
        // then
        assertThat(result).isFalse();
    }
}
```

### 2. LabApplicationPermissionHandler 테스트

```java
@ExtendWith(MockitoExtension.class)
class LabApplicationPermissionHandlerTest {
    
    @Mock
    private LabApplicationRepositoryPort labApplicationRepositoryPort;
    
    @Mock
    private UserRepositoryPort userRepositoryPort;
    
    @InjectMocks
    private LabApplicationPermissionHandler permissionHandler;
    
    @Test
    void 지원자_본인_지원서_조회_권한_허용() {
        // given
        Authentication authentication = createStudentAuthentication("student@test.com");
        Long applicationId = 1L;
        
        User applicant = DomainUserFactory.createStudent();
        applicant.setEmail("student@test.com");
        LabApplication application = DomainLabApplicationFactory.createPending(null, applicant);
        
        when(userRepositoryPort.findByEmail("student@test.com"))
            .thenReturn(Optional.of(applicant));
        when(labApplicationRepositoryPort.findById(applicationId))
            .thenReturn(Optional.of(application));
        
        // when
        boolean result = permissionHandler.hasPermission(authentication, applicationId, "VIEW");
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    void 랩장이_자신의_랩실_지원서_승인_권한_허용() {
        // given
        Authentication authentication = createLabLeaderAuthentication("leader@test.com");
        Long applicationId = 1L;
        
        Lab lab = DomainLabFactory.createAiLab();
        lab.setId(1L);
        
        User labLeader = DomainUserFactory.createLabLeader();
        labLeader.setEmail("leader@test.com");
        labLeader.assignLab(lab);
        
        User applicant = DomainUserFactory.createStudent();
        LabApplication application = DomainLabApplicationFactory.createPending(lab, applicant);
        
        when(userRepositoryPort.findByEmail("leader@test.com"))
            .thenReturn(Optional.of(labLeader));
        when(labApplicationRepositoryPort.findById(applicationId))
            .thenReturn(Optional.of(application));
        
        // when
        boolean result = permissionHandler.hasPermission(authentication, applicationId, "APPROVE");
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    void 다른_랩실_학생이_지원서_조회_권한_거부() {
        // given
        Authentication authentication = createStudentAuthentication("other@test.com");
        Long applicationId = 1L;
        
        User otherStudent = DomainUserFactory.createStudent();
        otherStudent.setEmail("other@test.com");
        
        User applicant = DomainUserFactory.createStudent();
        applicant.setEmail("applicant@test.com");
        LabApplication application = DomainLabApplicationFactory.createPending(null, applicant);
        
        when(userRepositoryPort.findByEmail("other@test.com"))
            .thenReturn(Optional.of(otherStudent));
        when(labApplicationRepositoryPort.findById(applicationId))
            .thenReturn(Optional.of(application));
        
        // when
        boolean result = permissionHandler.hasPermission(authentication, applicationId, "VIEW");
        
        // then
        assertThat(result).isFalse();
    }
}
```

## ⚠️ 예외 처리 테스트 전략

### GlobalExceptionHandler 테스트

```java
@WebMvcTest
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserQueryUseCase userQueryUseCase;
    
    @Test
    void 도메인_예외_처리_검증() throws Exception {
        // given
        when(userQueryUseCase.findUserById(999L))
            .thenThrow(new UserNotFoundException(999L));
        
        // when & then
        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void 검증_예외_처리_검증() throws Exception {
        // given
        String invalidRequest = """
            {
                "name": "",
                "email": "invalid-email",
                "password": "123"
            }
            """;
        
        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void 접근_거부_예외_처리_검증() throws Exception {
        // given
        // 권한이 없는 사용자로 설정
        
        // when & then
        mockMvc.perform(post("/api/admin/users")
                .with(user("student@test.com").roles("STUDENT")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }
}
```

## 🧪 CustomUserDetails 테스트

### CustomUserDetails 구현 테스트

```java
class CustomUserDetailsTest {
    
    @Test
    void 사용자_정보로_UserDetails_생성_성공() {
        // given
        User user = DomainUserFactory.createStudent();
        user.setEmail("test@example.com");
        
        // when
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        // then
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword().getValue());
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
            .isEqualTo("ROLE_STUDENT");
        
        // 계정 상태 확인
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
        
        // 원본 사용자 객체 접근
        assertThat(userDetails.getUser()).isEqualTo(user);
    }
    
    @Test
    void 다양한_역할의_권한_변환_검증() {
        // given
        Map<Role, String> roleToAuthority = Map.of(
            Role.STUDENT, "ROLE_STUDENT",
            Role.LAB_LEADER, "ROLE_LAB_LEADER",
            Role.PROFESSOR, "ROLE_PROFESSOR",
            Role.ADMIN, "ROLE_ADMIN"
        );
        
        // when & then
        roleToAuthority.forEach((role, expectedAuthority) -> {
            User user = DomainUserFactory.createWithRole(role);
            CustomUserDetails userDetails = new CustomUserDetails(user);
            
            assertThat(userDetails.getAuthorities()).hasSize(1);
            assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                .isEqualTo(expectedAuthority);
        });
    }
}
```

## 🎯 Common 테스트 베스트 프랙티스

### 1. 보안 테스트 원칙

- **토큰 생명주기**: 생성, 검증, 만료 전체 사이클 테스트
- **권한 경계**: 허용/거부 경계 조건 명확히 테스트
- **보안 설정**: 프로덕션과 동일한 보안 설정 테스트
- **인증 플로우**: 전체 인증 과정 End-to-End 테스트

### 2. Mock 보안 컨텍스트 설정

```java
// 테스트용 인증 컨텍스트 생성 유틸리티
public class SecurityTestUtils {
    
    public static Authentication createAuthentication(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
    }
    
    public static void setSecurityContext(User user) {
        Authentication authentication = createAuthentication(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
```

### 3. JWT 테스트 데이터 관리

```java
@TestConfiguration
public class JwtTestConfig {
    
    @Bean
    @Primary
    public JwtTokenProvider testJwtTokenProvider() {
        return new JwtTokenProvider(
            "test-secret-key-32-characters-long",
            3600000L  // 1시간
        );
    }
    
    @Bean
    public JwtTestHelper jwtTestHelper() {
        return new JwtTestHelper(testJwtTokenProvider());
    }
}

public class JwtTestHelper {
    private final JwtTokenProvider jwtTokenProvider;
    
    public String createTokenFor(User user) {
        return jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }
    
    public String createExpiredToken(User user) {
        // 만료된 토큰 생성 로직
    }
}
```

### 4. 예외 테스트 패턴

```java
@Test
void 예외_응답_형식_검증() {
    // given
    UserNotFoundException exception = new UserNotFoundException(999L);
    
    // when
    ErrorResponse errorResponse = ErrorResponse.of(exception.getErrorCode());
    
    // then
    assertThat(errorResponse.getStatus()).isEqualTo(404);
    assertThat(errorResponse.getCode()).isEqualTo("USER_NOT_FOUND");
    assertThat(errorResponse.getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
    assertThat(errorResponse.getTimestamp()).isNotNull();
}
```

## 📊 Common 테스트 메트릭

### 커버리지 목표

- **보안 컴포넌트**: 95% 이상 (JWT, 권한 평가)
- **예외 처리**: 90% 이상 (모든 예외 시나리오)
- **공통 유틸리티**: 85% 이상

### 테스트 성능 목표

- **JWT 테스트**: 평균 5ms 이하
- **권한 평가 테스트**: 평균 10ms 이하
- **예외 처리 테스트**: 평균 50ms 이하

### 보안 테스트 체크리스트

- [ ] 토큰 생성/검증 전체 사이클
- [ ] 토큰 만료 처리
- [ ] 잘못된 토큰 형식 처리
- [ ] 권한 평가 모든 시나리오
- [ ] 인증되지 않은 사용자 처리
- [ ] 예외 상황별 적절한 HTTP 상태코드

## 🔗 관련 테스트 가이드

- **테스트 전략**: `@test/CLAUDE.md`
- **Adapter 보안 테스트**: `@adapter/CLAUDE.md`
- **테스트 유틸리티**: `@testutil/CLAUDE.md`
- **Mock 유틸리티**: `@testutil/mock/CLAUDE.md`