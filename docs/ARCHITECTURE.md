# 아키텍처 설계 과정

> 이 문서는 제가 Rankus 프로젝트를 만들면서 내린 설계 결정들을 기록한 것입니다.
> 인생 첫 주도적 프로젝트에서 "토비의 스프링"을 읽고 DDD와 헥사고날 아키텍처에 도전했는데, 잘한 것도 있고 과하게 한 것도 있었습니다.
> 169개 커밋을 되돌아보며 왜 그런 선택을 했는지, 결과는 어땠는지 솔직하게 정리했습니다.

---

## 이 프로젝트의 설계 목표

프로젝트를 시작하면서 세 가지 목표가 있었습니다.

1. **DDD(Domain-Driven Design) 적용**: 서비스에 로직 다 때려넣는 빈약한 모델(Anemic Domain Model)은 피하자. 도메인 객체가 직접 비즈니스 로직을 가지게 하자.
2. **헥사고날 아키텍처**: 토비의 스프링에서 "설계를 잘 하면 DB를 바꾸거나 새 기술을 넣을 때 모듈만 쏙 교체하면 된다"라는 걸 읽었는데, 이걸 직접 해보고 싶었습니다.
3. **Spring Security 깊이 학습**: 단순히 로그인만 구현하는 게 아니라, 도메인별로 세밀한 권한 관리까지 해보고 싶었습니다.

결론부터 말하면, 세 가지 목표 모두 성공과 실패를 동시에 경험했습니다. 특히 헥사고날은 "해봤으니까 왜 조심하라고 하는지 안다"라는 게 가장 큰 수확이었고, 권한 시스템은 가장 힘들었지만 가장 많이 성장한 부분이었습니다.

---

## 1. 헥사고날 아키텍처 전환

### 토비의 스프링이 시작이었습니다

토비의 스프링을 읽으면서 **"설계만 잘 하면 DB를 교체하거나 새로운 기술을 도입할 때 모듈 하나만 바꾸면 된다"**는 개념에 꽂혔습니다. 당시 머릿속에 그리고 있던 그림은 이랬습니다.

- 우선 MySQL로 개발하고, 나중에 PostgreSQL로 교체해본다
- Redis 캐시 레이어를 도입한다
- 이메일 발송 서비스를 자유롭게 교체한다

손이 많이 가고 어렵다는 건 알고 있었습니다. 근데 인생 첫 프로젝트인데, "열심히 하면 되지 않을까?"라는 생각이었습니다. 지금 돌이켜보면 좀 무모했지만, 그때는 제대로 된 아키텍처를 한번 경험해보고 싶다는 마음이 컸습니다.

### 전환 전: 전형적인 레이어드 아키텍처

프로젝트 초기(2025년 5~6월)에는 Controller → Service → Repository의 전통적인 구조로 개발했습니다. 기능이 추가될수록 점점 불편해졌습니다. 서비스가 JPA Entity에 직접 의존하고, DTO 생성이랑 Repository 호출이랑 비즈니스 로직이 한 메서드에 다 섞여있었습니다. 도메인 모델은 getter만 있는 데이터 주머니였고, 테스트하려면 Spring Context를 통째로 올려야 했습니다.

### 전환: 하루 만에 전면 적용

**2025년 6월 8일, 단일 커밋(`20d070f`)으로 전면 전환했습니다. 79개 파일 변경, 4,495줄 삭제, 1,627줄 추가.**

지금 생각하면 점진적으로 했어야 하는데, 당시에는 "한번에 확 바꾸자"는 생각이었습니다. 결과적으로 이 커밋 하나가 프로젝트에서 가장 큰 변경이었습니다.

```java
// Before: Service가 DTO를 직접 생성하고, Repository를 직접 호출
@Service
public class UserService {
    @Autowired
    private UserRepository repository; // Spring Data 직접 의존

    public UserResponseDto signup(String name, String email, String password) {
        User user = new User(name, email, password);
        User saved = repository.save(user);
        return new UserResponseDto(saved); // 서비스에서 DTO 변환
    }
}
```

```java
// After: Service는 Port 인터페이스에만 의존
@Service
@RequiredArgsConstructor
@Transactional
public class LabApplicationCommandService implements LabApplicationCommandUseCase {
    private final LabRepositoryPort labRepositoryPort;           // Port 인터페이스
    private final LabApplicationRepositoryPort applicationPort;  // Port 인터페이스
    private final UserRepositoryPort userRepositoryPort;         // Port 인터페이스

    @Override
    public LabApplication applyToLabWithSlot(Long labId, Long userId, Long slotId) {
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));
        // 도메인 로직은 도메인 객체에게 위임
    }
}
```

