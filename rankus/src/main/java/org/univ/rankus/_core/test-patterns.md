# 테스트 패턴 (AI 코딩용)

## 🎯 Unit Test 템플릿

```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) class {ProductionClass}

Test {
    @Mock private {
        Dependency
    } dependency;
    @InjectMocks private {
        ProductionClass
    } target;
    
    @Test
    void{
        메서드명
    } _정상입력시_ {
        예상결과
    } () {
        // given - 실제 파라미터 타입 사용
        // when - 실제 메서드 호출
        // then - 실제 리턴 타입 검증
    }
}
```

## 🎭 Controller Test 템플릿 (Spring Boot 3.x 호환)

```java
@WebMvcTest({Controller}.class)
@AutoConfigureMockMvc(addFilters = false)  // 보안 필터 비활성화
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) class {Controller}

Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean  // ⚠️ NOT @MockBean (deprecated)
    private {
        UseCase
    } useCase;

    @MockitoBean
    private {
        PermissionHandler
    } permissionHandler;

    @AfterEach
    void clearSecurity () {
        SecurityContextHolder.clearContext();  // 테스트 격리
    }

    private void setupSecurityContext (Long userId){
        CustomUserDetails principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        authentication.setAuthenticated(true);  // 필수!
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    @Test
    @DisplayName("POST /api/{endpoint} > {기능} 성공 → 201 Created")
    void{
        메서드명
    } _Success() throws Exception {
        // given
        setupSecurityContext(USER_ID);
        {
            RequestDto
        } request = new {
            RequestDto
        } (validData);
        {
            Domain
        } entity = {DomainFactory}.build {
            ValidEntity
        } ();

        given(useCase. {
            method
        } (any())).willReturn(entity);
        given(permissionHandler.hasPermission(any(), any(), eq("ACTION"))).willReturn(true);

        // when & then
        mockMvc.perform(post("/api/{endpoint}")
                        .contentType(MediaType.APPLICATION_JSON)  // 필수 헤더
                        .content(objectMapper.writeValueAsString(request)))  // JSON 직렬화
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    @DisplayName("인증 없는 요청 시 500 Internal Server Error (필터 비활성화)")
    void unauthenticatedRequest_InternalServerError () throws Exception {
        // given
        {
            RequestDto
        } request = new {
            RequestDto
        } (validData);

        // when & then - 필터가 비활성화되어 userDetails가 null이 되어 500 발생
        mockMvc.perform(post("/api/{endpoint}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
```

## ⚠️ ErrorCode 테스트 패턴

```java

@Test
void {
    검증조건
}

_시_ {
    ErrorCode
}

_반환() {
    // when
    {
        Exception
    } exception = assertThrows({Exception}. class,
    () -> service.method(invalidInput));

    // then
    assertThat(exception.getErrorCode()).isEqualTo({ErrorCode}. {
        CODE
    });
}

// 주요 ErrorCode 매핑
// User: USER_001~010(입력값), USER_404(미존재), USER_409(중복)
// Lab: LAB_001~015(입력값), LAB_404(미존재), LAB_409(중복)
// LabApplication: LAP_001~012(입력값), LAP_404(미존재), LAP_422(상태변경불가)
// LabNotice: LNT_001~007(입력값), LNT_403(권한), LNT_404(미존재)
// Interview: INT_001~034(입력값/상태), INT_028~029(권한), INT_030~032(미존재)
// LabCreationRequest: LCR_006(중복), LCR_007(상태변경불가), LCR_009(미존재)
// Attendance: ATT_001~010(입력값), ATT_403(권한), ATT_404(미존재), ATT_409(중복), ATT_422(상태변경불가)
```

## 📝 다양한 도메인 테스트 예시

### Entity 테스트 패턴

```java
// 기본 Entity 생성 테스트
@Test
void {
    Domain
}

_생성_성공() {
    {
        Domain
    } entity = {Domain}.create(validParams);
    assertThat(entity.get {
        Field
    } ()).isEqualTo(expectedValue);
}

// 상태 전이 테스트 (상태 관리 Entity용)
@Test
void {
    Domain
}

_상태변경_성공() {
    {
        Domain
    } entity = {Domain}.create(validParams);
    entity.approve(); // or activate(), reject() 등
    assertThat(entity.getStatus()).isEqualTo({Status}.APPROVED);
}

// 연관관계 테스트
@Test
void {
    Domain
}

_연관관계_설정_성공() {
    {
        Domain
    } entity = {Domain}.create(parentEntity, childParams);
    assertThat(entity.getParent()).isEqualTo(parentEntity);
}
```

### Interview 도메인 테스트 패턴

