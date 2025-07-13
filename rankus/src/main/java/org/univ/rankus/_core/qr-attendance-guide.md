# QR 출석 시스템 개발 가이드 (AI 전용)

> 📋 **프로젝트**: Rankus QR 출석 시스템 구현  
> 🎯 **목표**: 소규모 랩실(5-20명) 대상 실시간 QR 출석 관리  
> 🏗️ **아키텍처**: 헥사고날 아키텍처 + Spring Boot 3.4.5

## 🚀 개발 개요

### 핵심 기능 요구사항

- **출석 세션 관리**: 랩 매니저/랩장이 출석 세션 시작/종료
- **QR 코드 생성**: 1-10분 유효시간 설정 가능한 QR 코드
- **실시간 출석 체크**: QR 스캔 시 즉시 출석 상태 업데이트
- **출석 상태 관리**: 출석(O), 결석(X), 지각 상태 및 수정 기능
- **출석 현황 조회**: 실시간 출석 현황 및 통계 조회

### 기술 스택 결정사항

- **토큰 방식**: 단순 ID 조합 (`labId-sessionId-timestamp`)
- **API 구조**: 프로젝트 RESTful 패턴 준수
- **중복 방지**: 애플리케이션 레벨 중복 체크
- **QR 최적화**: 기본형 ZXing 라이브러리 사용
- **실시간 업데이트**: 폴링 방식 (5초 간격)

## 🏗️ 아키텍처 설계

### 도메인 모델 구조

```
Attendance Domain
├── AttendanceSession (Root Aggregate)
│   ├── sessionId: Long
│   ├── labId: Long
│   ├── createdBy: Long (userId)
│   ├── title: String
│   ├── startTime: LocalDateTime
│   ├── endTime: LocalDateTime
│   ├── qrValidityMinutes: Integer (1-10)
│   ├── status: SessionStatus (ACTIVE, COMPLETED, CANCELLED)
│   └── attendanceRecords: List<AttendanceRecord>
│
├── AttendanceRecord (Entity)
│   ├── recordId: Long
│   ├── sessionId: Long
│   ├── userId: Long
│   ├── checkedAt: LocalDateTime
│   ├── status: AttendanceStatus (PRESENT, ABSENT, LATE)
│   └── isManuallyAdjusted: Boolean
│
├── QRToken (Value Object)
│   ├── token: String
│   ├── sessionId: Long
│   ├── generatedAt: LocalDateTime
│   └── expiresAt: LocalDateTime
│
└── AttendanceStatistics (Value Object)
    ├── totalMembers: Integer
    ├── presentCount: Integer
    ├── absentCount: Integer
    └── lateCount: Integer
```

### 헥사고날 아키텍처 매핑

```
Domain Layer:
├── AttendanceSession.java
├── AttendanceRecord.java
├── QRToken.java
├── AttendanceStatistics.java
├── SessionStatus.java (Enum)
├── AttendanceStatus.java (Enum)
└── AttendanceException.java

Application Layer:
├── port/in/
│   ├── AttendanceSessionCommandUseCase.java
│   ├── AttendanceSessionQueryUseCase.java
│   ├── AttendanceRecordCommandUseCase.java
│   └── AttendanceRecordQueryUseCase.java
├── port/out/
│   ├── AttendanceSessionRepositoryPort.java
│   ├── AttendanceRecordRepositoryPort.java
│   └── QRCodeGeneratorPort.java
└── service/
    ├── AttendanceSessionCommandService.java
    ├── AttendanceSessionQueryService.java
    ├── AttendanceRecordCommandService.java
    └── AttendanceRecordQueryService.java

Adapter Layer:
├── in/web/
│   ├── AttendanceSessionController.java
│   ├── AttendanceRecordController.java
│   └── dto/ (Request/Response DTOs)
└── out/persistence/
    ├── AttendanceSessionRepositoryAdapter.java
    ├── AttendanceRecordRepositoryAdapter.java
    └── QRCodeGeneratorAdapter.java
```

## 📊 데이터베이스 설계

### 테이블 구조

