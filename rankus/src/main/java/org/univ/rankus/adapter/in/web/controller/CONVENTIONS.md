# Controller 코딩 컨벤션

> REST API Controller 클래스의 네이밍, 구조, 구현 패턴

## 📛 네이밍 컨벤션

### 클래스 네이밍
- **Controller**: `{Domain}Controller` (예: `UserController`, `LabApplicationController`)
- **매핑 패스**: `/api/{domain}` (예: `/api/users`, `/api/labs`)
- **중첩 리소스**: `/api/{parent}/{parentId}/{child}` (예: `/api/labs/{labId}/applications`)

### 메서드 네이밍
- **HTTP 동사별**:
  - GET: `get{Resource}()`, `get{Resource}List()`
  - POST: `create{Resource}()`, `{action}{Resource}()`
  - PUT: `update{Resource}()`, `{action}{Resource}()`
  - DELETE: `delete{Resource}()`
- **액션 기반**: `approve{Resource}()`, `reject{Resource}()`

## 🏗️ 클래스 구조 패턴

### 기본 Controller 구조
```java
@RestController
@RequestMapping("/api/{domain}")
@RequiredArgsConstructor
@Validated
public class {Domain}Controller {
    
    // 1. 의존성 주입 (private final)
    private final {Domain}CommandUseCase {domain}CommandUseCase;
    private final {Domain}QueryUseCase {domain}QueryUseCase;
    
    // 2. POST 메서드 (생성)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
            @Valid @RequestBody {Domain}CreateRequestDto request) {
        // 구현
    }
    
    // 3. GET 메서드 (조회)
    @GetMapping
    public ResponseEntity<ApiResponse<List<{Domain}ResponseDto>>> get{Domain}List() {
        // 구현
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> get{Domain}(
            @PathVariable Long id) {
        // 구현
    }
    
    // 4. PUT 메서드 (수정)
    @PutMapping("/{id}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #id, '{Domain}', 'UPDATE')")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> update{Domain}(
            @PathVariable Long id,
            @Valid @RequestBody {Domain}UpdateRequestDto request) {
        // 구현
    }
    
    // 5. DELETE 메서드 (삭제)
    @DeleteMapping("/{id}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #id, '{Domain}', 'DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete{Domain}(@PathVariable Long id) {
        // 구현
    }
    
    // 6. 커스텀 액션 (필요시)
    @PutMapping("/{id}/approve")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #id, '{Domain}', 'APPROVE')")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> approve{Domain}(@PathVariable Long id) {
        // 구현
    }
}
```

### 중첩 리소스 Controller 구조
```java
@RestController
@RequestMapping("/api/{parent}/{parentId}/{child}")
@RequiredArgsConstructor
@Validated
public class {Child}Controller {
    
    private final {Child}CommandUseCase {child}CommandUseCase;
    private final {Child}QueryUseCase {child}QueryUseCase;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Child}ResponseDto>> create{Child}(
            @PathVariable Long {parent}Id,
            @Valid @RequestBody {Child}CreateRequestDto request) {
        
        {Child}ResponseDto response = {child}CommandUseCase.create{Child}({parent}Id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "{Child} 생성이 완료되었습니다"));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<{Child}ResponseDto>>> get{Child}List(
            @PathVariable Long {parent}Id) {
        
        List<{Child}ResponseDto> response = {child}QueryUseCase.find{Child}By{Parent}({parent}Id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

## 🎮 어노테이션 패턴

### 보안 어노테이션
```java
// 인증만 필요한 경우
@PreAuthorize("isAuthenticated()")

// 권한 검증이 필요한 경우
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #resourceId, 'ResourceType', 'ACTION')")

// 역할 기반 접근 제어
@PreAuthorize("hasRole('ADMIN') or hasRole('LAB_LEADER')")

// 복합 조건
@PreAuthorize("isAuthenticated() and (@unifiedPermissionEvaluator.hasPermission(authentication, #labId, 'Lab', 'MANAGE') or hasRole('ADMIN'))")
```

### 매핑 어노테이션
```java
// 기본 CRUD
@GetMapping                           // 목록 조회
@GetMapping("/{id}")                 // 단일 조회
@PostMapping                         // 생성
@PutMapping("/{id}")                 // 전체 수정
@PatchMapping("/{id}")               // 부분 수정
@DeleteMapping("/{id}")              // 삭제

// 커스텀 액션
@PutMapping("/{id}/approve")         // 승인
@PutMapping("/{id}/reject")          // 거부
@PostMapping("/{id}/restore")        // 복원
@PatchMapping("/{id}/status")        // 상태 변경
```

### 검증 어노테이션
```java
// 클래스 레벨
@Validated  // 메서드 파라미터 검증 활성화

