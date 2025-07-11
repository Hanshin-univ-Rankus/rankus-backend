# TestUtil 가이드

> 테스트 작성을 위한 유틸리티, 팩토리, Mock 헬퍼 클래스 활용 가이드

## 🧰 TestUtil 개요

### 핵심 목표

- **테스트 코드 재사용**: 공통 테스트 로직 및 데이터 생성 패턴 표준화
- **테스트 작성 효율성**: 반복적인 테스트 설정 및 데이터 생성 자동화
- **일관된 테스트 품질**: 표준화된 테스트 헬퍼를 통한 테스트 품질 향상
- **유지보수성**: 테스트 데이터 변경 시 중앙화된 관리 지점 제공

### 구성 요소

- **Factory 패턴**: 도메인 객체 및 통합 테스트 데이터 생성
- **Mock 유틸리티**: 인증, 쿼리 등 공통 Mock 설정
- **Base 테스트 클래스**: 계층별 테스트 공통 설정
- **테스트 헬퍼**: 반복적인 테스트 로직 추상화

## 📁 TestUtil 구조

### 현재 구현된 구조

```
src/test/java/org/univ/rankus/testutil/
├── CLAUDE.md                              # 이 파일
├── config/                                # 테스트 설정 베이스 클래스
│   ├── BaseRepositoryTest.java            # Repository 테스트 베이스
│   ├── BaseServiceTest.java               # Service 테스트 베이스  
│   └── BaseWebTest.java                   # Controller 테스트 베이스
├── factory/                               # 테스트 데이터 팩토리
│   ├── domain/                           # 도메인 객체 팩토리
│   │   ├── DomainLabApplicationFactory.java
│   │   ├── DomainLabFactory.java
│   │   ├── DomainLabImageFactory.java
│   │   ├── DomainUserFactory.java
│   │   ├── DomainInterviewFactory.java      # 면접 관련 팩토리
│   │   ├── DomainInterviewSlotFactory.java  # 면접 슬롯 팩토리
│   │   └── DomainScoreSubmissionFactory.java # 점수 신청 팩토리
│   ├── dto/                              # DTO 팩토리 (Controller 테스트용)
│   │   └── DtoFactory.java
│   └── integration/                      # 통합 테스트 팩토리
│       ├── IntegrationLabApplicationFactory.java
│       ├── IntegrationLabFactory.java
│       ├── IntegrationLabImageFactory.java
│       ├── IntegrationUserFactory.java
│       └── IntegrationScoreSubmissionFactory.java # 점수 신청 통합 팩토리
└── mock/                                 # Mock 유틸리티
    ├── AuthMockUtil.java                 # 인증 Mock 헬퍼
    └── QueryMockUtil.java                # 쿼리 Mock 헬퍼
```

## 🏭 Factory 패턴 활용

### 1. Domain Factory 패턴

**목표**: 순수 도메인 객체 생성을 위한 정적 팩토리 메서드

```java
public class DomainUserFactory {

    // 기본 학생 사용자 생성
    public static User createStudent() {
        return User.create(
                "홍길동",
                "student@test.com",
                "password123!",
                Role.STUDENT
        );
    }

    // 특정 역할의 사용자 생성
    public static User createWithRole(Role role) {
        return User.create(
                getNameByRole(role),
                getEmailByRole(role),
                "password123!",
                role
        );
    }

    // 랩장 생성
    public static User createLabLeader() {
        return createWithRole(Role.LAB_LEADER);
    }

    // 교수 생성
    public static User createProfessor() {
        return createWithRole(Role.PROFESSOR);
    }

    // 커스텀 데이터로 사용자 생성
    public static User createWithEmail(String email) {
        return User.create(
                "테스트사용자",
                email,
                "password123!",
                Role.STUDENT
        );
    }

    // ID가 설정된 사용자 생성 (테스트용)
    public static User createWithId(Long id) {
        User user = createStudent();
        user.setId(id);
        return user;
    }

    // 빌더 패턴 활용
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String name = "테스트사용자";
        private String email = "test@example.com";
        private String password = "password123!";
        private Role role = Role.STUDENT;
        private Long id;

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public User build() {
            User user = User.create(name, email, password, role);
            if (id != null) {
                user.setId(id);
            }
            return user;
        }
    }

    // 역할별 기본 정보 매핑
    private static String getNameByRole(Role role) {
        return switch (role) {
            case STUDENT -> "학생";
            case LAB_MEMBER -> "랩멤버";
            case LAB_MANAGER -> "랩관리자";
            case LAB_LEADER -> "랩장";
            case PROFESSOR -> "교수";
            case ADMIN -> "관리자";
        };
    }

    private static String getEmailByRole(Role role) {
        return role.name().toLowerCase() + "@test.com";
    }
}
```

### 2. Domain Lab Factory

