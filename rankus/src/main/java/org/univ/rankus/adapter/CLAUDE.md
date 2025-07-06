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
public class {Domain}Controller {
    private final {Domain}CommandUseCase commandUseCase;
    private final {Domain}QueryUseCase queryUseCase;
    
    @PostMapping @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
            @Valid @RequestBody {Domain}CreateRequestDto request) {
        {Domain} entity = commandUseCase.create{Domain}(request);
        return ResponseEntity.status(CREATED).body(ApiResponse.created({Domain}ResponseDto.from(entity)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> get{Domain}(@PathVariable Long id) {
        {Domain} entity = queryUseCase.find{Domain}ById(id);
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
@RestController @RequestMapping("/api/labs/{labId}/notices")
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
public class {Domain}RepositoryAdapter implements {Domain}RepositoryPort {
    private final SpringData{Domain}Repository springData{Domain}Repository;
    
    @Override
    public {Domain} save({Domain} entity) {
        return springData{Domain}Repository.save(entity);
    }
    
    @Override
    public Optional<{Domain}> findById(Long id) {
        return springData{Domain}Repository.findById(id);
    }
}
```

## 📊 DTO 변환 패턴

```java
// Controller에서 변환
{Domain} entity = commandUseCase.create{Domain}(request);
return ResponseEntity.ok(ApiResponse.success({Domain}ResponseDto.from(entity)));

// ResponseDto 팩토리 메서드
public static {Domain}ResponseDto from({Domain} entity) {
    return {Domain}ResponseDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .build();
}
```

**업데이트**: 2025-01-05 | **50줄** | AI 코딩 최적화