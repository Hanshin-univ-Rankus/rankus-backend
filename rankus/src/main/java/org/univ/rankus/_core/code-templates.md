# 코드 템플릿 모음 (AI 전용)

> 복사-붙여넣기 가능한 표준 코드 템플릿 (200줄 이하)

## 🏗️ Entity 템플릿

### 기본 Entity

```java
@Entity @Table(name = "{snake_case_table}")
public class {Domain} extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String {field};
    
    protected {Domain}() {} // JPA only
    
    private {Domain}(String {field}) {
        // 검증 로직
        if ({field} == null || {field}.isBlank()) {
            throw new {Domain}ValidationException({Domain}ErrorCode.{FIELD}_REQUIRED);
        }
        this.{field} = {field};
    }
    
    public static {Domain} create(String {field}) {
        return new {Domain}({field});
    }
    
    public void update{Field}(String {field}) {
        // 검증 로직
        this.{field} = {field};
    }
    
    // Getters only
    public Long getId() { return id; }
    public String get{Field}() { return {field}; }
}
```

### 상태 관리 Entity (PENDING → APPROVED/REJECTED 패턴)

```java
@Entity @Table(name = "{snake_case_table}")
public class {Domain} extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String {field};
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private {Domain}Status status = {Domain}Status.PENDING;
    
    protected {Domain}() {}
    
    private {Domain}(String {field}) {
        this.{field} = {field};
        this.status = {Domain}Status.PENDING;
    }
    
    public static {Domain} create(String {field}) {
        return new {Domain}({field});
    }
    
    public void approve() {
        if (status != {Domain}Status.PENDING) {
            throw new {Domain}ValidationException({Domain}ErrorCode.CANNOT_CHANGE_STATUS);
        }
        this.status = {Domain}Status.APPROVED;
    }
    
    public void reject() {
        if (status != {Domain}Status.PENDING) {
            throw new {Domain}ValidationException({Domain}ErrorCode.CANNOT_CHANGE_STATUS);
        }
        this.status = {Domain}Status.REJECTED;
    }
    
    public boolean isPending() { return status == {Domain}Status.PENDING; }
    public boolean isApproved() { return status == {Domain}Status.APPROVED; }
    public boolean isRejected() { return status == {Domain}Status.REJECTED; }
}
```

### 연관관계 Entity (ManyToOne 패턴)

```java
@Entity @Table(name = "{snake_case_table}")
public class {Domain}extends

BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "{parent}_id", nullable = false)
    private {
        Parent
    } {
        parent
    } ;
    
    @Column(nullable = false)
    private String {
        field
    } ;

    protected {
        Domain
    } () {
    }

    private {
        Domain
    } ({Parent} {
        parent
    },String {
        field
    }){
        // 검증 로직
        if ({parent} == null) {
            throw new {
                Domain
            } ValidationException({Domain}ErrorCode. {
                PARENT
            } _REQUIRED);
        }
        this. {
            parent
        } ={
            parent
        } ;
        this. {
            field
        } ={
            field
        } ;
    }

    public static {
        Domain
    } create({Parent} {
        parent
    },String {
        field
    }){
        return new {
            Domain
        } ({parent}, {field});
    }

    public boolean isOwnedBy ({Parent} {
        parent
    }){
        return this. {
            parent
        }.equals({parent});
    }
}
```

## 🎯 Service 템플릿

### Command Service

```java
@Service
@RequiredArgsConstructor
public class {Domain}CommandService implements{Domain}

CommandUseCase {
    private final {
        Domain
    } RepositoryPort {
        domain
    } RepositoryPort;

    @Override
    @Transactional
    public {
        Domain
    } create {
        Domain
    } ({Domain} CreateRequestDto request){
        // 1. 검증
        // 2. 도메인 생성
        {
            Domain
        } {
            domain
        } ={
            Domain
        }.create(request.getName());
        // 3. 저장
        return {domain} RepositoryPort.save({domain});
    }

    @Override
    @Transactional
    public {
        Domain
    } update {
        Domain
    } (Long id, {Domain} UpdateRequestDto request){
        // 1. 조회
        {
            Domain
        } {
            domain
        } ={
            domain
        } RepositoryPort.findById(id)
                .orElseThrow(() -> new {
            Domain
        } NotFoundException(id));
        // 2. 업데이트
        {
            domain
        }.update {
            Field
        } (request.getName());
        // 3. 저장
        return {domain} RepositoryPort.save({domain});
    }

    @Override
    @Transactional
    public void delete {
        Domain
    } (Long id){
        {
            Domain
        } {
            domain
        } ={
            domain
        } RepositoryPort.findById(id)
                .orElseThrow(() -> new {
            Domain
        } NotFoundException(id));
        {
            domain
        } RepositoryPort.delete({domain});
    }
}
```