```java
public class DomainLabFactory {

    public static Lab createAiLab() {
        return Lab.create(
                "AI랩",
                LabCategory.AI,
                "인공지능 연구실"
        );
    }

    public static Lab createDbLab() {
        return Lab.create(
                "DB랩",
                LabCategory.DB,
                "데이터베이스 연구실"
        );
    }

    public static Lab createWithCategory(LabCategory category) {
        return Lab.create(
                category.getKoreanName() + "랩",
                category,
                category.getKoreanName() + " 연구실"
        );
    }

    public static Lab createWithProfessor(String professorName) {
        Lab lab = createAiLab();
        lab.setProfessorName(professorName);
        return lab;
    }

    // 빌더 패턴
    public static LabBuilder builder() {
        return new LabBuilder();
    }

    public static class LabBuilder {
        private String name = "테스트랩";
        private LabCategory category = LabCategory.COMPUTER_SCIENCE;
        private String description = "테스트용 연구실";
        private String professorName;
        private Integer ranking = 0;
        private Long id;

        public LabBuilder name(String name) {
            this.name = name;
            return this;
        }

        public LabBuilder category(LabCategory category) {
            this.category = category;
            return this;
        }

        public LabBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LabBuilder professorName(String professorName) {
            this.professorName = professorName;
            return this;
        }

        public LabBuilder ranking(Integer ranking) {
            this.ranking = ranking;
            return this;
        }

        public LabBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public Lab build() {
            Lab lab = Lab.create(name, category, description);
            if (professorName != null) {
                lab.setProfessorName(professorName);
            }
            if (ranking != null) {
                lab.setRanking(ranking);
            }
            if (id != null) {
                lab.setId(id);
            }
            return lab;
        }
    }
}
```

### 3. Domain LabApplication Factory

```java
public class DomainLabApplicationFactory {

    public static LabApplication createPending() {
        User user = DomainUserFactory.createStudent();
        Lab lab = DomainLabFactory.createAiLab();
        return createPending(lab, user);
    }

    public static LabApplication createPending(Lab lab, User user) {
        return LabApplication.create(
                lab != null ? lab : DomainLabFactory.createAiLab(),
                user != null ? user : DomainUserFactory.createStudent(),
                LocalDateTime.now().plusDays(1)  // 내일 면접
        );
    }

    public static LabApplication createApproved() {
        LabApplication application = createPending();
        application.approve();
        return application;
    }

    public static LabApplication createRejected() {
        LabApplication application = createPending();
        application.reject();
        return application;
    }

    public static LabApplication createWithStatus(ApplicationStatus status) {
        LabApplication application = createPending();
        switch (status) {
            case APPROVED -> application.approve();
            case REJECTED -> application.reject();
            // PENDING는 기본 상태이므로 변경 없음
        }
        return application;
    }

    public static LabApplication createWithInterviewTime(LocalDateTime interviewTime) {
        User user = DomainUserFactory.createStudent();
        Lab lab = DomainLabFactory.createAiLab();
        return LabApplication.create(lab, user, interviewTime);
    }
}
```

### 4. Domain Interview Factory

```java
public class DomainInterviewFactory {

    // 기본 면접 생성
    public static Interview buildValidInterview() {
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(7);
        return new Interview(lab, startDate, endDate, 60, 5);
    }

    // 특정 랩실과 함께 면접 생성
    public static Interview buildInterviewWithLab(Lab lab) {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(7);
        return new Interview(lab, startDate, endDate, 60, 5);
    }

    // 특정 기간으로 면접 생성
    public static Interview buildInterviewWithDates(LocalDate startDate, LocalDate endDate) {
        Lab lab = DomainLabFactory.buildValidLab();
        return new Interview(lab, startDate, endDate, 60, 5);
    }

    // 활성화된 면접 생성
    public static Interview buildActiveInterview() {
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        Interview interview = new Interview(lab, startDate, endDate, 60, 5);
        ReflectionTestUtils.setField(interview, "status", InterviewStatus.ACTIVE);
        return interview;
    }

    // 비활성화된 면접 생성
    public static Interview buildInactiveInterview() {
        Lab lab = DomainLabFactory.buildValidLab();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        return new Interview(lab, startDate, endDate, 60, 5);
    }

    // ID가 설정된 면접 생성
    public static Interview buildInterviewWithId(Long id) {
        Interview interview = buildValidInterview();
        ReflectionTestUtils.setField(interview, "id", id);
        return interview;
    }
}
```

### 5. Domain InterviewSlot Factory

