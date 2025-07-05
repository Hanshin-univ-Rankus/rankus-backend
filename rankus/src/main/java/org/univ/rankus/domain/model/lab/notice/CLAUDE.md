# Notice Domain 상세 가이드

> 📋 **기본 패턴**: @core/patterns.md#entity-템플릿

## 📊 엔티티 매트릭스

### 핵심 엔티티 구조

| 엔티티           | 테이블          | 주요 필드                                       | 비즈니스 메서드                          |
|---------------|--------------|---------------------------------------------|-----------------------------------|
| **LabNotice** | `lab_notice` | title, content, type, isPinned, author, lab | `pin()`, `unpin()`, `isOwnedBy()` |

### 검증 규칙 매트릭스

| 엔티티       | 필드       | 규칙             | 제약사항          |
|-----------|----------|----------------|---------------|
| LabNotice | title    | 필수, 최대 100자    | Not null      |
| LabNotice | content  | 필수, 최대 2000자   | Not null      |
| LabNotice | type     | 필수, 열거형        | NORMAL/URGENT |
| LabNotice | isPinned | 불린값, 기본값 false | Not null      |
| LabNotice | author   | 필수, 사용자 참조     | ManyToOne     |
| LabNotice | lab      | 필수, 랩실 참조      | ManyToOne     |

### 상태 전이 매트릭스

| 엔티티       | 상태       | 가능한 전이          | 비즈니스 규칙  |
|-----------|----------|-----------------|----------|
| LabNotice | isPinned | false ↔ true    | 관리 권한 필요 |
| LabNotice | type     | NORMAL ↔ URGENT | 수정 권한 필요 |

### 연관관계 매트릭스

| 관계               | 주인        | 대상   | 매핑              | 제약     |
|------------------|-----------|------|-----------------|--------|
| LabNotice ↔ User | LabNotice | User | @ManyToOne LAZY | 작성자 관계 |
| LabNotice ↔ Lab  | LabNotice | Lab  | @ManyToOne LAZY | 랩실 소속  |

## 💎 Entity 상세 분석

### LabNotice Entity

#### 구현 특징

```java

@Entity
@Table(name = "lab_notice")
public class LabNotice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "content", length = 2000, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NoticeType type = NoticeType.NORMAL;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lab_id")
    private Lab lab;

    // 비즈니스 메서드
    public void pin() {
        this.isPinned = true;
    }

    public void unpin() {
        this.isPinned = false;
    }

    public void togglePin() {
        this.isPinned = !this.isPinned;
    }

    public boolean isOwnedBy(User user) {
        return this.author.equals(user);
    }

    public void update(String title, String content, NoticeType type, boolean isPinned) {
        validateTitle(title);
        validateContent(content);
        this.title = title;
        this.content = content;
        this.type = type;
        this.isPinned = isPinned;
    }
}
```

#### 설계 원칙

- **불변성**: 생성 후 update 메서드로만 수정 가능
- **캡슐화**: 모든 필드 private, 비즈니스 메서드로 접근
- **검증**: 생성 및 수정 시점에 데이터 검증
- **연관관계**: 지연 로딩으로 성능 최적화
- **상태 관리**: 명시적 메서드로 고정 상태 제어

#### 검증 규칙

```java
private void validateTitle(String title) {
    if (title == null || title.trim().isEmpty()) {
        throw new NoticeValidationException(NoticeErrorCode.TITLE_REQUIRED);
    }
    if (title.length() > 100) {
        throw new NoticeValidationException(NoticeErrorCode.TITLE_TOO_LONG);
    }
}

private void validateContent(String content) {
    if (content == null || content.trim().isEmpty()) {
        throw new NoticeValidationException(NoticeErrorCode.CONTENT_REQUIRED);
    }
    if (content.length() > 2000) {
        throw new NoticeValidationException(NoticeErrorCode.CONTENT_TOO_LONG);
    }
}
```

## 🎭 Enum 상세 분석

### NoticeType Enum

```java
public enum NoticeType {
    NORMAL("일반 공지"),
    URGENT("긴급 공지");

    private final String description;

    NoticeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUrgent() {
        return this == URGENT;
    }
}
```

**타입별 특징**:

- **NORMAL**: 일반적인 공지사항, 기본값
- **URGENT**: 긴급 공지사항, 우선순위 높음

## 🔗 엔티티 관계 상세

### 연관관계 매핑

