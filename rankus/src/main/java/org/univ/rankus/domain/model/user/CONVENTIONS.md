# User Domain 컨벤션

## 📛 네이밍

| 구분           | 패턴                                           | 예시                        |
|--------------|----------------------------------------------|---------------------------|
| Entity       | Domain명                                      | `User`                    |
| Value Object | 개념명                                          | `Password`                |
| Enum         | 단수형                                          | `Role`                    |
| Exception    | `{Domain}{Type}Exception`                    | `UserValidationException` |
| Method-조회    | `get{Property}()`, `is{Condition}()`         | `getName()`, `isActive()` |
| Method-검증    | `check{Condition}()`, `validate{Property}()` | `checkPassword()`         |
| Method-변경    | `change{Property}()`, `assign{Property}()`   | `changePassword()`        |
| Field        | camelCase / UPPER_SNAKE_CASE                 | `name` / `MAX_LENGTH`     |

## 🏗️ 구조 패턴

### User Entity

```java
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    protected User() {} // JPA
    private User(...) { validate(); }
    
    public static User create(...) { return new User(...); }
    public void checkPassword(String raw) { ... }
    public boolean canManageLabApplications(Lab lab) { ... }
}
```

### Password Value Object

```java
@Embeddable
public class Password {
    @Column(name = "password", length = 255, nullable = false)
    private final String value;
    
    private Password(String hashedPassword) { this.value = hashedPassword; }
    
    public static Password fromRaw(String raw, PasswordEncoder encoder) {
        validate(raw); return new Password(encoder.encode(raw));
    }
    
    public boolean matches(String raw, PasswordEncoder encoder) { ... }
}
```

## 🔒 검증 패턴

### 검증 매트릭스

| 필드       | 필수 | 최소 | 최대  | 규칙                |
|----------|----|----|-----|-------------------|
| name     | Y  | -  | 30  | trim(), non-empty |
| email    | Y  | -  | 255 | 형식, 중복 검사         |
| password | Y  | 8  | 255 | 복잡도               |
| role     | Y  | -  | -   | 열거형               |

### 검증 구현

```java
private void validateName(String name) {
    if (isNullOrEmpty(name)) throw ex(INVALID_NAME);
    if (name.length() > 30) throw ex(NAME_TOO_LONG);
}
```

## ⚠️ 예외 처리

상세: @exception/CONVENTIONS.md

## 🎭 Role Enum

```java
public enum Role {
    STUDENT("학생"), LAB_MEMBER("랩실 멤버"), LAB_MANAGER("랩실 관리자"),
    LAB_LEADER("랩장"), PROFESSOR("교수"), ADMIN("관리자");
    
    private final String description;
    Role(String description) { this.description = description; }
    
    public boolean hasHigherOrEqualAuthorityThan(Role other) {
        return this.ordinal() >= other.ordinal();
    }
    
    public boolean isLabManager() {
        return this == LAB_MANAGER || this == LAB_LEADER;
    }
}
```

## 🔗 연관관계

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "lab_id")
private Lab lab;

public void assignLab(Lab lab) {
    if (this.lab != null) this.lab.removeMember(this);
    this.lab = lab;
    if (lab != null) lab.addMember(this);
}

public void leaveLab() {
    if (this.lab != null) { this.lab.removeMember(this); this.lab = null; }
}
```

## 📋 비즈니스 로직

### 권한 확인

```java
public boolean canManageLabApplications(Lab targetLab) {
    return this.lab != null && this.lab.equals(targetLab) &&
           (role == LAB_LEADER || role == LAB_MANAGER || role == PROFESSOR);
}
```

### 비밀번호 변경

```java
public void changePassword(String newRaw) {
    Password newPassword = Password.fromRaw(newRaw, encoder);
    if (this.password.matches(newRaw, encoder)) 
        throw ex(SAME_AS_CURRENT_PASSWORD);
    this.password = newPassword;
}
```

## 🧪 테스트 패턴

### User 테스트

```java
@Test
void 유효한_정보로_사용자_생성() {
    User user = User.create("홍길동", "hong@example.com", "password123!", STUDENT, encoder);
    assertThat(user.getName()).isEqualTo("홍길동");
}

@Test
void 잘못된_이름_예외_발생() {
    assertThatThrownBy(() -> User.create("", "test@example.com", "password123!", STUDENT, encoder))
        .isInstanceOf(UserValidationException.class);
}
```

### Password 테스트

```java
@Test
void 비밀번호_생성_및_검증() {
    Password password = Password.fromRaw("password123!", encoder);
    assertThat(password.matches("password123!", encoder)).isTrue();
}
```

## 🎯 핵심 규칙

1. **불변성**: Value Object immutable 설계
2. **검증 우선**: 생성시 모든 검증 완료
3. **명확한 예외**: 구체적 ErrorCode 사용
4. **캡슐화**: private 구현, public 인터페이스
5. **팩토리 패턴**: 정적 메서드 생성
6. **연관관계**: 편의 메서드로 일관성 보장