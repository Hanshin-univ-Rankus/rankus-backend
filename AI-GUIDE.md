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

## 🔧 컴파일 오류 방지 체크리스트

### ⚠️ 실제 발생한 오류 유형

#### 1. ErrorCode 구현 누락
```java
// ❌ 컴파일 오류: getStatus() 메서드 누락
public enum LabResourceErrorCode implements ErrorCode {
    RESOURCE_NOT_FOUND("RESOURCE_001", "자료를 찾을 수 없습니다");
    // getStatus() 구현 누락!
}

// ✅ 올바른 구현
public enum LabResourceErrorCode implements ErrorCode {
    RESOURCE_NOT_FOUND("RESOURCE_001", HttpStatus.NOT_FOUND, "자료를 찾을 수 없습니다");
    
    // 모든 메서드 필수 구현
    @Override public String getCode() { return code; }
    @Override public HttpStatus getStatus() { return status; }  // ← 필수!
    @Override public String getMessage() { return message; }
}
```

#### 2. BaseCustomException 생성자 오류
```java
// ❌ 존재하지 않는 생성자 사용
throw new LabResourceNotFoundException(LabResourceErrorCode.RESOURCE_NOT_FOUND, "추가 메시지");

// ✅ 올바른 생성자 사용
throw new LabResourceNotFoundException(LabResourceErrorCode.RESOURCE_NOT_FOUND);
```

#### 3. CustomUserDetails 접근 오류
```java
// ❌ 존재하지 않는 메서드 호출
User user = currentUser.getUser(); // getUser() 메서드 없음!

// ✅ 올바른 방법
User user = userRepositoryPort.findById(currentUser.getUserId())
        .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
```

#### 4. PageResponse 사용법 오류
```java
// ❌ 잘못된 사용
PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(resourcePage);

// ✅ 올바른 사용
PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(resourcePage, dto -> dto);
```

#### 5. Builder 패턴 오류
```java
// ❌ @Builder.Default 누락
@Builder
public class LabResourceCreateRequestDto {
    private Boolean isPublic = true; // 무시됨!
}

// ✅ 올바른 사용
@Builder
public class LabResourceCreateRequestDto {
    @Builder.Default
    private Boolean isPublic = true; // 정상 작동
}
```

### 🔍 필수 확인 단계

1. **ErrorCode enum 작성 시**:
   - [ ] `getCode()`, `getStatus()`, `getMessage()` 모든 메서드 구현
   - [ ] HttpStatus 필드와 생성자 매개변수 확인

2. **예외 throw 시**:
   - [ ] `(ErrorCode)` 생성자만 사용
   - [ ] `(ErrorCode, String)` 생성자 사용 금지

3. **User 엔티티 접근 시**:
   - [ ] `CustomUserDetails`에서 직접 User 접근 금지
   - [ ] `UserRepositoryPort`를 통한 조회 방식 사용

4. **PageResponse 사용 시**:
   - [ ] `PageResponse.of(page, mapper)` 패턴 사용
   - [ ] Function 매개변수 필수 제공

5. **@Builder 사용 시**:
   - [ ] 초기값이 있는 필드에 `@Builder.Default` 추가

## 🎯 아키텍처 패턴

```
Controller → {Domain}CommandUseCase → {Domain}CommandService → {Domain}RepositoryPort
```

## 📛 네이밍 템플릿

| 타입         | 패턴                                | 예시                          |
|------------|-----------------------------------|------------------------------|
| Entity     | `{Domain}`                        | `User`, `Lab`, `AttendanceSession`, `CalendarEvent` |
| Service    | `{Domain}{Command\|Query}Service` | `UserCommandService`, `RankingQueryService`, `CalendarEventCommandService` |
| Controller | `{Domain}Controller`              | `LabNoticeController`, `RankingController`, `CalendarScheduleController` |
| UseCase    | `{Domain}{Command\|Query}UseCase` | `InterviewCommandUseCase`, `CalendarEventQueryUseCase` |
| DTO        | `{Domain}{Action}RequestDto`      | `UserCreateRequestDto`, `CalendarEventCreateRequestDto` |
| ErrorCode  | `{Domain}ErrorCode`               | `RankingErrorCode`, `CalendarEventErrorCode` |

## 🔧 즉시 사용 코드

