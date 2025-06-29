# Exception 코딩 컨벤션

> 예외 처리 체계의 네이밍, 구조, 구현 패턴

## 📛 네이밍 컨벤션

### Exception 클래스 네이밍
- **도메인 예외**: `{Domain}{Specific}Exception`
  - `UserNotFoundException`, `UserValidationException`
  - `LabApplicationNotFoundException`, `LabApplicationValidationException`
- **베이스 예외**: `{Domain}Exception`
  - `UserException`, `LabException`, `LabApplicationException`
- **글로벌 예외**: `{Type}Exception`
  - `ValidationException`, `AuthenticationException`

### ErrorCode Enum 네이밍
- **도메인별**: `{Domain}ErrorCode`
  - `UserErrorCode`, `LabErrorCode`, `LabApplicationErrorCode`
- **글로벌**: `GlobalErrorCode`
- **상수 네이밍**: `UPPER_SNAKE_CASE`
  - `USER_NOT_FOUND`, `INVALID_PASSWORD`, `DUPLICATE_EMAIL`

## 🏗️ 예외 계층 구조 패턴

### 베이스 예외 클래스
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
    
    protected BaseCustomException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
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

### 도메인별 베이스 예외
```java
public abstract class UserException extends BaseCustomException {
    
    protected UserException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    protected UserException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    protected UserException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
```

### 구체적 예외 클래스
```java
public class UserNotFoundException extends UserException {
    
    public UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }
    
    public UserNotFoundException(Long userId) {
        super(UserErrorCode.USER_NOT_FOUND, 
              String.format("사용자를 찾을 수 없습니다. ID: %d", userId));
    }
    
    public UserNotFoundException(String email) {
        super(UserErrorCode.USER_NOT_FOUND, 
              String.format("사용자를 찾을 수 없습니다. Email: %s", email));
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(UserErrorCode.USER_NOT_FOUND, message, cause);
    }
}
```

### 복잡한 예외 클래스
```java
public class UserValidationException extends UserException {
    
    private final Map<String, String> fieldErrors;
    
    public UserValidationException(ErrorCode errorCode) {
        super(errorCode);
        this.fieldErrors = new HashMap<>();
    }
    
    public UserValidationException(ErrorCode errorCode, String field, String message) {
        super(errorCode, message);
        this.fieldErrors = Map.of(field, message);
    }
    
    public UserValidationException(ErrorCode errorCode, Map<String, String> fieldErrors) {
        super(errorCode, createMessage(fieldErrors));
        this.fieldErrors = new HashMap<>(fieldErrors);
    }
    
    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }
    
    private static String createMessage(Map<String, String> fieldErrors) {
        return fieldErrors.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", "));
    }
}
```

## 🏷️ ErrorCode 인터페이스 및 구현

### ErrorCode 인터페이스
```java
public interface ErrorCode {
    
    /**
     * 에러 코드명을 반환합니다.
     * 
     * @return 에러 코드명
     */
    String getCode();
    
    /**
     * HTTP 상태를 반환합니다.
     * 
     * @return HttpStatus 객체
     */
    HttpStatus getStatus();
    
    /**
     * 에러 메시지를 반환합니다.
     * 
     * @return 에러 메시지
     */
    String getMessage();
}
```

### 글로벌 ErrorCode
```java
public enum GlobalErrorCode implements ErrorCode {
    
    // 4xx Client Error
    BAD_REQUEST(400, "잘못된 요청입니다"),
    UNAUTHORIZED(401, "인증이 필요합니다"),
    FORBIDDEN(403, "접근 권한이 없습니다"),
    NOT_FOUND(404, "요청한 리소스를 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED(405, "허용되지 않은 HTTP 메서드입니다"),
    CONFLICT(409, "요청이 현재 서버 상태와 충돌합니다"),
    
    // 422 Unprocessable Entity
    INVALID_INPUT(422, "입력값이 올바르지 않습니다"),
    
    // 5xx Server Error  
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다"),
    SERVICE_UNAVAILABLE(503, "서비스를 사용할 수 없습니다");
    
    private final int statusCode;
    private final String message;
    
    GlobalErrorCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
    
    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
```

