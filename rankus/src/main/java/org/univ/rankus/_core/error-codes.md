# ErrorCode 중앙 관리 (AI 전용)

> ErrorCode 할당 및 관리를 위한 중앙 집중식 가이드 (100줄 이하)

## 🎯 Prefix 할당 현황

| 도메인                | Prefix | 할당 범위   | 현재 사용 | 예시             |
|--------------------|--------|---------|---------|----------------|
| User               | `USER` | 001~099 | 001~010 | USER_001       |
| Lab                | `LAB`  | 001~099 | 001~015 | LAB_001        |
| LabApplication     | `LAP`  | 001~099 | 001~012 | LAP_001        |
| LabImage           | `LIM`  | 001~099 | 001~008 | LIM_001        |
| LabCreationRequest | `LCR`  | 001~099 | 001~010 | LCR_001        |
| Notice             | `NOT`  | 001~099 | 미할당     | NOT_001 (예약) |
| Ranking            | `RNK`  | 001~099 | 미할당     | RNK_001 (예약) |

## 📊 HTTP 상태별 코드 할당 규칙

| HTTP 상태 | 코드 범위   | 용도         | 네이밍 패턴                    |
|---------|---------|------------|---------------------------|
| 400     | 001~099 | 입력값 검증 오류  | `{FIELD}_REQUIRED`        |
| 401     | 401     | 인증 실패      | `INVALID_CREDENTIALS`     |
| 403     | 403     | 권한 부족      | `INSUFFICIENT_PERMISSION` |
| 404     | 404     | 리소스 없음     | `{DOMAIN}_NOT_FOUND`      |
| 409     | 409     | 충돌/중복      | `{FIELD}_DUPLICATED`      |
| 422     | 422     | 비즈니스 규칙 위반 | `CANNOT_CHANGE_STATUS`    |

## 🔤 ErrorCode 네이밍 패턴

### 필수 검증 오류 (400)
```java
{FIELD}_REQUIRED        // 필수값 누락
{FIELD}_TOO_LONG        // 길이 초과
{FIELD}_TOO_SHORT       // 길이 부족
{FIELD}_INVALID_FORMAT  // 형식 오류
{FIELD}_OUT_OF_RANGE    // 범위 초과
```

### 비즈니스 규칙 오류 (422)
```java
CANNOT_CHANGE_STATUS           // 상태 변경 불가
CANNOT_DELETE_REFERENCED       // 참조된 리소스 삭제 불가
CANNOT_APPROVE_OWN_REQUEST     // 본인 요청 승인 불가
ALREADY_APPLIED                // 이미 지원함
DEADLINE_PASSED                // 마감일 지나감
```

### 조회 실패 (404)
```java
{DOMAIN}_NOT_FOUND      // 도메인 엔티티 없음
{FIELD}_NOT_FOUND       // 특정 필드 기준 조회 실패
```

### 중복 오류 (409)
```java
{FIELD}_DUPLICATED      // 중복 생성
{FIELD}_ALREADY_EXISTS  // 이미 존재함
```

## 🏗️ ErrorCode Enum 템플릿

```java
public enum {Domain}ErrorCode implements ErrorCode {
    
    // 400 Bad Request - 입력값 검증
    {FIELD}_REQUIRED("{PREFIX}_001", HttpStatus.BAD_REQUEST, "{필드}는 필수입니다"),
    {FIELD}_TOO_LONG("{PREFIX}_002", HttpStatus.BAD_REQUEST, "{필드}는 {MAX}자 이하여야 합니다"),
    {FIELD}_INVALID_FORMAT("{PREFIX}_003", HttpStatus.BAD_REQUEST, "{필드} 형식이 올바르지 않습니다"),
    
    // 404 Not Found - 조회 실패
    {DOMAIN}_NOT_FOUND("{PREFIX}_404", HttpStatus.NOT_FOUND, "{도메인}을(를) 찾을 수 없습니다"),
    
    // 409 Conflict - 중복
    {FIELD}_DUPLICATED("{PREFIX}_409", HttpStatus.CONFLICT, "이미 사용 중인 {필드}입니다"),
    
    // 422 Unprocessable Entity - 비즈니스 규칙
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

## 📋 코드 할당 체크리스트

### 새 도메인 추가 시
```
□ Prefix 중복 확인
□ 코드 범위 할당 (001~099)
□ 네이밍 패턴 준수
□ 메시지 일관성 확인
```

### 새 ErrorCode 추가 시
```
□ HTTP 상태코드 매핑 확인
□ 동일 도메인 내 코드 중복 확인
□ 메시지 한국어, 명사형 종결
□ 필드명 일관성 확인
```

## 🔧 메시지 작성 규칙

### 필수 규칙
- **언어**: 한국어
- **종결어미**: 명사형 ("~습니다", "~입니다")
- **구체성**: 최대/최소값 명시
- **일관성**: 동일 도메인 내 톤 통일

### 예시
```java
// ✅ 올바른 메시지
"이름은 필수입니다"
"이름은 10자 이하여야 합니다"
"이메일 형식이 올바르지 않습니다"

// ❌ 잘못된 메시지
"이름을 입력해주세요"        // 명령형
"Name is required"         // 영어
"이름이 너무 길어요"          // 구어체
```

---

**업데이트**: 2025-01-04 | **라인 수**: 98줄 | **목적**: ErrorCode 중복 방지 및 일관성 확보