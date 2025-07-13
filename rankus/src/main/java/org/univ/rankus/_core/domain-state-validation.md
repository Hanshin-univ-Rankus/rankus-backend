# 도메인 상태 전이 검증 가이드 (AI 필독)

> 🎯 **목적**: 도메인 객체의 상태 전이 규칙을 정확히 이해하고 테스트에서 올바르게 활용

## 🔄 주요 도메인별 상태 전이 매트릭스

### 1. AttendanceRecord 상태 전이

| 현재 상태     | → | 가능한 전이          | 금지된 전이  | 검증 메서드                   |
|-----------|---|-----------------|---------|--------------------------|
| `PRESENT` | → | ABSENT, LATE    | PRESENT | `validateStatusChange()` |
| `ABSENT`  | → | PRESENT, LATE   | ABSENT  | `validateStatusChange()` |
| `LATE`    | → | PRESENT, ABSENT | LATE    | `validateStatusChange()` |

**핵심 규칙**: 동일한 상태로의 전이는 `AttendanceValidationException` 발생

```java
// ✅ 올바른 상태 전이 패턴
AttendanceRecord record = DomainAttendanceFactory.buildAbsentRecordWithId(1L);  // ABSENT 상태
record.

markAsPresent(userId, "출석으로 변경");  // ABSENT → PRESENT ✓

// ❌ 잘못된 상태 전이 패턴
AttendanceRecord record = DomainAttendanceFactory.buildValidRecordWithId(1L);  // 기본값: PRESENT
record.

markAsPresent(userId, "출석으로 변경");  // PRESENT → PRESENT ❌ Exception 발생
```

**테스트 팩토리 활용**:

```java
// 초기 상태별 팩토리 메서드
DomainAttendanceFactory.buildValidRecord()           // PRESENT 상태 (기본)
DomainAttendanceFactory.

buildAbsentRecord()          // ABSENT 상태
DomainAttendanceFactory.

buildLateRecord()            // LATE 상태
DomainAttendanceFactory.

buildAbsentRecordWithId(1L)  // ID가 설정된 ABSENT 상태
```

### 2. ScoreSubmission 상태 전이

| 현재 상태      | → | 가능한 전이             | 금지된 전이  | 추가 제약         |
|------------|---|--------------------|---------|---------------|
| `PENDING`  | → | APPROVED, REJECTED | PENDING | 6개월 만료 정책     |
| `APPROVED` | → | -                  | 모든 전이   | 최종 상태 (변경 불가) |
| `REJECTED` | → | -                  | 모든 전이   | 최종 상태 (변경 불가) |

**특수 규칙**:

- **본인 승인 금지**: 신청자가 본인 신청을 승인할 수 없음
- **만료 정책**: 6개월 후 자동 만료
- **정정 제한**: 1회만 가능

```java
// ✅ 올바른 승인 패턴
ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();
submission.

approve(999L);  // 다른 사용자가 승인 ✓

// ❌ 잘못된 승인 패턴  
ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();
submission.

approve(submission.getUser().

getId());  // 본인이 승인 ❌ Exception 발생
```

### 3. LabApplication 상태 전이

| 현재 상태      | → | 가능한 전이             | 금지된 전이  | 추가 검증        |
|------------|---|--------------------|---------|--------------|
| `PENDING`  | → | APPROVED, REJECTED | PENDING | 면접 시간 미래여야 함 |
| `APPROVED` | → | -                  | 모든 전이   | 최종 상태        |
| `REJECTED` | → | -                  | 모든 전이   | 최종 상태        |

**비즈니스 규칙**:

- **중복 지원 금지**: 동일 사용자가 동일 랩실에 중복 지원 불가
- **면접 시간 검증**: 반드시 미래 시점이어야 함
- **최종 상태**: APPROVED/REJECTED 후 변경 불가

### 4. AttendanceSession 상태 전이

| 현재 상태       | → | 가능한 전이               | 금지된 전이 | 특수 메서드                 |
|-------------|---|----------------------|--------|------------------------|
| `ACTIVE`    | → | COMPLETED, CANCELLED | ACTIVE | `generateQRToken()` 가능 |
| `COMPLETED` | → | -                    | 모든 전이  | 최종 상태                  |
| `CANCELLED` | → | -                    | 모든 전이  | 최종 상태                  |

