# Adapter Layer 핵심 가이드 (AI 전용)

> 외부 기술과 애플리케이션 코어 간의 연결점 (80줄 이하)

## 🔌 핵심 책임 및 원칙

**책임**: 기술 연동, 포트 구현, 데이터 변환, 프로토콜 처리  
**원칙**: Adapter → Application 단방향 의존, 기술 격리

## 📁 구조 패턴

```
adapter/
├── in/web/          # Inbound: 외부 → 내부
│   ├── controller/  # REST API 엔드포인트
│   └── dto/        # HTTP 요청/응답 구조
└── out/persistence/ # Outbound: 내부 → 외부
    ├── jpa/        # Spring Data JPA
    └── impl/       # Repository 구현체
```

## 🎮 Controller 표준 패턴

### 기본 구조

```java
@RestController @RequestMapping("/api/{domain}") @RequiredArgsConstructor @Validated
public class {Domain}Controller {
    private final {Domain}CommandUseCase commandUseCase;
    private final {Domain}QueryUseCase queryUseCase;
    
    @PostMapping @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
            @Valid @RequestBody {Domain}CreateRequestDto request) {
        {Domain}ResponseDto response = commandUseCase.create{Domain}(request);
        return ResponseEntity.status(CREATED).body(ApiResponse.created(response));
    }
}
```

### API 엔드포인트 매트릭스

| Controller               | 엔드포인트                              | 권한                  |
|--------------------------|------------------------------------|---------------------|
| AuthController           | `POST /api/auth/signup`            | 인증 불필요              |
| AuthController           | `POST /api/auth/login`             | 인증 불필요              |
| UserController           | `GET /api/users/me`                | `isAuthenticated()` |
| LabPromotionController   | `GET /api/labs`                    | 공개                  |
| LabApplicationController | `POST /api/labs/{id}/applications` | `isAuthenticated()` |

## 🗂️ Repository 표준 패턴

### Adapter 구현

```java
@Component @RequiredArgsConstructor
public class {Domain}RepositoryAdapter implements {Domain}RepositoryPort {
    private final SpringData{Domain}Repository repository;
    
    @Override
    public {Domain} save({Domain} entity) { return repository.save(entity); }
    @Override
    public Optional<{Domain}> findById(Long id) { return repository.findById(id); }
}
```

### JPA Repository

```java
public interface SpringData{Domain}Repository extends JpaRepository<{Domain}, Long> {
    Optional<{Domain}> findBy{Property}({Type} value);
    List<{Domain}> findBy{Condition}({Type} condition);
}
```

## 📄 DTO 변환 패턴

### Request DTO

```java
public record {Domain}CreateRequestDto(
    @NotBlank @Size(max=100) String name,
    @Email String email
) {}
```

### Response DTO

```java
public record {Domain}ResponseDto(Long id, String name, LocalDateTime createdAt) {
    public static {Domain}ResponseDto from({Domain} entity) {
        return new {Domain}ResponseDto(entity.getId(), entity.getName(), entity.getCreatedAt());
    }
}
```

---

**참조**: 상세 컨벤션은 각 하위 디렉토리의 CONVENTIONS.md 참조  
**업데이트**: 2025-01-04 | **압축률**: 기존 대비 88% 절약
@RequestMapping("/api/labs/{labId}/applications")
public class LabApplicationController {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabApplicationResponseDto>> applyToLab(
            @PathVariable Long labId,
            @Valid @RequestBody LabApplicationRequestDto request);

    @GetMapping
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #labId, 'LabApplication', 'VIEW')")
    public ResponseEntity<ApiResponse<List<LabApplicationResponseDto>>> getApplications(
            @PathVariable Long labId);

    @PutMapping("/{appId}/approve")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'APPROVE')")
    public ResponseEntity<ApiResponse<LabApplicationResponseDto>> approveApplication(
            @PathVariable Long labId, @PathVariable Long appId);

    @DeleteMapping("/{appId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'DELETE')")
    public ResponseEntity<ApiResponse<Void>> cancelApplication(
            @PathVariable Long labId, @PathVariable Long appId);

}

