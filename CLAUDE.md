# Rankus 프로젝트

> 대학 내 랩실 정보 불균형 해소와 랩실 운영 효율화를 위한 종합 플랫폼

## 🏗️ 프로젝트 개요

### 핵심 목표
- **랩실 홍보**: 연구실 정보 투명화 및 접근성 향상
- **지원 관리**: 체계적인 랩실 지원 및 선발 프로세스
- **운영 효율화**: QR 출석, 캘린더, 공지 등 통합 관리
- **동기 부여**: 랭킹 시스템을 통한 활동 촉진

### 기술 스택
- **Backend**: Java 17, Spring Boot 3.4.5, Spring Security, JPA/Hibernate
- **Database**: MySQL 8.0 (개발용 Docker Compose 환경)
- **Authentication**: JWT 기반 Stateless 인증
- **API Documentation**: SpringDoc OpenAPI 3.0 (Swagger UI)
- **Architecture**: 헥사고날 아키텍처 (Ports & Adapters)

## ⚙️ 빠른 시작

### 로컬 개발 환경 설정
```bash
# 1. 데이터베이스 컨테이너 실행
docker-compose up -d

# 2. 애플리케이션 실행 (기본 프로파일)
./gradlew bootRun

# 3. API 문서 확인
# http://localhost:8080/swagger-ui.html
```

### Spring Profiles
- **기본 (프로파일 없음)**: 로컬 개발용, JWT 보안 활성화, SQL 로깅 활성화
- **`dev`**: 빠른 개발용, 보안 비활성화, 디버깅 최적화
- **`aws`**: 배포용, 환경변수 기반 설정, 프로덕션 보안

### 데이터베이스 구성
- **개발용**: `localhost:3306/rankus` (docker-compose)
- **테스트용**: `localhost:3308/rankus_test` (통합 테스트 격리)

## 📚 상세 문서

### 경로 별칭 시스템
```
@src = rankus/src/main/java/org/univ/rankus
@core = @src/_core
@domain = @src/domain  
@app = @src/application
@adapter = @src/adapter
@common = @src/common
@config = @src/config
@test = rankus/src/test/java/org/univ/rankus
```

### AI 최적화 가이드 (우선 참조 ⭐)
- **AI 개발 필수**: @core/ai-essentials.md (300줄, 핵심 패턴)
- **테스트 패턴**: @core/test-patterns.md (150줄, 프로덕션 매칭)
- **코드 템플릿**: @core/code-templates.md (200줄, 복사-붙여넣기)
- **ErrorCode 관리**: @core/error-codes.md (100줄, 중앙 집중식)

### 상세 가이드 (참조용)
- **코딩 패턴**: @core/patterns.md
- **네이밍 규칙**: @core/conventions.md  
- **테스트 가이드**: @core/testing.md
- **HTTP/권한 매트릭스**: @core/http-matrix.md

### 계층별 가이드
- **소스코드 구조**: @src/CLAUDE.md
- **도메인 모델**: @domain/CLAUDE.md  
- **애플리케이션 계층**: @app/CLAUDE.md
- **어댑터 계층**: @adapter/CLAUDE.md
- **공통 컴포넌트**: @common/CLAUDE.md
- **설정 관리**: @config/CLAUDE.md

## 🎯 현재 구현 상태 (약 65%)

### ✅ 완료된 기능
- **회원 관리**: 가입, JWT 로그인, 권한 기반 접근 제어
- **랩실 홍보**: 목록, 상세, 이미지 관리
- **랩실 지원**: 신청, 승인/거부 프로세스
- **랩실 생성 요청**: 랩실 생성 신청, 관리자 승인/거부 워크플로우
- **공지사항 관리**: 랩실별 공지사항 생성, 수정, 삭제, 조회, 고정 기능 (완료)

### 🔄 다음 개발 우선순위
1. **랭킹 시스템**: 점수 관리 및 승인 프로세스
2. **QR 출석 시스템**: 실용적 운영 도구
3. **캘린더 기능**: 일정 관리 통합

## 🛠️ 개발 가이드

### 🔧 Gradle 실행 환경
```bash
# ⚠️ 중요: 모든 Gradle 명령은 rankus 디렉토리에서 실행해야 합니다
cd rankus

# 애플리케이션 실행
./gradlew bootRun

# 테스트 실행
./gradlew test

# 특정 테스트
./gradlew test --tests UserCommandServiceTest

# 빌드
./gradlew build

# 린트 및 타입 체크
./gradlew check
```

### 📁 프로젝트 구조 인식
- **루트 디렉토리**: `/project` (현재 위치)
- **Gradle 프로젝트**: `/project/rankus` (실행 디렉토리)
- **소스코드**: `/project/rankus/src/main/java/org/univ/rankus`

### 개발 워크플로우
1. `cd rankus` - Gradle 프로젝트 디렉토리로 이동
2. `./gradlew test` - 테스트 실행으로 현재 상태 확인
3. 코드 작성/수정
4. `./gradlew test` - 변경사항 테스트
5. `./gradlew check` - 린트/타입 체크 (가능한 경우)

### 배포
- **플랫폼**: AWS EC2
- **CI/CD**: GitHub Actions (`aws` 브랜치 push 시 자동 배포)
- **데이터베이스**: AWS RDS MySQL