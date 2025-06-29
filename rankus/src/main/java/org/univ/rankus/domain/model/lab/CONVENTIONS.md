# Lab Domain 코딩 컨벤션

> Lab 관련 도메인 객체의 네이밍, 구조, 구현 패턴

## 📛 네이밍 컨벤션

### 클래스 네이밍
- **Entity**: `Lab`, `LabApplication`, `LabImage` (도메인 개념 그대로)
- **Enum**: `LabCategory`, `ApplicationStatus`, `ImageType`
- **Exception**: `Lab{Specific}Exception` (@exception/CONVENTIONS.md 참조)
- **ErrorCode**: `LabErrorCode`, `LabApplicationErrorCode`, `LabImageErrorCode` (@exception/CONVENTIONS.md 참조)

### 메서드 네이밍
- **상태 전이**: `approve()`, `reject()` (간결한 동사)
- **검증**: `isOwnedBy()`, `isPending()`, `canBeApproved()`
- **할당**: `autoAssignProfessorIfMatches()`
- **설정**: `setProfessorName()` (검증 포함)

### 테이블 네이밍
- **Entity → Table**: snake_case 변환
  - `Lab` → `lab`
  - `LabApplication` → `lab_application`
  - `LabImage` → `lab_image`

## 🏗️ 클래스 구조 패턴

### Lab Entity 구조
```java
@Entity
@Table(name = "lab")
public class Lab extends BaseTimeEntity {
    
    // 1. 필드 (private)
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(length = 10, nullable = false)
    private String name;
    
    // 2. 생성자 (protected + private)
    protected Lab() {} // JPA 전용
    
    private Lab(String name, LabCategory category, String description) {
        validateName(name);
        this.name = name.trim();
        this.category = category;
        this.description = description;
        this.ranking = 0; // 기본값
    }
    
    // 3. 팩토리 메서드
    public static Lab create(String name, LabCategory category, String description) {
        return new Lab(name, category, description);
    }
    
    // 4. 비즈니스 메서드
    public void autoAssignProfessorIfMatches(User user) {
        if (user.getRole() == Role.PROFESSOR) {
            this.professorName = user.getName();
        }
    }
    
    // 5. 검증 메서드 (private)
    private void validateName(String name) { /* 구현 */ }
}
```

### LabApplication Entity 구조
```java
@Entity
@Table(name = "lab_application",
       uniqueConstraints = @UniqueConstraint(columnNames = {"lab_id", "user_id"}))
public class LabApplication extends BaseTimeEntity {
    
    // 1. 필드
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lab_id")
    private Lab lab;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private LocalDateTime interviewTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;
    
    // 2. 생성자
    protected LabApplication() {}
    
    private LabApplication(Lab lab, User user, LocalDateTime interviewTime) {
        validateInterviewTime(interviewTime);
        this.lab = lab;
        this.user = user;
        this.interviewTime = interviewTime;
        this.status = ApplicationStatus.PENDING;
    }
    
    // 3. 팩토리 메서드
    public static LabApplication create(Lab lab, User user, LocalDateTime interviewTime) {
        return new LabApplication(lab, user, interviewTime);
    }
    
    // 4. 상태 전이 메서드
    public void approve() {
        validateCanChangeStatus();
        this.status = ApplicationStatus.APPROVED;
    }
    
    public void reject() {
        validateCanChangeStatus();
        this.status = ApplicationStatus.REJECTED;
    }
    
    // 5. 조회 메서드
    public boolean isPending() {
        return this.status == ApplicationStatus.PENDING;
    }
    
    public boolean isOwnedBy(User user) {
        return this.user.equals(user);
    }
    
    // 6. 검증 메서드 (private)
    private void validateCanChangeStatus() {
        if (!isPending()) {
            throw new LabApplicationValidationException(
                LabApplicationErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION
            );
        }
    }
    
    private void validateInterviewTime(LocalDateTime interviewTime) {
        if (interviewTime == null) {
            throw new LabApplicationValidationException(
                LabApplicationErrorCode.INTERVIEW_TIME_REQUIRED
            );
        }
        if (interviewTime.isBefore(LocalDateTime.now())) {
            throw new LabApplicationValidationException(
                LabApplicationErrorCode.INTERVIEW_TIME_MUST_BE_FUTURE
            );
        }
    }
}
```

