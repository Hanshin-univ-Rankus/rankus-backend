# 컨트롤러 권한 체크 로직 종합 분석 보고서

## 📋 목차
1. [권한 시스템 아키텍처](#1-권한-시스템-아키텍처)
2. [역할(Role) 기반 권한 계층](#2-역할role-기반-권한-계층)
3. [도메인별 권한 체크 로직 분석](#3-도메인별-권한-체크-로직-분석)
4. [엔티티 관계와 권한 전파](#4-엔티티-관계와-권한-전파)
5. [권한 체크 패턴 및 일관성 분석](#5-권한-체크-패턴-및-일관성-분석)
6. [보안 취약점 및 개선사항](#6-보안-취약점-및-개선사항)

---

## 1. 권한 시스템 아키텍처

### 1.1 권한 체크 레이어 구조
```
Controller (@PreAuthorize)
    ↓
UnifiedPermissionEvaluator
    ↓
DomainPermissionEvaluator (인터페이스)
    ↓
구체적 Handler (AttendanceRecordPermissionHandler 등)
    ↓
User Domain Model (canView*, canManage* 메서드)
    ↓
Lab, Role 기반 최종 판정
```

### 1.2 권한 체크 방식
1. **토큰 기반 역할 체크**: `hasRole('ADMIN')`, `hasRole('PROFESSOR')`
2. **도메인 객체 기반 체크**: `@unifiedPermissionEvaluator.hasPermission(...)`
3. **커스텀 핸들러 체크**: `@xxxPermissionHandler.hasPermissionForLab(...)`
4. **사용자 ID 직접 비교**: `#userId == authentication.principal.userId`

---

## 2. 역할(Role) 기반 권한 계층

### 2.1 Role 정의 및 권한 수준
```
ADMIN (최고 권한)
    └─ 모든 랩실, 모든 도메인 접근/관리 가능
    └─ 시스템 전역 관리자

PROFESSOR (교수 권한)
    └─ 모든 랩실 조회/관리 가능
    └─ 학생 관리 및 승인 권한
    └─ ADMIN과 거의 동등한 권한

LAB_LEADER (랩장)
    └─ 소속 랩실의 모든 리소스 관리
    └─ 멤버 승인/거절, 면접 관리
    └─ 공지사항, 출석, 투표, 자료실 관리

LAB_MANAGER (매니저)
    └─ 소속 랩실의 일상 운영 관리
    └─ 공지사항, 출석, 투표, 자료실 관리
    └─ 멤버 승인 권한 없음 (LAB_LEADER만 가능)

LAB_MEMBER (일반 멤버)
    └─ 소속 랩실의 정보 조회
    └─ 출석 체크, 투표 참여
    └─ 자료실 업로드/다운로드
    └─ 관리 권한 없음

STUDENT (미소속 학생)
    └─ 공개 정보 조회
    └─ 랩실 지원 가능
    └─ 랩실 리소스 접근 불가
```

### 2.2 권한 우선순위 체크 패턴
모든 Permission Handler에서 공통적으로 사용하는 패턴:
```java
// 1단계: ADMIN/PROFESSOR는 무조건 허용
for (GrantedAuthority ga : auth.getAuthorities()) {
    String a = ga.getAuthority();
    if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
        return true; // 즉시 허용, DB 조회 생략
    }
}

// 2단계: CustomUserDetails 검증
Object principalObj = auth.getPrincipal();
if (!(principalObj instanceof CustomUserDetails)) {
    return false;
}

// 3단계: User 도메인 모델 기반 권한 체크
Long userId = ((CustomUserDetails) principalObj).getUserId();
User user = userQueryUseCase.getUserById(userId);
Lab lab = labPromotionQueryUseCase.getLabById(labId);

// 4단계: 권한 타입별 분기 처리
return switch (permission) {
    case "VIEW" -> user.canViewXxx(lab);
    case "MANAGE" -> user.canManageXxx(lab);
    default -> false;
};
```

---

## 3. 도메인별 권한 체크 로직 분석

### 3.1 AttendanceRecord (출석 기록)

#### 엔티티 관계
```
AttendanceRecord → AttendanceSession → Lab
                 → User (기록 소유자)
```

#### 권한 체크 로직
**AttendanceRecordPermissionHandler**:
- `VIEW`: 
  - 자신의 기록 조회: `record.isOwnedBy(userId)`
  - 또는 랩실 출석 조회 권한: `user.canViewLabAttendance(lab)`
  - LAB_MEMBER 이상 + 같은 랩
  
- `UPDATE/DELETE/MANAGE`:
  - 랩실 출석 관리 권한: `user.canManageLabAttendance(lab)`
  - LAB_MANAGER, LAB_LEADER만 가능

#### 컨트롤러 엔드포인트별 권한
```java
// 1. GET /api/attendance/records/{recordId}
@PreAuthorize("isAuthenticated()")
// ⚠️ 문제: 인증만 확인, 권한 체크 없음 → UseCase에서 체크 의존

// 2. GET /api/attendance/records/sessions/{sessionId}
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@attendanceRecordPermissionHandler.hasPermissionForSession(authentication, #sessionId, 'VIEW')")
// ✅ 세션 기반 권한 체크 (랩 멤버 확인)

// 3. GET /api/attendance/records/sessions/{sessionId}/users/{userId}
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@unifiedPermissionEvaluator.hasPermission(authentication, #sessionId, 'AttendanceSession', 'VIEW') or " +
    "#userId == authentication.principal.userId")
// ✅ 자신의 기록은 조회 가능 OR 세션 조회 권한

// 4. GET /api/attendance/records/users/{userId}
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or #userId == authentication.principal.userId")
// ✅ 자신의 기록만 조회 가능

// 5. GET /api/attendance/records/labs/{labId}
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_ATTENDANCE')")
// ✅ 랩실 출석 조회 권한 필요 (LAB_MEMBER 이상)

// 6. PUT /api/attendance/records/{recordId}/absent
// 7. PUT /api/attendance/records/{recordId}/late
// 8. PUT /api/attendance/records/{recordId}/present
// 9. PUT /api/attendance/records/{recordId}/status
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@attendanceRecordPermissionHandler.hasPermission(authentication, #recordId, 'MANAGE')")
// ✅ 출석 관리 권한 필요 (LAB_MANAGER, LAB_LEADER만)
```

#### 기능적 특징
- **본인 기록 조회**: 누구나 자신의 출석 기록 조회 가능
- **랩실 전체 조회**: LAB_MEMBER 이상만 가능
- **상태 변경**: LAB_MANAGER, LAB_LEADER만 가능 (관리자 권한)

---

### 3.2 AttendanceSession (출석 세션)

#### 엔티티 관계
```
AttendanceSession → Lab
                  → User (생성자)
                  → AttendanceRecord[] (1:N)
```

#### 권한 체크 로직
**AttendanceSessionPermissionHandler**:
- `VIEW`: `user.canViewLabAttendance(lab)` - LAB_MEMBER 이상
- `CREATE/UPDATE/DELETE/MANAGE`: `user.canManageLabAttendance(lab)` - LAB_MANAGER 이상

#### 컨트롤러 엔드포인트별 권한
```java
// 1. POST /api/labs/{labId}/attendance/sessions (세션 생성)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication, #labId, 'MANAGE_ATTENDANCE')")
// ✅ 출석 관리 권한 필요 (LAB_MANAGER, LAB_LEADER)

// 2. POST /api/labs/{labId}/attendance/sessions/{sessionId}/end (세션 종료)
// 3. POST /api/labs/{labId}/attendance/sessions/{sessionId}/cancel (세션 취소)
// 4. PUT /api/labs/{labId}/attendance/sessions/{sessionId}/title (제목 수정)
// 5. PUT /api/labs/{labId}/attendance/sessions/{sessionId}/qr-validity (QR 유효시간 수정)
// 6. POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr (QR 생성)
// 7. POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr/secure (보안 QR 생성)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@attendanceSessionPermissionHandler.hasPermission(authentication.principal, #sessionId, 'MANAGE')")
// ⚠️ 문제: authentication.principal 전달 (authentication이어야 함)
// ✅ 세션 관리 권한 필요

// 8. POST /api/labs/{labId}/attendance/sessions/check (출석 체크)
@PreAuthorize("isAuthenticated()")
// ✅ 인증된 사용자는 누구나 출석 체크 가능 (QR 토큰 검증은 UseCase에서)

// 9. GET /api/labs/{labId}/attendance/sessions/{sessionId} (세션 조회)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@attendanceSessionPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_ATTENDANCE')")
// ✅ 랩실 출석 조회 권한 (LAB_MEMBER 이상)
```

#### 기능적 특징
- **세션 관리**: LAB_MANAGER 이상만 생성/수정/종료 가능
- **QR 코드 생성**: 관리자만 가능 (보안상 중요)
- **출석 체크**: 모든 인증된 사용자 가능 (QR 토큰 유효성으로 제한)
- **경로 검증**: labId와 세션의 실제 labId 일치 여부 확인

---

### 3.3 LabNotice (랩실 공지사항)

#### 엔티티 관계
```
LabNotice → Lab
          → User (작성자)
```

#### 권한 체크 로직
**LabNoticePermissionHandler**:
- `VIEW`: `user.canViewLabNotices(lab)` - LAB_MEMBER 이상 (같은 랩)
- `CREATE/UPDATE/DELETE/MANAGE`: `user.canManageLabNotices(lab)` - LAB_MANAGER 이상

#### 컨트롤러 엔드포인트별 권한
```java
// 1. GET /api/labs/{labId}/notices (목록 조회)
// 2. GET /api/labs/{labId}/notices/all (전체 조회)
// 3. GET /api/labs/{labId}/notices/type/{type} (타입별 조회)
// 4. GET /api/labs/{labId}/notices/pinned (고정 공지 조회)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@labNoticePermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_NOTICES')")
// ✅ 랩 멤버만 공지사항 조회 가능

// 5. GET /api/labs/{labId}/notices/{noticeId} (상세 조회)
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'VIEW')")
// ✅ 공지사항 ID 기반 권한 체크

// 6. POST /api/labs/{labId}/notices (공지사항 생성)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@labNoticePermissionHandler.hasPermissionForLab(authentication, #labId, 'MANAGE_NOTICES')")
// ✅ 공지사항 관리 권한 (LAB_MANAGER, LAB_LEADER)

// 7. PUT /api/labs/{labId}/notices/{noticeId} (수정)
// 8. PATCH /api/labs/{labId}/notices/{noticeId}/pin (고정 토글)
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'UPDATE')")
// ✅ 공지사항별 수정 권한

// 9. DELETE /api/labs/{labId}/notices/{noticeId} (삭제)
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #noticeId, 'LabNotice', 'DELETE')")
// ✅ 공지사항별 삭제 권한
```

#### 기능적 특징
- **조회 권한**: 랩 멤버만 공지사항 조회 가능 (외부인 차단)
- **관리 권한**: LAB_MANAGER 이상만 공지 작성/수정/삭제
- **고정 공지**: 중요 공지를 상단에 고정 가능 (관리자만)

---

### 3.4 CalendarSchedule (캘린더 일정)

#### 엔티티 관계
```
CalendarEvent → Lab
              → EventType (SCHEDULE / INTERVIEW)
```

#### 권한 체크 로직
**CalendarPermissionHandler**:
- `VIEW_CALENDAR`: `user.canViewLabNotices(lab)` - LAB_MEMBER 이상
- `MANAGE_CALENDAR`: `user.canManageLabNotices(lab)` - LAB_MANAGER 이상
- `VIEW_APPLICANTS`: `user.canManageLabNotices(lab)` - LAB_MANAGER 이상

⚠️ **문제점**: Calendar 권한 체크에 `canViewLabNotices`를 사용 (의미적 불일치)

#### 컨트롤러 엔드포인트별 권한
```java
// 1. GET /api/labs/{labId}/calendar/schedules (일정 목록 조회)
// 2. GET /api/labs/{labId}/calendar/schedules/{eventId} (일정 상세)
@PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_CALENDAR')")
// ✅ 랩 멤버만 캘린더 조회 가능

// 3. POST /api/labs/{labId}/calendar/schedules (일정 생성)
// 4. PUT /api/labs/{labId}/calendar/schedules/{eventId} (일정 수정)
// 5. DELETE /api/labs/{labId}/calendar/schedules/{eventId} (일정 삭제)
@PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication, #labId, 'MANAGE_CALENDAR')")
// ✅ 캘린더 관리 권한 (LAB_MANAGER, LAB_LEADER)
```

#### 기능적 특징
- **일정 조회**: 랩 멤버만 가능 (내부 일정 관리)
- **일정 관리**: LAB_MANAGER 이상만 생성/수정/삭제
- **일정 타입**: SCHEDULE(일반), INTERVIEW(면접) 구분

---

### 3.5 Interview (면접 관리)

#### 엔티티 관계
```
Interview → Lab
          → InterviewSlot[] (1:N)
          → InterviewStatus (DRAFT, ACTIVE, CLOSED)
```

#### 권한 체크 로직
**InterviewPermissionHandler**:
- `MANAGE_INTERVIEWS`: `user.canManageLabNotices(lab)` - LAB_MANAGER 이상
- `VIEW_INTERVIEWS`: `user.canViewLabNotices(lab)` - LAB_MEMBER 이상

⚠️ **문제점**: Interview 권한 체크에 `canManageLabNotices`를 사용 (의미적 불일치)

#### 컨트롤러 엔드포인트별 권한
```java
// 1. POST /api/labs/{labId}/interviews (면접 생성)
// 2. POST /api/labs/{labId}/interviews/{interviewId}/activate (활성화)
// 3. POST /api/labs/{labId}/interviews/{interviewId}/deactivate (비활성화)
// 4. POST /api/labs/{labId}/interviews/{interviewId}/close (종료)
// 5. PUT /api/labs/{labId}/interviews/{interviewId} (수정)
// 6. DELETE /api/labs/{labId}/interviews/{interviewId} (삭제)
// 7. POST /api/labs/{labId}/interviews/{interviewId}/slots (슬롯 생성)
@PreAuthorize("@interviewPermissionHandler.hasPermissionForLab(authentication, #labId, 'MANAGE_INTERVIEWS')")
// ✅ 면접 관리 권한 (LAB_MANAGER, LAB_LEADER)

// 8. GET /api/labs/{labId}/interviews (면접 목록)
// 9. GET /api/labs/{labId}/interviews/{interviewId} (면접 상세)
@PreAuthorize("공개 API - 권한 체크 없음")
// ⚠️ 문제: 누구나 면접 정보 조회 가능 (지원자를 위한 설계)
```

#### 기능적 특징
- **면접 생성**: LAB_MANAGER 이상만 가능
- **슬롯 관리**: 시간대별 면접 슬롯 생성/삭제
- **지원자 조회**: 면접 관리자만 지원자 목록 확인 가능
- **공개 정보**: 면접 일정은 지원자를 위해 공개

---

### 3.6 LabApplication (랩실 지원)

#### 엔티티 관계
```
LabApplication → Lab
               → User (지원자)
               → ApplicationStatus (PENDING, APPROVED, REJECTED, CANCELLED)
               → Interview (optional)
               → InterviewSlot (optional)
```

#### 권한 체크 로직
**LabApplicationPermissionHandler**:
- `DELETE/CANCEL`: 본인만 취소 가능 `app.isOwnedBy(userId)` OR ADMIN
- `APPROVE/REJECT/VIEW`: 랩 관리자 `user.canManageLabApplications(lab)` - LAB_LEADER, LAB_MANAGER

#### 컨트롤러 엔드포인트별 권한
```java
// 1. POST /api/labs/{labId}/applications (지원 신청)
@PreAuthorize("isAuthenticated()")
// ✅ 인증된 사용자는 누구나 지원 가능

// 2. DELETE /api/labs/{labId}/applications/{appId} (신청 취소)
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'DELETE')")
// ✅ 본인만 취소 가능

// 3. POST /api/labs/{labId}/applications/slot-based (슬롯 기반 지원)
@PreAuthorize("isAuthenticated()")
// ✅ 면접 시스템 사용 시 슬롯 선택하여 지원

// 4. GET /api/labs/{labId}/applications (지원서 목록 조회)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@labApplicationPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW')")
// ✅ 랩 관리자만 지원서 목록 조회

// 5. PUT /api/labs/{labId}/applications/{appId}/approve (승인)
// 6. PUT /api/labs/{labId}/applications/{appId}/reject (거절)
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'APPROVE')")
// ✅ 랩 관리자만 승인/거절 가능
```

#### 기능적 특징
- **지원 제한**: 랩당 1회만 지원 가능
- **면접 시스템**: 면접 슬롯 선택하여 지원 (새 방식)
- **승인 권한**: LAB_LEADER, PROFESSOR, ADMIN만 승인/거절
- **본인 취소**: 지원자는 언제든 본인 신청 취소 가능

---

### 3.7 Vote (투표)

#### 엔티티 관계
```
Vote → Lab
     → User (생성자)
     → VoteOption[] (1:N)
     → VoteParticipation[] (1:N)
     → VoteStatus (ACTIVE, CLOSED, CANCELLED)
```

#### 권한 체크 로직
**VotePermissionHandler**:
- `VIEW`: `user.canViewVotes(lab)` - LAB_MEMBER 이상
- `PARTICIPATE`: `vote.canUserParticipate(user)` - 랩 멤버 + 투표 활성화 상태
- `MANAGE`: `vote.canUserManage(user)` - 투표 생성자 OR 랩 관리자
- `DELETE`: `vote.canUserManage(user) && vote.canBeDeleted()` - 관리 권한 + 삭제 가능 상태
- `VIEW_RESULTS`: `user.canManageVotes(lab)` - LAB_MANAGER 이상

#### 컨트롤러 엔드포인트별 권한
```java
// 1. GET /api/labs/{labId}/votes (투표 목록)
// 2. GET /api/labs/{labId}/votes/all (전체 투표)
// 3. GET /api/labs/{labId}/votes/active (활성 투표)
// 4. GET /api/labs/{labId}/votes/status/{status} (상태별 투표)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@votePermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_VOTES')")
// ✅ 랩 멤버만 투표 조회 가능

// 5. GET /api/labs/{labId}/votes/{voteId} (투표 상세)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@votePermissionHandler.hasPermissionForVote(authentication, #voteId, 'VIEW')")
// ✅ 투표별 조회 권한

// 6. POST /api/labs/{labId}/votes (투표 생성)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@votePermissionHandler.hasPermissionForLab(authentication, #labId, 'CREATE_VOTE')")
// ✅ 투표 생성 권한 (LAB_MANAGER, LAB_LEADER)

// 7. POST /api/labs/{labId}/votes/{voteId}/participate (투표 참여)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@votePermissionHandler.hasPermissionForVote(authentication, #voteId, 'PARTICIPATE')")
// ✅ 투표 참여 권한 (랩 멤버 + 활성 상태)

// 8. POST /api/labs/{labId}/votes/{voteId}/close (투표 종료)
// 9. POST /api/labs/{labId}/votes/{voteId}/cancel (투표 취소)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@votePermissionHandler.hasPermissionForVote(authentication, #voteId, 'MANAGE')")
// ✅ 투표 관리 권한 (생성자 OR 랩 관리자)

// 10. GET /api/labs/{labId}/votes/{voteId}/results (결과 조회)
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@votePermissionHandler.hasPermissionForVote(authentication, #voteId, 'VIEW_RESULTS')")
// ✅ 결과 조회 권한 (LAB_MANAGER 이상)
```

#### 기능적 특징
- **투표 생성**: LAB_MANAGER 이상만 가능
- **투표 참여**: 랩 멤버만 + 중복 참여 방지
- **투표 관리**: 생성자 또는 랩 관리자만 종료/취소
- **결과 조회**: 랩 관리자만 조회 가능 (익명성 보장)

---

### 3.8 LabResource (자료실)

#### 엔티티 관계
```
LabResource → Lab
            → User (업로더)
            → ResourceCategory (LECTURE_NOTE, ASSIGNMENT, RESEARCH, etc.)
            → isPublic (공개 여부)
```

#### 권한 체크 로직
**User Domain Method**:
- `canViewLabResources(lab)`: LAB_MEMBER 이상 (같은 랩)
- `canCreateLabResources(lab)`: LAB_MEMBER 이상 (조회와 동일)
- `canManageLabResource(resource)`: 
  - 자료 업로더: `resource.isUploadedBy(this)`
  - 랩 관리자: LAB_MANAGER, LAB_LEADER (같은 랩)
  - 시스템 관리자: ADMIN, PROFESSOR
- `canDownloadLabResource(resource)`:
  - 공개 자료: 랩 멤버 누구나
  - 비공개 자료: 업로더 OR 랩 관리자만

⚠️ **문제점**: LabResource에는 별도의 PermissionHandler가 없음 (UseCase 레벨에서 체크)

#### 컨트롤러 엔드포인트별 권한
```java
// 1. GET /api/labs/{labId}/resources (자료 목록)
// 2. GET /api/labs/{labId}/resources/all (전체 자료)
// 3. GET /api/labs/{labId}/resources/{resourceId} (자료 상세)
@PreAuthorize("isAuthenticated()")
// ⚠️ 문제: 인증만 확인, UseCase에서 권한 체크

// 4. POST /api/labs/{labId}/resources (자료 업로드)
@PreAuthorize("isAuthenticated()")
// ⚠️ 문제: 인증만 확인, UseCase에서 랩 멤버 체크

// 5. PUT /api/labs/{labId}/resources/{resourceId} (자료 수정)
// 6. DELETE /api/labs/{labId}/resources/{resourceId} (자료 삭제)
@PreAuthorize("isAuthenticated()")
// ⚠️ 문제: 인증만 확인, UseCase에서 소유자/관리자 체크

// 7. GET /api/labs/{labId}/resources/{resourceId}/download (다운로드)
@PreAuthorize("isAuthenticated()")
// ⚠️ 문제: 인증만 확인, UseCase에서 공개 여부/권한 체크
```

#### 기능적 특징
- **자료 업로드**: 랩 멤버는 누구나 업로드 가능
- **공개/비공개**: 업로더가 설정 (비공개는 관리자만 다운로드)
- **자료 수정/삭제**: 업로더 OR 랩 관리자만 가능
- **다운로드 제한**: 공개는 랩 멤버 누구나, 비공개는 제한

---

### 3.9 LabPromotion (랩실 홍보)

#### 엔티티 관계
```
Lab (독립 엔티티)
    → 공개 정보 (이름, 설명, 카테고리, 순위)
```

#### 권한 체크 로직
없음 - **완전 공개 API**

#### 컨트롤러 엔드포인트별 권한
```java
// 1. GET /api/labs (랩실 목록)
// 2. GET /api/labs/{labId} (랩실 상세)
// 권한 체크 없음 (공개 API)
```

#### 기능적 특징
- **완전 공개**: 인증 없이 누구나 조회 가능
- **홍보 목적**: 랩실 홍보 및 지원 유도
- **순위 정보**: 랩실 랭킹 시스템 기반

---

## 4. 엔티티 관계와 권한 전파

### 4.1 권한 전파 구조
```
User → Lab (소속)
       ↓
       ├─ LabNotice (공지사항)
       ├─ AttendanceSession (출석 세션)
       │   └─ AttendanceRecord (출석 기록)
       ├─ CalendarEvent (캘린더)
       ├─ Interview (면접)
       │   ├─ InterviewSlot (슬롯)
       │   └─ LabApplication (지원서)
       ├─ Vote (투표)
       │   ├─ VoteOption (선택지)
       │   └─ VoteParticipation (참여)
       └─ LabResource (자료)
```

### 4.2 권한 계층 관계
```
Lab 권한 (최상위)
    ↓
Domain 권한 (중간)
    ↓
Entity 권한 (최하위)
```

**예시**: 출석 기록 조회
1. Lab 권한: 사용자가 해당 Lab의 멤버인가?
2. Domain 권한: 출석 조회 권한이 있는가? (canViewLabAttendance)
3. Entity 권한: 특정 AttendanceRecord를 볼 수 있는가? (본인 OR 관리자)

### 4.3 권한 체크 순서
```java
// 1. 토큰 기반 글로벌 권한 (최우선)
if (role == ADMIN || role == PROFESSOR) return true;

// 2. Lab 소속 확인
if (user.lab == null || !user.lab.equals(targetLab)) return false;

// 3. Role 기반 Domain 권한
if (permission == MANAGE && role < LAB_MANAGER) return false;
if (permission == VIEW && role < LAB_MEMBER) return false;

// 4. Entity 소유권 확인 (특정 케이스)
if (entity.isOwnedBy(user)) return true;
```

---

## 5. 권한 체크 패턴 및 일관성 분석

### 5.1 일관된 패턴 (✅)
```java
// Pattern 1: Lab 기반 권한 체크
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@xxxPermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_XXX')")

// Pattern 2: Entity ID 기반 권한 체크
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #entityId, 'EntityType', 'VIEW')")

// Pattern 3: 자신의 리소스만 접근
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")

// Pattern 4: 인증만 필요 (UseCase에서 추가 체크)
@PreAuthorize("isAuthenticated()")
```

### 5.2 불일치 패턴 (⚠️)

#### 5.2.1 Authentication vs Authentication.Principal
```java
// ❌ 잘못된 사용
@PreAuthorize("@handler.hasPermission(authentication.principal, #id, 'MANAGE')")

// ✅ 올바른 사용
@PreAuthorize("@handler.hasPermission(authentication, #id, 'MANAGE')")
```
**발견 위치**: 
- `AttendanceSessionController` 일부 엔드포인트
- Handler는 `Authentication` 객체를 받아야 함

#### 5.2.2 의미적 권한 메서드 불일치
```java
// Calendar 권한 체크에 Notice 메서드 사용
public boolean hasPermissionForLab(Authentication auth, Long labId, String permission) {
    return switch (perm) {
        case MANAGE_CALENDAR -> user.canManageLabNotices(lab);  // ⚠️
        case VIEW_CALENDAR -> user.canViewLabNotices(lab);      // ⚠️
    };
}

// Interview 권한 체크에도 Notice 메서드 사용
case MANAGE_INTERVIEWS -> user.canManageLabNotices(lab);  // ⚠️
case VIEW_INTERVIEWS -> user.canViewLabNotices(lab);      // ⚠️
```
**문제점**: 의미적으로 다른 도메인인데 같은 권한 메서드 사용

#### 5.2.3 LabResource 권한 체크 부재
```java
// 모든 LabResource 엔드포인트
@PreAuthorize("isAuthenticated()")
```
**문제점**: 컨트롤러 레벨에서 권한 체크 없음, UseCase에 의존

### 5.3 권한 체크 레벨 비교

| 도메인 | Controller 체크 | Handler 체크 | UseCase 체크 | Domain 체크 |
|--------|----------------|--------------|--------------|-------------|
| AttendanceRecord | ✅ 부분적 | ✅ | ✅ | ✅ |
| AttendanceSession | ✅ | ✅ | ✅ | ✅ |
| LabNotice | ✅ | ✅ | ✅ | ✅ |
| Calendar | ✅ | ✅ | ✅ | ✅ |
| Interview | ✅ 부분적 | ✅ | ✅ | ✅ |
| LabApplication | ✅ | ✅ | ✅ | ✅ |
| Vote | ✅ | ✅ | ✅ | ✅ |
| LabResource | ⚠️ 인증만 | ❌ | ✅ | ✅ |
| LabPromotion | ❌ 공개 | ❌ | ❌ | ❌ |

---

## 6. 보안 취약점 및 개선사항

### 6.1 🔴 Critical (긴급 수정 필요)

#### 1. LabResource 권한 체크 누락
**문제**:
```java
@GetMapping("/{resourceId}")
@PreAuthorize("isAuthenticated()")  // ⚠️ 인증만 확인
public ResponseEntity<ApiResponse<LabResourceResponseDto>> getLabResource(
    @PathVariable Long labId,
    @PathVariable Long resourceId,
    @AuthenticationPrincipal CustomUserDetails currentUser
) {
    // UseCase에서만 권한 체크
}
```

**위험도**: 중간 - UseCase에서 체크하지만 일관성 없음

**개선안**:
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@labResourcePermissionHandler.hasPermissionForLab(authentication, #labId, 'VIEW_RESOURCES')")
```

#### 2. Authentication.Principal 전달 오류
**문제**:
```java
@PreAuthorize("@attendanceSessionPermissionHandler.hasPermission(authentication.principal, #sessionId, 'MANAGE')")
```

**위험도**: 높음 - Handler가 null 또는 타입 불일치로 권한 체크 실패 가능

**개선안**:
```java
@PreAuthorize("@attendanceSessionPermissionHandler.hasPermission(authentication, #sessionId, 'MANAGE')")
```

**영향 범위**:
- `POST /api/labs/{labId}/attendance/sessions/{sessionId}/end`
- `POST /api/labs/{labId}/attendance/sessions/{sessionId}/cancel`
- `PUT /api/labs/{labId}/attendance/sessions/{sessionId}/title`
- `PUT /api/labs/{labId}/attendance/sessions/{sessionId}/qr-validity`
- `POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr`
- `POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr/secure`

### 6.2 🟡 Medium (개선 권장)

#### 1. 의미적 권한 메서드 불일치
**문제**:
```java
// CalendarPermissionHandler.java
case MANAGE_CALENDAR -> user.canManageLabNotices(lab);  // ⚠️ Notice 메서드 사용

// InterviewPermissionHandler.java
case MANAGE_INTERVIEWS -> user.canManageLabNotices(lab);  // ⚠️ Notice 메서드 사용
```

**개선안**: User 도메인에 전용 메서드 추가
```java
// User.java에 추가
public boolean canManageLabCalendar(Lab lab) {
    if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
        return true;
    }
    return this.lab != null && this.lab.equals(lab)
            && (this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
}

public boolean canViewLabCalendar(Lab lab) {
    if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
        return true;
    }
    return this.lab != null && this.lab.equals(lab)
            && (this.role == Role.LAB_MEMBER || this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
}

public boolean canManageLabInterviews(Lab lab) {
    // 동일한 로직
}

public boolean canViewLabInterviews(Lab lab) {
    // 동일한 로직
}
```

#### 2. AttendanceRecord 개별 조회 권한 부재
**문제**:
```java
@GetMapping("/{recordId}")
@PreAuthorize("isAuthenticated()")  // ⚠️ 권한 체크 없음
```

**개선안**:
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or " +
    "@attendanceRecordPermissionHandler.hasPermission(authentication, #recordId, 'VIEW')")
```

#### 3. Interview 공개 API 보안
**문제**:
```java
@GetMapping  // 권한 체크 없음
public ResponseEntity<ApiResponse<List<InterviewResponseDto>>> getInterviewsByLab(
    @PathVariable Long labId
) {
    // 누구나 면접 정보 조회 가능
}
```

**의도**: 지원자를 위한 공개 정보

**개선안**: 필요시 상세 정보는 제한
```java
// 공개: 면접 날짜, 시간, 기본 정보
// 제한: 지원자 목록, 슬롯별 신청 현황 (관리자만)
```

### 6.3 🟢 Low (장기 개선)

#### 1. PermissionConstants 활용도 낮음
**현황**: 상수는 정의되어 있으나 컨트롤러 SpEL에서는 문자열 직접 사용

**개선안**: SpEL에서도 상수 참조 가능하도록 구조 개선 (선택사항)

#### 2. 권한 체크 로직 중복
**문제**: 모든 Handler에서 동일한 ADMIN/PROFESSOR 체크 반복

**개선안**: 추상 클래스 또는 유틸리티 메서드로 공통화
```java
public abstract class BasePermissionHandler {
    protected boolean isGlobalAdmin(Authentication auth) {
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
                return true;
            }
        }
        return false;
    }
}
```

#### 3. labId 경로 검증 불일치
**일부 API**: labId와 실제 엔티티의 labId 일치 여부 검증
**일부 API**: 검증 없음

**개선안**: 모든 API에서 일관되게 검증
```java
// UseCase에서 공통 검증 로직
if (!entity.getLabId().equals(labId)) {
    throw new PathMismatchException("경로의 labId와 리소스가 속한 랩이 일치하지 않습니다");
}
```

---

## 7. 권한 체크 흐름도 요약

### 7.1 일반적인 권한 체크 흐름
```
사용자 요청
    ↓
Controller @PreAuthorize
    ↓
[1단계] ROLE_ADMIN / ROLE_PROFESSOR 체크
    ↓ (통과 시 즉시 허용)
[2단계] CustomUserDetails 추출
    ↓
[3단계] User, Lab, Entity 조회
    ↓
[4단계] User Domain 권한 메서드 호출
    ↓
[5단계] Role + Lab 소속 여부 판정
    ↓
결과 반환 (true/false)
```

### 7.2 예외 케이스

#### Case 1: 자신의 리소스 접근
```
if (entity.isOwnedBy(userId)) return true;
```
- 출석 기록 조회: 본인 기록은 항상 조회 가능
- 투표 관리: 투표 생성자는 관리 권한 보유
- 자료 수정/삭제: 업로더는 항상 가능

#### Case 2: 공개 API
```
@GetMapping  // 권한 체크 없음
```
- LabPromotion: 랩실 홍보 정보 (완전 공개)
- Interview 목록: 지원자를 위한 공개 정보

#### Case 3: UseCase 레벨 권한 체크
```
@PreAuthorize("isAuthenticated()")
// UseCase에서 상세 권한 체크
```
- LabResource: 컨트롤러는 인증만, UseCase에서 상세 체크
- AttendanceRecord 일부: 복잡한 로직은 UseCase에서 처리

---

## 8. 최종 권장사항

### 8.1 즉시 수정 필요
1. ✅ **AttendanceSessionController**: `authentication.principal` → `authentication` 수정
2. ✅ **LabResourceController**: 권한 체크 추가 또는 PermissionHandler 생성

### 8.2 중기 개선
1. **User Domain**: Calendar, Interview 전용 권한 메서드 추가
2. **AttendanceRecordController**: 개별 조회 권한 체크 강화
3. **공통 Handler**: 중복 코드 제거 (BaseHandler 도입)

### 8.3 장기 개선
1. **권한 체크 일관성**: 모든 도메인에서 동일한 패턴 적용
2. **labId 검증**: 경로와 실제 엔티티 소속 일치 여부 검증 강화
3. **문서화**: 각 API별 필요 권한 명세서 작성

---

## 부록: 권한 매트릭스

### A. Role별 접근 가능한 기능

| 기능 | STUDENT | LAB_MEMBER | LAB_MANAGER | LAB_LEADER | PROFESSOR | ADMIN |
|------|---------|-----------|-------------|-----------|-----------|-------|
| 랩실 목록 조회 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 랩실 지원 | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ |
| 공지사항 조회 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 공지사항 작성 | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ |
| 출석 체크 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 출석 세션 생성 | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ |
| 출석 상태 변경 | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ |
| 투표 조회 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 투표 참여 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 투표 생성 | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ |
| 자료 조회 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 자료 업로드 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 타인 자료 삭제 | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ |
| 지원서 승인 | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| 면접 관리 | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ |

### B. API별 필요 최소 권한

| API 엔드포인트 | 최소 권한 | 추가 조건 |
|---------------|----------|----------|
| `GET /api/labs` | 없음 | 공개 API |
| `POST /api/labs/{labId}/applications` | STUDENT | 인증 필요 |
| `GET /api/labs/{labId}/notices` | LAB_MEMBER | 같은 랩 |
| `POST /api/labs/{labId}/notices` | LAB_MANAGER | 같은 랩 |
| `POST /api/labs/{labId}/attendance/sessions` | LAB_MANAGER | 같은 랩 |
| `POST /api/attendance/sessions/check` | LAB_MEMBER | QR 토큰 |
| `PUT /api/attendance/records/{id}/status` | LAB_MANAGER | 같은 랩 |
| `POST /api/labs/{labId}/votes` | LAB_MANAGER | 같은 랩 |
| `POST /api/labs/{labId}/votes/{id}/participate` | LAB_MEMBER | 같은 랩 |
| `POST /api/labs/{labId}/resources` | LAB_MEMBER | 같은 랩 |
| `DELETE /api/labs/{labId}/resources/{id}` | 업로더 OR LAB_MANAGER | 소유권 또는 관리 |

---

**작성일**: 2025-01-21  
**분석 대상**: Rankus 프로젝트 20개 컨트롤러, 11개 PermissionHandler  
**분석 방법**: 코드 정적 분석, 권한 체크 로직 추적, 엔티티 관계 매핑

