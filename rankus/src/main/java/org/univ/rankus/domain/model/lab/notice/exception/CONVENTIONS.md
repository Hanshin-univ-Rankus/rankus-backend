# Notice Exception 컨벤션

## 클래스 네이밍

- 베이스: `NoticeException`
- 구체적: `Notice{Specific}Exception`
- ErrorCode: `NoticeErrorCode`

## 예외 계층 구조

```
BaseCustomException
└── NoticeException
    ├── NoticeNotFoundException
    └── NoticeValidationException
```

## 표준 예외 클래스 구조

```java
public class Notice{Specific}Exception extends NoticeException {
    
    public Notice{Specific}Exception() {
        super(NoticeErrorCode.{ERROR_CODE});
    }
    
    public Notice{Specific}Exception(String message) {
        super(NoticeErrorCode.{ERROR_CODE}, message);
    }
    
    public Notice{Specific}Exception(String message, Throwable cause) {
        super(NoticeErrorCode.{ERROR_CODE}, message, cause);
    }
    
    // 특정 정보와 함께 생성하는 생성자
    public Notice{Specific}Exception(Long noticeId) {
        super(NoticeErrorCode.{ERROR_CODE}, 
              String.format("공지사항을 찾을 수 없습니다. ID: %d", noticeId));
    }
}
```

## ErrorCode 구조

```java
public enum NoticeErrorCode implements ErrorCode {
    
    // 400 Bad Request - 입력값 검증 오류
    NOTICE_TITLE_REQUIRED("NOTICE_001", HttpStatus.BAD_REQUEST, "공지사항 제목은 필수입니다"),
    NOTICE_TITLE_TOO_LONG("NOTICE_002", HttpStatus.BAD_REQUEST, "공지사항 제목은 100자를 초과할 수 없습니다"),
    NOTICE_CONTENT_REQUIRED("NOTICE_003", HttpStatus.BAD_REQUEST, "공지사항 내용은 필수입니다"),
    NOTICE_CONTENT_TOO_LONG("NOTICE_004", HttpStatus.BAD_REQUEST, "공지사항 내용은 2000자를 초과할 수 없습니다"),
    NOTICE_TYPE_REQUIRED("NOTICE_005", HttpStatus.BAD_REQUEST, "공지사항 타입은 필수입니다"),
    NOTICE_AUTHOR_REQUIRED("NOTICE_006", HttpStatus.BAD_REQUEST, "공지사항 작성자는 필수입니다"),
    NOTICE_LAB_REQUIRED("NOTICE_007", HttpStatus.BAD_REQUEST, "공지사항이 속할 랩실은 필수입니다"),
    
    // 403 Forbidden - 권한 오류
    INSUFFICIENT_PERMISSION_FOR_NOTICE_MANAGEMENT("NOTICE_403_001", HttpStatus.FORBIDDEN, "공지사항을 관리할 권한이 없습니다"),
    INSUFFICIENT_PERMISSION_FOR_NOTICE_VIEW("NOTICE_403_002", HttpStatus.FORBIDDEN, "공지사항을 조회할 권한이 없습니다"),
    NOT_NOTICE_OWNER("NOTICE_403_003", HttpStatus.FORBIDDEN, "본인이 작성한 공지사항이 아닙니다"),
    
    // 404 Not Found
    NOTICE_NOT_FOUND("NOTICE_404", HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다");
    
    private final String code;
    private final HttpStatus status;
    private final String message;
    
    NoticeErrorCode(String code, HttpStatus status, String message) {
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
}
```

## 구체적 예외 클래스들

### NoticeNotFoundException

```java
public class NoticeNotFoundException extends NoticeException {
    
    public NoticeNotFoundException() {
        super(NoticeErrorCode.NOTICE_NOT_FOUND);
    }
    
    public NoticeNotFoundException(Long noticeId) {
        super(NoticeErrorCode.NOTICE_NOT_FOUND, 
              String.format("공지사항을 찾을 수 없습니다. ID: %d", noticeId));
    }
    
    public NoticeNotFoundException(String message) {
        super(NoticeErrorCode.NOTICE_NOT_FOUND, message);
    }
    
    public NoticeNotFoundException(String message, Throwable cause) {
        super(NoticeErrorCode.NOTICE_NOT_FOUND, message, cause);
    }
}
```

### NoticeValidationException

```java
public class NoticeValidationException extends NoticeException {
    
    private final Map<String, String> fieldErrors;
    
    public NoticeValidationException(NoticeErrorCode errorCode) {
        super(errorCode);
        this.fieldErrors = new HashMap<>();
    }
    
    public NoticeValidationException(NoticeErrorCode errorCode, String message) {
        super(errorCode, message);
        this.fieldErrors = new HashMap<>();
    }
    
    // 단일 필드 검증 오류
    public NoticeValidationException(String field, String message) {
        super(NoticeErrorCode.NOTICE_TITLE_REQUIRED, message);
        this.fieldErrors = Map.of(field, message);
    }
    
    // 다중 필드 검증 오류
    public NoticeValidationException(Map<String, String> fieldErrors) {
        super(NoticeErrorCode.NOTICE_TITLE_REQUIRED, "공지사항 입력값이 올바르지 않습니다");
        this.fieldErrors = new HashMap<>(fieldErrors);
    }
    
    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }
}
```

