# Command UseCase 컨벤션

## 📛 네이밍
| 구분 | 패턴 | 예시 |
|------|------|------|
| Interface | `{Domain}CommandUseCase` | `UserCommandUseCase` |
| 생성 | `create{Resource}()` | `createUser()`, `createLab()` |
| 수정 | `update{Resource}()` | `updateUser()`, `updateLabInfo()` |
| 삭제 | `delete{Resource}()` | `deleteUser()`, `deleteLab()` |
| 상태변경 | `{action}{Resource}()` | `approveApplication()` |
| 관계변경 | `assign{Resource}()`, `remove{Resource}()` | `assignUserToLab()` |

## 🏗️ 인터페이스 구조

### 기본 패턴
```java
public interface {Domain}CommandUseCase {
    {Domain}ResponseDto create{Domain}({Domain}CreateRequestDto request);
    {Domain}ResponseDto update{Domain}(Long id, {Domain}UpdateRequestDto request);
    void delete{Domain}(Long id);
    {Domain}ResponseDto {action}{Domain}(Long id);  // 상태변경
}
```

### 실제 구현 예시
```java
public interface UserCommandUseCase {
    
    /**
     * 새로운 사용자를 생성합니다.
     * 
     * @param request 사용자 생성 요청 데이터
     * @return 생성된 사용자 정보
     * @throws UserValidationException 사용자 데이터가 유효하지 않은 경우
     * @throws DuplicateEmailException 이메일이 이미 존재하는 경우
     */
    UserResponseDto createUser(UserCreateRequestDto request);
    
    /**
     * 기존 사용자 정보를 수정합니다.
     * 
     * @param id 수정할 사용자 ID
     * @param request 수정할 사용자 데이터
     * @return 수정된 사용자 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws UserValidationException 수정 데이터가 유효하지 않은 경우
     */
    UserResponseDto updateUser(Long id, UserUpdateRequestDto request);
    
    /**
     * 사용자를 삭제합니다.
     * 
     * @param id 삭제할 사용자 ID
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws UserDeletionException 삭제할 수 없는 상태인 경우
     */
    void deleteUser(Long id);
    
    /**
     * 사용자 비밀번호를 변경합니다.
     * 
     * @param id 사용자 ID
     * @param request 비밀번호 변경 요청 데이터
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws InvalidPasswordException 현재 비밀번호가 일치하지 않는 경우
     */
    void changePassword(Long id, ChangePasswordRequestDto request);
}
```

### 복잡한 Command UseCase 구조
```java
public interface LabApplicationCommandUseCase {
    
    /**
     * 랩실에 지원서를 제출합니다.
     * 
     * @param labId 지원할 랩실 ID
     * @param request 지원서 데이터
     * @return 생성된 지원서 정보
     * @throws LabNotFoundException 랩실을 찾을 수 없는 경우
     * @throws DuplicateApplicationException 이미 지원한 랩실인 경우
     * @throws ApplicationPeriodException 지원 기간이 아닌 경우
     */
    LabApplicationResponseDto applyToLab(Long labId, LabApplicationRequestDto request);
    
    /**
     * 지원서를 승인합니다.
     * 
     * @param applicationId 승인할 지원서 ID
     * @return 승인된 지원서 정보
     * @throws LabApplicationNotFoundException 지원서를 찾을 수 없는 경우
     * @throws InvalidApplicationStatusException 승인할 수 없는 상태인 경우
     * @throws InsufficientPermissionException 승인 권한이 없는 경우
     */
    LabApplicationResponseDto approveApplication(Long applicationId);
    
    /**
     * 지원서를 거부합니다.
     * 
     * @param applicationId 거부할 지원서 ID
     * @param reason 거부 사유 (선택적)
     * @return 거부된 지원서 정보
     */
    LabApplicationResponseDto rejectApplication(Long applicationId, String reason);
    
    /**
     * 지원서를 취소합니다.
     * 
     * @param applicationId 취소할 지원서 ID
     * @throws LabApplicationNotFoundException 지원서를 찾을 수 없는 경우
     * @throws ApplicationCancellationException 취소할 수 없는 상태인 경우
     */
    void cancelApplication(Long applicationId);
    
    /**
     * 지원서의 면접 시간을 변경합니다.
     * 
     * @param applicationId 지원서 ID
     * @param newInterviewTime 새로운 면접 시간
     * @return 수정된 지원서 정보
     */
    LabApplicationResponseDto rescheduleInterview(Long applicationId, LocalDateTime newInterviewTime);
}
```

## 📋 메서드 시그니처 패턴

### 생성 메서드
```java
// 기본 생성
{Domain}ResponseDto create{Domain}({Domain}CreateRequestDto request);

// 부모 리소스와 함께 생성
{Domain}ResponseDto create{Domain}(Long parentId, {Domain}CreateRequestDto request);

// 배치 생성
List<{Domain}ResponseDto> create{Domain}s(List<{Domain}CreateRequestDto> requests);

// 파일과 함께 생성
{Domain}ResponseDto create{Domain}WithFile({Domain}CreateRequestDto request, MultipartFile file);
```

### 수정 메서드
```java
// 전체 수정
{Domain}ResponseDto update{Domain}(Long id, {Domain}UpdateRequestDto request);

// 부분 수정
{Domain}ResponseDto update{Property}(Long id, {Property}UpdateRequestDto request);

// 상태 변경
{Domain}ResponseDto change{Property}(Long id, {NewPropertyType} newValue);

// 관계 변경
void assign{Related}(Long id, Long relatedId);
void remove{Related}(Long id, Long relatedId);
```