### 도메인별 ErrorCode 표준화
```java
public enum UserErrorCode implements ErrorCode {
    
    // 400 Bad Request: 입력값 검증 오류
    NAME_REQUIRED        ("USER_001", HttpStatus.BAD_REQUEST,    "이름은 필수입니다"),
    EMAIL_REQUIRED       ("USER_002", HttpStatus.BAD_REQUEST,    "이메일은 필수입니다"),
    EMAIL_INVALID        ("USER_003", HttpStatus.BAD_REQUEST,    "이메일 형식이 올바르지 않습니다"),
    NAME_TOO_LONG        ("USER_004", HttpStatus.BAD_REQUEST,    "이름은 30자 이하여야 합니다"),
    PASSWORD_REQUIRED    ("USER_007", HttpStatus.BAD_REQUEST,    "비밀번호는 필수입니다"),

    // 409 Conflict: 데이터 중복/충돌
    EMAIL_DUPLICATED     ("USER_008", HttpStatus.CONFLICT,       "이미 사용 중인 이메일입니다"),

    // 401 Unauthorized: 인증 오류
    INVALID_CREDENTIALS  ("USER_009", HttpStatus.UNAUTHORIZED,   "이메일 또는 비밀번호가 일치하지 않습니다"),

    // 404 Not Found: 조회 실패
    USER_NOT_FOUND       ("USER_010", HttpStatus.NOT_FOUND,      "사용자를 찾을 수 없습니다");
    
    private final String code;
    private final HttpStatus status;
    private final String message;
    
    UserErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
    
    @Override
    public String getCode() {
        return code;
    }
    
    @Override
    public HttpStatus getStatus() {
        return status;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
    // 하위 호환성을 위한 메서드
    public int getStatusCode() {
        return status.value();
    }
}
```

### 상태별 ErrorCode 구조
```java
public enum LabApplicationErrorCode implements ErrorCode {
    
    // 404 Not Found
    LAB_APPLICATION_NOT_FOUND(404, "지원서를 찾을 수 없습니다"),
    
    // 400 Bad Request
    INTERVIEW_TIME_REQUIRED(400, "면접 시간은 필수입니다"),
    INTERVIEW_TIME_MUST_BE_FUTURE(400, "면접 시간은 미래 시점이어야 합니다"),
    INVALID_APPLICATION_STATUS(400, "올바르지 않은 지원서 상태입니다"),
    
    // 403 Forbidden
    INSUFFICIENT_PERMISSION_FOR_APPROVAL(403, "지원서를 승인할 권한이 없습니다"),
    INSUFFICIENT_PERMISSION_FOR_REJECTION(403, "지원서를 거부할 권한이 없습니다"),
    NOT_APPLICATION_OWNER(403, "본인의 지원서가 아닙니다"),
    
    // 409 Conflict  
    DUPLICATE_APPLICATION(409, "이미 해당 랩실에 지원한 이력이 있습니다"),
    
    // 422 Unprocessable Entity
    CANNOT_CHANGE_STATUS_AFTER_DECISION(422, "이미 처리된 지원서는 상태를 변경할 수 없습니다"),
    CANNOT_CANCEL_APPROVED_APPLICATION(422, "승인된 지원서는 취소할 수 없습니다"),
    APPLICATION_PERIOD_EXPIRED(422, "지원 기간이 만료되었습니다");
    
    private final int statusCode;
    private final String message;
    
    LabApplicationErrorCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
    
    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
```

## 📋 에러 응답 DTO