#### LabNotice ↔ User (ManyToOne)

- **페치 전략**: LAZY (N+1 문제 방지)
- **조인 컬럼**: author_id
- **Cascade**: NONE (수동 관리)
- **역할**: 공지사항 작성자

#### LabNotice ↔ Lab (ManyToOne)

- **페치 전략**: LAZY
- **조인 컬럼**: lab_id
- **Cascade**: NONE (수동 관리)
- **역할**: 공지사항이 속한 랩실

## 🛡️ 도메인 불변 조건 상세

### LabNotice Aggregate 불변 조건

1. **제목 필수성**: 제목은 반드시 존재해야 하며 비어있을 수 없음
2. **내용 필수성**: 내용은 반드시 존재해야 하며 비어있을 수 없음
3. **작성자 유효성**: 반드시 유효한 사용자가 작성자로 지정되어야 함
4. **랩실 연결성**: 반드시 유효한 랩실에 속해야 함
5. **타입 일관성**: NORMAL 또는 URGENT 중 하나의 값만 가져야 함
6. **고정 상태**: isPinned는 명시적 메서드로만 변경 가능

### 비즈니스 규칙

1. **권한 기반 수정**: 작성자, 랩실 관리자, 교수, 관리자만 수정 가능
2. **권한 기반 삭제**: 작성자, 랩실 관리자, 교수, 관리자만 삭제 가능
3. **권한 기반 조회**: 랩실 멤버, 교수, 관리자만 조회 가능
4. **고정 관리**: 관리 권한이 있는 사용자만 고정/해제 가능

## ⚠️ 예외 처리 상세

### Notice Exception 계층

```java
// 베이스 예외
public abstract class NoticeException extends BaseCustomException {
    protected NoticeException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected NoticeException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

// 구체적 예외들
public class NoticeNotFoundException extends NoticeException {
}

public class NoticeValidationException extends NoticeException {
}
```

### NoticeErrorCode

```java
public enum NoticeErrorCode implements ErrorCode {
    // 400 Bad Request
    TITLE_REQUIRED("NOTICE_001", HttpStatus.BAD_REQUEST, "제목은 필수입니다"),
    TITLE_TOO_LONG("NOTICE_002", HttpStatus.BAD_REQUEST, "제목은 100자를 초과할 수 없습니다"),
    CONTENT_REQUIRED("NOTICE_003", HttpStatus.BAD_REQUEST, "내용은 필수입니다"),
    CONTENT_TOO_LONG("NOTICE_004", HttpStatus.BAD_REQUEST, "내용은 2000자를 초과할 수 없습니다"),

    // 404 Not Found
    NOTICE_NOT_FOUND("NOTICE_404", HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다");
}
```

## 📋 도메인별 컨벤션 가이드

상세한 구현 컨벤션은 다음 파일을 참조하세요:

- **Notice 도메인**: `@notice/CONVENTIONS.md`

## 🧪 도메인 모델 테스트

### 테스트 전략

- **생성 테스트**: 유효한/무효한 데이터로 객체 생성
- **비즈니스 로직 테스트**: pin/unpin, update 메서드 동작
- **불변 조건 테스트**: 도메인 규칙 위반 시 예외 발생
- **권한 테스트**: isOwnedBy 메서드 정확성

### 테스트 팩토리 활용

공지사항 객체 생성을 위한 테스트 팩토리는 `testutil.factory.domain` 패키지에서 제공됩니다.

```java
// 기본 공지사항 생성
LabNotice notice = new LabNotice("제목", "내용", author, lab);

// 긴급 공지사항 생성  
LabNotice urgentNotice = new LabNotice("긴급 제목", "긴급 내용", NoticeType.URGENT, false, author, lab);

// 고정 공지사항 생성
LabNotice pinnedNotice = new LabNotice("고정 제목", "고정 내용", NoticeType.NORMAL, true, author, lab);
```

## 🎯 핵심 설계 원칙

1. **도메인 중심**: 비즈니스 로직을 엔티티 내부에 캡슐화
2. **불변성 보장**: 명시적 메서드로만 상태 변경 허용
3. **검증 우선**: 생성 및 수정 시 모든 검증 완료
4. **연관관계 최적화**: 지연 로딩으로 성능 최적화
5. **권한 분리**: 비즈니스 로직과 권한 검증 분리
6. **확장성**: 향후 이미지 첨부 등 기능 확장 고려