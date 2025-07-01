# Domain Layer 테스트 가이드

> 순수한 비즈니스 로직과 도메인 모델을 검증하는 Domain Layer 테스트 전략

## 🏛️ Domain Layer 테스트 개요

### 테스트 목표

- **비즈니스 로직 검증**: 엔티티 내부의 핵심 비즈니스 규칙 검증
- **도메인 불변 조건**: 도메인 규칙과 제약조건 위반 시 적절한 예외 발생
- **상태 전이 검증**: 엔티티 상태 변경의 정확성과 일관성
- **Value Object 동작**: 불변성, 동등성, 유효성 검증

### 테스트 범위

- 엔티티 생성 및 수정 로직
- 도메인 메서드의 비즈니스 규칙
- Value Object의 불변성과 검증
- 도메인 예외 발생 조건
- 연관관계 관리 로직

## 📁 Domain 테스트 구조

### 현재 구현된 테스트 파일

```
src/test/java/org/univ/rankus/domain/
├── CLAUDE.md                     # 이 파일
└── model/
    ├── lab/                      # Lab 도메인 테스트
    │   ├── LabApplicationTest.java
    │   ├── LabImageTest.java
    │   └── LabTest.java
    └── user/                     # User 도메인 테스트
        ├── PasswordTest.java     # Password Value Object 테스트
        └── UserTest.java
```

## 🧪 엔티티 테스트 전략

### 1. 순수 단위 테스트 패턴

**목표**: 외부 의존성 없는 순수한 도메인 로직 검증

```java
class UserTest {
    
    @Test
    void 유효한_정보로_사용자_생성_성공() {
        // given
        String name = "홍길동";
        String email = "hong@test.com";
        String password = "password123!";
        Role role = Role.STUDENT;
        
        // when
        User user = User.create(name, email, password, role);
        
        // then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getPassword().matches(password)).isTrue();
    }
    
    @Test
    void 빈_이름으로_사용자_생성시_예외_발생() {
        // given
        String emptyName = "";
        
        // when & then
        assertThatThrownBy(() -> 
            User.create(emptyName, "test@example.com", "password123!", Role.STUDENT)
        ).isInstanceOf(UserValidationException.class)
         .hasMessage("이름은 필수입니다");
    }
    
    @Test
    void 비밀번호_변경_성공() {
        // given
        User user = User.create("홍길동", "hong@test.com", "oldPassword123!", Role.STUDENT);
        String newPassword = "newPassword123!";
        
        // when
        user.changePassword(newPassword);
        
        // then
        assertThat(user.getPassword().matches(newPassword)).isTrue();
        assertThat(user.getPassword().matches("oldPassword123!")).isFalse();
    }
}
```

**특징**:

- 외부 프레임워크 의존성 없음
- 빠른 실행 속도 (평균 1-5ms)
- 순수 Java 객체 테스트
- Given-When-Then 패턴 적용

### 2. 비즈니스 로직 테스트

```java
class LabApplicationTest {
    
    @Test
    void PENDING_상태에서_승인하면_APPROVED_상태로_변경() {
        // given
        User user = DomainUserFactory.createStudent();
        Lab lab = DomainLabFactory.createAiLab();
        LabApplication application = LabApplication.create(
            lab, user, LocalDateTime.now().plusDays(1)
        );
        
        // when
        application.approve();
        
        // then
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(application.isPending()).isFalse();
    }
    
    @Test
    void 이미_처리된_지원서_승인시_예외_발생() {
        // given
        LabApplication application = DomainLabApplicationFactory.createApproved();
        
        // when & then
        assertThatThrownBy(() -> application.approve())
            .isInstanceOf(LabApplicationValidationException.class)
            .hasMessage("이미 처리된 지원서는 상태를 변경할 수 없습니다");
    }
    
    @Test
    void 지원서_소유권_확인() {
        // given
        User owner = DomainUserFactory.createStudent();
        User other = DomainUserFactory.createStudent();
        LabApplication application = DomainLabApplicationFactory.createPending(null, owner);
        
        // when & then
        assertThat(application.isOwnedBy(owner)).isTrue();
        assertThat(application.isOwnedBy(other)).isFalse();
    }
}
```

### 3. 상태 전이 테스트

