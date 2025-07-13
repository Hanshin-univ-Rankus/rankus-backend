# Controller 테스트 트러블슈팅 가이드 (AI 필독)

> 🚨 **목적**: Controller 테스트 실패를 방지하고 빠른 문제 해결을 위한 종합 가이드

## 🔥 자주 발생하는 실패 패턴과 해결책 (실제 사례 기반)

### 1️⃣ HttpMessageNotReadableException 🚫

**문제**: `@RequestBody` 어노테이션이 있는 엔드포인트에 요청 본문 없이 테스트

```java
// ❌ 실패 케이스
mockMvc.perform(put("/api/attendance/records/{recordId}/present", RECORD_ID))
        .

andExpected(status().

isOk());

// ✅ 해결 방법
AttendanceStatusUpdateRequestDto request = new AttendanceStatusUpdateRequestDto(
        AttendanceStatus.PRESENT, "테스트 출석 처리"
);

mockMvc.

perform(put("/api/attendance/records/{recordId}/present", RECORD_ID)
                .

contentType(MediaType.APPLICATION_JSON)
                .

content(objectMapper.writeValueAsString(request)))
        .

andExpect(status().

isOk());
```

**근본 원인**: Controller 메서드가 `@RequestBody` 파라미터를 요구하지만 테스트에서 본문을 제공하지 않음

**예방 체크리스트**:

- [ ] Controller 메서드에 `@RequestBody` 어노테이션 확인
- [ ] 해당 DTO 클래스 필드 검증 (`@NotNull`, `@Size` 등)
- [ ] `MediaType.APPLICATION_JSON` 헤더 설정
- [ ] `objectMapper.writeValueAsString()` 직렬화 사용

### 2️⃣ AttendanceValidationException - 동일 상태 전이 🔄

**문제**: 도메인 로직이 동일한 상태로의 전이를 금지하는데 테스트에서 위반

```java
// ❌ 실패 케이스: PRESENT → PRESENT 전이 시도
AttendanceRecord record = DomainAttendanceFactory.buildValidRecord(); // 기본값: PRESENT
record.

markAsPresent(USER_ID, "테스트"); // PRESENT → PRESENT 시도

// ✅ 해결 방법: 다른 초기 상태 사용
AttendanceRecord record = DomainAttendanceFactory.buildAbsentRecordWithId(RECORD_ID); // ABSENT 상태
record.

markAsPresent(USER_ID, "테스트"); // ABSENT → PRESENT 전이
```

**근본 원인**: Domain 객체가 현재 상태와 동일한 상태로 변경하는 것을 금지 (validateStatusChange)

**예방 체크리스트**:

- [ ] 도메인 엔티티의 초기 상태 확인
- [ ] 상태 전이 규칙 검증 (domain model 내 validation 로직)
- [ ] Factory에서 적절한 초기 상태 객체 생성 메서드 사용
- [ ] 테스트에서 유효한 상태 전이만 수행

### 3️⃣ Spring Boot 3.x 호환성 문제 📦

**문제**: Deprecated 어노테이션 사용으로 인한 컴파일 또는 런타임 오류

```java
// ❌ Spring Boot 3.x에서 deprecated
@MockBean
private UseCase useCase;

// ✅ Spring Boot 3.x 호환 방법
@MockitoBean
private UseCase useCase;
```

**추가 호환성 패턴**:

```java
// ✅ 완전한 Spring Boot 3.x Controller 테스트 패턴
@WebMvcTest(AttendanceRecordController.class)
@AutoConfigureMockMvc(addFilters = false)  // 보안 필터 비활성화
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttendanceRecordControllerTest {

    @MockitoBean  // NOT @MockBean
    private AttendanceRecordCommandUseCase commandUseCase;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();  // 테스트 격리
    }
}
```

### 4️⃣ Mock 기대값 불일치 🎭

**문제**: Mock 설정과 실제 호출 파라미터가 다름