```sql
-- 출석 세션 테이블
CREATE TABLE attendance_sessions
(
    session_id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    lab_id              BIGINT       NOT NULL,
    created_by          BIGINT       NOT NULL,
    title               VARCHAR(100) NOT NULL,
    start_time          DATETIME     NOT NULL,
    end_time            DATETIME,
    qr_validity_minutes INT          NOT NULL DEFAULT 5,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL,
    FOREIGN KEY (lab_id) REFERENCES labs (id),
    FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_lab_status (lab_id, status),
    INDEX idx_created_by (created_by)
);

-- 출석 기록 테이블
CREATE TABLE attendance_records
(
    record_id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id           BIGINT      NOT NULL,
    user_id              BIGINT      NOT NULL,
    checked_at           DATETIME    NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    is_manually_adjusted BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at           DATETIME    NOT NULL,
    updated_at           DATETIME    NOT NULL,
    FOREIGN KEY (session_id) REFERENCES attendance_sessions (session_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE KEY uk_session_user (session_id, user_id),
    INDEX idx_session_status (session_id, status),
    INDEX idx_user_session (user_id, session_id)
);
```

### 인덱스 전략

- **Primary Keys**: AUTO_INCREMENT for performance
- **Composite Index**: (lab_id, status) for session queries
- **Unique Constraint**: (session_id, user_id) for duplicate prevention
- **Query Optimization**: session_id, user_id for frequent joins

## 🎯 API 설계

### RESTful API 구조

```
# 출석 세션 관리
POST   /api/labs/{labId}/attendance/sessions              # 세션 생성
GET    /api/labs/{labId}/attendance/sessions              # 세션 목록 조회
GET    /api/labs/{labId}/attendance/sessions/{sessionId}  # 세션 상세 조회
PUT    /api/labs/{labId}/attendance/sessions/{sessionId}  # 세션 수정
DELETE /api/labs/{labId}/attendance/sessions/{sessionId}  # 세션 종료

# QR 코드 관리
GET    /api/attendance/sessions/{sessionId}/qr            # QR 코드 생성
POST   /api/attendance/qr/check                          # QR 출석 체크

# 출석 기록 관리
GET    /api/attendance/sessions/{sessionId}/records       # 출석 기록 조회
PUT    /api/attendance/records/{recordId}                 # 출석 상태 수정
GET    /api/attendance/sessions/{sessionId}/statistics    # 출석 통계 조회
```

### 권한 매트릭스

| API   | LAB_MEMBER | LAB_MANAGER | LAB_LEADER | PROFESSOR | ADMIN |
|-------|------------|-------------|------------|-----------|-------|
| 세션 생성 | ❌          | ✅           | ✅          | ✅         | ✅     |
| 세션 조회 | ✅          | ✅           | ✅          | ✅         | ✅     |
| 세션 수정 | ❌          | ✅           | ✅          | ✅         | ✅     |
| QR 생성 | ❌          | ✅           | ✅          | ✅         | ✅     |
| 출석 체크 | ✅          | ✅           | ✅          | ✅         | ✅     |
| 기록 수정 | ❌          | ✅           | ✅          | ✅         | ✅     |

## 🔧 핵심 구현 패턴

### 1. UseCase 인터페이스 패턴

```java
public interface AttendanceSessionCommandUseCase {
    AttendanceSession createSession(Long labId, String title, Integer qrValidityMinutes, Long createdBy);

    AttendanceSession endSession(Long sessionId, Long userId);

    QRToken generateQRCode(Long sessionId);

    AttendanceRecord checkAttendance(String qrToken, Long userId);
}

public interface AttendanceSessionQueryUseCase {
    AttendanceSession findSessionById(Long sessionId);

    List<AttendanceSession> findActiveSessionsByLabId(Long labId);

    AttendanceStatistics getSessionStatistics(Long sessionId);

    List<AttendanceRecord> findRecordsBySessionId(Long sessionId);
}
```

### 2. 도메인 엔티티 패턴

