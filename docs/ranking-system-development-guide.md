# 🎯 랭킹 시스템 개발 가이드

> **랭킹 시스템 완전 구현 로드맵 - 개발 방향성 유지를 위한 상세 가이드**

## 📋 개발 개요

### 🎯 목표
- 자유 신청 방식 점수 시스템 구현
- 다중 랩실 소속 지원
- 실시간 랭킹 계산 및 표시
- 강력한 중복 방지 시스템
- 체계적인 승인 워크플로우

### 📊 현재 상태
- **기존 기능**: 회원 관리, 랩실 홍보, 지원 시스템, 면접 시스템, 공지사항
- **다음 단계**: 랭킹 시스템 (전체 프로젝트의 약 20% 비중)
- **예상 개발 기간**: 5주 (Phase 1-4)

---

## 🏗️ Phase 1: 핵심 기능 구현 (2주)

### 1️⃣ Week 1: 기본 구조 설계

#### 🔹 ScoreSubmission 엔티티 설계
```java
// 위치: @domain/ranking/ScoreSubmission.java
@Entity
public class ScoreSubmission {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Lab lab;
    
    @Enumerated(EnumType.STRING)
    private ScoreCategory category;
    
    private String achievementDescription;
    private LocalDate achievementDate;
    private String proofFileUrl;
    private String applicationReason;
    private String relatedLink;
    
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status; // PENDING, APPROVED, REJECTED
    
    @Enumerated(EnumType.STRING)
    private VisibilityLevel visibility; // PUBLIC, LAB_ONLY, PRIVATE
    
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime expiresAt; // 6개월 후
    
    private boolean correctionUsed; // 정정 사용 여부
    private int correctionCount; // 정정 횟수 (최대 1회)
}
```

#### 🔹 지원 엔티티들
```java
// ScoreCategory enum
public enum ScoreCategory {
    ACADEMIC_ACHIEVEMENT(5, "학업 성과"),
    SCHOLARSHIP(15, "장학금"),
    GPA_MAINTENANCE(10, "GPA 유지"),
    CONTEST_INTERNAL_WINNER(30, "교내 대회 대상"),
    CONTEST_EXTERNAL_WINNER(50, "교외 대회 대상"),
    CERTIFICATION_NATIONAL(20, "국가 자격증"),
    CERTIFICATION_INTERNATIONAL(25, "국제 자격증"),
    CERTIFICATION_PRIVATE(10, "민간 자격증"),
    EDUCATION_COMPLETION(10, "교육 수료"),
    SEMINAR_PARTICIPATION(5, "세미나 참여"),
    RESEARCH_SCI_PAPER(100, "SCI 논문"),
    RESEARCH_GENERAL_PAPER(50, "일반 논문"),
    RESEARCH_PATENT(30, "특허");
    
    private final int defaultScore;
    private final String displayName;
}

// SubmissionStatus enum
public enum SubmissionStatus {
    PENDING, APPROVED, REJECTED
}

// VisibilityLevel enum
public enum VisibilityLevel {
    PUBLIC, LAB_ONLY, PRIVATE
}
```

#### 🔹 파일 업로드 보안 구현
```java
// 위치: @adapter/web/file/FileController.java
@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @PostMapping("/upload/score-proof")
    public ResponseEntity<FileUploadResponse> uploadScoreProof(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal user) {
        
        // 파일 검증 로직
        validateFile(file);
        
        // S3 업로드 또는 로컬 저장
        String fileUrl = fileService.uploadScoreProof(file, user.getUserId());
        
        return ResponseEntity.ok(new FileUploadResponse(fileUrl));
    }
    
    private void validateFile(MultipartFile file) {
        // 파일 크기 검증 (10MB 이하)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileSizeExceededException();
        }
        
        // 확장자 검증 (PDF, JPG, PNG, GIF)
        String extension = getFileExtension(file.getOriginalFilename());
        if (!Arrays.asList("pdf", "jpg", "jpeg", "png", "gif").contains(extension.toLowerCase())) {
            throw new UnsupportedFileTypeException();
        }
        
        // 파일 내용 검증 (MIME 타입 확인)
        validateMimeType(file);
    }
}
```

