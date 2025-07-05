# 테스트 작성 패턴 (AI 전용)

> 프로덕션 코드와 완벽히 일치하는 테스트 작성을 위한 필수 패턴 (150줄 이하)

## 🔍 프로덕션 코드 매칭 체크리스트

### 필수 검증 항목
```
□ 클래스명: {ProductionClass}Test 정확히 매칭
□ 메서드명: 프로덕션 메서드와 1:1 대응
□ 파라미터: 실제 타입과 개수 일치
□ 예외: 실제 던지는 예외 타입 확인
□ 리턴값: 실제 반환 타입 매칭
□ 의존성: 실제 주입되는 의존성 Mock 처리
□ 애노테이션: 프로덕션 코드 애노테이션 반영
```

### 코드 동기화 검증
```java
// 1. 프로덕션 메서드 시그니처 확인
public User createUser(UserCreateRequestDto request) throws UserValidationException

// 2. 테스트 메서드 정확히 매칭
@Test
void createUser_정상_입력시_사용자_생성됨() {
    // given - 실제 DTO 타입 사용
    UserCreateRequestDto request = new UserCreateRequestDto(/* ... */);
    
    // when - 실제 메서드 호출
    User result = service.createUser(request);
    
    // then - 실제 예외 타입 검증
    assertThatThrownBy(() -> service.createUser(invalidRequest))
        .isInstanceOf(UserValidationException.class);
}
```

## 🏗️ 테스트 구조 템플릿

### Unit Test 표준 구조
```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // UnnecessaryStubbingException 방지
class {ProductionClass}Test {
    @Mock private {Dependency} dependency;
    @InjectMocks private {ProductionClass} target;
    
    @Test
    void {실제메서드명}_시_{예상결과}가_발생한다() {
        // given - 실제 파라미터 타입 사용
        
        // when - 실제 메서드 호출
        
        // then - 실제 리턴 타입 검증
    }
}
```

### Controller Test 표준 구조
```java
@WebMvcTest({ProductionController}.class)
class {ProductionController}Test {
    @Autowired private MockMvc mockMvc;
    @MockBean private {ActualUseCase} useCase;  // 실제 주입되는 UseCase
    
    @Test
    @WithMockUser(roles = "USER")  // 실제 권한 설정과 매칭
    void {실제엔드포인트}_호출_테스트() throws Exception {
        // given - 실제 RequestDto 사용
        
        // when & then - 실제 URL 패턴 사용
        mockMvc.perform(post("/api/{actual-endpoint}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());  // 실제 상태코드
    }
}
```

## 🎭 Mock 패턴 (프로덕션 매칭)

### MockitoExtension 베스트 프랙티스
```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // 필수: UnnecessaryStubbingException 방지
class ServiceTest {
    
    // ✅ 좋은 예: 실제 사용되는 Mock만 설정
    @Test
    void 사용자_생성_성공() {
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        User result = service.createUser(request);
        
        verify(userRepository).save(any(User.class));  // 실제 호출 검증
    }
    
    // ❌ 나쁜 예: 사용되지 않는 Mock 설정
    @Test
    void 사용자_생성_실패() {
        when(userRepository.save(any(User.class))).thenReturn(savedUser);      // 사용안됨
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty()); // 사용안됨
        
        assertThatThrownBy(() -> service.createUser(invalidRequest))
            .isInstanceOf(ValidationException.class);
        // 위의 Mock들은 실제로 호출되지 않음 → UnnecessaryStubbingException
    }
}
```

### Repository Mock
```java
@Mock private {ActualRepository}Port repository;

// 실제 메서드 시그니처 확인 후 Mock 설정
when(repository.save(any({ActualEntity}.class))).thenReturn(savedEntity);
when(repository.findById(1L)).thenReturn(Optional.of(entity));
when(repository.findByEmail("test@email.com")).thenReturn(Optional.empty());
```

