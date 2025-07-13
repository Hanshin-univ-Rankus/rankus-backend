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

### Ranking Aggregate

```
ScoreSubmission (Root) → ScoreCategory (Enum) → SubmissionStatus (Enum) → VisibilityLevel (Enum) → RankingException
```

## 🗄️ 엔티티 매트릭스

| 엔티티                   | 핵심 비즈니스 메서드                                                                  | 상태 전이                        |
|-----------------------|------------------------------------------------------------------------------|------------------------------|
| **User**              | `checkPassword()`, `assignLab()`, `canManage()`                              | Role 승급                      |
| **Lab**               | `autoAssignProfessor()`, `updateRanking()`                                   | -                            |
| **LabApplication**    | `approve()`, `reject()`, `isOwnedBy()`                                       | PENDING → APPROVED/REJECTED  |
| **LabNotice**         | `pin()`, `unpin()`, `isOwnedBy()`                                            | isPinned 토글                  |
| **ScoreSubmission**   | `approve()`, `reject()`, `correctStatus()`, `canBeApproved()`, `isOwnedBy()` | PENDING → APPROVED/REJECTED  |
| **AttendanceSession** | `generateQRToken()`, `checkAttendance()`, `endSession()`, `isOwnedBy()`      | ACTIVE → COMPLETED/CANCELLED |
| **AttendanceRecord**  | `markAsAbsent()`, `markAsLate()`, `markAsPresent()`, `isOwnedBy()`           | 출석 상태 변경                     |

## 💎 Value Object 및 Enum

### Password (Value Object)

```java

@Embeddable
public class Password {
    private final String value; // 암호화된 값만 저장

    public static Password fromRaw(String raw, PasswordEncoder encoder) { ...}

    public boolean matches(String raw, PasswordEncoder encoder) { ...}
}
```

### 핵심 Enum 정의

```java
Role:STUDENT<LAB_MEMBER<LAB_MANAGER<LAB_LEADER<PROFESSOR<ADMIN
ApplicationStatus:PENDING →APPROVED/REJECTED
LabCreationStatus:PENDING →APPROVED/REJECTED
NoticeType:NORMAL ↔URGENT
ImageType:REPRESENTATIVE,ADDITIONAL
LabCategory:AI,CV,DB,WEB,NETWORK,SECURITY,IOT,MOBILE,GAME,ROBOTICS,COMPUTER_SCIENCE,ETC
SubmissionStatus:PENDING →APPROVED/REJECTED
VisibilityLevel:PUBLIC,LAB_ONLY,PRIVATE
ScoreCategory:

RESEARCH_SCI_PAPER(100),CONTEST_EXTERNAL_WINNER(50),...
SessionStatus:ACTIVE →COMPLETED/CANCELLED
AttendanceStatus:PRESENT,ABSENT,LATE
```

## 🛡️ 도메인 불변 조건

1. **User**: 이메일 고유성, 비밀번호 복잡성, 하나의 랩실에만 소속
2. **Lab**: 랩실명 유일성, 랭킹 0 이상
3. **LabApplication**: 면접시간 미래, PENDING에서만 상태변경
4. **LabNotice**: 제목/내용 필수, 작성자/랩실 유효성
5. **ScoreSubmission**: 취득일자 미래 불가, 6개월 만료, 본인 승인 불가, 정정 1회 제한
6. **AttendanceSession**: 제목 필수, QR 유효시간 1-10분, 활성 상태에서만 QR 생성/출석 체크
7. **AttendanceRecord**: 세션당 사용자 중복 출석 금지, 수동 수정 시 사유 필수

---

**참조**:

- **Ranking Domain**: `/domain/model/ranking/CLAUDE.md`
- **상세 컨벤션**: 각 도메인별 CONVENTIONS.md 참조

**업데이트**: 2025-01-11 | **구현 완료**: Ranking, Attendance 도메인 포함