### 삭제 메서드
```java
// 단일 삭제
void delete{Domain}(Long id);

// 소프트 삭제
{Domain}ResponseDto softDelete{Domain}(Long id);

// 배치 삭제
void delete{Domain}s(List<Long> ids);

// 조건부 삭제
void delete{Domain}sByCondition({ConditionDto} condition);
```

### 액션 메서드
```java
// 승인/거부
{Domain}ResponseDto approve{Domain}(Long id);
{Domain}ResponseDto reject{Domain}(Long id);
{Domain}ResponseDto reject{Domain}(Long id, String reason);

// 활성화/비활성화
{Domain}ResponseDto activate{Domain}(Long id);
{Domain}ResponseDto deactivate{Domain}(Long id);

// 복원
{Domain}ResponseDto restore{Domain}(Long id);

// 처리
{Domain}ResponseDto process{Domain}(Long id, {ProcessRequestDto} request);
```

## 🔐 보안 및 권한 고려사항

### 권한 검증이 필요한 메서드
```java
public interface LabApplicationCommandUseCase {
    
    // 인증된 사용자만 지원 가능
    LabApplicationResponseDto applyToLab(Long labId, LabApplicationRequestDto request);
    
    // 지원자 본인만 취소 가능
    void cancelApplication(Long applicationId);
    
    // 랩 관리자만 승인/거부 가능  
    LabApplicationResponseDto approveApplication(Long applicationId);
    LabApplicationResponseDto rejectApplication(Long applicationId, String reason);
    
    // 시스템 관리자만 가능
    void forceDeleteApplication(Long applicationId);
}
```

### 권한 검증 위치
```java
// UseCase 인터페이스에는 권한 검증 로직 없음
// 실제 권한 검증은 다음 위치에서 수행:
// 1. Controller 레벨: @PreAuthorize 어노테이션
// 2. Service 레벨: 메서드 내부에서 검증
// 3. Domain 레벨: 엔티티 메서드에서 비즈니스 규칙 검증
```

## ⚠️ 예외 처리 가이드

### 예외 문서화
```java
public interface UserCommandUseCase {
    
    /**
     * 사용자를 생성합니다.
     * 
     * @param request 사용자 생성 요청
     * @return 생성된 사용자 정보
     * @throws UserValidationException 입력 데이터가 유효하지 않은 경우
     * @throws DuplicateEmailException 이메일이 이미 존재하는 경우
     * @throws LabNotFoundException 지정된 랩실이 존재하지 않는 경우 (랩실 할당시)
     */
    UserResponseDto createUser(UserCreateRequestDto request);
}
```

### 예외 계층 구조 고려
```java
// 도메인별 예외 계층
UserException
├── UserNotFoundException
├── UserValidationException
├── DuplicateEmailException
└── UserDeletionException

LabApplicationException  
├── LabApplicationNotFoundException
├── DuplicateApplicationException
├── InvalidApplicationStatusException
└── ApplicationPeriodException
```

## 🔄 트랜잭션 고려사항

### 트랜잭션 경계
```java
// UseCase 인터페이스는 트랜잭션 경계를 정의하지 않음
// 실제 트랜잭션은 Service 구현체에서 @Transactional로 처리

public interface LabApplicationCommandUseCase {
    
    // 단일 트랜잭션으로 처리되어야 하는 메서드
    LabApplicationResponseDto applyToLab(Long labId, LabApplicationRequestDto request);
    
    // 여러 작업이 포함되어 트랜잭션이 중요한 메서드
    LabApplicationResponseDto approveApplicationWithNotification(Long applicationId);
}
```

### 복합 작업 고려
```java
public interface LabApplicationCommandUseCase {
    
    // 지원 승인 + 사용자 랩실 할당 + 알림 발송
    // 모든 작업이 성공하거나 모두 롤백되어야 함
    LabApplicationResponseDto approveApplicationWithAssignment(Long applicationId);
    
    // 지원서 삭제 + 관련 파일 삭제
    // 파일 삭제 실패시에도 지원서는 삭제되어야 할지 고려
    void deleteApplicationWithFiles(Long applicationId);
}
```

## 🧪 Command UseCase 테스트 가이드

### 인터페이스 테스트 전략
```java
// Command UseCase는 인터페이스이므로 직접 테스트하지 않음
// 대신 구현체(Service)를 테스트
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {
    
    @Mock
    private UserRepositoryPort userRepositoryPort;
    
    @InjectMocks  
    private UserCommandService userCommandService; // UseCase 구현체
    
    @Test
    void 유효한_데이터로_사용자를_생성한다() {
        // UseCase 구현체 테스트
    }
}
```

### 계약 테스트 (Contract Test)
```java
// UseCase 인터페이스의 계약을 검증하는 테스트
public abstract class UserCommandUseCaseContractTest {
    
    protected abstract UserCommandUseCase getUserCommandUseCase();
    
    @Test
    void 유효하지_않은_이메일로_생성시_예외_발생() {
        // given
        UserCreateRequestDto request = invalidEmailRequest();
        
        // when & then
        assertThatThrownBy(() -> getUserCommandUseCase().createUser(request))
            .isInstanceOf(UserValidationException.class);
    }
}
```

## 🎯 주요 규칙 요약

1. **명확한 네이밍**: 도메인과 액션을 명확히 표현하는 메서드명
2. **단일 책임**: 각 메서드는 하나의 비즈니스 작업만 담당
3. **예외 문서화**: JavaDoc으로 발생 가능한 예외 명시
4. **일관된 시그니처**: 비슷한 작업은 유사한 메서드 시그니처 사용
5. **비즈니스 중심**: 기술적 세부사항보다 비즈니스 의도에 집중
6. **테스트 가능**: 구현체가 쉽게 테스트할 수 있는 인터페이스 설계