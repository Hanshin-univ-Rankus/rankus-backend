# Rankus 프론트엔드 개발 참고 문서

> 대학 내 랩실 정보 불균형 해소와 랩실 운영 효율화를 위한 종합 플랫폼

## 📋 목차

1. [프로젝트 전체 개요](#프로젝트-전체-개요)
2. [핵심 기능 및 사용자 흐름](#핵심-기능-및-사용자-흐름)
3. [인증 및 사용자 관리](#인증-및-사용자-관리)
4. [랩실 관리 시스템](#랩실-관리-시스템)
5. [면접 시스템](#면접-시스템)
6. [QR 출석 시스템](#qr-출석-시스템)
7. [투표 시스템](#투표-시스템)
8. [캘린더 시스템](#캘린더-시스템)
9. [권한 시스템](#권한-시스템)
10. [랭킹 시스템](#랭킹-시스템)
11. [데이터 내보내기 및 통계](#데이터-내보내기-및-통계)
12. [API Request/Response 상세 예시](#api-requestresponse-상세-예시)
13. [에러 처리 및 상태 코드](#에러-처리-및-상태-코드)
14. [페이징 및 필터링](#페이징-및-필터링)
15. [파일 업로드 및 미디어 처리](#파일-업로드-및-미디어-처리)
16. [API 엔드포인트 참조](#api-엔드포인트-참조)
17. [프론트엔드-백엔드 협업 가이드](#프론트엔드-백엔드-협업-가이드)

---

## 프로젝트 전체 개요

### 🎯 핵심 목표
- **랩실 홍보**: 랩실 정보 공개와 지원자 모집
- **지원 관리**: 체계적인 지원서 접수 및 면접 관리
- **운영 효율화**: QR 출석, 투표, 일정 관리 등
- **동기 부여**: 랭킹 시스템을 통한 경쟁과 성과 관리

### 🏗️ 기술 스택
- **백엔드**: Java 17, Spring Boot 3.4.5, MySQL 8.0
- **인증**: JWT 토큰 기반 인증
- **API 문서**: Swagger UI (`http://localhost:8080/swagger-ui.html`)
- **아키텍처**: 헥사고날 아키텍처 (Ports & Adapters)

### 📊 구현 현황
- **완료 (95%)**: 회원 관리, 랩실 홍보, 지원 시스템, 면접, QR 출석, 랭킹, 투표, 캘린더, 랩실 멤버 관리, 통계 및 내보내기
- **진행 중**: 알림 시스템

---

## 핵심 기능 및 사용자 흐름

### 👥 주요 사용자 유형

1. **일반 학생 (STUDENT)**
   - 랩실 정보 탐색 → 지원서 작성 → 면접 참여 → 멤버 활동

2. **랩실 멤버 (LAB_MEMBER)**
   - 랩실 활동 참여 → 출석 체크 → 투표 참여 → 랭킹 기여

3. **랩실 매니저 (LAB_MANAGER)**
   - 멤버 관리 → 일정 조율 → 투표 생성 → 출석 관리

4. **랩실 리더 (LAB_LEADER)**
   - 전체 랩실 운영 → 멤버 권한 관리 → 면접 진행 → 통계 확인

5. **교수 (PROFESSOR)**
   - 랩실 생성 승인 → 전체 랩실 관리 → 시스템 감독

6. **관리자 (ADMIN)**
   - 시스템 전체 관리 → 사용자 권한 조정 → 데이터 관리

### 🔄 메인 사용자 플로우

```
1. 회원가입/로그인
   ↓
2. 랩실 탐색 (홍보 페이지)
   ↓
3. 관심 랩실 지원서 작성
   ↓
4. 면접 일정 선택 및 참여
   ↓
5. 승인 시 랩실 멤버 활동 시작
   ↓
6. 일상 활동 (출석, 투표, 일정 관리, 랭킹 참여)
```

---

## 인증 및 사용자 관리

### 🔐 인증 시스템

#### API 엔드포인트
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인
- `GET /api/users/me` - 내 정보 조회

#### 회원가입 플로우
1. **필수 정보 입력**
   ```json
   {
     "name": "홍길동",
     "email": "hong@example.com",
     "password": "securePassword123",
     "studentNumber": "20230001",
     "phoneNumber": "010-1234-5678",
     "grade": 3,
     "enrollmentStatus": "ENROLLED"
   }
   ```

2. **유효성 검증**
   - 이메일 형식 및 중복 확인
   - 학번 8-20자리 숫자
   - 전화번호 10-15자리 숫자/하이픈
   - 학년 1-8 범위

3. **자동 권한 할당**
   - 기본값: `STUDENT` 역할
   - 추후 랩실 가입 시 역할 변경 가능

#### 로그인 플로우
1. **인증 정보 전송**
   ```json
   {
     "email": "hong@example.com",
     "password": "securePassword123"
   }
   ```

2. **JWT 토큰 응답**
   ```json
   {
     "success": true,
     "data": {
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "user": {
         "id": 1,
         "name": "홍길동",
         "email": "hong@example.com",
         "role": "STUDENT"
       }
     }
   }
   ```

#### 인증 상태 관리
- **토큰 저장**: `localStorage` 또는 `sessionStorage`
- **자동 로그아웃**: 토큰 만료 시
- **권한 확인**: API 호출 시 `Authorization: Bearer {token}` 헤더 필요

### 👤 사용자 정보 관리

#### 사용자 역할 (Role)
```typescript
enum Role {
  STUDENT = "STUDENT",           // 일반 학생
  LAB_MEMBER = "LAB_MEMBER",     // 랩실 멤버
  LAB_MANAGER = "LAB_MANAGER",   // 랩실 매니저
  LAB_LEADER = "LAB_LEADER",     // 랩실 리더
  PROFESSOR = "PROFESSOR",       // 교수
  ADMIN = "ADMIN"               // 시스템 관리자
}
```

#### 재학 상태 (EnrollmentStatus)
```typescript
enum EnrollmentStatus {
  ENROLLED = "ENROLLED",         // 재학
  LEAVE = "LEAVE",              // 휴학
  GRADUATED = "GRADUATED"        // 졸업
}
```

---

## 랩실 관리 시스템

### 🏢 랩실 홍보 시스템

#### API 엔드포인트
- `GET /api/labs` - 전체 랩실 목록 (랭킹순)
- `GET /api/labs/{labId}` - 특정 랩실 상세 정보

#### 랩실 정보 구조
```typescript
interface Lab {
  id: number;
  name: string;                    // 랩실명 (최대 10자)
  category: LabCategory;           // 랩실 분야
  description?: string;            // 랩실 설명 (최대 255자)
  professorName?: string;          // 교수명 (최대 10자)
  ranking: number;                 // 랭킹 점수
  images?: LabImage[];            // 랩실 이미지들
  memberCount?: number;           // 멤버 수
  averageScore?: number;          // 평균 점수
}
```

#### 랩실 카테고리
```typescript
enum LabCategory {
  AI_ML = "AI_ML",                 // AI/머신러닝
  WEB = "WEB",                     // 웹 개발
  MOBILE = "MOBILE",               // 모바일 개발
  GAME = "GAME",                   // 게임 개발
  SECURITY = "SECURITY",           // 보안
  NETWORK = "NETWORK",             // 네트워크
  DATABASE = "DATABASE",           // 데이터베이스
  EMBEDDED = "EMBEDDED",           // 임베디드
  OTHER = "OTHER"                  // 기타
}
```

### 📝 랩실 지원 시스템

#### API 엔드포인트
- `POST /api/labs/{labId}/applications` - 랩실 지원 (레거시) - 면접 안보고 지원
- `POST /api/labs/{labId}/applications/slot-based` - 슬롯 기반 지원 - 면접 시간 선택 지원
- `GET /api/labs/{labId}/applications` - 지원서 목록 조회
- `PUT /api/labs/{labId}/applications/{appId}/approve` - 지원서 승인
- `PUT /api/labs/{labId}/applications/{appId}/reject` - 지원서 거절
- `DELETE /api/labs/{labId}/applications/{appId}` - 지원서 취소

#### 지원 플로우 (슬롯 기반)
1. **면접 일정 확인**
   ```typescript
   // GET /api/labs/{labId}/interviews/{interviewId}/slots/available
   interface InterviewSlot {
     id: number;
     startTime: string;           // ISO datetime
     endTime: string;             // ISO datetime
     maxApplicants: number;       // 최대 지원자 수
     currentApplicants: number;   // 현재 지원자 수
     isAvailable: boolean;        // 예약 가능 여부
   }
   ```

2. **지원서 작성**
   ```json
   {
     "slotId": 1
   }
   ```

3. **지원서 상태 관리**
   ```typescript
   enum ApplicationStatus {
     PENDING = "PENDING",         // 대기 중
     APPROVED = "APPROVED",       // 승인됨
     REJECTED = "REJECTED"        // 거절됨
   }
   ```

#### 권한별 기능 차이
- **학생**: 지원서 작성/취소만 가능
- **랩실 매니저/리더**: 지원서 승인/거절 가능
- **교수/관리자**: 모든 랩실 지원서 관리 가능

### 🏗️ 랩실 생성 요청

#### API 엔드포인트
- `POST /api/lab-creation-requests` - 랩실 생성 요청
- `GET /api/lab-creation-requests` - 생성 요청 목록
- `PUT /api/lab-creation-requests/{requestId}/approve` - 요청 승인
- `PUT /api/lab-creation-requests/{requestId}/reject` - 요청 거절

#### 생성 요청 플로우
1. **요청서 작성** (학생 → 교수/관리자)
2. **검토 및 승인** (교수/관리자)
3. **랩실 생성** (자동)
4. **신청자를 랩실 리더로 배정** (자동)

---

## 면접 시스템

### 🎯 면접 관리의 복잡성

면접 시스템은 **이중 구조**로 설계되어 있어 프론트엔드에서 주의 깊게 처리해야 합니다.

#### 핵심 개념
1. **Interview (면접 설정)**: 랩실별 면접 기간과 기본 설정
2. **InterviewSlot (면접 슬롯)**: 실제 면접 시간대와 예약 관리
3. **LabApplication (지원서)**: 지원자와 면접 슬롯 연결

### 📋 면접 설정 관리

#### API 엔드포인트
- `POST /api/labs/{labId}/interviews` - 면접 생성
- `GET /api/labs/{labId}/interviews` - 랩실별 면접 목록
- `POST /api/labs/{labId}/interviews/{interviewId}/activate` - 면접 활성화
- `POST /api/labs/{labId}/interviews/{interviewId}/deactivate` - 면접 비활성화
- `POST /api/labs/{labId}/interviews/{interviewId}/close` - 면접 종료

#### 면접 설정 생성
```typescript
interface InterviewCreateRequest {
  startDate: string;              // YYYY-MM-DD
  endDate: string;                // YYYY-MM-DD
  durationMinutes: number;        // 면접 소요 시간 (분)
  maxApplicantsPerSlot: number;   // 슬롯당 최대 지원자 수
}
```

#### 면접 상태 관리
```typescript
enum InterviewStatus {
  INACTIVE = "INACTIVE",          // 비활성 (생성 직후)
  ACTIVE = "ACTIVE",             // 활성 (지원 접수 중)
  CLOSED = "CLOSED"              // 종료 (지원 불가)
}
```

### ⏰ 면접 슬롯 관리

#### API 엔드포인트
- `POST /api/labs/{labId}/interviews/{interviewId}/slots` - 슬롯 생성
- `POST /api/labs/{labId}/interviews/{interviewId}/slots/batch` - 슬롯 일괄 생성
- `GET /api/labs/{labId}/interviews/{interviewId}/slots` - 슬롯 목록
- `GET /api/labs/{labId}/interviews/{interviewId}/slots/available` - 예약 가능한 슬롯

#### 슬롯 생성 예시
```typescript
interface SlotCreateRequest {
  startTime: string;              // ISO datetime: "2024-01-15T10:00:00"
  endTime: string;                // ISO datetime: "2024-01-15T10:30:00"
  maxApplicants: number;          // 이 슬롯의 최대 지원자 수
}

// 일괄 생성 예시
interface BatchSlotCreateRequest {
  slots: SlotCreateRequest[];
}
```

#### 슬롯 상태 관리
```typescript
enum SlotStatus {
  AVAILABLE = "AVAILABLE",        // 예약 가능
  RESERVED = "RESERVED",         // 예약됨 (부분)
  FULL = "FULL",                 // 만석
  CANCELED = "CANCELED"          // 취소됨
}
```

### 🔄 면접 시스템 사용자 플로우

#### 랩실 관리자 플로우
```
1. 면접 설정 생성 (기간, 소요시간 등)
   ↓
2. 면접 슬롯 생성 (구체적인 시간대들)
   ↓
3. 면접 활성화 (지원 접수 시작)
   ↓
4. 지원서 검토 및 승인/거절
   ↓
5. 면접 진행
   ↓
6. 면접 종료
```

#### 지원자 플로우
```
1. 활성화된 면접 확인
   ↓
2. 예약 가능한 슬롯 조회
   ↓
3. 원하는 슬롯 선택하여 지원
   ↓
4. 지원서 상태 확인
   ↓
5. 승인 시 면접 참여
```

### ⚠️ 프론트엔드 주의사항

#### 1. 상태 확인 로직
```typescript
// 면접 지원 가능 여부 확인
function canApplyToInterview(interview: Interview): boolean {
  return interview.status === 'ACTIVE' && 
         isWithinDateRange(interview.startDate, interview.endDate);
}

// 슬롯 예약 가능 여부 확인
function canReserveSlot(slot: InterviewSlot): boolean {
  return slot.status === 'AVAILABLE' && 
         slot.currentApplicants < slot.maxApplicants;
}
```

#### 2. 실시간 업데이트 필요
- 슬롯 예약 상황은 실시간으로 변할 수 있음
- 지원서 작성 페이지에서 주기적으로 슬롯 상태 확인 필요

#### 3. 에러 처리
```typescript
// 일반적인 면접 관련 에러들
enum InterviewErrors {
  INTERVIEW_NOT_ACTIVE = "면접이 활성화되지 않았습니다",
  SLOT_NOT_AVAILABLE = "해당 시간대는 예약할 수 없습니다",
  ALREADY_APPLIED = "이미 지원하신 랩실입니다",
  INTERVIEW_EXPIRED = "면접 기간이 종료되었습니다"
}
```

---

## QR 출석 시스템

### 📱 QR 출석 시스템 구조

QR 출석 시스템은 **출석 세션**과 **출석 기록**으로 구성되어 있습니다.

#### 핵심 개념
1. **AttendanceSession**: 출석을 체크할 수 있는 세션 (QR 코드 생성)
2. **AttendanceRecord**: 개별 사용자의 출석 기록
3. **QRToken**: 제한된 시간 동안 유효한 QR 코드 토큰

### 🎫 출석 세션 관리

#### API 엔드포인트
- `POST /api/labs/{labId}/attendance/sessions` - 출석 세션 생성
- `POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr` - QR 코드 생성
- `POST /api/labs/{labId}/attendance/sessions/{sessionId}/end` - 세션 종료
- `GET /api/labs/{labId}/attendance/sessions/active` - 활성 세션 목록

#### 출석 세션 생성
```typescript
interface AttendanceSessionCreateRequest {
  title: string;                  // 출석 세션 제목
  qrValidityMinutes: number;      // QR 코드 유효 시간 (분)
}

// 응답 예시
interface AttendanceSession {
  id: number;
  title: string;
  status: SessionStatus;          // ACTIVE, ENDED, CANCELED
  qrValidityMinutes: number;
  createdAt: string;
  labId: number;
}
```

#### 세션 상태 관리
```typescript
enum SessionStatus {
  ACTIVE = "ACTIVE",              // 활성 (출석 체크 가능)
  ENDED = "ENDED",               // 종료됨
  CANCELED = "CANCELED"          // 취소됨
}
```

### 📷 QR 코드 생성 및 스캔

#### QR 코드 생성 플로우
```typescript
// 1. QR 코드 생성 요청
// POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr
interface QRTokenResponse {
  token: string;                  // QR 코드에 포함될 토큰
  qrCodeBase64: string;          // Base64 인코딩된 QR 이미지
  expiresAt: string;             // 만료 시간 (ISO datetime)
}

// 2. 프론트엔드에서 QR 이미지 표시
function displayQRCode(qrData: QRTokenResponse) {
  const img = document.createElement('img');
  img.src = `data:image/png;base64,${qrData.qrCodeBase64}`;
  // QR 코드 표시
}
```

#### 출석 체크 플로우
```typescript
// 1. QR 코드 스캔 후 토큰 추출
// 2. 출석 체크 API 호출
// POST /api/labs/{labId}/attendance/sessions/check
interface AttendanceCheckRequest {
  qrToken: string;                // 스캔한 QR 토큰
}

interface AttendanceRecord {
  id: number;
  userId: number;
  sessionId: number;
  status: AttendanceStatus;       // PRESENT, LATE, ABSENT
  checkedAt: string;              // 출석 체크 시간
}
```

#### 출석 상태
```typescript
enum AttendanceStatus {
  PRESENT = "PRESENT",            // 출석
  LATE = "LATE",                 // 지각
  ABSENT = "ABSENT"              // 결석
}
```

### 🔄 출석 시스템 사용자 플로우

#### 랩실 관리자 플로우
```
1. 출석 세션 생성 (제목, QR 유효시간 설정)
   ↓
2. QR 코드 생성 및 표시
   ↓
3. 학생들의 출석 체크 대기
   ↓
4. 출석 현황 실시간 모니터링
   ↓
5. 세션 종료
   ↓
6. 출석 통계 확인
```

#### 학생 플로우
```
1. 활성 출석 세션 확인
   ↓
2. QR 코드 스캔 (카메라 사용)
   ↓
3. 출석 체크 완료 확인
   ↓
4. 개인 출석 기록 확인
```

### 📊 출석 통계 및 관리

#### API 엔드포인트
- `GET /api/labs/{labId}/attendance/sessions/{sessionId}/statistics` - 세션별 통계
- `GET /api/labs/{labId}/attendance/sessions/my-sessions` - 내 출석 기록
- `GET /api/labs/{labId}/attendance/records` - 출석 기록 목록

#### 출석 통계 구조
```typescript
interface AttendanceStatistics {
  totalMembers: number;           // 총 멤버 수
  presentCount: number;           // 출석자 수
  lateCount: number;             // 지각자 수
  absentCount: number;           // 결석자 수
  attendanceRate: number;        // 출석률 (%)
}
```

### ⚠️ 프론트엔드 주의사항

#### 1. QR 코드 스캔 구현
```typescript
// 카메라 권한 요청 및 QR 스캔 라이브러리 사용 예시
import { Html5QrcodeScanner } from "html5-qrcode";

function initQRScanner() {
  const scanner = new Html5QrcodeScanner(
    "qr-reader", 
    { fps: 10, qrbox: 250 }
  );
  
  scanner.render(onScanSuccess, onScanError);
}

function onScanSuccess(decodedText: string) {
  // decodedText에서 토큰 추출하여 출석 체크 API 호출
  checkAttendance(decodedText);
}
```

#### 2. 실시간 업데이트
- QR 코드는 제한된 시간만 유효하므로 주기적 갱신 필요
- 출석 현황은 실시간으로 업데이트되어야 함

#### 3. 에러 처리
```typescript
enum AttendanceErrors {
  INVALID_QR_TOKEN = "유효하지 않은 QR 코드입니다",
  EXPIRED_QR_TOKEN = "만료된 QR 코드입니다",
  ALREADY_CHECKED = "이미 출석 체크를 완료했습니다",
  SESSION_NOT_ACTIVE = "출석 세션이 활성화되지 않았습니다"
}
```

---

## 투표 시스템

### 🗳️ 투표 시스템의 복잡성

투표 시스템은 **생성**, **참여**, **관리**의 복잡한 권한 체계와 상태 관리가 필요합니다.

#### 핵심 개념
1. **Vote**: 투표 본체 (제목, 설명, 마감일)
2. **VoteOption**: 투표 선택지 (최대 5개)
3. **VoteParticipation**: 사용자의 투표 참여 기록

### 📝 투표 생성 및 관리

#### API 엔드포인트
- `POST /api/labs/{labId}/votes` - 투표 생성
- `GET /api/labs/{labId}/votes` - 랩실별 투표 목록 (페이징)
- `GET /api/labs/{labId}/votes/active` - 활성 투표 목록
- `PATCH /api/labs/{labId}/votes/{voteId}/close` - 투표 종료
- `DELETE /api/labs/{labId}/votes/{voteId}` - 투표 삭제

#### 투표 생성 요청
```typescript
interface VoteCreateRequest {
  title: string;                  // 투표 제목 (최대 200자)
  description?: string;           // 투표 설명 (최대 1000자)
  deadline: string;               // 마감일 (ISO datetime)
  optionTexts: string[];          // 선택지 텍스트 배열 (2-5개)
}

// 예시
const voteRequest: VoteCreateRequest = {
  title: "랩실 정기 모임 시간 투표",
  description: "매주 진행할 정기 모임의 최적 시간을 선택해 주세요.",
  deadline: "2024-01-20T23:59:59",
  optionTexts: [
    "화요일 오후 6시",
    "수요일 오후 7시", 
    "목요일 오후 6시",
    "금요일 오후 5시"
  ]
};
```

#### 투표 상태 관리
```typescript
enum VoteStatus {
  ACTIVE = "ACTIVE",              // 활성 (투표 가능)
  CLOSED = "CLOSED",             // 종료됨 (결과 확인 가능)
  CANCELED = "CANCELED"          // 취소됨
}
```

### 🎯 투표 참여 시스템

#### API 엔드포인트
- `POST /api/labs/{labId}/votes/{voteId}/participate` - 투표 참여
- `GET /api/labs/{labId}/votes/{voteId}/my-participation` - 내 투표 여부 확인
- `GET /api/labs/{labId}/votes/{voteId}/participations` - 투표 참여 기록 (관리자용)

#### 투표 참여 플로우
```typescript
// 1. 투표 상세 정보 조회
interface Vote {
  id: number;
  title: string;
  description?: string;
  status: VoteStatus;
  deadline: string;
  totalVotes: number;
  options: VoteOption[];
  canParticipate: boolean;        // 현재 사용자 참여 가능 여부
  hasParticipated: boolean;       // 현재 사용자 참여 완료 여부
}

interface VoteOption {
  id: number;
  text: string;
  voteCount: number;              // 득표 수
  percentage: number;             // 득표율
}

// 2. 투표 참여
interface VoteParticipateRequest {
  optionId: number;
}
```

### 🔐 투표 권한 시스템

투표 시스템의 권한은 **매우 세밀하게** 설계되어 있습니다.

#### 권한별 가능한 작업
```typescript
interface VotePermissions {
  // 투표 조회
  canView: boolean;               // 투표 목록/상세 조회
  
  // 투표 참여
  canParticipate: boolean;        // 투표 참여
  
  // 투표 생성
  canCreate: boolean;             // 새 투표 생성
  
  // 투표 관리
  canManage: boolean;             // 투표 종료/취소
  canDelete: boolean;             // 투표 삭제 (참여자 없을 때만)
  canViewResults: boolean;        // 참여 기록 상세 조회
}

// 역할별 권한 매트릭스
const VOTE_PERMISSIONS = {
  STUDENT: {
    canView: false,         // 랩실 소속이 아니면 조회 불가
    canParticipate: false,  // 랩실 소속이 아니면 참여 불가
    canCreate: false,
    canManage: false,
    canDelete: false,
    canViewResults: false
  },
  LAB_MEMBER: {
    canView: true,          // 소속 랩실만
    canParticipate: true,   // 소속 랩실만
    canCreate: false,
    canManage: false,
    canDelete: false,
    canViewResults: false
  },
  LAB_MANAGER: {
    canView: true,
    canParticipate: true,
    canCreate: true,        // 소속 랩실에만
    canManage: true,        // 소속 랩실에만
    canDelete: true,        // 소속 랩실에만
    canViewResults: true    // 소속 랩실에만
  },
  LAB_LEADER: {
    canView: true,
    canParticipate: true,
    canCreate: true,
    canManage: true,
    canDelete: true,
    canViewResults: true
  },
  PROFESSOR: {
    canView: true,          // 모든 랩실
    canParticipate: true,   // 모든 랩실
    canCreate: true,        // 모든 랩실
    canManage: true,        // 모든 랩실
    canDelete: true,        // 모든 랩실
    canViewResults: true    // 모든 랩실
  },
  ADMIN: {
    // 모든 권한 true
  }
};
```

### 📊 투표 결과 시각화

#### 실시간 결과 업데이트
```typescript
// 투표 결과 데이터 구조
interface VoteResults {
  vote: {
    id: number;
    title: string;
    status: VoteStatus;
    totalVotes: number;
    deadline: string;
  };
  options: {
    id: number;
    text: string;
    voteCount: number;
    percentage: number;
  }[];
  participations: {
    userId: number;
    userName: string;
    selectedOptionId: number;
    participatedAt: string;
  }[];  // 관리자만 조회 가능
}

// 프론트엔드에서 차트 라이브러리 사용 예시
function renderVoteChart(results: VoteResults) {
  const chartData = results.options.map(option => ({
    label: option.text,
    value: option.percentage,
    count: option.voteCount
  }));
  
  // Chart.js, D3.js 등을 사용한 시각화
}
```

### 🔄 투표 시스템 사용자 플로우

#### 투표 생성자 플로우 (매니저/리더)
```
1. 투표 정보 입력 (제목, 설명, 마감일)
   ↓
2. 선택지 입력 (2-5개)
   ↓
3. 투표 생성 및 자동 활성화
   ↓
4. 멤버들에게 알림 (선택사항)
   ↓
5. 투표 진행 상황 모니터링
   ↓
6. 적절한 시점에 투표 종료
   ↓
7. 결과 분석 및 공유
```

#### 투표 참여자 플로우 (멤버)
```
1. 랩실 활성 투표 목록 확인
   ↓
2. 투표 상세 내용 검토
   ↓
3. 선택지 중 하나 선택
   ↓
4. 투표 참여 완료 확인
   ↓
5. 실시간 결과 확인 (선택사항)
```

### ⚠️ 프론트엔드 주의사항

#### 1. 권한 기반 UI 제어
```typescript
function VoteComponent({ vote, userRole, userLab }: VoteProps) {
  const permissions = calculateVotePermissions(userRole, userLab, vote.lab);
  
  return (
    <div>
      {permissions.canView && <VoteDetails vote={vote} />}
      {permissions.canParticipate && !vote.hasParticipated && 
        <VoteForm voteId={vote.id} options={vote.options} />}
      {permissions.canManage && 
        <VoteManagementButtons voteId={vote.id} status={vote.status} />}
      {permissions.canViewResults && 
        <VoteResults voteId={vote.id} />}
    </div>
  );
}
```

#### 2. 실시간 상태 업데이트
- 투표 마감 시간에 따른 자동 상태 변경
- 다른 사용자의 투표 참여에 따른 결과 업데이트

#### 3. 에러 처리
```typescript
enum VoteErrors {
  ALREADY_PARTICIPATED = "이미 투표에 참여했습니다",
  VOTE_EXPIRED = "투표 기간이 종료되었습니다",
  INVALID_OPTION = "유효하지 않은 선택지입니다",
  PERMISSION_DENIED = "투표 권한이 없습니다",
  CANNOT_DELETE_VOTE = "참여자가 있는 투표는 삭제할 수 없습니다"
}
```

---

## 캘린더 시스템

### 📅 캘린더 시스템의 이중 구조

캘린더 시스템은 **일반 일정**과 **면접 일정**으로 구분되어 있어 각각 다른 API와 데이터 구조를 사용합니다.

#### 핵심 개념
1. **CalendarEvent**: 캘린더에 표시되는 모든 이벤트의 기본 엔티티
2. **EventType**: SCHEDULE (일반 일정) / INTERVIEW (면접 일정)
3. **자동 연동**: 면접 슬롯 생성 시 캘린더 이벤트 자동 생성

### 📋 일반 일정 관리

#### API 엔드포인트
- `GET /api/labs/{labId}/calendar/schedules` - 일반 일정 목록 조회
- `POST /api/labs/{labId}/calendar/schedules` - 일반 일정 생성
- `PUT /api/labs/{labId}/calendar/schedules/{eventId}` - 일반 일정 수정
- `DELETE /api/labs/{labId}/calendar/schedules/{eventId}` - 일반 일정 삭제

#### 일반 일정 데이터 구조
```typescript
interface ScheduleEvent {
  id: number;
  type: "SCHEDULE";
  title: string;                  // 일정 제목 (최대 100자)
  description?: string;           // 일정 설명 (최대 500자)
  eventDate: string;              // YYYY-MM-DD (날짜만)
  startTime?: null;               // 일반 일정은 시간 없음
  endTime?: null;                 // 일반 일정은 시간 없음
  labId: number;
}

// 일반 일정 생성 요청
interface ScheduleCreateRequest {
  title: string;
  description?: string;
  eventDate: string;              // YYYY-MM-DD
}
```

### 🎯 면접 일정 관리

#### API 엔드포인트
- `GET /api/labs/{labId}/calendar/interviews` - 면접 일정 목록 조회
- 면접 일정은 **Interview 시스템과 자동 연동**되므로 직접 생성/수정하지 않음

#### 면접 일정 데이터 구조
```typescript
interface InterviewEvent {
  id: number;
  type: "INTERVIEW";
  title: string;                  // 자동 생성: "면접 - {랩실명}"
  description?: string;           // 면접 설명
  eventDate: string;              // YYYY-MM-DD
  startTime: string;              // HH:mm:ss
  endTime: string;                // HH:mm:ss
  interviewId: number;            // 연결된 면접 ID
  labId: number;
}
```

### 📊 통합 캘린더 뷰

#### 통합 조회 API
```typescript
// 기간별 전체 일정 조회 (일반 + 면접)
// GET /api/labs/{labId}/calendar/events?startDate=2024-01-01&endDate=2024-01-31

interface CalendarEventResponse {
  schedules: ScheduleEvent[];     // 일반 일정들
  interviews: InterviewEvent[];   // 면접 일정들
}

// 프론트엔드에서 통합 처리
function mergeCalendarEvents(data: CalendarEventResponse): CalendarEvent[] {
  return [
    ...data.schedules.map(event => ({ ...event, eventType: 'schedule' })),
    ...data.interviews.map(event => ({ ...event, eventType: 'interview' }))
  ].sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime());
}
```

### 🔄 캘린더 시스템 사용자 플로우

#### 랩실 관리자 플로우
```
1. 캘린더 뷰에서 월/주 단위 일정 확인
   ↓
2-A. 일반 일정 생성 (미팅, 세미나 등)
2-B. 면접 설정 및 슬롯 생성 (자동으로 캘린더 연동)
   ↓
3. 일정 충돌 검토 및 조정
   ↓
4. 멤버들과 일정 공유
```

#### 랩실 멤버 플로우
```
1. 랩실 캘린더 확인
   ↓
2. 다가오는 일정 파악
   ↓
3. 면접 일정의 경우 지원자 정보 확인 (권한 있는 경우)
   ↓
4. 개인 일정과 비교하여 참석 계획 수립
```

### 🎨 프론트엔드 구현 가이드

#### 캘린더 라이브러리 활용
```typescript
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';

function LabCalendar({ labId }: { labId: number }) {
  const [events, setEvents] = useState<CalendarEvent[]>([]);
  
  const fetchEvents = async (startDate: string, endDate: string) => {
    const data = await api.getCalendarEvents(labId, startDate, endDate);
    const mergedEvents = mergeCalendarEvents(data);
    
    // FullCalendar 형식으로 변환
    const calendarEvents = mergedEvents.map(event => ({
      id: event.id.toString(),
      title: event.title,
      date: event.eventDate,
      start: event.startTime ? `${event.eventDate}T${event.startTime}` : event.eventDate,
      end: event.endTime ? `${event.eventDate}T${event.endTime}` : undefined,
      backgroundColor: event.type === 'INTERVIEW' ? '#ff6b6b' : '#4ecdc4',
      extendedProps: {
        type: event.type,
        description: event.description,
        interviewId: event.interviewId
      }
    }));
    
    setEvents(calendarEvents);
  };
  
  return (
    <FullCalendar
      plugins={[dayGridPlugin, timeGridPlugin]}
      initialView="dayGridMonth"
      events={events}
      eventClick={handleEventClick}
      datesSet={({ start, end }) => fetchEvents(
        start.toISOString().split('T')[0],
        end.toISOString().split('T')[0]
      )}
    />
  );
}
```

#### 이벤트 타입별 스타일링
```css
/* 일반 일정 */
.fc-event.schedule-event {
  background-color: #4ecdc4;
  border-color: #45b7b8;
}

/* 면접 일정 */
.fc-event.interview-event {
  background-color: #ff6b6b;
  border-color: #ee5a52;
}

/* 시간이 있는 이벤트 (면접) */
.fc-event.has-time {
  border-left: 4px solid #333;
}

/* 하루 종일 이벤트 (일반 일정) */
.fc-event.all-day {
  opacity: 0.7;
}
```

### ⚠️ 프론트엔드 주의사항

#### 1. 자동 연동 처리
```typescript
// 면접 슬롯 생성 시 캘린더 자동 업데이트
async function createInterviewSlot(interviewId: number, slotData: SlotCreateRequest) {
  try {
    const result = await api.createInterviewSlot(interviewId, slotData);
    
    // 면접 슬롯 생성 후 캘린더 이벤트도 자동 생성됨
    // 캘린더 뷰 새로고침 필요
    refreshCalendarView();
    
    return result;
  } catch (error) {
    // 에러 처리
  }
}
```

#### 2. 권한 기반 기능 제어
```typescript
interface CalendarPermissions {
  canViewCalendar: boolean;       // 캘린더 조회
  canManageSchedule: boolean;     // 일반 일정 관리
  canManageInterview: boolean;    // 면접 일정 관리 (Interview 시스템 통해서)
}

// 권한에 따른 UI 제어
function CalendarActions({ permissions }: { permissions: CalendarPermissions }) {
  return (
    <div>
      {permissions.canManageSchedule && (
        <Button onClick={() => setShowScheduleModal(true)}>
          일정 추가
        </Button>
      )}
      {permissions.canManageInterview && (
        <Button onClick={() => setShowInterviewModal(true)}>
          면접 설정
        </Button>
      )}
    </div>
  );
}
```

#### 3. 날짜/시간 처리
```typescript
// 시간대 관련 주의사항
const formatEventTime = (event: CalendarEvent) => {
  if (event.type === 'SCHEDULE') {
    // 일반 일정: 날짜만 표시
    return format(new Date(event.eventDate), 'yyyy-MM-dd');
  } else {
    // 면접 일정: 날짜 + 시간 표시
    const startDateTime = `${event.eventDate}T${event.startTime}`;
    const endDateTime = `${event.eventDate}T${event.endTime}`;
    return `${format(new Date(startDateTime), 'HH:mm')} - ${format(new Date(endDateTime), 'HH:mm')}`;
  }
};
```

---

## 권한 시스템

### 🔐 권한 시스템의 복잡성

Rankus의 권한 시스템은 **역할 기반**과 **도메인별 세밀한 권한**이 결합된 복잡한 구조입니다.

#### 권한 체계 개요
1. **기본 역할 (Role)**: 사용자의 기본 권한 수준
2. **랩실 소속**: 특정 랩실에 대한 권한
3. **도메인별 권한**: 기능별 세밀한 권한 제어
4. **컨텍스트 권한**: 상황에 따른 동적 권한

### 👥 사용자 역할 (Role) 체계

```typescript
enum Role {
  STUDENT = "STUDENT",           // 일반 학생
  LAB_MEMBER = "LAB_MEMBER",     // 랩실 멤버
  LAB_MANAGER = "LAB_MANAGER",   // 랩실 매니저
  LAB_LEADER = "LAB_LEADER",     // 랩실 리더
  PROFESSOR = "PROFESSOR",       // 교수
  ADMIN = "ADMIN"               // 시스템 관리자
}
```

#### 역할별 기본 권한
```typescript
interface RolePermissions {
  // 시스템 전체
  canAccessSystem: boolean;
  canCreateLab: boolean;
  
  // 사용자 관리
  canManageUsers: boolean;
  canChangeUserRoles: boolean;
  
  // 랩실 관리
  canManageAllLabs: boolean;
  canApproveLabCreation: boolean;
}

const ROLE_PERMISSIONS: Record<Role, RolePermissions> = {
  STUDENT: {
    canAccessSystem: true,
    canCreateLab: false,
    canManageUsers: false,
    canChangeUserRoles: false,
    canManageAllLabs: false,
    canApproveLabCreation: false
  },
  LAB_MEMBER: {
    canAccessSystem: true,
    canCreateLab: false,
    canManageUsers: false,
    canChangeUserRoles: false,
    canManageAllLabs: false,
    canApproveLabCreation: false
  },
  LAB_MANAGER: {
    canAccessSystem: true,
    canCreateLab: true,       // 랩실 생성 요청 가능
    canManageUsers: false,
    canChangeUserRoles: false,
    canManageAllLabs: false,
    canApproveLabCreation: false
  },
  LAB_LEADER: {
    canAccessSystem: true,
    canCreateLab: true,
    canManageUsers: false,    // 자신의 랩실 멤버만
    canChangeUserRoles: false, // 자신의 랩실 멤버만
    canManageAllLabs: false,
    canApproveLabCreation: false
  },
  PROFESSOR: {
    canAccessSystem: true,
    canCreateLab: true,
    canManageUsers: true,     // 모든 사용자
    canChangeUserRoles: true, // 모든 사용자
    canManageAllLabs: true,   // 모든 랩실
    canApproveLabCreation: true
  },
  ADMIN: {
    // 모든 권한 true
  }
};
```

### 🏢 랩실별 권한 시스템

#### 랩실 관련 권한 매트릭스
```typescript
interface LabPermissions {
  // 기본 접근
  canViewLab: boolean;
  canApplyToLab: boolean;
  
  // 지원서 관리
  canViewApplications: boolean;
  canManageApplications: boolean;
  
  // 면접 관리
  canViewInterviews: boolean;
  canManageInterviews: boolean;
  
  // 멤버 관리
  canViewMembers: boolean;
  canManageMembers: boolean;
  canTransferLeadership: boolean;
  
  // 출석 관리
  canViewAttendance: boolean;
  canManageAttendance: boolean;
  
  // 투표 관리
  canViewVotes: boolean;
  canCreateVotes: boolean;
  canManageVotes: boolean;
  
  // 캘린더 관리
  canViewCalendar: boolean;
  canManageCalendar: boolean;
  
  // 공지사항 관리
  canViewNotices: boolean;
  canManageNotices: boolean;
  
  // 통계 및 데이터
  canViewStatistics: boolean;
  canExportData: boolean;
}

// 역할과 랩실 소속에 따른 권한 계산
function calculateLabPermissions(
  userRole: Role, 
  userLabId: number | null, 
  targetLabId: number
): LabPermissions {
  const isOwnLab = userLabId === targetLabId;
  
  switch (userRole) {
    case 'STUDENT':
      return {
        canViewLab: true,
        canApplyToLab: !isOwnLab,        // 다른 랩실만 지원 가능
        canViewApplications: false,
        canManageApplications: false,
        canViewInterviews: true,
        canManageInterviews: false,
        canViewMembers: isOwnLab,
        canManageMembers: false,
        canTransferLeadership: false,
        canViewAttendance: false,
        canManageAttendance: false,
        canViewVotes: false,
        canCreateVotes: false,
        canManageVotes: false,
        canViewCalendar: false,
        canManageCalendar: false,
        canViewNotices: false,
        canManageNotices: false,
        canViewStatistics: false,
        canExportData: false
      };
      
    case 'LAB_MEMBER':
      return {
        canViewLab: true,
        canApplyToLab: false,
        canViewApplications: false,
        canManageApplications: false,
        canViewInterviews: isOwnLab,
        canManageInterviews: false,
        canViewMembers: isOwnLab,
        canManageMembers: false,
        canTransferLeadership: false,
        canViewAttendance: isOwnLab,
        canManageAttendance: false,
        canViewVotes: isOwnLab,
        canCreateVotes: false,
        canManageVotes: false,
        canViewCalendar: isOwnLab,
        canManageCalendar: false,
        canViewNotices: isOwnLab,
        canManageNotices: false,
        canViewStatistics: false,
        canExportData: false
      };
      
    case 'LAB_MANAGER':
      return {
        canViewLab: true,
        canApplyToLab: false,
        canViewApplications: isOwnLab,
        canManageApplications: isOwnLab,
        canViewInterviews: isOwnLab,
        canManageInterviews: isOwnLab,
        canViewMembers: isOwnLab,
        canManageMembers: isOwnLab,       // 역할 변경 제한적
        canTransferLeadership: false,
        canViewAttendance: isOwnLab,
        canManageAttendance: isOwnLab,
        canViewVotes: isOwnLab,
        canCreateVotes: isOwnLab,
        canManageVotes: isOwnLab,
        canViewCalendar: isOwnLab,
        canManageCalendar: isOwnLab,
        canViewNotices: isOwnLab,
        canManageNotices: isOwnLab,
        canViewStatistics: isOwnLab,
        canExportData: isOwnLab
      };
      
    case 'LAB_LEADER':
      return {
        // LAB_MANAGER의 모든 권한 + 추가 권한
        ...calculateLabPermissions('LAB_MANAGER', userLabId, targetLabId),
        canManageMembers: isOwnLab,       // 모든 멤버 역할 변경 가능
        canTransferLeadership: isOwnLab,  // 리더십 이양 가능
      };
      
    case 'PROFESSOR':
    case 'ADMIN':
      return {
        // 모든 권한 true (모든 랩실에 대해)
        canViewLab: true,
        canApplyToLab: false,
        canViewApplications: true,
        canManageApplications: true,
        canViewInterviews: true,
        canManageInterviews: true,
        canViewMembers: true,
        canManageMembers: true,
        canTransferLeadership: true,
        canViewAttendance: true,
        canManageAttendance: true,
        canViewVotes: true,
        canCreateVotes: true,
        canManageVotes: true,
        canViewCalendar: true,
        canManageCalendar: true,
        canViewNotices: true,
        canManageNotices: true,
        canViewStatistics: true,
        canExportData: true
      };
  }
}
```

### 🔍 프론트엔드 권한 체크 구현

#### 권한 체크 훅 (React Hook)
```typescript
interface User {
  id: number;
  role: Role;
  labId?: number;
}

interface PermissionContext {
  user: User;
  targetLabId?: number;
  resourceOwnerId?: number;
}

function usePermissions(context: PermissionContext) {
  const labPermissions = useMemo(() => {
    if (!context.targetLabId) return null;
    return calculateLabPermissions(
      context.user.role, 
      context.user.labId, 
      context.targetLabId
    );
  }, [context.user, context.targetLabId]);
  
  const hasPermission = useCallback((permission: keyof LabPermissions): boolean => {
    if (!labPermissions) return false;
    return labPermissions[permission];
  }, [labPermissions]);
  
  const isOwner = useCallback((resourceOwnerId?: number): boolean => {
    return resourceOwnerId === context.user.id;
  }, [context.user.id]);
  
  const canManageResource = useCallback((
    permission: keyof LabPermissions, 
    resourceOwnerId?: number
  ): boolean => {
    return hasPermission(permission) || isOwner(resourceOwnerId);
  }, [hasPermission, isOwner]);
  
  return {
    labPermissions,
    hasPermission,
    isOwner,
    canManageResource
  };
}
```

#### 컴포넌트에서 권한 체크 사용
```typescript
function LabManagementPage({ labId }: { labId: number }) {
  const { user } = useAuth();
  const { hasPermission, canManageResource } = usePermissions({
    user,
    targetLabId: labId
  });
  
  return (
    <div>
      {hasPermission('canViewMembers') && (
        <MemberList labId={labId} />
      )}
      
      {hasPermission('canManageApplications') && (
        <ApplicationManagement labId={labId} />
      )}
      
      {hasPermission('canCreateVotes') && (
        <CreateVoteButton labId={labId} />
      )}
      
      {hasPermission('canExportData') && (
        <ExportDataButton labId={labId} />
      )}
    </div>
  );
}
```

#### 조건부 렌더링 컴포넌트
```typescript
interface ProtectedComponentProps {
  permission: keyof LabPermissions;
  labId?: number;
  resourceOwnerId?: number;
  fallback?: React.ReactNode;
  children: React.ReactNode;
}

function ProtectedComponent({ 
  permission, 
  labId, 
  resourceOwnerId, 
  fallback, 
  children 
}: ProtectedComponentProps) {
  const { user } = useAuth();
  const { hasPermission, canManageResource } = usePermissions({
    user,
    targetLabId: labId,
    resourceOwnerId
  });
  
  const hasAccess = resourceOwnerId 
    ? canManageResource(permission, resourceOwnerId)
    : hasPermission(permission);
  
  if (!hasAccess) {
    return fallback || null;
  }
  
  return <>{children}</>;
}

// 사용 예시
function VoteCard({ vote }: { vote: Vote }) {
  return (
    <div>
      <h3>{vote.title}</h3>
      
      <ProtectedComponent 
        permission="canManageVotes" 
        labId={vote.labId}
        resourceOwnerId={vote.creatorId}
      >
        <VoteManagementButtons voteId={vote.id} />
      </ProtectedComponent>
      
      <ProtectedComponent 
        permission="canViewVotes" 
        labId={vote.labId}
        fallback={<div>권한이 없습니다.</div>}
      >
        <VoteResults voteId={vote.id} />
      </ProtectedComponent>
    </div>
  );
}
```

### ⚠️ 프론트엔드 보안 주의사항

#### 1. 클라이언트 권한 체크의 한계
```typescript
// ⚠️ 클라이언트 권한 체크는 UI 편의성을 위한 것
// 실제 보안은 서버에서 담당
function SecurityWarning() {
  /*
   * 주의사항:
   * 1. 프론트엔드 권한 체크는 UX 개선용일 뿐
   * 2. 실제 보안은 백엔드 API에서 처리
   * 3. 민감한 데이터는 항상 서버에서 필터링
   * 4. API 호출 시 항상 권한 재검증 필요
   */
}
```

#### 2. 동적 권한 업데이트
```typescript
// 사용자 역할이 변경되었을 때 권한 갱신
function useAuthUpdates() {
  const [user, setUser] = useState<User | null>(null);
  
  useEffect(() => {
    const handleRoleChange = (updatedUser: User) => {
      setUser(updatedUser);
      // 권한 관련 캐시 무효화
      invalidatePermissionCache();
    };
    
    // WebSocket이나 polling을 통한 실시간 업데이트
    subscribeToUserUpdates(handleRoleChange);
  }, []);
}
```

---

## 랭킹 시스템

### 🏆 랭킹 시스템 구조

랭킹 시스템은 **점수 제출**과 **랭킹 계산**으로 구성되어 있습니다.

#### API 엔드포인트
- `GET /api/rankings/labs` - 전체 랩실 랭킹 (페이징)
- `GET /api/rankings/labs/{labId}` - 특정 랩실 랭킹
- `GET /api/rankings/labs/{labId}/contributors` - 랩실 기여자 목록
- `POST /api/labs/{labId}/scores` - 점수 제출
- `GET /api/labs/{labId}/scores` - 점수 제출 내역

#### 점수 카테고리
```typescript
enum ScoreCategory {
  COMPETITION = "COMPETITION",       // 대회 참가/수상
  PROJECT = "PROJECT",              // 프로젝트 완성
  STUDY = "STUDY",                 // 스터디 참여
  PRESENTATION = "PRESENTATION",    // 발표
  RESEARCH = "RESEARCH",           // 연구 활동
  MENTORING = "MENTORING",         // 멘토링
  OTHER = "OTHER"                  // 기타
}
```

---

## 데이터 내보내기 및 통계

### 📊 통계 시스템

#### API 엔드포인트
- `GET /api/labs/{labId}/statistics/comprehensive` - 종합 통계
- `GET /api/labs/{labId}/attendance/export` - 출석 데이터 내보내기
- `GET /api/labs/{labId}/members/activity` - 멤버 활동 통계

#### 통계 데이터 구조
```typescript
interface LabComprehensiveStats {
  basicInfo: {
    memberCount: number;
    averageAttendanceRate: number;
    totalScore: number;
    ranking: number;
  };
  attendanceStats: {
    totalSessions: number;
    averageParticipation: number;
    monthlyTrends: MonthlyAttendance[];
  };
  activityStats: {
    totalVotes: number;
    totalProjects: number;
    memberContributions: UserContribution[];
  };
}
```

---

## API Request/Response 상세 예시

### 🔐 인증 관련 API

#### 회원가입 API
```http
POST /api/auth/signup
Content-Type: application/json

{
  "name": "홍길동",
  "email": "hong@example.com",
  "password": "securePassword123",
  "studentNumber": "20230001",
  "phoneNumber": "010-1234-5678",
  "grade": 3,
  "enrollmentStatus": "ENROLLED"
}
```

**성공 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "studentNumber": "20230001",
    "phoneNumber": "010-1234-5678",
    "grade": 3,
    "enrollmentStatus": "ENROLLED",
    "role": "STUDENT",
    "labId": null,
    "createdAt": "2024-01-15T10:30:00Z"
  },
  "message": "회원가입 성공",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### 로그인 API
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "hong@example.com",
  "password": "securePassword123"
}
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
    "user": {
      "id": 1,
      "name": "홍길동",
      "email": "hong@example.com",
      "role": "STUDENT",
      "labId": null
    }
  },
  "message": "로그인 성공",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### 내 정보 조회 API
```http
GET /api/users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "studentNumber": "20230001",
    "phoneNumber": "010-1234-5678",
    "grade": 3,
    "enrollmentStatus": "ENROLLED",
    "role": "LAB_MEMBER",
    "labId": 5,
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-20T14:15:00Z"
  },
  "message": "사용자 정보 조회 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

### 🏢 랩실 관리 API

#### 랩실 목록 조회 API
```http
GET /api/labs?page=0&size=10&sort=ranking,desc
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "AI Lab",
      "category": "AI_ML",
      "description": "인공지능과 머신러닝을 연구하는 랩실입니다.",
      "professorName": "김교수",
      "ranking": 850,
      "memberCount": 12,
      "averageScore": 78.5,
      "images": [
        {
          "id": 1,
          "imageType": "MAIN",
          "imageUrl": "/uploads/labs/1/main.jpg",
          "description": "랩실 전경"
        }
      ],
      "createdAt": "2024-01-01T00:00:00Z"
    },
    {
      "id": 2,
      "name": "Web Lab",
      "category": "WEB",
      "description": "웹 개발 전문 랩실입니다.",
      "professorName": "이교수",
      "ranking": 720,
      "memberCount": 8,
      "averageScore": 72.3,
      "images": [],
      "createdAt": "2024-01-02T00:00:00Z"
    }
  ],
  "message": "랩실 목록 조회 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 랩실 상세 조회 API
```http
GET /api/labs/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "AI Lab",
    "category": "AI_ML",
    "description": "인공지능과 머신러닝을 연구하는 랩실입니다. 최신 딥러닝 기술을 활용한 프로젝트를 진행하며, 학생들의 연구 역량 향상을 목표로 합니다.",
    "professorName": "김교수",
    "ranking": 850,
    "memberCount": 12,
    "averageScore": 78.5,
    "totalProjects": 15,
    "activeVotes": 2,
    "recentActivities": [
      {
        "type": "PROJECT_COMPLETION",
        "title": "CNN 이미지 분류 프로젝트 완료",
        "date": "2024-01-18T16:00:00Z"
      }
    ],
    "images": [
      {
        "id": 1,
        "imageType": "MAIN",
        "imageUrl": "/uploads/labs/1/main.jpg",
        "description": "랩실 전경"
      },
      {
        "id": 2,
        "imageType": "EQUIPMENT",
        "imageUrl": "/uploads/labs/1/equipment.jpg",
        "description": "GPU 서버"
      }
    ],
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-18T16:00:00Z"
  },
  "message": "랩실 정보 조회 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 랩실 지원서 작성 API (슬롯 기반)
```http
POST /api/labs/1/applications/slot-based
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "slotId": 5
}
```

**성공 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "labId": 1,
    "userId": 1,
    "userName": "홍길동",
    "userEmail": "hong@example.com",
    "interviewSlot": {
      "id": 5,
      "startTime": "2024-01-25T14:00:00Z",
      "endTime": "2024-01-25T14:30:00Z",
      "maxApplicants": 3,
      "currentApplicants": 1
    },
    "status": "PENDING",
    "createdAt": "2024-01-20T14:15:00Z"
  },
  "message": "가입 신청 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

### 🎯 면접 시스템 API

#### 면접 생성 API
```http
POST /api/labs/1/interviews
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "startDate": "2024-01-25",
  "endDate": "2024-01-27",
  "durationMinutes": 30,
  "maxApplicantsPerSlot": 3
}
```

**성공 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "labId": 1,
    "startDate": "2024-01-25",
    "endDate": "2024-01-27",
    "durationMinutes": 30,
    "maxApplicantsPerSlot": 3,
    "status": "INACTIVE",
    "totalSlots": 0,
    "availableSlots": 0,
    "createdAt": "2024-01-20T14:15:00Z"
  },
  "message": "면접 생성 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 면접 슬롯 일괄 생성 API
```http
POST /api/labs/1/interviews/10/slots/batch
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "slots": [
    {
      "startTime": "2024-01-25T09:00:00",
      "endTime": "2024-01-25T09:30:00",
      "maxApplicants": 3
    },
    {
      "startTime": "2024-01-25T09:30:00",
      "endTime": "2024-01-25T10:00:00",
      "maxApplicants": 3
    },
    {
      "startTime": "2024-01-25T10:00:00",
      "endTime": "2024-01-25T10:30:00",
      "maxApplicants": 3
    }
  ]
}
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 25,
      "interviewId": 10,
      "startTime": "2024-01-25T09:00:00Z",
      "endTime": "2024-01-25T09:30:00Z",
      "maxApplicants": 3,
      "currentApplicants": 0,
      "status": "AVAILABLE"
    },
    {
      "id": 26,
      "interviewId": 10,
      "startTime": "2024-01-25T09:30:00Z",
      "endTime": "2024-01-25T10:00:00Z",
      "maxApplicants": 3,
      "currentApplicants": 0,
      "status": "AVAILABLE"
    },
    {
      "id": 27,
      "interviewId": 10,
      "startTime": "2024-01-25T10:00:00Z",
      "endTime": "2024-01-25T10:30:00Z",
      "maxApplicants": 3,
      "currentApplicants": 0,
      "status": "AVAILABLE"
    }
  ],
  "message": "면접 슬롯 일괄 생성 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 예약 가능한 슬롯 조회 API
```http
GET /api/labs/1/interviews/10/slots/available
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 25,
      "interviewId": 10,
      "startTime": "2024-01-25T09:00:00Z",
      "endTime": "2024-01-25T09:30:00Z",
      "maxApplicants": 3,
      "currentApplicants": 0,
      "status": "AVAILABLE",
      "isAvailable": true
    },
    {
      "id": 26,
      "interviewId": 10,
      "startTime": "2024-01-25T09:30:00Z",
      "endTime": "2024-01-25T10:00:00Z",
      "maxApplicants": 3,
      "currentApplicants": 2,
      "status": "RESERVED",
      "isAvailable": true
    }
  ],
  "message": "예약 가능한 슬롯 조회 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

### 📱 QR 출석 시스템 API

#### 출석 세션 생성 API
```http
POST /api/labs/1/attendance/sessions
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "1월 3주차 정기 모임",
  "qrValidityMinutes": 10
}
```

**성공 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 45,
    "labId": 1,
    "title": "1월 3주차 정기 모임",
    "status": "ACTIVE",
    "qrValidityMinutes": 10,
    "creatorId": 3,
    "creatorName": "김매니저",
    "totalMembers": 12,
    "attendedMembers": 0,
    "createdAt": "2024-01-20T14:15:00Z"
  },
  "message": "출석 세션이 생성되었습니다",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### QR 코드 생성 API
```http
POST /api/labs/1/attendance/sessions/45/qr
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "ATT_45_1642684500_abc123def456",
    "qrCodeBase64": "iVBORw0KGgoAAAANSUhEUgAAASwAAAEsCAYAAAB5fY51AAAVR...",
    "expiresAt": "2024-01-20T14:25:00Z",
    "sessionId": 45,
    "sessionTitle": "1월 3주차 정기 모임"
  },
  "message": "QR 코드가 생성되었습니다",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 출석 체크 API
```http
POST /api/labs/1/attendance/sessions/check
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "qrToken": "ATT_45_1642684500_abc123def456"
}
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "sessionId": 45,
    "userId": 1,
    "userName": "홍길동",
    "status": "PRESENT",
    "checkedAt": "2024-01-20T14:18:30Z",
    "sessionTitle": "1월 3주차 정기 모임"
  },
  "message": "출석 체크가 완료되었습니다",
  "timestamp": "2024-01-20T14:18:30Z"
}
```

### 🗳️ 투표 시스템 API

#### 투표 생성 API
```http
POST /api/labs/1/votes
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "랩실 정기 모임 시간 투표",
  "description": "매주 진행할 정기 모임의 최적 시간을 선택해 주세요.",
  "deadline": "2024-01-25T23:59:59",
  "optionTexts": [
    "화요일 오후 6시",
    "수요일 오후 7시",
    "목요일 오후 6시",
    "금요일 오후 5시"
  ]
}
```

**성공 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 8,
    "title": "랩실 정기 모임 시간 투표",
    "description": "매주 진행할 정기 모임의 최적 시간을 선택해 주세요.",
    "status": "ACTIVE",
    "deadline": "2024-01-25T23:59:59Z",
    "totalVotes": 0,
    "creatorId": 3,
    "creatorName": "김매니저",
    "labId": 1,
    "options": [
      {
        "id": 25,
        "text": "화요일 오후 6시",
        "voteCount": 0,
        "percentage": 0.0,
        "order": 1
      },
      {
        "id": 26,
        "text": "수요일 오후 7시",
        "voteCount": 0,
        "percentage": 0.0,
        "order": 2
      },
      {
        "id": 27,
        "text": "목요일 오후 6시",
        "voteCount": 0,
        "percentage": 0.0,
        "order": 3
      },
      {
        "id": 28,
        "text": "금요일 오후 5시",
        "voteCount": 0,
        "percentage": 0.0,
        "order": 4
      }
    ],
    "canParticipate": true,
    "hasParticipated": false,
    "createdAt": "2024-01-20T14:15:00Z"
  },
  "message": "투표 생성 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 투표 참여 API
```http
POST /api/labs/1/votes/8/participate
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "selectedOptionId": 26
}
```

**성공 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 45,
    "voteId": 8,
    "userId": 1,
    "userName": "홍길동",
    "optionId": 26,
    "selectedOptionText": "수요일 오후 7시",
    "participatedAt": "2024-01-20T14:20:00Z"
  },
  "message": "투표 참여 성공",
  "timestamp": "2024-01-20T14:20:00Z"
}
```

#### 투표 결과 조회 API
```http
GET /api/labs/1/votes/8
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 8,
    "title": "랩실 정기 모임 시간 투표",
    "description": "매주 진행할 정기 모임의 최적 시간을 선택해 주세요.",
    "status": "ACTIVE",
    "deadline": "2024-01-25T23:59:59Z",
    "totalVotes": 8,
    "creatorId": 3,
    "creatorName": "김매니저",
    "labId": 1,
    "options": [
      {
        "id": 25,
        "text": "화요일 오후 6시",
        "voteCount": 1,
        "percentage": 12.5,
        "order": 1
      },
      {
        "id": 26,
        "text": "수요일 오후 7시",
        "voteCount": 4,
        "percentage": 50.0,
        "order": 2
      },
      {
        "id": 27,
        "text": "목요일 오후 6시",
        "voteCount": 2,
        "percentage": 25.0,
        "order": 3
      },
      {
        "id": 28,
        "text": "금요일 오후 5시",
        "voteCount": 1,
        "percentage": 12.5,
        "order": 4
      }
    ],
    "canParticipate": false,
    "hasParticipated": true,
    "mySelectedOptionId": 26,
    "remainingTime": "5 days 9 hours",
    "createdAt": "2024-01-20T14:15:00Z",
    "updatedAt": "2024-01-20T14:20:00Z"
  },
  "message": "투표 조회 성공",
  "timestamp": "2024-01-20T14:25:00Z"
}
```

### 📅 캘린더 시스템 API

#### 일반 일정 생성 API
```http
POST /api/labs/1/calendar/schedules
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "프로젝트 발표회",
  "description": "1학기 프로젝트 최종 발표회입니다.",
  "eventDate": "2024-01-30"
}
```

**성공 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "type": "SCHEDULE",
    "title": "프로젝트 발표회",
    "description": "1학기 프로젝트 최종 발표회입니다.",
    "eventDate": "2024-01-30",
    "startTime": null,
    "endTime": null,
    "labId": 1,
    "interviewId": null,
    "createdAt": "2024-01-20T14:15:00Z"
  },
  "message": "일반 일정 생성 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 기간별 일정 조회 API
```http
GET /api/labs/1/calendar/schedules?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 15,
      "type": "SCHEDULE",
      "title": "프로젝트 발표회",
      "description": "1학기 프로젝트 최종 발표회입니다.",
      "eventDate": "2024-01-30",
      "startTime": null,
      "endTime": null,
      "labId": 1,
      "interviewId": null,
      "isAllDay": true,
      "createdAt": "2024-01-20T14:15:00Z"
    },
    {
      "id": 16,
      "type": "INTERVIEW",
      "title": "면접 - AI Lab",
      "description": "2024년 1월 신규 멤버 면접",
      "eventDate": "2024-01-25",
      "startTime": "09:00:00",
      "endTime": "10:30:00",
      "labId": 1,
      "interviewId": 10,
      "isAllDay": false,
      "createdAt": "2024-01-20T14:15:00Z"
    }
  ],
  "message": "일반 일정 목록 조회 성공",
  "timestamp": "2024-01-20T14:25:00Z"
}
```

### 🏆 랭킹 시스템 API

#### 점수 제출 API
```http
POST /api/labs/1/scores
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "AI 해커톤 우승",
  "description": "2024년 전국 AI 해커톤에서 1등을 차지했습니다.",
  "category": "COMPETITION",
  "score": 90,
  "visibilityLevel": "PUBLIC",
  "evidenceUrl": "https://example.com/certificate.pdf"
}
```

**성공 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 67,
    "title": "AI 해커톤 우승",
    "description": "2024년 전국 AI 해커톤에서 1등을 차지했습니다.",
    "category": "COMPETITION",
    "score": 90,
    "visibilityLevel": "PUBLIC",
    "status": "APPROVED",
    "evidenceUrl": "https://example.com/certificate.pdf",
    "userId": 1,
    "userName": "홍길동",
    "labId": 1,
    "labName": "AI Lab",
    "submittedAt": "2024-01-20T14:15:00Z",
    "approvedAt": "2024-01-20T14:15:00Z"
  },
  "message": "점수 제출 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 랩실 랭킹 조회 API
```http
GET /api/rankings/labs?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "rank": 1,
        "labId": 1,
        "labName": "AI Lab",
        "labCategory": "AI_ML",
        "totalScore": 1250,
        "memberCount": 12,
        "averageScore": 104.2,
        "professorName": "김교수",
        "recentActivities": 8,
        "scoreChange": "+50"
      },
      {
        "rank": 2,
        "labId": 3,
        "labName": "Security Lab",
        "labCategory": "SECURITY",
        "totalScore": 1180,
        "memberCount": 10,
        "averageScore": 118.0,
        "professorName": "박교수",
        "recentActivities": 5,
        "scoreChange": "+20"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "direction": "DESC",
        "orderBy": "totalScore"
      },
      "offset": 0,
      "pageSize": 10,
      "pageNumber": 0
    },
    "totalElements": 25,
    "totalPages": 3,
    "last": false,
    "size": 10,
    "number": 0,
    "first": true,
    "numberOfElements": 10
  },
  "message": "랩실 랭킹 조회 성공",
  "timestamp": "2024-01-20T14:25:00Z"
}
```

---

## 에러 처리 및 상태 코드

### 📋 HTTP 상태 코드별 응답

#### 400 Bad Request - 입력값 검증 실패
```json
{
  "success": false,
  "message": "입력값 검증에 실패했습니다",
  "errorCode": "VALIDATION_FAILED",
  "details": {
    "field": "email",
    "rejectedValue": "invalid-email",
    "message": "올바른 이메일 형식이 아닙니다"
  },
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 401 Unauthorized - 인증 실패
```json
{
  "success": false,
  "message": "인증이 필요합니다",
  "errorCode": "UNAUTHORIZED",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 403 Forbidden - 권한 없음
```json
{
  "success": false,
  "message": "해당 작업을 수행할 권한이 없습니다",
  "errorCode": "PERMISSION_DENIED",
  "details": {
    "requiredRole": "LAB_MANAGER",
    "currentRole": "LAB_MEMBER",
    "resource": "VOTE_MANAGEMENT"
  },
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 404 Not Found - 리소스 없음
```json
{
  "success": false,
  "message": "요청한 리소스를 찾을 수 없습니다",
  "errorCode": "RESOURCE_NOT_FOUND",
  "details": {
    "resourceType": "Lab",
    "resourceId": 999
  },
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 409 Conflict - 비즈니스 로직 충돌
```json
{
  "success": false,
  "message": "이미 해당 랩실에 지원하셨습니다",
  "errorCode": "DUPLICATE_APPLICATION",
  "details": {
    "labId": 1,
    "applicationId": 15,
    "submittedAt": "2024-01-15T10:30:00Z"
  },
  "timestamp": "2024-01-20T14:15:00Z"
}
```

#### 422 Unprocessable Entity - 비즈니스 규칙 위반
```json
{
  "success": false,
  "message": "면접 기간이 아닙니다",
  "errorCode": "INTERVIEW_PERIOD_INVALID",
  "details": {
    "interviewStartDate": "2024-01-25",
    "interviewEndDate": "2024-01-27",
    "currentDate": "2024-01-20"
  },
  "timestamp": "2024-01-20T14:15:00Z"
}
```

### 🚨 프론트엔드 에러 처리 가이드

#### 공통 에러 처리 함수
```typescript
interface ApiError {
  success: false;
  message: string;
  errorCode?: string;
  details?: any;
  timestamp: string;
}

function handleApiError(error: ApiError, context?: string) {
  switch (error.errorCode) {
    case 'UNAUTHORIZED':
      // 토큰 만료 또는 인증 실패
      localStorage.removeItem('authToken');
      window.location.href = '/login';
      break;
      
    case 'PERMISSION_DENIED':
      // 권한 없음
      showNotification('권한이 없습니다', 'error');
      break;
      
    case 'VALIDATION_FAILED':
      // 입력값 검증 실패
      if (error.details?.field) {
        showFieldError(error.details.field, error.details.message);
      } else {
        showNotification(error.message, 'warning');
      }
      break;
      
    case 'RESOURCE_NOT_FOUND':
      // 리소스 없음
      showNotification('요청한 데이터를 찾을 수 없습니다', 'error');
      break;
      
    case 'DUPLICATE_APPLICATION':
      // 중복 지원
      showNotification('이미 지원한 랩실입니다', 'warning');
      break;
      
    case 'INTERVIEW_PERIOD_INVALID':
      // 면접 기간 아님
      showNotification('현재 면접 기간이 아닙니다', 'info');
      break;
      
    default:
      // 일반 에러
      showNotification(error.message || '알 수 없는 오류가 발생했습니다', 'error');
  }
}
```

#### Axios 인터셉터 설정
```typescript
import axios from 'axios';

// 요청 인터셉터
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.data) {
      handleApiError(error.response.data);
    } else if (error.request) {
      showNotification('네트워크 오류가 발생했습니다', 'error');
    } else {
      showNotification('예상치 못한 오류가 발생했습니다', 'error');
    }
    return Promise.reject(error);
  }
);
```

---

## 페이징 및 필터링

### 📄 페이징 파라미터

#### 표준 페이징 요청
```http
GET /api/labs?page=0&size=10&sort=ranking,desc
GET /api/labs/1/votes?page=0&size=20&sort=createdAt,desc
GET /api/rankings/labs?page=1&size=5&sort=totalScore,desc
```

#### 페이징 파라미터 설명
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지당 항목 수 (기본값: 20, 최대값: 100)
- `sort`: 정렬 기준 (`필드명,방향` 형식)
  - 방향: `asc` (오름차순), `desc` (내림차순)
  - 예: `createdAt,desc`, `name,asc`, `ranking,desc`

#### 페이징 응답 형식
```json
{
  "success": true,
  "data": {
    "content": [
      // 실제 데이터 배열
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "direction": "DESC",
        "orderBy": "ranking"
      },
      "offset": 0,
      "pageSize": 10,
      "pageNumber": 0
    },
    "totalElements": 45,
    "totalPages": 5,
    "last": false,
    "size": 10,
    "number": 0,
    "first": true,
    "numberOfElements": 10,
    "empty": false
  },
  "message": "조회 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

