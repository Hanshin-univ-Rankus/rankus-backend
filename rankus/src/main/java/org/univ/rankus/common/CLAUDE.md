# Common Layer 가이드

> 모든 계층에서 공통으로 사용되는 컴포넌트와 설정을 제공하는 계층

## 🛠️ Common Layer 개요

### 핵심 책임
- **공통 베이스 클래스**: 모든 엔티티가 상속받는 기본 클래스
- **예외 처리 체계**: 전역 예외 처리 및 에러 응답 표준화
- **보안 설정**: 인증/인가 및 JWT 토큰 관리
- **공통 유틸리티**: 전 계층에서 사용하는 헬퍼 클래스

### 설계 원칙
- **재사용성**: 중복 코드 제거 및 공통 로직 집중화
- **일관성**: 표준화된 예외 처리 및 응답 형식
- **확장성**: 새로운 공통 기능 추가 용이성
- **독립성**: 특정 비즈니스 로직에 의존하지 않음

## 📁 Common Layer 구조

### BaseTimeEntity
**JPA Auditing을 통한 생성/수정 시간 자동 관리**

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // getters...
}
```

**활용법**:
- 모든 엔티티가 상속받아 자동으로 시간 필드 관리
- `@EnableJpaAuditing` 설정으로 자동 시간 입력

### Exception 체계 (`exception/`)

#### 베이스 예외 클래스
```java
public abstract class BaseCustomException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    protected BaseCustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    protected BaseCustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public int getStatusCode() {
        return errorCode.getStatusCode();
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
```

#### ErrorCode 인터페이스
```java
public interface ErrorCode {
    int getStatusCode();
    String getMessage();
}
```

#### 전역 ErrorCode
```java
public enum GlobalErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다"),
    INVALID_INPUT(400, "입력값이 올바르지 않습니다"),
    UNAUTHORIZED(401, "인증이 필요합니다"),
    ACCESS_DENIED(403, "접근 권한이 없습니다"),
    NOT_FOUND(404, "요청한 리소스를 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED(405, "허용되지 않은 HTTP 메서드입니다"),
    CONFLICT(409, "요청이 현재 서버 상태와 충돌합니다");
}
```

#### 에러 응답 DTO
```java
public class ErrorResponse {
    
    private int status;
    private String code;
    private String message;
    private LocalDateTime timestamp;
    
    private ErrorResponse(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
            errorCode.getStatusCode(),
            errorCode.name(),
            errorCode.getMessage()
        );
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
            errorCode.getStatusCode(),
            errorCode.name(),
            message
        );
    }
}
```

#### 전역 예외 처리기
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(BaseCustomException e) {
        log.error("Business exception occurred: {}", e.getMessage(), e);
        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());
        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
            
        log.warn("Validation failed: {}", message);
        ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.INVALID_INPUT, message);
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.ACCESS_DENIED);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred", e);
        ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
```

## 🔐 Security 컴포넌트 (`security/`)

Security 컴포넌트에 대한 상세 내용은 다음을 참조하세요:
- 공통 Security: @security/CONVENTIONS.md
- JWT 구현: @security/jwt/CONVENTIONS.md
- 권한 관리: @security/permission/CONVENTIONS.md
- 사용자 인증: @security/customUser/CONVENTIONS.md


## ⚠️ 예외 처리 전략

### 계층별 예외 처리 흐름
```
Domain Exception → Application Exception → Adapter Exception → HTTP Response
     ↓                      ↓                      ↓                ↓
 비즈니스 규칙 위반      유스케이스 실패        HTTP 에러 변환      클라이언트 응답
```

### 예외 처리 모범 사례
1. **도메인 예외**: 비즈니스 규칙 위반 시 명확한 예외 발생
2. **로깅 전략**: 
   - ERROR: 시스템 오류 (500번대)
   - WARN: 비즈니스 예외 (400번대)
   - INFO: 정상 처리
3. **클라이언트 응답**: 일관된 에러 응답 형식 제공

## 🎯 Common 컴포넌트 상세 가이드

각 공통 컴포넌트의 구체적인 구현 방법과 컨벤션은 다음을 참조하세요:

- **예외 처리**: @exception/CONVENTIONS.md
- **보안 설정**: @security/CONVENTIONS.md
- **JWT 구현**: @security/jwt/CONVENTIONS.md
- **권한 관리**: @security/permission/CONVENTIONS.md
- **사용자 인증**: @security/customUser/CONVENTIONS.md

## 🧪 Common Layer 테스트

### JWT 토큰 테스트
```java
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;
    
    @Test
    void 유효한_토큰을_생성할_수_있다() {
        // given
        String email = "test@example.com";
        Role role = Role.STUDENT;
        
        // when
        String token = jwtTokenProvider.createToken(email, role);
        
        // then
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getEmail(token)).isEqualTo(email);
    }
}
```

### 권한 평가 테스트
```java
@ExtendWith(MockitoExtension.class)
class UnifiedPermissionEvaluatorTest {
    
    @Mock
    private LabApplicationPermissionHandler labApplicationPermissionHandler;
    
    @InjectMocks
    private UnifiedPermissionEvaluator permissionEvaluator;
    
    @Test
    void LabApplication_권한을_올바르게_위임한다() {
        // given
        Authentication auth = createAuthentication();
        Long targetId = 1L;
        
        when(labApplicationPermissionHandler.hasPermission(auth, targetId, "VIEW"))
            .thenReturn(true);
        
        // when
        boolean result = permissionEvaluator.hasPermission(
            auth, targetId, "LabApplication", "VIEW");
        
        // then
        assertThat(result).isTrue();
        verify(labApplicationPermissionHandler).hasPermission(auth, targetId, "VIEW");
    }
}
```