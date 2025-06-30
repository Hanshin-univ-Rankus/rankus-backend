# 공통 코드 패턴

## 🏗️ 클래스 구조 템플릿

### Entity 템플릿
```java
@Entity @Table(name = "{table}")
public class {Domain} extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    // 비즈니스 필드들
    
    protected {Domain}() {} // JPA
    private {Domain}(/* 생성자 파라미터 */) { /* 검증 및 초기화 */ }
    
    public static {Domain} create(/* 파라미터 */) { return new {Domain}(/* 파라미터 */); }
    
    // 비즈니스 메서드들
    public void {businessMethod}() { /* 도메인 로직 */ }
    
    // Getter만 public
    public Long getId() { return id; }
}
```

### Service 템플릿  
```java
@Service @Transactional @RequiredArgsConstructor
public class {Domain}{Command|Query}Service implements {Domain}{Command|Query}UseCase {
    private final {Domain}RepositoryPort {domain}RepositoryPort;
    
    @Override
    public {Domain}ResponseDto {method}({RequestDto} request) {
        // 1. 검증/조회 → 2. 도메인 로직 → 3. 저장 → 4. 응답 변환
    }
}
```

### Controller 템플릿
```java
@RestController @RequestMapping("/api/{domains}") @RequiredArgsConstructor @Validated
public class {Domain}Controller {
    private final {Domain}{Command|Query}UseCase useCase;
    
    @PostMapping @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
            @Valid @RequestBody {Domain}CreateRequestDto request) {
        {Domain}ResponseDto response = useCase.create{Domain}(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }
}
```

### Repository Adapter 템플릿
```java
@Component @RequiredArgsConstructor
public class {Domain}RepositoryAdapter implements {Domain}RepositoryPort {
    private final SpringData{Domain}Repository repository;
    
    @Override
    public {Domain} save({Domain} {domain}) { return repository.save({domain}); }
    @Override
    public Optional<{Domain}> findById(Long id) { return repository.findById(id); }
}
```

## 🎭 공통 Enum 패턴

### ErrorCode 구현
```java
public enum {Domain}ErrorCode implements ErrorCode {
    {ERROR_NAME}("CODE", HttpStatus.{STATUS}, "메시지");
    
    private final String code;
    private final HttpStatus status;  
    private final String message;
    
    @Override
    public String getCode() { return code; }
    @Override
    public HttpStatus getStatus() { return status; }
    @Override
    public String getMessage() { return message; }
}
```

## 📋 공통 메서드 패턴

| 작업 | 패턴 | 리턴 | 설명 |
|------|------|------|------|
| 생성 | `create{D}(req)` | `{D}Dto` | 검증→생성→저장→응답 |
| 조회 | `find{D}ById(id)` | `{D}Dto` | 조회→변환 |
| 수정 | `update{D}(id,req)` | `{D}Dto` | 조회→수정→저장→응답 |
| 삭제 | `delete{D}(id)` | `void` | 조회→삭제 |
| 상태변경 | `{action}{D}(id)` | `{D}Dto` | 조회→상태변경→저장→응답 |

## 🔐 보안 패턴

| 권한 | 패턴 | 사용 케이스 |
|------|------|-------------|
| 인증만 | `@PreAuthorize("isAuthenticated()")` | 기본 CRUD |
| 소유권 | `@PreAuthorize("@permissionEvaluator.hasPermission(authentication, #id, 'Type', 'ACTION')")` | 개인 리소스 |
| 역할 | `@PreAuthorize("hasRole('ADMIN')")` | 관리 기능 |

## ⚠️ 예외 처리 패턴

### 도메인 예외 계층
```
{Domain}Exception (abstract)
├── {Domain}NotFoundException
├── {Domain}ValidationException  
└── {Domain}BusinessException
```

### 검증 실패 패턴
```java
if (condition) {
    throw new {Domain}ValidationException({Domain}ErrorCode.{ERROR_CODE});
}
```