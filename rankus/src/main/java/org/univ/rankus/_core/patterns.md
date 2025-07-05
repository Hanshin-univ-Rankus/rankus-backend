# 공통 코드 패턴 (상세 가이드)

> 📋 **AI 최적화 가이드**: @core/ai-essentials.md (AI 개발자 우선 참조)  
> 📄 **코드 템플릿**: @core/code-templates.md (복사-붙여넣기용)  
> ⚠️ **예외 관리**: @core/error-codes.md (ErrorCode 중앙 관리)

## 🏗️ 클래스 구조 템플릿

### Import 가이드라인

```java
// ✅ 권장: Explicit imports
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.Role;

// ❌ 금지: Wildcard imports
// import org.springframework.*;
// import org.univ.rankus.domain.model.user.*;
```

### 트랜잭션 관리 패턴

```java
// Command Service: 각 메서드에 @Transactional
@Service
@RequiredArgsConstructor
public class UserCommandService {
    
    @Override
    @Transactional
    public User createUser(UserCreateRequestDto request) { /* ... */ }
    
    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateRequestDto request) { /* ... */ }
}

// Query Service: 각 메서드에 @Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserQueryService {
    
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) { /* ... */ }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() { /* ... */ }
}
```

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
@Service @RequiredArgsConstructor
public class {Domain}{Command|Query}Service implements {Domain}{Command|Query}UseCase {
    private final {Domain}RepositoryPort {domain}RepositoryPort;
    