// 파라미터 레벨
@Valid @RequestBody RequestDto request        // Request Body 검증
@PathVariable @Min(1) Long id                // Path Variable 검증
@RequestParam @NotBlank String keyword       // Request Parameter 검증
```

## 📊 HTTP 상태코드 규칙

### 성공 응답
```java
// 200 OK - 조회, 수정 성공
return ResponseEntity.ok(ApiResponse.success(data));

// 201 Created - 생성 성공
return ResponseEntity.status(HttpStatus.CREATED)
    .body(ApiResponse.success(data, "리소스가 생성되었습니다"));

// 204 No Content - 삭제 성공
return ResponseEntity.noContent().build();

// 또는 삭제 성공 메시지 포함
return ResponseEntity.ok(ApiResponse.success(null, "삭제가 완료되었습니다"));
```

### 에러 응답 (GlobalExceptionHandler에서 처리)
```java
// 400 Bad Request - 잘못된 요청
// 401 Unauthorized - 인증 필요
// 403 Forbidden - 권한 없음
// 404 Not Found - 리소스 없음
// 409 Conflict - 충돌 (중복 등)
// 500 Internal Server Error - 서버 오류
```

## 📦 Request/Response 처리 패턴

### Request 처리
```java
@PostMapping
public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
        @Valid @RequestBody UserCreateRequestDto request,
        @AuthenticationPrincipal CustomUserDetails currentUser) {
    
    // 1. 현재 사용자 정보 활용 (필요시)
    Long currentUserId = currentUser.getUser().getId();
    
    // 2. UseCase 호출
    UserResponseDto response = userCommandUseCase.createUser(request);
    
    // 3. 응답 생성
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(response, "사용자가 생성되었습니다"));
}
```

### Response 생성
```java
// 성공 응답 (데이터 있음)
ApiResponse<UserResponseDto> response = ApiResponse.success(userData);

// 성공 응답 (메시지 포함)
ApiResponse<UserResponseDto> response = ApiResponse.success(userData, "사용자 정보 조회 성공");

// 성공 응답 (데이터 없음)
ApiResponse<Void> response = ApiResponse.success(null, "삭제 완료");
```

## 🔍 페이징 처리 패턴

### 페이징 파라미터
```java
@GetMapping
public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getUserList(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "createdAt") String sort,
        @RequestParam(defaultValue = "desc") String direction) {
    
    Pageable pageable = PageRequest.of(page, size, 
        Sort.Direction.fromString(direction), sort);
    
    PageResponse<UserResponseDto> response = userQueryUseCase.findUsers(pageable);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

### PageResponse 구조
```java
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
    }
}
```

## ⚠️ 예외 처리 패턴

### Controller 레벨 예외 처리
```java
// Controller에서는 예외를 잡지 않고 UseCase로 위임
@PostMapping
public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
        @Valid @RequestBody UserCreateRequestDto request) {
    
    // UseCase에서 발생하는 예외는 GlobalExceptionHandler에서 처리
    UserResponseDto response = userCommandUseCase.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(response));
}
```

### 파라미터 검증 실패
```java
// @Valid 어노테이션으로 자동 검증
// 실패시 MethodArgumentNotValidException 발생
// GlobalExceptionHandler에서 400 Bad Request로 처리
```

## 🧪 Controller 테스트 패턴

### WebMvcTest 활용
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserCommandUseCase userCommandUseCase;
    
    @MockBean
    private UserQueryUseCase userQueryUseCase;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void 사용자_생성_API_테스트() throws Exception {
        // given
        UserCreateRequestDto request = UserCreateRequestDto.builder()
            .name("홍길동")
            .email("hong@example.com")
            .password("password123!")
            .role(Role.STUDENT)
            .build();
            
        UserResponseDto response = UserResponseDto.builder()
            .id(1L)
            .name("홍길동")
            .email("hong@example.com")
            .role(Role.STUDENT)
            .build();
            
        when(userCommandUseCase.createUser(any(UserCreateRequestDto.class)))
            .thenReturn(response);
        
        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpected(jsonPath("$.status").value(201))
                .andExpected(jsonPath("$.data.name").value("홍길동"));
                
        verify(userCommandUseCase).createUser(any(UserCreateRequestDto.class));
    }
}
```

## 🎯 주요 규칙 요약

1. **단일 책임**: 각 Controller는 하나의 도메인 리소스만 담당
2. **표준 매핑**: RESTful API 설계 원칙 준수
3. **보안 우선**: 모든 엔드포인트에 적절한 보안 설정
4. **검증 철저**: 입력값에 대한 충분한 검증
5. **응답 일관성**: ApiResponse로 표준화된 응답 형식
6. **예외 위임**: 비즈니스 예외는 GlobalExceptionHandler에 위임
7. **테스트 가능**: MockMvc를 활용한 통합 테스트 작성