```java
public class DomainInterviewSlotFactory {

    // 기본 슬롯 생성
    public static InterviewSlot buildValidSlot() {
        Lab lab = DomainLabFactory.buildValidLab();
        Interview interview = DomainInterviewFactory.buildInterviewWithLab(lab);
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, 5);
    }

    // 특정 시간으로 슬롯 생성
    public static InterviewSlot buildSlotWithTime(LocalDateTime startTime) {
        Lab lab = DomainLabFactory.buildValidLab();
        Interview interview = DomainInterviewFactory.buildInterviewWithLab(lab);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, 5);
    }

    // 특정 시간과 랩실로 슬롯 생성
    public static InterviewSlot buildSlotWithTimeAndLab(LocalDateTime startTime, Lab lab) {
        Interview interview = DomainInterviewFactory.buildInterviewWithLab(lab);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, 5);
    }

    // 예약이 있는 슬롯 생성
    public static InterviewSlot buildSlotWithReservations() {
        InterviewSlot slot = buildSlotWithCapacity(5);
        ReflectionTestUtils.setField(slot, "currentApplicants", 2);
        return slot;
    }

    // 특정 용량의 슬롯 생성
    public static InterviewSlot buildSlotWithCapacity(Integer maxApplicants) {
        Interview interview = DomainInterviewFactory.buildValidInterview();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(1);
        return new InterviewSlot(interview, startTime, endTime, maxApplicants);
    }

    // 예약 수를 지정한 슬롯 생성
    public static InterviewSlot buildSlotWithApplicants(Integer currentApplicants, Integer maxApplicants) {
        InterviewSlot slot = buildSlotWithCapacity(maxApplicants);
        ReflectionTestUtils.setField(slot, "currentApplicants", currentApplicants);
        return slot;
    }
}
```

### 6. Domain ScoreSubmission Factory

```java
public class DomainScoreSubmissionFactory {

    // 기본 점수 신청 생성
    public static ScoreSubmission buildValidSubmission() {
        User user = DomainUserFactory.buildValidUserWithId(1L);
        Lab lab = DomainLabFactory.buildValidLabWithId(1L);

        return new ScoreSubmission(
                user, lab, ScoreCategory.RESEARCH_SCI_PAPER,
                "SCI 논문 게재 성과", LocalDate.now().minusDays(30),
                "https://example.com/proof.pdf", "연구 성과 인정 신청",
                "https://example.com/paper-link", VisibilityLevel.PUBLIC
        );
    }

    // 특정 사용자와 랩실로 점수 신청 생성
    public static ScoreSubmission buildSubmissionWithUserAndLab(User user, Lab lab) {
        return new ScoreSubmission(
                user, lab, ScoreCategory.CERTIFICATION_NATIONAL,
                "국가 자격증 취득", LocalDate.now().minusDays(20),
                "https://example.com/cert.pdf", "자격증 취득 인정 신청",
                null, VisibilityLevel.LAB_ONLY
        );
    }

    // 승인된 점수 신청 생성
    public static ScoreSubmission buildApprovedSubmission() {
        ScoreSubmission submission = buildValidSubmission();
        ReflectionTestUtils.setField(submission, "status", SubmissionStatus.APPROVED);
        ReflectionTestUtils.setField(submission, "approvedBy", 999L);
        ReflectionTestUtils.setField(submission, "approvedAt", LocalDateTime.now());
        return submission;
    }

    // 거절된 점수 신청 생성
    public static ScoreSubmission buildRejectedSubmission() {
        ScoreSubmission submission = buildValidSubmission();
        ReflectionTestUtils.setField(submission, "status", SubmissionStatus.REJECTED);
        ReflectionTestUtils.setField(submission, "approvedBy", 999L);
        ReflectionTestUtils.setField(submission, "approvedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(submission, "rejectionReason", "증빙서류 부족");
        return submission;
    }

    // 만료된 점수 신청 생성
    public static ScoreSubmission buildExpiredSubmission() {
        ScoreSubmission submission = buildValidSubmission();
        ReflectionTestUtils.setField(submission, "expiresAt", LocalDateTime.now().minusDays(1));
        return submission;
    }

    // 빌더 패턴 지원
    public static ScoreSubmissionBuilder builder() {
        return new ScoreSubmissionBuilder();
    }

    public static class ScoreSubmissionBuilder {
        private User user = DomainUserFactory.buildValidUserWithId(10L);
        private Lab lab = DomainLabFactory.buildValidLabWithId(10L);
        private ScoreCategory category = ScoreCategory.ACADEMIC_ACHIEVEMENT;
        private String achievementDescription = "기본 성과 내용";
        private LocalDate achievementDate = LocalDate.now().minusDays(30);
        private String proofFileUrl = "https://example.com/proof.pdf";
        private String applicationReason = "점수 신청";
        private String relatedLink = null;
        private VisibilityLevel visibility = VisibilityLevel.PUBLIC;

        public ScoreSubmissionBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ScoreSubmissionBuilder lab(Lab lab) {
            this.lab = lab;
            return this;
        }

        public ScoreSubmissionBuilder category(ScoreCategory category) {
            this.category = category;
            return this;
        }

        public ScoreSubmissionBuilder achievementDescription(String description) {
            this.achievementDescription = description;
            return this;
        }

        public ScoreSubmissionBuilder achievementDate(LocalDate date) {
            this.achievementDate = date;
            return this;
        }

        public ScoreSubmissionBuilder visibility(VisibilityLevel visibility) {
            this.visibility = visibility;
            return this;
        }

        public ScoreSubmission build() {
            return new ScoreSubmission(
                    user, lab, category, achievementDescription, achievementDate,
                    proofFileUrl, applicationReason, relatedLink, visibility
            );
        }
    }
}
```

