# ErrorCode 중앙 관리 (AI 코딩용)

## 📊 Prefix 할당

| 도메인 | Prefix | 현재 사용 | 예시 |
|-------|--------|---------|------|
| User | `USER` | 001~010 | USER_001 |
| Lab | `LAB` | 001~015 | LAB_001 |
| LabApplication | `LAP` | 001~012 | LAP_001 |
| LabImage | `LIM` | 001~008 | LIM_001 |
| LabCreationRequest | `LCR` | 001~010 | LCR_001 |
| **Notice** | **`LNT`** | **001~007** | **LNT_001** |
| **Interview** | **`INT`** | **001~034** | **INT_001** |
| Ranking | `RNK` | 미할당 | RNK_001 |

## 🎯 ErrorCode 템플릿

```java
public enum {Domain}ErrorCode implements ErrorCode {
    // 400 입력값 검증
    {FIELD}_REQUIRED("{PREFIX}_001", BAD_REQUEST, "{필드}는 필수입니다"),
    {FIELD}_TOO_LONG("{PREFIX}_002", BAD_REQUEST, "{필드}는 {MAX}자 이하여야 합니다"),
    
    // 403 권한 오류
    INSUFFICIENT_PERMISSION_FOR_{DOMAIN}("{PREFIX}_403", FORBIDDEN, "{도메인}에 대한 권한이 없습니다"),
    NOT_{DOMAIN}_AUTHOR("{PREFIX}_403_AUTH", FORBIDDEN, "본인이 작성한 {도메인}이 아닙니다"),
    
    // 404 조회 실패
    {DOMAIN}_NOT_FOUND("{PREFIX}_404", NOT_FOUND, "{도메인}을 찾을 수 없습니다"),
    
    // 409 중복
    {FIELD}_DUPLICATED("{PREFIX}_409", CONFLICT, "이미 사용 중인 {필드}입니다"),
    
    // 422 비즈니스 규칙
    CANNOT_CHANGE_STATUS("{PREFIX}_422", UNPROCESSABLE_ENTITY, "현재 상태에서는 변경할 수 없습니다");
}
```

## 📝 Notice ErrorCode 예시

```java
// LNT_001~007: 입력값 검증
TITLE_REQUIRED("LNT_001", BAD_REQUEST, "공지사항 제목은 필수입니다"),
CONTENT_TOO_LONG("LNT_004", BAD_REQUEST, "공지사항 내용은 2000자를 초과할 수 없습니다"),

// LNT_403: 권한 오류
INSUFFICIENT_PERMISSION_FOR_NOTICE("LNT_403", FORBIDDEN, "공지사항에 대한 권한이 없습니다"),
NOT_NOTICE_AUTHOR("LNT_403_AUTH", FORBIDDEN, "본인이 작성한 공지사항이 아닙니다"),

// LNT_404: 조회 실패
NOTICE_NOT_FOUND("LNT_404", NOT_FOUND, "공지사항을 찾을 수 없습니다")
```

## 📝 Interview ErrorCode 예시

```java
// INT_001~013: 입력값 검증
DATE_REQUIRED("INT_001", BAD_REQUEST, "면접 시작일과 종료일은 필수입니다"),
INVALID_DURATION("INT_004", BAD_REQUEST, "면접 소요 시간은 1분 이상이어야 합니다"),
ALREADY_ACTIVATED("INT_008", BAD_REQUEST, "이미 활성화된 면접입니다"),

// INT_019~024: 슬롯 상태 관련
SLOT_FULL("INT_019", BAD_REQUEST, "면접 슬롯이 가득 찼습니다"),
SLOT_CANCELLED("INT_020", BAD_REQUEST, "취소된 면접 슬롯입니다"),

// INT_028~029: 권한 오류
UNAUTHORIZED_INTERVIEW_ACCESS("INT_028", UNAUTHORIZED, "면접 관리 권한이 없습니다"),

// INT_030~032: 조회 실패
INTERVIEW_NOT_FOUND("INT_030", NOT_FOUND, "해당 면접을 찾을 수 없습니다"),
SLOT_NOT_FOUND("INT_031", NOT_FOUND, "해당 면접 슬롯을 찾을 수 없습니다"),

// INT_033~034: 중복/충돌 오류
DUPLICATE_INTERVIEW("INT_033", CONFLICT, "해당 랩실에 이미 활성화된 면접이 있습니다"),
SLOT_TIME_CONFLICT("INT_034", CONFLICT, "중복된 시간대의 면접 슬롯이 있습니다")
```

## ✅ 체크리스트

- [ ] Prefix 중복 확인
- [ ] HTTP 상태코드 매핑
- [ ] 메시지 한국어 명사형
- [ ] 실제 구현과 문서 일치

**업데이트**: 2025-01-05 | **40줄** | AI 코딩 최적화