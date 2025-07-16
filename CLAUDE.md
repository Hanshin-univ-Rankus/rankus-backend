# Rankus 프로젝트

> 대학 내 랩실 정보 불균형 해소와 랩실 운영 효율화를 위한 종합 플랫폼

## 🏗️ 프로젝트 개요

**핵심 목표**: 랩실 홍보, 지원 관리, 운영 효율화, 동기 부여  
**기술 스택**: Java 17, Spring Boot 3.4.5, MySQL 8.0, JWT 인증  
**아키텍처**: 헥사고날 아키텍처 (Ports & Adapters)

## ⚡ 빠른 시작

```bash
# 1. 데이터베이스 실행
docker-compose up -d

# 2. 애플리케이션 실행 (rankus 디렉토리에서)
cd rankus && ./gradlew bootRun

# 3. API 문서 확인
# http://localhost:8080/swagger-ui.html
```

## 📊 구현 현황 (약 90%)

**✅ 완료**: 회원 관리, 랩실 홍보, 지원 시스템, 공지사항, 면접, 랭킹, QR 출석 시스템, 랩실 멤버 관리, 통계 및 내보내기  
**🔄 진행 중**: 캘린더, 알림

**🆕 최근 추가**: 랩실 멤버 역할 관리, 벌크 출석 업데이트, 랩실 통계 시스템, 리더십 이양 기능

## 📋 AI 개발 가이드

- **🎯 핵심 패턴**: `AI-GUIDE.md` (우선 참조)
- **📚 상세 참조**: `REFERENCE.md`
- **🧪 테스트 패턴**: `@core/test-patterns.md`

## 🛠️ 개발 환경

**Spring Profiles**:
- 기본: JWT 보안 활성화, SQL 로깅
- `dev`: 보안 비활성화, 빠른 개발
- `aws`: 프로덕션 배포

**중요**: 모든 Gradle 명령은 `rankus` 디렉토리에서 실행