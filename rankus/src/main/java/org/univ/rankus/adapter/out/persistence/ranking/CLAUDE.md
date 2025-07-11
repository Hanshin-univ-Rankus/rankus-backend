# Ranking Persistence Layer 가이드 (AI 코딩용)

> 점수 신청 및 랭킹 시스템의 Repository 구현 및 JPA 쿼리 패턴

## 🏗️ Repository 구조

```
adapter/out/persistence/
├── impl/ScoreSubmissionRepositoryAdapter.java    # Port 구현체
└── jpa/SpringDataScoreSubmissionRepository.java  # JPA Repository
```

## 📊 ScoreSubmissionRepositoryAdapter 패턴

### 기본 구현 구조

```java
@Component
@RequiredArgsConstructor
public class ScoreSubmissionRepositoryAdapter implements ScoreSubmissionRepositoryPort {
    
    private final SpringDataScoreSubmissionRepository springDataScoreSubmissionRepository;
    
    // 기본 CRUD는 직접 위임
    @Override
    public ScoreSubmission save(ScoreSubmission submission) {
        return springDataScoreSubmissionRepository.save(submission);
    }
    
    // 복잡한 쿼리는 JPA Repository에서 구현 후 위임
    @Override
    public Integer sumScoresByLabIdAndStatus(Long labId, SubmissionStatus status) {
        return springDataScoreSubmissionRepository.sumScoresByLabIdAndStatus(labId, status);
    }
}
```

## 🔍 SpringDataScoreSubmissionRepository 특화 쿼리

### 1. 점수 집계 쿼리 (핵심)

```java
@Query("SELECT COALESCE(SUM(CASE " +
       "WHEN s.category = 'RESEARCH_SCI_PAPER' THEN 100 " +
       "WHEN s.category = 'CONTEST_EXTERNAL_WINNER' THEN 50 " +
       "WHEN s.category = 'RESEARCH_GENERAL_PAPER' THEN 50 " +
       "WHEN s.category = 'CONTEST_EXTERNAL_RUNNER_UP' THEN 35 " +
       "WHEN s.category = 'CONTEST_INTERNAL_WINNER' THEN 30 " +
       "WHEN s.category = 'RESEARCH_PATENT' THEN 30 " +
       "WHEN s.category = 'CERTIFICATION_INTERNATIONAL' THEN 25 " +
       "WHEN s.category = 'CERTIFICATION_NATIONAL' THEN 20 " +
       "WHEN s.category = 'CONTEST_INTERNAL_RUNNER_UP' THEN 20 " +
       "WHEN s.category = 'SCHOLARSHIP' THEN 15 " +
       "WHEN s.category = 'CERTIFICATION_PRIVATE' THEN 10 " +
       "WHEN s.category = 'EDUCATION_COMPLETION' THEN 10 " +
       "WHEN s.category = 'EXTRACURRICULAR_ACTIVITY' THEN 10 " +
       "WHEN s.category = 'GPA_MAINTENANCE' THEN 10 " +
       "WHEN s.category = 'ACADEMIC_ACHIEVEMENT' THEN 5 " +
       "WHEN s.category = 'SEMINAR_PARTICIPATION' THEN 5 " +
       "WHEN s.category = 'VOLUNTEER_ACTIVITY' THEN 5 " +
       "ELSE 0 END), 0) FROM ScoreSubmission s " +
       "WHERE s.lab.id = :labId AND s.status = :status")
Integer sumScoresByLabIdAndStatus(@Param("labId") Long labId, @Param("status") SubmissionStatus status);
```

**⚠️ 중요**: enum 메서드 호출은 HQL에서 지원되지 않으므로 명시적 CASE 문 사용

### 2. 중복 검사 쿼리

```java
// 날짜별 중복 검사
List<ScoreSubmission> findByUserIdAndAchievementDateOrderBySubmittedAtDesc(
    Long userId, LocalDate achievementDate);

// 완전 중복 검사 (날짜 + 카테고리)
List<ScoreSubmission> findByUserIdAndAchievementDateAndCategoryOrderBySubmittedAtDesc(
    Long userId, LocalDate achievementDate, ScoreCategory category);

// DB 레벨 중복 확인
boolean existsByUserIdAndLabIdAndAchievementDateAndCategory(
    Long userId, Long labId, LocalDate achievementDate, ScoreCategory category);
```

### 3. 만료 처리 쿼리

```java
// 만료된 신청 조회
List<ScoreSubmission> findByStatusAndExpiresAtBeforeOrderBySubmittedAtDesc(
    SubmissionStatus status, LocalDateTime expiresAt);

// 만료 예정 신청 조회 (알림용)
List<ScoreSubmission> findByStatusAndExpiresAtBetweenOrderBySubmittedAtDesc(
    SubmissionStatus status, LocalDateTime from, LocalDateTime to);
```

### 4. 페이징 조회 쿼리

```java
// 사용자별 페이징
Page<ScoreSubmission> findByUserIdOrderBySubmittedAtDesc(Long userId, Pageable pageable);

// 랩실별 페이징
Page<ScoreSubmission> findByLabIdOrderBySubmittedAtDesc(Long labId, Pageable pageable);

// 상태별 페이징
Page<ScoreSubmission> findByStatusOrderBySubmittedAtDesc(SubmissionStatus status, Pageable pageable);

// 복합 조건 페이징
Page<ScoreSubmission> findByLabIdAndStatusOrderBySubmittedAtDesc(
    Long labId, SubmissionStatus status, Pageable pageable);
```

## 🎯 Repository Port 매트릭스

### ScoreSubmissionRepositoryPort 메서드

