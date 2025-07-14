# AI 개발 핵심 가이드

> Claude/GPT용 즉시 사용 가능한 개발 패턴

## 🎯 아키텍처 패턴

```
Controller → {Domain}CommandUseCase → {Domain}CommandService → {Domain}RepositoryPort
```

## 📛 네이밍 템플릿

| 타입         | 패턴                                | 예시                          |
|------------|-----------------------------------|------------------------------|
| Entity     | `{Domain}`                        | `User`, `Lab`, `AttendanceSession` |
| Service    | `{Domain}{Command\|Query}Service` | `UserCommandService`, `RankingQueryService` |
| Controller | `{Domain}Controller`              | `LabNoticeController`, `RankingController` |
| UseCase    | `{Domain}{Command\|Query}UseCase` | `InterviewCommandUseCase` |
| DTO        | `{Domain}{Action}RequestDto`      | `UserCreateRequestDto` |
| ErrorCode  | `{Domain}ErrorCode`               | `RankingErrorCode` |

## 🔧 즉시 사용 코드

### Service 패턴
```java
@Service @RequiredArgsConstructor
public class {Domain}CommandService implements {Domain}CommandUseCase {
    private final {Domain}RepositoryPort repository;
    
    @Override @Transactional
    public {Domain} create{Domain}({Domain}CreateRequestDto request) {
        {Domain} entity = {Domain}.create(request.getName());
        return repository.save(entity);
    }
}
```

### Controller 패턴
```java
@RestController @RequestMapping("/api/{domains}") @RequiredArgsConstructor
public class {Domain}Controller {
    private final {Domain}CommandUseCase commandUseCase;
    
    @PostMapping @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
        @Valid @RequestBody {Domain}CreateRequestDto request) {
        {Domain} entity = commandUseCase.create{Domain}(request);
        return ResponseEntity.status(CREATED).body(ApiResponse.created({Domain}ResponseDto.from(entity)));
    }
}
```

## 🔐 권한 패턴

| 케이스    | 패턴                                                                                                             |
|--------|----------------------------------------------------------------------------------------------------------------|
| 인증만   | `@PreAuthorize("isAuthenticated()")`                                                                           |
| 소유권   | `@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #id, 'Domain', 'ACTION')")`          |
| 랩실 권한 | `@PreAuthorize("@labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'ACTION')")` |

## 📝 ErrorCode 템플릿

| Prefix       | 도메인           | 현재 사용   |
|--------------|---------------|---------|
| `USER`       | User          | 001~010 |
| `LAB`        | Lab           | 001~015 |
| `RANKING`    | Ranking       | 001~023 |
| `ATT`        | Attendance    | 001~022 |

```java
public enum {Domain}ErrorCode implements ErrorCode {
    {FIELD}_REQUIRED("{PREFIX}_001", BAD_REQUEST, "{필드}는 필수입니다"),
    {DOMAIN}_NOT_FOUND("{PREFIX}_404", NOT_FOUND, "{도메인}을 찾을 수 없습니다");
}
```

## 🧪 테스트 템플릿

```java
@ExtendWith(MockitoExtension.class)
class {Domain}CommandServiceTest {
    @Mock private {Domain}RepositoryPort repository;
    @InjectMocks private {Domain}CommandService service;
    
    @Test
    void create{Domain}_성공() {
        // given
        {Domain}CreateRequestDto request = new {Domain}CreateRequestDto("name");
        {Domain} entity = {Domain}.create("name");
        when(repository.save(any())).thenReturn(entity);
        
        // when
        {Domain} result = service.create{Domain}(request);
        
        // then
        assertThat(result.getName()).isEqualTo("name");
    }
}
```