```java
// 면접 생성 테스트
@Test
void Interview_생성_성공() {
    Lab lab = DomainLabFactory.buildValidLab();
    LocalDate startDate = LocalDate.of(2024, 1, 15);
    LocalDate endDate = LocalDate.of(2024, 1, 22);

    Interview interview = new Interview(lab, startDate, endDate, 60, 5);

    assertThat(interview.getLab()).isEqualTo(lab);
    assertThat(interview.getStartDate()).isEqualTo(startDate);
    assertThat(interview.getEndDate()).isEqualTo(endDate);
    assertThat(interview.getStatus()).isEqualTo(InterviewStatus.INACTIVE);
}

// 면접 상태 전이 테스트
@Test
void Interview_활성화_성공() {
    Interview interview = DomainInterviewFactory.buildInactiveInterview();

    interview.activate();

    assertThat(interview.getStatus()).isEqualTo(InterviewStatus.ACTIVE);
    assertThat(interview.isActive()).isTrue();
}

// 면접 슬롯 시간 충돌 테스트
@Test
void InterviewSlot_시간_충돌_검증() {
    Interview interview = DomainInterviewFactory.buildValidInterview();
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 14, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 15, 0);

    InterviewSlot slot = new InterviewSlot(interview, startTime, endTime, 5);

    assertThat(slot.getStartTime()).isEqualTo(startTime);
    assertThat(slot.getEndTime()).isEqualTo(endTime);
    assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
}
```

### Ranking 도메인 테스트 패턴

```java
// 점수 신청 생성 테스트
@Test
void ScoreSubmission_생성_성공() {
    User user = DomainUserFactory.buildValidUser();
    Lab lab = DomainLabFactory.buildValidLab();
    LocalDate achievementDate = LocalDate.of(2024, 1, 10);

    ScoreSubmission submission = new ScoreSubmission(
            user, lab, ScoreCategory.ACADEMIC_ACHIEVEMENT,
            "성과 내용", achievementDate, "proof.pdf",
            "신청 사유", null, VisibilityLevel.PUBLIC
    );

    assertThat(submission.getUser()).isEqualTo(user);
    assertThat(submission.getLab()).isEqualTo(lab);
    assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.PENDING);
    assertThat(submission.getScore()).isEqualTo(ScoreCategory.ACADEMIC_ACHIEVEMENT.getDefaultScore());
}

// 점수 승인 테스트
@Test
void ScoreSubmission_승인_성공() {
    ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();
    Long approverId = 999L;

    submission.approve(approverId);

    assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.APPROVED);
    assertThat(submission.getApprovedBy()).isEqualTo(approverId);
    assertThat(submission.getApprovedAt()).isNotNull();
}

// 점수 거절 테스트
@Test
void ScoreSubmission_거절_성공() {
    ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();
    Long approverId = 999L;
    String reason = "증빙서류 부족";

    submission.reject(approverId, reason);

    assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.REJECTED);
    assertThat(submission.getApprovedBy()).isEqualTo(approverId);
    assertThat(submission.getRejectionReason()).isEqualTo(reason);
}
```

### 시간 기반 로직 테스트 패턴

```java
// 고정 시간 기반 테스트 (권장)
@Test
void 시간_기반_로직_테스트() {
    LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 14, 0);
    InterviewSlot slot = new InterviewSlot(
            interview, fixedTime, fixedTime.plusHours(1), 5
    );

    // 시간 기반 검증
    assertThat(slot.getStartTime()).isEqualTo(fixedTime);
    assertThat(slot.getDuration()).isEqualTo(Duration.ofHours(1));
}

// 만료 시간 테스트
@Test
void ScoreSubmission_만료_확인() {
    ScoreSubmission submission = DomainScoreSubmissionFactory.buildValidSubmission();
    LocalDateTime pastTime = LocalDateTime.of(2024, 1, 1, 0, 0);

    ReflectionTestUtils.setField(submission, "expiresAt", pastTime);

    assertThat(submission.isExpired()).isTrue();
}

// 시간 범위 검증 테스트
@Test
void Interview_기간_내_슬롯_검증() {
    LocalDate interviewStart = LocalDate.of(2024, 1, 15);
    LocalDate interviewEnd = LocalDate.of(2024, 1, 22);
    Interview interview = DomainInterviewFactory.buildInterviewWithDates(interviewStart, interviewEnd);

    LocalDateTime slotTime = LocalDateTime.of(2024, 1, 16, 14, 0);
    InterviewSlot slot = new InterviewSlot(interview, slotTime, slotTime.plusHours(1), 5);

    assertThat(slot.getStartTime().toLocalDate()).isBetween(interviewStart, interviewEnd);
}
```

### Controller 권한 테스트 패턴