### 🔍 필터링 및 검색

#### 랩실 목록 필터링
```http
GET /api/labs?category=AI_ML&professorName=김교수&minRanking=500
GET /api/labs?search=인공지능&category=AI_ML,WEB&sort=ranking,desc
```

#### 투표 목록 필터링
```http
GET /api/labs/1/votes?status=ACTIVE&createdAfter=2024-01-01&createdBefore=2024-01-31
```

#### 출석 기록 필터링
```http
GET /api/labs/1/attendance/records?status=PRESENT&startDate=2024-01-01&endDate=2024-01-31&userId=1
```

### 📱 무한 스크롤 구현 예시

```typescript
interface PaginatedData<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  number: number;
  size: number;
}

function useInfiniteScroll<T>(
  fetchFunction: (page: number, size: number) => Promise<PaginatedData<T>>,
  pageSize: number = 20
) {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [page, setPage] = useState(0);

  const loadMore = useCallback(async () => {
    if (loading || !hasMore) return;

    setLoading(true);
    try {
      const response = await fetchFunction(page, pageSize);
      
      setData(prev => [...prev, ...response.content]);
      setHasMore(!response.last);
      setPage(prev => prev + 1);
    } catch (error) {
      console.error('데이터 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  }, [fetchFunction, page, pageSize, loading, hasMore]);

  const reset = useCallback(() => {
    setData([]);
    setPage(0);
    setHasMore(true);
  }, []);

  return { data, loading, hasMore, loadMore, reset };
}

// 사용 예시
function LabList() {
  const { data: labs, loading, hasMore, loadMore } = useInfiniteScroll(
    (page, size) => api.getLabs({ page, size, sort: 'ranking,desc' }),
    10
  );

  return (
    <InfiniteScroll
      dataLength={labs.length}
      next={loadMore}
      hasMore={hasMore}
      loader={<div>로딩 중...</div>}
    >
      {labs.map(lab => (
        <LabCard key={lab.id} lab={lab} />
      ))}
    </InfiniteScroll>
  );
}
```