### Service 패턴 - 예외 처리 필수 규칙

```java
@Service @RequiredArgsConstructor
public class {Domain}CommandService implements {Domain}CommandUseCase {
    private final {Domain}RepositoryPort repository;
    private final UserRepositoryPort userRepositoryPort;  // User 조회용
    
    @Override @Transactional
    public {Domain} create{Domain}({Domain}CreateRequestDto request, Long currentUserId) {
        // ✅ 필수: ErrorCode와 함께 예외 처리
        Lab lab = labRepositoryPort.findById(request.getLabId())
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));
        
        // ✅ 필수: UserRepositoryPort를 통한 User 조회
        User currentUser = userRepositoryPort.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        
        // ✅ 필수: 권한 검증
        if (!currentUser.canCreate{Domain}s(lab)) {
            throw new {Domain}PermissionException({Domain}ErrorCode.{DOMAIN}_ACCESS_DENIED);
        }
        
        {Domain} entity = new {Domain}(request.getName(), lab, currentUser);
        return repository.save(entity);
    }
}
```

### 🚨 예외 클래스 생성자 규칙

#### ✅ 올바른 BaseCustomException 사용
```java
// Rankus 프로젝트의 BaseCustomException은 단일 생성자만 지원
public class LabResourceNotFoundException extends BaseCustomException {
    public LabResourceNotFoundException(LabResourceErrorCode errorCode) {
        super(errorCode);  // ← 이 생성자만 존재
    }
}

// 서비스에서 사용
throw new LabResourceNotFoundException(LabResourceErrorCode.RESOURCE_NOT_FOUND);
```

#### ❌ 잘못된 사용 (컴파일 오류 발생)
```java
// 이런 생성자는 존재하지 않음!
throw new LabResourceNotFoundException(LabResourceErrorCode.RESOURCE_NOT_FOUND, "추가 메시지");
throw new LabResourceNotFoundException("에러 메시지");
```

#### 🔍 필수 확인사항
1. **BaseCustomException 상속**: 모든 도메인 예외는 BaseCustomException 상속
2. **단일 생성자만 사용**: `(ErrorCode errorCode)` 생성자만 지원
3. **ErrorCode 필수**: 예외 생성 시 반드시 적절한 ErrorCode enum 값 전달

### Controller 패턴 - PageResponse 올바른 사용법

```java
@RestController @RequestMapping("/api/{domains}") @RequiredArgsConstructor
public class {Domain}Controller {
    private final {Domain}CommandUseCase commandUseCase;
    private final {Domain}QueryUseCase queryUseCase;
    
    @PostMapping @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
        @Valid @RequestBody {Domain}CreateRequestDto request,
        @AuthenticationPrincipal CustomUserDetails currentUser) {
        {Domain} entity = commandUseCase.create{Domain}(request, currentUser.getUserId());
        // ✅ 반드시 ApiResponse 래퍼 사용
        return ResponseEntity.status(CREATED).body(ApiResponse.created({Domain}ResponseDto.from(entity)));
    }
    
    @GetMapping @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<{Domain}ResponseDto>>> get{Domain}s(
        @PageableDefault(size = 20) Pageable pageable,
        @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        Page<{Domain}ResponseDto> page = queryUseCase.get{Domain}s(pageable, currentUser.getUserId());
        
        // ✅ PageResponse 올바른 사용법: Function 매개변수 필수!
        PageResponse<{Domain}ResponseDto> pageResponse = PageResponse.of(page, dto -> dto);
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "{도메인} 목록 조회 성공"));
    }
}
```

### 🔧 PageResponse 사용법 가이드

#### ✅ 올바른 PageResponse 사용
```java
// 1. 기본 패턴 - 변환 함수 제공
PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(resourcePage, dto -> dto);

// 2. 변환이 필요한 경우
PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(
    resourcePage, 
    resource -> LabResourceResponseDto.from(resource)
);

// 3. 메서드 참조 사용
PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(
    resourcePage, 
    LabResourceResponseDto::from
);
```

#### ❌ 잘못된 PageResponse 사용 (컴파일 오류)
```java
// Function 매개변수 누락 - 컴파일 오류 발생!
PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(resourcePage);

// 잘못된 메서드 - 존재하지 않음
PageResponse<LabResourceResponseDto> pageResponse = PageResponse.from(resourcePage);
```

