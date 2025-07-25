# 🚨 예외 처리 리팩터링 가이드

> 코드 리팩터링 시 예외 처리에서 발생하는 테스트 실패를 방지하기 위한 체크리스트

## ⚠️ 리팩터링 실패 원인 분석

### 1. 예외 타입 변경으로 인한 테스트 실패

```java
// 변경 전: 일반적인 예외 사용
throw new IllegalArgumentException("Lab not found");
throw new IllegalStateException("Permission denied");

// 변경 후: 도메인 특화 예외 사용  
throw new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND);
throw new LabPermissionException(LabErrorCode.LAB_PERMISSION_DENIED);
```

### 2. Import 문 누락

```java
// ❌ 새로운 예외 클래스 import 누락
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.lab.exception.LabValidationException;
```

### 3. 실제 발생한 컴파일 오류 사례 (2025-07-25)

#### 🚨 사례 1: ErrorCode 인터페이스 구현 누락

```java
// ❌ 컴파일 오류 발생
public enum LabResourceErrorCode implements ErrorCode {
    RESOURCE_NOT_FOUND("RESOURCE_001", "자료를 찾을 수 없습니다");
    // getStatus() 메서드 구현 누락!
}

// ✅ 해결: 모든 메서드 구현
public enum LabResourceErrorCode implements ErrorCode {
    RESOURCE_NOT_FOUND("RESOURCE_001", HttpStatus.NOT_FOUND, "자료를 찾을 수 없습니다");
    
    @Override public String getCode() { return code; }
    @Override public HttpStatus getStatus() { return status; }  // ← 필수!
    @Override public String getMessage() { return message; }
}
```

#### 🚨 사례 2: BaseCustomException 생성자 오류

```java
// ❌ 존재하지 않는 생성자 사용 - 컴파일 오류
.orElseThrow(() -> new LabResourceNotFoundException(
    LabResourceErrorCode.RESOURCE_NOT_FOUND, "추가 메시지"));

// ✅ 해결: 올바른 생성자 사용
.orElseThrow(() -> new LabResourceNotFoundException(
    LabResourceErrorCode.RESOURCE_NOT_FOUND));
```

#### 🚨 사례 3: CustomUserDetails 잘못된 접근

```java
// ❌ 존재하지 않는 메서드 - 컴파일 오류
User user = currentUser.getUser();

// ✅ 해결: UserRepositoryPort를 통한 조회
User user = userRepositoryPort.findById(currentUser.getUserId())
        .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
```

#### 🚨 사례 4: PageResponse 사용법 오류

```java
// ❌ Function 매개변수 누락 - 컴파일 오류
PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(resourcePage);

// ✅ 해결: Function 매개변수 제공
PageResponse<LabResourceResponseDto> pageResponse = PageResponse.of(resourcePage, dto -> dto);
```

#### 🚨 사례 5: Builder 패턴 초기값 무시

```java
// ❌ @Builder.Default 누락 - 런타임에 의도와 다른 값
@Builder
public class LabResourceCreateRequestDto {
    private Boolean isPublic = true; // Builder 사용 시 무시됨
}

// ✅ 해결: @Builder.Default 추가
@Builder
public class LabResourceCreateRequestDto {
    @Builder.Default
    private Boolean isPublic = true; // 정상 작동
}
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

## 🔄 리팩터링 시 권장 순서 (실제 경험 기반)

1. **ErrorCode 인터페이스 정의부터**: 모든 메서드 구현 확인
2. **예외 클래스 생성**: BaseCustomException 생성자 규칙 준수
3. **서비스 계층 import 문 일괄 추가**: 컴파일 오류 방지
4. **한 서비스씩 점진적 리팩터링**: 전체를 한 번에 변경하지 말 것
5. **컴파일 검증**: `./gradlew compileJava` 즉시 실행
6. **테스트 컴파일 검증**: `./gradlew compileTestJava` 즉시 실행
7. **테스트 실패 시 즉시 수정**: 다음 단계로 넘어가지 말 것

## 🚨 실전 교훈 (2025-07-25 경험)

### 🎯 핵심 원칙 업데이트

> **"컴파일부터 성공시켜라 - 테스트는 그 다음이다"**

**Phase 1**: 컴파일 성공

- ErrorCode 인터페이스 모든 메서드 구현
- BaseCustomException 생성자 규칙 준수
- Import 문 완전성 확인
- PageResponse 사용법 정확성 확인
- Builder 패턴 @Builder.Default 확인

**Phase 2**: 테스트 수정

- 서비스 코드 변경 → 해당 테스트 코드 즉시 업데이트
- Import 문 변경 → 테스트 Import 문도 함께 업데이트
- 예외 타입 변경 → 테스트 예외 검증도 함께 변경

### 🔍 필수 검증 명령어

```bash
# 1. 컴파일 검증 (최우선)
./gradlew compileJava

# 2. 테스트 컴파일 검증  
./gradlew compileTestJava

# 3. 특정 테스트 실행 (선택적)
./gradlew test --tests="*특정클래스*"
```

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