| 카테고리   | 메서드                                                     | 반환 타입                     | 용도          |
|--------|---------------------------------------------------------|---------------------------|-------------|
| **기본** | `save(submission)`                                      | ScoreSubmission           | 저장/업데이트     |
| **기본** | `findById(id)`                                          | Optional<ScoreSubmission> | ID 기반 조회    |
| **조회** | `findByUserIdOrderBySubmittedAtDesc()`                  | Page<ScoreSubmission>     | 사용자별 페이징 조회 |
| **조회** | `findByLabIdOrderBySubmittedAtDesc()`                   | Page<ScoreSubmission>     | 랩실별 페이징 조회  |
| **조회** | `findByStatusOrderBySubmittedAtDesc()`                  | Page<ScoreSubmission>     | 상태별 페이징 조회  |
| **중복** | `findByUserIdAndAchievementDate()`                      | List<ScoreSubmission>     | 날짜 중복 검사    |
| **중복** | `existsByUserIdAndLabIdAndAchievementDateAndCategory()` | boolean                   | DB 중복 확인    |
| **만료** | `findByStatusAndExpiresAtBefore()`                      | List<ScoreSubmission>     | 만료 처리       |
| **집계** | `sumScoresByLabIdAndStatus()`                           | Integer                   | 랩실 총점 계산    |
| **집계** | `sumScoresByUserIdAndLabIdAndStatus()`                  | Integer                   | 사용자 기여도 계산  |

## 🔧 JPA 쿼리 작성 가이드

### DO: 올바른 쿼리 패턴

```java
// ✅ 명시적 CASE 문으로 점수 계산
@Query("SELECT COALESCE(SUM(CASE WHEN s.category = 'RESEARCH_SCI_PAPER' THEN 100 ELSE 0 END), 0)")

// ✅ 연관관계 조인 활용  
@Query("SELECT s FROM ScoreSubmission s WHERE s.user.id = :userId")

// ✅ 파라미터 바인딩
List<ScoreSubmission> findByStatusAndExpiresAtBefore(@Param("status") SubmissionStatus status)

// ✅ 정렬 명시
List<ScoreSubmission> findByUserIdOrderBySubmittedAtDesc(Long userId)
```

### DON'T: 피해야 할 패턴

```java
// ❌ enum 메서드 호출 (HQL 미지원)
@Query("SELECT SUM(s.category.getDefaultScore()) FROM ScoreSubmission s")

// ❌ 복잡한 JOIN 없이 N+1 유발
@Query("SELECT s.user.name FROM ScoreSubmission s")  // user가 LAZY로딩

// ❌ 하드코딩된 값
@Query("SELECT s FROM ScoreSubmission s WHERE s.status = 'APPROVED'")

// ❌ 정렬 누락으로 인한 불안정한 페이징
Page<ScoreSubmission> findByLabId(Long labId, Pageable pageable)
```

## 🚨 성능 최적화 팁

### 1. 인덱스 활용

```sql
-- 자주 사용되는 조회 조건에 복합 인덱스
CREATE INDEX idx_score_submission_user_status ON score_submissions(user_id, status);
CREATE INDEX idx_score_submission_lab_status ON score_submissions(lab_id, status);
CREATE INDEX idx_score_submission_expires_at ON score_submissions(expires_at);
```

### 2. 배치 처리

```java
// 대량 업데이트는 배치로 처리
@Modifying
@Query("UPDATE ScoreSubmission s SET s.status = :newStatus WHERE s.status = :oldStatus AND s.expiresAt < :now")
int updateExpiredSubmissions(@Param("newStatus") SubmissionStatus newStatus, 
                           @Param("oldStatus") SubmissionStatus oldStatus,
                           @Param("now") LocalDateTime now);
```

### 3. 페이징 최적화

```java
// 카운트 쿼리 분리로 성능 향상
@Query(value = "SELECT s FROM ScoreSubmission s WHERE s.lab.id = :labId",
       countQuery = "SELECT count(s) FROM ScoreSubmission s WHERE s.lab.id = :labId")
Page<ScoreSubmission> findByLabIdOptimized(@Param("labId") Long labId, Pageable pageable);
```

## 🧪 테스트 패턴

### Repository 테스트 구조

```java
@DataJpaTest
class ScoreSubmissionRepositoryAdapterTest {
    
    @Autowired TestEntityManager entityManager;
    @Autowired SpringDataScoreSubmissionRepository springDataRepository;
    
    private ScoreSubmissionRepositoryAdapter repositoryAdapter;
    
    @BeforeEach
    void setUp() {
        repositoryAdapter = new ScoreSubmissionRepositoryAdapter(springDataRepository);
    }
    
    @Test
    void 점수_집계_쿼리_정확성_검증() {
        // given: 다양한 카테고리의 점수 신청 생성
        
        // when: 집계 쿼리 실행
        Integer totalScore = repositoryAdapter.sumScoresByLabIdAndStatus(labId, APPROVED);
        
        // then: 예상 점수와 일치 확인
        assertThat(totalScore).isEqualTo(expectedScore);
    }
}
```

## 📋 핵심 구현 체크리스트

- ✅ `@Component` 어노테이션으로 Spring Bean 등록
- ✅ `@RequiredArgsConstructor`로 의존성 주입
- ✅ 모든 포트 메서드에 `@Override` 구현
- ✅ 복잡한 집계 쿼리는 CASE 문으로 구현
- ✅ 정렬 조건 명시로 페이징 안정성 확보
- ✅ 중복 검사를 위한 다양한 조회 메서드 제공
- ✅ 만료 처리를 위한 시간 기반 쿼리 구현

---

**참조**: 기본 Repository 패턴은 `/impl/CONVENTIONS.md` 참조  
**업데이트**: 2025-01-09 | **구현 완료**: ScoreSubmission Repository 전체