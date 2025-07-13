# Controller 테스트 체크리스트 (AI 빠른 참조용)

> ⚡ **목적**: Controller 테스트 작성 시 빠르게 확인할 수 있는 핵심 체크리스트

## 🚀 빠른 시작 체크리스트 (30초 검증)

### ✅ 1단계: 기본 설정 (5초)

- [ ] `@WebMvcTest({Controller}.class)`
- [ ] `@AutoConfigureMockMvc(addFilters = false)`
- [ ] `@ExtendWith(MockitoExtension.class)`
- [ ] `@MockitoSettings(strictness = Strictness.LENIENT)`

### ✅ 2단계: 의존성 주입 (10초)

- [ ] `@Autowired MockMvc mockMvc`
- [ ] `@Autowired ObjectMapper objectMapper`
- [ ] `@MockitoBean {UseCase}` (NOT @MockBean)
- [ ] `@MockitoBean {PermissionHandler}`

### ✅ 3단계: 보안 설정 (5초)

- [ ] `@AfterEach void clearSecurity()` 메서드
- [ ] `setupSecurityContext(userId)` 헬퍼 메서드
- [ ] `authentication.setAuthenticated(true)` 설정

### ✅ 4단계: 테스트 메서드 (10초)

- [ ] Request DTO 생성 및 필수 필드 확인
- [ ] `contentType(MediaType.APPLICATION_JSON)` 헤더
- [ ] `objectMapper.writeValueAsString(request)` 직렬화
- [ ] Mock 설정 시 구체적 파라미터 매칭

## 📋 도메인별 특수 고려사항

### Attendance 도메인

```java
// ✅ 상태 전이 고려
AttendanceRecord record = DomainAttendanceFactory.buildAbsentRecordWithId(RECORD_ID);  // PRESENT 아닌 상태
AttendanceStatusUpdateRequestDto request = new AttendanceStatusUpdateRequestDto(
        AttendanceStatus.PRESENT, "테스트 출석 처리"
);
```

### ScoreSubmission 도메인

```java
// ✅ 본인 승인 금지 고려
given(commandUseCase.approveSubmission(eq(SUBMISSION_ID),eq(APPROVER_ID)))
        .

willReturn(approvedSubmission);
// APPROVER_ID ≠ submission.getUser().getId()
```

### Lab Application 도메인

```java
// ✅ 미래 면접 시간 설정
LabApplicationRequestDto request = new LabApplicationRequestDto(
        LocalDateTime.now().plusDays(1)  // 반드시 미래 시점
);
```

## 🎯 HTTP 메서드별 패턴

### POST 요청 (생성)

```java
mockMvc.perform(post("/api/{endpoint}")
                .

contentType(MediaType.APPLICATION_JSON)
                .

content(objectMapper.writeValueAsString(request)))
        .

andExpect(status().

isCreated())  // 201
        .

andExpect(jsonPath("$.status").

value(201));
```

### PUT 요청 (수정)

```java
mockMvc.perform(put("/api/{endpoint}/{id}", entityId)
                .

contentType(MediaType.APPLICATION_JSON)
                .

content(objectMapper.writeValueAsString(request)))
        .

andExpect(status().

isOk())  // 200
        .

andExpect(jsonPath("$.status").

value(200));
```

### GET 요청 (조회)

```java
mockMvc.perform(get("/api/{endpoint}/{id}", entityId))
        .

andExpect(status().

isOk())  // 200
        .

andExpect(jsonPath("$.data.id").

value(entityId));
```

### DELETE 요청 (삭제)

```java
mockMvc.perform(delete("/api/{endpoint}/{id}", entityId))
        .

andExpect(status().

isNoContent());  // 204
```

## ⚠️ 자주 하는 실수 방지

### 실수 1: @RequestBody 누락

```java
// ❌ 실수
mockMvc.perform(put("/api/endpoint/{id}", id))  // 본문 없음

// ✅ 해결  
        mockMvc.

perform(put("/api/endpoint/{id}", id)
                .

contentType(MediaType.APPLICATION_JSON)
                .

content(objectMapper.writeValueAsString(request)))
```

### 실수 2: Mock 파라미터 불일치