**핵심 제약**:

- **QR 생성 제한**: ACTIVE 상태에서만 QR 토큰 생성 가능
- **출석 체크 제한**: ACTIVE 상태에서만 출석 체크 가능
- **중복 체크 금지**: 동일 사용자가 동일 세션에 중복 출석 불가

### 5. Interview & InterviewSlot 상태 전이

| 엔티티             | 현재 상태     | → | 가능한 전이       | 특수 규칙           |
|-----------------|-----------|---|--------------|-----------------|
| `Interview`     | INACTIVE  | → | ACTIVE       | 시작일 도달 시 자동 활성화 |
| `Interview`     | ACTIVE    | → | COMPLETED    | 종료일 도달 시 자동 완료  |
| `InterviewSlot` | AVAILABLE | → | FULL, CLOSED | 신청자 수에 따라 상태 변경 |
| `InterviewSlot` | FULL      | → | AVAILABLE    | 신청 취소 시 가능      |

## 🧪 상태 전이 테스트 패턴

### 1. 정상 상태 전이 테스트

```java

@Test
@DisplayName("출석 기록 상태 변경: ABSENT → PRESENT 성공")
void attendanceRecord_정상_상태전이_성공() {
    // given
    AttendanceRecord record = DomainAttendanceFactory.buildAbsentRecord();
    Long adjustedBy = 1L;
    String reason = "수동 출석 처리";

    // when
    record.markAsPresent(adjustedBy, reason);

    // then
    assertThat(record.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    assertThat(record.isManuallyAdjusted()).isTrue();
    assertThat(record.getAdjustedBy()).isEqualTo(adjustedBy);
    assertThat(record.getAdjustmentReason()).isEqualTo(reason);
}
```

### 2. 금지된 상태 전이 테스트

```java

@Test
@DisplayName("출석 기록 동일 상태 전이 시 AttendanceValidationException 발생")
void attendanceRecord_동일상태전이_예외발생() {
    // given
    AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();  // PRESENT 상태

    // when & then
    AttendanceValidationException exception = assertThrows(
            AttendanceValidationException.class,
            () -> record.markAsPresent(1L, "동일 상태로 변경 시도")
    );

    assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.INVALID_STATUS_TRANSITION);
    assertThat(exception.getMessage()).contains("동일한 상태로는 변경할 수 없습니다");
}
```

### 3. 비즈니스 규칙 검증 테스트

```java

@Test
@DisplayName("본인 점수 신청 승인 시 RankingValidationException 발생")
void scoreSubmission_본인승인_예외발생() {
    // given
    User user = DomainUserFactory.buildValidUserWithId(1L);
    ScoreSubmission submission = DomainScoreSubmissionFactory.buildSubmissionWithUser(user);

    // when & then
    RankingValidationException exception = assertThrows(
            RankingValidationException.class,
            () -> submission.approve(user.getId())  // 본인이 승인 시도
    );

    assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.CANNOT_APPROVE_OWN_SUBMISSION);
}
```

## 🛠️ Factory 메서드 활용 전략

### Attendance 도메인 Factory

```java
public class DomainAttendanceFactory {

    // 기본 상태 (PRESENT)
    public static AttendanceRecord buildValidRecord() {
        // AttendanceRecord.create()는 기본적으로 PRESENT 상태로 생성
    }

    // 특정 상태로 생성
    public static AttendanceRecord buildAbsentRecord() {
        AttendanceRecord record = buildValidRecord();
        record.markAsAbsent(DEFAULT_CREATOR_ID, "테스트용 결석 상태");
        return record;
    }

    public static AttendanceRecord buildLateRecord() {
        AttendanceRecord record = buildValidRecord();
        record.markAsLate(DEFAULT_CREATOR_ID, "테스트용 지각 상태");
        return record;
    }

    // ID가 포함된 상태별 생성
    public static AttendanceRecord buildAbsentRecordWithId(Long recordId) {
        AttendanceRecord record = buildAbsentRecord();
        ReflectionTestUtils.setField(record, "recordId", recordId);
        return record;
    }
}
```

### Ranking 도메인 Factory