### 2️⃣ Week 2: API 구현

#### 🔹 점수 신청 API
```java
// 위치: @adapter/web/ranking/ScoreSubmissionController.java
@RestController
@RequestMapping("/api/score-submissions")
public class ScoreSubmissionController {
    
    @PostMapping
    public ResponseEntity<ScoreSubmissionResponse> submitScore(
            @RequestBody @Valid ScoreSubmissionRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        
        ScoreSubmissionCommand command = ScoreSubmissionCommand.builder()
            .userId(user.getUserId())
            .labId(request.getLabId())
            .category(request.getCategory())
            .achievementDescription(request.getAchievementDescription())
            .achievementDate(request.getAchievementDate())
            .proofFileUrl(request.getProofFileUrl())
            .applicationReason(request.getApplicationReason())
            .relatedLink(request.getRelatedLink())
            .visibility(request.getVisibility())
            .build();
        
        ScoreSubmissionResult result = scoreSubmissionService.submitScore(command);
        
        return ResponseEntity.ok(ScoreSubmissionResponse.from(result));
    }
    
    @GetMapping
    public ResponseEntity<List<ScoreSubmissionResponse>> getMySubmissions(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ScoreSubmission> submissions = scoreSubmissionService
            .getUserSubmissions(user.getUserId(), PageRequest.of(page, size));
        
        return ResponseEntity.ok(submissions.map(ScoreSubmissionResponse::from).getContent());
    }
}
```

#### 🔹 승인/거부 API
```java
@PostMapping("/{submissionId}/approve")
public ResponseEntity<Void> approveSubmission(
        @PathVariable Long submissionId,
        @AuthenticationPrincipal UserPrincipal user) {
    
    ApprovalCommand command = ApprovalCommand.builder()
        .submissionId(submissionId)
        .approverId(user.getUserId())
        .action(ApprovalAction.APPROVE)
        .build();
    
    scoreApprovalService.processApproval(command);
    
    return ResponseEntity.ok().build();
}

@PostMapping("/{submissionId}/reject")
public ResponseEntity<Void> rejectSubmission(
        @PathVariable Long submissionId,
        @RequestBody @Valid RejectionRequest request,
        @AuthenticationPrincipal UserPrincipal user) {
    
    ApprovalCommand command = ApprovalCommand.builder()
        .submissionId(submissionId)
        .approverId(user.getUserId())
        .action(ApprovalAction.REJECT)
        .rejectionReason(request.getReason())
        .build();
    
    scoreApprovalService.processApproval(command);
    
    return ResponseEntity.ok().build();
}
```

---

## 🔍 Phase 2: 사용자 경험 개선 (1주)