```java

@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @Column(nullable = false)
    private Long labId;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false, length = 100)
    private String title;

    // 비즈니스 메서드
    public QRToken generateQRToken() {
        validateSessionActive();
        return QRToken.create(this.sessionId, this.qrValidityMinutes);
    }

    public AttendanceRecord checkAttendance(Long userId, LocalDateTime checkedAt) {
        validateSessionActive();
        validateUserNotAlreadyChecked(userId);
        return AttendanceRecord.create(this.sessionId, userId, checkedAt);
    }

    public void endSession() {
        validateSessionActive();
        this.status = SessionStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }
}
```

### 3. 서비스 구현 패턴

```java

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceSessionCommandService implements AttendanceSessionCommandUseCase {

    private final AttendanceSessionRepositoryPort sessionRepository;
    private final AttendanceRecordRepositoryPort recordRepository;
    private final UserRepositoryPort userRepository;
    private final QRCodeGeneratorPort qrCodeGenerator;

    @Override
    public AttendanceSession createSession(Long labId, String title, Integer qrValidityMinutes, Long createdBy) {
        // 1. 권한 검증
        User creator = userRepository.findById(createdBy)
                .orElseThrow(() -> new UserNotFoundException(createdBy));

        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(labId));

        if (!creator.canManageLabAttendance(lab)) {
            throw new AttendancePermissionException("출석 관리 권한이 없습니다.");
        }

        // 2. 도메인 객체 생성
        AttendanceSession session = AttendanceSession.create(labId, title, qrValidityMinutes, createdBy);

        // 3. 저장 및 반환
        return sessionRepository.save(session);
    }

    @Override
    public AttendanceRecord checkAttendance(String qrToken, Long userId) {
        // 1. QR 토큰 검증
        QRToken token = QRToken.fromString(qrToken);
        if (token.isExpired()) {
            throw new QRTokenExpiredException("QR 코드가 만료되었습니다.");
        }

        // 2. 세션 조회 및 검증
        AttendanceSession session = sessionRepository.findById(token.getSessionId())
                .orElseThrow(() -> new AttendanceSessionNotFoundException(token.getSessionId()));

        // 3. 출석 체크 (도메인 로직)
        AttendanceRecord record = session.checkAttendance(userId, LocalDateTime.now());

        // 4. 저장 및 반환
        return recordRepository.save(record);
    }
}
```

### 4. Controller 구현 패턴

```java

@RestController
@RequestMapping("/api/labs/{labId}/attendance/sessions")
@RequiredArgsConstructor
public class AttendanceSessionController {

    private final AttendanceSessionCommandUseCase commandUseCase;
    private final AttendanceSessionQueryUseCase queryUseCase;

    @PostMapping
    @PreAuthorize("@attendancePermissionHandler.canManageAttendance(authentication.principal, #labId)")
    public ResponseEntity<ApiResponse<AttendanceSessionResponseDto>> createSession(
            @PathVariable Long labId,
            @RequestBody @Valid AttendanceSessionCreateRequestDto request,
            Authentication authentication) {

        Long createdBy = ((UserPrincipal) authentication.getPrincipal()).getId();

        AttendanceSession session = commandUseCase.createSession(
                labId, request.getTitle(), request.getQrValidityMinutes(), createdBy);

        AttendanceSessionResponseDto response = AttendanceSessionResponseDto.from(session);

        return ResponseEntity.ok(ApiResponse.success(response, "출석 세션이 생성되었습니다."));
    }

    @GetMapping("/{sessionId}/qr")
    @PreAuthorize("@attendancePermissionHandler.canManageAttendance(authentication.principal, #labId)")
    public ResponseEntity<byte[]> generateQRCode(
            @PathVariable Long labId,
            @PathVariable Long sessionId) {

        QRToken token = commandUseCase.generateQRCode(sessionId);
        byte[] qrImage = qrCodeGenerator.generateQRImage(token.getToken());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }
}
```

## 🛡️ 보안 및 예외 처리

### 권한 검증 패턴

```java

@Component
@RequiredArgsConstructor
public class AttendancePermissionHandler {

    private final UserRepositoryPort userRepository;
    private final LabRepositoryPort labRepository;

    public boolean canManageAttendance(UserPrincipal principal, Long labId) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UserNotFoundException(principal.getId()));

        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(labId));

        return user.canManageLabAttendance(lab);
    }

    public boolean canViewAttendance(UserPrincipal principal, Long labId) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UserNotFoundException(principal.getId()));

        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(labId));

        return user.canViewLabAttendance(lab);
    }
}
```

