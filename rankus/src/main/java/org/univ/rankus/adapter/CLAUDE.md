# Adapter Layer 가이드 (AI 코딩용)

## 🔌 구조 패턴

```
adapter/
├── in/web/          # Controller + DTO
│   ├── controller/  # REST API
│   └── dto/        # Request/Response DTO
└── out/persistence/ # Repository Adapter
    ├── jpa/        # Spring Data JPA
    └── impl/       # Adapter 구현
```

## 🎮 Controller 템플릿

```java
@RestController @RequestMapping("/api/{domain}") @RequiredArgsConstructor @Validated
public class {Domain}

Controller {
    private final {
        Domain
    } CommandUseCase commandUseCase;
    private final {
        Domain
    } QueryUseCase queryUseCase;
    
    @PostMapping @PreAuthorize("isAuthenticated()")
    public ResponseEntity < ApiResponse < {Domain} ResponseDto >> create {
        Domain
    } (
    @Valid @RequestBody {
        Domain
    } CreateRequestDto request){
        {
            Domain
        } entity = commandUseCase.create {
            Domain
        } (request);
        return ResponseEntity.status(CREATED).body(ApiResponse.created({Domain}ResponseDto.from(entity)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity < ApiResponse < {Domain} ResponseDto >> get {
        Domain
    } (@PathVariable Long id){
        {
            Domain
        } entity = queryUseCase.find {
            Domain
        } ById(id);
        return ResponseEntity.ok(ApiResponse.success({Domain}ResponseDto.from(entity)));
    }
}
```

## 🔐 권한 패턴

| 패턴    | 코드                                                                                                             | 사용 케이스  |
|-------|----------------------------------------------------------------------------------------------------------------|---------|
| 인증만   | `@PreAuthorize("isAuthenticated()")`                                                                           | 기본 CRUD |
| 역할    | `@PreAuthorize("hasRole('ADMIN')")`                                                                            | 관리 기능   |
| 소유권   | `@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #id, 'Domain', 'ACTION')")`          | 개인 리소스  |
| 랩실 권한 | `@PreAuthorize("@labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'ACTION')")` | 랩실 관련   |

## 📝 Notice Controller 패턴

```java

@RestController
@RequestMapping("/api/labs/{labId}/notices")
public class LabNoticeController {

    @GetMapping
    @PreAuthorize("@labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_NOTICES')")
    public ResponseEntity<ApiResponse<PageResponse<LabNoticeResponseDto>>> getLabNotices(
            @PathVariable Long labId, Pageable pageable) {
        // 구현
    }

    @PostMapping
    @PreAuthorize("@labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_NOTICES')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> createNotice(
            @PathVariable Long labId, @Valid @RequestBody LabNoticeCreateRequestDto request) {
        // 구현
    }

    @PutMapping("/{noticeId}")
    @PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'UPDATE')")
    public ResponseEntity<ApiResponse<LabNoticeResponseDto>> updateNotice(
            @PathVariable Long labId, @PathVariable Long noticeId, @Valid @RequestBody LabNoticeUpdateRequestDto request) {
        // 구현
    }
}
```

## 🗄️ Repository Adapter 템플릿

```java
@Component @RequiredArgsConstructor
public class {Domain}RepositoryAdapter implements{Domain}

RepositoryPort {
    private final SpringData {
        Domain
    } Repository springData {
        Domain
    } Repository;

    @Override
    public {
        Domain
    } save({Domain}entity) {
        return springData {
            Domain
        } Repository.save(entity);
    }
    
    @Override
    public Optional < {Domain} > findById(Long id) {
        return springData {
            Domain
        } Repository.findById(id);
    }
}
```

## 📊 DTO 변환 패턴