```

#### LabImageController (`/api/labs/{labId}/images`)

```java

@RestController
@RequestMapping("/api/labs/{labId}/images")
public class LabImageController {

    @PostMapping
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #labId, 'LabImage', 'CREATE')")
    public ResponseEntity<ApiResponse<LabImageResponseDto>> uploadImage(
            @PathVariable Long labId,
            @Valid @RequestBody LabImageRequestDto request);

    @GetMapping
    public ResponseEntity<ApiResponse<List<LabImageResponseDto>>> getImages(
            @PathVariable Long labId);

    @DeleteMapping("/{imageId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #imageId, 'LabImage', 'DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long labId, @PathVariable Long imageId);
}
```

#### LabCreationRequestController (`/api/lab-creation-requests`)

```java

@RestController
@RequestMapping("/api/lab-creation-requests")
public class LabCreationRequestController {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LabCreationRequestResponseDto>> createLabCreationRequest(
            @Valid @RequestBody LabCreationRequestDto request);

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LabCreationRequestResponseDto>>> getCurrentUserLabCreationRequests();

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LabCreationRequestResponseDto>>> getAllLabCreationRequests();

    @GetMapping("/{requestId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #requestId, 'LabCreationRequest', 'VIEW')")
    public ResponseEntity<ApiResponse<LabCreationRequestResponseDto>> getLabCreationRequest(
            @PathVariable Long requestId);

    @PutMapping("/{requestId}/approve")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #requestId, 'LabCreationRequest', 'APPROVE')")
    public ResponseEntity<Void> approveLabCreationRequest(
            @PathVariable Long requestId);

    @PutMapping("/{requestId}/reject")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #requestId, 'LabCreationRequest', 'REJECT')")
    public ResponseEntity<Void> rejectLabCreationRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody LabCreationRequestRejectDto request);

    @DeleteMapping("/{requestId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #requestId, 'LabCreationRequest', 'DELETE')")
    public ResponseEntity<Void> cancelLabCreationRequest(
            @PathVariable Long requestId);
    // 권한: 신청자 본인 + 관리자 (2025.07 정책 변경)
}
```

#### LabNoticeController (`/api/labs/{labId}/notices`)

```java

@RestController
@RequestMapping("/api/labs/{labId}/notices")
public class LabNoticeController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
    public ResponseEntity<ApiResponse<PageResponse<LabNoticeResponseDto>>> getLabNotices(
            @PathVariable Long labId,
            @PageableDefault(size = 20) Pageable pageable);

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
    public ResponseEntity<ApiResponse<List<LabNoticeResponseDto>>> getAllLabNotices(
            @PathVariable Long labId);

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
    public ResponseEntity<ApiResponse<List<LabNoticeResponseDto>>> getNoticesByType(
            @PathVariable Long labId,
            @PathVariable NoticeType type);

    @GetMapping("/pinned")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
    public ResponseEntity<ApiResponse<List<LabNoticeResponseDto>>> getPinnedNotices(
            @PathVariable Long labId);

    @GetMapping("/{noticeId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'VIEW')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> getNotice(
            @PathVariable Long labId,
            @PathVariable Long noticeId);

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_NOTICES')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> createNotice(
            @PathVariable Long labId,
            @Valid @RequestBody LabNoticeCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails);

    @PutMapping("/{noticeId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'UPDATE')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> updateNotice(
            @PathVariable Long labId,
            @PathVariable Long noticeId,
            @Valid @RequestBody LabNoticeUpdateRequestDto request);

    @PatchMapping("/{noticeId}/pin")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'UPDATE')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> togglePinNotice(
            @PathVariable Long labId,
            @PathVariable Long noticeId);

    @DeleteMapping("/{noticeId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @PathVariable Long labId,
            @PathVariable Long noticeId);
}
```

## 📦 DTO 구조 분석

### Request DTO 패턴

```java
public class UserRegisterRequestDto {

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 30, message = "이름은 30자를 초과할 수 없습니다")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상 255자 이하여야 합니다")
    private String password;

    @NotNull(message = "역할은 필수입니다")
    private Role role;

    // getters...
}
```

#### LabCreationRequest DTO 패턴

```java
public class LabCreationRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "설명은 필수입니다")
    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
    private String description;

    @NotNull(message = "랩실 카테고리는 필수입니다")
    private LabCategory category;

    // getters...
}