## 🔧 Integration Factory 패턴

### 실제 데이터베이스와 연동하는 통합 테스트용 팩토리

```java

@Component
public class IntegrationUserFactory {

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createAndSaveStudent() {
        User user = DomainUserFactory.createStudent();
        return userRepositoryPort.save(user);
    }

    public User createAndSaveWithRole(Role role) {
        User user = DomainUserFactory.createWithRole(role);
        return userRepositoryPort.save(user);
    }

    public User createAndSaveWithEmail(String email) {
        User user = DomainUserFactory.createWithEmail(email);
        return userRepositoryPort.save(user);
    }

    public List<User> createAndSaveMultipleStudents(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = DomainUserFactory.builder()
                    .name("학생" + i)
                    .email("student" + i + "@test.com")
                    .build();
            users.add(userRepositoryPort.save(user));
        }
        return users;
    }

    public User createAndSaveLabMember(Lab lab) {
        User user = DomainUserFactory.createWithRole(Role.LAB_MEMBER);
        user.assignLab(lab);
        return userRepositoryPort.save(user);
    }
}
```

```java

@Component
public class IntegrationLabFactory {

    @Autowired
    private LabRepositoryPort labRepositoryPort;

    @Autowired
    private IntegrationUserFactory userFactory;

    public Lab createAndSaveAiLab() {
        Lab lab = DomainLabFactory.createAiLab();
        return labRepositoryPort.save(lab);
    }

    public Lab createAndSaveWithProfessor() {
        Lab lab = DomainLabFactory.createAiLab();
        User professor = userFactory.createAndSaveWithRole(Role.PROFESSOR);
        lab.autoAssignProfessorIfMatches(professor);
        return labRepositoryPort.save(lab);
    }

    public Lab createLabWithMembers(int memberCount) {
        Lab lab = createAndSaveAiLab();

        for (int i = 0; i < memberCount; i++) {
            userFactory.createAndSaveLabMember(lab);
        }

        return lab;
    }
}
```

## 📦 DTO Factory 패턴

### 목표

Controller 테스트에서 사용하는 Request/Response DTO 객체를 일관되게 생성하기 위한 팩토리

### 특징

- **타입 안전성**: Map 대신 강타입 DTO 사용
- **재사용성**: Controller 테스트 간 공통 활용
- **실제 구조 반영**: 실제 DTO 클래스 구조에 정확히 맞춤

### 기본 구조

```java
public final class DtoFactory {
    private DtoFactory() {
    }

    // User Request DTOs
    public static UserRegisterRequestDto buildUserRegisterRequest() {
        return UserRegisterRequestDto.builder()
                .name("테스트사용자")
                .email("test@example.com")
                .password("Password!123")
                .build();
    }

    public static UserRegisterRequestDto buildUserRegisterRequest(String name, String email, String password) {
        return UserRegisterRequestDto.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();
    }

    // Record 타입 DTO (생성자 방식)
    public static LabApplicationRequestDto buildLabApplicationRequest() {
        return new LabApplicationRequestDto(
                LocalDateTime.now().plusDays(1)
        );
    }

    // Response DTOs (실제 DTO 구조에 맞게)
    public static UserResponseDto buildUserResponseDto() {
        return UserResponseDto.builder()
                .id(1L)
                .name("테스트사용자")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static AuthResponseDto buildAuthResponseDto() {
        return AuthResponseDto.builder()
                .token("mock-jwt-token")
                .user(buildUserResponseDto())
                .build();
    }
}
```

### 활용 예시

```java

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Test
    void 회원가입_성공() throws Exception {
        // given - DTO Factory 활용
        UserRegisterRequestDto request = DtoFactory.buildUserRegisterRequest("홍길동", "new@example.com", "password123");
        String json = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }
}
```

### 주요 장점

1. **타입 안전성**: 컴파일 타임에 오류 발견
2. **재사용성**: 여러 테스트에서 동일한 DTO 생성 로직 활용
3. **유지보수성**: DTO 구조 변경시 중앙화된 관리
4. **가독성**: Map 기반 JSON 대신 명확한 의도 표현

### 주의사항

- **실제 DTO 구조와 일치**: 실제 DTO 클래스의 필드와 메서드에 정확히 맞춰야 함
- **Record 타입 고려**: Record 타입 DTO는 생성자 방식 사용
- **Builder 패턴 확인**: 모든 DTO가 Builder를 지원하는 것은 아님

## 🎭 Mock 유틸리티

### 1. AuthMockUtil - 인증 관련 Mock 헬퍼

