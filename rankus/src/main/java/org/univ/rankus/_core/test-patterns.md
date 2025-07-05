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
// LabNotice: LNT_001~007(입력값), LNT_403(권한), LNT_404(미존재)
// LabCreationRequest: LCR_006(중복), LCR_007(상태변경불가), LCR_009(미존재)
```

## 📝 Notice 테스트 예시

```java
// Entity 테스트
@Test
void LabNotice_생성_성공() {
    LabNotice notice = LabNotice.create(title, content, author, lab, NoticeType.NORMAL);
    assertThat(notice.getType()).isEqualTo(NoticeType.NORMAL);
    assertThat(notice.isPinned()).isFalse();
}

// 권한 테스트
@Test @WithMockUser(roles = "USER")
void 공지사항_생성_권한_있음() throws Exception {
    mockMvc.perform(post("/api/labs/{labId}/notices", 1L)
            .content(objectMapper.writeValueAsString(request)))
            .andExpected(status().isCreated());
}

// ErrorCode 테스트
@Test
void 제목_누락시_LNT_001_반환() {
    NoticeException exception = assertThrows(NoticeException.class,
        () -> service.createLabNotice(invalidRequest));
    assertThat(exception.getErrorCode()).isEqualTo(NoticeErrorCode.TITLE_REQUIRED);
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

**업데이트**: 2025-01-05 | **60줄** | AI 코딩 최적화