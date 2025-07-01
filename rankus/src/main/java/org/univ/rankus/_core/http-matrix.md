# HTTP 상태코드 및 권한 매트릭스

## 📊 HTTP 상태코드 매트릭스

### 성공 응답 (2xx)

| 코드  | 상황       | Controller 패턴                    | ApiResponse 메서드         |
|-----|----------|----------------------------------|-------------------------|
| 200 | 조회/수정 성공 | `ResponseEntity.ok()`            | `ApiResponse.success()` |
| 201 | 생성 성공    | `ResponseEntity.status(CREATED)` | `ApiResponse.created()` |
| 204 | 삭제 성공    | `ResponseEntity.noContent()`     | `ApiResponse.deleted()` |

### 클라이언트 오류 (4xx)

| 코드  | 상황     | 발생 케이스          | ErrorCode 예시           |
|-----|--------|-----------------|------------------------|
| 400 | 잘못된 요청 | 검증 실패, 잘못된 파라미터 | `INVALID_INPUT`        |
| 401 | 인증 필요  | JWT 토큰 없음/만료    | `UNAUTHORIZED`         |
| 403 | 권한 없음  | 접근 권한 부족        | `ACCESS_DENIED`        |
| 404 | 리소스 없음 | 엔티티 조회 실패       | `USER_NOT_FOUND`       |
| 409 | 충돌     | 중복 생성, 상태 충돌    | `EMAIL_DUPLICATED`     |
| 422 | 처리 불가  | 비즈니스 규칙 위반      | `CANNOT_CHANGE_STATUS` |

### 서버 오류 (5xx)

| 코드  | 상황    | 처리 방법                       |
|-----|-------|-----------------------------|
| 500 | 서버 오류 | GlobalExceptionHandler에서 처리 |

## 🔐 권한 매트릭스

### 역할별 권한 계층

| 역할          | 계층 | 포함 권한               |
|-------------|----|---------------------|
| ADMIN       | 6  | 모든 권한               |
| PROFESSOR   | 5  | LAB_LEADER + 랩 관리   |
| LAB_LEADER  | 4  | LAB_MANAGER + 최종 승인 |
| LAB_MANAGER | 3  | LAB_MEMBER + 일반 관리  |
| LAB_MEMBER  | 2  | STUDENT + 랩 활동      |
| STUDENT     | 1  | 기본 조회/지원            |

### 리소스별 액션 권한

| 리소스                | 액션      | STUDENT | LAB_MEMBER | LAB_MANAGER | LAB_LEADER | PROFESSOR | ADMIN |
|--------------------|---------|---------|------------|-------------|------------|-----------|-------|
| **User**           | VIEW    | Own     | Own        | Own         | Own        | Own       | All   |
|                    | CREATE  | ✅       | ✅          | ✅           | ✅          | ✅         | ✅     |
|                    | UPDATE  | Own     | Own        | Own         | Own        | Own       | All   |
|                    | DELETE  | ❌       | ❌          | ❌           | ❌          | ❌         | All   |
| **Lab**            | VIEW    | ✅       | ✅          | ✅           | ✅          | ✅         | ✅     |
|                    | CREATE  | ❌       | ❌          | ❌           | ❌          | Own       | ✅     |
|                    | UPDATE  | ❌       | ❌          | Lab         | Lab        | Lab       | All   |
|                    | DELETE  | ❌       | ❌          | ❌           | ❌          | ❌         | All   |
| **LabApplication** | VIEW    | Own     | Own+Lab    | Lab         | Lab        | Lab       | All   |
|                    | CREATE  | ✅       | ✅          | ✅           | ✅          | ✅         | ✅     |
|                    | APPROVE | ❌       | ❌          | Lab         | Lab        | Lab       | All   |
|                    | REJECT  | ❌       | ❌          | Lab         | Lab        | Lab       | All   |
|                    | DELETE  | Own     | Own        | Lab         | Lab        | Lab       | All   |
| **LabImage**       | VIEW    | ✅       | ✅          | ✅           | ✅          | ✅         | ✅     |
|                    | CREATE  | ❌       | Lab        | Lab         | Lab        | Lab       | All   |
|                    | UPDATE  | ❌       | Lab        | Lab         | Lab        | Lab       | All   |
|                    | DELETE  | ❌       | Lab        | Lab         | Lab        | Lab       | All   |

### @PreAuthorize 패턴 매트릭스

| 권한 체크 | 패턴                                                                    | 사용 케이스      |
|-------|-----------------------------------------------------------------------|-------------|
| 인증만   | `isAuthenticated()`                                                   | 기본 CRUD, 지원 |
| 역할 기반 | `hasRole('ADMIN')`                                                    | 관리자 전용      |
| 소유권   | `@permissionEvaluator.hasPermission(auth, #id, 'Type', 'ACTION')`     | 개인 리소스      |
| 복합 조건 | `isAuthenticated() and (hasRole('ADMIN') or @permissionEvaluator...)` | 복잡한 권한      |

### HTTP 메서드별 기본 권한

| HTTP   | 일반적 권한       | 예외 케이스        |
|--------|--------------|---------------|
| GET    | Public or 인증 | 개인정보는 소유권 체크  |
| POST   | 인증           | 관리 기능은 역할 체크  |
| PUT    | 소유권 or 관리권한  | 상태변경은 특별 권한   |
| DELETE | 소유권 or 관리권한  | 시스템 데이터는 관리자만 |

## 🎯 권한 검증 플로우

### 1. URL 기반 (SecurityConfig)

```
JWT Filter → URL 매칭 → 기본 인증 확인
```

### 2. 메서드 기반 (@PreAuthorize)

```
메서드 호출 → @PreAuthorize 평가 → 권한 확인 → 실행
```

### 3. 도메인 기반 (PermissionEvaluator)

```
메서드 실행 → 리소스 조회 → 비즈니스 권한 확인 → 허용/거부
```