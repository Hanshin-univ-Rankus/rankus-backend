# Domain Layer 가이드

> 순수한 비즈니스 로직과 도메인 모델을 포함하는 핵심 계층

## 🏛️ Domain Layer 개요

### 핵심 책임

- **비즈니스 규칙 구현**: 엔티티 내부의 도메인 로직
- **도메인 무결성 보장**: 데이터 검증 및 제약조건 관리
- **도메인 개념 표현**: 업무 영역의 핵심 개념을 코드로 모델링
- **외부 의존성 배제**: 프레임워크나 라이브러리에 독립적

### 설계 원칙

- **Rich Domain Model**: 엔티티가 행위(메서드)를 포함
- **Aggregate Root**: 관련 엔티티들의 일관성 경계
- **Value Object 활용**: 불변 객체로 도메인 개념 표현
- **Domain Event**: 도메인 변경사항을 이벤트로 표현

## 📊 도메인 모델 구조

### 핵심 Aggregate

#### 1. User Aggregate

```
User (Root)
├── Password (Value Object)
└── UserException (Domain Exception)
```

#### 2. Lab Aggregate

```
Lab (Root)
├── LabApplication (Entity)
├── LabImage (Entity)
├── LabCreationRequest (Entity)
├── LabNotice (Entity)
└── LabException (Domain Exception)
```

#### 3. Notice Aggregate

```
LabNotice (Root)
├── NoticeType (Enum)
└── NoticeException (Domain Exception)
```

### 도메인 관계도

```
User ────── ManyToOne ──────► Lab
 │                           │
 │                           ├── LabApplication
 │                           ├── LabImage
 │                           ├── LabNotice
 └── LabApplication ◄────────┘
 └── LabCreationRequest ◄────┘
 └── LabNotice ◄─────────────┘
```

## 🗄️ 엔티티 상세 정보

### User Entity

- **역할**: 시스템 사용자 (학생, 교수, 관리자)
- **핵심 비즈니스 로직**:
    - `checkPassword()`: 로그인 시 비밀번호 검증
    - `changePassword()`: 비밀번호 변경 및 검증
    - `assignLab()`: 랩실 배정
    - `canManageLabApplications()`: 지원서 관리 권한 확인

### Lab Entity

- **역할**: 연구실 정보 및 운영 관리
- **핵심 비즈니스 로직**:
    - `autoAssignProfessorIfMatches()`: 교수 자동 할당
    - 랭킹 계산 로직 (향후 구현)

### LabApplication Entity

- **역할**: 랩실 지원서 및 상태 관리
- **핵심 비즈니스 로직**:
    - `approve()`: 지원 승인 (PENDING → APPROVED)
    - `reject()`: 지원 거부 (PENDING → REJECTED)
    - `isOwnedBy()`: 소유권 확인

### LabImage Entity

- **역할**: 랩실 홍보 이미지 관리
- **이미지 타입**: REPRESENTATIVE(대표), ADDITIONAL(추가)

### LabCreationRequest Entity

- **역할**: 랩실 생성 요청 및 승인 관리
- **핵심 비즈니스 로직**:
    - `approve()`: 생성 요청 승인 (PENDING → APPROVED)
    - `reject()`: 생성 요청 거부 (PENDING → REJECTED)
    - `isOwnedBy()`: 요청자 소유권 확인

### LabNotice Entity

- **역할**: 랩실 공지사항 관리 및 게시
- **테이블**: `lab_notice`
- **연관관계**: 
    - User (ManyToOne - 작성자)
    - Lab (ManyToOne - 소속 랩실)
- **핵심 비즈니스 로직**:
    - `pin()`: 공지사항 고정
    - `unpin()`: 공지사항 고정 해제
    - `togglePin()`: 고정 상태 토글
    - `update()`: 제목, 내용, 타입 일괄 수정
    - `isOwnedBy()`: 작성자 소유권 확인
    - `canBeViewedBy()`: 조회 권한 확인
    - `canBeModifiedBy()`: 수정 권한 확인

## 🎭 도메인 Enum 정의

### Role (사용자 역할)

- `STUDENT`: 일반 학생
- `LAB_MEMBER`: 랩실 멤버
- `LAB_MANAGER`: 랩실 관리자
- `LAB_LEADER`: 랩장
- `PROFESSOR`: 교수
- `ADMIN`: 시스템 관리자

### LabCategory (연구 분야)

- 기술 분야: `AI`, `CV`, `DB`, `WEB`, `NETWORK`, `SECURITY`
- 응용 분야: `IOT`, `MOBILE`, `GAME`, `ROBOTICS`
- 기타: `COMPUTER_SCIENCE`, `ETC`

### ApplicationStatus (지원 상태)