```java
public class AuthMockUtil {

    public static Authentication createMockAuthentication(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    public static Authentication createStudentAuthentication() {
        User student = DomainUserFactory.createStudent();
        return createMockAuthentication(student);
    }

    public static Authentication createLabLeaderAuthentication(Lab lab) {
        User labLeader = DomainUserFactory.createLabLeader();
        labLeader.assignLab(lab);
        return createMockAuthentication(labLeader);
    }

    public static Authentication createProfessorAuthentication() {
        User professor = DomainUserFactory.createProfessor();
        return createMockAuthentication(professor);
    }

    public static Authentication createAdminAuthentication() {
        User admin = DomainUserFactory.createWithRole(Role.ADMIN);
        return createMockAuthentication(admin);
    }

    // SecurityContext에 인증 정보 설정
    public static void setSecurityContext(User user) {
        Authentication authentication = createMockAuthentication(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void setSecurityContextAsStudent() {
        setSecurityContext(DomainUserFactory.createStudent());
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // JWT 토큰 관련 Mock 설정
    public static void mockJwtToken(JwtTokenProvider jwtTokenProvider, User user) {
        String token = "mock.jwt.token";
        when(jwtTokenProvider.createToken(user.getEmail(), user.getRole()))
                .thenReturn(token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getEmail(token)).thenReturn(user.getEmail());
    }
}
```

### 2. QueryMockUtil - 쿼리 관련 Mock 헬퍼

```java
public class QueryMockUtil {

    // Repository Mock 설정 헬퍼
    public static void mockUserRepositoryFindById(UserRepositoryPort repository, User user) {
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    public static void mockUserRepositoryFindByEmail(UserRepositoryPort repository, User user) {
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    public static void mockUserRepositoryNotFound(UserRepositoryPort repository, Long userId) {
        when(repository.findById(userId)).thenReturn(Optional.empty());
    }

    public static void mockLabRepositoryFindById(LabRepositoryPort repository, Lab lab) {
        when(repository.findById(lab.getId())).thenReturn(Optional.of(lab));
    }

    public static void mockLabApplicationRepository(
            LabApplicationRepositoryPort repository,
            LabApplication application) {
        when(repository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(repository.save(any(LabApplication.class)))
                .thenReturn(application);
    }

    // 페이징 쿼리 Mock
    public static <T> Page<T> mockPage(List<T> content, Pageable pageable) {
        return new PageImpl<>(content, pageable, content.size());
    }

    public static void mockUserRepositoryFindAll(UserRepositoryPort repository,
                                                 List<User> users,
                                                 Pageable pageable) {
        Page<User> page = mockPage(users, pageable);
        when(repository.findAll(pageable)).thenReturn(page);
    }

    // 존재 여부 검증 Mock
    public static void mockExistsByEmail(UserRepositoryPort repository,
                                         String email,
                                         boolean exists) {
        when(repository.existsByEmail(email)).thenReturn(exists);
    }

    public static void mockDuplicateApplication(LabApplicationRepositoryPort repository,
                                                Long labId,
                                                Long userId,
                                                boolean exists) {
        when(repository.existsByLabIdAndUserId(labId, userId)).thenReturn(exists);
    }
}
```

## 🏗️ Base 테스트 클래스

### 1. BaseRepositoryTest

```java

@DataJpaTest
@Import({TestDataConfig.class, IntegrationUserFactory.class, IntegrationLabFactory.class})
public abstract class BaseRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;

    @Autowired
    protected IntegrationUserFactory userFactory;

    @Autowired
    protected IntegrationLabFactory labFactory;

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected <T> T persistAndFlush(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    protected <T> T merge(T entity) {
        return entityManager.merge(entity);
    }

    // 공통 데이터 생성 헬퍼
    protected User createPersistedUser() {
        return userFactory.createAndSaveStudent();
    }

    protected Lab createPersistedLab() {
        return labFactory.createAndSaveAiLab();
    }
}
```

### 2. BaseServiceTest

```java

@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    protected void verifyNoMoreInteractions(Object... mocks) {
        Mockito.verifyNoMoreInteractions(mocks);
    }

    protected <T> ArgumentCaptor<T> captor(Class<T> clazz) {
        return ArgumentCaptor.forClass(clazz);
    }

    // 공통 Mock 설정
    protected void setupMockSecurityContext(User user) {
        AuthMockUtil.setSecurityContext(user);
    }

    @AfterEach
    void tearDown() {
        AuthMockUtil.clearSecurityContext();
    }

    // 공통 검증 메서드
    protected void assertUserResponseDto(UserResponseDto response, User user) {
        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getName()).isEqualTo(user.getName());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getRole()).isEqualTo(user.getRole());
    }

    protected void assertLabResponseDto(LabResponseDto response, Lab lab) {
        assertThat(response.getId()).isEqualTo(lab.getId());
        assertThat(response.getName()).isEqualTo(lab.getName());
        assertThat(response.getCategory()).isEqualTo(lab.getCategory());
        assertThat(response.getDescription()).isEqualTo(lab.getDescription());
    }
}
```

