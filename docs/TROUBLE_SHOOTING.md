# 트러블슈팅: 삽질의 기록

> 토비의 스프링을 읽고 "나도 할 수 있겠다" 싶어서 시작한 첫 프로젝트에서 겪은 문제들입니다.
> 전체 169개 커밋 중 fix 커밋이 74개(44%)입니다. 거의 절반이 버그 수정이었습니다.
> 커밋 해시와 실제 코드를 기반으로 작성했습니다.

## 목차

- [1. JWT 리프레시 토큰: 3번의 시행착오](#1-jwt-리프레시-토큰-3번의-시행착오)
- [2. 면접 슬롯 동시 예약: 동시성 제어](#2-면접-슬롯-동시-예약-동시성-제어)
- [3. QR 토큰 보안: 평문에서 AES-256-GCM까지](#3-qr-토큰-보안-평문에서-aes-256-gcm까지)
- [4. 권한 시스템 GLOBAL_004 에러](#4-권한-시스템-global_004-에러)
- [5. Google → Mailjet SMTP 마이그레이션](#5-google--mailjet-smtp-마이그레이션)
- [6. CORS 설정과 역직렬화 문제](#6-cors-설정과-역직렬화-문제)

---

## 1. JWT 리프레시 토큰: 3번의 시행착오

### 문제를 만나기까지

처음에는 액세스 토큰 하나로 인증을 처리했습니다. 만료 시간을 길게 잡으면 보안이 걱정이고, 짧게 잡으면 사용 중에 튕기는 문제가 있었습니다. 어떤 값을 넣어야 하는지 감이 안 왔습니다.

Spring Security OAuth2 Resource Server 의존성(`spring-boot-starter-oauth2-resource-server`)도 build.gradle에 추가해봤지만, 설정이 복잡해서 프로젝트 일정 안에 제대로 적용하지 못했습니다. 그래서 JWT 인증을 직접 구현하는 쪽으로 방향을 바꿨는데, 결과적으로 토큰이 어떻게 동작하는지 하나하나 이해하게 된 계기가 됐습니다.

### 1차 시도: 만료 시간만 바꿔보자 (`8085b61`)

가장 단순한 생각이었습니다. "만료 시간을 적절하게 맞추면 되지 않을까?" 해서 일단 1분으로 줄여서 문제를 재현해봤습니다.

```yaml
# 1차 시도 - 테스트용 짧은 만료 시간
jwt:
  access-token-expiration: 60000   # 1분
```

당연히 이걸로 해결될 리가 없었습니다. 1시간이든 1일이든, 만료 시간 하나로 보안과 사용성을 동시에 잡을 수 없다는 걸 이때 느꼈습니다.

### 2차 시도: 급한 마음에 임시 해결 (`8acd18f`)

이 커밋 메시지가 `fix: 리프레시 토큰 임시해결`이었습니다. 말 그대로 "임시"였습니다. 리프레시 토큰 구조를 도입하긴 했는데, 다른 기능 개발이 급해서 핵심 로직(로테이션, 재사용 방지)은 나중에 하기로 하고 일단 돌아가게만 만들었습니다.

```yaml
# 2차 시도 - 둘 다 같은 만료 시간 (잘못된 접근)
jwt:
  access-token-expiration: 604800000   # 7일
  refresh-token-expiration: 604800000  # 7일
```

git diff를 보면 이때 application.yml에 `# 현재 7일로 임시 처리`라는 주석까지 달아놨습니다. 스스로도 이게 맞지 않다는 걸 알고 있었던 겁니다. 액세스 토큰과 리프레시 토큰이 둘 다 7일이면, 리프레시 토큰이 있을 이유가 없습니다. 토큰 재사용 방지도 없어서, 탈취되면 7일 동안 무한정 새 토큰을 발급받을 수 있는 상태였습니다.

"임시 해결책"이 새로운 보안 문제를 만든 케이스였습니다.

### 3차 시도: 제대로 해보자 (`e3dfcf8`, `e085b25`)

결국 다시 돌아와서, 제대로 된 구조를 만들기로 했습니다. 액세스 토큰은 짧게(15분) + Stateless, 리프레시 토큰은 길게(7일) + DB에서 관리. 이렇게 분리하는 게 정석이라는 걸 이때 이해했습니다.

```java
// RefreshToken 엔티티 - DB에서 상태를 추적
@Entity @Table(name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_token_email", columnList = "user_email"),
        @Index(name = "idx_refresh_token_active", columnList = "is_active")
    })
public class RefreshToken {
    @Column(name = "token_id", length = 36)
    private String tokenId;          // UUID로 고유 식별

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;        // 로테이션 시 이전 토큰 비활성화

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public boolean isValid() {
        return isActive && !isExpired();
    }
}
```

```java
// JwtTokenProvider - 토큰 발급 시 로테이션 적용
public TokenPair generateTokens(User user) {
    // 1. 기존 리프레시 토큰 전부 비활성화 (로테이션)
    refreshTokenRepository.deactivateAllByUserEmail(user.getEmail());

    // 2. 새 액세스 토큰 (15분, Stateless)
    String accessToken = Jwts.builder()
        .setSubject(String.valueOf(user.getId()))
        .claim("role", user.getRole().name())
        .setId(UUID.randomUUID().toString())     // JTI: 블랙리스트용
        .setExpiration(new Date(now + 900_000))   // 15분
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    // 3. 새 리프레시 토큰 (7일, Stateful - DB 저장)
    RefreshToken refreshToken = new RefreshToken(
        UUID.randomUUID().toString(), user.getEmail(),
        LocalDateTime.now().plusDays(7)
    );
    refreshTokenRepository.save(refreshToken);

    return new TokenPair(accessToken, refreshToken.getTokenId());
}
```

```java
// 블랙리스트 기반 로그아웃 - 즉각 토큰 무효화
@Entity @Table(name = "blacklisted_tokens")
public class BlacklistedToken {
    @Column(name = "token_id", nullable = false, unique = true)
    private String tokenId;       // JWT의 JTI 값

    @Column(name = "reason", length = 50)
    private String reason;        // "LOGOUT"

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;  // 원본 토큰 만료 시점에 자동 정리
}
```

### 토큰 검증 흐름

```
요청 → JwtAuthenticationFilter
  ├─ 서명 검증 (HS256) ← Stateless, DB 조회 없음
  ├─ JTI로 블랙리스트 확인 ← 로그아웃된 토큰 차단
  ├─ 만료 시간 확인 (clockSkew 60초 허용)
  └─ role claim에서 권한 추출 ← DB 조회 대신 토큰에서 직접

갱신 요청 → /api/auth/refresh
  ├─ 리프레시 토큰 DB 조회 (isActive && !isExpired)
  ├─ 기존 리프레시 토큰 비활성화
  └─ 새 액세스 토큰 + 새 리프레시 토큰 발급
```

### 돌아보면

git log를 보면 JWT 관련 커밋만 6개가 넘습니다. `8085b61`(테스트용 1분) → `8acd18f`(임시 7일) → `e3dfcf8`(리프레시 토큰 적용) → `ac22f01`(role 누락 수정) → `adb1e6f`(디버깅 로깅 추가) → `14e8b3e`(보안 강화). 한 번에 제대로 만들지 못하고 계속 고쳤습니다.

특히 "임시해결"이라고 이름 붙인 커밋이 결국 새 보안 문제를 만든 경험이 기억에 남습니다. "나중에 고치겠다"는 생각이 얼마나 위험한지, 보안 관련 코드에서는 더더욱 그렇다는 걸 몸으로 배웠습니다.

---

## 2. 면접 슬롯 동시 예약: 동시성 제어

### 문제 상황

면접 슬롯 예약에서 동시 요청이 들어오면 데이터 정합성이 깨졌습니다. 정원 3명인 슬롯에 4명이 동시에 요청하면, 모두 "현재 예약자 수 = 2"를 읽고 각각 예약에 성공하는 Lost Update 문제였습니다.

```
시간   학생A              학생B              학생C
─────────────────────────────────────────────────────
T1    SELECT (booked=2)
T2                       SELECT (booked=2)
T3                                          SELECT (booked=2)
T4    UPDATE booked=3 ✓
T5                       UPDATE booked=3 ✓  ← 덮어씀!
T6                                          UPDATE booked=3 ✓  ← 또 덮어씀!

결과: 3명 모두 예약 성공, 그런데 DB에는 booked=3 (실제로는 5명)
```

### 어떤 방법들을 찾아봤는가

동시성 제어를 처음 공부하면서 세 가지 방법을 비교했습니다.

**`synchronized` — 가장 먼저 떠오른 방법**

Java에서 동시성 하면 `synchronized`가 먼저 생각났습니다. 근데 이건 단일 JVM에서만 동작합니다. 지금은 EC2 한 대지만, 나중에 서버를 늘리면 각 JVM이 따로 놀게 되니까 의미가 없어집니다. 애초에 DB 레벨에서 해결해야 한다고 판단했습니다.

**Optimistic Locking (`@Version`) — JPA에서 많이 추천하는 방법**

JPA 동시성 제어를 검색하면 제일 먼저 나오는 방법입니다. 충돌이 적을 때 성능이 좋다는 장점이 있습니다. 근데 면접 슬롯은 "선착순 한 자리"를 다투는 상황이라 충돌이 잦습니다. 충돌할 때마다 예외 던지고 재시도하면 UX가 나빠질 것 같았습니다.

**Pessimistic Locking (`SELECT FOR UPDATE`) — 최종 선택**

결국 Optimistic이냐 Pessimistic이냐는 충돌 빈도로 결정해야 한다고 판단했습니다. 면접 예약은 한정된 자원을 놓고 경쟁하는 시나리오니까, 먼저 잠그고 처리하는 Pessimistic 방식이 맞다고 생각했습니다.

### 구현 (`09cc4e6`)

Pessimistic Lock에 `@Retryable`을 조합했습니다. build.gradle에 Spring Retry 의존성을 추가한 커밋이기도 합니다.

```java
// Repository: 행 단위 잠금
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM InterviewSlot s WHERE s.id = :id")
InterviewSlot findByIdForUpdate(@Param("id") Long id);
```

```java
// Service: Pessimistic Lock + Retry
@Retryable(
    value = {ObjectOptimisticLockingFailureException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 100, multiplier = 2)  // 100ms → 200ms → 400ms
)
@Transactional(isolation = Isolation.READ_COMMITTED)
public LabApplication applyToLabWithSlot(Long labId, Long userId, Long slotId) {

    // 1. 행 잠금 획득 — 다른 트랜잭션은 여기서 대기
    InterviewSlot slot = interviewSlotRepositoryPort.findByIdForUpdate(slotId)
        .orElseThrow(() -> new InterviewNotFoundException(InterviewErrorCode.SLOT_NOT_FOUND));

    // 2. 잠금 획득 후 다시 확인 (중요!)
    //    잠금 대기하는 동안 다른 트랜잭션이 슬롯을 채웠을 수 있음
    if (!slot.isAvailable()) {
        throw new LabApplicationValidationException(LabApplicationErrorCode.SLOT_NOT_AVAILABLE);
    }

    // 3. 예약 처리 — 잠금 범위 안에서 원자적 실행
    slot.book();
    LabApplication app = new LabApplication(lab, user, slot);
    return labApplicationRepositoryPort.save(app);

    // 4. 트랜잭션 커밋 시 잠금 해제 → 대기 중인 다음 트랜잭션 진행
}
```

### 적용 후 동작

```
시간   학생A                    학생B                    학생C
─────────────────────────────────────────────────────────────────
T1    SELECT ... FOR UPDATE
      → 잠금 획득 ✓
T2                             SELECT ... FOR UPDATE
                               → 잠금 대기 ⏳
T3                                                      SELECT ... FOR UPDATE
                                                        → 잠금 대기 ⏳
T4    booked=2 → 3, COMMIT
      → 잠금 해제
T5                             잠금 획득
                               booked=3, capacity=3
                               → SLOT_NOT_AVAILABLE ✗
T6                                                      잠금 획득
                                                        booked=3, capacity=3
                                                        → SLOT_NOT_AVAILABLE ✗

결과: 학생A만 예약 성공. 나머지는 정확한 에러 응답 ✓
```

트랜잭션 격리 수준은 `READ_COMMITTED`를 선택했습니다. `SERIALIZABLE`은 데드락 위험이 크고, `SELECT FOR UPDATE`가 이미 행 단위 직렬화를 보장하니까 `READ_COMMITTED`면 충분했습니다.

### 돌아보면

동시성 제어를 직접 구현해본 건 처음이었습니다. 교과서에서 읽은 Optimistic/Pessimistic Lock을 실제 코드에 적용하면서, "이론으로 아는 것"과 "직접 구현하는 것"의 차이를 느꼈습니다. 특히 잠금 획득 후에도 조건을 다시 확인해야 한다는 점(`isAvailable()`)은 직접 겪어봐야 알 수 있는 부분이었습니다.

---

## 3. QR 토큰 보안: 평문에서 AES-256-GCM까지

### 처음에 만든 QR 코드

QR 출석 기능을 처음 구현했을 때(`7월 13일 커밋`) ZXing 라이브러리로 QR 코드를 생성하고, 토큰은 그냥 단순하게 만들었습니다. 커밋 메시지가 `feat: QR 출석 및 출결 기능 구현`이었는데, 이때는 기본 동작(생성 → 스캔 → 기록)이 되는지가 급했습니다.

```java
// QRToken.java - 레거시 포맷
// 형식: "labId-sessionId-timestamp"
String token = String.format("%d-%d-%d", labId, sessionId,
    now.toEpochSecond(ZoneOffset.UTC));

// 파싱: 하이픈으로 분리
String[] parts = tokenString.split("-");
Long labId = Long.parseLong(parts[0]);       // 누구나 읽을 수 있음
Long sessionId = Long.parseLong(parts[1]);   // 위조 가능
```

QR 코드를 사진으로 찍어서 공유하면 현장에 없는 사람도 출석할 수 있고, 토큰 구조가 `숫자-숫자-숫자`라서 패턴을 알면 위조도 가능했습니다. 기밀성, 무결성, 인증 세 가지가 다 없었습니다.

### 시간 기반 토큰으로 개선 시도 (8~9월)

만료 시간을 넣어서 "사진 공유" 문제는 좀 줄여보려 했습니다. 근데 토큰 생성 로직 자체가 `labId + sessionId + timestamp`라서, 이 패턴을 알면 여전히 유효한 토큰을 만들 수 있었습니다.

이때 커밋 메시지 중에 `feat: 일단 QR 에러는 안뜸`이라는 게 있는데, 당시 심정이 잘 드러납니다. 에러가 안 뜨게 만드는 데 급급했습니다.

### AES-256-GCM 암호화 적용 (10월, `95cf9dd`)

결국 제대로 된 암호화가 필요하다는 걸 깨달았습니다. QR 토큰에 필요한 보안 속성을 정리해보니 세 가지였습니다.

1. **기밀성**: 내용을 읽을 수 없어야 함 → 대칭키 암호화(AES)
2. **무결성**: 변조되면 감지해야 함 → 인증 태그(GCM의 Authentication Tag)
3. **인증**: 서버 비밀키 없이 생성 불가 → 비밀키 기반 암호화

AES-CBC도 생각해봤지만, CBC는 기밀성만 제공하고 무결성은 HMAC을 별도로 붙여야 합니다. 순서를 잘못하면(Encrypt-then-MAC이 아닌 MAC-then-Encrypt) 취약점이 생긴다고 해서, 암호화 + 무결성을 한번에 해주는 GCM 모드를 선택했습니다.

```java
// SecureQRToken.java - AES-256-GCM 암호화
private static String encryptPayload(String payload, String secretKey) {
    SecretKeySpec keySpec = new SecretKeySpec(
        secretKey.getBytes(StandardCharsets.UTF_8), "AES"   // 32바이트 = 256비트 키
    );
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

    // 랜덤 12바이트 IV — 같은 데이터도 매번 다른 암호문 생성
    byte[] iv = new byte[12];
    new SecureRandom().nextBytes(iv);
    GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);  // 128비트 인증 태그

    cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
    byte[] encryptedData = cipher.doFinal(
        payload.getBytes(StandardCharsets.UTF_8)
    );

    // IV(12) + 암호문 + 인증태그 → Base64 URL-safe 인코딩
    byte[] result = new byte[iv.length + encryptedData.length];
    System.arraycopy(iv, 0, result, 0, iv.length);
    System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(result);
}
```

```java
// 복호화 시 GCM이 자동으로 인증 태그 검증
// → 1비트라도 변조되면 AEADBadTagException 발생
cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
byte[] plaintext = cipher.doFinal(ciphertext);
```

### 보안 속성 비교

| 속성 | 레거시 (평문) | 최종 (AES-256-GCM) |
|------|:---:|:---:|
| 기밀성 (내용 읽기 불가) | ✗ | ✓ AES-256 암호화 |
| 무결성 (변조 감지) | ✗ | ✓ 128비트 인증 태그 |
| 인증 (위조 불가) | ✗ | ✓ 서버 비밀키 필요 |
| 재사용 방지 | ✗ | ✓ 만료 시간 + nonce |
| 패턴 예측 불가 | ✗ | ✓ 랜덤 IV |

### 레거시 호환

이미 기존 QR 코드가 남아있는 환경을 위해, 복호화 시 Secure 포맷을 먼저 시도하고 실패하면 Legacy로 폴백하는 구조를 적용했습니다.

```yaml
# application.yml
rankus:
  qr:
    secret-key: ${QR_SECRET_KEY:LocalDevQRSecretKey_2025__32__OK}
    legacy-enabled: true  # 마이그레이션 기간 동안 레거시 허용
```

비밀키는 환경변수로 관리하고, GitHub Actions에서 빌드 전에 키 길이(32바이트)를 검증합니다.

```yaml
# GitHub Actions - 빌드 전 키 길이 검증
- name: Validate QR_SECRET_KEY length
  run: |
    KEY_LENGTH=$(echo -n "${{ secrets.QR_SECRET_KEY }}" | wc -c)
    if [ "$KEY_LENGTH" -ne 32 ]; then
      echo "ERROR: QR_SECRET_KEY must be exactly 32 bytes"
      exit 1
    fi
```

### 돌아보면

`일단 QR 에러는 안뜸`이라는 커밋 메시지가 참 부끄럽지만, 그게 당시의 솔직한 상태였습니다. 보안은 "동작 검증 먼저, 강화는 나중에"라는 순서 자체는 맞았지만, "나중에"가 너무 늦으면 안 된다는 걸 느꼈습니다.

---

## 4. 권한 시스템 GLOBAL_004 에러

### 솔직히 제일 힘들었던 부분

권한 시스템은 이 프로젝트에서 가장 많이 헤맸던 부분입니다. Spring Security의 권한 체계를 충분히 이해하지 못한 상태에서 6단계 Role + 도메인별 권한이라는 복잡한 구조를 만들려고 했습니다. 결과적으로 SecurityConfig를 12번이나 수정했고, 권한 관련 에러를 고치느라 커밋이 수없이 쌓였습니다.

### 배포하고 나서 터진 문제

배포 후 특정 API에서 403 Forbidden(GLOBAL_004) 에러가 발생했습니다. JWT 토큰은 유효하고, Role도 맞는데 `@PreAuthorize`에서 권한 체크가 실패했습니다.

### 원인: 몰랐던 ROLE_ 접두사 규칙

JWT 토큰 생성과 권한 체크 사이에 접두사 처리가 안 맞았습니다.

```java
// 토큰 생성 시: "LAB_LEADER" (접두사 없음)
.claim("role", user.getRole().name())

// 권한 체크 시: Spring Security의 hasRole()은 자동으로 "ROLE_" 접두사를 추가
@PreAuthorize("hasRole('LAB_LEADER')")
// → 내부적으로 "ROLE_LAB_LEADER"를 찾음
// → 토큰에는 "LAB_LEADER"만 있어서 매칭 실패
```

`hasRole()`은 자동으로 "ROLE_" 접두사를 붙이는데, 이걸 몰랐습니다. 개발 환경(`dev` 프로필)에서는 모든 요청을 허용하고 있었기 때문에, 배포할 때까지 이 문제를 발견하지 못했습니다.

### 5연속 커밋으로 해결 (밤 10시 ~ 11시 반)

```
eb7310f → 5a2d0c2 → 776aa53 → 052d717 → 9f1a76c
```

git log를 보면 이 5개 커밋이 2025년 10월 21일 밤 10시 17분부터 11시 41분까지, 1시간 24분 동안 연속으로 올라갔습니다. 접두사 문제만 고치면 끝일 줄 알았는데, 손대다 보니 권한 로직 전체가 산발적이라는 걸 발견했습니다. 그래서 이 기회에 도메인 기반으로 전면 재설계했습니다.

```java
// 수정: Authentication 생성 시 ROLE_ 접두사 추가
var authorities = List.of(
    new SimpleGrantedAuthority("ROLE_" + roleFromToken)
);
```

```java
// 도메인 권한 핸들러의 fast path
for (GrantedAuthority ga : auth.getAuthorities()) {
    String a = ga.getAuthority();
    if ("ROLE_ADMIN".equals(a) || "ROLE_PROFESSOR".equals(a)) {
        return true;  // 상위 역할은 즉시 통과
    }
}
// 나머지는 도메인 객체의 권한 로직으로 판단
```

이 과정에서 `ControllerSecuritySmokeTests`도 추가했고, 927줄짜리 PERMISSION_ANALYSIS.md 문서를 작성해서 권한 체계를 정리했습니다. 새벽에 이 문서를 쓰면서 "처음부터 이렇게 정리하고 시작했으면 훨씬 수월했을 텐데"라는 생각이 들었습니다.

### 돌아보면

보안 관련 기능은 "만들면서 배우자"가 통하지 않았습니다. `hasRole()` vs `hasAuthority()` 같은 기본을 먼저 이해했어야 했습니다. 개발 환경에서 모든 요청을 허용한 것도, 편하긴 했지만 버그를 배포까지 숨기는 부작용이 있었습니다.

다만 밤새 5연속 커밋으로 전면 개편한 건, 어려운 부분을 회피하지 않고 끝까지 파고든 경험이었습니다. 이때 Spring Security의 인증·인가 흐름을 가장 깊이 이해하게 됐습니다.

---

## 5. Google → Mailjet SMTP 마이그레이션

### 왜 바꿨는가

Google SMTP를 쓰고 있었는데, 일일 발송 한도(500건)에 테스트 중에도 걸리기 시작했습니다. 앱 비밀번호 관리도 번거로웠습니다.

SendGrid, Mailgun, Mailjet을 비교했는데, Mailjet이 무료 플랜(200건/일 + 6,000건/월)이 학교 프로젝트에 충분했고, SMTP 프로토콜 호환이라 기존 코드를 그대로 쓸 수 있었습니다.

### 바꾸는 과정

```
commit 17217f4  feat: 이메일 인증 기능 구현 (Google SMTP)
commit f1cd963  fix: 이메일 발송 서비스 마이그레이션 (Google → Mailjet)
commit c1a2e11  feat: 이메일 인증 의무화 적용
```

실제로 바꾼 건 `application.yml`의 설정값뿐이었습니다.

```yaml
# Before (Google SMTP)
spring.mail:
  host: smtp.gmail.com
  port: 587
  username: ${GMAIL_USERNAME}
  password: ${GMAIL_APP_PASSWORD}

# After (Mailjet SMTP) - 설정값만 교체
spring.mail:
  host: in-v3.mailjet.com
  port: 587
  username: ${MAILJET_API_KEY}
  password: ${MAILJET_SECRET_KEY}
```

헥사고날 아키텍처의 `SpringMailSender`(Adapter) 설정만 바꾸니 도메인과 서비스 코드는 한 줄도 안 건드렸습니다.

마이그레이션과 함께 이메일 인증 제약도 추가했습니다.

```java
// EmailVerification 엔티티
@Column(nullable = false, length = 100)
private String email;              // @hs.ac.kr 도메인만 허용

@Column(nullable = false, length = 6)
private String verificationCode;   // SecureRandom 기반 6자리

@Column(nullable = false)
private LocalDateTime expiryTime;  // 발급 후 5분

@Column(nullable = false)
private int sendCount;             // 시간당 최대 5회 발송 제한
```

### 돌아보면

이 마이그레이션은 헥사고날 아키텍처가 정말 빛난 유일한 순간이었습니다. 프로젝트 전체에서 헥사고날 때문에 생긴 보일러플레이트 부담은 컸지만([ARCHITECTURE.md](ARCHITECTURE.md#1-헥사고날-아키텍처-전환) 참고), 이때만큼은 "어댑터만 바꾸면 된다"는 이론이 진짜로 동작하는 걸 직접 경험했습니다. 이런 교체가 잦은 영역에만 선택적으로 적용했으면 부담도 줄었을 거라는 생각이 들었습니다.

---

## 6. CORS 설정과 역직렬화 문제

### CORS 문제 (`fe3f462`)

커밋 메시지가 `hotfix: cors 설정`이었습니다. React 팀과 처음 연동하는데 CORS 에러가 터졌습니다. 그동안 Swagger UI로만 테스트해서 전혀 몰랐던 문제였습니다.

```java
public CorsConfigurationSource corsConfigurationSource() {
    var config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

### 역직렬화 문제 (`a446c4f`)

Java Record를 Request DTO로 쓰는데, Jackson이 생성자를 못 찾아서 역직렬화가 안 됐습니다.

```java
// Before: 역직렬화 실패
public record LabApplicationSlotRequestDto(Long slotId, String message) {}

// After: @JsonCreator 명시
public record LabApplicationSlotRequestDto(
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    Long slotId,
    String message
) {}
```

### 돌아보면

CORS는 프론트엔드 연동 시작할 때 바로 세팅했어야 했습니다. "나중에 처리하겠다"고 미뤘더니 연동 첫날에 막혀서 `hotfix`로 급하게 올렸습니다. 프로젝트 초기 SecurityConfig에서 같이 설정하는 게 맞습니다.

---

## 관련 문서

- [ARCHITECTURE.md](ARCHITECTURE.md) — 아키텍처 선택의 상세한 근거와 설계 의도
- [RETROSPECTIVE.md](RETROSPECTIVE.md) — 각 선택에 대한 솔직한 회고와 다시 시작한다면
- [DEPLOYMENT.md](DEPLOYMENT.md) — 배포 환경에서 발생한 문제들 (키 검증 등)
- [README.md](../README.md) — 프로젝트 전체 개요