```java
class LabApplicationStatusTransitionTest {
    
    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"APPROVED", "REJECTED"})
    void 최종_상태에서는_더_이상_상태_변경_불가(ApplicationStatus finalStatus) {
        // given
        LabApplication application = DomainLabApplicationFactory.createPending();
        
        // 최종 상태로 변경
        if (finalStatus == ApplicationStatus.APPROVED) {
            application.approve();
        } else {
            application.reject();
        }
        
        // when & then
        assertThatThrownBy(() -> application.approve())
            .isInstanceOf(LabApplicationValidationException.class);
        assertThatThrownBy(() -> application.reject())
            .isInstanceOf(LabApplicationValidationException.class);
    }
    
    @Test
    void 상태_전이_이력_추적() {
        // given
        LabApplication application = DomainLabApplicationFactory.createPending();
        LocalDateTime beforeApproval = LocalDateTime.now();
        
        // when
        application.approve();
        
        // then
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(application.getUpdatedAt()).isAfter(beforeApproval);
    }
}
```

## 💎 Value Object 테스트 전략

### Password Value Object 테스트

```java
class PasswordTest {
    
    @Test
    void 유효한_비밀번호로_Password_객체_생성_성공() {
        // given
        String rawPassword = "password123!";
        
        // when
        Password password = Password.fromRaw(rawPassword);
        
        // then
        assertThat(password).isNotNull();
        assertThat(password.matches(rawPassword)).isTrue();
        assertThat(password.matches("wrongPassword")).isFalse();
    }
    
    @Test
    void 짧은_비밀번호_생성시_예외_발생() {
        // given
        String shortPassword = "123";
        
        // when & then
        assertThatThrownBy(() -> Password.fromRaw(shortPassword))
            .isInstanceOf(PasswordValidationException.class)
            .hasMessage("비밀번호는 최소 8자 이상이어야 합니다");
    }
    
    @Test
    void Password_객체_불변성_검증() {
        // given
        String rawPassword = "password123!";
        Password password1 = Password.fromRaw(rawPassword);
        Password password2 = Password.fromRaw(rawPassword);
        
        // when & then
        assertThat(password1).isEqualTo(password2);
        assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
        
        // Value Object는 불변이므로 수정 메서드가 없어야 함
        // password.setValue() 같은 메서드는 존재하지 않음
    }
    
    @Test
    void 비밀번호_암호화_검증() {
        // given
        String rawPassword = "password123!";
        
        // when
        Password password = Password.fromRaw(rawPassword);
        
        // then
        // 암호화된 값이 원본과 다른지 확인
        assertThat(password.toString()).isNotEqualTo(rawPassword);
        
        // 하지만 matches로는 일치 확인 가능
        assertThat(password.matches(rawPassword)).isTrue();
    }
}
```

## 🎭 Enum 테스트 전략

### Role Enum 테스트

```java
class RoleTest {
    
    @Test
    void 모든_역할_값이_정의되어_있음() {
        // given & when
        Role[] roles = Role.values();
        
        // then
        assertThat(roles).hasSize(6);
        assertThat(roles).contains(
            Role.STUDENT, Role.LAB_MEMBER, Role.LAB_MANAGER,
            Role.LAB_LEADER, Role.PROFESSOR, Role.ADMIN
        );
    }
    
    @Test
    void 권한_계층_구조_검증() {
        // given & when & then
        assertThat(Role.ADMIN.hasHigherOrEqualAuthorityThan(Role.PROFESSOR)).isTrue();
        assertThat(Role.PROFESSOR.hasHigherOrEqualAuthorityThan(Role.LAB_LEADER)).isTrue();
        assertThat(Role.LAB_LEADER.hasHigherOrEqualAuthorityThan(Role.LAB_MANAGER)).isTrue();
        assertThat(Role.LAB_MANAGER.hasHigherOrEqualAuthorityThan(Role.LAB_MEMBER)).isTrue();
        assertThat(Role.LAB_MEMBER.hasHigherOrEqualAuthorityThan(Role.STUDENT)).isTrue();
        
        // 역방향은 false
        assertThat(Role.STUDENT.hasHigherOrEqualAuthorityThan(Role.LAB_MEMBER)).isFalse();
    }
    
    @Test
    void 랩실_관리자_권한_확인() {
        // given & when & then
        assertThat(Role.LAB_LEADER.isLabManager()).isTrue();
        assertThat(Role.LAB_MANAGER.isLabManager()).isTrue();
        assertThat(Role.PROFESSOR.isLabManager()).isFalse();
        assertThat(Role.STUDENT.isLabManager()).isFalse();
    }
}
```