### 예외 처리 매트릭스

```java
public enum AttendanceErrorCode implements ErrorCode {
    // 세션 관련
    SESSION_NOT_FOUND("A001", "출석 세션을 찾을 수 없습니다."),
    SESSION_ALREADY_ENDED("A002", "이미 종료된 출석 세션입니다."),
    SESSION_NOT_ACTIVE("A003", "활성화된 출석 세션이 아닙니다."),

    // QR 토큰 관련
    QR_TOKEN_EXPIRED("A004", "QR 코드가 만료되었습니다."),
    QR_TOKEN_INVALID("A005", "유효하지 않은 QR 코드입니다."),

    // 출석 기록 관련
    ALREADY_CHECKED_IN("A006", "이미 출석 체크되었습니다."),
    RECORD_NOT_FOUND("A007", "출석 기록을 찾을 수 없습니다."),

    // 권한 관련
    ATTENDANCE_PERMISSION_DENIED("A008", "출석 관리 권한이 없습니다."),
    VIEW_ATTENDANCE_PERMISSION_DENIED("A009", "출석 조회 권한이 없습니다.");
}
```

## 🧪 테스트 전략

### 테스트 팩토리 패턴

```java
public class AttendanceTestFactory {

    public static AttendanceSession createActiveSession(Long labId, Long createdBy) {
        return AttendanceSession.builder()
                .labId(labId)
                .createdBy(createdBy)
                .title("테스트 출석")
                .qrValidityMinutes(5)
                .status(SessionStatus.ACTIVE)
                .build();
    }

    public static AttendanceRecord createPresentRecord(Long sessionId, Long userId) {
        return AttendanceRecord.builder()
                .sessionId(sessionId)
                .userId(userId)
                .checkedAt(LocalDateTime.now())
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    public static QRToken createValidToken(Long sessionId) {
        return QRToken.create(sessionId, 5); // 5분 유효
    }
}
```

### 통합 테스트 패턴

```java

@SpringBootTest
@Transactional
class AttendanceIntegrationTest {

    @Autowired
    private AttendanceSessionCommandUseCase commandUseCase;

    @Autowired
    private AttendanceSessionQueryUseCase queryUseCase;

    @Test
    void 출석_세션_생성_및_QR_체크_통합_테스트() {
        // Given
        Long labId = 1L;
        Long createdBy = 1L;
        Long userId = 2L;

        // When: 세션 생성
        AttendanceSession session = commandUseCase.createSession(labId, "통합 테스트", 5, createdBy);

        // Then: 세션 생성 확인
        assertThat(session.getSessionId()).isNotNull();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);

        // When: QR 코드 생성
        QRToken token = commandUseCase.generateQRCode(session.getSessionId());

        // Then: 토큰 유효성 확인
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.isExpired()).isFalse();

        // When: 출석 체크
        AttendanceRecord record = commandUseCase.checkAttendance(token.getToken(), userId);

        // Then: 출석 기록 확인
        assertThat(record.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(record.getUserId()).isEqualTo(userId);
    }
}
```

## 📝 개발 단계별 체크리스트

### Phase 1: 도메인 모델 구현 (1주)

- [ ] AttendanceSession 엔티티 구현
- [ ] AttendanceRecord 엔티티 구현
- [ ] QRToken 값 객체 구현
- [ ] Enum 클래스 구현 (SessionStatus, AttendanceStatus)
- [ ] 도메인 예외 클래스 구현
- [ ] 도메인 모델 단위 테스트 작성

### Phase 2: 애플리케이션 레이어 구현 (1주)

- [ ] UseCase 인터페이스 정의
- [ ] Service 구현체 작성
- [ ] Repository Port 인터페이스 정의
- [ ] 권한 검증 로직 구현
- [ ] 애플리케이션 레이어 테스트 작성

### Phase 3: 어댑터 레이어 구현 (1주)

