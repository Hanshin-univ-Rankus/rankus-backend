# Common Layer 가이드

> 📋 **예외 체계**: @common/exception/CONVENTIONS.md  
> 🔐 **보안 설정**: @common/security/CONVENTIONS.md  
> 🎯 **공통 패턴**: @core/conventions.md#common-컴포넌트  
> 📋 **표준 템플릿**: @core/templates.md

## 🛠️ 핵심 구조

### 책임 매트릭스
| 컴포넌트 | 역할 | 구현체 |
|----------|------|--------|
| BaseTimeEntity | JPA Auditing | 생성/수정 시간 자동 관리 |
| Exception | 전역 예외 처리 | ErrorCode + ErrorResponse |
| Security | 인증/인가 | JWT + Permission 평가 |
| Utility | 공통 헬퍼 | SecurityUtils, ValidationUtils |

## 🏗️ 기본 템플릿

### BaseTimeEntity

> 📋 **표준 템플릿**: @core/templates.md#basetimeentity

### Exception 체계

> 📋 **상세 구현**: @common/exception/CONVENTIONS.md

> 📋 **표준 템플릿**: @core/templates.md#basecustomexception  
> 📋 **ErrorCode**: @core/templates.md#errorcode-enum  
> 📋 **GlobalExceptionHandler**: @core/templates.md#globalexceptionhandler

### Security 컴포넌트

> 🔐 **공통 Security**: @common/security/CONVENTIONS.md  
> 🎫 **JWT 구현**: @common/security/jwt/CONVENTIONS.md  
> 🛡️ **권한 관리**: @common/security/permission/CONVENTIONS.md  
> 👤 **사용자 인증**: @common/security/customUser/CONVENTIONS.md

### 예외 처리 플로우
```
Domain Exception → Application → Adapter → HTTP Response
비즈니스 규칙 위반 → 유스케이스 실패 → HTTP 변환 → 클라이언트 응답
```

### 로깅 전략 매트릭스
| 상태코드 | 로그 레벨 | 예외 타입 |
|----------|-----------|----------|
| 500번대 | ERROR | 시스템 오류 |
| 400번대 | WARN | 비즈니스 예외 |
| 2xx | INFO | 정상 처리 |

## 🧪 테스트 패턴

> 🧪 **테스트 가이드**: @core/testing.md#common-테스트

> 🧪 **테스트 템플릿**: @core/templates.md#jwt-테스트

## 🎯 주요 규칙 요약

1. **BaseTimeEntity**: 모든 엔티티 상속, JPA Auditing 활성화
2. **예외 계층**: BaseCustomException → 도메인 예외 → 구체적 예외
3. **ErrorCode**: 상태코드 + 메시지 표준화
4. **Security**: JWT + Permission 평가자 통합
5. **테스트**: Mock 활용한 단위 테스트 작성