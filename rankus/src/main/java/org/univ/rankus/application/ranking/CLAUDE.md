# Ranking Application Layer 가이드

> 점수 신청 및 랭킹 시스템의 애플리케이션 서비스 계층

## 🎯 Application Layer 핵심 가이드 (AI 전용)

> 점수 신청 및 랭킹 시스템의 UseCase 및 Service 구현 (100줄 이하)

## 🏗️ UseCase 구조

### ScoreSubmission UseCase

```
ScoreSubmissionCommandUseCase → ScoreSubmissionCommandService
ScoreSubmissionQueryUseCase → ScoreSubmissionQueryService
```

### Ranking UseCase

```
RankingQueryUseCase → RankingQueryService
```

## 📊 Service 매트릭스

| 서비스                               | 트랜잭션                     | 주요 메서드                                                                 | 의존성                                    |
|-----------------------------------|--------------------------|------------------------------------------------------------------------|----------------------------------------|
| **ScoreSubmissionCommandService** | @Transactional           | `submitScore()`, `approveSubmission()`, `rejectSubmission()`           | ScoreSubmissionRepositoryPort + 정책 클래스 |
| **ScoreSubmissionQueryService**   | @Transactional(readOnly) | `findSubmissionById()`, `checkDuplicates()`, `calculateUserScore()`    | ScoreSubmissionRepositoryPort + 정책 클래스 |
| **RankingQueryService**           | @Transactional(readOnly) | `getLabRankings()`, `calculateLabTotalScore()`, `getTopContributors()` | 모든 RepositoryPort + RankingPolicy      |

## 🔄 Command Service 핵심 로직

### ScoreSubmissionCommandService

```java
@Service @RequiredArgsConstructor @Transactional
public class ScoreSubmissionCommandService {
    
    // 점수 신청
    submitScore(userId, labId, category, ...): ScoreSubmission
    
    // 승인/거부
    approveSubmission(submissionId, approverId): void
    rejectSubmission(submissionId, approverId, reason): void
    
    // 정정
    correctSubmissionStatus(submissionId, correcterId): void
    
    // 삭제 (본인만)
    deleteSubmission(submissionId, userId): void
    
    // 자동 만료 처리
    processExpiredSubmissions(): void
}
```

### 권한 검증 플로우

```java
1. 사용자/랩실 존재 확인 (UserNotFoundException, LabNotFoundException)
2. 정책 기반 권한 확인 (ScoreSubmissionPolicy.canApprove())
3. 도메인 로직 실행 (ScoreSubmission.approve/reject())
4. 영속화 (scoreSubmissionRepositoryPort.save())
```

## 🔍 Query Service 핵심 로직

### ScoreSubmissionQueryService

```java
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ScoreSubmissionQueryService {
    
    // 기본 조회
    findSubmissionById(submissionId): ScoreSubmission
    findSubmissionsByUserId(userId, pageable): Page<ScoreSubmission>
    findSubmissionsByLabId(labId, pageable): Page<ScoreSubmission>
    
    // 승인자별 조회
    findPendingSubmissionsForApprover(approverId, pageable): Page<ScoreSubmission>
    
    // 중복 검사
    checkDuplicates(userId, date, category): DuplicateCheckResult
    
    // 점수 계산
    calculateUserScoreInLab(userId, labId): int
    calculateUserTotalScore(userId): int
}
```

### RankingQueryService

```java
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class RankingQueryService {
    
    // 랭킹 조회
    getLabRankings(pageable): Page<LabRankingResult>
    getLabRanking(labId): LabRankingResult
    
    // 기여도 분석
    getTopContributors(labId, limit): List<UserContribution>
    calculateUserContributionInLab(userId, labId): int
    
    // 점수 계산
    calculateLabTotalScore(labId): int
}
```

## 🔗 Repository Port 매트릭스

### ScoreSubmissionRepositoryPort

| 메서드                                | 반환 타입                     | 용도            |
|------------------------------------|---------------------------|---------------|
| `save(submission)`                 | ScoreSubmission           | 저장/업데이트       |
| `findById(id)`                     | Optional<ScoreSubmission> | ID 기반 조회      |
| `findByUserId(userId, pageable)`   | Page<ScoreSubmission>     | 사용자별 조회 (페이징) |
| `findByLabId(labId, pageable)`     | Page<ScoreSubmission>     | 랩실별 조회 (페이징)  |
| `findByUserIdAndAchievementDate()` | List<ScoreSubmission>     | 중복 검사용        |
| `findByStatusAndExpiresAtBefore()` | List<ScoreSubmission>     | 만료 처리용        |
| `sumScoresByLabIdAndStatus()`      | Integer                   | 랩실 총점 계산      |

## 🛡️ 정책 클래스 활용

### ScoreSubmissionPolicy

```java
// 권한 검증
canApprove(User approver, ScoreSubmission submission): boolean

// 정정 가능성 확인
canCorrect(ScoreSubmission submission): boolean
canCorrectApprovalToRejection(submission): boolean
canCorrectRejectionToApproval(submission): boolean

// 만료 처리
shouldAutoReject(ScoreSubmission submission): boolean
```

### DuplicateCheckPolicy

```java
// 중복 검사 실행
checkDuplicates(userId, date, category, existingSubmissions): DuplicateCheckResult

// 중복 경고 레벨 결정
determineDuplicateWarningLevel(result): DuplicateWarningLevel

// 중복 허용 여부
isDuplicateAllowed(result): boolean
```

### RankingPolicy

```java
// 점수 계산
calculateLabTotalScore(submissions): int
calculateUserContribution(userId, labId, submissions): int

// 순위 계산
calculateRankWithTies(totalScore, sortedScores): int
```

## 🚨 예외 처리 패턴

### 공통 예외 처리 플로우

```java
1. 필수 엔티티 존재 확인
   - orElseThrow(() -> new {Domain}NotFoundException(ErrorCode))

2. 권한 검증
   - if (!policy.canApprove()) throw new RankingValidationException()

3. 비즈니스 로직 검증
   - if (!submission.canBeApproved()) throw new ValidationException()

4. 도메인 로직 실행
   - submission.approve() (내부에서 추가 검증)
```

## 🔄 트랜잭션 전략

| 서비스 타입  | 트랜잭션 설정                           | 이유                 |
|---------|-----------------------------------|--------------------|
| Command | `@Transactional`                  | 상태 변경 시 데이터 일관성 보장 |
| Query   | `@Transactional(readOnly = true)` | 읽기 최적화 + 변경 방지     |

## 📋 핵심 사용 패턴

### 점수 신청 생성

```java
ScoreSubmission submission = commandService.submitScore(
    userId, labId, category, description, date, 
    fileUrl, reason, link, visibility
);
```

### 중복 검사

```java
DuplicateCheckResult result = queryService.checkDuplicates(
    userId, achievementDate, category
);
```

### 랭킹 조회

```java
Page<LabRankingResult> rankings = rankingService.getLabRankings(pageable);
```

---

**참조**: 기존 Application 패턴은 @application/CLAUDE.md 참조  
**업데이트**: 2025-01-09 | **구현 완료**: 모든 UseCase 및 Service 구현 완료