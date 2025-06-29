# Lab Exception 컨벤션

## 클래스 네이밍
- 베이스: `LabException`, `LabApplicationException`, `LabImageException`
- 구체적: `{Domain}{Specific}Exception`
- ErrorCode: `LabErrorCode`, `LabApplicationErrorCode`, `LabImageErrorCode`

## 예외 계층 구조
```
BaseCustomException
├── LabException
│   ├── LabNotFoundException
│   └── LabValidationException
├── LabApplicationException
│   ├── LabApplicationNotFoundException
│   └── LabApplicationValidationException
└── LabImageException
    ├── LabImageNotFoundException
    └── LabImageValidationException
```

## Lab 도메인 ErrorCode
```java
public enum LabErrorCode implements ErrorCode {
    
    // 400 Bad Request
    LAB_NAME_REQUIRED("LAB_001", HttpStatus.BAD_REQUEST, "랩실 이름은 필수입니다"),
    LAB_NAME_TOO_LONG("LAB_002", HttpStatus.BAD_REQUEST, "랩실 이름은 10자를 초과할 수 없습니다"),
    RANKING_MUST_BE_NON_NEGATIVE("LAB_003", HttpStatus.BAD_REQUEST, "랭킹은 0 이상이어야 합니다"),
    
    // 404 Not Found  
    LAB_NOT_FOUND("LAB_404", HttpStatus.NOT_FOUND, "랩실을 찾을 수 없습니다");
}
```

## LabApplication 도메인 ErrorCode
```java
public enum LabApplicationErrorCode implements ErrorCode {
    
    // 400 Bad Request
    INTERVIEW_TIME_REQUIRED("LAB_APP_001", HttpStatus.BAD_REQUEST, "면접 시간은 필수입니다"),
    INTERVIEW_TIME_MUST_BE_FUTURE("LAB_APP_002", HttpStatus.BAD_REQUEST, "면접 시간은 미래 시점이어야 합니다"),
    
    // 403 Forbidden
    INSUFFICIENT_PERMISSION_FOR_APPROVAL("LAB_APP_403", HttpStatus.FORBIDDEN, "지원서를 승인할 권한이 없습니다"),
    NOT_APPLICATION_OWNER("LAB_APP_404", HttpStatus.FORBIDDEN, "본인의 지원서가 아닙니다"),
    
    // 404 Not Found
    LAB_APPLICATION_NOT_FOUND("LAB_APP_404", HttpStatus.NOT_FOUND, "지원서를 찾을 수 없습니다"),
    
    // 409 Conflict
    DUPLICATE_APPLICATION("LAB_APP_409", HttpStatus.CONFLICT, "이미 해당 랩실에 지원한 이력이 있습니다"),
    
    // 422 Unprocessable Entity
    CANNOT_CHANGE_STATUS_AFTER_DECISION("LAB_APP_422", HttpStatus.UNPROCESSABLE_ENTITY, "이미 처리된 지원서는 상태를 변경할 수 없습니다");
}
```

## LabImage 도메인 ErrorCode  
```java
public enum LabImageErrorCode implements ErrorCode {
    
    // 400 Bad Request
    IMAGE_URL_REQUIRED("LAB_IMG_001", HttpStatus.BAD_REQUEST, "이미지 URL은 필수입니다"),
    INVALID_IMAGE_URL_FORMAT("LAB_IMG_002", HttpStatus.BAD_REQUEST, "올바르지 않은 이미지 URL 형식입니다"),
    
    // 404 Not Found
    LAB_IMAGE_NOT_FOUND("LAB_IMG_404", HttpStatus.NOT_FOUND, "랩실 이미지를 찾을 수 없습니다");
}
```

## 상태 전이 예외 패턴
```java
public class LabApplicationValidationException extends LabApplicationException {
    
    public void validateCanChangeStatus() {
        if (this.status != ApplicationStatus.PENDING) {
            throw new LabApplicationValidationException(
                LabApplicationErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION,
                String.format("현재 상태: %s", this.status)
            );
        }
    }
}
```

## 권한 검증 예외 패턴
```java
public class InsufficientPermissionException extends LabApplicationException {
    
    public InsufficientPermissionException(String action) {
        super(LabApplicationErrorCode.INSUFFICIENT_PERMISSION_FOR_APPROVAL,
              String.format("%s 권한이 없습니다", action));
    }
}
```

## 비즈니스 규칙 예외 패턴
```java
public class DuplicateApplicationException extends LabApplicationException {
    
    public DuplicateApplicationException(Long labId, Long userId) {
        super(LabApplicationErrorCode.DUPLICATE_APPLICATION,
              String.format("사용자 %d가 랩실 %d에 이미 지원했습니다", userId, labId));
    }
}
```

## 도메인 이벤트와 예외
- 상태 전이 실패 시 예외 발생
- 도메인 규칙 위반 시 즉시 예외 발생
- 예외 발생 후 롤백 처리

## 테스트 패턴
```java
@Test 
void 중복_지원시_예외_발생() {
    // given
    Long labId = 1L;
    Long userId = 1L;
    
    // when & then
    assertThatThrownBy(() -> 
        new DuplicateApplicationException(labId, userId)
    ).isInstanceOf(LabApplicationException.class)
     .hasMessageContaining("이미 지원했습니다");
}
```