```java
// 도메인별 권한 테스트
@Test
@WithMockUser(roles = "USER")
void {
    Domain
}

_생성_권한_있음() throws Exception {
    mockMvc.perform(post("/api/{domains}")
                    .content(objectMapper.writeValueAsString(request)))
            .andExpected(status().isCreated());
}

// 랩실 권한 테스트
@Test
@WithMockUser(roles = "LAB_MANAGER")
void {
    Domain
}

_관리_권한_있음() throws Exception {
    mockMvc.perform(post("/api/labs/{labId}/{domains}", 1L)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpected(status().isCreated());
}
```

### ErrorCode 테스트 패턴

```java
// 입력값 검증 테스트
@Test
void {
    필드
}

_누락시_ {
    PREFIX
}

_001_반환() {
    {
        Domain
    } Exception exception = assertThrows({Domain}Exception.class,
            () -> service.create {
        Domain
    } (invalidRequest));
    assertThat(exception.getErrorCode()).isEqualTo({Domain}ErrorCode. {
        FIELD
    } _REQUIRED);
}

// 상태 변경 불가 테스트
@Test
void 잘못된_상태에서_변경시_{PREFIX}

_422_반환() {
    {
        Domain
    } Exception exception = assertThrows({Domain}Exception.class,
            () -> entity.changeStatus());
    assertThat(exception.getErrorCode()).isEqualTo({Domain}ErrorCode.CANNOT_CHANGE_STATUS);
}

// 조회 실패 테스트
@Test
void 존재하지않는_{Domain}

_조회시_ {
    PREFIX
}

_404_반환() {
    {
        Domain
    } Exception exception = assertThrows({Domain}Exception.class,
            () -> service.find {
        Domain
    } ById(999L));
    assertThat(exception.getErrorCode()).isEqualTo({Domain}ErrorCode. {
        DOMAIN
    } _NOT_FOUND);
}

// Interview ErrorCode 테스트
@Test
void 중복된_활성화_면접_생성시_INT_012_반환() {
    InterviewValidationException exception = assertThrows(InterviewValidationException.class,
            () -> service.createInterview(labId, startDate, endDate, duration, maxApplicants));
    assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.DUPLICATE_INTERVIEW);
}

@Test
void 시간_충돌_슬롯_생성시_INT_015_반환() {
    InterviewValidationException exception = assertThrows(InterviewValidationException.class,
            () -> service.createInterviewSlot(interviewId, startTime, endTime, maxApplicants));
    assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_TIME_CONFLICT);
}

// Ranking ErrorCode 테스트
@Test
void 존재하지않는_점수신청_조회시_RANKING_404_반환() {
    RankingValidationException exception = assertThrows(RankingValidationException.class,
            () -> service.findSubmissionById(999L));
    assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.SUBMISSION_NOT_FOUND);
}

@Test
void 중복된_성과_신청시_RANKING_009_반환() {
    RankingValidationException exception = assertThrows(RankingValidationException.class,
            () -> service.submitScore(userId, labId, category, description, achievementDate, proofUrl, reason, relatedLink, visibility));
    assertThat(exception.getErrorCode()).isEqualTo(RankingErrorCode.DUPLICATE_ACHIEVEMENT);
}

// Attendance ErrorCode 테스트
@Test
void 제목_누락시_ATT_001_반환() {
    AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
            () -> AttendanceSession.create(1L, 1L, null, 5));
    assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.TITLE_REQUIRED);
}

@Test
void QR_유효시간_범위_초과시_ATT_004_반환() {
    AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
            () -> AttendanceSession.create(1L, 1L, "테스트", 11));
    assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.QR_VALIDITY_INVALID);
}

@Test
void 중복_출석_체크시_ATT_007_반환() {
    AttendanceSession session = AttendanceSession.create(1L, 1L, "테스트", 5);
    session.checkAttendance(2L, LocalDateTime.now());

    AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
            () -> session.checkAttendance(2L, LocalDateTime.now()));
    assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.ALREADY_CHECKED_IN);
}

@Test
void 비활성_세션_QR_생성시_ATT_006_반환() {
    AttendanceSession session = AttendanceSession.create(1L, 1L, "테스트", 5);
    session.endSession();

    AttendanceValidationException exception = assertThrows(AttendanceValidationException.class,
            () -> session.generateQRToken());
    assertThat(exception.getErrorCode()).isEqualTo(AttendanceErrorCode.SESSION_NOT_ACTIVE);
}
```

## 🔧 Mock 패턴

```java
// Repository Mock
when(repository.save(any( {
    Entity
}.class))).

thenReturn(savedEntity);

when(repository.findById(1L)).

thenReturn(Optional.of(entity));

verify(repository).

save(any( {
    Entity
}.class));

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
.

relation(entity.getRelation() !=null?
        RelationDto.

from(entity.getRelation()):null)

// ✅ 테스트 격리: 필요시에만 상태 설정
        if(테스트_검증_필요시){
        ReflectionTestUtils.

setField(entity, "field",value);
}
```

**업데이트**: 2025-07-06 | **75줄** | 테스트 안전성 원칙 추가