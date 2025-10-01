# Rankus API 문서 📚

> 프론트엔드 개발자를 위한 포괄적인 API 가이드

## 🌟 개요

Rankus는 대학 연구실 정보 플랫폼으로, JWT 기반 인증을 사용하는 RESTful API를 제공합니다.

### 기본 정보
- **Base URL**: `http://localhost:8080`
- **API 문서**: [Swagger UI](http://localhost:8080/swagger-ui.html)
- **인증 방식**: JWT Bearer Token
- **응답 형식**: JSON (표준 ApiResponse 래퍼)

## 🔐 인증 및 권한 관리

### JWT 인증 플로우 - **Phase 1 보안 강화** ✨
1. **로그인 (v2)** → 액세스 토큰 + 리프레시 토큰 발급
2. **API 호출**: `Authorization: Bearer {accessToken}`
3. **자동 갱신**: 액세스 토큰 만료 시 리프레시 토큰으로 갱신
4. **로그아웃**: 토큰 블랙리스트로 즉시 무효화

**토큰 수명**:
- 액세스 토큰: 15분 (보안 강화)
- 리프레시 토큰: 7일 (사용자 편의성)

### 사용자 권한 레벨
- **STUDENT**: 일반 학생 (기본 권한)
- **PROFESSOR**: 교수 (랩실 관리 권한)
- **ADMIN**: 관리자 (전체 시스템 관리)

## 📋 표준 응답 구조

모든 API는 일관된 응답 구조를 사용합니다:

```json
{
  "success": true,
  "message": "성공 메시지",
  "data": {
    // 실제 응답 데이터
  }
}
```

### 에러 응답 구조
```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null
}
```

## 🔑 Authentication API

### 회원가입
```http
POST /api/auth/signup
Content-Type: application/json

{
  "studentId": "20230001",
  "email": "student@univ.ac.kr",
  "password": "password123",
  "name": "홍길동",
  "phone": "010-1234-5678"
}
```

**응답 예시**:
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "id": 1,
    "studentId": "20230001",
    "email": "student@univ.ac.kr",
    "name": "홍길동",
    "role": "STUDENT"
  }
}
```

### 로그인
```http
POST /api/auth/signin
Content-Type: application/json

{
  "email": "student@univ.ac.kr",
  "password": "password123"
}
```

**응답 예시**:
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "studentId": "20230001",
      "email": "student@univ.ac.kr",
      "name": "홍길동",
      "role": "STUDENT"
    }
  }
}
```

### 새로운 로그인 (v2) - **Phase 1 보안 강화** ✨
> 리프레시 토큰을 포함한 향상된 로그인 시스템

```http
POST /api/auth/login/v2
Content-Type: application/json

{
  "email": "student@univ.ac.kr",
  "password": "password123"
}
```

**응답 예시**:
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "studentId": "20230001",
      "email": "student@univ.ac.kr",
      "name": "홍길동",
      "role": "STUDENT"
    },
    "expiresAt": "2024-01-15T10:15:00",
    "refreshExpiresAt": "2024-01-22T10:00:00"
  }
}
```

### 토큰 갱신 - **Phase 1 보안 강화** ✨
> 액세스 토큰 만료 시 자동 갱신

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답 예시**:
```json
{
  "success": true,
  "message": "토큰 갱신 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": "2024-01-15T10:15:00"
  }
}
```

**오류 응답 예시**:
```json
{
  "success": false,
  "message": "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.",
  "data": null
}
```

### 로그아웃 - **Phase 1 보안 강화** ✨
> 토큰 무효화를 통한 안전한 로그아웃

```http
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

**응답 예시**:
```json
{
  "success": true,
  "message": "로그아웃되었습니다",
  "data": null
}
```

**🔒 Phase 1 보안 개선사항**:
- **액세스 토큰**: 15분 수명으로 보안 강화
- **리프레시 토큰**: 7일 수명으로 사용자 편의성 확보
- **토큰 블랙리스트**: 로그아웃 시 즉시 토큰 무효화
- **자동 갱신**: 토큰 만료 전 자동 갱신 지원

## 👤 User Management API

### 내 정보 조회
```http
GET /api/users/me
Authorization: Bearer {token}
```