```java
public class DomainScoreSubmissionFactory {

    // 기본 상태 (PENDING)
    public static ScoreSubmission buildValidSubmission() {
        // 기본적으로 PENDING 상태로 생성
    }

    // 특정 상태로 생성
    public static ScoreSubmission buildApprovedSubmission() {
        ScoreSubmission submission = buildValidSubmission();
        ReflectionTestUtils.setField(submission, "status", SubmissionStatus.APPROVED);
        ReflectionTestUtils.setField(submission, "approvedBy", 999L);
        ReflectionTestUtils.setField(submission, "approvedAt", LocalDateTime.now());
        return submission;
    }

    public static ScoreSubmission buildRejectedSubmission() {
        ScoreSubmission submission = buildValidSubmission();
        ReflectionTestUtils.setField(submission, "status", SubmissionStatus.REJECTED);
        ReflectionTestUtils.setField(submission, "rejectionReason", "증빙서류 부족");
        return submission;
    }
}
```

## 🚨 자주 하는 실수와 해결책

### 실수 1: 기본 상태 모르고 테스트

```java
// ❌ 실수: AttendanceRecord 기본 상태를 모르고 PRESENT → PRESENT 시도
@Test
void markAsPresent_Test() {
    AttendanceRecord record = DomainAttendanceFactory.buildValidRecord();  // PRESENT 상태
    record.markAsPresent(1L, "테스트");  // ❌ Exception 발생
}

// ✅ 해결: 적절한 초기 상태 사용
@Test
void markAsPresent_Test() {
    AttendanceRecord record = DomainAttendanceFactory.buildAbsentRecord();  // ABSENT 상태
    record.markAsPresent(1L, "테스트");  // ✅ 성공
}
```

### 실수 2: 비즈니스 규칙 무시

```java
// ❌ 실수: 본인 승인 시도
ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();
submission.

approve(submission.getUser().

getId());  // ❌ Exception

// ✅ 해결: 다른 사용자 ID 사용
        submission.

approve(999L);  // ✅ 성공
```

### 실수 3: 최종 상태에서 변경 시도

```java
// ❌ 실수: APPROVED 상태에서 재승인 시도
ScoreSubmission submission = DomainScoreSubmissionFactory.buildApprovedSubmission();
submission.

approve(999L);  // ❌ Exception

// ✅ 해결: PENDING 상태에서만 승인
ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();  // PENDING
submission.

approve(999L);  // ✅ 성공
```

## 📋 상태 전이 체크리스트

### 테스트 작성 전 확인사항

- [ ] 도메인 엔티티의 초기 상태 확인
- [ ] 가능한 상태 전이 목록 확인
- [ ] 비즈니스 규칙 및 제약사항 확인
- [ ] 적절한 Factory 메서드 선택
- [ ] 예외 상황 시나리오 고려

### 상태 전이 테스트 작성 패턴

```java

@Test
@DisplayName("{도메인} {현재상태} → {목표상태} 전이 성공")
void {
    domain
}

_ {
    currentState
}

To {
    targetState
}

_Success() {
    // given - 적절한 초기 상태 설정
    {
        Domain
    } entity = {DomainFactory}.build {
        CurrentState
    } {
        Domain
    } ();

    // when - 상태 전이 실행
    entity. {
        transitionMethod
    } (validParams);

    // then - 상태 변경 검증
    assertThat(entity.getStatus()).isEqualTo({TargetStatus});
    // 추가 필드 검증
}

@Test
@DisplayName("{도메인} {현재상태} → {목표상태} 전이 실패 시 {ErrorCode} 발생")
void {
    domain
}

_ {
    currentState
}

To {
    targetState
}

_Exception() {
    // given - 상태 전이가 금지된 상태 설정
    {
        Domain
    } entity = {DomainFactory}.build {
        InvalidState
    } {
        Domain
    } ();

    // when & then - 예외 발생 검증
    {
        Domain
    } Exception exception = assertThrows({Domain}Exception.class,
            () -> entity. {
        transitionMethod
    } (validParams));
    assertThat(exception.getErrorCode()).isEqualTo({ErrorCode}. {
        SPECIFIC_ERROR
    });
}
```

---

**업데이트**: 2025-07-13 | **실제 사례**: AttendanceRecord 상태 전이 문제 해결 완료