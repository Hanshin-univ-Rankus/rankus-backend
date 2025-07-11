# Ranking Web Adapter Layer 가이드 (AI 코딩용)

> 점수 신청 및 랭킹 시스템의 Controller 및 DTO 구현 패턴

## 🎮 Controller 구조

```
adapter/in/web/
├── controller/
│   ├── ScoreSubmissionController.java    # 점수 신청 API
│   └── RankingController.java           # 랭킹 조회 API
└── dto/
    ├── request/                         # 요청 DTO
    └── response/                        # 응답 DTO
```

## 📊 ScoreSubmissionController 패턴

### 기본 구조

```java
@Validated
@RestController  
@RequiredArgsConstructor
@RequestMapping("/api/score-submissions")
@Tag(name = "ScoreSubmission", description = "점수 신청 API")
public class ScoreSubmissionController {

    private final ScoreSubmissionCommandUseCase scoreSubmissionCommandUseCase;
    private final ScoreSubmissionQueryUseCase scoreSubmissionQueryUseCase;
}
```

### 핵심 권한 패턴

| 기능        | 권한 검증                                                                                                          | 설명             |
|-----------|----------------------------------------------------------------------------------------------------------------|----------------|
| **점수 신청** | `@PreAuthorize("isAuthenticated()")`                                                                           | 인증된 사용자만 신청 가능 |
| **신청 조회** | `@PreAuthorize("isAuthenticated()")`                                                                           | 인증된 사용자만 조회 가능 |
| **점수 승인** | `@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or hasRole('LAB_LEADER') or hasRole('LAB_MANAGER')")` | 관리 권한 필요       |
| **점수 거부** | `@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or hasRole('LAB_LEADER') or hasRole('LAB_MANAGER')")` | 관리 권한 필요       |

### API 엔드포인트 매트릭스

| HTTP 메서드   | 경로                                       | 기능             | 권한          |
|------------|------------------------------------------|----------------|-------------|
| **POST**   | `/api/score-submissions`                 | 점수 신청          | 인증 필요       |
| **GET**    | `/api/score-submissions`                 | 신청 목록 조회 (페이징) | 인증 필요       |
| **GET**    | `/api/score-submissions/{id}`            | 신청 상세 조회       | 인증 필요       |
| **POST**   | `/api/score-submissions/{id}/approve`    | 점수 승인          | 관리 권한       |
| **POST**   | `/api/score-submissions/{id}/reject`     | 점수 거부          | 관리 권한       |
| **POST**   | `/api/score-submissions/{id}/correct`    | 점수 정정          | 관리 권한       |
| **DELETE** | `/api/score-submissions/{id}`            | 신청 삭제          | 본인 또는 관리 권한 |
| **GET**    | `/api/score-submissions/check-duplicate` | 중복 검사          | 인증 필요       |

## 🎯 RankingController 패턴

### 기본 구조

```java
@Validated
@RestController
@RequiredArgsConstructor  
@RequestMapping("/api/rankings")
@Tag(name = "Ranking", description = "랭킹 조회 API")
public class RankingController {

    private final RankingQueryUseCase rankingQueryUseCase;
}
```

### API 엔드포인트 매트릭스

| HTTP 메서드 | 경로                                           | 기능             | 반환 타입                               |
|----------|----------------------------------------------|----------------|-------------------------------------|
| **GET**  | `/api/rankings/labs`                         | 전체 랩실 랭킹 (페이징) | `Page<RankingResponseDto>`          |
| **GET**  | `/api/rankings/labs/{labId}`                 | 특정 랩실 랭킹       | `RankingResponseDto`                |
| **GET**  | `/api/rankings/labs/{labId}/contributors`    | 랩실 상위 기여자      | `List<UserContributionResponseDto>` |
| **GET**  | `/api/rankings/my-labs`                      | 내 랩실 랭킹        | `List<RankingResponseDto>`          |
| **GET**  | `/api/rankings/labs/{labId}/total-score`     | 랩실 총 점수        | `Integer`                           |
| **GET**  | `/api/rankings/labs/{labId}/my-contribution` | 내 랩실 기여도       | `Integer`                           |

## 📝 Request DTO 패턴

### ScoreSubmissionCreateRequestDto

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreSubmissionCreateRequestDto {
    
    @NotNull(message = "랩실 ID는 필수입니다")
    @Positive(message = "랩실 ID는 양수여야 합니다")
    private Long labId;
    
    @NotNull(message = "점수 카테고리는 필수입니다")
    private ScoreCategory category;
    
    @NotBlank(message = "성과 내용은 필수입니다")
    @Size(max = 500, message = "성과 내용은 500자 이하여야 합니다")
    private String achievementDescription;
    
    @NotNull(message = "취득일자는 필수입니다")
    @PastOrPresent(message = "취득일자는 미래일 수 없습니다")
    private LocalDate achievementDate;
    
    @NotBlank(message = "증빙서류 URL은 필수입니다")
    @Size(max = 500, message = "증빙서류 URL은 500자 이하여야 합니다")
    private String proofFileUrl;
    
    @Size(max = 200, message = "신청 사유는 200자 이하여야 합니다")
    private String applicationReason;
    
    @Size(max = 500, message = "관련 링크는 500자 이하여야 합니다")
    @URL(message = "올바른 URL 형식이어야 합니다")
    private String relatedLink;
    
    @NotNull(message = "공개 범위는 필수입니다")
    private VisibilityLevel visibility;
}
```

### ScoreSubmissionApprovalRequestDto

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreSubmissionApprovalRequestDto {
    
    @Size(max = 200, message = "거부 사유는 200자 이하여야 합니다")
    private String rejectionReason; // 거부 시에만 사용
}
```