```java
// ❌ 실패 케이스: anyString() vs 실제 구체적 값
given(commandUseCase.markAsPresent(eq(RECORD_ID),eq(USER_ID),

anyString()))
        .

willReturn(record);

// 실제 호출: request.getReason()이 구체적 값
record =commandUseCase.

markAsPresent(recordId, userId, request.getReason());

// ✅ 해결 방법: 구체적 값 매칭
given(commandUseCase.markAsPresent(eq(RECORD_ID),eq(USER_ID),

eq("테스트 출석 처리")))
        .

willReturn(record);
```

### 5️⃣ 권한 검증 실패 🔐

**문제**: PreAuthorize에서 요구하는 권한과 테스트 설정이 불일치

```java
// ❌ 실패 케이스: 권한 불일치
@PreAuthorize("@attendanceRecordPermissionHandler.hasPermission(authentication.principal, #recordId, 'MANAGE')")
public ResponseEntity<> markAsPresent(Long recordId, ...) { ...}

// 테스트에서 'UPDATE' 권한 Mock
given(attendanceRecordPermissionHandler.hasPermission(any(),eq(RECORD_ID),

eq("UPDATE")))
        .

willReturn(true);

// ✅ 해결 방법: 정확한 권한 문자열 사용
given(attendanceRecordPermissionHandler.hasPermission(any(),eq(RECORD_ID),

eq("MANAGE")))
        .

willReturn(true);
```

## 🛠️ Controller 테스트 작성 체크리스트

### 📋 사전 준비 단계

1. **Controller 메서드 분석**
    - [ ] HTTP 메서드 (GET, POST, PUT, DELETE) 확인
    - [ ] `@RequestBody` 어노테이션 유무 확인
    - [ ] `@PathVariable`, `@RequestParam` 파라미터 확인
    - [ ] `@PreAuthorize` 권한 요구사항 확인

2. **DTO 요구사항 분석**
    - [ ] Request DTO 필수 필드 확인 (`@NotNull`, `@Size` 등)
    - [ ] Enum 값 검증 (`@Valid`)
    - [ ] 생성자 방식 vs Builder 패턴 확인

3. **도메인 로직 검증**
    - [ ] 도메인 엔티티의 초기 상태 확인
    - [ ] 상태 전이 규칙 검증
    - [ ] 비즈니스 검증 로직 확인

### 📝 테스트 코드 작성 단계

```java

@WebMvcTest(AttendanceRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttendanceRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceRecordCommandUseCase commandUseCase;

    @MockitoBean
    private AttendanceRecordPermissionHandler permissionHandler;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("PUT /api/attendance/records/{recordId}/present > 출석 처리 성공 → 200 OK")
    void markAsPresent_Success() throws Exception {
        // given - 1. 인증 설정
        setupSecurityContext(USER_ID);

        // given - 2. 도메인 객체 준비 (적절한 초기 상태)
        AttendanceRecord record = DomainAttendanceFactory.buildAbsentRecordWithId(RECORD_ID);
        record.markAsPresent(USER_ID, "테스트 수동 출석 처리");

        // given - 3. Request DTO 준비
        AttendanceStatusUpdateRequestDto request = new AttendanceStatusUpdateRequestDto(
                AttendanceStatus.PRESENT, "테스트 출석 처리"
        );

        // given - 4. Mock 설정 (정확한 파라미터 매칭)
        given(commandUseCase.markAsPresent(eq(RECORD_ID), eq(USER_ID), eq("테스트 출석 처리")))
                .willReturn(record);
        given(permissionHandler.hasPermission(any(), eq(RECORD_ID), eq("MANAGE")))
                .willReturn(true);

        // when & then
        mockMvc.perform(put("/api/attendance/records/{recordId}/present", RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)  // 필수 헤더
                        .content(objectMapper.writeValueAsString(request)))  // 직렬화
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }
}
```

### 🔍 자주 놓치는 디테일

1. **Import 확인**
   ```java
   import org.springframework.test.context.bean.override.mockito.MockitoBean;  // NOT MockBean
   import org.springframework.http.MediaType;
   import org.univ.rankus.adapter.in.web.dto.request.AttendanceStatusUpdateRequestDto;
   ```

2. **인증 컨텍스트 설정**
   ```java
   private void setupSecurityContext(Long userId) {
       CustomUserDetails principal = mock(CustomUserDetails.class);
       given(principal.getUserId()).willReturn(userId);
       TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
       authentication.setAuthenticated(true);  // 필수!
       SecurityContextHolder.getContext().setAuthentication(authentication);
   }
   ```