### Query Service

```java
@Service
@RequiredArgsConstructor
public class {Domain}QueryService implements{Domain}

QueryUseCase {
    private final {
        Domain
    } RepositoryPort {
        domain
    } RepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public {
        Domain
    } find {
        Domain
    } ById(Long id) {
        return {domain} RepositoryPort.findById(id)
                .orElseThrow(() -> new {
            Domain
        } NotFoundException(id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List < {Domain} > findAll {
        Domain
    } s() {
        return {domain} RepositoryPort.findAll();
    }
}
```

## 🌐 Controller 템플릿

### 기본 CRUD Controller

```java
@RestController
@RequestMapping("/api/{domains}")
@RequiredArgsConstructor
@Validated
public class {Domain}Controller {
    private final {Domain}CommandUseCase {domain}CommandUseCase;
    private final {Domain}QueryUseCase {domain}QueryUseCase;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
            @Valid @RequestBody {Domain}CreateRequestDto request) {
        {Domain} {domain} = {domain}CommandUseCase.create{Domain}(request);
        {Domain}ResponseDto response = {Domain}ResponseDto.from({domain});
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> get{Domain}(
            @PathVariable Long id) {
        {Domain} {domain} = {domain}QueryUseCase.find{Domain}ById(id);
        {Domain}ResponseDto response = {Domain}ResponseDto.from({domain});
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> update{Domain}(
            @PathVariable Long id,
            @Valid @RequestBody {Domain}UpdateRequestDto request) {
        {Domain} {domain} = {domain}CommandUseCase.update{Domain}(id, request);
        {Domain}ResponseDto response = {Domain}ResponseDto.from({domain});
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete{Domain}(@PathVariable Long id) {
        {domain}CommandUseCase.delete{Domain}(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
```

### 랩실 소속 리소스 Controller

```java
@RestController
@RequestMapping("/api/labs/{labId}/{domains}")
@RequiredArgsConstructor
@Validated
public class {Domain}Controller {
    private final {Domain}CommandUseCase {domain}CommandUseCase;
    private final {Domain}QueryUseCase {domain}QueryUseCase;
    
    @PostMapping
    @PreAuthorize("@{domain}PermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_{DOMAINS}')")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> create{Domain}(
            @PathVariable Long labId,
            @Valid @RequestBody {Domain}CreateRequestDto request) {
        {Domain} {domain} = {domain}CommandUseCase.create{Domain}(labId, request);
        {Domain}ResponseDto response = {Domain}ResponseDto.from({domain});
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }
    
    @GetMapping
    @PreAuthorize("@{domain}PermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'VIEW_{DOMAINS}')")
    public ResponseEntity<ApiResponse<List<{Domain}ResponseDto>>> get{Domain}List(
            @PathVariable Long labId) {
        List<{Domain}> {domains} = {domain}QueryUseCase.find{Domain}sByLabId(labId);
        List<{Domain}ResponseDto> response = {domains}.stream()
            .map({Domain}ResponseDto::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/activate")
    @PreAuthorize("@{domain}PermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_{DOMAINS}')")
    public ResponseEntity<ApiResponse<{Domain}ResponseDto>> activate{Domain}(
            @PathVariable Long labId,
            @PathVariable Long id) {
        {Domain} {domain} = {domain}CommandUseCase.activate{Domain}(id);
        {Domain}ResponseDto response = {Domain}ResponseDto.from({domain});
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### 상태 변경 Controller (approve/reject 패턴)

```java
@PostMapping("/{id}/approve")
@

PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #labId, 'Lab', 'APPROVE_{DOMAINS}')")
public ResponseEntity<ApiResponse

< {
    Domain
}

ResponseDto>>

approve {
    Domain
}(
@PathVariable
Long labId,
@PathVariable
Long id){
        {Domain}{domain}={domain}CommandUseCase.

approve {
    Domain
}(id);
        {Domain}
ResponseDto response = {Domain}ResponseDto.

from( {
    domain
});
        return ResponseEntity.

ok(ApiResponse.success(response));
        }

        @

PostMapping("/{id}/reject")
@

PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #labId, 'Lab', 'APPROVE_{DOMAINS}')")
public ResponseEntity<ApiResponse

< {
    Domain
}

ResponseDto>>

