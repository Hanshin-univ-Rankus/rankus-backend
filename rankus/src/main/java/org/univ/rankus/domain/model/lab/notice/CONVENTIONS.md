# Notice Domain 컨벤션

## 📛 네이밍

| 구분        | 패턴                                           | 예시                           |
|-----------|----------------------------------------------|------------------------------|
| Entity    | Domain명                                      | `LabNotice`                  |
| Enum      | 기능명                                          | `NoticeType`                 |
| Exception | `Notice{Type}Exception`                      | `NoticeValidationException`  |
| Method-조회 | `get{Property}()`, `is{Condition}()`         | `getTitle()`, `isPinned()`   |
| Method-검증 | `check{Condition}()`, `validate{Property}()` | `checkOwnership()`           |
| Method-변경 | `change{Property}()`, `update{Property}()`   | `updateContent()`, `pin()`   |
| Field     | camelCase / UPPER_SNAKE_CASE                 | `title` / `MAX_TITLE_LENGTH` |

## 🏗️ 구조 패턴

### LabNotice Entity

```java
@Entity
@Table(name = "lab_notice")
public class LabNotice extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String content;

    @Enumerated(STRING)
    @Column(nullable = false)
    private NoticeType type = NORMAL;

    @Column(nullable = false)
    private boolean isPinned = false;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "lab_id")
    private Lab lab;

    protected LabNotice() {} // JPA

    private LabNotice(...) {
        validate();
    }


    public void update(String title, String content, NoticeType type) { ... }
    public void pin() { this.isPinned = true; }
    public void unpin() { this.isPinned = false; }
    public void togglePin() { this.isPinned = !this.isPinned; }
    public boolean isOwnedBy(User user) { return this.author.equals(user); }
}
```

### NoticeType Enum

```java
public enum NoticeType {
    NORMAL("일반 공지"), URGENT("긴급 공지");

    private final String description;

    NoticeType(String description) {
        this.description = description;
    }

    public boolean isUrgent() {
        return this == URGENT;
    }

    public String getDescription() {
        return description;
    }
}
```

## 🔒 검증 패턴

### 검증 매트릭스

| 필드      | 필수 | 최소 | 최대   | 규칙                |
|---------|----|----|------|-------------------|
| title   | Y  | -  | 100  | trim(), non-empty |
| content | Y  | -  | 2000 | trim(), non-empty |
| type    | Y  | -  | -    | 열거형               |
| author  | Y  | -  | -    | 유효한 User 객체       |
| lab     | Y  | -  | -    | 유효한 Lab 객체        |

### 검증 구현

```java
private void validateTitle(String title) {
    if (isNullOrEmpty(title)) throw ex(NOTICE_TITLE_REQUIRED);
    if (title.trim().length() > 100) throw ex(NOTICE_TITLE_TOO_LONG);
}

private void validateContent(String content) {
    if (isNullOrEmpty(content)) throw ex(NOTICE_CONTENT_REQUIRED);
    if (content.trim().length() > 2000) throw ex(NOTICE_CONTENT_TOO_LONG);
}

private void validateAuthor(User author) {
    if (author == null) throw ex(NOTICE_AUTHOR_REQUIRED);
}

private void validateLab(Lab lab) {
    if (lab == null) throw ex(NOTICE_LAB_REQUIRED);
}
```

## ⚠️ 예외 처리

상세: @exception/CONVENTIONS.md

## 🔗 연관관계

### LabNotice ↔ User (작성자)

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "author_id")
private User author;

public boolean isOwnedBy(User user) {
    return this.author.equals(user);
}
```

### LabNotice ↔ Lab (랩실)

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "lab_id")
private Lab lab;

public boolean belongsTo(Lab targetLab) {
    return this.lab.equals(targetLab);
}
```

## 📋 비즈니스 로직

### 고정 상태 관리

```java
public void pin() {
    this.isPinned = true;
}

public void unpin() {
    this.isPinned = false;
}

public void togglePin() {
    this.isPinned = !this.isPinned;
}

public boolean isPinned() {
    return isPinned;
}
```