### 🔹 중복 검사 로직 구현
```java
// 위치: @application/ranking/ScoreSubmissionService.java
@Service
public class ScoreSubmissionService {
    
    public DuplicateCheckResult checkDuplicates(ScoreSubmissionCommand command) {
        // 날짜 중복 검사
        List<ScoreSubmission> sameDateSubmissions = scoreSubmissionRepository
            .findByUserIdAndAchievementDate(command.getUserId(), command.getAchievementDate());
        
        // 날짜 + 카테고리 중복 검사
        List<ScoreSubmission> exactDuplicates = sameDateSubmissions.stream()
            .filter(s -> s.getCategory() == command.getCategory())
            .collect(Collectors.toList());
        
        return DuplicateCheckResult.builder()
            .hasDateDuplicates(!sameDateSubmissions.isEmpty())
            .hasExactDuplicates(!exactDuplicates.isEmpty())
            .duplicateSubmissions(sameDateSubmissions)
            .build();
    }
    
    public ScoreSubmissionResult submitScore(ScoreSubmissionCommand command) {
        // 중복 검사 실행
        DuplicateCheckResult duplicateCheck = checkDuplicates(command);
        
        // 중복 정보와 함께 저장
        ScoreSubmission submission = ScoreSubmission.builder()
            .user(userRepository.findById(command.getUserId()).orElseThrow())
            .lab(labRepository.findById(command.getLabId()).orElseThrow())
            .category(command.getCategory())
            .achievementDescription(command.getAchievementDescription())
            .achievementDate(command.getAchievementDate())
            .proofFileUrl(command.getProofFileUrl())
            .applicationReason(command.getApplicationReason())
            .relatedLink(command.getRelatedLink())
            .visibility(command.getVisibility())
            .status(SubmissionStatus.PENDING)
            .submittedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMonths(6))
            .correctionUsed(false)
            .correctionCount(0)
            .build();
        
        ScoreSubmission saved = scoreSubmissionRepository.save(submission);
        
        return ScoreSubmissionResult.builder()
            .submission(saved)
            .duplicateCheck(duplicateCheck)
            .build();
    }
}
```

### 🔹 실시간 랭킹 계산
```java
// 위치: @application/ranking/RankingService.java
@Service
public class RankingService {
    
    @Cacheable(value = "labRankings", key = "#labId")
    public LabRankingResult getLabRanking(Long labId) {
        // 랩실의 승인된 점수 총합 계산
        List<ScoreSubmission> approvedSubmissions = scoreSubmissionRepository
            .findByLabIdAndStatus(labId, SubmissionStatus.APPROVED);
        
        int totalScore = approvedSubmissions.stream()
            .mapToInt(s -> s.getCategory().getDefaultScore())
            .sum();
        
        // 전체 랭킹에서의 순위 계산
        List<LabRankingDto> allLabRankings = calculateAllLabRankings();
        int rank = calculateRank(labId, allLabRankings);
        
        return LabRankingResult.builder()
            .labId(labId)
            .totalScore(totalScore)
            .rank(rank)
            .approvedSubmissions(approvedSubmissions)
            .build();
    }
    
    private List<LabRankingDto> calculateAllLabRankings() {
        return labRepository.findAll().stream()
            .map(lab -> {
                int totalScore = scoreSubmissionRepository
                    .findByLabIdAndStatus(lab.getId(), SubmissionStatus.APPROVED)
                    .stream()
                    .mapToInt(s -> s.getCategory().getDefaultScore())
                    .sum();
                
                return LabRankingDto.builder()
                    .labId(lab.getId())
                    .labName(lab.getName())
                    .totalScore(totalScore)
                    .build();
            })
            .sorted(Comparator.comparingInt(LabRankingDto::getTotalScore).reversed())
            .collect(Collectors.toList());
    }
    
    @CacheEvict(value = "labRankings", allEntries = true)
    public void refreshRankings() {
        // 승인/거부 시 캐시 무효화
    }
}
```

---

## ⚡ Phase 3: 운영 기능 (1주)

### 🔹 자동 거부 스케줄링
```java
// 위치: @application/ranking/ScheduledRankingService.java
@Component
public class ScheduledRankingService {
    
    @Scheduled(cron = "0 0 6 * * ?") // 매일 오전 6시
    public void processExpiredSubmissions() {
        LocalDateTime now = LocalDateTime.now();
        
        List<ScoreSubmission> expiredSubmissions = scoreSubmissionRepository
            .findByStatusAndExpiresAtBefore(SubmissionStatus.PENDING, now);
        
        for (ScoreSubmission submission : expiredSubmissions) {
            submission.reject("시스템 자동 거부 (6개월 기한 만료)");
            scoreSubmissionRepository.save(submission);
            
            // 알림 발송 (추후 구현)
            notificationService.sendExpirationNotification(submission);
        }
        
        log.info("Processed {} expired submissions", expiredSubmissions.size());
    }
    
    @Scheduled(cron = "0 0 6 * * ?") // 매일 오전 6시
    public void refreshRankingCache() {
        rankingService.refreshRankings();
        log.info("Ranking cache refreshed");
    }
}
```