---

## 파일 업로드 및 미디어 처리

### 📁 파일 업로드 API

#### 랩실 이미지 업로드
```http
POST /api/labs/1/images
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="lab-main.jpg"
Content-Type: image/jpeg

[바이너리 이미지 데이터]
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="imageType"

MAIN
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="description"

랩실 전경 사진
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

**성공 응답 (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "labId": 1,
    "imageType": "MAIN",
    "imageUrl": "/uploads/labs/1/images/15_lab-main.jpg",
    "originalFileName": "lab-main.jpg",
    "fileSize": 2457600,
    "description": "랩실 전경 사진",
    "uploadedAt": "2024-01-20T14:15:00Z"
  },
  "message": "이미지 업로드 성공",
  "timestamp": "2024-01-20T14:15:00Z"
}
```

### 🎯 QR 코드 처리

#### QR 코드 이미지 표시
```typescript
// Base64 QR 코드 이미지 처리
interface QRCodeData {
  token: string;
  qrCodeBase64: string;
  expiresAt: string;
}

function QRCodeDisplay({ qrData }: { qrData: QRCodeData }) {
  const qrImageSrc = `data:image/png;base64,${qrData.qrCodeBase64}`;
  
  return (
    <div className="qr-code-container">
      <img 
        src={qrImageSrc} 
        alt="출석 체크 QR 코드"
        className="qr-code-image"
      />
      <p>만료시간: {new Date(qrData.expiresAt).toLocaleString()}</p>
    </div>
  );
}
```