### 3. BaseWebTest

```java

@WebMvcTest
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
public abstract class BaseWebTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // 공통 테스트 헬퍼
    protected ResultActions performGet(String url, Object... params) throws Exception {
        return mockMvc.perform(get(url, params));
    }

    protected ResultActions performPost(String url, Object requestBody) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    protected ResultActions performPut(String url, Object requestBody) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    protected ResultActions performDelete(String url, Object... params) throws Exception {
        return mockMvc.perform(delete(url, params));
    }

    // 인증된 요청 헬퍼
    protected ResultActions performAuthenticatedGet(String url, User user) throws Exception {
        return mockMvc.perform(get(url)
                .with(authentication(AuthMockUtil.createMockAuthentication(user))));
    }

    protected ResultActions performAuthenticatedPost(String url, Object requestBody, User user)
            throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .with(authentication(AuthMockUtil.createMockAuthentication(user))));
    }

    // 공통 검증 메서드
    protected void assertSuccessResponse(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    protected void assertErrorResponse(ResultActions result, int expectedStatus, String expectedCode)
            throws Exception {
        result.andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.code").value(expectedCode));
    }
}
```

## 🏗️ 테스트 설계 원칙

### Factory 책임 분리

| Factory 타입          | 책임 범위      | 상태 관리   | 의존성       |
|---------------------|------------|---------|-----------|
| Domain Factory      | 비즈니스 객체 생성 | 상태 무관   | 외부 의존 금지  |
| Integration Factory | DB 연동 객체   | 영속성 관리  | Spring 의존 |
| DTO Factory         | API 계약 객체  | 프로토콜 준수 | 구조 일치성    |

### 테스트 격리 전략

```java
// ✅ 상태 독립적 객체 생성
public static Entity createValid() {
    return new Entity(defaultValues); // 외부 상태 비의존
}

// ✅ 필요시에만 테스트별 상태 조정
@Test
void 특정_상태_검증() {
    Entity entity = createValid();
    setTestSpecificState(entity); // 테스트에서만 조정
}
```

## 🎯 TestUtil 활용 베스트 프랙티스

### 1. Factory 사용 원칙

#### Factory 타입별 활용 가이드

| Factory 타입              | 사용 목적          | 특징                | 예시                                              |
|-------------------------|----------------|-------------------|-------------------------------------------------|
| **Domain Factory**      | 순수 객체 생성       | 외부 의존성 없음, 정적 메서드 | `DomainUserFactory.buildStudentUser()`          |
| **Integration Factory** | 실제 DB 연동       | @Component, 실제 저장 | `IntegrationUserFactory.createAndSaveStudent()` |
| **DTO Factory**         | Controller 테스트 | HTTP 요청/응답 DTO    | `DtoFactory.buildUserRegisterRequest()`         |

#### 핵심 원칙

- **도메인 Factory**: 순수 객체 생성, 외부 의존성 없음, **상태 중립성 유지**
- **Integration Factory**: 실제 DB 연동, @Component로 Spring 관리
- **DTO Factory**: 실제 DTO 구조와 정확히 일치, 타입 안전성 확보
- **빌더 패턴**: 복잡한 객체 생성 시 가독성 향상
- **메서드 체이닝**: 유연한 테스트 데이터 생성

### 2. Mock 활용 지침

```java
// 좋은 예: 명확한 Mock 설정
@Test
void 사용자_조회_성공() {
    // given
    User user = DomainUserFactory.createStudent();
    QueryMockUtil.mockUserRepositoryFindById(userRepository, user);

    // when & then
    // 테스트 로직
}

// 나쁜 예: 복잡한 Mock 설정을 테스트마다 반복
@Test
void 사용자_조회_성공() {
    // given - 매번 동일한 Mock 설정 반복
    User user = User.create(...);
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenReturn(user);
    // ...
}
```

### 3. DTO Factory 활용 패턴

```java
// ✅ 권장: DTO Factory 활용
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Test
    void 사용자_등록_성공() throws Exception {
        // given - DTO Factory 활용
        UserRegisterRequestDto request = DtoFactory.buildUserRegisterRequest("홍길동", "hong@test.com", "password123");
        String json = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }
}

// ❌ 비권장: Map 기반 JSON 생성
@Test
void 사용자_등록_실패() throws Exception {
    // given - Map 사용 (타입 안전성 부족)
    Map<String, Object> request = Map.of(
            "name", "홍길동",
            "email", "invalid-email", // 오타 가능성
            "password", "123"
    );
    String json = objectMapper.writeValueAsString(request);
    // ...
}

// ✅ Record 타입 DTO 활용
@Test
void 랩실_지원_성공() throws Exception {
    // given - Record 타입은 생성자 방식
    LabApplicationRequestDto request = DtoFactory.buildLabApplicationRequest(LocalDateTime.now().plusDays(1));

    // when & then
    // 테스트 로직
}
```

