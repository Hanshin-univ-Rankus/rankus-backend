# Lab Domain 컨벤션

## 📛 네이밍
| 구분 | 패턴 | 예시 |
|------|------|------|
| Entity | Domain명 | `Lab`, `LabApplication`, `LabImage` |
| Enum | 기능명 | `LabCategory`, `ApplicationStatus`, `ImageType` |
| Method-상태 | 동사 | `approve()`, `reject()` |
| Method-검증 | `is{Condition}()`, `can{Action}()` | `isPending()`, `canBeApproved()` |
| Table | snake_case | `lab`, `lab_application`, `lab_image` |

## 🏗️ 주요 Entity

### Lab
```java
@Entity
@Table(name = "lab")
public class Lab extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(length = 10, nullable = false)
    private String name;
    
    protected Lab() {}
    private Lab(...) { validate(); }
    
    public static Lab create(String name, LabCategory category, String description) {
        return new Lab(name, category, description);
    }
    
    public void autoAssignProfessorIfMatches(User user) {
        if (user.getRole() == PROFESSOR) this.professorName = user.getName();
    }
}
```

### LabApplication
```java
@Entity
@Table(name = "lab_application",
       uniqueConstraints = @UniqueConstraint(columnNames = {"lab_id", "user_id"}))
public class LabApplication extends BaseTimeEntity {
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "lab_id")
    private Lab lab;
    
    @Column(nullable = false)
    private LocalDateTime interviewTime;
    
    @Enumerated(STRING)
    private ApplicationStatus status = PENDING;
    
    public void approve() { validateCanChangeStatus(); this.status = APPROVED; }
    public void reject() { validateCanChangeStatus(); this.status = REJECTED; }
    
    public boolean isPending() { return status == PENDING; }
    public boolean isOwnedBy(User user) { return this.user.equals(user); }
}
```

### LabImage
```java
@Entity
@Table(name = "lab_image")
public class LabImage {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "lab_id")
    private Lab lab;
    
    @Column(length = 255, nullable = false)
    private String imageUrl;
    
    @Enumerated(STRING)
    private ImageType type;
    
    public void setImageUrl(String url) { validate(url); this.imageUrl = url; }
}
```

## 🔒 검증 패턴

### 검증 매트릭스
| Entity | 필드 | 필수 | 최대 | 규칙 |
|--------|------|------|------|------|
| Lab | name | Y | 10 | trim(), non-empty |
| Lab | ranking | Y | - | >= 0 |
| LabApplication | interviewTime | Y | - | 미래시점 |
| LabApplication | status | Y | - | PENDING에서만 변경 |
| LabImage | imageUrl | Y | 255 | URL 형식 |

### 검증 구현
```java
private void validateName(String name) {
    if (isNullOrEmpty(name)) throw ex(LAB_NAME_REQUIRED);
    if (name.length() > 10) throw ex(LAB_NAME_TOO_LONG);
}
```

## ⚠️ 예외 처리
상세: @exception/CONVENTIONS.md

## 🎭 주요 Enum

### LabCategory
```java
public enum LabCategory {
    AI("인공지능"), CV("컴퓨터비전"), DB("데이터베이스"), WEB("웹개발"),
    NETWORK("네트워크"), SECURITY("보안"), IOT("사물인터넷"), MOBILE("모바일"),
    GAME("게임"), ROBOTICS("로보틱스"), COMPUTER_SCIENCE("컴퓨터과학"), ETC("기타");
}
```

### ApplicationStatus
```java
public enum ApplicationStatus {
    PENDING("심사대기"), APPROVED("승인"), REJECTED("거부");
    
    public boolean isFinal() { return this == APPROVED || this == REJECTED; }
    public boolean canTransitionTo(ApplicationStatus newStatus) {
        return this == PENDING && (newStatus == APPROVED || newStatus == REJECTED);
    }
}
```

### ImageType
```java
public enum ImageType {
    REPRESENTATIVE("대표이미지", 1), ADDITIONAL("추가이미지", 10);
    
    private final String description;
    private final int maxCount;
}
```

## 🔗 연관관계

### 제약조건
```java
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"lab_id", "user_id"}))
public class LabApplication { /* 중복지원 방지 */ }
```

### 지연로딩
```java
@OneToMany(mappedBy = "lab", fetch = LAZY, cascade = ALL)
private List<LabApplication> applications;

@OneToMany(mappedBy = "lab", fetch = LAZY, cascade = ALL)
private List<LabImage> images;
```

## 📋 비즈니스 로직

### 상태전이
```java
public void approve() {
    if (!status.canTransitionTo(APPROVED)) throw ex(CANNOT_CHANGE_STATUS);
    this.status = APPROVED;
    // DomainEvents.raise(new ApplicationApprovedEvent(this)); // 향후
}
```

### 권한확인
```java
public boolean canBeModifiedBy(User user) {
    return user.isLabLeaderOrLabManagerInLab(lab) || user.getRole() == ADMIN;
}

public boolean canBeViewedBy(User user) {
    return isOwnedBy(user) || user.isLabLeaderOrLabManagerInLab(lab) || user.getRole() == ADMIN;
}
```

## 🧪 테스트 패턴

### 상태전이
```java
@Test
void PENDING에서_승인시_APPROVED_상태변경() {
    LabApplication app = createPendingApplication();
    app.approve();
    assertThat(app.getStatus()).isEqualTo(APPROVED);
}

@Test
void 이미처리된_지원서_상태변경_예외() {
    LabApplication app = createApprovedApplication();
    assertThatThrownBy(() -> app.reject())
        .isInstanceOf(LabApplicationValidationException.class);
}
```

### 검증로직
```java
@Test
void 빈이름_랩실생성_예외() {
    assertThatThrownBy(() -> Lab.create("", AI, "설명"))
        .isInstanceOf(LabValidationException.class);
}
```

## 🎯 핵심 규칙
1. **상태불변성**: 명시적 메서드로만 전이
2. **철저한 검증**: 모든 입력값 검증
3. **명확한 예외**: 구체적 ErrorCode 사용
4. **연관관계**: FK 제약과 JPA 설정 일치
5. **도메인로직**: 엔티티 내부 구현
6. **팩토리패턴**: 정적 메서드 생성 활용