# User Exception 컨벤션

## 클래스 네이밍

- 베이스: `UserException`
- 구체적: `User{Specific}Exception`
- ErrorCode: `UserErrorCode`, `PasswordErrorCode`

## 예외 계층 구조

```
BaseCustomException
└── UserException
    ├── UserNotFoundException
    ├── UserValidationException
    └── PasswordValidationException
```

## 표준 예외 클래스 구조

```java
public class User{Specific}Exception extends UserException {
    
    public User{Specific}Exception() {
        super(UserErrorCode.{ERROR_CODE});
    }
    
    public User{Specific}Exception(String message) {
        super(UserErrorCode.{ERROR_CODE}, message);
    }
    
    public User{Specific}Exception(String message, Throwable cause) {
        super(UserErrorCode.{ERROR_CODE}, message, cause);
    }
    
    // 특정 정보와 함께 생성하는 생성자
    public User{Specific}Exception(Long userId) {
        super(UserErrorCode.{ERROR_CODE}, 
              String.format("사용자를 찾을 수 없습니다. ID: %d", userId));
    }
}
```

## ErrorCode 구조

```java
public enum UserErrorCode implements ErrorCode {
    
    // 400 Bad Request
    NAME_REQUIRED("USER_001", HttpStatus.BAD_REQUEST, "이름은 필수입니다"),
    EMAIL_REQUIRED("USER_002", HttpStatus.BAD_REQUEST, "이메일은 필수입니다"),
    
    // 409 Conflict  
    EMAIL_DUPLICATED("USER_008", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    
    // 401 Unauthorized
    INVALID_CREDENTIALS("USER_009", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
    
    // 404 Not Found
    USER_NOT_FOUND("USER_010", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다");
    
    private final String code;
    private final HttpStatus status;
    private final String message;
}
```

## 구체적 예외 클래스들

### UserNotFoundException

```java
public class UserNotFoundException extends UserException {
    public UserNotFoundException() { /* 기본 */ }
    public UserNotFoundException(Long userId) { /* ID 포함 */ }
    public UserNotFoundException(String email) { /* 이메일 포함 */ }
}
```

### UserValidationException

```java
public class UserValidationException extends UserException {
    private final Map<String, String> fieldErrors;
    
    public UserValidationException(String field, String message) { /* 단일 필드 */ }
    public UserValidationException(Map<String, String> fieldErrors) { /* 다중 필드 */ }
}
```

### PasswordValidationException

```java
public class PasswordValidationException extends UserException {
    public PasswordValidationException() { /* 기본 */ }
    public PasswordValidationException(PasswordErrorCode errorCode) { /* 특정 코드 */ }
}
```

## 예외 발생 시점

- `UserNotFoundException`: Repository 조회 실패 시
- `UserValidationException`: 도메인 객체 생성/수정 시 검증 실패
- `PasswordValidationException`: 비밀번호 관련 검증 실패

## 메시지 생성 규칙

1. 기본 메시지: ErrorCode에서 제공
2. 구체적 정보: 생성자에서 포맷팅
3. 다국어 지원 고려사항

## 테스트 패턴

```java
@Test
void 사용자_없음_예외_테스트() {
    Long userId = 999L;
    UserNotFoundException exception = new UserNotFoundException(userId);
    
    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    assertThat(exception.getMessage()).contains("ID: 999");
}
```