## 🔗 연관관계 테스트

### 양방향 연관관계 테스트

```java
class UserLabRelationshipTest {
    
    @Test
    void 사용자_랩실_할당_양방향_연관관계_설정() {
        // given
        User user = DomainUserFactory.createStudent();
        Lab lab = DomainLabFactory.createAiLab();
        
        // when
        user.assignLab(lab);
        
        // then
        assertThat(user.getLab()).isEqualTo(lab);
        assertThat(lab.getMembers()).contains(user);
    }
    
    @Test
    void 사용자_랩실_탈퇴_연관관계_해제() {
        // given
        User user = DomainUserFactory.createStudent();
        Lab lab = DomainLabFactory.createAiLab();
        user.assignLab(lab);
        
        // when
        user.leaveLab();
        
        // then
        assertThat(user.getLab()).isNull();
        assertThat(lab.getMembers()).doesNotContain(user);
    }
    
    @Test
    void 사용자는_하나의_랩실에만_소속_가능() {
        // given
        User user = DomainUserFactory.createStudent();
        Lab lab1 = DomainLabFactory.createAiLab();
        Lab lab2 = DomainLabFactory.createDbLab();
        
        user.assignLab(lab1);
        
        // when
        user.assignLab(lab2);
        
        // then
        assertThat(user.getLab()).isEqualTo(lab2);
        assertThat(lab1.getMembers()).doesNotContain(user);
        assertThat(lab2.getMembers()).contains(user);
    }
}
```

## ⚠️ 도메인 예외 테스트

### 예외 발생 조건 테스트

```java
class DomainExceptionTest {
    
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void 유효하지_않은_이름으로_사용자_생성시_예외_발생(String invalidName) {
        // when & then
        assertThatThrownBy(() -> 
            User.create(invalidName, "test@example.com", "password123!", Role.STUDENT)
        ).isInstanceOf(UserValidationException.class)
         .satisfies(exception -> {
             UserValidationException userException = (UserValidationException) exception;
             assertThat(userException.getErrorCode()).isEqualTo(UserErrorCode.INVALID_NAME);
         });
    }
    
    @Test
    void 중복_지원_시도시_예외_발생() {
        // given
        User user = DomainUserFactory.createStudent();
        Lab lab = DomainLabFactory.createAiLab();
        
        // 첫 번째 지원은 성공
        LabApplication firstApplication = LabApplication.create(
            lab, user, LocalDateTime.now().plusDays(1)
        );
        
        // when & then - 동일 사용자의 동일 랩실 중복 지원
        assertThatThrownBy(() -> LabApplication.create(lab, user, LocalDateTime.now().plusDays(2)))
            .isInstanceOf(LabApplicationValidationException.class)
            .hasMessage("이미 해당 랩실에 지원한 이력이 있습니다");
    }
    
    @Test
    void 과거_시점_면접시간_설정시_예외_발생() {
        // given
        User user = DomainUserFactory.createStudent();
        Lab lab = DomainLabFactory.createAiLab();
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        
        // when & then
        assertThatThrownBy(() -> LabApplication.create(lab, user, pastTime))
            .isInstanceOf(LabApplicationValidationException.class)
            .hasMessage("면접 시간은 미래 시점이어야 합니다");
    }
}
```

## 🧩 도메인 팩토리 메서드 테스트

### 팩토리 메서드 검증

