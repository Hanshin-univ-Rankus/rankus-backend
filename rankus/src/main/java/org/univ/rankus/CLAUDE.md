# Rankus 소스코드 아키텍처 가이드

> 헥사고날 아키텍처(Ports & Adapters)를 기반으로 한 계층형 구조 설명

## 🏗️ 아키텍처 개요

### 핵심 원칙
- **의존성 역전**: Domain ← Application ← Adapter
- **포트/어댑터 패턴**: 인터페이스를 통한 계층 분리
- **도메인 중심 설계**: 비즈니스 로직의 외부 기술 독립성
- **단일 책임**: 각 계층은 명확한 역할 분담

### 계층별 의존성 방향
```
┌─────────────────────────────────────────┐
│             Adapter Layer               │ ← 외부 기술 (Spring, JPA, HTTP)
│  ┌─────────────────────────────────┐    │
│  │        Application Layer        │    │ ← 유스케이스 구현
│  │   ┌─────────────────────────┐   │    │
│  │   │      Domain Layer       │   │    │ ← 순수 비즈니스 로직
│  │   └─────────────────────────┘   │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

## 📁 패키지 구조 및 역할

### Domain Layer (`domain/`)
- **순수한 비즈니스 로직**, 외부 프레임워크 의존성 없음
- **Entity**: 비즈니스 규칙과 도메인 로직 포함
- **Value Object**: 불변 객체로 도메인 개념 표현
- **Domain Exception**: 비즈니스 규칙 위반시 발생

### Application Layer (`application/`)
- **유스케이스 구현**, 도메인 객체를 조합하여 비즈니스 플로우 처리
- **Port Interface**: 외부 계층과의 통신 인터페이스
  - `port.in`: Inbound 포트 (컨트롤러 → 서비스)
  - `port.out`: Outbound 포트 (서비스 → 인프라)
- **Service**: 포트 인터페이스 구현, 트랜잭션 경계

### Adapter Layer (`adapter/`)
- **외부 기술과의 연동**, 포트 인터페이스 구현
- **Inbound Adapter** (`in/`): 외부 요청을 애플리케이션으로 전달
  - `web`: REST API 컨트롤러, DTO
- **Outbound Adapter** (`out/`): 애플리케이션에서 외부 시스템 호출
  - `persistence`: 데이터베이스 연동 (JPA)

### Common Layer (`common/`)
- **공통 컴포넌트**, 모든 계층에서 사용 가능
- **BaseTimeEntity**: JPA Auditing 베이스 클래스
- **Exception Handling**: 전역 예외 처리
- **Security**: 인증/인가 설정

### Config Layer (`config/`)
- **설정 클래스**, Spring Bean 설정 및 환경 구성

## 🔗 계층별 상호작용 패턴

### 1. Inbound Flow (외부 → 내부)
```
HTTP Request → Controller (Adapter)
             ↓
           UseCase (Application Port)
             ↓
           Service (Application)
             ↓
           Domain Entity (Domain)
```

### 2. Outbound Flow (내부 → 외부)
```
Service (Application)
   ↓
Repository Port (Application)
   ↓
Repository Adapter (Adapter)
   ↓
JPA Repository (Infrastructure)
```

## 📋 네이밍 컨벤션

### 인터페이스 네이밍
- **UseCase**: `{Domain}{Command|Query}UseCase`
  - 예: `UserCommandUseCase`, `LabQueryUseCase`
- **Port**: `{Domain}RepositoryPort`, `AuthTokenPort`
- **Service**: `{Domain}{Command|Query}Service`

### 구현체 네이밍
- **Controller**: `{Domain}Controller`
- **Service**: `{Domain}{Command|Query}Service`
- **Adapter**: `{Domain}RepositoryAdapter`
- **Repository**: `SpringData{Domain}Repository`

### 예외 네이밍
- **Exception**: `{Domain}{Specific}Exception`
- **ErrorCode**: `{Domain}ErrorCode`

## 🎯 각 계층별 상세 가이드

각 계층의 구체적인 구현 방법과 컨벤션은 해당 계층의 CLAUDE.md 파일을 참조하세요:

- **Domain Layer**: `@domain/CLAUDE.md`
- **Application Layer**: `@application/CLAUDE.md`
- **Adapter Layer**: `@adapter/CLAUDE.md`
- **Common Components**: `@common/CLAUDE.md`
- **Configuration**: `@config/CLAUDE.md`

## 🛡️ 아키텍처 제약사항

### 의존성 규칙
1. **Domain**은 어떤 계층도 의존하지 않음
2. **Application**은 Domain만 의존
3. **Adapter**는 Application과 Domain 의존 가능
4. **Common**은 모든 계층에서 사용 가능

### 허용되지 않는 의존성
- ❌ Domain → Application/Adapter
- ❌ Application → Adapter
- ❌ 계층 건너뛰기 (Controller → Domain 직접 호출)

## 📚 추가 리소스

### 헥사고날 아키텍처 관련
- 포트/어댑터 패턴의 장점과 구현 방법
- 의존성 역전 원칙(DIP) 적용 사례
- 도메인 주도 설계(DDD)와의 연관성

### 코드 품질 가이드
- 각 계층별 단위 테스트 작성법
- Mock 객체 활용 전략
- 통합 테스트 설계 방법