### 📱 파일 업로드 컴포넌트 예시

```typescript
interface FileUploadProps {
  accept?: string;
  maxSize?: number; // bytes
  onUpload: (file: File) => Promise<void>;
  loading?: boolean;
}

function FileUpload({ accept = "image/*", maxSize = 5 * 1024 * 1024, onUpload, loading }: FileUploadProps) {
  const [dragActive, setDragActive] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const validateFile = (file: File): boolean => {
    if (file.size > maxSize) {
      setError(`파일 크기는 ${maxSize / 1024 / 1024}MB를 초과할 수 없습니다`);
      return false;
    }
    
    if (accept && !file.type.match(accept.replace('*', '.*'))) {
      setError('지원하지 않는 파일 형식입니다');
      return false;
    }
    
    setError(null);
    return true;
  };

  const handleFiles = async (files: FileList | null) => {
    if (!files || files.length === 0) return;
    
    const file = files[0];
    if (!validateFile(file)) return;
    
    try {
      await onUpload(file);
    } catch (error) {
      setError('파일 업로드에 실패했습니다');
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragActive(false);
    handleFiles(e.dataTransfer.files);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    handleFiles(e.target.files);
  };

  return (
    <div 
      className={`file-upload ${dragActive ? 'drag-active' : ''}`}
      onDragEnter={() => setDragActive(true)}
      onDragLeave={() => setDragActive(false)}
      onDragOver={(e) => e.preventDefault()}
      onDrop={handleDrop}
    >
      <input
        type="file"
        accept={accept}
        onChange={handleChange}
        disabled={loading}
        style={{ display: 'none' }}
        id="file-input"
      />
      
      <label htmlFor="file-input" className="file-upload-label">
        {loading ? (
          <div>업로드 중...</div>
        ) : (
          <div>
            <div>파일을 드래그하거나 클릭하여 선택하세요</div>
            <div className="file-info">
              최대 {maxSize / 1024 / 1024}MB, {accept}
            </div>
          </div>
        )}
      </label>
      
      {error && <div className="error-message">{error}</div>}
    </div>
  );
}

// 사용 예시
function LabImageUpload({ labId }: { labId: number }) {
  const [uploading, setUploading] = useState(false);

  const handleUpload = async (file: File) => {
    setUploading(true);
    
    const formData = new FormData();
    formData.append('file', file);
    formData.append('imageType', 'MAIN');
    formData.append('description', '랩실 이미지');

    try {
      const response = await api.uploadLabImage(labId, formData);
      showNotification('이미지 업로드 성공', 'success');
      // 이미지 목록 새로고침
      refreshImageList();
    } catch (error) {
      handleApiError(error);
    } finally {
      setUploading(false);
    }
  };

  return (
    <FileUpload
      accept="image/*"
      maxSize={5 * 1024 * 1024}
      onUpload={handleUpload}
      loading={uploading}
    />
  );
}
```

### 📊 파일 다운로드 처리

#### Excel 데이터 내보내기
```typescript
async function downloadExcelData(labId: number, type: 'attendance' | 'scores' | 'members') {
  try {
    const response = await fetch(`/api/labs/${labId}/export/${type}`, {
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`,
      },
    });

    if (!response.ok) {
      throw new Error('다운로드 실패');
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    // Content-Disposition 헤더에서 파일명 추출
    const contentDisposition = response.headers.get('Content-Disposition');
    const filename = contentDisposition
      ? contentDisposition.split('filename=')[1]?.replace(/"/g, '')
      : `${type}_${labId}_${new Date().toISOString().slice(0, 10)}.xlsx`;
    
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    showNotification('다운로드 완료', 'success');
  } catch (error) {
    showNotification('다운로드 실패', 'error');
  }
}
```

