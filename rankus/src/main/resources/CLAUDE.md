# Resources 디렉토리 가이드

> 애플리케이션 실행에 필요한 설정 파일과 정적 리소스를 관리하는 디렉토리

## 📁 Resources 디렉토리 개요

### 핵심 역할
- **설정 관리**: 애플리케이션 환경별 설정 파일 관리
- **데이터 초기화**: 개발/테스트용 초기 데이터 제공
- **정적 리소스**: CSS, JS, 이미지 등 웹 리소스 관리 (향후 확장)
- **메시지 국제화**: 다국어 지원을 위한 메시지 파일 관리 (향후 확장)

### 디렉토리 구조
```
src/main/resources/
├── CLAUDE.md                 # 이 파일
├── application.yml           # 기본(개발) 환경 설정
├── application-secure.yml    # 로컬 보안 테스트 환경 설정
├── application-aws.yml       # AWS 배포 환경 설정
├── data.sql                  # 개발용 초기 데이터
├── static/                   # 정적 웹 리소스 (향후 확장)
│   ├── css/
│   ├── js/
│   └── images/
└── messages/                 # 국제화 메시지 (향후 확장)
    ├── messages.properties
    ├── messages_ko.properties
    └── messages_en.properties
```

## ⚙️ 설정 파일 구조

### 1. application.yml (기본 환경)
**프로파일**: 기본 (프로파일 없음)
**용도**: 로컬 개발 환경, JWT 보안 활성화

```yaml
spring:
  config:
    activate:
      on-profile: default
  datasource:
    url: jdbc:mysql://localhost:3306/rankus
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: create  # 매번 스키마 재생성
    defer-datasource-initialization: true
  sql:
    init:
      mode: always  # data.sql 항상 실행

security:
  jwt:
    secret: yDeGly34tDXIxkjo6MQKgBNCI+2iMFLdT0i8zD2JZuE=  # 개발용 고정값
    expiration-ms: 3600000
```

**특징**:
- 매번 데이터베이스 스키마 재생성 (`create`)
- `SecurityConfig`로 JWT 보안 활성화
- 개발용 고정 JWT 시크릿 키 사용
- SQL 로깅 활성화

### 2. application-dev.yml (빠른 개발 환경)
**프로파일**: `dev`
**용도**: 보안 비활성화로 빠른 개발 및 테스트

```yaml
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://localhost:3306/rankus
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: create
    defer-datasource-initialization: true
  sql:
    init:
      mode: always

security:
  jwt:
    secret: yDeGly34tDXIxkjo6MQKgBNCI+2iMFLdT0i8zD2JZuE=
    expiration-ms: 3600000
```

**특징**:
- `DevSecurityConfig` 활성화 (보안 비활성화)
- 모든 엔드포인트 접근 허용
- 빠른 개발 및 테스트에 최적화
- 스키마 재생성 및 초기 데이터 로드

### 3. application-aws.yml (배포 환경)
**프로파일**: `aws`
**용도**: AWS EC2 배포 환경

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    
  jpa:
    hibernate:
      ddl-auto: validate

jwt:
  secret-key: ${JWT_SECRET_KEY}
  expiration-time: ${JWT_EXPIRATION_TIME:86400000}

logging:
  level:
    root: INFO
```

**특징**:
- 모든 설정을 환경변수로 관리
- 프로덕션 수준 로깅 설정
- 데이터베이스 스키마는 별도 마이그레이션으로 관리

## 🗃️ 데이터 초기화 (data.sql)

### 목적
- **개발 편의성**: 개발자가 즉시 테스트할 수 있는 데이터 제공
- **API 테스트**: Swagger UI에서 바로 테스트 가능한 샘플 데이터
- **데모 환경**: 기능 시연을 위한 일관된 데이터

### 데이터 구조
```sql
-- 1. 랩실 데이터
INSERT INTO labs (id, name, category, description, ranking, professor_name)
VALUES
    (1, 'AI랩', 'AI', '인공지능 랩', 1, null),
    (2, 'DB랩', 'DB', '데이터베이스 랩', 2, null);

-- 2. 사용자 데이터 (비밀번호: "password123!")
INSERT INTO users (id, name, email, password_hash, role, lab_id)
VALUES
    (1, '학생1', 'user1@example.com', '$2a$10$...', 'STUDENT', NULL),
    (2, '랩장1', 'leader1@example.com', '$2a$10$...', 'LAB_LEADER', 1),
    (3, '교수', 'prof@example.com', '$2a$10$...', 'PROFESSOR', 2);