### 4. Base 클래스 상속 활용

```java
// Repository 테스트
class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SpringDataUserRepository userRepository;

    @Test
    void 이메일로_사용자_조회() {
        // given
        User user = createPersistedUser();  // Base 클래스 헬퍼 활용

        // when & then
        Optional<User> found = userRepository.findByEmail(user.getEmail());
        assertThat(found).isPresent();
    }
}
```

### 4. 테스트 데이터 관리

```java
// 테스트 클래스별 공통 데이터
class UserCommandServiceTest extends BaseServiceTest {

    private User testUser;
    private UserCreateRequestDto validRequest;

    @BeforeEach
    void setUpTestData() {
        testUser = DomainUserFactory.createStudent();
        validRequest = UserCreateRequestDto.builder()
                .name(testUser.getName())
                .email(testUser.getEmail())
                .password("password123!")
                .role(testUser.getRole())
                .build();
    }

    @Test
    void 유효한_데이터로_사용자_생성() {
        // testUser, validRequest 활용
    }
}
```

## 📊 TestUtil 품질 지표

### 재사용성 메트릭

- **Factory 메서드 사용률**: 90% 이상
- **중복 코드 감소**: 50% 이상
- **테스트 작성 시간**: 30% 단축

### 유지보수성 지표

- **테스트 데이터 변경**: 중앙화된 Factory에서만 수정
- **Mock 설정 표준화**: 공통 패턴 적용
- **Base 클래스 활용도**: 80% 이상

## 🚨 실수 방지 체크리스트 (AI 필독)

### 📋 테스트 작성 전 사전 검증 단계

#### 1️⃣ Integration Factory 메서드 존재성 확인

```bash
# 실제 메서드명 확인 방법
grep -n "public static" IntegrationUserFactory.java
grep -n "public static" IntegrationLabFactory.java
```

**자주 발생하는 실수와 해결책:**

- ❌ `createAndSaveStudent()` → ✅ `createAndSaveStudent(UserRepositoryPort repo)`
- ❌ `createAndSaveAiLab()` → ✅ `createAndSaveAiLab(LabRepositoryPort repo)`
- ❌ `createAndSaveWithRole(Role.STUDENT)` → ✅ `createAndSaveWithRole(repo, Role.STUDENT)`

#### 2️⃣ Repository 파라미터 전달 패턴 준수

```java
// ✅ 올바른 패턴
User user = userFactory.createAndSaveStudent(userRepositoryPort);
Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);

// ❌ 잘못된 패턴 (Repository 파라미터 누락)
User user = userFactory.createAndSaveStudent();
Lab lab = labFactory.createAndSaveAiLab();
```

#### 3️⃣ Domain 메서드명 정확성 검증

```java
// ✅ 올바른 메서드명
user.assignLab(lab);        // NOT user.assignToLab(lab)
user.

changeRole(Role.ADMIN); // NOT user.setRole(Role.ADMIN)

// 확인 방법: Domain 클래스 직접 검사
grep -n "public void"User.java
```

#### 4️⃣ Enum 값 존재성 확인

```java
// ✅ 올바른 Enum 값
LabCategory.AI          // NOT LabCategory.ARTIFICIAL_INTELLIGENCE
LabCategory.DB          // NOT LabCategory.DATABASE
LabCategory.SECURITY    // NOT LabCategory.INFORMATION_SECURITY

// 확인 방법: Enum 클래스 직접 검사
grep -A 20"public enum"LabCategory.java
```

### ⚡ AI 효율성 매트릭스

#### Integration Factory 메서드 시그니처 테이블

| Factory 클래스            | 메서드명                       | 시그니처                                   | 용도           |
|------------------------|----------------------------|----------------------------------------|--------------|
| IntegrationUserFactory | `createAndSaveStudent`     | `(UserRepositoryPort repo)`            | 학생 사용자 생성    |
| IntegrationUserFactory | `createAndSaveWithRole`    | `(UserRepositoryPort repo, Role role)` | 특정 역할 사용자 생성 |
| IntegrationUserFactory | `createAndSaveLabMember`   | `(UserRepositoryPort repo, Lab lab)`   | 랩 멤버 생성      |
| IntegrationLabFactory  | `createAndSaveAiLab`       | `(LabRepositoryPort repo)`             | AI 랩 생성      |
| IntegrationLabFactory  | `createAndSaveDbLab`       | `(LabRepositoryPort repo)`             | DB 랩 생성      |
| IntegrationLabFactory  | `createAndSaveSecurityLab` | `(LabRepositoryPort repo)`             | 보안 랩 생성      |

#### 의존성 주입 패턴 가이드

