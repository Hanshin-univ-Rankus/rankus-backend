# 상세 참조 가이드

> 깊이 있는 구현 정보와 확장 패턴

## 🏗️ 헥사고날 아키텍처

### 계층별 책임

| 계층          | 역할         | 의존성              |
|-------------|------------|------------------|
| Domain      | 순수 비즈니스 로직 | 없음               |
| Application | 유스케이스 구현   | Domain만          |
| Adapter     | 외부 기술 연동   | App + Domain     |
| Common      | 공통 컴포넌트    | 모든 계층            |

### 데이터 플로우

```
HTTP Request → Controller → UseCase → Service → Domain → Repository
                     ↓
HTTP Response ← DTO ← Entity ← Domain ← Repository
```

## 📊 도메인 모델

### 핵심 Aggregate

| Aggregate         | 상태 전이                        | 핵심 비즈니스 메서드                    |
|-------------------|------------------------------|--------------------------------|
| User              | Role 승급                      | `checkPassword()`, `assignLab()` |
| Lab               | -                            | `autoAssignProfessor()`         |
| LabApplication    | PENDING → APPROVED/REJECTED  | `approve()`, `reject()`         |
| LabNotice         | isPinned 토글                  | `pin()`, `unpin()`              |
| ScoreSubmission   | PENDING → APPROVED/REJECTED  | `approve()`, `reject()`         |
| AttendanceSession | ACTIVE → COMPLETED/CANCELLED | `generateQRToken()`, `endSession()` |

### 중요 Enum 정의

```java
Role: STUDENT < LAB_MEMBER < LAB_MANAGER < LAB_LEADER < PROFESSOR < ADMIN
ApplicationStatus: PENDING → APPROVED/REJECTED
SubmissionStatus: PENDING → APPROVED/REJECTED
SessionStatus: ACTIVE → COMPLETED/CANCELLED
AttendanceStatus: PRESENT, ABSENT, LATE
ScoreCategory: RESEARCH_SCI_PAPER(100), CONTEST_EXTERNAL_WINNER(50), ...
```

## 🔐 보안 및 권한

### 권한 매트릭스

| 리소스            | STUDENT | LAB_MEMBER | LAB_MANAGER | LAB_LEADER | ADMIN |
|----------------|---------|------------|-------------|------------|-------|
| User           | Own     | Own        | Own         | Own        | All   |
| Lab            | View    | View       | Lab         | Lab        | All   |
| LabApplication | Own     | Own+Lab    | Lab         | Lab        | All   |
| LabNotice      | Lab     | Lab        | Lab         | Lab        | All   |
| Interview      | View    | View       | Lab         | Lab        | All   |
| Ranking        | View    | View       | View        | View       | All   |

### JWT 구조

```json
{
  "sub": "user@example.com",
  "userId": 1,
  "role": "LAB_MEMBER",
  "labId": 5,
  "exp": 1640995200
}
```

## 🔧 Repository 패턴

### 복잡한 쿼리 예시

```java
// Ranking 집계
@Query("SELECT SUM(s.score) FROM ScoreSubmission s WHERE s.lab.id = :labId AND s.status = 'APPROVED'")
Integer sumApprovedScoresByLabId(@Param("labId") Long labId);

// 중복 검사
@Query("SELECT s FROM ScoreSubmission s WHERE s.user.id = :userId AND s.achievementDate = :date")
List<ScoreSubmission> findByUserIdAndAchievementDate(@Param("userId") Long userId, @Param("date") LocalDate date);

// 상위 기여자
@Query("SELECT new org.univ.rankus.dto.UserContribution(s.user.id, s.user.name, SUM(s.score)) " +
       "FROM ScoreSubmission s WHERE s.lab.id = :labId AND s.status = 'APPROVED' " +
       "GROUP BY s.user.id ORDER BY SUM(s.score) DESC")
List<UserContribution> findTopContributorsByLabId(@Param("labId") Long labId, Pageable pageable);
```

## 🧪 고급 테스트 패턴

### Controller 테스트 (Spring Boot 3.x)

```java
@WebMvcTest(AttendanceRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AttendanceRecordControllerTest {
    
    @MockitoBean // Spring Boot 3.x
    private AttendanceRecordCommandUseCase commandUseCase;
    
    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }
    
    private void setupSecurityContext(Long userId) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(userId);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(principal, null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
```

### 시간 기반 테스트

```java
@Test
void QR_만료_검증() {
    LocalDateTime expiredTime = LocalDateTime.of(2024, 1, 1, 0, 0);
    AttendanceSession session = DomainAttendanceFactory.buildSessionWithQRExpiry(expiredTime);
    
    AttendanceValidationException exception = assertThrows(
        AttendanceValidationException.class,
        () -> session.checkAttendance(userId, LocalDateTime.now())
    );
    
    assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.QR_TOKEN_EXPIRED);
}
```

## 🚨 ErrorCode 상세

### 도메인별 ErrorCode 범위

```java
// User: USER_001~010
USER_001: "이름은 필수입니다"
USER_404: "사용자를 찾을 수 없습니다"
USER_409: "이미 사용 중인 이메일입니다"

// Ranking: RANKING_001~023  
RANKING_001: "사용자는 필수입니다"
RANKING_015: "해당 점수 신청을 찾을 수 없습니다"
RANKING_016: "점수 상태 변경이 불가능합니다"

// Attendance: ATT_001~022
ATT_001: "출석 세션 제목은 필수입니다"
ATT_006: "활성화된 출석 세션이 아닙니다"
ATT_007: "이미 출석 체크되었습니다"
```

## 📋 Factory 패턴

### Domain Factory 예시

```java
public class DomainAttendanceFactory {
    public static AttendanceSession buildActiveSession() {
        return AttendanceSession.create(1L, 1L, "테스트 세션", 5);
    }
    
    public static AttendanceRecord buildAbsentRecordWithId(Long id) {
        AttendanceRecord record = new AttendanceRecord(/* params */);
        ReflectionTestUtils.setField(record, "id", id);
        record.markAsAbsent("결석 처리");
        return record;
    }
}
```

## 🔄 상태 전이 규칙

### 비즈니스 규칙

1. **ScoreSubmission**: 6개월 만료, 본인 승인 불가, 정정 1회 제한
2. **AttendanceSession**: QR 1-10분 유효, 활성 상태에서만 출석 체크
3. **LabApplication**: PENDING에서만 승인/거부 가능
4. **LabNotice**: 작성자 또는 랩실 관리자만 수정/삭제

### 권한 검증 순서

1. 인증 확인 (`isAuthenticated()`)
2. 역할 기반 권한 (`hasRole()`)
3. 소유권 검증 (`@unifiedPermissionEvaluator`)
4. 비즈니스 규칙 검증 (Domain Layer)