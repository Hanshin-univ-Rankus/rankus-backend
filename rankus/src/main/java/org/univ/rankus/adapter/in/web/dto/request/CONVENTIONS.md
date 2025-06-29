# Request DTO 코딩 컨벤션

> HTTP 요청 데이터를 처리하는 Request DTO 클래스의 네이밍, 구조, 검증 패턴

## 📛 네이밍 컨벤션

### 클래스 네이밍
- **기본 패턴**: `{Domain}{Action}RequestDto`
- **생성**: `UserCreateRequestDto`, `LabApplicationCreateRequestDto`
- **수정**: `UserUpdateRequestDto`, `LabUpdateRequestDto`
- **로그인**: `UserLoginRequestDto`
- **등록**: `UserRegisterRequestDto`

### 필드 네이밍
- **camelCase 사용**: `firstName`, `lastName`, `interviewTime`
- **boolean 필드**: `isActive`, `hasPermission` (is 접두사 선택적)
- **시간 필드**: `createdAt`, `updatedAt`, `scheduledAt`

## 🏗️ 클래스 구조 패턴

### 기본 Request DTO 구조
```java
public class {Domain}{Action}RequestDto {
    
    // 1. 검증 어노테이션과 함께 필드 선언
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 30, message = "이름은 30자를 초과할 수 없습니다")
    private String name;
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String email;
    
    @NotNull(message = "역할은 필수입니다")
    private Role role;
    
    // 2. 기본 생성자 (Jackson 역직렬화용)
    public {Domain}{Action}RequestDto() {}
    
    // 3. 전체 필드 생성자 (Builder 패턴과 함께 사용)
    public {Domain}{Action}RequestDto(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
    
    // 4. Getter 메서드 (필드별로)
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    
    // 5. Builder 패턴 (선택적, Lombok 사용 권장)
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String name;
        private String email;
        private Role role;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder role(Role role) {
            this.role = role;
            return this;
        }
        
        public {Domain}{Action}RequestDto build() {
            return new {Domain}{Action}RequestDto(name, email, role);
        }
    }
}
```

### Lombok 활용 구조
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {
    
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 30, message = "이름은 30자 이하여야 합니다")
    private String name;
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상 255자 이하여야 합니다")
    private String password;
    
    @NotNull(message = "역할은 필수입니다")
    private Role role;
}
```

## ✅ 검증 어노테이션 패턴

### 문자열 검증
```java
// 필수 문자열
@NotBlank(message = "필드명은 필수입니다")
private String requiredField;

// 길이 제한
@Size(max = 100, message = "필드명은 100자를 초과할 수 없습니다")
private String limitedField;

// 길이 범위
@Size(min = 8, max = 255, message = "비밀번호는 8자 이상 255자 이하여야 합니다")
private String password;

// 이메일 형식
@Email(message = "올바른 이메일 형식이 아닙니다")
private String email;

// URL 형식 검증 (이미지 URL 등)
@Pattern(regexp = "^(https?|ftp)://.*$", message = "올바른 URL 형식이 아닙니다")
@Size(max = 255, message = "URL은 255자 이하여야 합니다")
private String imageUrl;

// 정규식 패턴
@Pattern(regexp = "^[0-9]{10,11}$", message = "전화번호는 10-11자리 숫자여야 합니다")
private String phoneNumber;
```

### 숫자 검증
```java
// null 불허
@NotNull(message = "숫자 필드는 필수입니다")
private Integer requiredNumber;

// 최솟값
@Min(value = 0, message = "값은 0 이상이어야 합니다")
private Integer positiveNumber;

// 최댓값
@Max(value = 100, message = "값은 100 이하여야 합니다")
private Integer limitedNumber;

// 범위
@Min(1) @Max(10)
private Integer rangeNumber;

// 양수만
@Positive(message = "값은 양수여야 합니다")
private Integer positiveOnly;
```

### 날짜/시간 검증
```java
// 필수 날짜
@NotNull(message = "날짜는 필수입니다")
private LocalDateTime dateTime;

// 미래 날짜만
@Future(message = "미래 날짜여야 합니다")
private LocalDateTime futureDate;

// 과거 날짜만
@Past(message = "과거 날짜여야 합니다")
private LocalDateTime pastDate;

// 현재 또는 미래
@FutureOrPresent(message = "현재 또는 미래 날짜여야 합니다")
private LocalDateTime presentOrFuture;
```

### 컬렉션 검증
```java
// 비어있지 않은 리스트
@NotEmpty(message = "목록은 비어있을 수 없습니다")
private List<String> nonEmptyList;

