# 🚨 예외 처리 리팩터링 가이드

> 코드 리팩터링 시 예외 처리에서 발생하는 테스트 실패를 방지하기 위한 체크리스트

## ⚠️ 리팩터링 실패 원인 분석

### 1. 예외 타입 변경으로 인한 테스트 실패

```java
// 변경 전: 일반적인 예외 사용
throw new IllegalArgumentException("Lab not found");
throw new

IllegalStateException("Permission denied");

// 변경 후: 도메인 특화 예외 사용  
throw new

LabNotFoundException(LabErrorCode.LAB_NOT_FOUND);
throw new

LabPermissionException(LabErrorCode.LAB_PERMISSION_DENIED);
```

### 2. Import 문 누락

```java
// ❌ 새로운 예외 클래스 import 누락

import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.lab.exception.LabValidationException;
```

## 🛠️ 리팩터링 필수 체크리스트

### Phase 1: 예외 클래스 정의

- [ ] 도메인별 ErrorCode enum 정의 및 확장
- [ ] 도메인 특화 예외 클래스 생성
- [ ] BaseCustomException 상속 구조 확인

### Phase 2: 서비스 계층 리팩터링

- [ ] ✅ **모든 관련 import 문 한 번에 추가**
- [ ] 기존 `IllegalArgumentException` → 도메인 예외로 변경
- [ ] 기존 `IllegalStateException` → 도메인 예외로 변경
- [ ] 예외 메시지를 ErrorCode로 표준화

### Phase 3: 테스트 코드 업데이트 (중요!)

- [ ] ⚠️ **테스트에서 예상 예외 타입 변경**
- [ ] 테스트 import 문 업데이트
- [ ] 예외 메시지 검증 로직 수정
- [ ] Mock 설정에서 예외 타입 변경

### Phase 4: 컨트롤러 계층 확인

- [ ] @ExceptionHandler 설정 확인
- [ ] ApiResponse 래퍼 적용 확인

## 🧪 테스트 실패 방지 패턴

### 1. 예외 검증 테스트 업데이트

```java
// ❌ 변경 전
@Test
void 권한_없으면_예외발생() {
    assertThrows(IllegalStateException.class,
            () -> service.getLabMembers(labId, userId));
}

// ✅ 변경 후  
@Test
void 권한_없으면_예외발생() {
    assertThrows(LabPermissionException.class,
            () -> service.getLabMembers(labId, userId));
}
```

### 2. Mock 설정 업데이트

```java
// ❌ 변경 전
when(userRepositoryPort.findById(anyLong()))
        .

thenThrow(new IllegalArgumentException("User not found"));

// ✅ 변경 후
when(userRepositoryPort.findById(anyLong()))
        .

thenThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));
```

## 🔄 리팩터링 시 권장 순서

1. **예외 클래스부터 정의**: ErrorCode → Exception 클래스
2. **한 서비스씩 점진적 리팩터링**: 전체를 한 번에 변경하지 말 것
3. **리팩터링 후 즉시 테스트 실행**: 다른 서비스 변경 전에 테스트 확인
4. **테스트 실패 시 즉시 수정**: 다음 단계로 넘어가지 말 것

## 🎯 핵심 원칙

> **"코드 변경과 테스트 업데이트를 함께 진행하라"**

- 서비스 코드 변경 → 해당 테스트 코드 즉시 업데이트
- Import 문 변경 → 테스트 Import 문도 함께 업데이트
- 예외 타입 변경 → 테스트 예외 검증도 함께 변경

## 📝 체크리스트 템플릿

```markdown
### 리팩터링 전 확인사항

- [ ] 변경할 예외 타입 목록 작성
- [ ] 영향받는 테스트 파일 목록 작성
- [ ] 새로운 Import 문 목록 작성

### 리팩터링 후 확인사항

- [ ] 모든 컴파일 에러 해결
- [ ] 모든 테스트 통과
- [ ] 새로운 예외 타입으로 올바르게 변경
- [ ] ErrorCode 메시지 일관성 확인
```