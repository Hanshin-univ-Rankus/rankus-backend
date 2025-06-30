# 표준 코드 템플릿

> 🏗️ **패턴 가이드**: @core/patterns.md  
> 📋 **네이밍 규칙**: @core/conventions.md

## 🎯 핵심 템플릿

### Config 클래스
```java
@Configuration @Enable{Feature} @RequiredArgsConstructor
public class {Feature}Config {
    private final {Feature}Properties properties;
    
    @Bean @ConditionalOnMissingBean
    public {Type} {beanName}() { return new {Type}(); }
    
    @Bean @Profile("!test")
    public {Type} {productionBean}() { return new {Type}(); }
}
```

### Security Config
```java
@Configuration @EnableWebSecurity @EnableMethodSecurity
@Profile("!default") @RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    
    @Bean public SecurityFilterChain filterChain(HttpSecurity http) {
        // CSRF 비활성화, Stateless, 공개 API 허용, JWT 필터 추가
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```

### BaseTimeEntity
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

### BaseCustomException
```java
public abstract class BaseCustomException extends RuntimeException {
    private final ErrorCode errorCode;
    
    protected BaseCustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public int getStatusCode() { return errorCode.getStatusCode(); }
    public ErrorCode getErrorCode() { return errorCode; }
}
```

### ErrorCode Enum
```java
public enum {Domain}ErrorCode implements ErrorCode {
    // 400 Bad Request
    {FIELD}_REQUIRED("001", BAD_REQUEST, "{필드}는 필수입니다"),
    {FIELD}_INVALID("002", BAD_REQUEST, "{필드} 형식이 올바르지 않습니다"),
    
    // 404 Not Found
    {DOMAIN}_NOT_FOUND("010", NOT_FOUND, "{도메인}을 찾을 수 없습니다"),
    
    // 409 Conflict
    {FIELD}_DUPLICATED("020", CONFLICT, "이미 사용 중인 {필드}입니다"),
    
    // 422 Unprocessable Entity
    CANNOT_CHANGE_STATUS("030", UNPROCESSABLE_ENTITY, "상태를 변경할 수 없습니다");
    
    private final String code;
    private final HttpStatus status;
    private final String message;
    
    // 생성자 및 getter 구현
}
```

### ErrorResponse
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String code;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    
    public static ErrorResponse of(ErrorCode errorCode) { /* 구현 */ }
    public static ErrorResponse of(ErrorCode errorCode, String message, String path) { /* 구현 */ }
}
```

### GlobalExceptionHandler
```java
@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(BaseCustomException e, HttpServletRequest request) {
        log.error("Business exception: {}", e.getMessage(), e);
        ErrorResponse response = ErrorResponse.of(e.getErrorCode(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(e.getStatusCode()).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation failed: {}", e.getMessage());
        ValidationErrorResponse response = ValidationErrorResponse.fromBindingResult(e.getBindingResult());
        response.setPath(request.getRequestURI());
        return ResponseEntity.badRequest().body(response);
    }
}
```

### Properties 클래스
```java
@ConfigurationProperties(prefix = "app.feature")
@Component @Data @Validated
public class FeatureProperties {
    @NotBlank private String name;
    @Min(1) @Max(100) private int maxSize = 10;
    private boolean enabled = true;
    private List<String> allowedValues = new ArrayList<>();
    private Duration timeout = Duration.ofSeconds(30);
    
    @PostConstruct void validate() { /* 필수 설정 검증 */ }
}
```

## 🧪 테스트 템플릿

### Config 테스트
```java
@SpringBootTest @TestPropertySource(properties = {"app.feature.enabled=true", "app.feature.max-size=50"})
class ConfigTest {
    @Autowired FeatureProperties featureProperties;
    
    @Test void 설정값_로딩_검증() { /* enabled: true, maxSize: 50 검증 */ }
}
```

### 보안 설정 테스트
```java
class SecurityConfigTest {
    @Test void 인증_없이_보호된_API_접근시_401() { /* MockMvc → 401 검증 */ }
    @Test void 공개_API는_인증_없이_접근_가능() { /* /api/labs → 200 OK */ }
}
```

### 예외 테스트
```java
class {Domain}NotFoundExceptionTest {
    @Test void 기본_생성자_테스트() { /* ErrorCode, 상태코드, 메시지 검증 */ }
    @Test void ID_파라미터_생성자_테스트() { /* 메시지에 ID 포함 검증 */ }
}
```

### JWT 테스트
```java
class JwtTokenProviderTest {
    @Test void 유효한_토큰_생성_검증() { /* 토큰 생성 → 검증 → 이메일/역할 추출 */ }
    @Test void 만료된_토큰_검증_실패() { /* 만료 토큰 → validateToken() false */ }
}
```

## 🏗️ 계층별 템플릿

### Domain Layer
```java
// Entity 템플릿
@Entity @Table(name = "{table_name}")
public class {Domain} extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY) private Long id;
    
    protected {Domain}() {} // JPA
    private {Domain}(...) { validate(); }
    public static {Domain} create(...) { return new {Domain}(...); }
}

