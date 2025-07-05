# 공통 테스트 패턴 (상세 가이드)

> 📋 **AI 최적화 가이드**: @core/test-patterns.md (AI 개발자 우선 참조)  
> 🔍 **프로덕션 매칭**: @core/test-patterns.md (코드 불일치 문제 해결)

## 🧪 테스트 구조 템플릿

### Unit Test 템플릿

```java
@ExtendWith(MockitoExtension.class)
class {Class}Test {
    @Mock private {Dependency} dependency;
    @InjectMocks private {Class} target;
    
    @Test
    void {행위}_시_{결과}가_발생한다() {
        // given
        
        // when
        
        // then
    }
}
```

### Integration Test 템플릿

```java
@SpringBootTest
@Transactional
class {Class}IntegrationTest {
    @Autowired private {Class} target;
    
    @Test
    void {시나리오}_통합_테스트() {
        // given
        
        // when
        
        // then
    }
}
```

### Controller Test 템플릿

```java
@WebMvcTest({Class}Controller.class)
class {Class}ControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private {Class}UseCase useCase;
    
    @Test
    @WithMockUser(roles = "USER")
    void {API}_호출_테스트() throws Exception {
        // given
        
        // when & then
        mockMvc.perform(post("/api/endpoint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
```

## 🏭 테스트 팩토리 패턴

### Domain 객체 팩토리

```java
public class {Domain}TestFactory {
    public static {Domain} create{Domain}() {
        return {Domain}.create(/* 기본값들 */);
    }
    
    public static {Domain} create{Domain}(String customField) {
        return {Domain}.create(customField, /* 기본값들 */);
    }
}
```

### Builder 패턴

```java
public class {Domain}TestBuilder {
    private String field1 = "기본값";
    private String field2 = "기본값";
    
    public {Domain}TestBuilder field1(String value) { this.field1 = value; return this; }
    public {Domain}TestBuilder field2(String value) { this.field2 = value; return this; }
    
    public {Domain} build() { return {Domain}.create(field1, field2); }
}
```

## 🎭 Mock 활용 패턴

### Repository Mock

```java
@Mock private {Domain}RepositoryPort repository;

// 성공 케이스
when(repository.findById(1L)).thenReturn(Optional.of(domain));
when(repository.save(any({Domain}.class))).thenReturn(savedDomain);

// 실패 케이스  
when(repository.findById(999L)).thenReturn(Optional.empty());
```

### UseCase Mock

```java
@MockBean private {Domain}CommandUseCase useCase;

when(useCase.create{Domain}(any())).thenReturn(responseDto);
```

## 📋 테스트 케이스 패턴

| 테스트 타입  | 명명 패턴               | 예시                     |
|---------|---------------------|------------------------|
| 성공 케이스  | `{행위}_시_{결과}가_발생한다` | `사용자_생성시_정상적으로_저장된다`   |
| 실패 케이스  | `{조건}_시_{예외}가_발생한다` | `잘못된_이메일_시_검증예외가_발생한다` |
| 경계값     | `{경계조건}_테스트`        | `이메일_길이_100자_경계값_테스트`  |
| 비즈니스 로직 | `{비즈니스_규칙}_검증`      | `중복_이메일_가입_차단_검증`      |

## ⚠️ 예외 테스트 패턴

### 예외 발생 검증

```java
@Test
void 존재하지_않는_사용자_조회시_예외_발생() {
    // given
    when(repository.findById(999L)).thenReturn(Optional.empty());
    
    // when & then
    assertThatThrownBy(() -> service.findById(999L))
        .isInstanceOf(UserNotFoundException.class);
}
```

### 예외 메시지 검증

```java
@Test
void 검증_실패시_적절한_메시지_반환() {
    // when & then
    assertThatThrownBy(() -> service.createUser(invalidRequest))
        .isInstanceOf(UserValidationException.class)
        .hasMessage("이메일은 필수입니다");
}
```

## 🔐 Security 테스트 패턴

### 인증 테스트

```java
@Test
@WithMockUser(roles = "USER")
void 인증된_사용자_접근_허용() throws Exception {
    mockMvc.perform(get("/api/protected"))
        .andExpect(status().isOk());
}

@Test
void 미인증_사용자_접근_거부() throws Exception {
    mockMvc.perform(get("/api/protected"))
        .andExpect(status().isUnauthorized());
}
```

### 권한 테스트

```java
@Test
@WithMockUser(roles = "ADMIN")
void 관리자_권한_접근_허용() { /* 테스트 */ }

@Test
@WithMockUser(roles = "USER")  
void 일반_사용자_권한_접근_거부() { /* 테스트 */ }
```

## 📊 검증 패턴

### 상태 검증

```java
assertThat(result.getStatus()).isEqualTo(APPROVED);
assertThat(result.getName()).isEqualTo("예상값");
```

### Mock 호출 검증

```java
verify(repository).save(any(User.class));
verify(repository, times(1)).findById(1L);
verify(repository, never()).delete(any());
```

### 컬렉션 검증

```java
assertThat(results).hasSize(3);
assertThat(results).extracting("name").contains("홍길동", "김철수");
```