# Rankus 프로젝트 권한 시스템 분석 보고서

## 📋 개요

이 문서는 Rankus 프로젝트의 인증(Authentication) 및 인가(Authorization) 시스템에 대한 종합적인 분석 보고서입니다.

## 🏗️ 아키텍처 개요

### 핵심 보안 아키텍처
- **인증 방식**: JWT 기반 Stateless 인증
- **인가 방식**: 역할 기반 접근 제어 (RBAC) + 리소스 기반 권한 검증
- **다계층 보안**: URL → Method → Domain 레벨 권한 검증
- **헥사고날 아키텍처**: 보안 로직과 비즈니스 로직의 깔끔한 분리

## 🔐 1. 핵심 보안 구성 요소

### 1.1 Spring Security 설정

#### SecurityConfig.java (`@Profile({"aws", "!dev"})`)
```java
// 프로덕션 환경 보안 설정
- JWT 기반 Stateless 세션 관리
- CORS 설정 적용
- 커스텀 인증/인가 실패 핸들러
- PUBLIC_URLS를 제외한 모든 요청 인증 필요
```

#### DevSecurityConfig.java (`@Profile("dev")`)
```java
// 개발 환경 보안 설정
- 모든 요청 허용 (permitAll)
- 빠른 개발을 위한 보안 비활성화
```

#### MethodSecurityConfig.java
```java
// 메서드 레벨 보안 활성화
- @PreAuthorize 애노테이션 지원
- UnifiedPermissionEvaluator 연동
```

### 1.2 JWT 인증 시스템

#### JwtTokenProvider.java
```java
// JWT 토큰 생성 및 검증
- HMAC SHA-256 서명 알고리즘
- Base64 인코딩된 시크릿 키
- 토큰 만료 시간 설정 가능
- 토큰 파싱 및 유효성 검증
```

#### JwtAuthenticationFilter.java
```java
// JWT 인증 필터
- Authorization 헤더에서 Bearer 토큰 추출
- 토큰 유효성 검증 및 SecurityContext 설정
- 무효한 토큰 시 401 Unauthorized 응답
```

#### CustomUserDetails.java
```java
// 사용자 인증 정보
- 사용자 ID, 역할, 소속 랩실 정보 포함
- Spring Security UserDetails 인터페이스 구현
```

## 👥 2. 역할 기반 접근 제어 (RBAC)

### 2.1 사용자 역할 (Role.java)
```java
public enum Role {
    STUDENT,     // 일반 학생 (기본값)
    LAB_MEMBER,  // 랩원
    LAB_MANAGER, // 랩 관리 담당
    LAB_LEADER,  // 랩장
    PROFESSOR,   // 담당 교수
    ADMIN        // 시스템 관리자
}
```

### 2.2 역할 계층 구조
```
ADMIN > PROFESSOR > LAB_LEADER > LAB_MANAGER > LAB_MEMBER > STUDENT
```

- **ADMIN**: 모든 시스템 기능 접근 가능
- **PROFESSOR**: 모든 랩실 관리 가능
- **LAB_LEADER**: 소속 랩실의 모든 관리 기능
- **LAB_MANAGER**: 소속 랩실의 일부 관리 기능
- **LAB_MEMBER**: 소속 랩실의 기본 기능 접근
- **STUDENT**: 지원 및 기본 조회 기능

### 2.3 도메인 특화 권한 메서드 (User.java)

#### 랩실 지원 관리 권한
```java
public boolean canManageLabApplications(Lab lab) {
    // ADMIN: 모든 랩실 관리 가능
    // LAB_LEADER, PROFESSOR: 소속 랩실만 관리 가능
}
```

#### 공지사항 권한
```java
public boolean canViewLabNotices(Lab lab) {
    // ADMIN, PROFESSOR: 모든 랩실 공지 조회 가능
    // LAB_MEMBER 이상: 소속 랩실 공지 조회 가능
}

public boolean canManageLabNotices(Lab lab) {
    // ADMIN, PROFESSOR: 모든 랩실 공지 관리 가능
    // LAB_MANAGER 이상: 소속 랩실 공지 관리 가능
}
```