### 사용자 검색
```http
GET /api/users/search?keyword=홍길동
Authorization: Bearer {token}
```

### 사용자 목록 조회 (관리자)
```http
GET /api/users?page=0&size=10&sortBy=createdAt&direction=DESC
Authorization: Bearer {token}
```

## 🧪 Lab Management API

### 랩실 목록 조회
```http
GET /api/labs?page=0&size=10&sortBy=createdAt&direction=DESC
```

**응답 예시**:
```json
{
  "success": true,
  "message": "랩실 목록 조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "인공지능 연구실",
        "description": "AI 및 머신러닝 연구",
        "professorName": "김교수",
        "maxMembers": 10,
        "currentMembers": 7,
        "imageUrl": "/uploads/lab1.jpg"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 25,
    "totalPages": 3
  }
}
```

### 랩실 상세 정보
```http
GET /api/labs/{labId}
```

### 랩실 생성 요청
```http
POST /api/labs/requests
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "새로운 연구실",
  "description": "연구실 설명",
  "researchArea": "연구 분야"
}
```

### 랩실 생성 요청 승인/거부 (관리자)
```http
POST /api/labs/requests/{requestId}/approve
Authorization: Bearer {token}
```

```http
POST /api/labs/requests/{requestId}/reject
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "거부 사유"
}
```

## 📝 Lab Application API

### 랩실 지원하기
```http
POST /api/labs/{labId}/applications
Authorization: Bearer {token}
Content-Type: application/json

{
  "motivation": "지원 동기",
  "experience": "관련 경험",
  "portfolio": "포트폴리오 URL"
}
```

### 내 지원 내역 조회
```http
GET /api/labs/applications/my
Authorization: Bearer {token}
```

### 지원자 목록 조회 (랩실 관리자)
```http
GET /api/labs/{labId}/applications
Authorization: Bearer {token}
```

### 지원 승인/거부
```http
POST /api/labs/{labId}/applications/{applicationId}/approve
Authorization: Bearer {token}
```

```http
POST /api/labs/{labId}/applications/{applicationId}/reject
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "거부 사유"
}
```

## 📅 Interview System API

### 면접 슬롯 조회
```http
GET /api/interviews/slots?labId=1&date=2024-01-15
Authorization: Bearer {token}
```

### 면접 슬롯 생성 (랩실 관리자)
```http
POST /api/interviews/slots
Authorization: Bearer {token}
Content-Type: application/json

{
  "labId": 1,
  "interviewDate": "2024-01-15",
  "startTime": "09:00",
  "endTime": "09:30",
  "capacity": 1
}
```

### 면접 슬롯 예약
```http
POST /api/interviews/slots/{slotId}/book
Authorization: Bearer {token}
Content-Type: application/json

{
  "applicationId": 1
}
```

### 면접 슬롯 취소
```http
DELETE /api/interviews/slots/{slotId}/cancel
Authorization: Bearer {token}
```

### 면접 상태 변경 (RESTful) - **Phase 3 API 일관성** ✨
> REST 원칙에 따른 면접 상태 관리

```http
PATCH /api/labs/{labId}/interviews/{interviewId}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "ACTIVE"
}
```

**지원 가능한 상태**:
- `ACTIVE`: 면접 활성화
- `INACTIVE`: 면접 비활성화 
- `CLOSED`: 면접 종료

