# AI 개발 필수 가이드

> AI가 코드 작성 시 반드시 준수해야 할 핵심 규칙과 패턴 (300줄 이하)

## 🏗️ 아키텍처 원칙 (20줄)

### 계층 구조
```
Adapter(Controller) → Application(Service) → Domain(Entity) → Adapter(Repository)
```

### 의존성 방향
- **Inbound**: Controller → UseCase ← Service
- **Outbound**: Service → RepositoryPort ← Repository
- **원칙**: 도메인이 인프라에 의존하지 않음

### 트랜잭션 패턴
- **Command**: `@Transactional`
- **Query**: `@Transactional(readOnly = true)`
- **위치**: Service 각 메서드에 적용

## 📛 네이밍 규칙 (30줄)

### 클래스 네이밍
```
Entity:     {Domain}                           # User, Lab
Service:    {Domain}{Command|Query}Service     # UserCommandService
Controller: {Domain}Controller                 # UserController
DTO:        {Domain}{Action}RequestDto         # UserCreateRequestDto
Exception:  {Domain}{Type}Exception            # UserNotFoundException
ErrorCode:  {Domain}ErrorCode                  # UserErrorCode
```

### 메서드 네이밍
```
Service:    create{Domain}(), find{Domain}ById(), update{Domain}()
Controller: create{Domain}(), get{Domain}(), update{Domain}()
Repository: save(), findById(), findBy{Property}()
```

### ErrorCode 네이밍
```
Prefix: USER, LAB, LAP, LIM, LCR
Pattern: {PREFIX}_{CODE}  # USER_001, LAB_404
Fields: {FIELD}_REQUIRED, {FIELD}_TOO_LONG, {DOMAIN}_NOT_FOUND
```

## 🏗️ 클래스 구조 템플릿 (50줄)

### Entity 템플릿
```java
@Entity @Table(name = "{table}")
public class {Domain} extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    protected {Domain}() {} // JPA only
    private {Domain}(/* params */) { /* validation */ }
    
    public static {Domain} create(/* params */) { return new {Domain}(/* params */); }
    public void {businessMethod}() { /* domain logic */ }
    
    // Only getters public
    public Long getId() { return id; }
}
```

### Service 템플릿
```java
@Service @RequiredArgsConstructor
public class {Domain}{Command|Query}Service implements {Domain}{Command|Query}UseCase {
    private final {Domain}RepositoryPort repository;
    
    @Override
    @Transactional  // Query: @Transactional(readOnly = true)
    public {Domain} {method}({RequestDto} request) {
        // 1. 검증/조회 → 2. 도메인 로직 → 3. 저장 → 4. Entity 반환
    }
}
```

### Controller 템플릿
```java
@RestController @RequestMapping("/api/{domains}") @RequiredArgsConstructor @Validated
public class {Domain}Controller {
    private final {Domain}{Command|Query}UseCase useCase;
    
    @PostMapping @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
            @Valid @RequestBody {Domain}CreateRequestDto request) {
        {Domain}ResponseDto response = useCase.create{Domain}(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }
}
```

## ⚠️ 예외 처리 패턴 (40줄)

### 예외 계층
```
BaseCustomException
└── {Domain}Exception (optional)
    ├── {Domain}NotFoundException
    ├── {Domain}ValidationException
    └── {Domain}BusinessException
```

### ErrorCode 템플릿
```java
public enum {Domain}ErrorCode implements ErrorCode {
    // 400 Bad Request
    {FIELD}_REQUIRED("{PREFIX}_001", HttpStatus.BAD_REQUEST, "{필드}는 필수입니다"),
    {FIELD}_TOO_LONG("{PREFIX}_002", HttpStatus.BAD_REQUEST, "{필드}가 너무 깁니다"),
    
    // 404 Not Found
    {DOMAIN}_NOT_FOUND("{PREFIX}_404", HttpStatus.NOT_FOUND, "{도메인}을(를) 찾을 수 없습니다"),
    
    // 409 Conflict
    {FIELD}_DUPLICATED("{PREFIX}_409", HttpStatus.CONFLICT, "이미 사용 중인 {필드}입니다");
    
    // 생성자 및 getter 메서드들...
}
```

### 예외 발생 패턴
```java
// 검증 실패
if (condition) {
    throw new {Domain}ValidationException({Domain}ErrorCode.{ERROR_CODE});
}

// 조회 실패
return repository.findById(id)
    .orElseThrow(() -> new {Domain}NotFoundException(id));
```

## 🧪 테스트 작성 체크리스트 (30줄)

### 필수 테스트 구조
```java
@ExtendWith(MockitoExtension.class)
class {Class}Test {
    @Mock private {Dependency} dependency;
    @InjectMocks private {Class} target;
    
    @Test
    void {행위}_시_{결과}가_발생한다() {
        // given - 테스트 데이터 준비
        // when - 실제 실행
        // then - 결과 검증
    }
}
```

### Mock 패턴
```java
// 성공 케이스
when(repository.findById(1L)).thenReturn(Optional.of(domain));
when(repository.save(any({Domain}.class))).thenReturn(savedDomain);

// 실패 케이스
when(repository.findById(999L)).thenReturn(Optional.empty());
```

### 검증 패턴
```java
// 예외 검증
assertThatThrownBy(() -> service.method())
    .isInstanceOf({Domain}NotFoundException.class);

// 상태 검증
assertThat(result.getStatus()).isEqualTo(EXPECTED_STATUS);
```

## 🔍 최신 코드 스타일 검증 (30줄)

### 작성 전 필수 체크리스트
```
□ 네이밍: 기존 Domain 클래스들의 네이밍 패턴 준수
□ 구조: Entity/Service/Controller 템플릿 정확히 적용
□ 예외: ErrorCode 접두사 할당 규칙 준수
□ 테스트: 프로덕션 코드와 1:1 매칭 구조
□ 트랜잭션: Command/Query 패턴 구분
□ 권한: @PreAuthorize 적절한 권한 설정
□ HTTP: 상태코드 매트릭스 준수 (201/200/404/400 등)
```

### 코드 품질 규칙
```
□ Import: Explicit imports only (wildcard 금지)
□ 생성자: Entity는 protected 기본 생성자 + static factory method
□ 검증: 도메인 생성 시점에 검증 로직 포함
□ DTO 변환: Controller에서 수행 (Service는 Entity 반환)
□ 메시지: 한국어, 명사형 종결 ("~습니다.")
```

### 자동 검증 대상
- 최근 20개 클래스의 네이밍 패턴 일치
- ErrorCode 할당 중복 여부
- 테스트 클래스 존재 여부
- @PreAuthorize 권한 설정 적절성

---

**업데이트**: 2025-01-04 | **파일 크기**: 약 280줄 | **토큰 최적화**: 기존 대비 95% 절약