## 📤 Response DTO 패턴

### ScoreSubmissionResponseDto

```java
@Getter
@Builder
@AllArgsConstructor
public class ScoreSubmissionResponseDto {
    
    private Long id;
    private Long userId;
    private String userName;
    private Long labId;
    private String labName;
    private ScoreCategory category;
    private String categoryDescription;
    private int score;
    private String achievementDescription;
    private LocalDate achievementDate;
    private String proofFileUrl;
    private String applicationReason;
    private String relatedLink;
    private SubmissionStatus status;
    private VisibilityLevel visibility;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime expiresAt;
    private String rejectionReason;
    
    // 팩토리 메서드
    public static ScoreSubmissionResponseDto from(ScoreSubmission submission) {
        return ScoreSubmissionResponseDto.builder()
                .id(submission.getId())
                .userId(submission.getUser().getId())
                .userName(submission.getUser().getName())
                .labId(submission.getLab().getId())
                .labName(submission.getLab().getName())
                .category(submission.getCategory())
                .categoryDescription(submission.getCategory().getDescription())
                .score(submission.getScore())
                .achievementDescription(submission.getAchievementDescription())
                .achievementDate(submission.getAchievementDate())
                .proofFileUrl(submission.getProofFileUrl())
                .applicationReason(submission.getApplicationReason())
                .relatedLink(submission.getRelatedLink())
                .status(submission.getStatus())
                .visibility(submission.getVisibility())
                .approvedBy(submission.getApprovedBy())
                .approvedAt(submission.getApprovedAt())
                .submittedAt(submission.getSubmittedAt())
                .expiresAt(submission.getExpiresAt())
                .rejectionReason(submission.getRejectionReason())
                .build();
    }
}
```

### RankingResponseDto

```java
@Getter
@Builder
@AllArgsConstructor
public class RankingResponseDto {
    
    private Long labId;
    private String labName;
    private String labCategory;
    private int totalScore;
    private int rank;
    private int memberCount;
    
    // 중첩 클래스: 사용자 기여도
    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserContributionResponseDto {
        private Long userId;
        private String userName;
        private int contributionScore;
        private int submissionCount;
        
        public static UserContributionResponseDto from(RankingQueryUseCase.UserContribution contribution) {
            return UserContributionResponseDto.builder()
                    .userId(contribution.getUserId())
                    .userName(contribution.getUserName())
                    .contributionScore(contribution.getContributionScore())
                    .submissionCount(contribution.getSubmissionCount())
                    .build();
        }
    }
    
    // 팩토리 메서드
    public static RankingResponseDto from(RankingQueryUseCase.LabRankingResult ranking) {
        return RankingResponseDto.builder()
                .labId(ranking.getLabId())
                .labName(ranking.getLabName())
                .labCategory(ranking.getLabCategory())
                .totalScore(ranking.getTotalScore())
                .rank(ranking.getRank())
                .memberCount(ranking.getMemberCount())
                .build();
    }
}
```

### DuplicateCheckResponseDto

```java
@Getter
@Builder
@AllArgsConstructor
public class DuplicateCheckResponseDto {
    
    private boolean hasDateDuplicates;
    private boolean hasExactDuplicates;
    private String warningLevel;
    private String warningMessage;
    private List<ScoreSubmissionResponseDto> duplicateSubmissions;
    
    public static DuplicateCheckResponseDto from(DuplicateCheckPolicy.DuplicateCheckResult result) {
        DuplicateCheckPolicy.DuplicateWarningLevel level = 
            new DuplicateCheckPolicy().determineDuplicateWarningLevel(result);
            
        return DuplicateCheckResponseDto.builder()
                .hasDateDuplicates(result.hasDateDuplicates())
                .hasExactDuplicates(result.hasExactDuplicates())
                .warningLevel(level.name())
                .warningMessage(level.getDescription())
                .duplicateSubmissions(result.getDuplicateSubmissions().stream()
                        .map(ScoreSubmissionResponseDto::from)
                        .toList())
                .build();
    }
}
```

## 🎯 Controller 메서드 구현 패턴

### 1. 생성 (POST)