**응답 예시**:
```json
{
  "success": true,
  "message": "면접 상태가 변경되었습니다",
  "data": {
    "id": 1,
    "title": "1차 면접",
    "status": "ACTIVE",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

### 면접 슬롯 상태 변경 (RESTful) - **Phase 3 API 일관성** ✨
> REST 원칙에 따른 면접 슬롯 상태 관리

```http
PATCH /api/labs/{labId}/interviews/{interviewId}/slots/{slotId}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "CANCELLED"
}
```

**지원 가능한 상태**:
- `CANCELLED`: 슬롯 취소
- `AVAILABLE`: 슬롯 재활성화

**응답 예시**:
```json
{
  "success": true,
  "message": "면접 슬롯 상태가 변경되었습니다",
  "data": {
    "id": 1,
    "dateTime": "2024-01-15T10:00:00",
    "status": "AVAILABLE",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

## 📢 Notice Management API

### 공지사항 목록 조회
```http
GET /api/notices?labId=1&page=0&size=10
Authorization: Bearer {token}
```

### 공지사항 상세 조회
```http
GET /api/notices/{noticeId}
Authorization: Bearer {token}
```

### 공지사항 생성
```http
POST /api/notices
Authorization: Bearer {token}
Content-Type: application/json

{
  "labId": 1,
  "title": "공지사항 제목",
  "content": "공지사항 내용",
  "pinned": false
}
```

### 공지사항 수정
```http
PUT /api/notices/{noticeId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "pinned": true
}
```

### 공지사항 삭제
```http
DELETE /api/notices/{noticeId}
Authorization: Bearer {token}
```

## 🏆 Ranking System API

### 랭킹 조회
```http
GET /api/rankings?labId=1&page=0&size=10&sortBy=totalScore&direction=DESC
Authorization: Bearer {token}
```

**응답 예시**:
```json
{
  "success": true,
  "message": "랭킹 조회 성공",
  "data": {
    "content": [
      {
        "userId": 1,
        "userName": "홍길동",
        "totalScore": 95,
        "rank": 1,
        "labId": 1,
        "labName": "인공지능 연구실"
      }
    ],
    "totalElements": 15,
    "totalPages": 2
  }
}
```

### 점수 신청
```http
POST /api/score-submissions
Authorization: Bearer {token}
Content-Type: application/json

{
  "labId": 1,
  "activityType": "RESEARCH",
  "description": "논문 발표",
  "score": 10,
  "evidenceFileId": 1
}
```

### 점수 신청 목록 조회
```http
GET /api/score-submissions?labId=1&status=PENDING
Authorization: Bearer {token}
```

### 점수 신청 승인/거부
```http
POST /api/score-submissions/{submissionId}/approve
Authorization: Bearer {token}
```

```http
POST /api/score-submissions/{submissionId}/reject
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "거부 사유"
}
```

### 점수 제출 상태 변경 (RESTful) - **Phase 3 API 일관성** ✨
> REST 원칙에 따른 점수 제출 상태 관리

```http
PATCH /api/score-submissions/{submissionId}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "APPROVED"
}
```

**지원 가능한 상태**:
- `APPROVED`: 승인
- `REJECTED`: 거부 (거부 사유 필수)

**승인 요청 예시**:
```json
{
  "status": "APPROVED"
}
```

**거부 요청 예시**:
```json
{
  "status": "REJECTED",
  "rejectionReason": "제출된 자료가 기준에 미달됩니다"
}
```

**응답 예시**:
```json
{
  "success": true,
  "message": "점수 제출 상태가 변경되었습니다",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

## 📋 Attendance Management API

### 출석 세션 상태 변경 (RESTful) - **Phase 3 API 일관성** ✨
> REST 원칙에 따른 출석 세션 상태 관리

```http
PATCH /api/labs/{labId}/attendance/sessions/{sessionId}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "COMPLETED"
}
```

**지원 가능한 상태**:
- `COMPLETED`: 출석 세션 완료
- `CANCELLED`: 출석 세션 취소

**완료 요청 예시**:
```json
{
  "status": "COMPLETED"
}
```

**취소 요청 예시**:
```json
{
  "status": "CANCELLED"
}
```

**응답 예시**:
```json
{
  "success": true,
  "message": "출석 세션 상태가 변경되었습니다",
  "data": {
    "id": 1,
    "title": "연구실 세미나",
    "status": "COMPLETED",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

**🔄 Phase 3 RESTful 개선사항**:
- **통일된 상태 변경**: 모든 상태 변경 작업을 PATCH 메서드로 표준화
- **명확한 의미론**: POST(생성) vs PATCH(상태변경)의 의미론적 구분
- **백워드 호환성**: 기존 POST 엔드포인트와 병행 운영 가능

## 🔐 Secure QR 기반 출석 플로우 (NEW)
> Phase 3 - Secure QR 도입 및 글로벌 출석 체크 흐름

### 개요
기존 단순 문자열 QR(labId-sessionId-timestamp) 방식에서 AES-GCM 암호화 기반 Secure QR 토큰으로 확장되었습니다.

### 전체 흐름
1. 관리자/교수/권한자 세션 생성 → Secure QR 생성 API 호출
2. 서버가 암호화된 토큰(encryptedToken)과 출석 URL(attendanceUrl = https://rankus.vercel.app/attend?qt=TOKEN) 반환
3. 프론트에서 URL을 QR 이미지로 렌더링
4. 학생이 모바일로 스캔 → 웹 /attend?qt=TOKEN 페이지 진입
5. 프론트: GET /api/attendance/qr/resolve 로 토큰 메타/유효성 사전 검증
6. 로그인 미완료 시 로그인 → 완료 후 원래 페이지 복귀
7. 유효(valid=true)이면 POST /api/attendance/check 로 출석 처리
8. 처리 결과 UI 표시

### 장점
- 토큰 위변조 및 예측 방지 (AES-256-GCM + nonce)
- 만료/비활성 세션 사전 안내 (resolve 단계)
- 글로벌 엔드포인트 통일 (labId 경로 의존 제거)

### 1) Secure QR 생성
```http
POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr/secure
Authorization: Bearer {token}
```
**응답 예시**
```json
{
  "success": true,
  "message": "보안 QR 코드가 생성되었습니다",
  "data": {
    "encryptedToken": "AbCdEfGh...",
    "sessionId": 42,
    "generatedAt": "2025-10-02T12:55:10",
    "expiresAt": "2025-10-02T13:00:10",
    "isExpired": false,
    "attendanceUrl": "https://rankus.vercel.app/attend?qt=AbCdEfGh..."
  }
}
```

### 2) 토큰 해석 (공개, 인증 불필요)
```http
GET /api/attendance/qr/resolve?token=AbCdEfGh...
```
**성공(유효) 응답 예시**
```json
{
  "success": true,
  "message": "QR 토큰 해석 완료",
  "data": {
    "originalToken": "AbCdEfGh...",
    "type": "SECURE",
    "valid": true,
    "reason": "OK",
    "labId": 3,
    "sessionId": 42,
    "sessionTitle": "주간 세미나",
    "sessionStatus": "ACTIVE",
    "generatedAt": "2025-10-02T12:55:10",
    "expiresAt": "2025-10-02T13:00:10"
  }
}
```
**만료된 경우**
```json
{
  "success": true,
  "message": "QR 토큰 해석 완료",
  "data": {
    "originalToken": "AbCdEfGh...",
    "type": "SECURE",
    "valid": false,
    "reason": "EXPIRED",
    "message": "QR이 만료되었습니다",
    "sessionId": 42,
    "expiresAt": "2025-10-02T13:00:10"
  }
}
```
**비활성(종료/취소) 세션**
```json
{
  "success": true,
  "message": "QR 토큰 해석 완료",
  "data": {
    "originalToken": "AbCdEfGh...",
    "type": "SECURE",
    "valid": false,
    "reason": "SESSION_INACTIVE",
    "message": "활성화된 출석 세션이 아닙니다",
    "sessionStatus": "COMPLETED",
    "sessionId": 42
  }
}
```
**잘못된 토큰**
```json
{
  "success": true,
  "message": "QR 토큰 해석 완료",
  "data": {
    "originalToken": "xxx",
    "valid": false,
    "reason": "INVALID",
    "message": "유효하지 않은 QR 코드입니다"
  }
}
```

### 3) 글로벌 출석 체크 (인증 필요)
```http
POST /api/attendance/check
Authorization: Bearer {token}
Content-Type: application/json

{
  "token": "AbCdEfGh..."
}
```
**응답 예시**
```json
{
  "success": true,
  "message": "출석 체크가 완료되었습니다",
  "data": {
    "recordId": 310,
    "sessionId": 42,
    "userId": 77,
    "checkedAt": "2025-10-02T12:57:31"
  }
}
```
**이미 출석한 경우** (예시)
```json
{
  "success": false,
  "message": "이미 출석 체크되었습니다",
  "data": null
}
```

### 4) 에러 Reason 코드 (resolve 응답 data.reason)
| Reason | 의미 | 처리 가이드 |
|--------|------|-------------|
| OK | 유효 | 바로 출석 시도 |
| INVALID | 파싱/암호해독 실패 | 새 QR 요청 안내 |
| EXPIRED | 만료 | 세션 관리자에게 새 QR 요청 |
| SESSION_INACTIVE | 세션 종료/취소 | 결과 화면 표시 후 재시도 불가 |
| SESSION_NOT_FOUND | 세션 없음 | 잘못된/구버전 QR 가능성 안내 |

### 5) 레거시 엔드포인트 Deprecation
| 기존 | 상태 | 대체 |
|------|------|------|
| POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr | Deprecated | POST /api/labs/{labId}/attendance/sessions/{sessionId}/qr/secure |
| POST /api/labs/{labId}/attendance/sessions/check | Deprecated | POST /api/attendance/check |

> 레거시는 일시적 병행 운영 후 제거 예정. 프론트 신규 구현은 반드시 Secure & 글로벌 API 사용.

### 6) 프론트 구현 체크리스트
- URL 파라미터 qt 읽기 → resolve 호출
- valid=false 시 reason별 메시지 매핑
- valid=true & 로그인됨 → global check 호출
- 출석 성공 후: 세션 제목/시간 표시 & 상태 갱신

### 7) 보안 권장사항
- secret-key 환경변수로 주입 (QR_SECRET_KEY)
- 토큰 유효시간 10분 이하 유지
- 필요 시 Rate Limit: /api/attendance/qr/resolve IP 별 분당 제한

---

## ⚠️ 에러 코드 참조

### 인증 관련 에러
- **401 Unauthorized**: 인증 토큰이 없거나 유효하지 않음
- **403 Forbidden**: 권한이 없음

### 일반적인 에러
- **400 Bad Request**: 잘못된 요청 데이터
- **404 Not Found**: 리소스를 찾을 수 없음
- **409 Conflict**: 중복된 데이터 (예: 이미 지원한 랩실)
- **500 Internal Server Error**: 서버 내부 오류

### 비즈니스 로직 에러
- **DUPLICATE_APPLICATION**: 이미 지원한 랩실
- **INVALID_TIME_SLOT**: 유효하지 않은 시간대
- **INSUFFICIENT_PERMISSION**: 권한 부족
- **RESOURCE_NOT_FOUND**: 리소스 없음

## 🔧 개발 팁

### 1. 인증 헤더 설정
```javascript
const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
});
```

### 2. 페이지네이션 처리
```javascript
const fetchLabs = async (page = 0, size = 10) => {
  const response = await apiClient.get(`/api/labs?page=${page}&size=${size}`);
  return response.data.data;
};
```

### 3. 파일 업로드 처리
```javascript
const uploadFile = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await apiClient.post('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  
  return response.data.data;
};
```

### 4. 에러 처리
```javascript
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // 토큰 만료 시 로그인 페이지로 리다이렉트
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

## 📱 사용자 플로우 예시

### 랩실 지원 플로우
1. **랩실 목록 조회** → `GET /api/labs`
2. **랩실 상세 정보** → `GET /api/labs/{labId}`
3. **지원서 작성** → `POST /api/labs/{labId}/applications`
4. **지원 상태 확인** → `GET /api/labs/applications/my`

### 면접 예약 플로우
1. **면접 슬롯 조회** → `GET /api/interviews/slots`
2. **슬롯 예약** → `POST /api/interviews/slots/{slotId}/book`
3. **예약 확인** → `GET /api/interviews/my-bookings`

### 점수 신청 플로우
1. **증빙 파일 업로드** → `POST /api/files/upload`
2. **점수 신청** → `POST /api/score-submissions`
3. **신청 상태 확인** → `GET /api/score-submissions/my`
4. **랭킹 확인** → `GET /api/rankings`

## 🌐 환경별 설정

### 개발 환경
- **Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### 프로덕션 환경
- **Base URL**: `https://api.rankus.kr`
- **HTTPS 필수**: 모든 요청은 HTTPS로 전송

---

💡 **추가 도움이 필요하신가요?**
- Swagger UI에서 실시간 API 테스트 가능
- 각 엔드포인트의 상세한 파라미터는 Swagger 문서 참조
- 질문이 있으시면 백엔드 개발팀에 문의해주세요!