```java
// Controller에서 변환
{Domain}entity =commandUseCase.

create {
    Domain
}(request);
        return ResponseEntity.

ok(ApiResponse.success( {
    Domain
}ResponseDto.

from(entity)));

// ResponseDto 팩토리 메서드
public static {
    Domain
}

ResponseDto from( {
    Domain
}

entity){
        return{Domain}ResponseDto.

builder()
        .

id(entity.getId())
        .

name(entity.getName())
        .

build();
}
```

## 🏆 Ranking Controller 패턴

```java

@RestController
@RequestMapping("/api/score-submissions")
public class ScoreSubmissionController {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ScoreSubmissionResponseDto>> createScoreSubmission(
            @Valid @RequestBody ScoreSubmissionCreateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 점수 신청 생성
    }

    @PostMapping("/{submissionId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or hasRole('LAB_LEADER') or hasRole('LAB_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> approveScoreSubmission(
            @PathVariable Long submissionId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 점수 승인
    }
}

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    @GetMapping("/labs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<RankingResponseDto>>> getLabRankings(
            @PageableDefault(size = 20) Pageable pageable) {
        // 전체 랩실 랭킹 조회
    }
}
```

## 📊 구현된 Controller 매트릭스

| 도메인                 | Controller                    | 주요 기능                | 권한 검증          | 상태     |
|---------------------|-------------------------------|----------------------|----------------|--------|
| User                | UserController                | 내 정보 조회              | 인증 필요          | 완료     |
| Lab                 | LabPromotionController        | 랩실 홍보, 이미지 관리        | 인증 + 랩실 권한     | 완료     |
| LabApplication      | LabApplicationController      | 랩실 지원, 승인/거부         | 인증 + 권한 검증     | 완료     |
| LabCreationRequest  | LabCreationRequestController  | 랩실 생성 요청, 관리자 승인     | 인증 + 관리자 권한    | 완료     |
| LabNotice           | LabNoticeController           | 공지사항 CRUD, 고정/해제     | 인증 + 랩실 권한     | 완료     |
| Interview           | InterviewController           | 면접 설정, 슬롯 관리         | 인증 + 랩실 관리 권한  | 완료     |
| **ScoreSubmission** | **ScoreSubmissionController** | **점수 신청, 승인/거부, 정정** | **인증 + 관리 권한** | **완료** |
| **Ranking**         | **RankingController**         | **랭킹 조회, 기여도 분석**    | **인증 필요**      | **완료** |

## 🔧 Ranking Repository 특화 패턴

```java

@Component
@RequiredArgsConstructor
public class ScoreSubmissionRepositoryAdapter implements ScoreSubmissionRepositoryPort {
    private final SpringDataScoreSubmissionRepository springDataRepository;

    // 복잡한 집계 쿼리 위임
    @Override
    public Integer sumScoresByLabIdAndStatus(Long labId, SubmissionStatus status) {
        return springDataRepository.sumScoresByLabIdAndStatus(labId, status);
    }

    // 중복 검사를 위한 복합 조건 조회
    @Override
    public List<ScoreSubmission> findByUserIdAndAchievementDate(Long userId, LocalDate date) {
        return springDataRepository.findByUserIdAndAchievementDateOrderBySubmittedAtDesc(userId, date);
    }
}
```

## 📋 특화 DTO 패턴

### Ranking 요청/응답 DTO

```java
// 점수 신청 요청
@NotNull
@Positive
private Long labId;
@NotNull
private ScoreCategory category;
@NotBlank
@Size(max = 500)
private String achievementDescription;
@NotNull
@PastOrPresent
private LocalDate achievementDate;

// 랭킹 응답
private Long labId;
private String labName;
private int totalScore;
private int rank;
private List<UserContributionResponseDto> topContributors;
```

**참조**:

- **Ranking Web**: `/adapter/in/web/ranking/CLAUDE.md`
- **Ranking Persistence**: `/adapter/out/persistence/ranking/CLAUDE.md`

**업데이트**: 2025-01-09 | **구현 완료**: Ranking 시스템 포함