### 공지사항 수정

```java
public void update(String title, String content, NoticeType type) {
    validateTitle(title);
    validateContent(content);
    validateType(type);
    
    this.title = title.trim();
    this.content = content.trim();
    this.type = type;
}
```

### 권한 확인

```java
public boolean isOwnedBy(User user) {
    return this.author.equals(user);
}

public boolean canBeViewedBy(User user) {
    // 교수, 관리자는 모든 랩실 공지 조회 가능
    if (user.getRole() == PROFESSOR || user.getRole() == ADMIN) {
        return true;
    }
    // 랩실 멤버는 자신의 랩실 공지만 조회 가능
    return user.getLab() != null && user.getLab().equals(this.lab);
}

public boolean canBeModifiedBy(User user) {
    // 작성자는 항상 수정 가능
    if (isOwnedBy(user)) {
        return true;
    }
    // 교수, 관리자는 모든 랩실 공지 수정 가능
    if (user.getRole() == PROFESSOR || user.getRole() == ADMIN) {
        return true;
    }
    // 랩실 관리자는 자신의 랩실 공지만 수정 가능
    return user.getLab() != null && user.getLab().equals(this.lab) &&
            (user.getRole() == LAB_LEADER || user.getRole() == LAB_MANAGER);
}
```

## 🧪 테스트 패턴

### LabNotice 테스트

```java
@Test
void 유효한_정보로_공지사항_생성() {
    LabNotice notice = new LabNotice(
        "공지 제목", "공지 내용", NORMAL, author, lab
    );
    assertThat(notice.getTitle()).isEqualTo("공지 제목");
    assertThat(notice.getType()).isEqualTo(NORMAL);
    assertThat(notice.isPinned()).isFalse();
}

@Test
void 빈_제목_공지사항_생성_예외() {
    assertThatThrownBy(() -> new LabNotice(
        "", "공지 내용", NORMAL, author, lab
    )).isInstanceOf(NoticeValidationException.class);
}

@Test
void 공지사항_고정_토글() {
    LabNotice notice = createNotice();
    
    notice.pin();
    assertThat(notice.isPinned()).isTrue();
    
    notice.unpin();
    assertThat(notice.isPinned()).isFalse();
    
    notice.togglePin();
    assertThat(notice.isPinned()).isTrue();
}
```

### NoticeType 테스트

```java
@Test
void URGENT_타입_확인() {
    assertThat(URGENT.isUrgent()).isTrue();
    assertThat(NORMAL.isUrgent()).isFalse();
}

@Test
void 타입_설명_확인() {
    assertThat(NORMAL.getDescription()).isEqualTo("일반 공지");
    assertThat(URGENT.getDescription()).isEqualTo("긴급 공지");
}
```

### 권한 테스트

```java
@Test
void 작성자_수정_권한() {
    LabNotice notice = createNotice(author);
    assertThat(notice.canBeModifiedBy(author)).isTrue();
}

@Test
void 교수_모든_공지_수정_권한() {
    User professor = createProfessor();
    LabNotice notice = createNotice(author);
    assertThat(notice.canBeModifiedBy(professor)).isTrue();
}

@Test
void 랩실_멤버_타랩실_공지_수정_권한_없음() {
    User otherLabMember = createLabMember(otherLab);
    LabNotice notice = createNotice(author, lab);
    assertThat(notice.canBeModifiedBy(otherLabMember)).isFalse();
}
```

## 🎯 핵심 규칙

1. **불변성**: 생성 시 모든 필수 검증 완료
2. **검증 우선**: 모든 입력값 검증 후 상태 변경
3. **명확한 예외**: 구체적 ErrorCode 사용
4. **캡슐화**: private 구현, public 인터페이스
5. **생성자 패턴**: 직접 생성자 사용
6. **권한 기반**: 메서드 레벨 권한 확인
7. **연관관계**: 지연 로딩으로 성능 최적화
8. **비즈니스 로직**: 엔티티 내부 구현으로 응집성 확보