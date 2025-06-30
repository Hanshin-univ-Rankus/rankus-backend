# Config Layer 가이드

> 🛠️ **Config 컨벤션**: @config/CONVENTIONS.md  
> 🔐 **보안 설정**: @common/security/CONVENTIONS.md  
> 🎧 **프로파일**: @core/conventions.md#환경별-설정  
> 📋 **표준 템플릿**: @core/templates.md

## ⚙️ 핵심 구성

### 책임 매트릭스
| Config 클래스 | 역할 | 프로파일 |
|------------|------|----------|
| SecurityConfig | Spring Security | !default |
| DevSecurityConfig | 개발용 보안 비활성화 | default |
| SwaggerConfig | API 문서 | 전체 |
| CorsConfig | CORS 설정 | 전체 |
| JacksonConfig | JSON 직렬화 | 전체 |
| DomainConfig | JPA Auditing | 전체 |

## 🏗️ 기본 템플릿

> 📋 **Security 템플릿**: @core/templates.md#security-config

### 기타 Config 매트릭스
| Config | 주요 설정 | Bean |
|--------|----------|------|
| SwaggerConfig | OpenAPI 3.0, JWT 보안 | OpenAPI |
| CorsConfig | `/api/**` 매핑, Credentials 허용 | WebMvcConfigurer |
| JacksonConfig | JavaTimeModule, snake_case | ObjectMapper |
| DomainConfig | JPA Auditing 활성화 | Clock |
| MethodSecurityConfig | @PreAuthorize 지원 | PermissionEvaluator |

## 🌍 환경별 설정

### 프로파일 매트릭스
| 프로파일 | 용도 | 데이터베이스 | 보안 | 로깅 |
|---------|------|------------|------|---------|
| default | 개발 | localhost:3306 | 비활성화 | DEBUG |
| secure | 로컬 보안 테스트 | localhost:3306 | JWT 활성화 | WARN |
| aws | 배포 | 환경변수 | JWT 활성화 | INFO |

### 주요 설정 항목
```yaml
# application.yml (기본)
spring:
  profiles.active: default
  jpa.hibernate.ddl-auto: create-drop
  datasource.url: jdbc:mysql://localhost:3306/rankus
jwt:
  secret-key: "development-only-key"
  expiration-time: 86400000

# application-aws.yml (배포)
spring:
  datasource: ${DB_URL}, ${DB_USERNAME}, ${DB_PASSWORD}
  jpa.hibernate.ddl-auto: validate
jwt.secret-key: ${JWT_SECRET_KEY}
```

## 🔐 주요 보안 설정

> 📋 **Properties 템플릿**: @core/templates.md#properties-클래스

### 보안 헤더 필터
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`

## 🧪 테스트 패턴

> 🧪 **Config 테스트**: @core/testing.md#config-테스트

> 🧪 **테스트 템플릿**: @core/templates.md#보안-설정-테스트

## 🎯 모범 사례

### 환경별 관리 전략
| 환경 | 관리 방식 | 예시 |
|------|----------|------|
| 개발 | IDE/로컬 파일 | .env, application.yml |
| 테스트 | 테스트 프로파일 | application-test.yml |
| 운영 | 환경변수/클라우드 | AWS Parameter Store |

### 보안 체크리스트
- ✅ JWT Secret 32자+ 강력한 키
- ✅ BCrypt 비밀번호 암호화
- ✅ CORS 운영환경 도메인 제한
- ✅ HTTPS 운영환경 강제

### 성능 최적화
- ✅ HikariCP Connection Pool
- ✅ JPA Batch Insert/Update (batch_size: 20)
- ✅ 지연 로딩 기본 적용
- ✅ 2차 캐시 선택적 사용

## 🎯 주요 규칙 요약

1. **프로파일 분리**: default(개발), secure(로컬테스트), aws(배포)
2. **보안 전략**: 개발용 보안 비활성화, 운영용 보안 강화
3. **환경변수**: 민감정보 환경변수 분리
4. **Bean 조건**: @ConditionalOnMissingBean, @Profile 활용
5. **설정 검증**: @PostConstruct로 필수 설정 검증