- [ ] Controller 구현
- [ ] Repository Adapter 구현
- [ ] QR 코드 생성 어댑터 구현
- [ ] DTO 클래스 구현
- [ ] 통합 테스트 작성

### Phase 4: 최종 통합 및 배포 (추가 시간)

- [ ] 전체 통합 테스트
- [ ] 성능 테스트
- [ ] 보안 테스트
- [ ] 배포 스크립트 작성

## 🎯 성능 최적화 가이드

### 1. 데이터베이스 최적화

```sql
-- 필수 인덱스
CREATE INDEX idx_attendance_sessions_lab_status ON attendance_sessions (lab_id, status);
CREATE INDEX idx_attendance_records_session_status ON attendance_records (session_id, status);
CREATE INDEX idx_attendance_records_user_session ON attendance_records (user_id, session_id);

-- 파티셔닝 (향후 확장 시)
-- 월별 파티셔닝으로 성능 향상
```

### 2. 캐싱 전략

```java

@Service
@RequiredArgsConstructor
public class AttendanceSessionQueryService {

    // 활성 세션 목록 캐싱 (5분)
    @Cacheable(value = "activeSessionsCache", key = "#labId")
    public List<AttendanceSession> findActiveSessionsByLabId(Long labId) {
        return sessionRepository.findByLabIdAndStatus(labId, SessionStatus.ACTIVE);
    }

    // 세션 통계 캐싱 (1분)
    @Cacheable(value = "sessionStatisticsCache", key = "#sessionId")
    public AttendanceStatistics getSessionStatistics(Long sessionId) {
        return calculateStatistics(sessionId);
    }
}
```

### 3. 동시성 제어

```java

@Service
@RequiredArgsConstructor
public class AttendanceRecordCommandService {

    // 중복 출석 방지를 위한 동시성 제어
    @Transactional
    public AttendanceRecord checkAttendance(String qrToken, Long userId) {
        // 1. SELECT FOR UPDATE로 레코드 락
        Optional<AttendanceRecord> existing = recordRepository
                .findBySessionIdAndUserIdForUpdate(sessionId, userId);

        if (existing.isPresent()) {
            throw new AlreadyCheckedInException("이미 출석 체크되었습니다.");
        }

        // 2. 출석 기록 생성
        AttendanceRecord record = AttendanceRecord.create(sessionId, userId, LocalDateTime.now());
        return recordRepository.save(record);
    }
}
```

## 🔗 의존성 추가 가이드

### build.gradle 추가 의존성

```gradle
dependencies {
    // QR 코드 생성
    implementation 'com.google.zxing:core:3.5.1'
    implementation 'com.google.zxing:javase:3.5.1'
    
    // 캐싱 (선택사항)
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'com.github.ben-manes.caffeine:caffeine'
    
    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.h2database:h2'
}
```

### application.yml 설정 추가

```yaml
# QR 출석 시스템 설정
attendance:
  qr:
    default-validity-minutes: 5
    max-validity-minutes: 10
    image-size: 300
  session:
    max-active-sessions-per-lab: 3
    auto-end-after-hours: 24

# 캐싱 설정
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m
```

## 🚨 주의사항 및 제약사항

### 1. 비즈니스 규칙

- 한 랩실당 최대 3개의 활성 세션 허용
- QR 코드 유효시간: 1-10분 범위
- 세션당 사용자 중복 출석 금지
- 세션 종료 후 출석 수정 불가

### 2. 기술적 제약사항

- 소규모 환경 최적화 (동시 사용자 30명 이하)
- 단일 서버 환경 (분산 환경 미고려)
- 기본 보안 수준 (엔터프라이즈 보안 미적용)

### 3. 확장성 고려사항

- 향후 WebSocket 실시간 업데이트 대응 가능
- 대용량 데이터 처리를 위한 파티셔닝 준비
- 마이크로서비스 분리 가능한 구조

---

**개발 완료 후 검증사항**:

1. 모든 테스트 케이스 통과
2. API 문서 자동 생성 (Swagger)
3. 성능 테스트 (동시 사용자 30명)
4. 보안 검증 (권한 체크, 토큰 검증)
5. 사용자 시나리오 테스트 완료