# ErrorCode 중앙 관리 (AI 코딩용)

## 📊 Prefix 할당

| 도메인                | Prefix        | 현재 사용       | 예시              |
|--------------------|---------------|-------------|-----------------|
| User               | `USER`        | 001~010     | USER_001        |
| Lab                | `LAB`         | 001~015     | LAB_001         |
| LabApplication     | `LAP`         | 001~012     | LAP_001         |
| LabImage           | `LIM`         | 001~008     | LIM_001         |
| LabCreationRequest | `LCR`         | 001~010     | LCR_001         |
| **Notice**         | **`LNT`**     | **001~007** | **LNT_001**     |
| **Interview**      | **`INT`**     | **001~034** | **INT_001**     |
| **Ranking**        | **`RANKING`** | **001~023** | **RANKING_001** |
| **Attendance**     | **`ATT`**     | **001~022** | **ATT_001**     |

## 🎯 ErrorCode 템플릿

```java
public enum {Domain}ErrorCode implements

ErrorCode {
    // 400 입력값 검증
    {
        FIELD
    } _REQUIRED("{PREFIX}_001", BAD_REQUEST, "{필드}는 필수입니다"),
            {FIELD} _TOO_LONG("{PREFIX}_002", BAD_REQUEST, "{필드}는 {MAX}자 이하여야 합니다"),

            // 403 권한 오류
            INSUFFICIENT_PERMISSION_FOR_ {
        DOMAIN
    } ("{PREFIX}_403", FORBIDDEN, "{도메인}에 대한 권한이 없습니다"),
    NOT_ {
        DOMAIN
    } _AUTHOR("{PREFIX}_403_AUTH", FORBIDDEN, "본인이 작성한 {도메인}이 아닙니다"),

            // 404 조회 실패
            {DOMAIN} _NOT_FOUND("{PREFIX}_404", NOT_FOUND, "{도메인}을 찾을 수 없습니다"),

            // 409 중복
            {FIELD} _DUPLICATED("{PREFIX}_409", CONFLICT, "이미 사용 중인 {필드}입니다"),

            // 422 비즈니스 규칙
            CANNOT_CHANGE_STATUS("{PREFIX}_422", UNPROCESSABLE_ENTITY, "현재 상태에서는 변경할 수 없습니다");
}
```

## 📝 Notice ErrorCode 예시

```java
// LNT_001~007: 입력값 검증
TITLE_REQUIRED("LNT_001",BAD_REQUEST, "공지사항 제목은 필수입니다"),

CONTENT_TOO_LONG("LNT_004",BAD_REQUEST, "공지사항 내용은 2000자를 초과할 수 없습니다"),

// LNT_403: 권한 오류
INSUFFICIENT_PERMISSION_FOR_NOTICE("LNT_403",FORBIDDEN, "공지사항에 대한 권한이 없습니다"),

NOT_NOTICE_AUTHOR("LNT_403_AUTH",FORBIDDEN, "본인이 작성한 공지사항이 아닙니다"),

// LNT_404: 조회 실패
NOTICE_NOT_FOUND("LNT_404",NOT_FOUND, "공지사항을 찾을 수 없습니다")
```

## 📝 Interview ErrorCode 예시

```java
// INT_001~013: 입력값 검증
DATE_REQUIRED("INT_001",BAD_REQUEST, "면접 시작일과 종료일은 필수입니다"),

INVALID_DURATION("INT_004",BAD_REQUEST, "면접 소요 시간은 1분 이상이어야 합니다"),

ALREADY_ACTIVATED("INT_008",BAD_REQUEST, "이미 활성화된 면접입니다"),

// INT_019~024: 슬롯 상태 관련
SLOT_FULL("INT_019",BAD_REQUEST, "면접 슬롯이 가득 찼습니다"),

SLOT_CANCELLED("INT_020",BAD_REQUEST, "취소된 면접 슬롯입니다"),

// INT_028~029: 권한 오류
UNAUTHORIZED_INTERVIEW_ACCESS("INT_028",UNAUTHORIZED, "면접 관리 권한이 없습니다"),

// INT_030~032: 조회 실패
INTERVIEW_NOT_FOUND("INT_030",NOT_FOUND, "해당 면접을 찾을 수 없습니다"),

SLOT_NOT_FOUND("INT_031",NOT_FOUND, "해당 면접 슬롯을 찾을 수 없습니다"),

// INT_033~034: 중복/충돌 오류
DUPLICATE_INTERVIEW("INT_033",CONFLICT, "해당 랩실에 이미 활성화된 면접이 있습니다"),

SLOT_TIME_CONFLICT("INT_034",CONFLICT, "중복된 시간대의 면접 슬롯이 있습니다")
```

