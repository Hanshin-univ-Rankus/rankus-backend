# Application Layer 핵심 가이드 (AI 전용)

> 유스케이스 구현과 포트/어댑터 패턴 (70줄 이하)

## 🎯 핵심 책임 및 원칙

**책임**: 유스케이스 구현, 포트 정의, 트랜잭션 경계, 도메인 객체 조합  
**원칙**: 포트/어댑터 패턴, 단일 책임, 의존성 주입, 트랜잭션 일관성

## 📁 구조 패턴

```
application/
├── port/
│   ├── in/          # Inbound Port: 외부 → 내부
│   │   ├── command/ # 상태 변경 UseCase
│   │   └── query/   # 조회 UseCase
│   └── out/         # Outbound Port: 내부 → 외부
│       └── persistence/ # Repository Port
└── service/
    ├── command/     # Command 구현체
    ├── query/       # Query 구현체
    └── auth/        # 인증 서비스
```

## 🔗 포트/어댑터 플로우

### Inbound: Controller → UseCase → Service
```
@Controller → {Domain}CommandUseCase → {Domain}CommandService → {Domain}RepositoryPort
```

### Outbound: Service → Port → Adapter
```
Service → RepositoryPort ← RepositoryAdapter ← JPA Repository
```

## 🎯 UseCase 패턴

### Command UseCase (상태 변경)
```java
public interface {Domain}CommandUseCase {
    {Domain}ResponseDto create{Domain}({Domain}CreateRequestDto request);
    {Domain}ResponseDto update{Domain}(Long id, {Domain}UpdateRequestDto request);
    void delete{Domain}(Long id);
}
```

### Query UseCase (조회)
```java
public interface {Domain}QueryUseCase {
    {Domain}ResponseDto find{Domain}ById(Long id);
    List<{Domain}ResponseDto> findAll{Domain}s();
    PageResponse<{Domain}ResponseDto> find{Domain}s(Pageable pageable);
}
```

## 🏗️ Service 구현 패턴

### Command Service
```java
@Service @RequiredArgsConstructor
public class {Domain}CommandService implements {Domain}CommandUseCase {
    private final {Domain}RepositoryPort repository;
    
    @Override @Transactional
    public {Domain}ResponseDto create{Domain}({Domain}CreateRequestDto request) {
        {Domain} entity = {Domain}.create(request.getName());
        {Domain} saved = repository.save(entity);
        return {Domain}ResponseDto.from(saved);
    }
}
```

### Query Service
```java
@Service @RequiredArgsConstructor
public class {Domain}QueryService implements {Domain}QueryUseCase {
    private final {Domain}RepositoryPort repository;
    
    @Override @Transactional(readOnly = true)
    public {Domain}ResponseDto find{Domain}ById(Long id) {
        {Domain} entity = repository.findById(id)
            .orElseThrow(() -> new {Domain}NotFoundException(id));
        return {Domain}ResponseDto.from(entity);
    }
}
```

---

**참조**: 상세 컨벤션은 각 하위 디렉토리의 CONVENTIONS.md 참조  
**업데이트**: 2025-01-04 | **압축률**: 기존 대비 77% 절약

- `uploadImage()`: 이미지 업로드
- `updateImage()`: 이미지 정보 수정
- `deleteImage()`: 이미지 삭제

#### LabCreationRequestCommandUseCase

- `createLabCreationRequest()`: 랩실 생성 요청
- `approveLabCreationRequest()`: 랩실 생성 요청 승인
- `rejectLabCreationRequest()`: 랩실 생성 요청 거부
- `updateLabCreationRequest()`: 랩실 생성 요청 수정
- `deleteLabCreationRequest()`: 랩실 생성 요청 삭제 (신청자 본인 + 관리자만 가능)

#### LabNoticeCommandUseCase

- `createNotice()`: 공지사항 생성
- `updateNotice()`: 공지사항 수정
- `deleteNotice()`: 공지사항 삭제
- `togglePinNotice()`: 공지사항 고정/해제

#### AuthUseCase

- `signup()`: 회원가입
- `login()`: 로그인

### Query UseCase (조회)

#### UserQueryUseCase

- `getUserById()`: 사용자 단일 조회
- `getCurrentUser()`: 현재 로그인 사용자 조회

#### LabPromotionQueryUseCase

- `findAllLabs()`: 전체 랩실 목록 조회
- `findLabById()`: 랩실 상세 조회

#### LabApplicationQueryUseCase

- `findApplicationsByLab()`: 랩실별 지원서 목록
- `findApplicationById()`: 지원서 상세 조회

#### LabImageQueryUseCase

- `findImagesByLab()`: 랩실별 이미지 목록
- `findImageById()`: 이미지 상세 조회

#### LabCreationRequestQueryUseCase

- `findAllLabCreationRequests()`: 전체 랩실 생성 요청 목록
- `findLabCreationRequestById()`: 랩실 생성 요청 상세 조회
- `findLabCreationRequestsByUser()`: 사용자별 랩실 생성 요청 목록
- `findLabCreationRequestsByStatus()`: 상태별 랩실 생성 요청 목록