### 결과: 빛난 순간과 현실

**이메일 교체 — 헥사고날이 진짜 빛난 순간**

8월에 Google SMTP에서 Mailjet으로 바꿔야 했을 때, `SpringMailSender` Adapter 하나만 수정하면 됐습니다. 도메인 코드랑 서비스 코드는 한 줄도 안 건드렸습니다. "어댑터만 바꾸면 된다"가 진짜 되는구나, 하는 순간이었습니다. 이 경험 하나만으로도 헥사고날을 해본 의미는 있었다고 생각합니다.

**하지만 현실은...**

솔직히 보일러플레이트의 비용을 너무 만만하게 봤습니다.

| 항목 | 수치 |
|------|------|
| Port 인터페이스 (인바운드) | 28개 |
| Port 인터페이스 (아웃바운드) | 24개 |
| Repository Adapter | 21개 |
| 이 중 단순 위임(thin wrapper) | **20개** |

21개의 Repository Adapter 중에서 실제로 의미있는 로직이 있는 건 `LocalFileUploadAdapter` 딱 1개뿐이었습니다. 나머지 20개는 JPA Repository를 그대로 호출하는 위임 코드였습니다.

```java
// 20개 중 대부분이 이런 형태 — 위임만 하는 어댑터
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public User save(User user) {
        return springDataUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id);
    }
    // ... 모든 메서드가 단순 위임
}
```

그리고 당초 목표였던 **PostgreSQL 교체와 Redis 도입은 결국 하지 못했습니다**. 보일러플레이트 관리하느라 시간을 뺏기면서 새 기능 개발이 느려졌고, 교체를 시도할 여유 자체가 없었습니다. 교체를 해보려고 헥사고날을 도입했는데, 헥사고날 관리하느라 교체를 못 한 셈입니다.

### 돌아보면

토비의 스프링에서 읽은 유연한 설계의 가치는 진짜였습니다. 근데 그게 **모든 레이어에 항상 필요하다는 뜻은 아니었습니다**. 이메일처럼 실제로 교체가 발생하는 곳에서만 Port를 두고, 나머지는 직접 의존해도 됐을 것 같습니다.

52개 Port 인터페이스와 21개 Adapter를 직접 유지하면서, "필요할 때만 하라"는 말이 왜 나온 건지 뼈저리게 느꼈습니다. 실패했지만, 이 경험 덕분에 다음 프로젝트에서는 "이건 추상화가 필요한 곳인가?"를 따져볼 수 있는 감각이 생겼습니다.

---

## 2. DDD와 Rich Domain Model

### 빈약한 도메인 모델은 피하고 싶었습니다

이 프로젝트의 핵심 목표 중 하나가 DDD였습니다. 많은 Spring Boot 프로젝트에서 서비스에 모든 로직을 넣고, 엔티티는 getter만 있는 데이터 주머니로 쓰는 걸 봤는데, 그렇게 하고 싶지 않았습니다. 도메인 객체가 직접 비즈니스 규칙을 가지는 Rich Domain Model을 경험해보고 싶었습니다.

### 구현: Password 값 객체

DDD의 Value Object 패턴을 비밀번호에 적용했습니다. 학습 목적도 있었지만, 비밀번호를 String으로 그냥 들고 다니면 로그에 찍히거나 직렬화 과정에서 유출될 수 있다는 걱정도 있었습니다.

```java
@Embeddable
public class Password {
    @Column(name = "password_hash", nullable = false)
    private String hashed;

    // 팩토리 메서드 — 유효성 검증 + 해싱을 한 곳에서
    public static Password fromRaw(String rawPassword, PasswordEncoder encoder) {
        if (rawPassword == null || rawPassword.isBlank())
            throw new PasswordValidationException(PasswordErrorCode.PASSWORD_REQUIRED);
        if (rawPassword.length() < 8)
            throw new PasswordValidationException(PasswordErrorCode.PASSWORD_TOO_SHORT);
        if (rawPassword.length() > 255)
            throw new PasswordValidationException(PasswordErrorCode.PASSWORD_TOO_LONG);

        return new Password(encoder.encode(rawPassword));
    }

    // 비교 — PasswordEncoder를 외부에서 주입받아 사용
    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        if (rawPassword == null || rawPassword.isBlank()) return false;
        return encoder.matches(rawPassword, this.hashed);
    }
}
```