### LabImage Entity 구조
```java
@Entity
@Table(name = "lab_image")
public class LabImage {
    
    // 1. 필드
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lab_id")
    private Lab lab;
    
    @Column(length = 255, nullable = false)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType type;
    
    // 2. 생성자
    protected LabImage() {}
    
    private LabImage(Lab lab, String imageUrl, ImageType type) {
        validateImageUrl(imageUrl);
        this.lab = lab;
        this.imageUrl = imageUrl;
        this.type = type;
    }
    
    // 3. 팩토리 메서드
    public static LabImage create(Lab lab, String imageUrl, ImageType type) {
        return new LabImage(lab, imageUrl, type);
    }
    
    // 4. 비즈니스 메서드
    public void setImageUrl(String imageUrl) {
        validateImageUrl(imageUrl);
        this.imageUrl = imageUrl;
    }
    
    // 5. 검증 메서드 (private)
    private void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new LabImageValidationException(
                LabImageErrorCode.IMAGE_URL_REQUIRED
            );
        }
        
        // URL 형식 검증
        if (!imageUrl.matches("^(https?|ftp)://.*$")) {
            throw new LabImageValidationException(
                LabImageErrorCode.INVALID_IMAGE_URL_FORMAT
            );
        }
    }
}
```

## 🔒 검증 규칙 패턴

### Lab 엔티티 검증
```java
private void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
        throw new LabValidationException(LabErrorCode.LAB_NAME_REQUIRED);
    }
    if (name.length() > 10) {
        throw new LabValidationException(LabErrorCode.LAB_NAME_TOO_LONG);
    }
}

private void validateRanking(Integer ranking) {
    if (ranking == null) {
        throw new LabValidationException(LabErrorCode.RANKING_REQUIRED);
    }
    if (ranking < 0) {
        throw new LabValidationException(LabErrorCode.RANKING_MUST_BE_NON_NEGATIVE);
    }
}
```

### LabApplication 상태 전이 검증
```java
private void validateCanChangeStatus() {
    if (this.status != ApplicationStatus.PENDING) {
        throw new LabApplicationValidationException(
            LabApplicationErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION,
            String.format("현재 상태: %s", this.status)
        );
    }
}
```

## ⚠️ 예외 처리 패턴

Lab 도메인 예외 처리에 대한 상세 내용은 @exception/CONVENTIONS.md를 참조하세요.

## 🎭 Enum 구현 패턴

### LabCategory Enum
```java
public enum LabCategory {
    AI("인공지능", "AI"),
    CV("컴퓨터 비전", "Computer Vision"),
    DB("데이터베이스", "Database"),
    WEB("웹 개발", "Web Development"),
    NETWORK("네트워크", "Network"),
    SECURITY("보안", "Security"),
    IOT("사물인터넷", "Internet of Things"),
    MOBILE("모바일", "Mobile"),
    GAME("게임", "Game"),
    ROBOTICS("로보틱스", "Robotics"),
    COMPUTER_SCIENCE("컴퓨터과학", "Computer Science"),
    ETC("기타", "Others");
    
    private final String koreanName;
    private final String englishName;
    
    LabCategory(String koreanName, String englishName) {
        this.koreanName = koreanName;
        this.englishName = englishName;
    }
    
    public String getKoreanName() { return koreanName; }
    public String getEnglishName() { return englishName; }
}
```

### ApplicationStatus Enum
```java
public enum ApplicationStatus {
    PENDING("심사 대기", "pending"),
    APPROVED("승인됨", "approved"),
    REJECTED("거부됨", "rejected");
    
    private final String description;
    private final String code;
    
    ApplicationStatus(String description, String code) {
        this.description = description;
        this.code = code;
    }
    
    public String getDescription() { return description; }
    public String getCode() { return code; }
    
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
    
    public boolean canTransitionTo(ApplicationStatus newStatus) {
        return this == PENDING && (newStatus == APPROVED || newStatus == REJECTED);
    }
}
```

### ImageType Enum
```java
public enum ImageType {
    REPRESENTATIVE("대표 이미지", 1),  // 랩실당 1개만 허용
    ADDITIONAL("추가 이미지", 10);    // 랩실당 최대 10개
    
    private final String description;
    private final int maxCount;
    
    ImageType(String description, int maxCount) {
        this.description = description;
        this.maxCount = maxCount;
    }
    
    public String getDescription() { return description; }
    public int getMaxCount() { return maxCount; }
}
```

