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

### JWT 인증 플로우
1. **로그인** → JWT 토큰 발급
2. **헤더 추가**: `Authorization: Bearer {token}`
3. **토큰 만료 시 재로그인 필요**

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

## 📁 File Upload API

### 파일 업로드
```http
POST /api/files/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: (파일 데이터)
```

**응답 예시**:
```json
{
  "success": true,
  "message": "파일 업로드 성공",
  "data": {
    "fileId": 1,
    "fileName": "evidence.pdf",
    "fileSize": 1024000,
    "fileUrl": "/uploads/evidence.pdf",
    "mimeType": "application/pdf"
  }
}
```

### 파일 다운로드
```http
GET /api/files/{fileId}/download
Authorization: Bearer {token}
```

### 중복 파일 확인
```http
GET /api/files/check-duplicate?fileName=evidence.pdf&fileSize=1024000
Authorization: Bearer {token}
```

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