```java
// ✅ Integration Factory 의존성 주입 패턴
@Component
public class IntegrationScoreSubmissionFactory {

    @Autowired
    private ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;
    @Autowired
    private UserRepositoryPort userRepositoryPort;
    @Autowired
    private LabRepositoryPort labRepositoryPort;

    // 메서드에서 Repository 파라미터 전달 필수
    public ScoreSubmission createAndSaveSubmission() {
        User user = userFactory.createAndSaveStudent(userRepositoryPort);
        Lab lab = labFactory.createAndSaveAiLab(labRepositoryPort);
        // ...
    }
}
```

#### 테스트 어노테이션 조합 가이드

```java
// ✅ Controller 테스트 (Spring Boot 3.x 호환)
@WebMvcTest(ControllerClass.class)
@ExtendWith(MockitoExtension.class)
class ControllerTest {
    @MockitoBean  // NOT @MockBean (deprecated)
    private UseCase useCase;
}

// ✅ Service 테스트
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // 필수
class ServiceTest {
    @Mock
    private RepositoryPort repositoryPort;
}

// ✅ Repository 테스트
@DataJpaTest
@Import({TestDataConfig.class, IntegrationUserFactory.class})
class RepositoryTest extends BaseRepositoryTest {
    // ...
}
```

### 🛠️ 일반적인 실수 패턴과 해결책

#### 실수 1: Factory 메서드 누락 파라미터

```java
// ❌ 실수
User user = userFactory.createAndSaveStudent();

// ✅ 해결
User user = userFactory.createAndSaveStudent(userRepositoryPort);
```

#### 실수 2: Domain 메서드명 오타

```java
// ❌ 실수
user.assignToLab(lab);

// ✅ 해결 (실제 메서드명 확인)
user.

assignLab(lab);
```

#### 실수 3: Enum 값 부정확

```java
// ❌ 실수
LabCategory.DATABASE

// ✅ 해결 (실제 Enum 값 확인)
LabCategory.DB
```

#### 실수 4: Spring Boot 3.x 비호환 어노테이션

```java
// ❌ 실수 (deprecated)
@MockBean
private UseCase useCase;

// ✅ 해결
@MockitoBean
private UseCase useCase;
```

### 🔍 사전 검증 명령어

```bash
# 1. Factory 메서드 존재 확인
grep -r "createAndSave" src/test/java/org/univ/rankus/testutil/factory/integration/

# 2. Domain 메서드 존재 확인
grep -r "public void\|public boolean" src/main/java/org/univ/rankus/domain/model/

# 3. Enum 값 확인
find src/main/java -name "*.java" -exec grep -l "public enum" {} \;
```

## 🔥 중요: ID 충돌 문제 예방 가이드 (AI 필독)

### ⚠️ 문제 요약

- **증상**: EntityExistsException, OptimisticLockingFailureException
- **원인**: Domain Factory에서 고정 ID 설정
- **결과**: 57개 테스트 실패

### 🎯 3대 핵심 원칙

#### 1️⃣ Domain Factory: ID 설정 절대 금지

```java
// ✅ 올바른 방법
public static Lab buildValidLab() {
    return new Lab("TestLab", LabCategory.AI, "Description", "ProfX");
}

// ❌ 금지된 방법
public static Lab buildValidLab() {
    Lab lab = new Lab("TestLab", LabCategory.AI, "Description", "ProfX");
    ReflectionTestUtils.setField(lab, "id", 1L); // 절대 금지
    return lab;
}
```

#### 2️⃣ Integration Factory: 자동 ID 생성만 허용

```java
// ✅ 올바른 방법
public static Lab persistValidLab(TestEntityManager em) {
    Lab lab = DomainLabFactory.buildValidLab(); // Domain Factory 활용
    em.persist(lab);  // DB가 자동 ID 생성
    em.flush();
    return lab;
}
```

#### 3️⃣ 테스트 격리: 독립적 데이터 사용

```yaml
# src/test/resources/application.yml
spring:
  sql:
    init:
      mode: never  # data.sql 비활성화
```

### 📋 체크리스트 (새 Factory 작성시)

- [ ] Domain Factory에 `ReflectionTestUtils.setField(*, "id", *)` 없음
- [ ] Integration Factory는 `em.persist()` 또는 `repo.save()` 사용
- [ ] 테스트 실행시 EntityExistsException 미발생
- [ ] 각 테스트는 독립적 데이터 생성

### 🚨 금지 패턴

```java
// ❌ 이런 코드 발견시 즉시 수정 필요
ReflectionTestUtils.setField(entity, "id",1L);
ReflectionTestUtils.

setField(entity, "id",Math.abs(uid.hashCode())%10000L+1L);
```

## 🔗 관련 가이드

- **테스트 전략**: `@test/CLAUDE.md`
- **Domain 테스트**: `@domain/CLAUDE.md`
- **Application 테스트**: `@application/CLAUDE.md`
- **Adapter 테스트**: `@adapter/CLAUDE.md`