## 예외 발생 시점

- `NoticeNotFoundException`: Repository 조회 실패 시
- `NoticeValidationException`: 도메인 객체 생성/수정 시 검증 실패
- 권한 관련 예외: UseCase에서 권한 확인 실패 시

## 권한 검증 예외 패턴

```java
public class InsufficientNoticePermissionException extends NoticeException {
    
    public InsufficientNoticePermissionException(String action) {
        super(NoticeErrorCode.INSUFFICIENT_PERMISSION_FOR_NOTICE_MANAGEMENT,
              String.format("공지사항 %s 권한이 없습니다", action));
    }
    
    public InsufficientNoticePermissionException(Long noticeId, String action) {
        super(NoticeErrorCode.INSUFFICIENT_PERMISSION_FOR_NOTICE_MANAGEMENT,
              String.format("공지사항 %d에 대한 %s 권한이 없습니다", noticeId, action));
    }
}
```

## 비즈니스 규칙 예외 패턴

```java
public void validateTitle(String title) {
    if (isNullOrEmpty(title)) {
        throw new NoticeValidationException(
            NoticeErrorCode.NOTICE_TITLE_REQUIRED,
            "공지사항 제목은 필수입니다"
        );
    }
    
    if (title.trim().length() > 100) {
        throw new NoticeValidationException(
            NoticeErrorCode.NOTICE_TITLE_TOO_LONG,
            String.format("공지사항 제목은 100자를 초과할 수 없습니다. 현재: %d자", title.trim().length())
        );
    }
}

public void validateContent(String content) {
    if (isNullOrEmpty(content)) {
        throw new NoticeValidationException(
            NoticeErrorCode.NOTICE_CONTENT_REQUIRED,
            "공지사항 내용은 필수입니다"
        );
    }
    
    if (content.trim().length() > 2000) {
        throw new NoticeValidationException(
            NoticeErrorCode.NOTICE_CONTENT_TOO_LONG,
            String.format("공지사항 내용은 2000자를 초과할 수 없습니다. 현재: %d자", content.trim().length())
        );
    }
}
```

## 헬퍼 메서드

```java
// 예외 생성 헬퍼 메서드
private static NoticeValidationException ex(NoticeErrorCode errorCode) {
    return new NoticeValidationException(errorCode);
}

private static NoticeValidationException ex(NoticeErrorCode errorCode, String message) {
    return new NoticeValidationException(errorCode, message);
}

// null 또는 빈 값 체크 헬퍼
private static boolean isNullOrEmpty(String value) {
    return value == null || value.trim().isEmpty();
}
```

## 메시지 생성 규칙

1. **기본 메시지**: ErrorCode에서 제공하는 기본 메시지 사용
2. **구체적 정보**: 생성자에서 구체적인 값 포함하여 포맷팅
3. **사용자 친화적**: 기술적 용어보다 일반 사용자가 이해하기 쉬운 용어 사용
4. **다국어 지원**: 향후 국제화를 고려한 메시지 구조

## 테스트 패턴

```java
@Test
void 공지사항_없음_예외_테스트() {
    // given
    Long noticeId = 999L;
    
    // when
    NoticeNotFoundException exception = new NoticeNotFoundException(noticeId);
    
    // then
    assertThat(exception.getErrorCode()).isEqualTo(NoticeErrorCode.NOTICE_NOT_FOUND);
    assertThat(exception.getMessage()).contains("ID: 999");
}

@Test
void 제목_검증_예외_테스트() {
    // given
    String longTitle = "a".repeat(101);
    
    // when & then
    assertThatThrownBy(() -> validateTitle(longTitle))
        .isInstanceOf(NoticeValidationException.class)
        .hasMessageContaining("100자를 초과할 수 없습니다");
}

@Test
void 권한_부족_예외_테스트() {
    // given
    String action = "수정";
    
    // when
    InsufficientNoticePermissionException exception = 
        new InsufficientNoticePermissionException(action);
    
    // then
    assertThat(exception.getErrorCode())
        .isEqualTo(NoticeErrorCode.INSUFFICIENT_PERMISSION_FOR_NOTICE_MANAGEMENT);
    assertThat(exception.getMessage()).contains("수정 권한이 없습니다");
}

@Test
void 다중_필드_검증_예외_테스트() {
    // given
    Map<String, String> fieldErrors = Map.of(
        "title", "제목은 필수입니다",
        "content", "내용은 필수입니다"
    );
    
    // when
    NoticeValidationException exception = new NoticeValidationException(fieldErrors);
    
    // then
    assertThat(exception.getFieldErrors()).hasSize(2);
    assertThat(exception.getFieldErrors().get("title")).isEqualTo("제목은 필수입니다");
}
```

## 핵심 규칙

1. **명확한 에러 코드**: 각 검증 실패 상황에 대해 구체적인 에러 코드 정의
2. **구체적인 메시지**: 사용자가 무엇을 수정해야 하는지 명확히 제시
3. **일관된 구조**: 모든 Notice 관련 예외는 동일한 패턴으로 구현
4. **권한 분리**: 조회/수정/삭제 권한을 명확히 구분하여 예외 처리
5. **필드별 검증**: 각 필드의 검증 규칙을 명확히 정의하고 예외로 표현
6. **계층적 예외**: 베이스 예외부터 구체적 예외까지 계층적으로 설계