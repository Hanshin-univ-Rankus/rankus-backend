# User Domain 코딩 컨벤션

> User 관련 도메인 객체의 네이밍, 구조, 구현 패턴

## 📛 네이밍 컨벤션

### 클래스 네이밍
- **Entity**: `User` (도메인 개념 그대로)
- **Value Object**: `Password` (도메인 개념 + 접미사 없음)
- **Enum**: `Role` (단수형)
- **Exception**: `User{Specific}Exception` (@exception/CONVENTIONS.md 참조)
- **ErrorCode**: `UserErrorCode`, `PasswordErrorCode` (@exception/CONVENTIONS.md 참조)

### 메서드 네이밍
- **조회**: `get{Property}()`, `is{Condition}()`
- **검증**: `check{Condition}()`, `validate{Property}()`
- **상태변경**: `change{Property}()`, `assign{Property}()`
- **권한확인**: `isLabLeaderOrLabManagerInLab()`

### 필드 네이밍
- **기본**: camelCase
- **상수**: UPPER_SNAKE_CASE
- **컬럼매핑**: snake_case (DB 컬럼명)

## 🏗️ 클래스 구조 패턴

### User Entity 구조
```java
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    
    // 1. 필드 (private, final 선호)
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    // 2. 생성자 (protected, 팩토리 메서드 활용)
    protected User() {} // JPA 전용
    
    private User(String name, String email, Password password, Role role) {
        // 검증 로직
        // 필드 초기화
    }
    
    // 3. 팩토리 메서드 (public static)
    public static User create(String name, String email, String rawPassword, Role role, PasswordEncoder encoder) {
        return new User(name, email, Password.fromRaw(rawPassword, encoder), role);
    }
    
    // 4. 비즈니스 메서드 (public)
    public void checkPassword(String rawPassword) { /* 구현 */ }
    public void changePassword(String newRawPassword) { /* 구현 */ }
    
    // 5. 유틸리티 메서드 (public)
    public boolean isLabLeaderOrLabManagerInLab(Lab lab) { /* 구현 */ }
    
    // 6. Getter (필요한 것만 public)
    public Long getId() { return id; }
    public String getName() { return name; }
    
    // 7. 검증 메서드 (private)
    private void validateName(String name) { /* 구현 */ }
    
    // 8. equals/hashCode (id 기반)
    @Override
    public boolean equals(Object obj) { /* 구현 */ }
}
```

### Value Object 구조 (Password)
```java
@Embeddable
public class Password {
    
    // 1. 필드 (private final)
    @Column(name = "password", length = 255, nullable = false)
    private final String value;
    
    // 2. 생성자 (private)
    private Password(String hashedPassword) {
        this.value = hashedPassword;
    }
    
    // 3. 팩토리 메서드 (public static)
    public static Password fromRaw(String rawPassword, PasswordEncoder encoder) {
        validatePassword(rawPassword);
        return new Password(encoder.encode(rawPassword));
    }
    
    // 4. 비즈니스 메서드
    public boolean matches(String rawPassword, PasswordEncoder encoder) { /* 구현 */ }
    
    // 5. 검증 메서드 (private static)
    private static void validatePassword(String password) { /* 구현 */ }
    
    // 7. equals/hashCode (value 기반)
    @Override
    public boolean equals(Object obj) { /* 구현 */ }
}
```

## 🔒 검증 규칙 패턴

### 엔티티 검증
```java
// 생성자 또는 setter에서 검증
private User(String name, String email, Password password, Role role) {
    validateName(name);
    validateEmail(email);
    Objects.requireNonNull(password, "비밀번호는 필수입니다");
    Objects.requireNonNull(role, "역할은 필수입니다");
    
    this.name = name.trim();
    this.email = email.toLowerCase().trim();
    this.password = password;
    this.role = role;
}

private void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
        throw new UserValidationException(UserErrorCode.INVALID_NAME);
    }
    if (name.length() > 30) {
        throw new UserValidationException(UserErrorCode.NAME_TOO_LONG);
    }
}
```

### Value Object 검증
```java
private static void validatePassword(String password) {
    if (password == null || password.isEmpty()) {
        throw new PasswordValidationException(PasswordErrorCode.PASSWORD_REQUIRED);
    }
    if (password.length() < 8) {
        throw new PasswordValidationException(PasswordErrorCode.PASSWORD_TOO_SHORT);
    }
    if (password.length() > 255) {
        throw new PasswordValidationException(PasswordErrorCode.PASSWORD_TOO_LONG);
    }
}
```

