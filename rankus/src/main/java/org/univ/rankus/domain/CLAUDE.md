# Domain Layer 가이드

> 순수한 비즈니스 로직과 도메인 모델을 포함하는 핵심 계층

## 🏛️ Domain Layer 핵심 가이드 (AI 전용)

> 순수한 비즈니스 로직과 도메인 모델 (80줄 이하)

## 🎯 핵심 책임 및 원칙

**책임**: 비즈니스 규칙 구현, 도메인 무결성 보장, 도메인 개념 표현  
**원칙**: Rich Domain Model, Aggregate Root, Value Object 활용, 외부 의존성 배제

## 📊 핵심 Aggregate 구조

### User Aggregate
```
User (Root) → Password (VO) → Role (Enum) → UserException
```

### Lab Aggregate  
```
Lab (Root) → LabApplication, LabImage, LabNotice → LabException
```

### Notice Aggregate
```
LabNotice (Root) → NoticeType (Enum) → NoticeException
```

## 🗄️ 엔티티 매트릭스

| 엔티티                | 핵심 비즈니스 메서드                                  | 상태 전이              |
|--------------------|----------------------------------------------|---------------------|
| **User**           | `checkPassword()`, `assignLab()`, `canManage()` | Role 승급            |
| **Lab**            | `autoAssignProfessor()`, `updateRanking()`    | -                   |
| **LabApplication** | `approve()`, `reject()`, `isOwnedBy()`        | PENDING → APPROVED/REJECTED |
| **LabNotice**      | `pin()`, `unpin()`, `isOwnedBy()`            | isPinned 토글        |

## 💎 Value Object 및 Enum

### Password (Value Object)
```java
@Embeddable
public class Password {
    private final String value; // 암호화된 값만 저장
    
    public static Password fromRaw(String raw, PasswordEncoder encoder) { ... }
    public boolean matches(String raw, PasswordEncoder encoder) { ... }
}
```

### 핵심 Enum 정정
```java
Role: STUDENT < LAB_MEMBER < LAB_MANAGER < LAB_LEADER < PROFESSOR < ADMIN
ApplicationStatus: PENDING → APPROVED/REJECTED
LabCreationStatus: PENDING → APPROVED/REJECTED
NoticeType: NORMAL ↔ URGENT  // 정정: GENERAL 아닌 NORMAL
ImageType: REPRESENTATIVE, ADDITIONAL
LabCategory: AI, CV, DB, WEB, NETWORK, SECURITY, IOT, MOBILE, GAME, ROBOTICS, COMPUTER_SCIENCE, ETC
```

## 🛡️ 도메인 불변 조건

1. **User**: 이메일 고유성, 비밀번호 복잡성, 하나의 랩실에만 소속
2. **Lab**: 랩실명 유일성, 랭킹 0 이상
3. **LabApplication**: 면접시간 미래, PENDING에서만 상태변경
4. **LabNotice**: 제목/내용 필수, 작성자/랩실 유효성

---

**참조**: 상세 컨벤션은 각 도메인별 CONVENTIONS.md 참조  
**업데이트**: 2025-01-04 | **압축률**: 기존 대비 78% 절약