이렇게 하면 `Password` 객체는 항상 해시된 상태로만 존재합니다. 평문 비밀번호가 도메인 안에 남아있을 수가 없는 구조입니다.

### 구현: 도메인이 직접 판단하게

User 엔티티를 단순 데이터 컨테이너가 아니라, 자기 권한과 상태를 직접 판단하는 객체로 만들었습니다.

```java
@Entity
public class User extends BaseTimeEntity {
    @Enumerated(EnumType.STRING)
    private Role role; // 단일 역할 (명확한 계층)

    @Embedded
    private Password password;  // String이 아닌 값 객체

    public User(String name, String email, Password password) {
        this.name = validateName(name);   // 생성자에서 검증
        this.email = validateEmail(email);
        this.password = password;
        this.role = Role.STUDENT;
    }

    // 도메인이 자신의 권한을 직접 판단
    public boolean canManageLabApplications(Lab lab) {
        if (this.role == Role.ADMIN) return true;
        return (this.role == Role.LAB_LEADER || this.role == Role.PROFESSOR)
                && this.lab != null && this.lab.equals(lab);
    }
}
```

### 결과

DDD는 이 프로젝트에서 **가장 잘한 선택**이었습니다. "이 사용자가 이 랩의 공지를 볼 수 있나?"라는 질문에 서비스가 아니라 도메인 객체가 직접 답할 수 있게 됐습니다. 덕분에 단위 테스트도 훨씬 쉬워졌고, 비즈니스 로직이 어디 있는지 찾기도 쉬워졌습니다.

헥사고날은 과했지만, DDD만큼은 다음 프로젝트에서도 반드시 적용할 생각입니다.

---

## 3. 6단계 Role 계층

### 대학원 연구실 구조를 그대로 코드에 옮기고 싶었습니다

실제 연구실에는 교수, 랩장, 관리자, 멤버, 일반 학생 같은 위계가 있고, 각각 할 수 있는 게 다릅니다. 이걸 코드에 정확히 반영하고 싶었습니다.

```
ADMIN > PROFESSOR > LAB_LEADER > LAB_MANAGER > LAB_MEMBER > STUDENT
```

처음에는 여기서 더 나아가서 **커스텀 Role**까지 만들려고 했습니다. "장비 관리자"같이 표준 역할에 없는 권한을 교수가 직접 정의할 수 있게 하려는 거였습니다.

### 결과

6단계 Role 자체는 처음 설계한 대로 잘 동작했습니다. 하지만 **커스텀 Role은 포기**했습니다. 6단계만으로도 권한 체크가 복잡한데, 사용자 정의 Role까지 추가하면 감당이 안 될 것 같았습니다.