### 기본 ErrorResponse 구조
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private int status;
    private String code;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    
    // 기본 에러 응답 생성
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
            errorCode.getStatusCode(),
            errorCode.getCode(),
            errorCode.getMessage(),
            LocalDateTime.now(),
            null
        );
    }
    
    // 커스텀 메시지와 함께 에러 응답 생성
    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return new ErrorResponse(
            errorCode.getStatusCode(),
            errorCode.getCode(),
            customMessage,
            LocalDateTime.now(),
            null
        );
    }
    
    // 경로 정보와 함께 에러 응답 생성
    public static ErrorResponse of(ErrorCode errorCode, String customMessage, String path) {
        return new ErrorResponse(
            errorCode.getStatusCode(),
            errorCode.getCode(),
            customMessage,
            LocalDateTime.now(),
            path
        );
    }
}
```

### 검증 에러 응답 구조
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationErrorResponse extends ErrorResponse {
    
    private List<FieldError> fieldErrors;
    
    @Data
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
    }
    
    public static ValidationErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        response.setStatus(errorCode.getStatusCode());
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());
        response.setTimestamp(LocalDateTime.now());
        response.setFieldErrors(fieldErrors);
        return response;
    }
    
    public static ValidationErrorResponse fromBindingResult(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
            .map(error -> new FieldError(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());
            
        return of(GlobalErrorCode.INVALID_INPUT, fieldErrors);
    }
}
```

## ⚠️ 전역 예외 처리기

### GlobalExceptionHandler 구조
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 커스텀 비즈니스 예외 처리
     */
    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            BaseCustomException e, HttpServletRequest request) {
        
        log.error("Business exception occurred: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            e.getErrorCode(), 
            e.getMessage(), 
            request.getRequestURI()
        );
        
        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }
    
    /**
     * Bean Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        log.warn("Validation failed: {}", e.getMessage());
        
        ValidationErrorResponse errorResponse = ValidationErrorResponse.fromBindingResult(e.getBindingResult());
        errorResponse.setPath(request.getRequestURI());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * 제약조건 위반 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException e, HttpServletRequest request) {
        
        log.warn("Constraint violation: {}", e.getMessage());
        
        String message = e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
            
        ErrorResponse errorResponse = ErrorResponse.of(
            GlobalErrorCode.INVALID_INPUT, 
            message, 
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * 접근 거부 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException e, HttpServletRequest request) {
        
        log.warn("Access denied: {} for {}", e.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            GlobalErrorCode.FORBIDDEN,
            "접근 권한이 없습니다",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * 처리되지 않은 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception e, HttpServletRequest request) {
        
        log.error("Unexpected exception occurred", e);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            GlobalErrorCode.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다",
            request.getRequestURI()
        );
        
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
```

## 🧪 예외 처리 테스트 패턴

### 예외 클래스 테스트
```java
class UserNotFoundExceptionTest {
    
    @Test
    void 기본_생성자로_예외_생성() {
        // when
        UserNotFoundException exception = new UserNotFoundException();
        
        // then
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
        assertThat(exception.getStatusCode()).isEqualTo(404);
        assertThat(exception.getMessage()).isEqualTo("사용자를 찾을 수 없습니다");
    }
    
    @Test
    void 사용자_ID로_예외_생성() {
        // given
        Long userId = 123L;
        
        // when
        UserNotFoundException exception = new UserNotFoundException(userId);
        
        // then
        assertThat(exception.getMessage()).contains("ID: 123");
    }
}
```

### GlobalExceptionHandler 테스트
```java
@WebMvcTest
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserQueryUseCase userQueryUseCase;
    
    @Test
    void 사용자_없음_예외_처리() throws Exception {
        // given
        when(userQueryUseCase.findUserById(999L))
            .thenThrow(new UserNotFoundException(999L));
        
        // when & then
        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").exists());
    }
}
```

## 🎯 주요 규칙 요약

1. **계층적 구조**: BaseCustomException → 도메인 베이스 → 구체적 예외
2. **명확한 네이밍**: 도메인과 구체적 상황을 나타내는 명확한 이름
3. **적절한 HTTP 상태코드**: 비즈니스 상황에 맞는 상태코드 사용
4. **풍부한 정보**: 디버깅에 도움되는 충분한 정보 제공
5. **일관된 응답**: 모든 예외를 일관된 형식으로 응답
6. **로깅 전략**: 심각도에 따른 적절한 로그 레벨 사용
7. **테스트 가능**: 예외 상황에 대한 충분한 테스트 작성