# AI 개발 핵심 가이드

> Claude/GPT용 즉시 사용 가능한 개발 패턴

## 🚨 중요: 코드 작성 전 필수 체크

### 1. 실제 구현 확인 원칙
- **메서드 호출 전 반드시 실제 클래스 확인**
- **파라미터 타입과 개수 정확히 매칭**
- **존재하지 않는 메서드 사용 금지**

### 2. 테스트 작성 시 UltraThink 체크리스트
```
✅ 프로덕션 코드의 실제 메서드 시그니처 확인
✅ Factory 클래스의 실제 메서드들 확인
✅ DTO/Response 클래스의 실제 메서드들 확인
✅ Command/UseCase 인터페이스의 실제 구조 확인
✅ Import 문 검증
```

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
        // ✅ 도메인 특화 예외 사용
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));
        
        if (!hasPermission(user, lab)) {
            throw new LabPermissionException(LabErrorCode.LAB_PERMISSION_DENIED);
        }
        
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
        // ✅ 반드시 ApiResponse 래퍼 사용
        return ResponseEntity.status(CREATED).body(ApiResponse.created({Domain}ResponseDto.from(entity)));
    }
}
```

## 🔐 권한 패턴

| 케이스      | 패턴                                                                                                             |
|----------|----------------------------------------------------------------------------------------------------------------|
| 인증만     | `@PreAuthorize("isAuthenticated()")`                                                                           |
| 소유권     | `@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #id, 'Domain', 'ACTION')")`          |
| 랩실 권한   | `@PreAuthorize("@labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'ACTION')")` |
| 랩실 멤버 권한 | `@PreAuthorize("@labMemberPermissionEvaluator.hasLabPermission(authentication, #labId, 'MANAGE')")`           |
| 벌크 업데이트 | `@PreAuthorize("@labMemberPermissionEvaluator.hasLabPermission(authentication, #request.labId, 'MANAGE')")`   |

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

### 기본 Service 테스트
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

### ⚠️ 흔한 실수와 해결법

| 실수 | 올바른 방법 |
|------|------------|
| `response.isPartialSuccess()` | `!response.isCompleteSuccess()` |
| `response.getFailedRecordIds()` | `response.getResults().stream().filter(result -> !result.isSuccess()).map(Result::getRecordId).toList()` |
| `Command(labId, request)` | `service.method(labId, request, managerId)` |
| `buildRecord(1L, lab, status)` | `buildAbsentRecordWithId(1L)` |
| `IllegalArgumentException` | 도메인 특화 예외 (`LabNotFoundException`, `LabPermissionException`) |
| `IllegalStateException` | 도메인 특화 예외 (`LabValidationException`, `LabPermissionException`) |

### 랩실 관리 서비스 패턴
```java
@Service @RequiredArgsConstructor
public class ManageLabMemberRoleService implements ManageLabMemberRoleCommand {
    private final UserRepositoryPort userRepositoryPort;
    private final LabMemberPermissionEvaluator permissionEvaluator;
    
    @Override @Transactional
    public void changeUserRole(Long labId, Long targetUserId, Role newRole, Long managerId) {
        // ✅ 권한 검증
        if (!permissionEvaluator.hasLabPermission(managerId, labId, "MANAGE")) {
            throw new LabPermissionException(LabErrorCode.LAB_PERMISSION_DENIED);
        }
        
        // ✅ 도메인 로직 수행
        User targetUser = userRepositoryPort.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        targetUser.changeRole(newRole);
        userRepositoryPort.save(targetUser);
    }
}
```

### 벌크 업데이트 패턴
```java
@Service @RequiredArgsConstructor
public class BulkUpdateAttendanceService implements BulkUpdateAttendanceCommand {
    
    @Override @Transactional
    public BulkAttendanceUpdateResponse bulkUpdateAttendance(
            Long labId, BulkAttendanceUpdateRequest request, Long managerId) {
        
        // ✅ 벌크 처리 결과 추적
        List<BulkUpdateResult> results = new ArrayList<>();
        
        for (BulkUpdateAttendanceCommand.Command command : request.getCommands()) {
            try {
                attendanceService.updateAttendanceStatus(command.recordId(), command.newStatus());
                results.add(BulkUpdateResult.success(command.recordId()));
            } catch (Exception e) {
                results.add(BulkUpdateResult.failure(command.recordId(), e.getMessage()));
            }
        }
        
        return new BulkAttendanceUpdateResponse(results);
    }
}
```

### 팩토리 메서드 올바른 사용
```java
// ❌ 잘못된 사용
DomainAttendanceFactory.buildAttendanceRecord(1L, lab, status)

// ✅ 올바른 사용 
DomainAttendanceFactory.buildValidSession()
DomainAttendanceFactory.buildValidSessionWithId(1L)
DomainAttendanceFactory.buildSessionWithTitle("테스트")
DomainAttendanceFactory.buildSessionWithValidityMinutes(5)
```