reject {
    Domain
}(
@PathVariable
Long labId,
@PathVariable
Long id){
        {Domain}{domain}={domain}CommandUseCase.

reject {
    Domain
}(id);
        {Domain}
ResponseDto response = {Domain}ResponseDto.

from( {
    domain
});
        return ResponseEntity.

ok(ApiResponse.success(response));
        }
```

## 🗂️ Repository 템플릿

### Port Interface

```java
public interface {Domain}

RepositoryPort {
    {
        Domain
    } save({Domain} {
        domain
    });
    Optional < {Domain} > findById(Long id);
    List < {Domain} > findAll();
    void delete ({Domain} {
        domain
    });
    boolean existsById (Long id);
}
```

### Adapter Implementation

```java
@Component
@RequiredArgsConstructor
public class {Domain}RepositoryAdapter implements{Domain}

RepositoryPort {
    private final SpringData {
        Domain
    } Repository repository;

    @Override
    public {
        Domain
    } save({Domain} {
        domain
    }){
        return repository.save({domain});
    }
    
    @Override
    public Optional < {Domain} > findById(Long id) {
        return repository.findById(id);
    }
    
    @Override
    public List < {Domain} > findAll() {
        return repository.findAll();
    }

    @Override
    public void delete ({Domain} {
        domain
    }){
        repository.delete({domain});
    }

    @Override
    public boolean existsById (Long id){
        return repository.existsById(id);
    }
}
```

## 📄 DTO 템플릿

### Request DTO

```java
public record {
    Domain
}

CreateRequestDto(
        @NotBlank(message = "{필드}는 필수입니다")
        @Size(max = 100, message = "{필드}는 100자 이하여야 합니다")
        String {
    field
}
){}

public record {
    Domain
}

UpdateRequestDto(
        @NotBlank(message = "{필드}는 필수입니다")
        @Size(max = 100, message = "{필드}는 100자 이하여야 합니다")
        String {
    field
}
){}
```

### Response DTO

```java
public record {
    Domain
}

ResponseDto(
        Long id,
        String {
    field
},
LocalDateTime createdAt,
LocalDateTime updatedAt
){

public static {
    Domain
}

ResponseDto from( {
    Domain
} {domain}){
        return new{Domain}

ResponseDto( {
    domain
}.

getId(), {
    domain
}.

get {
    Field
}(),
        {domain}.

getCreatedAt(), {
    domain
}.

getUpdatedAt()
        );
                }
                }
```

## ⚠️ 예외 템플릿

### ValidationException

```java
public class {Domain}ValidationException extends

BaseCustomException {
    public {
        Domain
    } ValidationException({Domain}ErrorCode errorCode) {
        super(errorCode);
    }

    public {
        Domain
    } ValidationException({Domain}ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
```

### NotFoundException

```java
public class {Domain}NotFoundException extends

BaseCustomException {
    public {
        Domain
    } NotFoundException() {
        super({Domain}ErrorCode. {
            DOMAIN
        } _NOT_FOUND);
    }

    public {
        Domain
    } NotFoundException(Long id) {
        super({Domain}ErrorCode. {
            DOMAIN
        } _NOT_FOUND,
                String.format("{Domain}을(를) 찾을 수 없습니다. ID: %d", id));
    }
}
```

### ErrorCode Enum

```java
public enum {Domain}ErrorCode implements

ErrorCode {
    // 400 Bad Request
    {
        FIELD
    } _REQUIRED("{PREFIX}_001", HttpStatus.BAD_REQUEST, "{필드}는 필수입니다"),
            {FIELD} _TOO_LONG("{PREFIX}_002", HttpStatus.BAD_REQUEST, "{필드}가 너무 깁니다"),

            // 404 Not Found
            {DOMAIN} _NOT_FOUND("{PREFIX}_404", HttpStatus.NOT_FOUND, "{도메인}을(를) 찾을 수 없습니다"),

            // 409 Conflict
            {FIELD} _DUPLICATED("{PREFIX}_409", HttpStatus.CONFLICT, "이미 사용 중인 {필드}입니다");

    private final String code;
    private final HttpStatus status;
    private final String message;

    {
        Domain
    } ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override
    public String getCode () {
        return code;
    }
    @Override
    public HttpStatus getStatus () {
        return status;
    }
    @Override
    public String getMessage () {
        return message;
    }
}
```

---

**사용법**: `{Domain}`, `{field}`, `{domains}` 등을 실제 값으로 치환하여 사용
**업데이트**: 2025-01-04 | **라인 수**: 198줄