// Value Object 템플릿
@Embeddable
public class {ValueObject} {
    @Column(name = "{column}", length = {length}, nullable = false)
    private final {Type} value;
    
    private {ValueObject}({Type} value) { validate(value); this.value = value; }
    public static {ValueObject} from({Type} value) { return new {ValueObject}(value); }
}
```

### Application Layer
```java
// UseCase 템플릿
public interface {Domain}CommandUseCase {
    {Domain}ResponseDto create{Domain}({Domain}CreateRequestDto request);
    {Domain}ResponseDto update{Domain}(Long id, {Domain}UpdateRequestDto request);
    void delete{Domain}(Long id);
}

// Service 템플릿
@Service @Transactional @RequiredArgsConstructor
public class {Domain}CommandService implements {Domain}CommandUseCase {
    private final {Domain}RepositoryPort {domain}RepositoryPort;
    
    @Override
    public {Domain}ResponseDto create{Domain}({Domain}CreateRequestDto request) {
        {Domain} {domain} = {Domain}.create(request.get...());
        return {Domain}ResponseDto.from({domain}RepositoryPort.save({domain}));
    }
}
```

### Adapter Layer
```java
// Controller 템플릿
@RestController @RequestMapping("/api/{domains}") @RequiredArgsConstructor
@Tag(name = "{Domain} API")
public class {Domain}Controller {
    private final {Domain}CommandUseCase {domain}CommandUseCase;
    
    @PostMapping @Operation(summary = "{Domain} 생성")
    public ResponseEntity<{Domain}ResponseDto> create{Domain}(
            @Valid @RequestBody {Domain}CreateRequestDto request) {
        return ResponseEntity.status(CREATED).body({domain}CommandUseCase.create{Domain}(request));
    }
}

// Repository 템플릿
@Repository @RequiredArgsConstructor
public class {Domain}RepositoryAdapter implements {Domain}RepositoryPort {
    private final {Domain}JpaRepository {domain}JpaRepository;
    
    @Override
    public {Domain} save({Domain} {domain}) { return {domain}JpaRepository.save({domain}); }
    @Override
    public Optional<{Domain}> findById(Long id) { return {domain}JpaRepository.findById(id); }
}
```

### DTO 템플릿
```java
// Request DTO
public record {Domain}CreateRequestDto(
    @NotBlank(message = "{필드}는 필수입니다") String {field1},
    @NotNull(message = "{필드}는 필수입니다") {Type} {field2}
) {}

// Response DTO
public record {Domain}ResponseDto(Long id, String {field1}, {Type} {field2}) {
    public static {Domain}ResponseDto from({Domain} {domain}) {
        return new {Domain}ResponseDto({domain}.getId(), {domain}.get{Field1}(), {domain}.get{Field2}());
    }
}
```

## 🧪 테스트 템플릿 확장

### Unit Test
```java
@ExtendWith(MockitoExtension.class)
class {Domain}CommandServiceTest {
    @Mock private {Domain}RepositoryPort {domain}RepositoryPort;
    @InjectMocks private {Domain}CommandService {domain}CommandService;
    
    @Test void {scenario}() {
        // given - when - then 패턴
        // Mock 설정 - 메서드 호출 - 결과 검증
    }
}
```

### Integration Test
```java
@SpringBootTest @TestPropertySource("/application-test.properties")
@Transactional @TestMethodOrder(OrderAnnotation.class)
class {Domain}IntegrationTest {
    @Autowired {Domain}CommandUseCase {domain}CommandUseCase;
    
    @Test @Order(1) void {scenario}() {
        // 실제 DB 연동 테스트
    }
}
```

## 🎯 템플릿 사용법

### 변수 치환 매트릭스
| 변수 | 설명 | 예시 |
|------|------|------|
| `{Domain}` | PascalCase 도메인명 | `User`, `LabApplication` |
| `{domain}` | camelCase 도메인명 | `user`, `labApplication` |
| `{domains}` | 복수형 URL | `users`, `lab-applications` |
| `{field}`, `{Field}` | 필드명 (camel/Pascal) | `name`, `Name` |
| `{Type}` | 데이터 타입 | `String`, `LocalDateTime` |
| `{table_name}` | DB 테이블명 | `user`, `lab_application` |

### 적용 절차
1. **도메인 식별**: Domain, domain, domains 결정
2. **필드 정의**: field, Type 등 구체화  
3. **특화 로직**: 도메인별 비즈니스 메서드 추가
4. **테스트 작성**: 템플릿 기반 테스트 케이스