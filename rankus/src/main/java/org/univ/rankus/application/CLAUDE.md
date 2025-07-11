# Application Layer 가이드 (AI 코딩용)

## 🎯 포트/어댑터 패턴

```
Controller → {Domain}CommandUseCase → {Domain}CommandService → {Domain}RepositoryPort
Controller → {Domain}QueryUseCase → {Domain}QueryService → {Domain}RepositoryPort
```

## 🔗 UseCase 템플릿

### Command UseCase (상태 변경)

```java
public interface {Domain}

CommandUseCase {
    {
        Domain
    } create {
        Domain
    } ({Domain} CreateRequestDto request);
    {
        Domain
    } update {
        Domain
    } (Long id, {Domain} UpdateRequestDto request);
    void delete {
        Domain
    } (Long id);
}
```

### Query UseCase (조회)

```java
public interface {Domain}

QueryUseCase {
    {
        Domain
    } find {
        Domain
    } ById(Long id);
    List < {Domain} > findAll {
        Domain
    } s();
    Page < {Domain} > find {
        Domain
    } s(Pageable pageable);
}
```

## 🏗️ Service 구현 패턴

### Command Service

```java
@Service @RequiredArgsConstructor
public class {Domain}CommandService implements{Domain}

CommandUseCase {
    private final {
        Domain
    } RepositoryPort repository;

    @Override @Transactional
    public {
        Domain
    } create {
        Domain
    } ({Domain} CreateRequestDto request){
        {
            Domain
        } entity = {Domain}.create(request.getName());
        return repository.save(entity);
    }
}
```

### Query Service

```java
@Service @RequiredArgsConstructor
public class {Domain}QueryService implements{Domain}

QueryUseCase {
    private final {
        Domain
    } RepositoryPort repository;

    @Override @Transactional(readOnly = true)
    public {
        Domain
    } find {
        Domain
    } ById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new {
            Domain
        } NotFoundException(id));
    }
}
```

## 📝 Notice UseCase 패턴

### LabNoticeCommandUseCase

```java
public interface LabNoticeCommandUseCase {
    LabNotice createNotice(String title, String content, Long authorId, Long labId, NoticeType type, boolean pinned);

    LabNotice updateNotice(Long noticeId, String title, String content, NoticeType type, boolean pinned);

    void deleteNotice(Long noticeId);

    LabNotice togglePinNotice(Long noticeId);
}
```

### LabNoticeQueryUseCase

```java
public interface LabNoticeQueryUseCase {
    Page<LabNotice> getNoticesByLabId(Long labId, Pageable pageable);

    List<LabNotice> getNoticesByLabId(Long labId);

    List<LabNotice> getNoticesByLabIdAndType(Long labId, NoticeType type);

    List<LabNotice> getPinnedNoticesByLabId(Long labId);

    LabNotice getNoticeById(Long noticeId);
}
```

## 📊 구현된 UseCase

| 도메인                 | Command | Query | 상태     |
|---------------------|---------|-------|--------|
| User                | ✅       | ✅     | 완료     |
| Lab                 | ✅       | ✅     | 완료     |
| LabApplication      | ✅       | ✅     | 완료     |
| LabImage            | ✅       | ✅     | 완료     |
| LabCreationRequest  | ✅       | ✅     | 완료     |
| **LabNotice**       | **✅**   | **✅** | **완료** |
| **Interview**       | **✅**   | **✅** | **완료** |
| **ScoreSubmission** | **✅**   | **✅** | **완료** |
| **Ranking**         | **❌**   | **✅** | **완료** |
| Auth                | ✅       | ❌     | 부분     |

## 🏆 Ranking UseCase 패턴

### ScoreSubmissionCommandUseCase

```java
public interface ScoreSubmissionCommandUseCase {
    ScoreSubmission submitScore(Long userId, Long labId, ScoreCategory category, ...);

    void approveSubmission(Long submissionId, Long approverId);

    void rejectSubmission(Long submissionId, Long approverId, String reason);

    void correctSubmissionStatus(Long submissionId, SubmissionStatus newStatus, Long correcterId);

    void deleteSubmission(Long submissionId, Long userId);
}
```

### ScoreSubmissionQueryUseCase

```java
public interface ScoreSubmissionQueryUseCase {
    ScoreSubmission findSubmissionById(Long submissionId);

    Page<ScoreSubmission> findSubmissionsByUserId(Long userId, Pageable pageable);

    Page<ScoreSubmission> findSubmissionsByLabId(Long labId, Pageable pageable);

    DuplicateCheckPolicy.DuplicateCheckResult checkDuplicates(Long userId, LocalDate date, ScoreCategory category);

    int calculateUserScoreInLab(Long userId, Long labId);
}
```

### RankingQueryUseCase

```java
public interface RankingQueryUseCase {
    Page<LabRankingResult> getLabRankings(Pageable pageable);

    LabRankingResult getLabRanking(Long labId);

    List<UserContribution> getTopContributors(Long labId, int limit);

    int calculateLabTotalScore(Long labId);

    int calculateUserContributionInLab(Long userId, Long labId);
}
```

## 🔧 핵심 규칙

- **Entity 반환**: Service는 Domain Entity 반환
- **DTO 변환**: Controller에서 Entity → DTO 변환
- **트랜잭션**: Command(@Transactional), Query(@Transactional(readOnly=true))
- **예외 처리**: orElseThrow로 즉시 예외 발생
- **정책 활용**: 복잡한 비즈니스 로직은 Policy 클래스로 분리

**참조**:

- **Ranking Application**: `/application/ranking/CLAUDE.md`

**업데이트**: 2025-01-09 | **구현 완료**: Ranking 시스템 포함