```java
@Operation(summary = "점수 신청", description = "새로운 점수 신청을 생성합니다.")
@PostMapping
@PreAuthorize("isAuthenticated()")
public ResponseEntity<ApiResponse<ScoreSubmissionResponseDto>> createScoreSubmission(
        @Valid @RequestBody ScoreSubmissionCreateRequestDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    ScoreSubmission submission = scoreSubmissionCommandUseCase.submitScore(
            userDetails.getUserId(), request.getLabId(), request.getCategory(),
            request.getAchievementDescription(), request.getAchievementDate(),
            request.getProofFileUrl(), request.getApplicationReason(),
            request.getRelatedLink(), request.getVisibility()
    );
    
    ScoreSubmissionResponseDto responseDto = ScoreSubmissionResponseDto.from(submission);
    
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(responseDto, "점수 신청이 성공적으로 생성되었습니다"));
}
```

### 2. 조회 (GET)

```java
@Operation(summary = "점수 신청 목록 조회", description = "점수 신청 목록을 페이징하여 조회합니다.")
@GetMapping
@PreAuthorize("isAuthenticated()")
public ResponseEntity<ApiResponse<PageResponse<ScoreSubmissionResponseDto>>> getScoreSubmissions(
        @PageableDefault(size = 20) Pageable pageable,
        @RequestParam(required = false) Long labId,
        @RequestParam(required = false) SubmissionStatus status,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    Page<ScoreSubmission> submissionPage = scoreSubmissionQueryUseCase
            .findSubmissionsForUser(userDetails.getUserId(), labId, status, pageable);
    
    PageResponse<ScoreSubmissionResponseDto> pageResponse = 
            PageResponse.of(submissionPage, ScoreSubmissionResponseDto::from);
    
    return ResponseEntity.ok(ApiResponse.success(pageResponse, "점수 신청 목록 조회 성공"));
}
```

### 3. 승인/거부 (POST)

```java
@Operation(summary = "점수 승인", description = "점수 신청을 승인합니다.")
@PostMapping("/{submissionId}/approve")
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or hasRole('LAB_LEADER') or hasRole('LAB_MANAGER')")
public ResponseEntity<ApiResponse<Void>> approveScoreSubmission(
        @PathVariable @Positive(message = "신청 ID는 양수여야 합니다") Long submissionId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    scoreSubmissionCommandUseCase.approveSubmission(submissionId, userDetails.getUserId());
    
    return ResponseEntity.ok(ApiResponse.success(null, "점수 신청이 승인되었습니다"));
}
```

## 🔧 응답 메시지 패턴

### 성공 응답

```java
// 생성 성공
ApiResponse.created(data, "점수 신청이 성공적으로 생성되었습니다")

// 조회 성공  
ApiResponse.success(data, "점수 신청 목록 조회 성공")

// 승인 성공
ApiResponse.success(null, "점수 신청이 승인되었습니다")

// 거부 성공
ApiResponse.success(null, "점수 신청이 거부되었습니다")

// 삭제 성공
ApiResponse.deleted("점수 신청이 삭제되었습니다")
```

### 검증 메시지

```java
// 필수 값 검증
@NotNull(message = "랩실 ID는 필수입니다")
@NotBlank(message = "성과 내용은 필수입니다")

// 크기 검증
@Size(max = 500, message = "성과 내용은 500자 이하여야 합니다")
@Positive(message = "랩실 ID는 양수여야 합니다")

// 날짜 검증
@PastOrPresent(message = "취득일자는 미래일 수 없습니다")

// URL 검증
@URL(message = "올바른 URL 형식이어야 합니다")
```

## 🧪 Controller 테스트 패턴

### 통합 테스트 구조

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class ScoreSubmissionControllerIntegrationTest {
    
    @Autowired TestRestTemplate restTemplate;
    @Autowired TestEntityManager entityManager;
    
    @Test
    void 점수_신청_생성_성공() {
        // given: 인증된 사용자와 유효한 요청 데이터
        
        // when: POST /api/score-submissions
        
        // then: 201 Created와 응답 데이터 검증
    }
}
```

## 📋 핵심 구현 체크리스트

- ✅ `@Validated`, `@RestController`, `@RequiredArgsConstructor` 어노테이션 순서
- ✅ `@RequestMapping("/api/{domain}")` 패턴
- ✅ `@Tag(name, description)` Swagger 문서화
- ✅ `@Operation(summary, description)` 메서드별 문서화
- ✅ `@PreAuthorize` 권한 검증 명시
- ✅ `@Valid @RequestBody` 요청 데이터 검증
- ✅ `@AuthenticationPrincipal CustomUserDetails` 인증 정보 활용
- ✅ `ResponseEntity<ApiResponse<DTO>>` 응답 타입 통일
- ✅ HTTP 상태 코드 적절한 설정 (201 Created, 200 OK)
- ✅ 응답 메시지 한국어로 명확하게 작성

---

**참조**: 기본 Controller 패턴은 `/adapter/CLAUDE.md` 참조  
**업데이트**: 2025-01-09 | **구현 완료**: ScoreSubmission, Ranking Controller 전체