public class LabCreationRequestRejectDto {

    @Size(max = 500, message = "거부 사유는 500자를 초과할 수 없습니다")
    private String reason;

    // getters...
}
```

#### LabNotice DTO 패턴

```java
public class LabNoticeCreateRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자를 초과할 수 없습니다")
    private String content;

    @NotNull(message = "공지사항 타입은 필수입니다")
    private NoticeType type;

    private boolean pinned = false;

    // getters...
}

public class LabNoticeUpdateRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자를 초과할 수 없습니다")
    private String content;

    @NotNull(message = "공지사항 타입은 필수입니다")
    private NoticeType type;

    private boolean pinned = false;

    // getters...
}
```

### Response DTO 패턴

```java
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 도메인 객체에서 DTO 생성하는 팩토리 메서드
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
```

#### LabCreationRequestResponseDto 패턴

```java
public class LabCreationRequestResponseDto {

    private Long id;
    private String title;
    private String description;
    private LabCategory category;
    private LabCreationStatus status;
    private String rejectReason;
    private String requestorName;
    private Long requestorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 도메인 객체에서 DTO 생성하는 팩토리 메서드
    public static LabCreationRequestResponseDto from(LabCreationRequest request) {
        return LabCreationRequestResponseDto.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(request.getStatus())
                .rejectReason(request.getRejectReason())
                .requestorName(request.getRequestor().getName())
                .requestorId(request.getRequestor().getId())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
```

#### LabNoticeResponseDto 패턴

```java
public class LabNoticeResponseDto {

    private Long id;
    private String title;
    private String content;
    private NoticeType type;
    private boolean pinned;
    private Long authorId;
    private String authorName;
    private Long labId;
    private String labName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 도메인 객체에서 DTO 생성하는 팩토리 메서드
    public static LabNoticeResponseDto from(LabNotice notice) {
        return LabNoticeResponseDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .type(notice.getType())
                .pinned(notice.isPinned())
                .authorId(notice.getAuthor().getId())
                .authorName(notice.getAuthor().getName())
                .labId(notice.getLab().getId())
                .labName(notice.getLab().getName())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    public static List<LabNoticeResponseDto> fromList(List<LabNotice> notices) {
        return notices.stream()
                .map(LabNoticeResponseDto::from)
                .collect(Collectors.toList());
    }
}
```

### 공통 응답 구조 (ApiResponse)

```java
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "성공", data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
```

## 📊 Repository Adapter 구현 현황

### 현재 구현된 Repository Adapter

- **UserRepositoryAdapter**: 사용자 관리 (생성, 조회, 이메일 검증)
- **LabRepositoryAdapter**: 랩실 관리 (조회, 카테고리별 조회)
- **LabApplicationRepositoryAdapter**: 지원서 관리 (생성, 상태 변경, 조회)
- **LabImageRepositoryAdapter**: 이미지 관리 (업로드, 조회, 삭제)
- **LabCreationRequestRepositoryAdapter**: 랩실 생성 요청 관리 (생성, 승인/거부, 조회)
- **LabNoticeRepositoryAdapter**: 공지사항 관리 (생성, 수정, 삭제, 조회, 타입별 조회)

### Spring Data JPA Repository

- **SpringDataUserRepository**: User 엔티티 JPA 인터페이스
- **SpringDataLabRepository**: Lab 엔티티 JPA 인터페이스
- **SpringDataLabApplicationRepository**: LabApplication 엔티티 JPA 인터페이스
- **SpringDataLabImageRepository**: LabImage 엔티티 JPA 인터페이스
- **SpringDataLabCreationRequestRepository**: LabCreationRequest 엔티티 JPA 인터페이스
- **SpringDataLabNoticeRepository**: LabNotice 엔티티 JPA 인터페이스

## 💾 Persistence Adapter 구현

Persistence 구현에 대한 상세 내용은 다음을 참조하세요:

- Repository Adapter: @out/persistence/impl/CONVENTIONS.md
- JPA Repository: @out/persistence/jpa/CONVENTIONS.md
- Security Adapter: @out/security/CONVENTIONS.md

## 🔐 보안 및 권한 처리

### Method Security 적용

```java

@RestController
public class LabApplicationController {

    // 인증된 사용자만 접근 가능
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> applyToLab(...) { ...}

    // 커스텀 권한 검증
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'APPROVE')")
    public ResponseEntity<?> approveApplication(...) { ...}
}
```

### 권한 검증 로직

```java

@Component
public class UnifiedPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetId,
                                 Object targetType, Object permission) {

        if (targetType.equals("LabApplication")) {
            return labApplicationPermissionHandler.hasPermission(
                    authentication, (Long) targetId, (String) permission);
        }

        return false;
    }
}
```

## ⚠️ 예외 처리 (Global Exception Handler)

### 전역 예외 처리기

```java

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 도메인 예외 처리
    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(BaseCustomException e) {
        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());
        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }

    // 검증 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.INVALID_INPUT, message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // 인가 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.ACCESS_DENIED);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
}
```

## 🎯 미구현 Adapter (향후 개발)

### 향후 추가될 Controller

- **RankingController**: 점수 관리, 랭킹 조회
- **NoticeController**: 공지사항 CRUD
- **AttendanceController**: QR 출석 관리
- **CalendarController**: 일정 관리
- **AdminController**: 관리자 기능

### 향후 추가될 Outbound Adapter

- **EmailAdapter**: 이메일 발송 (알림, 인증)
- **FileStorageAdapter**: 파일 업로드 (AWS S3)
- **CacheAdapter**: 캐싱 (Redis)
- **NotificationAdapter**: 푸시 알림 (FCM)

## 📋 Adapter 상세 가이드

각 어댑터의 구체적인 구현 방법과 컨벤션은 다음을 참조하세요:

- **Controller**: @in/web/controller/CONVENTIONS.md
- **Request DTO**: @in/web/dto/request/CONVENTIONS.md
- **Response DTO**: @in/web/dto/response/CONVENTIONS.md
- **Repository Adapter**: @out/persistence/impl/CONVENTIONS.md
- **JPA Repository**: @out/persistence/jpa/CONVENTIONS.md
- **Security Adapter**: @out/security/CONVENTIONS.md

## 🧪 Adapter Layer 테스트

### Controller 테스트 (WebMvcTest)

```java

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserQueryUseCase userQueryUseCase;

    @Test
    void 현재_사용자_정보를_조회할_수_있다() throws Exception {
        // given
        UserResponseDto response = UserResponseDto.builder()...build();
        when(userQueryUseCase.findCurrentUser()).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(createAuthentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(response.getName()));
    }
}
```

### Repository Adapter 테스트 (DataJpaTest)

```java

@DataJpaTest
class UserRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    private UserRepositoryAdapter userRepositoryAdapter;

    @BeforeEach
    void setUp() {
        userRepositoryAdapter = new UserRepositoryAdapter(springDataUserRepository);
    }

    @Test
    void 이메일로_사용자_존재_여부를_확인할_수_있다() {
        // given
        User user = User.create("홍길동", "hong@example.com", "password123!", Role.STUDENT);
        entityManager.persistAndFlush(user);

        // when
        boolean exists = userRepositoryAdapter.existsByEmail("hong@example.com");

        // then
        assertThat(exists).isTrue();
    }
}
```