## 📝 Ranking ErrorCode 예시

```java
// RANKING_001~011: 입력값 검증
USER_REQUIRED("RANKING_001",BAD_REQUEST, "사용자는 필수입니다"),

ACHIEVEMENT_DESCRIPTION_TOO_LONG("RANKING_005",BAD_REQUEST, "성과 내용은 500자 이하여야 합니다"),

ACHIEVEMENT_DATE_FUTURE("RANKING_007",BAD_REQUEST, "취득일자는 미래일 수 없습니다"),

// RANKING_012~014: 권한 오류
INSUFFICIENT_PERMISSION("RANKING_012",FORBIDDEN, "점수 승인 권한이 없습니다"),

CANNOT_APPROVE_OWN_SUBMISSION("RANKING_014",FORBIDDEN, "본인이 신청한 점수는 승인할 수 없습니다"),

// RANKING_015: 조회 실패
SUBMISSION_NOT_FOUND("RANKING_015",NOT_FOUND, "해당 점수 신청을 찾을 수 없습니다"),

// RANKING_016~020: 비즈니스 로직 충돌
INVALID_STATUS_TRANSITION("RANKING_016",CONFLICT, "점수 상태 변경이 불가능합니다"),

CORRECTION_LIMIT_EXCEEDED("RANKING_019",CONFLICT, "정정 가능 횟수를 초과했습니다"),

// RANKING_021~023: 파일 업로드 오류
FILE_SIZE_EXCEEDED("RANKING_021",UNPROCESSABLE_ENTITY, "파일 크기는 10MB 이하여야 합니다"),

UNSUPPORTED_FILE_TYPE("RANKING_022",UNPROCESSABLE_ENTITY, "지원하지 않는 파일 형식입니다")
```

## 📝 Attendance ErrorCode 예시

```java
// ATT_001~010: 입력값 검증
TITLE_REQUIRED("ATT_001",BAD_REQUEST, "출석 세션 제목은 필수입니다"),

QR_VALIDITY_INVALID("ATT_004",BAD_REQUEST, "QR 코드 유효시간은 1~10분 사이여야 합니다"),

SESSION_NOT_ACTIVE("ATT_006",BAD_REQUEST, "활성화된 출석 세션이 아닙니다"),

ALREADY_CHECKED_IN("ATT_007",BAD_REQUEST, "이미 출석 체크되었습니다"),

QR_TOKEN_EXPIRED("ATT_008",BAD_REQUEST, "QR 코드가 만료되었습니다"),

// ATT_403: 권한 오류
INSUFFICIENT_PERMISSION_FOR_ATTENDANCE("ATT_403",FORBIDDEN, "출석 관리 권한이 없습니다"),

// ATT_404: 조회 실패
SESSION_NOT_FOUND("ATT_404",NOT_FOUND, "출석 세션을 찾을 수 없습니다"),

// ATT_409: 중복 오류
DUPLICATE_SESSION("ATT_409",CONFLICT, "이미 진행 중인 출석 세션이 있습니다"),

// ATT_422: 비즈니스 규칙 위반
CANNOT_END_INACTIVE_SESSION("ATT_422",UNPROCESSABLE_ENTITY, "비활성화된 세션은 종료할 수 없습니다")
```

## ✅ 체크리스트

- [ ] Prefix 중복 확인
- [ ] HTTP 상태코드 매핑
- [ ] 메시지 한국어 명사형
- [ ] 실제 구현과 문서 일치

**업데이트**: 2025-01-05 | **40줄** | AI 코딩 최적화