### 🔹 점수 정정 시스템
```java
// 위치: @application/ranking/ScoreCorrectionService.java
@Service
public class ScoreCorrectionService {
    
    public CorrectionResult correctSubmission(CorrectionCommand command) {
        ScoreSubmission submission = scoreSubmissionRepository
            .findById(command.getSubmissionId())
            .orElseThrow(() -> new SubmissionNotFoundException());
        
        // 정정 규칙 검증
        validateCorrectionRules(submission, command);
        
        // 정정 실행
        SubmissionStatus previousStatus = submission.getStatus();
        submission.correctStatus(command.getNewStatus(), command.getApproverId());
        
        ScoreSubmission corrected = scoreSubmissionRepository.save(submission);
        
        // 랭킹 캐시 무효화
        rankingService.refreshRankings();
        
        return CorrectionResult.builder()
            .submission(corrected)
            .previousStatus(previousStatus)
            .newStatus(command.getNewStatus())
            .build();
    }
    
    private void validateCorrectionRules(ScoreSubmission submission, CorrectionCommand command) {
        // 정정 횟수 제한 (1회)
        if (submission.getCorrectionCount() >= 1) {
            throw new CorrectionLimitExceededException();
        }
        
        // 승인 → 거절: 언제든지 가능
        if (submission.getStatus() == SubmissionStatus.APPROVED && 
            command.getNewStatus() == SubmissionStatus.REJECTED) {
            return;
        }
        
        // 거절 → 승인: 정정 내역이 없을 때만 가능
        if (submission.getStatus() == SubmissionStatus.REJECTED && 
            command.getNewStatus() == SubmissionStatus.APPROVED) {
            if (submission.isCorrectionUsed()) {
                throw new CorrectionNotAllowedException("이미 정정된 내역은 재승인할 수 없습니다.");
            }
            return;
        }
        
        throw new InvalidCorrectionException();
    }
}
```

---

## 🚀 Phase 4: 성능 최적화 (1주)

### 🔹 배치 처리 전환
```java
// 위치: @application/ranking/BatchRankingService.java
@Service
public class BatchRankingService {
    
    @Transactional
    public void batchUpdateRankings() {
        List<Lab> allLabs = labRepository.findAll();
        
        List<LabRankingEntity> rankings = allLabs.stream()
            .map(this::calculateLabRanking)
            .collect(Collectors.toList());
        
        // 배치 저장
        labRankingRepository.saveAll(rankings);
        
        // 캐시 갱신
        rankings.forEach(ranking -> {
            cacheManager.getCache("labRankings").put(ranking.getLabId(), ranking);
        });
    }
    
    private LabRankingEntity calculateLabRanking(Lab lab) {
        // 복잡한 집계 쿼리 사용
        Integer totalScore = scoreSubmissionRepository
            .sumScoresByLabIdAndStatus(lab.getId(), SubmissionStatus.APPROVED);
        
        return LabRankingEntity.builder()
            .labId(lab.getId())
            .totalScore(totalScore != null ? totalScore : 0)
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
```

### 🔹 데이터베이스 최적화
```sql
-- 인덱스 설정
CREATE INDEX idx_score_submission_user_date ON score_submission(user_id, achievement_date);
CREATE INDEX idx_score_submission_lab_status ON score_submission(lab_id, status);
CREATE INDEX idx_score_submission_status_expires ON score_submission(status, expires_at);

-- 성능 쿼리
SELECT 
    l.id,
    l.name,
    COALESCE(SUM(sc.default_score), 0) as total_score,
    COUNT(ss.id) as submission_count
FROM labs l
LEFT JOIN score_submissions ss ON l.id = ss.lab_id AND ss.status = 'APPROVED'
LEFT JOIN score_categories sc ON ss.category = sc.name
GROUP BY l.id, l.name
ORDER BY total_score DESC;
```