- `PENDING`: 심사 대기
- `APPROVED`: 승인됨
- `REJECTED`: 거부됨

### LabCreationStatus (랩실 생성 요청 상태)

- `PENDING`: 심사 대기
- `APPROVED`: 승인됨
- `REJECTED`: 거부됨

### NoticeType (공지사항 타입)

- `NORMAL`: 일반 공지
- `URGENT`: 긴급 공지

## 💎 Value Object 활용

### Password Value Object

```java

@Embeddable
public class Password {
    // 암호화된 비밀번호만 저장, 평문 저장 금지
    private String value;

    // 팩토리 메서드로 생성 (PasswordEncoder 주입 필요)
    public static Password fromRaw(String rawPassword, PasswordEncoder encoder);

    // 비밀번호 일치 확인 (PasswordEncoder 주입 필요)
    public boolean matches(String rawPassword, PasswordEncoder encoder);
}
```

**설계 특징**:

- 불변 객체 (Immutable)
- 도메인 검증 로직 포함
- 암호화 책임은 도메인 인터페이스로 분리

## ⚠️ 도메인 예외 처리

### 예외 계층 구조

```
BaseCustomException
├── UserException
│   ├── UserNotFoundException
│   ├── UserValidationException
│   └── PasswordValidationException
├── LabException
│   ├── LabNotFoundException
│   ├── LabApplicationException
│   ├── LabImageException
│   └── LabCreationRequestException
│       ├── LabCreationRequestNotFoundException
│       └── LabCreationRequestValidationException
└── NoticeException
    ├── NoticeNotFoundException
    └── NoticeValidationException
```

### ErrorCode 패턴

```java
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다"),
    INVALID_PASSWORD(400, "비밀번호가 올바르지 않습니다"),
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다");
}
```

## 🔒 도메인 불변 조건 (Invariant)

### User Aggregate 불변 조건

1. 이메일은 고유해야 함
2. 비밀번호는 최소 8자 이상
3. 사용자는 최대 하나의 랩실에만 소속

### Lab Aggregate 불변 조건

1. 랩실명은 필수이며 중복 불가
2. 랭킹은 0 이상의 정수
3. 한 사용자는 동일 랩실에 중복 지원 불가

### LabApplication 불변 조건

1. 면접 시간은 미래 시점이어야 함
2. PENDING 상태에서만 승인/거부 가능
3. 지원자 본인만 지원서 취소 가능

### LabCreationRequest 불변 조건

1. 요청 제목과 설명은 필수 항목
2. PENDING 상태에서만 승인/거부 가능
3. 요청자 본인만 요청서 수정/삭제 가능
4. 승인 시 자동으로 Lab 엔티티 생성

### LabNotice 불변 조건

1. 제목과 내용은 필수 항목
2. 제목은 100자, 내용은 2000자 이내
3. 작성자와 소속 랩실은 필수 관계
4. 고정 상태는 명시적 메서드로만 변경 가능
5. 수정 권한은 작성자, 랩실 관리자, 교수, 관리자만 보유
6. 타입은 NORMAL 또는 URGENT만 허용
7. 삭제 권한은 수정 권한과 동일한 규칙 적용

## 🎯 도메인 서비스 (향후 확장)

### 현재 미구현, 향후 필요한 도메인 서비스

- **RankingCalculationService**: 랭킹 점수 계산 로직
- **InterviewScheduleService**: 면접 시간 충돌 방지 로직
- **LabCapacityService**: 랩실 정원 관리 로직

## 📋 도메인 모델 상세 가이드

도메인 모델의 구체적인 구현 방법과 컨벤션은 다음 파일을 참조하세요:

- **도메인 모델 구현**: @model/CLAUDE.md
- **User 도메인 컨벤션**: @model/user/CONVENTIONS.md
- **Lab 도메인 컨벤션**: @model/lab/CONVENTIONS.md
- **Notice 도메인 가이드**: @model/notice/CLAUDE.md
- **Notice 도메인 컨벤션**: @model/notice/CONVENTIONS.md

## 🧪 도메인 테스트 전략

### 단위 테스트 중점사항

- **비즈니스 로직 검증**: 엔티티 메서드의 정확한 동작
- **불변 조건 확인**: 도메인 규칙 위반시 예외 발생
- **Value Object 테스트**: 불변성과 동등성 검증
- **상태 전이 테스트**: 엔티티 상태 변경 로직

### 테스트 예시

```java

@Test
void 지원서_승인시_상태가_APPROVED로_변경된다() {
    // given
    LabApplication application = createPendingApplication();

    // when
    application.approve();

    // then
    assertThat(application.getStatus()).isEqualTo(APPROVED);
}
```