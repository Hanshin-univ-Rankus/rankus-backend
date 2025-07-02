# Application Layer 가이드

> 유스케이스 구현과 포트/어댑터 패턴을 통한 비즈니스 플로우 관리

## 🎯 Application Layer 개요

### 핵심 책임

- **유스케이스 구현**: 사용자의 요구사항을 구체적인 비즈니스 플로우로 변환
- **포트 인터페이스 정의**: 외부 계층과의 통신 규약 명시
- **트랜잭션 경계**: 비즈니스 로직의 원자성 보장
- **도메인 객체 조합**: 여러 도메인 객체를 조합하여 복잡한 비즈니스 로직 구현

### 설계 원칙

- **포트/어댑터 패턴**: 인터페이스를 통한 의존성 역전
- **단일 책임 원칙**: 각 서비스는 하나의 유스케이스만 담당
- **의존성 주입**: 포트 인터페이스를 통한 느슨한 결합
- **트랜잭션 일관성**: 데이터 변경의 원자성 보장

## 📁 Application Layer 구조

### Port 인터페이스 (`port/`)

#### Inbound Port (`port/in/`)

- **외부에서 애플리케이션으로 들어오는 요청의 인터페이스**
- **Command**: 상태 변경 작업 (생성, 수정, 삭제)
- **Query**: 조회 작업 (단일 조회, 목록 조회, 검색)

#### Outbound Port (`port/out/`)

- **애플리케이션에서 외부 시스템으로 나가는 요청의 인터페이스**
- **Repository**: 데이터 영속성 관리
- **External Service**: 외부 API 호출 (향후 확장)

### Service 구현 (`service/`)

#### Command Service (`service/command/`)

- **상태 변경 유스케이스 구현**
- 생성, 수정, 삭제 작업
- 트랜잭션 관리

#### Query Service (`service/query/`)

- **조회 유스케이스 구현**
- 단일/복수 엔티티 조회
- 검색 및 필터링

#### Auth Service (`service/auth/`)

- **인증/인가 유스케이스 구현**
- 로그인, 회원가입
- JWT 토큰 관리

## 🔗 포트/어댑터 패턴 상세

### Inbound Flow (외부 → 내부)

```
Controller (Adapter) 
    ↓ 호출
UseCase Interface (Inbound Port)
    ↓ 구현
Service (Application Core)
    ↓ 호출  
Repository Port (Outbound Port)
```

### Outbound Flow (내부 → 외부)

```
Service (Application Core)
    ↓ 호출
Repository Port (Outbound Port)
    ↓ 구현
Repository Adapter (Adapter)
    ↓ 호출
JPA Repository (Infrastructure)
```

## 📋 현재 구현된 UseCase

### Command UseCase (상태 변경)

#### UserCommandUseCase

- `createUser()`: 사용자 생성
- `updateUser()`: 사용자 정보 수정
- `deleteUser()`: 사용자 삭제

#### LabApplicationCommandUseCase

- `applyToLab()`: 랩실 지원
- `approveApplication()`: 지원 승인
- `rejectApplication()`: 지원 거부
- `cancelApplication()`: 지원 취소

#### LabImageCommandUseCase

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