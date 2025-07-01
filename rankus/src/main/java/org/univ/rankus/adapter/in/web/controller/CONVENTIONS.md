# Controller 컨벤션

> 📋 **네이밍 규칙**: @core/conventions.md#controller  
> 🏗️ **기본 패턴**: @core/patterns.md#controller-템플릿  
> 📊 **HTTP 매트릭스**: @core/http-matrix.md

## 🎯 핵심 규칙

### 네이밍 패턴

| 타입  | 패턴                                 | 예시                                           |
|-----|------------------------------------|----------------------------------------------|
| 클래스 | `{Domain}Controller`               | `UserController`, `LabApplicationController` |
| 경로  | `/api/{domain}`                    | `/api/users`, `/api/labs`                    |
| 중첩  | `/api/{parent}/{parentId}/{child}` | `/api/labs/{labId}/applications`             |

### 메서드 매트릭스

| HTTP   | 메서드 패턴                                        | 사용 케이스 |
|--------|-----------------------------------------------|--------|
| GET    | `get{Resource}()` / `get{Resource}List()`     | 조회     |
| POST   | `create{Resource}()` / `{action}{Resource}()` | 생성/액션  |
| PUT    | `update{Resource}()` / `{action}{Resource}()` | 수정/액션  |
| DELETE | `delete{Resource}()`                          | 삭제     |

## 🏗️ 구조 패턴

### 기본 Controller 템플릿

```java
@RestController
@RequestMapping("/api/{domain}")
@RequiredArgsConstructor
@Validated
public class {Domain}Controller {
    
    private final {Domain}CommandUseCase commandUseCase;
    private final {Domain}QueryUseCase queryUseCase;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
            @Valid @RequestBody {Domain}CreateRequestDto request) {
        // 구현: commandUseCase 호출 → ApiResponse.created() 반환
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> get{Domain}(@PathVariable Long id) {
        // 구현: queryUseCase 호출 → ApiResponse.success() 반환  
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(...)")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> update{Domain}(...) {
        // 구현: commandUseCase 호출 → ApiResponse.success() 반환
    }
}
```

## 🎮 어노테이션 매트릭스

> 📋 **권한 매트릭스**: @core/http-matrix.md#preauthorize-패턴-매트릭스

### 보안 어노테이션

| 권한 타입 | 패턴                                                         | 사용 케이스  |
|-------|------------------------------------------------------------|---------|
| 인증만   | `@PreAuthorize("isAuthenticated()")`                       | 기본 CRUD |
| 역할 기반 | `@PreAuthorize("hasRole('ADMIN')")`                        | 관리 기능   |
| 소유권   | `@PreAuthorize("@permissionEvaluator.hasPermission(...)")` | 개인 리소스  |

### 매핑 어노테이션

| HTTP   | 매핑                                                      | 용도       |
|--------|---------------------------------------------------------|----------|
| GET    | `@GetMapping` / `@GetMapping("/{id}")`                  | 목록/단일 조회 |
| POST   | `@PostMapping` / `@PostMapping("/{id}/action")`         | 생성/액션    |
| PUT    | `@PutMapping("/{id}")` / `@PutMapping("/{id}/approve")` | 수정/상태변경  |
| DELETE | `@DeleteMapping("/{id}")`                               | 삭제       |

### 검증 어노테이션

| 레벨    | 어노테이션                     | 용도                   |
|-------|---------------------------|----------------------|
| 클래스   | `@Validated`              | 파라미터 검증 활성화          |
| Body  | `@Valid @RequestBody`     | Request Body 검증      |
| Path  | `@PathVariable @Min(1)`   | Path Variable 검증     |
| Param | `@RequestParam @NotBlank` | Request Parameter 검증 |

## 📊 응답 패턴

> 📋 **상태코드 매트릭스**: @core/http-matrix.md#http-상태코드-매트릭스

### 표준 ApiResponse 팩토리 메서드 사용 ✅

| 상태  | ResponseEntity 패턴                | ApiResponse 메서드                      | 사용 예시      |
|-----|----------------------------------|--------------------------------------|------------|
| 200 | `ResponseEntity.ok()`            | `ApiResponse.success(data)`          | 조회 성공      |
| 200 | `ResponseEntity.ok()`            | `ApiResponse.success(data, message)` | 메시지 커스텀    |
| 201 | `ResponseEntity.status(CREATED)` | `ApiResponse.created(data)`          | 생성 성공      |
| 201 | `ResponseEntity.status(CREATED)` | `ApiResponse.created(data, message)` | 생성 메시지 커스텀 |
| 204 | `ResponseEntity.noContent()`     | -                                    | 삭제 성공      |

### ❌ 사용 금지 패턴 (수정 완료)

```java
// ❌ 금지: 수동 빌더 패턴
ApiResponse.builder()
    .status(200)
    .message("성공")
    .data(data)
    .build();

// ❌ 금지: 하드코딩된 상태코드
ApiResponse.builder()
    .status(HttpStatus.OK.value())
    .build();
```

### ✅ 표준 패턴 (현재 적용됨)

```java
// ✅ 권장: 팩토리 메서드 사용
ApiResponse.success(data, "조회 성공");
ApiResponse.created(data, "생성 성공");
ApiResponse.success(data); // 기본 메시지
```

## 📦 처리 플로우

### Request → Response 플로우

```java
// 1. 파라미터 주입 → 2. UseCase 호출 → 3. 응답 생성
@PostMapping
public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
        @Valid @RequestBody UserCreateRequestDto request,
        @AuthenticationPrincipal CustomUserDetails currentUser) {
    
    UserResponseDto response = commandUseCase.createUser(request);
    return ResponseEntity.status(CREATED).body(ApiResponse.created(response));
}
```

### 페이징 파라미터 패턴

```java
@GetMapping
public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getUserList(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "createdAt") String sort) {
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    PageResponse<UserResponseDto> response = queryUseCase.findUsers(pageable);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

## ⚠️ 예외 및 검증

### 예외 처리 원칙

- Controller: 예외 처리 없음, UseCase 위임
- 검증: `@Valid` 자동 검증 → GlobalExceptionHandler 처리
- 권한: `@PreAuthorize` 실패 → 403 Forbidden

## 🧪 테스트 패턴

> 📋 **테스트 가이드**: @core/testing.md#controller-테스트

### WebMvcTest 구조

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private UserCommandUseCase commandUseCase;
    @MockBean private UserQueryUseCase queryUseCase;
    
    @Test @WithMockUser
    void API_테스트() { /* 구현 */ }
}
```

## 🎯 핵심 규칙

1. **단일 책임**: 도메인별 Controller 분리
2. **RESTful 설계**: 표준 HTTP 메서드 활용
3. **보안 우선**: 적절한 @PreAuthorize 설정
4. **검증 철저**: @Valid + 파라미터 검증
5. **응답 일관성**: ApiResponse 표준화
6. **예외 위임**: GlobalExceptionHandler 활용
7. **테스트 완비**: WebMvcTest + MockBean