```java
// ❌ 실수
given(useCase.method(eq(ID),eq(USER_ID),

anyString())).

willReturn(result);
// 실제 호출: useCase.method(ID, USER_ID, request.getReason())

// ✅ 해결
given(useCase.method(eq(ID),eq(USER_ID),

eq("구체적 값"))).

willReturn(result);
```

### 실수 3: 권한 문자열 불일치

```java
// ❌ 실수
@PreAuthorize("...hasPermission(..., 'MANAGE')")
    // Controller
given(permissionHandler.hasPermission(..., eq("UPDATE"))).

willReturn(true);  // Test

// ✅ 해결
given(permissionHandler.hasPermission(..., eq("MANAGE"))).

willReturn(true);
```

## 🔧 디버깅 명령어

### 실패 원인 빠른 진단

```bash
# 1. 특정 테스트만 실행
./gradlew test --tests "*{Controller}Test*"

# 2. @RequestBody 확인
grep -A 5 -B 5 "@RequestBody" src/main/java/.../controller/{Controller}.java

# 3. PreAuthorize 권한 확인  
grep -A 2 "@PreAuthorize" src/main/java/.../controller/{Controller}.java

# 4. DTO 필드 검증 확인
grep -A 10 "class.*RequestDto" src/main/java/.../dto/request/
```

## 📝 완전한 테스트 메서드 템플릿

```java

@Test
@DisplayName("{HTTP메서드} {엔드포인트} > {기능} 성공 → {상태코드} {상태메시지}")
void {
    메서드명
}

_Success() throws Exception {
    // given - 인증 설정
    setupSecurityContext(USER_ID);

    // given - 도메인 객체 준비 (적절한 상태)
    {
        Domain
    } entity = {DomainFactory}.build {
        적절한상태
    } WithId(ENTITY_ID);

    // given - Request DTO 준비
    {
        RequestDto
    } request = new {
        RequestDto
    } (validValues);

    // given - Mock 설정 (구체적 파라미터)
    given(useCase. {
        method
    } (eq(ENTITY_ID), eq(USER_ID), eq("구체적값")))
        .willReturn(entity);
    given(permissionHandler.hasPermission(any(), eq(ENTITY_ID), eq("정확한권한")))
            .willReturn(true);

    // when & then
    mockMvc.perform({httpMethod} ("/api/{endpoint}/{id}", ENTITY_ID)
                    .contentType(MediaType.APPLICATION_JSON)  // @RequestBody 필요시
            .content(objectMapper.writeValueAsString(request)))  // @RequestBody 필요시
            .andExpect(status(). {
        expectedStatus
    } ())
            .andExpect(jsonPath("$.status").value({statusCode}));
}

@Test
@DisplayName("인증 없는 요청 시 500 Internal Server Error (필터 비활성화)")
void unauthenticatedRequest_InternalServerError() throws Exception {
    // given
    {
        RequestDto
    } request = new {
        RequestDto
    } (validValues);

    // when & then - 필터 비활성화로 userDetails가 null이 되어 500 발생
    mockMvc.perform({httpMethod} ("/api/{endpoint}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpected(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500));
}
```

## 🚨 긴급 상황 대응

### 테스트 대량 실패 시 순서

1. **Spring Boot 3.x 호환성 확인**
   ```bash
   grep -r "@MockBean\|import.*MockBean" src/test/java/
   ```

2. **도메인 상태 전이 확인**
   ```bash
   grep -r "buildValid.*WithId\|markAs" src/test/java/
   ```

3. **@RequestBody 요구사항 확인**
   ```bash
   grep -r "@RequestBody" src/main/java/.../controller/
   ```

### 빠른 수정 패턴

```java
// 1. @MockBean → @MockitoBean 일괄 변경
// 2. addFilters = false 추가
// 3. setupSecurityContext() 메서드 추가
// 4. clearSecurity() @AfterEach 추가
// 5. 적절한 Factory 메서드 사용
```

---

**활용법**: 테스트 작성 전 이 체크리스트를 순서대로 확인하여 실패 방지  
**업데이트**: 2025-07-13 | **검증**: AttendanceRecordControllerTest 성공 기반