#### 🔍 필수 확인사항
1. **Function 매개변수 필수**: `PageResponse.of(page, mapper)` 패턴 사용
2. **변환 함수 제공**: 최소한 `dto -> dto` 형태로라도 제공
3. **메서드 시그니처 확인**: `PageResponse.of(Page<T> page, Function<T, R> mapper)`

## 🔐 권한 패턴

| 케이스      | 패턴                                                                                                             |
|----------|----------------------------------------------------------------------------------------------------------------|
| 인증만     | `@PreAuthorize("isAuthenticated()")`                                                                           |
| 소유권     | `@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #id, 'Domain', 'ACTION')")`          |
| 랩실 권한   | `@PreAuthorize("@labNoticePermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'ACTION')")` |
| 랩실 멤버 권한 | `@PreAuthorize("@labMemberPermissionEvaluator.hasLabPermission(authentication, #labId, 'MANAGE')")`           |
| 벌크 업데이트 | `@PreAuthorize("@labMemberPermissionEvaluator.hasLabPermission(authentication, #request.labId, 'MANAGE')")`   |
| 캘린더 권한 | `@PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_CALENDAR')")` |

## 📝 ErrorCode 구현 가이드

### 📋 현재 사용 중인 Prefix

| Prefix         | 도메인           | 현재 사용   |
|----------------|---------------|---------|
| `USER`         | User          | 001~024 |
| `LAB`          | Lab           | 001~015 |
| `RANKING`      | Ranking       | 001~023 |
| `ATT`          | Attendance    | 001~022 |
| `CALENDAR_EVENT` | CalendarEvent | 001~016 |
| `RESOURCE`     | LabResource   | 001~011 |

### 🔧 필수 구현 템플릿

```java
public enum {Domain}ErrorCode implements ErrorCode {
    // 필수 필드들
    {FIELD}_REQUIRED("{PREFIX}_001", HttpStatus.BAD_REQUEST, "{필드}는 필수입니다"),
    {DOMAIN}_NOT_FOUND("{PREFIX}_002", HttpStatus.NOT_FOUND, "{도메인}을 찾을 수 없습니다"),
    {DOMAIN}_ACCESS_DENIED("{PREFIX}_003", HttpStatus.FORBIDDEN, "{도메인}에 접근할 권한이 없습니다");

    private final String code;
    private final HttpStatus status;  // ← 필수! 누락하면 컴파일 오류
    private final String message;

    {Domain}ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    // 모든 메서드 구현 필수
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {  // ← 이 메서드 반드시 구현!
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

### ⚠️ 주의사항

1. **HttpStatus 필드 필수**: `getStatus()` 메서드 구현을 위해 반드시 포함
2. **생성자 매개변수 순서**: `(String code, HttpStatus status, String message)`
3. **모든 인터페이스 메서드 구현**: `getCode()`, `getStatus()`, `getMessage()` 필수

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
| `PageResponse.of(page)` | `PageResponse.of(page, dto -> dto)` |
| `@Builder 초기값 무시` | `@Builder.Default private Boolean isPublic = true;` |

### 🔧 Builder 패턴 주의사항

#### ✅ 올바른 @Builder.Default 사용
```java
@Builder
@Data
public class LabResourceCreateRequestDto {
    @NotBlank
    private String title;
    
    @NotNull
    private ResourceCategory category;
    
    // ✅ 필수: @Builder.Default로 초기값 보호
    @Builder.Default
    @Schema(description = "공개 여부", defaultValue = "true")
    private Boolean isPublic = true;
}
```

#### ❌ 잘못된 Builder 사용 (초기값 무시됨)
```java
@Builder
@Data
public class LabResourceCreateRequestDto {
    private String title;
    private ResourceCategory category;
    
    // ❌ @Builder.Default 누락 - 초기값이 무시됨!
    private Boolean isPublic = true;  // Builder 사용 시 null이 될 수 있음
}
```

#### 🔍 Builder Pattern 필수 확인사항
1. **초기값이 있는 필드**: 반드시 `@Builder.Default` 어노테이션 추가
2. **Boolean 필드 특히 주의**: null 방지를 위해 `@Builder.Default` 필수
3. **Lombok @Builder**: 초기값 표현식을 무시하므로 명시적 어노테이션 필요

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