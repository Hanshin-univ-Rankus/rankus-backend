# Domain Model 상세 가이드

> 엔티티, Value Object, Enum의 구체적인 구현 방법과 비즈니스 로직

## 📊 엔티티 상세 분석

### User Entity 구현 분석

#### 핵심 필드
```java
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(length = 30, nullable = false)
    private String name;
    
    @Column(length = 100, nullable = false, unique = true)
    private String email;
    
    @Embedded
    private Password password;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    private Lab lab;
}
```

#### 핵심 비즈니스 메서드
- **`checkPassword(String rawPassword)`**: 로그인 시 비밀번호 검증
- **`changePassword(String newRawPassword)`**: 비밀번호 변경 및 검증
- **`assignLab(Lab lab)`**: 랩실 배정
- **`isLabLeaderOrLabManagerInLab(Lab targetLab)`**: 특정 랩실에서의 권한 확인

#### 검증 규칙
- 이름: 필수, 최대 30자
- 이메일: 필수, 유효한 형식, 고유값, 최대 100자
- 비밀번호: Password Value Object로 검증
- 역할: 기본값 STUDENT

### Lab Entity 구현 분석

#### 핵심 필드
```java
@Entity
@Table(name = "lab")
public class Lab extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    @Column(length = 10, nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabCategory category;
    
    @Column(length = 255)
    private String description;
    
    @Column(nullable = false)
    private Integer ranking = 0;
    
    @Column(length = 10)
    private String professorName;
}
```

#### 핵심 비즈니스 메서드
- **`autoAssignProfessorIfMatches(User user)`**: 사용자가 교수일 경우 자동 할당
- **`setProfessorName(String name)`**: 교수명 설정 및 검증
- **`updateRanking(Integer newRanking)`**: 랭킹 값 검증 후 업데이트
- **`increaseRanking(Integer points)`**: 랭킹 점수 증가
- **`decreaseRanking(Integer points)`**: 랭킹 점수 감소 (최소 0)

#### 검증 규칙
- 이름: 필수, 최대 10자, 공백 제거
- 카테고리: 필수 enum 값
- 설명: 선택, 최대 255자
- 랭킹: 필수, 0 이상의 정수
- 교수명: 선택, 최대 10자

### LabApplication Entity 구현 분석

#### 핵심 필드
```java
@Entity
@Table(name = "lab_application", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"lab_id", "user_id"}))
public class LabApplication extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
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
}
```

#### 핵심 비즈니스 메서드
- **`approve()`**: 지원 승인 (PENDING → APPROVED)
- **`reject()`**: 지원 거부 (PENDING → REJECTED)  
- **`isOwnedBy(User user)`**: 지원서 소유권 확인

#### 상태 전이 규칙
```
PENDING ──approve()──► APPROVED
   │
   └──reject()───► REJECTED
```

#### 검증 규칙
- 랩실: 필수 참조
- 사용자: 필수 참조
- 면접시간: 필수, 미래 시점
- 상태: 기본값 PENDING
- 중복 지원 방지: (lab_id, user_id) 유니크 제약

### LabImage Entity 구현 분석

#### 핵심 필드
```java
@Entity
@Table(name = "lab_image")
public class LabImage {
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
}
```

#### 핵심 비즈니스 메서드
- **`setImageUrl(String url)`**: URL 형식 검증 후 설정

#### 검증 규칙
- 랩실: 필수 참조
- 이미지 URL: 필수, 유효한 URL 형식, 최대 255자
- 타입: 필수 enum 값 (REPRESENTATIVE/ADDITIONAL)

## 💎 Value Object 상세 분석

### Password Value Object

#### 구현 특징
```java
@Embeddable
public class Password {
    @Column(name = "password", length = 255, nullable = false)
    private String value; // 암호화된 비밀번호만 저장
    
    // private 생성자로 직접 생성 방지
    private Password(String hashedPassword) {
        this.value = hashedPassword;
    }
    
    // 팩토리 메서드로 생성 (도메인 인터페이스 사용)
    public static Password fromRaw(String rawPassword, PasswordEncoder encoder) {
        validatePassword(rawPassword);
        return new Password(encoder.encode(rawPassword));
    }
    
    // 비밀번호 검증 (도메인 인터페이스 사용)
    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.value);
    }
}
```

