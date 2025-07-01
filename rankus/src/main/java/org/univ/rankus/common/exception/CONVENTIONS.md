# Exception 컨벤션

> 📋 **ErrorCode 인터페이스**: @core/conventions.md#error-패턴  
> 🔄 **계층별 처리**: @core/http-matrix.md#exception-플로우  
> 📋 **표준 템플릿**: @core/patterns.md

## 🎯 핵심 구조

### 네이밍 매트릭스

| 타입        | 패턴                            | 예시                                                 |
|-----------|-------------------------------|----------------------------------------------------|
| 베이스       | `{Domain}Exception`           | `UserException`, `LabException`                    |
| 구체적       | `{Domain}{Specific}Exception` | `UserNotFoundException`, `UserValidationException` |
| ErrorCode | `{Domain}ErrorCode`           | `UserErrorCode`, `LabApplicationErrorCode`         |
| 글로벌       | `GlobalErrorCode`             | 공통 HTTP 에러                                         |

### 계층 구조

```java
BaseCustomException
├── UserException
│   ├── UserNotFoundException
│   ├── UserValidationException
│   └── DuplicateEmailException
└── LabApplicationException
    ├── LabApplicationNotFoundException
    └── InvalidApplicationStatusException
```

## 🏗️ 기본 템플릿

### BaseCustomException

> 📋 **표준 템플릿**: @core/patterns.md#basecustomexception

### 도메인 베이스 예외

```java
public abstract class {Domain}Exception extends BaseCustomException {
    protected {Domain}Exception(ErrorCode errorCode) { super(errorCode); }
    protected {Domain}Exception(ErrorCode errorCode, String message) { super(errorCode, message); }
}
```

### 구체적 예외

```java
public class {Domain}NotFoundException extends {Domain}Exception {
    public {Domain}NotFoundException() { super({Domain}ErrorCode.{DOMAIN}_NOT_FOUND); }
    public {Domain}NotFoundException(Long id) { 
        super({Domain}ErrorCode.{DOMAIN}_NOT_FOUND, "ID: " + id); 
    }
}
```

## 🏷️ ErrorCode 구현

### ErrorCode 구현

> 📋 **표준 템플릿**: @core/patterns.md#errorcode-enum

## 📋 에러 응답 구조

### 에러 응답 구조

> 📋 **표준 템플릿**: @core/patterns.md#errorresponse

## ⚠️ GlobalExceptionHandler

### 예외 처리 매트릭스

| 예외 타입                             | HTTP 상태 | 로그 레벨 | 처리 방법                   |
|-----------------------------------|---------|-------|-------------------------|
| `BaseCustomException`             | 도메인별    | ERROR | ErrorResponse 변환        |
| `MethodArgumentNotValidException` | 400     | WARN  | ValidationErrorResponse |
| `AccessDeniedException`           | 403     | WARN  | 접근 거부 메시지               |
| `Exception`                       | 500     | ERROR | 일반 서버 오류                |

### GlobalExceptionHandler

> 📋 **표준 템플릿**: @core/patterns.md#globalexceptionhandler

## 🧪 테스트 패턴

> 📋 **테스트 가이드**: @core/testing.md#exception-테스트

> 🧪 **테스트 템플릿**: @core/templates.md#예외-테스트

## 🎯 주요 규칙 요약

1. **계층적 구조**: BaseCustomException → 도메인 베이스 → 구체적 예외
2. **명확한 네이밍**: 도메인과 상황 반영한 명명
3. **적절한 상태코드**: HTTP 상태와 비즈니스 상황 일치
4. **일관된 응답**: ErrorResponse 표준화
5. **로깅 전략**: ERROR(서버오류), WARN(비즈니스예외)
6. **테스트 완비**: 예외 생성과 처리 모두 테스트