# Adapter Layer 가이드

> 외부 기술과 애플리케이션 코어 간의 연결점 역할을 하는 어댑터 계층

## 🔌 Adapter Layer 개요

### 핵심 책임

- **기술 연동**: 외부 프레임워크/라이브러리와의 통합
- **포트 구현**: Application Layer의 포트 인터페이스 구현
- **데이터 변환**: 외부 형식 ↔ 도메인 모델 간 변환
- **프로토콜 처리**: HTTP, JPA 등 특정 프로토콜 처리

### 설계 원칙

- **포트 구현**: Application Layer에서 정의한 인터페이스 구현
- **기술 격리**: 특정 기술의 세부사항을 애플리케이션 코어로부터 격리
- **단방향 의존성**: Adapter → Application 방향으로만 의존
- **변환 책임**: 외부 데이터와 도메인 모델 간 변환 담당

## 📁 Adapter Layer 구조

### Inbound Adapter (`in/`)

**외부 요청을 애플리케이션 내부로 전달하는 어댑터**

#### Web Adapter (`in/web/`)

- **Controller**: REST API 엔드포인트 제공
- **DTO**: HTTP 요청/응답 데이터 구조
- **Exception Handler**: HTTP 에러 응답 처리

### Outbound Adapter (`out/`)

**애플리케이션에서 외부 시스템으로 요청을 전달하는 어댑터**

#### Persistence Adapter (`out/persistence/`)

- **Repository Adapter**: Application Port 구현
- **JPA Repository**: Spring Data JPA 인터페이스
- **Entity Mapping**: 도메인 모델 ↔ JPA 엔티티 변환

## 🎮 Controller 구현 현황

### 현재 구현된 Controller

#### AuthController (`/api/auth`)

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(
            @Valid @RequestBody UserRegisterRequestDto request);
            
    @PostMapping("/login") 
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody UserLoginRequestDto request);
}
```

#### UserController (`/api/users`)

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser();
}
```

#### LabPromotionController (`/api/labs`)

```java
@RestController
@RequestMapping("/api/labs")
public class LabPromotionController {
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<LabResponseDto>>> getAllLabs();
    
    @GetMapping("/{labId}")
    public ResponseEntity<ApiResponse<LabResponseDto>> getLabById(
            @PathVariable Long labId);
}
```

#### LabApplicationController (`/api/labs/{labId}/applications`)

```java
@RestController
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

### Spring Data JPA Repository

- **SpringDataUserRepository**: User 엔티티 JPA 인터페이스
- **SpringDataLabRepository**: Lab 엔티티 JPA 인터페이스
- **SpringDataLabApplicationRepository**: LabApplication 엔티티 JPA 인터페이스
- **SpringDataLabImageRepository**: LabImage 엔티티 JPA 인터페이스
- **SpringDataLabCreationRequestRepository**: LabCreationRequest 엔티티 JPA 인터페이스

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
    public ResponseEntity<?> applyToLab(...) { ... }
    
    // 커스텀 권한 검증
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'APPROVE')")
    public ResponseEntity<?> approveApplication(...) { ... }
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