3. **DTO 생성 패턴**
   ```java
   // Record 타입 DTO
   AttendanceStatusUpdateRequestDto request = new AttendanceStatusUpdateRequestDto(status, reason);
   
   // Builder 패턴 DTO
   UserRegisterRequestDto request = UserRegisterRequestDto.builder()
       .name("테스트사용자")
       .email("test@example.com")
       .password("password123!")
       .build();
   ```

## ⚡ 빠른 진단 명령어

### 🔍 실패한 테스트 분석

```bash
# 1. 특정 테스트 클래스만 실행
./gradlew test --tests "*AttendanceRecordControllerTest*"

# 2. 상세 에러 로그 확인
./gradlew test --tests "*AttendanceRecordControllerTest*" --info

# 3. 특정 메서드만 테스트
./gradlew test --tests "*AttendanceRecordControllerTest.markAsPresent_Success"
```

### 🔧 코드 검증 명령어

```bash
# 1. @RequestBody 사용 확인
grep -r "@RequestBody" src/main/java/org/univ/rankus/adapter/in/web/controller/

# 2. DTO 필드 검증 확인
grep -r "@NotNull\|@Size\|@Valid" src/main/java/org/univ/rankus/adapter/in/web/dto/request/

# 3. PreAuthorize 권한 확인
grep -r "@PreAuthorize" src/main/java/org/univ/rankus/adapter/in/web/controller/

# 4. Domain 상태 전이 메서드 확인
grep -r "public void mark\|public void update" src/main/java/org/univ/rankus/domain/model/
```

## 🎯 도메인별 특수 케이스

### Attendance 도메인

- **초기 상태**: 모든 `AttendanceRecord`는 `PRESENT` 상태로 생성
- **상태 전이**: 동일 상태로의 변경 금지 (`validateStatusChange`)
- **필수 필드**: `reason` (1-200자), `adjustedBy` (양수)

### Score Submission 도메인

- **만료 정책**: 6개월 후 자동 만료
- **승인 제한**: 본인이 본인 신청 승인 불가
- **정정 제한**: 1회만 가능

### Interview 도메인

- **시간 충돌**: 동일 시간대 슬롯 생성 금지
- **상태 전이**: ACTIVE → COMPLETED/CANCELLED만 가능
- **예약 제한**: 최대 신청자 수 초과 불가

### Lab Application 도메인

- **중복 지원**: 동일 사용자 동일 랩실 중복 지원 금지
- **면접 시간**: 반드시 미래 시점
- **상태 전이**: PENDING에서만 APPROVED/REJECTED로 변경 가능

## 🚨 긴급 상황 대응

### 테스트 대량 실패 시

1. **즉시 확인 사항**
   ```bash
   # Spring Boot 버전 확인
   grep "org.springframework.boot" build.gradle
   
   # MockBean import 확인
   grep -r "import.*MockBean" src/test/java/
   ```

2. **단계별 복구**
   ```bash
   # 1단계: 특정 테스트만 격리 실행
   ./gradlew test --tests "*AttendanceRecordControllerTest*"
   
   # 2단계: 의존성 확인
   ./gradlew dependencies --configuration testCompileClasspath
   
   # 3단계: 캐시 정리 후 재실행
   ./gradlew clean test --tests "*AttendanceRecordControllerTest*"
   ```

### 테스트 환경 문제 시

```bash
# Docker 컨테이너 재시작
docker-compose down
docker-compose up -d

# 테스트 DB 초기화
./gradlew flywayClean flywayMigrate

# Gradle 캐시 정리
./gradlew clean build --refresh-dependencies
```

## 📚 참고 자료

- **Spring Boot 3.x 테스트 가이드**: `/test/CLAUDE.md`
- **Adapter 테스트 전략**: `/adapter/CLAUDE.md`
- **Domain Factory 사용법**: `/testutil/CLAUDE.md`
- **Mock 활용 패턴**: `/testutil/mock/`

---

**업데이트**: 2025-07-13 | **실제 사례**: AttendanceRecordControllerTest 수정 완료