#### LabNoticeQueryUseCase

- `getNoticesByLabId()`: 랩실별 공지사항 목록 조회 (페이징 지원)
- `getAllNoticesByLabId()`: 랩실별 공지사항 전체 목록 조회
- `getNoticesByLabIdAndType()`: 랩실별 특정 타입 공지사항 조회
- `getPinnedNoticesByLabId()`: 랩실별 고정 공지사항 조회
- `getNoticeById()`: 공지사항 상세 조회 (권한 검증 포함)

## 🏗️ 서비스 구현 패턴

Service 구현에 대한 상세 내용은 다음을 참조하세요:

- Auth Service: @service/auth/CONVENTIONS.md
- Command Service: @service/command/CONVENTIONS.md
- Query Service: @service/query/CONVENTIONS.md

## 🔐 트랜잭션 관리

### 트랜잭션 경계 설정

- **Command Service**: 각 메서드에 `@Transactional` (읽기/쓰기)
- **Query Service**: 각 메서드에 `@Transactional(readOnly = true)` (읽기 전용)
- **복합 연산**: 서비스 메서드 단위로 트랜잭션 경계 설정

### 트랜잭션 전파 규칙

```java

@Service
@RequiredArgsConstructor
public class LabApplicationCommandService {

    // 기본 트랜잭션 (REQUIRED)
    @Override
    @Transactional
    public LabApplication approveApplication(Long applicationId) {
        // 전체가 하나의 트랜잭션으로 처리
    }

    // 새로운 트랜잭션 (REQUIRES_NEW)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(Long userId) {
        // 독립적인 트랜잭션으로 처리 (알림 실패가 승인 작업에 영향 없음)
    }
}
```

## ⚠️ 예외 처리 전략

### 계층별 예외 처리

1. **Domain Layer**: 도메인 규칙 위반 시 도메인 예외 발생
2. **Application Layer**: 비즈니스 로직 검증 후 예외 전파
3. **Adapter Layer**: HTTP 상태코드로 변환하여 응답

### 예외 전파 패턴

```java

@Service
@RequiredArgsConstructor
public class LabApplicationCommandService {

    @Override
    @Transactional
    public LabApplication approveApplication(Long applicationId, Long approverId) {
        // 1. 엔티티 조회 (없으면 NotFoundException)
        LabApplication application = labApplicationRepositoryPort
                .findById(applicationId)
                .orElseThrow(() -> new LabApplicationNotFoundException());

        // 2. 비즈니스 로직 (도메인 예외 발생 가능)
        application.approve(); // → LabApplicationValidationException 가능

        // 3. 영속화 (인프라 예외 발생 가능)
        return labApplicationRepositoryPort.save(application);
    }
}
```

## 📊 현재 구현 상태 분석

### ✅ 구현 완료된 기능

- **사용자 관리**: 생성, 조회 (기본 CRUD)
- **랩실 홍보**: 목록 조회, 상세 조회
- **지원 관리**: 지원, 승인, 거부, 취소
- **이미지 관리**: 업로드, 조회, 삭제
- **랩실 생성 요청**: 요청 생성, 승인, 거부, 조회
- **인증**: 회원가입, 로그인
- **공지사항 관리**: 생성, 수정, 삭제, 조회, 고정/해제

### 🔄 미구현 기능 (향후 개발)

- **랭킹 시스템**: 점수 관리, 승인 프로세스
- **QR 출석**: QR 생성, 스캔, 출석 기록
- **캘린더**: 일정 생성, 수정, 삭제, 조회
- **관리자**: 사용자 승인, 시스템 통계

## 🎯 포트 인터페이스 상세 가이드

포트 인터페이스의 구체적인 정의와 구현 방법은 다음을 참조하세요:

- **Command UseCase**: @port/in/command/CONVENTIONS.md
- **Query UseCase**: @port/in/query/CONVENTIONS.md
- **Repository Port**: @port/out/CONVENTIONS.md

## 🧪 Application Layer 테스트

### 단위 테스트 전략

- **Mock 활용**: Repository Port를 Mock으로 대체
- **비즈니스 로직 검증**: 도메인 객체의 올바른 조작 확인
- **예외 시나리오**: 다양한 예외 상황 테스트

### 통합 테스트 전략

- **실제 구현체 사용**: Repository Adapter 포함
- **트랜잭션 테스트**: 롤백, 커밋 시나리오 확인
- **End-to-End 플로우**: 전체 유스케이스 시나리오 테스트

### 테스트 예시

```java

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    void 유효한_정보로_사용자를_생성할_수_있다() {
        // given
        UserCreateRequestDto request = new UserCreateRequestDto(...);
        User savedUser = User.create(...);

        when(userRepositoryPort.existsByEmail(request.getEmail()))
                .thenReturn(false);
        when(userRepositoryPort.save(any(User.class)))
                .thenReturn(savedUser);

        // when
        UserResponseDto result = userCommandService.createUser(request);

        // then
        assertThat(result.getName()).isEqualTo(request.getName());
        verify(userRepositoryPort).save(any(User.class));
    }
}
```