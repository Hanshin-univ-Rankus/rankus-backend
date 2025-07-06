# 테스트 패턴 (AI 코딩용)

## 🎯 Unit Test 템플릿

```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class {ProductionClass}Test {
    @Mock private {Dependency} dependency;
    @InjectMocks private {ProductionClass} target;
    
    @Test
    void {메서드명}_정상입력시_{예상결과}() {
        // given - 실제 파라미터 타입 사용
        // when - 실제 메서드 호출
        // then - 실제 리턴 타입 검증
    }
}
```

## 🎭 Controller Test 템플릿

```java
@WebMvcTest({Controller}.class)
class {Controller}Test {
    @Autowired private MockMvc mockMvc;
    @MockBean private {UseCase} useCase;
    
    @Test @WithMockUser(roles = "USER")
    void {엔드포인트}_호출_테스트() throws Exception {
        mockMvc.perform(post("/api/{endpoint}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
```

## ⚠️ ErrorCode 테스트 패턴

```java
@Test
void {검증조건}_시_{ErrorCode}_반환() {
    // when
    {Exception} exception = assertThrows({Exception}.class,
        () -> service.method(invalidInput));
    
    // then
    assertThat(exception.getErrorCode()).isEqualTo({ErrorCode}.{CODE});
}

// 주요 ErrorCode 매핑
// User: USER_001~010(입력값), USER_404(미존재), USER_409(중복)
// Lab: LAB_001~015(입력값), LAB_404(미존재), LAB_409(중복)
// LabApplication: LAP_001~012(입력값), LAP_404(미존재), LAP_422(상태변경불가)
// LabNotice: LNT_001~007(입력값), LNT_403(권한), LNT_404(미존재)
// Interview: INT_001~034(입력값/상태), INT_028~029(권한), INT_030~032(미존재)
// LabCreationRequest: LCR_006(중복), LCR_007(상태변경불가), LCR_009(미존재)
```

## 📝 다양한 도메인 테스트 예시

### Entity 테스트 패턴

```java
// 기본 Entity 생성 테스트
@Test
void {Domain}_생성_성공() {
    {Domain} entity = {Domain}.create(validParams);
    assertThat(entity.get{Field}()).isEqualTo(expectedValue);
}

// 상태 전이 테스트 (상태 관리 Entity용)
@Test
void {Domain}_상태변경_성공() {
    {Domain} entity = {Domain}.create(validParams);
    entity.approve(); // or activate(), reject() 등
    assertThat(entity.getStatus()).isEqualTo({Status}.APPROVED);
}

// 연관관계 테스트
@Test
void {Domain}_연관관계_설정_성공() {
    {Domain} entity = {Domain}.create(parentEntity, childParams);
    assertThat(entity.getParent()).isEqualTo(parentEntity);
}
```

### Controller 권한 테스트 패턴

```java
// 도메인별 권한 테스트
@Test @WithMockUser(roles = "USER")
void {Domain}_생성_권한_있음() throws Exception {
    mockMvc.perform(post("/api/{domains}")
            .content(objectMapper.writeValueAsString(request)))
            .andExpected(status().isCreated());
}

// 랩실 권한 테스트
@Test @WithMockUser(roles = "LAB_MANAGER")
void {Domain}_관리_권한_있음() throws Exception {
    mockMvc.perform(post("/api/labs/{labId}/{domains}", 1L)
            .content(objectMapper.writeValueAsString(request)))
            .andExpected(status().isCreated());
}
```

### ErrorCode 테스트 패턴

```java
// 입력값 검증 테스트
@Test
void {필드}_누락시_{PREFIX}_001_반환() {
    {Domain}Exception exception = assertThrows({Domain}Exception.class,
        () -> service.create{Domain}(invalidRequest));
    assertThat(exception.getErrorCode()).isEqualTo({Domain}ErrorCode.{FIELD}_REQUIRED);
}

// 상태 변경 불가 테스트
@Test
void 잘못된_상태에서_변경시_{PREFIX}_422_반환() {
    {Domain}Exception exception = assertThrows({Domain}Exception.class,
        () -> entity.changeStatus());
    assertThat(exception.getErrorCode()).isEqualTo({Domain}ErrorCode.CANNOT_CHANGE_STATUS);
}

// 조회 실패 테스트
@Test
void 존재하지않는_{Domain}_조회시_{PREFIX}_404_반환() {
    {Domain}Exception exception = assertThrows({Domain}Exception.class,
        () -> service.find{Domain}ById(999L));
    assertThat(exception.getErrorCode()).isEqualTo({Domain}ErrorCode.{DOMAIN}_NOT_FOUND);
}
```

## 🔧 Mock 패턴

```java
// Repository Mock
when(repository.save(any({Entity}.class))).thenReturn(savedEntity);
when(repository.findById(1L)).thenReturn(Optional.of(entity));
verify(repository).save(any({Entity}.class));

// Security Mock
@WithMockUser(roles = "ADMIN") // 실제 권한 설정
```

## 🛡️ 테스트 안전성 원칙

| 계층      | 원칙       | 적용 패턴                         |
|---------|----------|-------------------------------|
| Factory | 순수성 유지   | 외부 상태 변경 금지, ID/시간 등 자동 설정 금지 |
| DTO     | Null 안전성 | 모든 연관 객체 null 체크 필수           |
| Entity  | 상태 독립성   | 테스트간 엔티티 상태 격리 보장             |
| Mock    | 완전성 검증   | 모든 의존성 Mock 설정 완료 확인          |

```java
// ✅ Factory 순수성: 외부 상태 의존 금지
public static Entity createEntity() {
    return new Entity(validData); // ID, 타임스탬프 등 설정 금지
}

// ✅ DTO Null 안전성: 연관 객체 검증
.relation(entity.getRelation() != null ? 
    RelationDto.from(entity.getRelation()) : null)

// ✅ 테스트 격리: 필요시에만 상태 설정
if (테스트_검증_필요시) {
    ReflectionTestUtils.setField(entity, "field", value);
}
```

**업데이트**: 2025-07-06 | **75줄** | 테스트 안전성 원칙 추가