    @Override
    @Transactional // Command: @Transactional, Query: @Transactional(readOnly = true)
    public {Domain} {method}({RequestDto} request) {
        // 1. 검증/조회 → 2. 도메인 로직 → 3. 저장 → 4. Domain Entity 반환
        // Note: DTO 변환은 Controller 계층에서 수행
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
    // 400 Bad Request: 입력값 검증 오류
    NAME_REQUIRED("CODE_001", HttpStatus.BAD_REQUEST, "이름은 필수입니다"),
    NAME_TOO_LONG("CODE_002", HttpStatus.BAD_REQUEST, "이름은 10자 이하여야 합니다"),
    
    // 404 Not Found: 조회 실패
    {DOMAIN}_NOT_FOUND("CODE_404", HttpStatus.NOT_FOUND, "해당 리소스를 찾을 수 없습니다");
    
    private final String code;
    private final HttpStatus status;  
    private final String message;
    
    {Domain}ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
    
    @Override
    public String getCode() { return code; }
    @Override
    public HttpStatus getStatus() { return status; }
    @Override
    public String getMessage() { return message; }
}
```

### GlobalExceptionHandler 템플릿

```java
@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(BaseCustomException e, HttpServletRequest request) {
        log.error("Business exception: {}", e.getMessage(), e);
        ErrorResponse response = ErrorResponse.of(e.getErrorCode(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(e.getStatusCode()).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation failed: {}", e.getMessage());
        ValidationErrorResponse response = ValidationErrorResponse.fromBindingResult(e.getBindingResult());
        return ResponseEntity.badRequest().body(response);
    }
}
```

### Config 클래스 템플릿

```java
@Configuration @Enable{Feature} @RequiredArgsConstructor
public class {Feature}Config {
    private final {Feature}Properties properties;
    
    @Bean @ConditionalOnMissingBean
    public {Type} {beanName}() { return new {Type}(); }
    
    @Bean @Profile("!test")
    public {Type} {productionBean}() { return new {Type}(); }
}
```

## 📋 공통 메서드 패턴

| 작업   | 패턴                  | 리턴     | 설명                   |
|------|---------------------|--------|----------------------|
| 생성   | `create{D}(req)`    | `{D}`  | 검증→생성→저장→Entity 반환   |
| 조회   | `find{D}ById(id)`   | `{D}`  | 조회→Entity 반환         |
| 수정   | `update{D}(id,req)` | `{D}`  | 조회→수정→저장→Entity 반환   |
| 삭제   | `delete{D}(id)`     | `void` | 조회→삭제                |
| 상태변경 | `{action}{D}(id)`   | `{D}`  | 조회→상태변경→저장→Entity 반환 |

## 🔐 보안 패턴

| 권한  | 패턴                                                                                           | 사용 케이스  |
|-----|----------------------------------------------------------------------------------------------|---------|
| 인증만 | `@PreAuthorize("isAuthenticated()")`                                                         | 기본 CRUD |
| 소유권 | `@PreAuthorize("@permissionEvaluator.hasPermission(authentication, #id, 'Type', 'ACTION')")` | 개인 리소스  |
| 역할  | `@PreAuthorize("hasRole('ADMIN')")`                                                          | 관리 기능   |

## ⚠️ 예외 처리 패턴

### 도메인 예외 계층

```
BaseCustomException
└── {Domain}Exception (abstract, optional)
    ├── {Domain}NotFoundException
    ├── {Domain}ValidationException
    └── {Domain}BusinessException
```

### 📋 ValidationException 표준 템플릿

```java
public class {Domain}ValidationException extends BaseCustomException {
    
    public {Domain}ValidationException({Domain}ErrorCode errorCode) {
        super(errorCode);
    }
    
    public {Domain}ValidationException({Domain}ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public {Domain}ValidationException({Domain}ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
```

### 📋 NotFoundException 표준 템플릿

```java
public class {Domain}NotFoundException extends BaseCustomException {
    
    public {Domain}NotFoundException() {
        super({Domain}ErrorCode.{DOMAIN}_NOT_FOUND);
    }
    
    public {Domain}NotFoundException(Long id) {
        super({Domain}ErrorCode.{DOMAIN}_NOT_FOUND, 
              String.format("{Domain}을(를) 찾을 수 없습니다. ID: %d", id));
    }
    
    public {Domain}NotFoundException(String identifier) {
        super({Domain}ErrorCode.{DOMAIN}_NOT_FOUND, 
              String.format("{Domain}을(를) 찾을 수 없습니다. 식별자: %s", identifier));
    }
}
```

### 📋 ErrorCode Enum 표준 템플릿

```java
public enum {Domain}ErrorCode implements ErrorCode {
    
    // 400 Bad Request - 입력값 검증 오류
    {FIELD}_REQUIRED("{PREFIX}_001", HttpStatus.BAD_REQUEST, "{필드}는 필수입니다"),
    {FIELD}_TOO_LONG("{PREFIX}_002", HttpStatus.BAD_REQUEST, "{필드}가 너무 깁니다"),
    {FIELD}_INVALID_FORMAT("{PREFIX}_003", HttpStatus.BAD_REQUEST, "{필드} 형식이 올바르지 않습니다"),
    
    // 404 Not Found - 조회 실패
    {DOMAIN}_NOT_FOUND("{PREFIX}_404", HttpStatus.NOT_FOUND, "{도메인}을(를) 찾을 수 없습니다"),
    
    // 409 Conflict - 중복/충돌
    {FIELD}_DUPLICATED("{PREFIX}_409", HttpStatus.CONFLICT, "이미 사용 중인 {필드}입니다"),
    
    // 422 Unprocessable Entity - 비즈니스 규칙 위반
    CANNOT_CHANGE_STATUS("{PREFIX}_422", HttpStatus.UNPROCESSABLE_ENTITY, "현재 상태에서는 변경할 수 없습니다");
    
    private final String code;
    private final HttpStatus status;
    private final String message;
    
    {Domain}ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
    
    @Override
    public String getCode() { return code; }
    @Override
    public HttpStatus getStatus() { return status; }
    @Override
    public String getMessage() { return message; }
}
```

### 🎯 예외 코드 할당 규칙

| 도메인                | Prefix | 예시                 |
|--------------------|--------|--------------------|
| User               | USER   | USER_001, USER_404 |
| Lab                | LAB    | LAB_001, LAB_404   |
| LabApplication     | LAP    | LAP_001, LAP_404   |
| LabImage           | LIM    | LIM_001, LIM_404   |
| LabCreationRequest | LCR    | LCR_001, LCR_404   |

### 🔍 HTTP 상태 코드 매핑

| 상태 코드 | ErrorCode 접미사 | 용도         |
|-------|---------------|------------|
| 400   | _001~_099     | 입력값 검증 오류  |
| 401   | _401          | 인증 실패      |
| 403   | _403          | 권한 부족      |
| 404   | _404          | 리소스 없음     |
| 409   | _409          | 충돌/중복      |
| 422   | _422          | 비즈니스 규칙 위반 |

### 검증 실패 패턴

```java
// 단순 검증
if (condition) {
    throw new {Domain}ValidationException({Domain}ErrorCode.{ERROR_CODE});
}

// 상세 정보 포함
if (name.length() > MAX_LENGTH) {
    throw new {Domain}ValidationException(
        {Domain}ErrorCode.NAME_TOO_LONG,
        String.format("이름 길이: %d, 최대: %d", name.length(), MAX_LENGTH)
    );
}
```