// 크기 제한
@Size(min = 1, max = 10, message = "항목은 1개 이상 10개 이하여야 합니다")
private List<String> limitedList;

// 각 요소 검증
@Valid
private List<@NotBlank String> validatedStringList;
```

### 중첩 객체 검증
```java
// 중첩 객체 검증
@Valid
@NotNull(message = "주소 정보는 필수입니다")
private AddressDto address;

// 중첩 리스트 검증
@Valid
@NotEmpty(message = "이미지 목록은 비어있을 수 없습니다")
private List<@Valid LabImageRequestDto> images;
```

## 📝 커스텀 검증 어노테이션

### 커스텀 검증 어노테이션 정의
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "이미 사용 중인 이메일입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### 검증 로직 구현
```java
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    private final UserRepositoryPort userRepositoryPort;
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true; // @NotNull로 별도 검증
        }
        return !userRepositoryPort.existsByEmail(email);
    }
}
```

### 사용 예시
```java
public class UserCreateRequestDto {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @UniqueEmail  // 커스텀 검증
    private String email;
}
```

## 🔄 복잡한 Request DTO 패턴

### 중첩 리소스 Request DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabApplicationCreateRequestDto {
    
    @NotNull(message = "면접 시간은 필수입니다")
    @Future(message = "면접 시간은 미래여야 합니다")
    private LocalDateTime interviewTime;
    
    @Size(max = 500, message = "지원 동기는 500자 이하여야 합니다")
    private String motivation;
    
    @Valid
    @NotEmpty(message = "첨부 파일은 최소 1개 이상이어야 합니다")
    private List<@Valid AttachmentDto> attachments;
    
    @Data
    @Builder
    @NoArgsConstructor 
    @AllArgsConstructor
    public static class AttachmentDto {
        
        @NotBlank(message = "파일 이름은 필수입니다")
        private String fileName;
        
        @NotBlank(message = "파일 URL은 필수입니다")
        @URL(message = "올바른 URL 형식이 아닙니다")
        private String fileUrl;
        
        @NotNull(message = "파일 타입은 필수입니다")
        private AttachmentType type;
    }
}
```

### 이미지 업로드 Request DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabImageRequestDto {
    
    @NotBlank(message = "이미지 URL은 필수입니다")
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "올바른 URL 형식이 아닙니다")
    @Size(max = 255, message = "이미지 URL은 255자 이하여야 합니다")
    private String imageUrl;
    
    @NotNull(message = "이미지 타입은 필수입니다")
    private ImageType type;
}
```

### 조건부 검증 Request DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    
    @Size(max = 30, message = "이름은 30자를 초과할 수 없습니다")
    private String name;  // 선택적 수정
    
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String email;  // 선택적 수정
    
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상 255자 이하여야 합니다")
    private String newPassword;  // 선택적 수정
    
    // 비밀번호 변경시에만 필수
    private String currentPassword;
    
    // 커스텀 검증: 새 비밀번호가 있으면 현재 비밀번호도 필수
    @AssertTrue(message = "비밀번호 변경시 현재 비밀번호는 필수입니다")
    private boolean isPasswordChangeValid() {
        return newPassword == null || 
               (currentPassword != null && !currentPassword.isEmpty());
    }
}
```

## 🎯 DTO 변환 패턴

### Domain 객체로 변환
```java
public class UserCreateRequestDto {
    
    // ... 필드 및 검증
    
    // Domain 객체 생성 메서드
    public User toEntity() {
        return User.create(
            this.name,
            this.email,
            this.password,
            this.role
        );
    }
    
    // 또는 정적 팩토리 메서드
    public static User toEntity(UserCreateRequestDto dto) {
        return User.create(
            dto.getName(),
            dto.getEmail(),
            dto.getPassword(),
            dto.getRole()
        );
    }
}
```

### 서비스 레이어에서 변환
```java
// Request DTO는 단순히 데이터 홀더 역할만
// 변환 로직은 서비스 레이어에서 처리
@Service
public class UserCommandService {
    
    public UserResponseDto createUser(UserCreateRequestDto request) {
        // DTO → Domain 변환
        User user = User.create(
            request.getName(),
            request.getEmail(),
            request.getPassword(),
            request.getRole()
        );
        
        // 비즈니스 로직 처리
        User savedUser = userRepositoryPort.save(user);
        
        // Domain → Response DTO 변환
        return UserResponseDto.from(savedUser);
    }
}
```

## 🧪 Request DTO 테스트 패턴

