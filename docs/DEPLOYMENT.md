# 배포 전략과 CI/CD 파이프라인

> 배포 아키텍처, CI/CD 파이프라인 구축, 환경별 설정 분리, 그리고 배포 중 겪은 문제와 해결 과정을 기록합니다.

## 목차

- [1. 배포 아키텍처 개요](#1-배포-아키텍처-개요)
- [2. GitHub Actions CI/CD 파이프라인](#2-github-actions-cicd-파이프라인)
- [3. 프로필 기반 설정 분리](#3-프로필-기반-설정-분리)
- [4. 보안 설정 분리](#4-보안-설정-분리)
- [5. 배포 중 겪은 문제와 개선](#5-배포-중-겪은-문제와-개선)
- [6. 코드 품질 관리 (JaCoCo)](#6-코드-품질-관리-jacoco)
- [7. Docker 로컬 개발 환경](#7-docker-로컬-개발-환경)

---

## 1. 배포 아키텍처 개요

```
┌─────────────┐     push      ┌──────────────────┐     upload     ┌─────────┐
│  Developer   │ ──────────→  │  GitHub Actions   │ ──────────→   │   AWS   │
│  (aws branch)│              │  (Build + Test)   │               │   S3    │
└─────────────┘              └──────────────────┘               └────┬────┘
                                                                     │ download
                                                                     ▼
                                                              ┌─────────────┐
                                                              │   AWS EC2   │
                                                              │  (Deploy)   │
                                                              │  app.jar    │
                                                              └──────┬──────┘
                                                                     │
                                                                     ▼
                                                              ┌─────────────┐
                                                              │  MySQL 8.0  │
                                                              │  (AWS RDS)  │
                                                              └─────────────┘
```

### 왜 이 구조인가

학교 프로젝트 규모에서 ECS나 EKS는 오버 엔지니어링이었습니다. EC2 단일 인스턴스 + S3 경유 배포가 비용 대비 가장 합리적이었습니다.

**S3를 경유하는 이유**: GitHub Actions에서 EC2로 직접 SCP할 수도 있지만, S3에 올리면 이전 버전 JAR도 보관할 수 있고, 문제 발생 시 특정 버전을 빠르게 복구할 수 있습니다. 또한 향후 여러 인스턴스에 배포할 때도 S3에서 각각 다운로드하면 됩니다.

**한계**: 직접 JAR를 교체하는 방식이라 무중단 배포는 지원하지 않습니다. 배포 시 수 초간 서비스가 중단됩니다.

---

## 2. GitHub Actions CI/CD 파이프라인

`aws` 브랜치에 push하면 자동으로 빌드 → S3 업로드 → EC2 배포가 실행됩니다.

```yaml
# .github/workflows/ci-dev.yml
name: CI-AWS
on:
  push:
    branches: [aws]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      # 1. 보안 키 사전 검증 — 잘못된 설정이 배포되는 것을 방지
      - name: Validate QR_SECRET_KEY length
        run: |
          KEY_LENGTH=$(echo -n "${{ secrets.QR_SECRET_KEY }}" | wc -c)
          if [ "$KEY_LENGTH" -ne 32 ]; then
            echo "ERROR: QR_SECRET_KEY must be exactly 32 bytes"
            exit 1
          fi

      # 2. Java 17 + Gradle 빌드
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: cd rankus && ./gradlew clean bootJar

      # 3. S3 업로드
      - uses: aws-actions/configure-aws-credentials@v4
      - run: aws s3 cp rankus/build/libs/app.jar s3://${{ secrets.S3_BUCKET }}/app.jar

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      # 4. EC2 배포
      - name: Deploy to EC2
        run: |
          # 백업 관리 — 최근 5개만 유지 (디스크 절약)
          ssh ec2 "cd ~/app && ls -t app.jar-backup-*.jar 2>/dev/null | tail -n +6 | xargs -r rm"

          # 로그 용량 관리 — 최근 5000줄만 유지
          ssh ec2 "cd ~/app && tail -5000 deploy.log > deploy.log.tmp && mv deploy.log.tmp deploy.log"

          # 기존 프로세스 종료 → 백업 → 새 JAR 배포
          ssh ec2 "cd ~/app && kill $(pgrep -f 'java -jar') || true"
          ssh ec2 "cd ~/app && cp app.jar app.jar-backup-$(date +%Y%m%d%H%M%S).jar"
          scp ./app.jar ec2:~/app/app.jar

          # 환경변수 설정 + 실행
          ssh ec2 "cd ~/app && nohup java -jar app.jar \
            --spring.profiles.active=aws >> deploy.log 2>&1 &"
```

### 설계 결정과 이유

**빌드 전 키 검증**: QR 코드 암호화에 정확히 32바이트 키가 필요합니다. 키 길이가 맞지 않으면 애플리케이션이 기동은 되지만 QR 관련 API에서 런타임 에러가 발생합니다. 빌드 단계에서 미리 검증하여 "잘못된 빌드가 배포되는 것 자체를 방지"했습니다.

**백업 전략**: 배포 시 이전 JAR을 타임스탬프와 함께 백업합니다. 롤백이 필요하면 `cp app.jar-backup-20251021120000.jar app.jar`로 즉시 복구할 수 있습니다. 디스크 절약을 위해 최근 5개만 유지합니다.

**`|| true` 패턴**: `kill $(pgrep -f 'java -jar') || true`에서 `|| true`가 없으면, 기존 프로세스가 없는 경우(첫 배포) 스크립트 전체가 중단됩니다.

---

## 3. 프로필 기반 설정 분리

Spring Profile로 환경별 설정을 분리했습니다.

### application.yml (기본값 — 로컬 개발)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create
  datasource:
    url: jdbc:mysql://localhost:3306/rankus
    username: root
    password: password

security:
  jwt:
    secret: yDeGly34tDXIxkjo6MQKgBNCI+2iMFLdT0i8zD2JZuE=
    access-expiration-ms: 900000      # 15분
    refresh-expiration-ms: 604800000  # 7일

rankus:
  qr:
    secret-key: LocalDevQRSecretKey_2025__32__OK
    legacy-enabled: true
```

### application-aws.yml (운영)

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}            # 환경변수에서 주입
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

security:
  jwt:
    secret: ${JWT_SECRET}

rankus:
  qr:
    secret-key: ${QR_SECRET_KEY:?QR Secret(32 bytes) must be provided}
    # ?문법: 환경변수 없으면 애플리케이션 기동 실패
```

### 설계 결정

**운영 설정에 하드코딩된 값 없음**: 모든 민감 정보는 환경변수로 주입합니다. `${KEY:?메시지}` 문법으로, 환경변수가 없으면 애플리케이션이 기동조차 하지 않도록 강제합니다. 이는 빌드 시점 검증과 함께 이중 안전장치 역할을 합니다.

**로컬에서는 편하게**: 개발 환경에는 기본값이 있어서, 별도 설정 없이 `docker-compose up` + `./gradlew bootRun`으로 바로 실행할 수 있습니다.

---

## 4. 보안 설정 분리

### 운영 보안 (SecurityConfig.java)

```java
@Profile({"aws", "!dev"})
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(SecurityConstants.ALWAYS_PUBLIC_URLS).permitAll()
                .requestMatchers(HttpMethod.GET, SecurityConstants.PUBLIC_GET_URLS).permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frameOptions -> frameOptions.deny())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)       // HSTS 1년
                    .includeSubDomains(true)
                )
            )
            .addFilterBefore(jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### 개발 보안 (DevSecurityConfig.java)

```java
@Profile("dev")
@Configuration
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
```

### 왜 분리했는가

로컬 개발 시 매번 JWT 토큰을 발급받아 Swagger에 입력하는 것이 비효율적이었습니다. `dev` 프로필에서는 모든 요청을 허용하여 개발 속도를 높이고, 운영에서는 `@Profile({"aws", "!dev"})`로 반드시 JWT 인증을 거치도록 강제합니다.

**교훈**: 이 분리에는 부작용도 있었습니다. 개발 환경에서 모든 요청을 허용하다 보니 권한 관련 버그(GLOBAL_004 에러)가 운영 배포 전까지 발견되지 않았습니다. 이에 대한 상세한 내용은 [TROUBLE_SHOOTING.md의 권한 시스템 항목](TROUBLE_SHOOTING.md#4-권한-시스템-global_004-에러)에서 다룹니다.

---

## 5. 배포 중 겪은 문제와 개선

### 문제 1: 디스크 용량 부족

배포를 반복하면서 백업 JAR 파일이 누적되고, deploy.log 파일이 무한히 커져 EC2 디스크가 가득 찼습니다.

**해결**: CI/CD 파이프라인에 정리 로직 추가.

```bash
# 백업 JAR: 최근 5개만 유지
ls -t app.jar-backup-*.jar | tail -n +6 | xargs -r rm

# 로그: 최근 5000줄만 유지
tail -5000 deploy.log > deploy.log.tmp && mv deploy.log.tmp deploy.log
```

### 문제 2: 암호화 키 누락으로 기동 실패

QR_SECRET_KEY 환경변수를 설정하지 않고 배포하여, 애플리케이션이 기동은 되지만 QR 관련 API에서 런타임 에러가 발생했습니다.

**해결**: 이중 검증 적용.
1. **빌드 시점**: CI/CD에서 키 길이 검증 (빌드 자체를 실패시킴)
2. **기동 시점**: `${QR_SECRET_KEY:?}` 문법으로 환경변수 없으면 기동 불가

"빌드 시점에 잡을 수 있는 에러는 빌드 시점에 잡자"는 원칙을 적용했습니다.

### 문제 3: 배포 후 프로세스 미종료

`kill $(pgrep -f 'java -jar')`이 실패하면(기존 프로세스가 없는 경우) 이후 명령이 실행되지 않는 문제가 있었습니다.

**해결**: `|| true`를 추가하여 프로세스가 없어도 스크립트가 계속 진행되도록 수정.

---

## 6. 코드 품질 관리 (JaCoCo)

### 커버리지 기준

```groovy
// build.gradle
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit { counter = 'LINE'; value = 'COVEREDRATIO'; minimum = 0.80 }
            limit { counter = 'BRANCH'; value = 'COVEREDRATIO'; minimum = 0.70 }
        }
        rule {
            element = 'CLASS'
            includes = ['org.univ.rankus.domain.model.*']
            limit { counter = 'LINE'; value = 'COVEREDRATIO'; minimum = 0.95 }
        }
        rule {
            element = 'CLASS'
            includes = ['org.univ.rankus.application.service.*']
            limit { counter = 'LINE'; value = 'COVEREDRATIO'; minimum = 0.90 }
        }
        rule {
            element = 'CLASS'
            includes = ['org.univ.rankus.adapter.*']
            limit { counter = 'LINE'; value = 'COVEREDRATIO'; minimum = 0.85 }
        }
    }
}
```

### 왜 레이어별로 기준이 다른가

도메인 모델(95%)에 가장 높은 기준을 두었습니다. 비즈니스 로직의 핵심이 도메인에 있으므로, 여기가 가장 확실하게 검증되어야 합니다.

반면 어댑터(85%)는 외부 시스템과의 연결 코드가 많아 테스트 작성이 상대적으로 어렵고, 통합 테스트에서 더 효과적으로 검증됩니다.

### 커버리지 측정 제외 대상

DTO, Config, Exception, Constants 클래스는 커버리지 측정에서 제외하여, 의미 있는 코드에 집중할 수 있도록 했습니다. "커버리지 숫자만 채우기 위한 getter/setter 호출 테스트"를 방지하기 위함입니다.

---

## 7. Docker 로컬 개발 환경

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    container_name: rankus-db
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: rankus
    ports:
      - "3306:3306"
    volumes:
      - db-data:/var/lib/mysql

  mysql_test:
    image: mysql:8.0
    container_name: rankus-db-test
    profiles: ["test"]          # docker-compose --profile test up 으로만 실행
    environment:
      MYSQL_DATABASE: rankus_test
    ports:
      - "3308:3306"             # 개발 DB와 포트 충돌 방지
```

`profiles: ["test"]`를 사용하여, 테스트용 MySQL은 `--profile test`를 명시적으로 붙여야만 실행됩니다. 일반 개발 시에는 `docker-compose up -d`만으로 개발 DB만 뜹니다.

---

## 관련 문서

- [TROUBLE_SHOOTING.md](TROUBLE_SHOOTING.md) — 배포 과정에서 발생한 기술적 문제들
- [ARCHITECTURE.md](ARCHITECTURE.md) — 프로필별 Security 설정의 아키텍처적 맥락
- [README.md](../README.md) — 프로젝트 전체 개요