## 🔗 연관관계 처리 패턴

### 유니크 제약 조건 (LabApplication)
```java
@Table(name = "lab_application",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_lab_application_lab_user", 
           columnNames = {"lab_id", "user_id"}
       ))
public class LabApplication extends BaseTimeEntity {
    // 동일한 사용자가 동일한 랩실에 중복 지원하는 것을 방지
}
```

### 지연 로딩 설정
```java
// Lab → LabApplication (OneToMany)
@OneToMany(mappedBy = "lab", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
private List<LabApplication> applications = new ArrayList<>();

// Lab → LabImage (OneToMany)
@OneToMany(mappedBy = "lab", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
private List<LabImage> images = new ArrayList<>();
```

## 📋 비즈니스 로직 패턴

### 상태 전이 로직 (LabApplication)
```java
public void approve() {
    // 상태 전이 가능 여부 확인
    if (!this.status.canTransitionTo(ApplicationStatus.APPROVED)) {
        throw new LabApplicationValidationException(
            LabApplicationErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION
        );
    }
    
    // 상태 변경
    this.status = ApplicationStatus.APPROVED;
    
    // 도메인 이벤트 발생 (향후 구현)
    // DomainEvents.raise(new ApplicationApprovedEvent(this));
}
```

### 권한 확인 로직
```java
public boolean canBeModifiedBy(User user) {
    return user.isLabLeaderOrLabManagerInLab(this.lab) || 
           user.getRole() == Role.ADMIN;
}

public boolean canBeViewedBy(User user) {
    // 본인 지원서는 항상 조회 가능
    if (isOwnedBy(user)) {
        return true;
    }
    
    // 해당 랩실의 관리자는 조회 가능
    return user.isLabLeaderOrLabManagerInLab(this.lab) || 
           user.getRole() == Role.ADMIN;
}
```

## 🧪 테스트 작성 패턴

### 상태 전이 테스트
```java
class LabApplicationTest {
    
    @Test
    void PENDING_상태에서_승인하면_APPROVED_상태가_된다() {
        // given
        LabApplication application = createPendingApplication();
        
        // when
        application.approve();
        
        // then
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
    }
    
    @Test
    void 이미_처리된_지원서는_상태_변경시_예외가_발생한다() {
        // given
        LabApplication application = createApprovedApplication();
        
        // when & then
        assertThatThrownBy(() -> application.reject())
            .isInstanceOf(LabApplicationValidationException.class)
            .hasMessage("이미 처리된 지원서는 상태를 변경할 수 없습니다");
    }
}
```

### 검증 로직 테스트
```java
class LabTest {
    
    @Test
    void 빈_이름으로_랩실_생성시_예외가_발생한다() {
        // given
        String emptyName = "";
        
        // when & then
        assertThatThrownBy(() -> 
            Lab.create(emptyName, LabCategory.AI, "설명")
        ).isInstanceOf(LabValidationException.class);
    }
    
    @Test
    void 유효한_정보로_랩실을_생성할_수_있다() {
        // given
        String name = "AI랩실";
        LabCategory category = LabCategory.AI;
        String description = "인공지능 연구실";
        
        // when
        Lab lab = Lab.create(name, category, description);
        
        // then
        assertThat(lab.getName()).isEqualTo(name);
        assertThat(lab.getCategory()).isEqualTo(category);
        assertThat(lab.getDescription()).isEqualTo(description);
        assertThat(lab.getRanking()).isEqualTo(0);
    }
}
```

## 🎯 주요 규칙 요약

1. **상태 불변성**: 상태 전이는 명시적 메서드로만 가능
2. **검증 철저**: 모든 입력값에 대한 검증 로직 포함
3. **예외 명확성**: 구체적인 ErrorCode와 메시지 제공
4. **연관관계 관리**: 외래키 제약과 JPA 설정 일치
5. **비즈니스 규칙**: 도메인 로직은 엔티티 내부에 구현
6. **팩토리 패턴**: 복잡한 생성 로직은 정적 팩토리 메서드 활용