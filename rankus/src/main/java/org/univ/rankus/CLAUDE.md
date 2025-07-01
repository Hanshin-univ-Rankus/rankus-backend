# Rankus 소스코드 아키텍처 가이드

> 헥사고날 아키텍처(Ports & Adapters) 기반 계층형 구조
> 📫 **핵심 패턴**: @core/patterns.md  
> 🏷️ **네이밍**: @core/conventions.md  
> 🧪 **테스트**: @core/testing.md

## 🏗️ 아키텍처 원칙

### 의존성 매트릭스

| 원칙           | 설명                             | 구현                       |
|--------------|--------------------------------|--------------------------|
| 의존성 역전       | Domain ← Application ← Adapter | 내부 → 외부 의존               |
| Port/Adapter | 인터페이스 계층 분리                    | UseCase + RepositoryPort |
| 도메인 중심       | 비즈니스 로직 독립                     | 외부 기술 비의존                |
| 단일 책임        | 각 계층 명확한 역할                    | 계층별 엄격한 경계               |

### 계층별 의존성

```
Adapter (Spring, JPA, HTTP) ← 외부 기술 연동
│
Application (UseCase, Service) ← 비즈니스 플로우
│
Domain (Entity, ValueObject) ← 순수 비즈니스 로직
```

## 📁 계층별 책임 매트릭스

| 계층          | 주요 컴포넌트                         | 역할         | 의존성              |
|-------------|---------------------------------|------------|------------------|
| Domain      | Entity, VO, Exception           | 순수 비즈니스 로직 | 없음               |
| Application | UseCase, Service, Port          | 유스케이스 구현   | Domain만          |
| Adapter     | Controller, Repository          | 외부 기술 연동   | App + Domain     |
| Common      | Exception, Security, BaseEntity | 공통 컴포넌트    | 모든 계층            |
| Config      | SecurityConfig, DatabaseConfig  | Bean 설정    | Spring Framework |

### 주요 패키지 구조

```
domain/{Domain}/           # Entity, VO, Exception
application/
 ├── port/in/             # Inbound 포트 (UseCase)
 ├── port/out/            # Outbound 포트 (Repository)
 └── service/             # 서비스 구현
adapter/
 ├── in/web/              # REST API
 └── out/persistence/     # JPA Repository
common/                     # 공통 컴포넌트
config/                     # Spring 설정
```

## 🔗 데이터 플로우 매트릭스

### Inbound (외부 → 내부)

| 단계 | 컴포넌트                  | 역할              |
|----|-----------------------|-----------------|
| 1  | HTTP Request          | 클라이언트 요청        |
| 2  | Controller (Adapter)  | DTO → Domain 변환 |
| 3  | UseCase (Application) | 비즈니스 로직 수행      |
| 4  | Domain Entity         | 도메인 규칙 적용       |

### Outbound (내부 → 외부)

| 단계 | 컴포넌트                  | 역할       |
|----|-----------------------|----------|
| 1  | Service (Application) | 비즈니스 로직  |
| 2  | Repository Port       | 인터페이스 정의 |
| 3  | Repository Adapter    | JPA 구현   |
| 4  | Database              | 데이터 저장   |

## 🏷️ 네이밍 매트릭스

> 🏷️ **상세 네이밍**: @core/conventions.md

| 컴포넌트 타입    | 네이밍 패턴                            | 예시                      |
|------------|-----------------------------------|-------------------------|
| UseCase    | `{Domain}{Command\|Query}UseCase` | `UserCommandUseCase`    |
| Port       | `{Domain}RepositoryPort`          | `UserRepositoryPort`    |
| Service    | `{Domain}{Command\|Query}Service` | `UserCommandService`    |
| Controller | `{Domain}Controller`              | `UserController`        |
| Adapter    | `{Domain}RepositoryAdapter`       | `UserRepositoryAdapter` |
| Exception  | `{Domain}{Specific}Exception`     | `UserNotFoundException` |
| ErrorCode  | `{Domain}ErrorCode`               | `UserErrorCode`         |

## 🎯 계층별 가이드

| 계층          | 가이드 링크                 | 핵심 내용                           |
|-------------|------------------------|---------------------------------|
| Domain      | @domain/CLAUDE.md      | Entity, VO, Exception           |
| Application | @application/CLAUDE.md | UseCase, Service, Port          |
| Adapter     | @adapter/CLAUDE.md     | Controller, Repository          |
| Common      | @common/CLAUDE.md      | BaseEntity, Security, Exception |
| Config      | @config/CLAUDE.md      | Spring Bean, Profile            |

## 🛡️ 아키텍처 제약사항

### 의존성 규칙 매트릭스

| 계층          | 허용 의존성                      | 금지 의존성               |
|-------------|-----------------------------|----------------------|
| Domain      | 없음                          | Application, Adapter |
| Application | Domain, Common              | Adapter              |
| Adapter     | Application, Domain, Common | 없음                   |
| Common      | 없음 (모든 계층에서 사용)             | 없음                   |

### 금지 패턴

- ❌ Controller → Domain 직접 호출
- ❌ Domain → 외부 기술 의존
- ❌ Service → 다른 Service 직접 호출

## 📚 추가 리소스

> 📫 **아키텍처 패턴**: @core/patterns.md#hexagonal  
> 🧪 **테스트 전략**: @core/testing.md#architecture-테스트  
> 🔄 **DDD 연관**: @core/conventions.md#domain-설계

### 핵심 개념

- **포트/어댑터**: 인터페이스 계층 분리
- **의존성 역전**: 내부 → 외부 의존 역전
- **도메인 중심**: 비즈니스 로직 독립성