#### 출석 관리 권한
```java
public boolean canManageLabAttendance(Lab lab) {
    // ADMIN, PROFESSOR: 모든 랩실 출석 관리 가능
    // LAB_MANAGER 이상: 소속 랩실 출석 관리 가능
}

public boolean canViewLabAttendance(Lab lab) {
    // ADMIN, PROFESSOR: 모든 랩실 출석 조회 가능
    // LAB_MEMBER 이상: 소속 랩실 출석 조회 가능
}
```

#### 투표 시스템 권한
```java
public boolean canViewVotes(Lab lab) {
    // LAB_MEMBER 이상: 소속 랩실 투표 참여 및 조회
}

public boolean canCreateVotes(Lab lab) {
    // LAB_MANAGER 이상: 소속 랩실 투표 생성
}

public boolean canManageVotes(Lab lab) {
    // LAB_MANAGER 이상: 소속 랩실 투표 관리
}
```

## 🔒 3. 다계층 권한 검증 시스템

### 3.1 1계층: URL 레벨 보안
```java
// SecurityConstants.java
public static final String[] PUBLIC_URLS = {
    "/api/auth/**",           // 인증 관련 API
    "/api/labs/**",           // 랩실 조회 API (GET만 허용)
    "/swagger-ui/**",         // API 문서
    "/v3/api-docs/**"        // OpenAPI 스펙
};
```

### 3.2 2계층: 메서드 레벨 인가
```java
// @PreAuthorize 애노테이션 사용 예시
@PreAuthorize("isAuthenticated()")  // 단순 인증 확인

@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #appId, 'LabApplication', 'DELETE')")
// 도메인 특화 권한 검증
```

### 3.3 3계층: 도메인 레벨 권한 검증

#### UnifiedPermissionEvaluator.java
```java
// 중앙집중식 권한 평가자
- 도메인별 PermissionHandler로 위임
- targetType 기반 라우팅
- 확장 가능한 구조
```

#### DomainPermissionEvaluator 인터페이스
```java
public interface DomainPermissionEvaluator {
    String targetType();  // 처리할 도메인 타입
    boolean hasPermission(Object principal, Serializable targetId, String permission);
}
```

#### 구현된 Permission Handler들
1. **LabApplicationPermissionHandler**: 랩실 지원 관련 권한
2. **AttendanceRecordPermissionHandler**: 출석 기록 권한
3. **AttendanceSessionPermissionHandler**: 출석 세션 권한
4. **LabNoticePermissionHandler**: 랩실 공지사항 권한
5. **VotePermissionHandler**: 투표 시스템 권한
6. **CalendarPermissionHandler**: 캘린더 관련 권한
7. **LabImagePermissionHandler**: 랩실 이미지 권한
8. **LabCreationRequestPermissionHandler**: 랩실 생성 요청 권한
9. **InterviewPermissionHandler**: 면접 관련 권한

## 🚨 4. 예외 처리 및 오류 응답

### 4.1 보안 관련 예외 처리

#### JwtAuthenticationEntryPoint.java
```java
// 인증 실패 시 401 Unauthorized JSON 응답
{
    "status": 401,
    "code": "UNAUTHORIZED",
    "message": "인증이 필요합니다",
    "path": "/api/..."
}
```

#### CustomAccessDeniedHandler.java
```java
// 인가 실패 시 403 Forbidden JSON 응답
{
    "status": 403,
    "code": "FORBIDDEN", 
    "message": "접근 권한이 없습니다",
    "path": "/api/..."
}
```

#### GlobalExceptionHandler.java
```java
// 전역 예외 처리
- BaseCustomException: 도메인별 예외
- MethodArgumentNotValidException: 입력 검증 오류
- AuthenticationException: 인증 오류
- AccessDeniedException: 인가 오류
```

## ⚠️ 5. 발견된 보안 이슈 및 개선 사항

### 5.1 🔴 중요 보안 이슈