-- 3. 랩실 이미지
INSERT INTO lab_images (id, lab_id, image_url, type)
VALUES
    (1, 1, 'https://example.com/lab1_img1.png', 'REPRESENTATIVE');

-- 4. 지원서 데이터
INSERT INTO lab_applications (id, lab_id, user_id, interview_time, status)
VALUES
    (1, 1, 1, '2025-07-01 13:00:00', 'PENDING');
```

### 테스트 계정 정보
| 이메일 | 비밀번호 | 역할 | 소속 랩실 |
|--------|----------|------|-----------|
| user1@example.com | password123! | STUDENT | 없음 |
| leader1@example.com | password123! | LAB_LEADER | AI랩 |
| prof@example.com | password123! | PROFESSOR | DB랩 |
| admin@example.com | password123! | ADMIN | 없음 |

## 📊 환경별 설정 비교

| 설정 항목 | 기본 | dev | aws |
|-----------|------|-----|-----|
| **보안** | 활성화 | 비활성화 | 활성화 |
| **DDL** | create | create | validate |
| **데이터 초기화** | data.sql | data.sql | 없음 |
| **JWT 시크릿** | 고정값 | 고정값 | 환경변수 |
| **로깅** | INFO | DEBUG | INFO |
| **SQL 로깅** | 활성화 | 활성화 | 비활성화 |

## 🔐 보안 설정 관리

### 환경변수 설정 예시
```bash
# 빠른 개발 환경 (dev 프로파일)
./gradlew bootRun --args='--spring.profiles.active=dev'

# AWS 배포 환경 (aws 프로파일)
export DB_URL="jdbc:mysql://your-rds-endpoint:3306/rankus"
export DB_USERNAME="your-db-username"
export DB_PASSWORD="your-db-password"
export JWT_SECRET_KEY="your-production-secret-key"
```

### JWT 시크릿 키 생성
```bash
# 안전한 32바이트 시크릿 키 생성
openssl rand -base64 32
```

## 🌍 국제화 메시지 (향후 확장)

### 메시지 파일 구조 (예정)
```
messages/
├── messages.properties           # 기본 (한국어)
├── messages_ko.properties        # 한국어 명시
└── messages_en.properties        # 영어
```

### 메시지 예시
```properties
# messages_ko.properties
user.not.found=사용자를 찾을 수 없습니다
lab.application.success=랩실 지원이 완료되었습니다

# messages_en.properties  
user.not.found=User not found
lab.application.success=Lab application completed successfully
```

## 📈 정적 리소스 관리 (향후 확장)

### 정적 리소스 구조 (예정)
```
static/
├── css/
│   ├── common.css
│   └── admin.css
├── js/
│   ├── common.js
│   └── lab.js
├── images/
│   ├── logo.png
│   └── default-lab.jpg
└── docs/
    ├── api-guide.html
    └── user-manual.pdf
```

### 접근 경로
- **CSS**: `/css/common.css`
- **JS**: `/js/common.js`
- **이미지**: `/images/logo.png`
- **문서**: `/docs/api-guide.html`

## 🎯 개발 가이드

### 새로운 프로파일 추가
1. `application-{profile}.yml` 파일 생성
2. 프로파일별 특화 설정 작성
3. 환경변수 또는 JVM 옵션으로 활성화
   ```bash
   # 환경변수
   export SPRING_PROFILES_ACTIVE=secure
   
   # JVM 옵션
   java -Dspring.profiles.active=secure -jar rankus.jar
   ```

### 설정 우선순위
1. **JVM 시스템 프로퍼티** (`-Djwt.secret-key=...`)
2. **환경변수** (`JWT_SECRET_KEY`)
3. **프로파일별 설정파일** (`application-aws.yml`)
4. **기본 설정파일** (`application.yml`)

### 개발 시 주의사항
- **default 프로파일**: 보안이 비활성화되어 있으므로 운영 환경에서 절대 사용 금지
- **JWT 시크릿**: 운영 환경에서는 반드시 환경변수로 관리
- **데이터베이스 비밀번호**: 설정 파일에 평문으로 저장하지 않고 환경변수 사용
- **DDL 모드**: 운영 환경에서는 `validate`만 사용, 스키마 변경은 별도 마이그레이션 도구 활용

## 🔗 관련 설정 파일

- **보안 설정**: `@config/SecurityConfig.java`, `@config/DevSecurityConfig.java`
- **JWT 설정**: `@common/security/jwt/JwtTokenProvider.java`
- **데이터베이스 설정**: `@config/DomainConfig.java`
- **Swagger 설정**: `@config/SwaggerConfig.java`