#### 설계 원칙
- **불변성**: 한 번 생성되면 변경 불가
- **캡슐화**: 암호화 로직 외부 의존성으로 분리
- **검증**: 생성 시점에 비밀번호 규칙 검증
- **보안**: 평문 비밀번호 저장 금지
- **의존성 역전**: 도메인 인터페이스 활용으로 프레임워크 독립성 확보

#### 의존성 분리 구조
```
Password (Domain) → PasswordEncoder (Domain Interface)
                    ↑
         Spring Security BCryptPasswordEncoder (Infrastructure)
```

#### 검증 규칙
- 길이: 최소 8자, 최대 255자
- 구성: 영문, 숫자, 특수문자 조합 권장
- 암호화: 외부 PasswordEncoder 인터페이스 활용

## 🎭 Enum 상세 분석

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
}
```

**권한 계층**: STUDENT < LAB_MEMBER < LAB_MANAGER < LAB_LEADER < PROFESSOR < ADMIN

### LabCategory Enum
```java
public enum LabCategory {
    AI("인공지능"), CV("컴퓨터 비전"), DB("데이터베이스"),
    WEB("웹 개발"), NETWORK("네트워크"), SECURITY("보안"),
    IOT("사물인터넷"), MOBILE("모바일"), GAME("게임"),
    ROBOTICS("로보틱스"), COMPUTER_SCIENCE("컴퓨터과학"), ETC("기타");
}
```

### ApplicationStatus Enum
```java
public enum ApplicationStatus {
    PENDING("심사 대기"),
    APPROVED("승인됨"),
    REJECTED("거부됨");
}
```

### ImageType Enum
```java
public enum ImageType {
    REPRESENTATIVE("대표 이미지"),
    ADDITIONAL("추가 이미지");
}
```

## 🔗 엔티티 관계 상세

### 연관관계 매핑

#### User ↔ Lab (ManyToOne)
- **페치 전략**: LAZY (N+1 문제 방지)
- **조인 컬럼**: lab_id
- **Cascade**: NONE (수동 관리)

#### User ↔ LabApplication (OneToMany)
- **페치 전략**: LAZY
- **매핑**: user_id 외래키
- **삭제**: 사용자 삭제 시 지원서 함께 삭제

#### Lab ↔ LabApplication (OneToMany)
- **페치 전략**: LAZY  
- **매핑**: lab_id 외래키
- **삭제**: 랩실 삭제 시 지원서 함께 삭제

#### Lab ↔ LabImage (OneToMany)
- **페치 전략**: LAZY
- **매핑**: lab_id 외래키
- **Cascade**: ALL (랩실과 함께 관리)

## 🛡️ 도메인 불변 조건 상세

### User Aggregate 불변 조건
1. **이메일 고유성**: 시스템 내 유일한 이메일
2. **비밀번호 복잡성**: 최소 8자 이상의 안전한 비밀번호
3. **랩실 소속**: 사용자는 최대 하나의 랩실에만 소속 가능
4. **역할 일관성**: 교수는 반드시 랩실과 연결되어야 함

### Lab Aggregate 불변 조건
1. **랩실명 유일성**: 동일한 이름의 랩실 존재 불가
2. **랭킹 유효성**: 0 이상의 정수값
3. **교수 연결**: 교수가 배정된 랩실은 해당 교수만 관리 가능
4. **지원 중복 방지**: 동일 사용자의 동일 랩실 중복 지원 불가

### LabApplication 불변 조건
1. **면접 시간 유효성**: 반드시 미래 시점
2. **상태 전이 제한**: PENDING 상태에서만 승인/거부 가능
3. **소유권 확인**: 지원자 본인만 지원서 수정/삭제 가능
4. **일회성 전이**: 승인/거부 후 상태 변경 불가

## 📋 도메인별 컨벤션 가이드

각 도메인의 구체적인 코딩 컨벤션과 구현 패턴은 다음 파일을 참조하세요:

- **User 도메인**: `@user/CONVENTIONS.md`
- **Lab 도메인**: `@lab/CONVENTIONS.md`

## 🧪 도메인 모델 테스트

### 테스트 전략
- **생성 테스트**: 유효한/무효한 데이터로 객체 생성
- **비즈니스 로직 테스트**: 도메인 메서드의 정확한 동작
- **불변 조건 테스트**: 도메인 규칙 위반 시 예외 발생
- **상태 전이 테스트**: 엔티티 상태 변경의 정확성

### 테스트 팩토리 활용
도메인 객체 생성을 위한 테스트 팩토리는 `testutil.factory.domain` 패키지에서 제공됩니다.