```java
class DomainFactoryMethodTest {
    
    @Test
    void User_create_팩토리_메서드_기본_값_설정() {
        // given
        String name = "홍길동";
        String email = "hong@test.com";
        String password = "password123!";
        Role role = Role.STUDENT;
        
        // when
        User user = User.create(name, email, password, role);
        
        // then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getLab()).isNull();  // 기본값
        assertThat(user.getCreatedAt()).isNull();  // JPA에서 설정
        assertThat(user.getUpdatedAt()).isNull();  // JPA에서 설정
    }
    
    @Test
    void Lab_create_팩토리_메서드_기본_랭킹_설정() {
        // given
        String name = "AI랩";
        LabCategory category = LabCategory.AI;
        String description = "인공지능 연구실";
        
        // when
        Lab lab = Lab.create(name, category, description);
        
        // then
        assertThat(lab.getName()).isEqualTo(name);
        assertThat(lab.getCategory()).isEqualTo(category);
        assertThat(lab.getDescription()).isEqualTo(description);
        assertThat(lab.getRanking()).isEqualTo(0);  // 기본값
        assertThat(lab.getProfessorName()).isNull();  // 기본값
    }
}
```

## 🎯 Domain 테스트 베스트 프랙티스

### 1. 테스트 데이터 생성 패턴

```java
// 팩토리 메서드 활용
class UserTest {
    
    private User createValidUser() {
        return User.create("홍길동", "hong@test.com", "password123!", Role.STUDENT);
    }
    
    private User createUserWithRole(Role role) {
        return User.create("사용자", "user@test.com", "password123!", role);
    }
    
    @Test
    void 테스트에서_팩토리_메서드_활용() {
        // given
        User user = createValidUser();
        
        // when & then
        assertThat(user.getRole()).isEqualTo(Role.STUDENT);
    }
}
```

### 2. 매개변수화 테스트 활용

```java
@ParameterizedTest
@CsvSource({
    "홍길동, hong@test.com, STUDENT",
    "김철수, kim@test.com, LAB_MEMBER",
    "이영희, lee@test.com, LAB_MANAGER"
})
void 다양한_역할의_사용자_생성_성공(String name, String email, Role role) {
    // when
    User user = User.create(name, email, "password123!", role);
    
    // then
    assertThat(user.getName()).isEqualTo(name);
    assertThat(user.getEmail()).isEqualTo(email);
    assertThat(user.getRole()).isEqualTo(role);
}
```

### 3. 불변 조건 테스트

```java
@Test
void 도메인_불변_조건_검증() {
    // given
    User user = createValidUser();
    
    // when & then - 불변 조건 확인
    assertThat(user.getName()).isNotBlank();
    assertThat(user.getEmail()).contains("@");
    assertThat(user.getRole()).isNotNull();
    assertThat(user.getPassword()).isNotNull();
}
```

### 4. 동등성 및 해시코드 테스트

```java
@Test
void 엔티티_동등성_ID_기반_검증() {
    // given
    User user1 = DomainUserFactory.createStudent();
    User user2 = DomainUserFactory.createStudent();
    
    user1.setId(1L);
    user2.setId(1L);
    
    User user3 = DomainUserFactory.createStudent();
    user3.setId(2L);
    
    // when & then
    assertThat(user1).isEqualTo(user2);  // 같은 ID
    assertThat(user1).isNotEqualTo(user3);  // 다른 ID
    assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
}
```

## 📊 Domain 테스트 메트릭

### 커버리지 목표

- **엔티티 클래스**: 95% 이상 (핵심 비즈니스 로직)
- **Value Object**: 100% (모든 메서드 검증)
- **Enum**: 90% 이상 (모든 값과 메서드)
- **도메인 예외**: 100% (모든 예외 케이스)

### 테스트 성능 목표

- **단위 테스트**: 평균 1ms 이하
- **테스트 격리**: 각 테스트 완전 독립
- **외부 의존성**: 0개 (순수 Java)

### 품질 체크리스트

- [ ] 모든 public 메서드 테스트
- [ ] 모든 비즈니스 규칙 검증
- [ ] 예외 발생 조건 테스트
- [ ] 상태 전이 시나리오 커버
- [ ] Value Object 불변성 검증
- [ ] 연관관계 정합성 확인

## 🔗 관련 테스트 가이드

- **테스트 전략**: `@test/CLAUDE.md`
- **Application 테스트**: `@application/CLAUDE.md`
- **테스트 팩토리**: `@testutil/factory/domain/CLAUDE.md`
- **도메인 컨벤션**: `@model/user/CONVENTIONS.md`, `@model/lab/CONVENTIONS.md`