그리고 Spring Security의 `ROLE_` 접두사 규칙을 제대로 이해하지 못해서 운영 배포 때 GLOBAL_004 에러가 터지기도 했습니다. `hasRole('ADMIN')`이 내부적으로 `ROLE_ADMIN`을 찾는다는 걸 몰랐던 겁니다. (상세: [TROUBLE_SHOOTING.md #4](TROUBLE_SHOOTING.md#4-권한-시스템-global_004-에러))

### 돌아보면

도메인을 정확히 반영한 건 맞았습니다. 근데 커스텀 Role 같은 확장은 실제로 누가 "이런 기능 필요합니다"라고 요청했을 때 만들어도 됐을 것 같습니다. `ROLE_` 접두사 문제는... Spring Security를 더 공부하고 시작했어야 합니다.

---

## 4. 권한 시스템의 3단계 진화

### 솔직히 제일 어려웠던 부분입니다

권한에 대한 공부가 부족한 상태에서 코드를 짜기 시작했습니다. git 기록을 보면 SecurityConfig 파일만 **12번** 수정했고, 10월 21일 밤에는 권한 관련 커밋을 **5개 연속**으로 쳤습니다(밤 10시 17분 ~ 11시 41분). 927줄짜리 `PERMISSION_ANALYSIS.md`를 작성하면서까지 고민한 흔적이 남아있습니다.

처음부터 계획된 설계가 아닙니다. 문제를 만나고, 부족함을 느끼고, 개선하는 과정이 3번 반복된 겁니다.

### Phase 1: 산발적 Role 체크

초기에는 필요한 곳마다 if문으로 역할을 확인했습니다.

```java
if (!user.getRole().equals(Role.ADMIN)) {
    throw new PermissionException();
}
```

같은 코드가 여기저기 복붙되고, 새 역할이 추가되면 전부 찾아서 고쳐야 하는 문제가 있었습니다.

### Phase 2: @PreAuthorize 어노테이션

Spring Security 공식 방법인 메서드 레벨 보안으로 바꿨습니다.

```java
@PreAuthorize("hasRole('ADMIN') or hasRole('LAB_LEADER')")
@PostMapping("/{labId}/approve")
public ResponseEntity<?> approve(@PathVariable Long labId, @RequestBody ApproveRequest req) {
    // LAB_LEADER라면 아무 랩의 지원서든 승인 가능 — 자기 랩이 아니어도!
}
```

이게 문제였습니다. 역할만 확인하지, **"이 사람이 이 랩 소속인지"는 전혀 모릅니다**. A 랩의 리더가 B 랩의 지원서를 승인할 수 있는 보안 구멍이 생긴 겁니다.

### Phase 3: 도메인 객체 기반 통합 권한 평가기

실제로 "A 랩 리더가 B 랩 지원서를 승인할 수 있다"는 버그를 발견하고, 근본적으로 다시 설계했습니다. 이때가 10월 21일 밤이었고, SecurityConfig를 몇 번이고 뜯어고치면서 `UnifiedPermissionEvaluator`를 만들었습니다.

핵심은 권한 판단을 도메인 객체에게 맡기는 겁니다. `UnifiedPermissionEvaluator`가 11개의 도메인별 `PermissionHandler`에게 위임합니다.

```java
@Component
public class UnifiedPermissionEvaluator implements PermissionEvaluator {
    private final Map<String, DomainPermissionEvaluator> evaluators;

    public UnifiedPermissionEvaluator(List<DomainPermissionEvaluator> handlers) {
        this.evaluators = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        DomainPermissionEvaluator::targetType,
                        Function.identity()
                ));
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId,
                                 String targetType, Object permission) {
        DomainPermissionEvaluator evaluator = evaluators.get(targetType);
        if (evaluator == null) return false;
        return evaluator.hasPermission(auth, targetId, (String) permission);
    }
}
```

```java
// 구체적 핸들러 — 도메인 객체에게 권한 판단 위임
@Component
@RequiredArgsConstructor
public class LabApplicationPermissionHandler implements DomainPermissionEvaluator {

    @Override
    public String targetType() { return "LabApplication"; }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String permission) {
        // ADMIN/PROFESSOR는 빠른 경로로 즉시 허용
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) return true;
        }

        User user = userQueryUseCase.getUserById(userId);
        switch (permission.toUpperCase()) {
            case "CANCEL":
            case "DELETE": {
                LabApplication app = queryUseCase.getApplicationById((Long) targetId);
                return user.isAdmin() || app.isOwnedBy(userId); // 본인 것만 취소
            }
            case "APPROVE":
            case "REJECT": {
                LabApplication app = queryUseCase.getApplicationById((Long) targetId);
                return user.canManageLabApplications(app.getLab()); // 도메인이 판단
            }
            default: return false;
        }
    }
}
```

### 11개 PermissionHandler

| 핸들러 | 보호 대상 |
|--------|----------|
| `LabApplicationPermissionHandler` | 랩 지원서 승인/거절/취소 |
| `AttendanceSessionPermissionHandler` | 출석 세션 생성/종료 |
| `AttendanceRecordPermissionHandler` | 출석 기록 수정 |
| `InterviewPermissionHandler` | 면접 관리 |
| `VotePermissionHandler` | 투표 생성/마감 |
| `CalendarPermissionHandler` | 일정 관리 |
| `LabNoticePermissionHandler` | 공지사항 관리 |
| `LabResourcePermissionHandler` | 자료실 접근/관리 |
| `LabDashboardPermissionHandler` | 대시보드 조회 |
| `LabImagePermissionHandler` | 랩 이미지 관리 |
| `LabCreationRequestPermissionHandler` | 랩 생성 요청 |

### 3단계 비교

| 관점 | Phase 1 | Phase 2 | Phase 3 |
|------|---------|---------|---------|
| 도메인 이해 | 없음 | 없음 | **있음** (도메인에게 질의) |
| 보안 | 구멍 있음 | 역할만 확인 | **역할 + 소속 + 리소스** |
| 테스트 | 어려움 | `@MockBean` 필요 | 도메인 메서드 단위 테스트 |
| 확장 | 모든 곳 수정 | 어노테이션 추가 | **핸들러만 추가** |

### 돌아보면

처음부터 Phase 3로 갈 수 있었느냐고 하면, 솔직히 못 했을 것 같습니다. Phase 1에서 "이건 코드가 중복되는데?" 하고 Phase 2로 갔고, Phase 2에서 "역할만 봐선 안 되는데?" 하고 Phase 3로 간 겁니다. 도메인을 이해하는 깊이가 깊어지면서 설계도 같이 성숙해진 과정이었습니다.

가장 중요한 깨달음은, **"이 사람이 ADMIN인가?"를 확인하는 것과 "이 사람이 이 리소스에 대해 이 행동을 할 수 있는가?"를 묻는 것은 근본적으로 다르다**는 것이었습니다.

---

## 5. Command / Query 분리

### DB 최적화의 기반을 만들고 싶었습니다

Command/Query 분리를 도입한 건, 나중에 읽기 전용 DB 레플리카를 붙이거나 쿼리 최적화를 독립적으로 하기 위해서였습니다. 읽기와 쓰기를 분리해놓으면 그때 가서 수정이 쉬울 거라고 생각했습니다.

### 구조

모든 도메인에 대해 Command 서비스와 Query 서비스를 나눴습니다.

```java
// Command — 쓰기 트랜잭션
@Service
@RequiredArgsConstructor
@Transactional
public class LabApplicationCommandService implements LabApplicationCommandUseCase {
    // 상태 변경 메서드들
}

// Query — 읽기 전용 트랜잭션
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabApplicationQueryService implements LabApplicationQueryUseCase {
    // 조회 메서드들
}
```

이렇게 해서 **36개 UseCase 인터페이스**가 Command/Query로 분리되어 있습니다.

### 결과: 구조는 잡았는데 활용을 못 했습니다

솔직히 읽기/쓰기 분리 구조는 잡았지만, 이걸 활용한 최적화는 못 했습니다. `@Transactional(readOnly = true)`로 JPA Dirty Checking을 비활성화한 게 실질적인 이점이었고, 읽기 전용 레플리카나 캐시 레이어 같은 것까지는 도달하지 못했습니다.

다만 코드 읽기는 확실히 편해졌습니다. "이 메서드가 상태를 바꾸나, 조회만 하나?"가 클래스 이름에서 바로 보이니까요.

### 돌아보면

헥사고날과 비슷한 문제였습니다. "미래를 위한 선제적 분리"인데, 그 미래가 오지 않았습니다. 36개의 UseCase 인터페이스를 유지하는 비용을 생각하면, 실제로 레플리카를 도입할 때 분리해도 충분했을 것 같습니다. 코드 가독성이라는 부수적인 이점은 있었지만, 그것만으로 36개 인터페이스를 정당화하기엔 좀 부족합니다.

---

## 6. 서비스 반환값 리팩터링

### 서비스가 DTO를 아는 게 이상했습니다

헥사고날 전환 전에는 서비스가 DTO를 반환했습니다. 그러니까 서비스가 "클라이언트에 뭘 보여줄지"까지 알아야 하는 상황이었습니다. API가 바뀌면 서비스 코드도 같이 수정해야 했고, 이건 뭔가 잘못됐다는 느낌이었습니다.

### 도메인 객체를 반환하도록 변경

**40개 파일, 3,633줄 삭제, 1,890줄 추가.** 이것도 큰 작업이었습니다.

서비스는 도메인 객체만 반환하고, DTO 변환은 컨트롤러(어댑터)에서 합니다.

```java
// Service — 도메인 객체 반환
@Override
public LabApplication getApplicationById(Long appId) {
    return applicationPort.findById(appId)
            .orElseThrow(() -> new LabApplicationNotFoundException(
                    LabApplicationErrorCode.APPLICATION_NOT_FOUND));
}
```

```java
// Controller — 여기서 DTO로 변환
@GetMapping("/api/labs/{labId}/applications/{appId}")
public ResponseEntity<LabApplicationResponse> getApplication(
        @PathVariable Long labId, @PathVariable Long appId) {
    LabApplication app = queryUseCase.getApplicationById(appId);
    return ResponseEntity.ok(LabApplicationResponse.from(app)); // 어댑터에서 변환
}
```

### 결과

서비스에서 DTO 관련 코드가 싹 사라졌습니다. 같은 도메인 객체를 API마다 다르게 표현할 수도 있게 됐고요(목록용 간략 DTO, 상세용 전체 DTO). 다만 DTO 변환을 전부 수동으로 작성해야 해서, 이게 꽤 반복적인 작업이었습니다. (MapStruct를 몰랐던 아쉬움은 [RETROSPECTIVE.md](RETROSPECTIVE.md#2-아쉬운-선택들)에서 다룹니다.)

---

## 7. 테스트 팩토리 패턴

### 테스트 데이터 만드는 게 너무 번거로웠습니다

6단계 Role 계층에 Lab 소속 관계까지 있으니, 테스트 데이터 생성이 까다로웠습니다. "ADMIN 권한의 사용자", "특정 랩에 소속된 LAB_LEADER" 같은 조합을 매번 만들면 코드 중복이 심해질 게 뻔했습니다. 그래서 프로젝트 초기부터 팩토리 패턴을 도입했습니다.

### 구현

도메인 팩토리 12개 + 통합 테스트 팩토리 9개 = **총 22개의 팩토리 클래스**입니다.

```java
@Component
public class DomainLabFactory {
    public Lab createLab() {
        return new Lab("테스트 랩실", "설명", "컴퓨터과학과", LabCategory.AI);
    }

    public User createAdmin() {
        User admin = createUser();
        admin.changeRole(Role.ADMIN);
        return admin;
    }

    public InterviewSlot createInterviewSlot() {
        Interview interview = new Interview("테스트 면접",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        return new InterviewSlot(interview, LocalDateTime.now().plusDays(1), 3);
    }
}
```

### 결과

`labFactory.createAdmin()` 한 줄이면 ADMIN 사용자가 만들어지니까, 테스트 코드가 깔끔해졌습니다. 엔티티 생성자가 바뀌어도 팩토리만 수정하면 90개 넘는 테스트가 자동으로 따라왔습니다.

다만 리팩터링이 깊어질수록 팩토리 자체를 유지보수하는 게 은근 부담이었습니다. 도메인 모델이 바뀔 때마다 팩토리도 같이 고쳐야 했거든요. 규모가 더 커지면 FixtureMonkey 같은 라이브러리를 고려해야 할 것 같습니다.

---

## 8. 기술 스택 선택의 맥락

### MySQL

가장 익숙한 DB였고, AWS RDS를 쓸 계획이었습니다. 최종적으로는 PostgreSQL로 교체해보는 게 목표였는데, 이게 헥사고날을 선택한 이유 중 하나이기도 했습니다. 결과적으로 교체까지는 못 갔습니다.

### Java 17 + Spring Boot 3.4.5

Spring Boot 3.x의 최소 요구가 Java 17이라서 선택했습니다. LTS 버전이라 안정적이고, Records 지원 같은 최신 기능도 쓸 수 있었습니다.

### 모놀리스 아키텍처

마이크로서비스는 1인 개발에서 오버엔지니어링이라고 판단했습니다. 헥사고날 아키텍처도 과한 편이었는데 MSA까지 하면 진짜 아무것도 못 만들었을 것 같습니다. 모놀리스 안에서 헥사고날, DDD, Spring Security에 집중하는 게 맞았습니다.

### 47개 커스텀 Exception 클래스

체계적인 예외 처리를 해보고 싶었고, 도메인별로 명확한 에러 코드를 프론트엔드에 전달하기 위해 만들었습니다. `GlobalExceptionHandler`에서 모든 예외를 통합 처리하고, 일관된 `ErrorResponse`로 반환합니다. React 팀이 에러 코드를 보고 프론트에서 분기 처리할 수 있게 한 건 좋은 선택이었습니다.

### Swagger (springdoc-openapi)

React 팀과의 주요 소통 수단이었습니다. 21개 컨트롤러에 @Tag를 적용하고, JWT 보안 설정까지 넣어서, 프론트 팀이 별도 문서 없이 Swagger UI에서 바로 API를 확인하고 테스트할 수 있게 했습니다. CI/CD yaml 파일을 25번이나 수정하면서 배포 환경을 만들었는데, Swagger가 제대로 올라갔을 때의 안도감은 아직도 기억납니다.

---

## 관련 문서

- [TROUBLE_SHOOTING.md](TROUBLE_SHOOTING.md) — 각 설계에서 발생한 기술적 문제 해결 과정
- [DEPLOYMENT.md](DEPLOYMENT.md) — 배포 환경 구축과 CI/CD
- [RETROSPECTIVE.md](RETROSPECTIVE.md) — 프로젝트 전체 회고
- [README.md](../README.md) — 프로젝트 전체 개요
