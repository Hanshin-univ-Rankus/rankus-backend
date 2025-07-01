# Command Service 컨벤션

> 📋 **공통 패턴**: @core/patterns.md#service-템플릿  
> 📝 **네이밍 규칙**: @core/conventions.md#service-메서드

## 특화 규칙

### 클래스 네이밍

- **패턴**: `{Domain}CommandService`
- **구현**: `{Domain}CommandUseCase`

### 메서드 플로우

| 작업   | 플로우             | 트랜잭션 |
|------|-----------------|------|
| 생성   | 검증→생성→저장→응답     | ✅    |
| 수정   | 조회→수정→저장→응답     | ✅    |
| 삭제   | 조회→검증→삭제        | ✅    |
| 상태변경 | 조회→비즈니스로직→저장→응답 | ✅    |

## 반환값 규칙

- **Domain Entity 반환**: 모든 public 메서드는 Domain Entity 또는 Value Object를 반환한다
- **DTO 변환 위치**: Controller 계층에서 Entity → DTO 변환을 담당한다
- **매개변수 처리**: DTO를 매개변수로 받을 경우 Service 내부에서 Entity로 변환한다
- **Import 규칙**: Wildcard import 금지, explicit import 사용

### 구현 원칙

1. `@Override` 필수, 도메인 중심 로직
2. 검증은 도메인 계층 위임
3. Repository Port 통한 영속화
4. 각 메서드에 `@Transactional` 어노테이션

### 예외 처리

- 도메인 예외 → 그대로 전파
- 비즈니스 예외 → 도메인 예외 변환

### 테스트

> 📋 **테스트 패턴**: @core/testing.md#unit-test-템플릿

- Mock: Repository Port
- 검증: 도메인 로직 + Repository 호출