### 검증 테스트
```java
class UserCreateRequestDtoTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void 유효한_요청_데이터로_검증_통과() {
        // given
        UserCreateRequestDto request = UserCreateRequestDto.builder()
            .name("홍길동")
            .email("hong@example.com")
            .password("password123!")
            .role(Role.STUDENT)
            .build();
        
        // when
        Set<ConstraintViolation<UserCreateRequestDto>> violations = 
            validator.validate(request);
        
        // then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void 빈_이름으로_검증_실패() {
        // given
        UserCreateRequestDto request = UserCreateRequestDto.builder()
            .name("")  // 빈 문자열
            .email("hong@example.com")
            .password("password123!")
            .role(Role.STUDENT)
            .build();
        
        // when
        Set<ConstraintViolation<UserCreateRequestDto>> violations = 
            validator.validate(request);
        
        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("이름은 필수입니다");
    }
}
```

## 📝 검증 메시지 표준화

### 메시지 패턴 가이드
```java
// 필수 필드
@NotBlank(message = "{필드명}은 필수입니다")
@NotNull(message = "{필드명}은 필수입니다")

// 길이 제한
@Size(max = 30, message = "{필드명}은 30자 이하여야 합니다")
@Size(min = 8, max = 255, message = "{필드명}은 8자 이상 255자 이하여야 합니다")

// 형식 검증
@Email(message = "올바른 이메일 형식이 아닙니다")
@Pattern(regexp = "^(https?|ftp)://.*$", message = "올바른 URL 형식이 아닙니다")

// 시간 제약
@Future(message = "{필드명}은 미래 시점이어야 합니다")
@Past(message = "{필드명}은 과거 시점이어야 합니다")

// 숫자 범위
@Min(value = 0, message = "{필드명}은 0 이상이어야 합니다")
@Max(value = 100, message = "{필드명}은 100 이하여야 합니다")
```

### 실제 적용 예시
```java
// ✅ 일관된 메시지 패턴
@NotBlank(message = "이름은 필수입니다")
@Size(max = 30, message = "이름은 30자 이하여야 합니다")
private String name;

// ❌ 비일관적인 메시지 패턴
@NotBlank(message = "이름을 입력해주세요")
@Size(max = 30, message = "이름이 너무 깁니다")
private String name;
```

## 📈 최근 개선사항 (2025년 6월)

### 향상된 검증 패턴
```java
// LabImageRequestDto - URL 패턴과 길이 제한 결합
@NotBlank(message = "이미지 URL은 필수입니다")
@Pattern(regexp = "^(https?|ftp)://.*$", message = "올바른 URL 형식이 아닙니다")
@Size(max = 255, message = "이미지 URL은 255자 이하여야 합니다")
private String imageUrl;

// UserRegisterRequestDto - 세분화된 길이 제한
@NotBlank(message = "이름은 필수입니다")
@Size(max = 30, message = "이름은 30자 이하여야 합니다")
private String name;

@NotBlank(message = "비밀번호는 필수입니다")
@Size(min = 8, max = 255, message = "비밀번호는 8자 이상 255자 이하여야 합니다")
private String password;

// LabApplicationRequestDto - 시간 검증 강화
@NotNull(message = "면접 시간은 필수입니다")
@Future(message = "면접 시간은 미래 시점이어야 합니다")
private LocalDateTime interviewTime;
```

### 표준화된 메시지 패턴
- **URL 검증**: `"올바른 URL 형식이 아닙니다"`
- **길이 제한**: `"{필드명}은 {숫자}자 이하여야 합니다"`
- **범위 제한**: `"{필드명}은 {최소}자 이상 {최대}자 이하여야 합니다"`
- **시간 검증**: `"{필드명}은 미래 시점이어야 합니다"`

## 🎯 주요 규칙 요약

1. **명확한 네이밍**: 도메인과 액션을 포함한 명확한 클래스명
2. **철저한 검증**: 모든 입력 필드에 적절한 검증 어노테이션 적용
3. **일관된 메시지**: 표준화된 검증 메시지 패턴 사용
4. **의미있는 메시지**: 사용자가 이해하기 쉬운 검증 메시지
5. **불변성 고려**: 가능한 final 필드 사용 또는 Builder 패턴 활용
6. **단순한 역할**: DTO는 데이터 전달에만 집중, 비즈니스 로직 배제
7. **테스트 가능**: 검증 로직에 대한 충분한 단위 테스트 작성
8. **복합 검증**: URL 형식과 길이 제한 등 여러 검증 조건 조합 활용