#### 1. PUBLIC_URLS 과도한 노출
```java
// 현재 설정
"/api/labs/**"  // 모든 랩실 API가 인증 없이 접근 가능

// 권장 개선안
"/api/labs",           // GET /api/labs (랩실 목록 조회만)
"/api/labs/*/images"   // 랩실 이미지만 공개
```

**위험도**: 높음  
**영향**: 인증 없이 랩실 상세 정보, 멤버 정보 등 민감한 데이터 접근 가능

#### 2. JWT 토큰 무효화 시 필터 체인 중단
```java
// JwtAuthenticationFilter.java 60-67행
} else {
    SecurityContextHolder.clearContext();
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write("{\"error\":\"Invalid JWT token\"}");
    response.getWriter().flush();
    return; // 필터 체인 중단
}
```

**문제점**: 무효한 토큰이 있으면 필터 체인이 중단되어 PUBLIC_URLS도 접근 불가  
**권장 개선안**: PUBLIC_URLS인 경우 필터 체인 계속 진행

### 5.2 🟡 개선 권장 사항

#### 1. 역할 계층 구조 명시화
- Spring Security의 RoleHierarchy 설정 추가
- 상위 역할이 하위 역할 권한 자동 상속

#### 2. 세션 고정 공격 방지
- SessionCreationPolicy.STATELESS 사용으로 기본 방어됨
- 추가 세션 보안 헤더 설정 권장

#### 3. CORS 설정 강화
```java
// 현재: corsConfig.corsConfigurationSource() 사용
// 권장: 구체적인 허용 도메인 명시
```

#### 4. 보안 헤더 추가
```java
// 권장 추가 보안 헤더
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Strict-Transport-Security (HTTPS 환경)
```

## ✅ 6. 보안 우수 사례

### 6.1 잘 구현된 보안 패턴

1. **JWT 기반 Stateless 인증**: 확장성 우수
2. **BCrypt 암호화**: 강력한 비밀번호 해싱
3. **다계층 권한 검증**: 깊이 있는 보안 검증
4. **도메인별 Permission Handler**: 확장 가능한 권한 시스템
5. **헥사고날 아키텍처**: 보안과 비즈니스 로직 분리
6. **개발/프로덕션 환경 분리**: 개발 편의성과 보안 양립
7. **JSON 기반 오류 응답**: 일관된 API 응답 형식

### 6.2 테스트 커버리지
- Permission Handler들에 대한 포괄적인 단위 테스트
- 다양한 권한 시나리오 테스트
- 인증/인가 실패 케이스 테스트

## 🔧 7. 권장 수정 사항

### 7.1 즉시 수정 필요 (보안 이슈)

1. **PUBLIC_URLS 범위 축소**
2. **JWT 필터 로직 개선**
3. **보안 헤더 추가**

### 7.2 중장기 개선 사항

1. **역할 계층 구조 명시화**
2. **감사 로그 시스템 구축**
3. **Rate Limiting 도입**
4. **API 키 기반 외부 연동 보안**

## 📊 8. 결론

Rankus 프로젝트의 권한 시스템은 전반적으로 **견고하고 잘 설계된 보안 아키텍처**를 가지고 있습니다.

### 강점
- ✅ 다계층 보안 검증 시스템
- ✅ 확장 가능한 Permission Handler 구조
- ✅ 역할 기반 접근 제어의 체계적 구현
- ✅ 헥사고날 아키텍처와의 우수한 통합

### 개선 필요 영역
- ⚠️ PUBLIC_URLS 과도한 노출 (즉시 수정 필요)
- ⚠️ JWT 필터 로직 개선 (즉시 수정 필요)
- 💡 추가 보안 헤더 설정
- 💡 역할 계층 구조 명시화

**종합 평가**: A- (우수, 일부 보안 이슈 수정 필요)

---

## 📝 문서 정보
- **작성일**: 2025-07-21
- **분석 대상**: Rankus 프로젝트 권한 시스템
- **분석 범위**: 인증, 인가, 권한 검증, 예외 처리
- **작성자**: Claude Code 분석 시스템