### UseCase Mock
```java
@MockBean private {ActualUseCase} useCase;

// 실제 UseCase 메서드와 정확히 매칭
when(useCase.createUser(any(UserCreateRequestDto.class)))
    .thenReturn(expectedResponseDto);
```

## ⚠️ 예외 테스트 (실제 예외 매칭)

### 예외 발생 검증
```java
@Test
void {실제조건}_시_{실제예외클래스명}_발생() {
    // given - 실제 실패 조건 재현
    
    // when & then - 실제 예외 클래스 검증
    assertThatThrownBy(() -> service.actualMethod(invalidInput))
        .isInstanceOf({ActualException}.class)
        .hasMessage("{실제메시지}");
}
```

### ErrorCode 매칭 검증
```java
@Test
void {실제검증조건}_시_{실제ErrorCode}_반환() {
    // given
    
    // when
    {ActualException} exception = assertThrows({ActualException}.class, 
        () -> service.actualMethod(invalidInput));
    
    // then - 실제 ErrorCode 확인 (실제 Enum 값과 매칭)
    assertThat(exception.getErrorCode()).isEqualTo({ActualErrorCode}.{ACTUAL_CODE});
}

// ⚠️ 주요 ErrorCode 매핑
// LabCreationRequest: LCR_XXX (LCR_006: 중복, LCR_007: 상태변경불가, LCR_009: 미존재)
// LabNotice: LNT_XXX (LNT_404: 미존재)
// User: USER_XXX
// Lab: LAB_XXX
```

## 📊 검증 패턴 (실제 상태 매칭)

### 상태 검증
```java
// 실제 Entity 상태 확인
assertThat(result.getStatus()).isEqualTo({ActualStatus}.{ACTUAL_VALUE});
assertThat(result.getName()).isEqualTo(expectedName);
```

### Mock 호출 검증
```java
// 실제 호출되는 메서드 검증
verify(repository).save(any({ActualEntity}.class));
verify(repository, times(1)).findById(1L);
verify(repository, never()).delete(any());
```

## 🔐 Security 테스트 (실제 권한 매칭)

### 권한 테스트
```java
@Test
@WithMockUser(roles = "ADMIN")  // 실제 권한 설정
void {실제권한}_사용자_접근_허용() throws Exception {
    mockMvc.perform(get("/api/{actual-endpoint}"))
        .andExpect(status().isOk());
}

@Test
@WithMockUser(roles = "USER")
void {실제권한}_사용자_접근_거부() throws Exception {
    mockMvc.perform(get("/api/{restricted-endpoint}"))
        .andExpect(status().isForbidden());  // 실제 상태코드
}
```

## 🎯 데이터 생성 패턴 (실제 구조 매칭)

### 테스트 데이터 팩토리
```java
public class {ActualEntity}TestFactory {
    public static {ActualEntity} create{ActualEntity}() {
        return {ActualEntity}.create(
            "validName",      // 실제 검증 규칙 통과하는 값
            "test@email.com", // 실제 이메일 형식
            "validPassword"   // 실제 비밀번호 규칙 통과하는 값
        );
    }
    
    public static {ActualEntity} createInvalid{ActualEntity}() {
        return {ActualEntity}.create(
            "",              // 실제 검증 실패 케이스
            "invalid-email", // 실제 이메일 검증 실패 케이스
            "123"            // 실제 비밀번호 검증 실패 케이스
        );
    }
}
```

## 🚀 Integration Test 패턴

### 통합 테스트 구조
```java
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class {ActualService}IntegrationTest {
    @Autowired private {ActualService} service;
    @Autowired private {ActualRepository}Port repository;
    
    @Test
    void {실제시나리오}_통합_테스트() {
        // given - 실제 데이터베이스 상태 설정
        
        // when - 실제 서비스 호출
        
        // then - 실제 저장 상태 검증
    }
}
```

---

**업데이트**: 2025-01-04 | **라인 수**: 148줄 | **목적**: 프로덕션 코드 불일치 문제 해결