---

## 🎯 핵심 개발 원칙

### 1. 헥사고날 아키텍처 준수
```
@domain/ranking/           - 도메인 로직
@application/ranking/      - 애플리케이션 서비스
@adapter/web/ranking/      - REST API
@adapter/persistence/      - 데이터 접근
```

### 2. 테스트 주도 개발
```java
// 각 기능별 테스트 작성 필수
@ExtendWith(MockitoExtension.class)
class ScoreSubmissionServiceTest {
    
    @Test
    void submitScore_WithValidData_ShouldCreateSubmission() {
        // given
        ScoreSubmissionCommand command = createValidCommand();
        
        // when
        ScoreSubmissionResult result = service.submitScore(command);
        
        // then
        assertThat(result.getSubmission().getStatus()).isEqualTo(SubmissionStatus.PENDING);
        assertThat(result.getDuplicateCheck().hasDateDuplicates()).isFalse();
    }
    
    @Test
    void submitScore_WithDuplicateDate_ShouldReturnDuplicateWarning() {
        // given & when & then
    }
}
```

### 3. 보안 검증
- 파일 업로드 보안 (크기, 타입, 내용 검증)
- 권한 기반 접근 제어 (랩장, 임원, admin, 교수)
- SQL 인젝션 방지 (JPA 사용)
- XSS 방지 (입력 데이터 검증)

### 4. 성능 고려사항
- 실시간 랭킹 → 배치 처리 전환
- 캐싱 전략 (Redis 또는 로컬 캐시)
- 데이터베이스 인덱스 최적화
- 페이징 처리

---

## 🔍 체크리스트

### Phase 1 완료 기준
- [ ] ScoreSubmission 엔티티 구현 및 테스트
- [ ] 파일 업로드 API 구현 및 보안 검증
- [ ] 점수 신청 API 구현 및 테스트
- [ ] 승인/거부 API 구현 및 권한 검증
- [ ] 중복 검사 로직 구현 및 테스트
- [ ] 실시간 랭킹 계산 구현 및 테스트

### Phase 2 완료 기준
- [ ] 중복 경고 UX 구현
- [ ] 상세 승인 상태 정보 제공
- [ ] 관리자 대시보드 기본 기능
- [ ] 공개 범위 설정 기능

### Phase 3 완료 기준
- [ ] 자동 거부 스케줄링 구현
- [ ] 점수 정정 시스템 구현
- [ ] 졸업 처리 로직 구현
- [ ] 기본 모니터링 구현

### Phase 4 완료 기준
- [ ] 배치 처리 전환 완료
- [ ] 캐싱 전략 적용
- [ ] 데이터베이스 최적화
- [ ] 성능 테스트 통과

---

## 📞 개발 중 참고사항

### 우선순위 원칙
1. **기능 동작** > 성능 최적화
2. **보안** > 사용자 편의성
3. **테스트 커버리지** > 빠른 개발
4. **문서화** > 코드 작성

### 막힐 때 참고할 문서
- `@core/ai-essentials.md` - 핵심 패턴
- `@core/code-templates.md` - 코드 템플릿
- `@core/error-codes.md` - 에러 코드 관리
- `@core/test-patterns.md` - 테스트 패턴

### 개발 환경 명령어
```bash
cd rankus
./gradlew bootRun           # 애플리케이션 실행
./gradlew test              # 테스트 실행
./gradlew test --tests ScoreSubmissionServiceTest  # 특정 테스트
./gradlew check             # 린트 및 타입 체크
```

---

**🎯 다음 단계: Phase 1의 ScoreSubmission 엔티티 설계부터 시작하세요!**