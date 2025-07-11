# Ranking Domain Layer 가이드

> 점수 신청 및 랭킹 시스템의 도메인 모델과 비즈니스 로직

## 🎯 Domain Layer 핵심 가이드 (AI 전용)

> 점수 신청 및 랭킹 시스템의 도메인 모델과 비즈니스 로직 (100줄 이하)

## 🏛️ 핵심 Aggregate 구조

### ScoreSubmission Aggregate (Root)

```
ScoreSubmission (Root) → ScoreCategory (Enum) → SubmissionStatus (Enum) → VisibilityLevel (Enum)
                      → RankingValidationException → RankingErrorCode
                      → RankingPolicy, ScoreSubmissionPolicy, DuplicateCheckPolicy
```

## 📊 엔티티 매트릭스

| 엔티티                 | 핵심 비즈니스 메서드                                                                  | 상태 전이                       |
|---------------------|------------------------------------------------------------------------------|-----------------------------|
| **ScoreSubmission** | `approve()`, `reject()`, `correctStatus()`, `canBeApproved()`, `isOwnedBy()` | PENDING → APPROVED/REJECTED |

## 💎 Enum 및 Value Object

### ScoreCategory Enum (점수 체계)

```java
// 학업 성과
ACADEMIC_ACHIEVEMENT(5), SCHOLARSHIP(15), GPA_MAINTENANCE(10)

// 대회 수상  
CONTEST_INTERNAL_WINNER(30), CONTEST_EXTERNAL_WINNER(50)

// 자격증
CERTIFICATION_NATIONAL(20), CERTIFICATION_INTERNATIONAL(25), CERTIFICATION_PRIVATE(10)

// 연구 성과
RESEARCH_SCI_PAPER(100), RESEARCH_GENERAL_PAPER(50), RESEARCH_PATENT(30)
```

### 핵심 Enum 정의

```java
SubmissionStatus: PENDING → APPROVED/REJECTED (canTransitionTo() 메서드)
VisibilityLevel: PUBLIC, LAB_ONLY, PRIVATE (공개 범위 제어)
DuplicateWarningLevel: NONE, DATE_DUPLICATE, EXACT_DUPLICATE
```

## 🔄 비즈니스 로직 매트릭스

### 승인 권한 체계

| 역할              | 권한 범위  | 제약 사항       |
|-----------------|--------|-------------|
| **ADMIN**       | 모든 랩실  | 본인 신청 승인 불가 |
| **PROFESSOR**   | 모든 랩실  | 본인 신청 승인 불가 |
| **LAB_LEADER**  | 소속 랩실만 | 본인 신청 승인 불가 |
| **LAB_MANAGER** | 소속 랩실만 | 본인 신청 승인 불가 |

### 중복 검사 정책

| 중복 유형          | 처리 방식    | UI 표시            |
|----------------|----------|------------------|
| **날짜 중복**      | 경고 표시    | 노란색 경고 + 2순위 배치  |
| **날짜+카테고리 중복** | 완전 중복 경고 | 빨간색 테두리 + 1순위 배치 |
| **완전 중복**      | 신청 불가    | 오류 메시지 표시        |

### 점수 정정 규칙

| 정정 유형       | 허용 조건             | 제약 사항       |
|-------------|-------------------|-------------|
| **승인 → 거부** | 언제든지 가능           | 정정 횟수 1회 제한 |
| **거부 → 승인** | 이전 정정 내역 없을 때만 가능 | 정정 횟수 1회 제한 |

## 🛡️ 도메인 불변 조건

### ScoreSubmission 불변 조건

1. **필수 필드**: user, lab, category, achievementDescription, achievementDate, proofFileUrl, visibility
2. **날짜 제약**: 취득일자는 미래일 수 없음
3. **길이 제약**: 성과 내용 500자, 신청 사유 200자 이하
4. **상태 전이**: PENDING 상태에서만 승인/거부 가능
5. **권한 제약**: 본인 신청은 승인 불가
6. **만료 제약**: 신청 후 6개월 이내에만 처리 가능
7. **정정 제약**: 최대 1회만 정정 가능

## 📋 도메인 정책 클래스

### RankingPolicy

```java
// 랭킹 계산 정책
calculateLabTotalScore(List<ScoreSubmission>): 랩실 총점 계산
calculateUserContribution(userId, labId, submissions): 사용자 기여도 계산
calculateRankWithTies(totalScore, sortedScores): 동점 처리 순위 계산
```

### ScoreSubmissionPolicy

```java
// 점수 신청 정책
canApprove(User, ScoreSubmission): 승인 권한 확인
canCorrect(ScoreSubmission): 정정 가능 여부
shouldAutoReject(ScoreSubmission): 자동 거부 여부
```

### DuplicateCheckPolicy

```java
// 중복 검사 정책
checkDuplicates(userId, date, category, submissions): 중복 검사 실행
determineDuplicateWarningLevel(result): 경고 레벨 결정
isDuplicateAllowed(result): 중복 허용 여부
```

## 🚨 예외 처리 매트릭스

### RankingErrorCode (주요 에러 코드)

| 에러 코드                             | HTTP 상태 | 상황          |
|-----------------------------------|---------|-------------|
| **USER_REQUIRED**                 | 400     | 사용자 필수 값 누락 |
| **ACHIEVEMENT_DATE_FUTURE**       | 400     | 취득일자 미래 입력  |
| **INSUFFICIENT_PERMISSION**       | 403     | 승인 권한 없음    |
| **CANNOT_APPROVE_OWN_SUBMISSION** | 403     | 본인 신청 승인 시도 |
| **INVALID_STATUS_TRANSITION**     | 409     | 잘못된 상태 전이   |
| **CORRECTION_LIMIT_EXCEEDED**     | 409     | 정정 횟수 초과    |
| **FILE_SIZE_EXCEEDED**            | 422     | 파일 크기 초과    |

## 🔗 연관관계 매트릭스

| 관계                     | 주인              | 대상   | 매핑              | 제약       |
|------------------------|-----------------|------|-----------------|----------|
| ScoreSubmission ↔ User | ScoreSubmission | User | @ManyToOne LAZY | 신청자 관계   |
| ScoreSubmission ↔ Lab  | ScoreSubmission | Lab  | @ManyToOne LAZY | 신청 랩실 관계 |

## 🎯 핵심 사용 패턴

### 점수 신청 생성

```java
ScoreSubmission submission = new ScoreSubmission(
    user, lab, category, description, date, 
    fileUrl, reason, link, visibility
);
```

### 점수 승인 처리

```java
if (submission.canUserApprove(approver)) {
    submission.approve(approver.getId());
}
```

### 중복 검사 실행

```java
DuplicateCheckResult result = duplicatePolicy.checkDuplicates(
    userId, date, category, existingSubmissions
);
```

---

**참조**: 기존 도메인 패턴은 @domain/CLAUDE.md 참조  
**업데이트**: 2025-01-09 | **구현 완료**: ScoreSubmission Aggregate 전체