## ⚠️ 예외 처리 패턴

User 도메인 예외 처리에 대한 상세 내용은 @exception/CONVENTIONS.md를 참조하세요.

## 🎭 Enum 구현 패턴

### Role Enum
```java
public enum Role {
    STUDENT("학생"),
    LAB_MEMBER("랩실 멤버"),
    LAB_MANAGER("랩실 관리자"),
    LAB_LEADER("랩장"),
    PROFESSOR("교수"),
    ADMIN("관리자");
    
    private final String description;
    
    Role(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    // 권한 레벨 비교 메서드
    public boolean hasHigherOrEqualAuthorityThan(Role other) {
        return this.ordinal() >= other.ordinal();
    }
    
    // 특정 권한 확인 메서드
    public boolean isLabManager() {
        return this == LAB_MANAGER || this == LAB_LEADER;
    }
}
```

## 🔗 연관관계 처리 패턴

### ManyToOne 매핑 (User → Lab)
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "lab_id")
private Lab lab;

// 연관관계 편의 메서드
public void assignLab(Lab lab) {
    // 기존 연관관계 해제
    if (this.lab != null) {
        this.lab.removeMember(this);
    }
    
    // 새로운 연관관계 설정
    this.lab = lab;
    if (lab != null) {
        lab.addMember(this);
    }
}

public void leaveLab() {
    if (this.lab != null) {
        this.lab.removeMember(this);
        this.lab = null;
    }
}
```

## 📋 비즈니스 로직 패턴

### 권한 확인 로직
```java
public boolean isLabLeaderOrLabManagerInLab(Lab targetLab) {
    // null 체크
    if (this.lab == null || targetLab == null) {
        return false;
    }
    
    // 소속 랩실 확인
    if (!this.lab.equals(targetLab)) {
        return false;
    }
    
    // 권한 확인
    return this.role == Role.LAB_LEADER || 
           this.role == Role.LAB_MANAGER ||
           this.role == Role.PROFESSOR;
}
```

### 상태 변경 로직
```java
public void changePassword(String newRawPassword) {
    // 검증 (PasswordEncoder 주입 필요)
    Password newPassword = Password.fromRaw(newRawPassword, encoder);
    
    // 기존 비밀번호와 동일한지 확인
    if (this.password.matches(newRawPassword, encoder)) {
        throw new UserValidationException(UserErrorCode.SAME_AS_CURRENT_PASSWORD);
    }
    
    // 변경
    this.password = newPassword;
}
```

## 🧪 테스트 작성 패턴

### 도메인 객체 테스트
```java
class UserTest {
    
    @Test
    void 사용자_생성시_유효한_정보로_생성된다() {
        // given
        String name = "홍길동";
        String email = "hong@example.com";
        String password = "password123!";
        Role role = Role.STUDENT;
        
        // when
        User user = User.create(name, email, password, role, mockEncoder);
        
        // then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRole()).isEqualTo(role);
    }
    
    @Test
    void 잘못된_이름으로_사용자_생성시_예외가_발생한다() {
        // given
        String invalidName = "";
        
        // when & then
        assertThatThrownBy(() -> 
            User.create(invalidName, "test@example.com", "password123!", Role.STUDENT, mockEncoder)
        ).isInstanceOf(UserValidationException.class);
    }
}
```

### Value Object 테스트
```java
class PasswordTest {
    
    @Test
    void 유효한_비밀번호로_Password_객체를_생성할_수_있다() {
        // given
        String rawPassword = "password123!";
        
        // when
        Password password = Password.fromRaw(rawPassword, mockEncoder);
        
        // then
        assertThat(password.matches(rawPassword, mockEncoder)).isTrue();
    }
    
    @Test
    void 짧은_비밀번호로_생성시_예외가_발생한다() {
        // given
        String shortPassword = "123";
        
        // when & then
        assertThatThrownBy(() -> Password.fromRaw(shortPassword, mockEncoder))
            .isInstanceOf(PasswordValidationException.class);
    }
}
```

## 🎯 주요 규칙 요약

1. **불변성 보장**: Value Object는 immutable로 설계
2. **검증 우선**: 객체 생성 시점에 모든 검증 수행
3. **예외 활용**: 도메인 규칙 위반 시 명확한 예외 발생
4. **캡슐화**: 내부 구현은 private, 인터페이스만 public
5. **팩토리 메서드**: 복잡한 생성 로직은 정적 팩토리 메서드 활용
6. **연관관계 관리**: 양방향 연관관계는 편의 메서드로 일관성 보장