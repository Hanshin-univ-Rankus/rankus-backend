# Key Technical Insights from Rankus Code Evolution

> Based on actual code diffs, not commit messages

---

## 1. The Hexagonal Architecture Transformation

### Why It Mattered

The transition from traditional layered to hexagonal architecture (June 8, 2025) wasn't cosmetic—it was **structural**. The code went from:

```
❌ BEFORE: Domain tied to JPA implementation details
  └─ Services returned DTOs (hiding domain)
  └─ Repositories were SpringData-only
  └─ Controllers directly called Services
  └─ Permission checks scattered everywhere

✅ AFTER: Domain as pure business logic
  └─ Services return domain models
  └─ Repositories abstracted through ports
  └─ Controllers call UseCase interfaces
  └─ Permission checks centralized
```

### The Numbers Tell the Story

**-4,495 lines deleted, +1,627 lines added = Net reduction of 60% despite feature parity**

What was cut:
- Redundant DTO mapping boilerplate
- Mixed concerns in service layer
- Anemic domain models
- Framework-coupled repositories

What was added:
- Port interfaces (input/output)
- Adapter implementations
- Rich domain models with business logic
- Test infrastructure for new patterns

### Real Code Impact

**Before:** User saving required DTO conversion
```java
@Service public class UserService {
    public UserResponseDto signup(SignupRequest req) {
        User u = new User(req.getName(), req.getEmail(), req.getPassword());
        User saved = repo.save(u);
        return new UserResponseDto(saved);  // ✗ DTO mapping here
    }
}
```

**After:** Domain model is the contract
```java
@Service @RequiredArgsConstructor
public class UserCommandService implements UserCommandUseCase {
    private final UserRepositoryPort repository;

    @Override
    public void signup(String name, String email, String rawPassword) {
        Password pwd = Password.fromRaw(rawPassword, encoder);
        User user = new User(name, email, pwd);
        repository.save(user);  // ✓ Save domain directly
    }
}

// Controller decides how to represent it to client
@RestController @RequiredArgsConstructor
public class AuthController {
    private final UserCommandUseCase useCase;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest req) {
        useCase.signup(req.getName(), req.getEmail(), req.getPassword());
        return ResponseEntity.ok(new SignupResponse("완료"));  // ✓ Conversion at boundary
    }
}
```

---

## 2. The Permission System Evolution: Three Phases

### Phase 1: Simple Role Checks (Naive)
```java
// ✗ PHASE 1: Scattered throughout code
if (!user.getRole().equals(Role.ADMIN)) {
    throw new PermissionException();
}
if (user.getRole().equals(Role.LAB_LEADER) && user.getLab().equals(targetLab)) {
    // allowed
}
```

**Problem:** Logic duplicated everywhere, hard to audit, breaks with new roles

### Phase 2: @PreAuthorize Annotations (Better)
```java
// ✓ PHASE 2: Centralized to method level
@PreAuthorize("hasRole('ADMIN') or hasRole('LAB_LEADER')")
@PostMapping("/{labId}/approve")
public ResponseEntity<?> approve(@PathVariable Long labId, @RequestBody ApproveRequest req) {
    // Can still access wrong lab's data!
}
```

**Problem:** Still doesn't understand domain (can approve wrong lab's students)

### Phase 3: Domain-Aware Permission Handlers (Correct)
```java
// ✓ PHASE 3: Domain understands its own permissions
@Component @RequiredArgsConstructor
public class LabApplicationPermissionHandler implements DomainPermissionEvaluator {

    @Override public boolean hasPermission(Authentication auth,
                                           Serializable targetId,
                                           String permission) {
        // 1. Check token-level roles first
        if (hasRole(auth, "ADMIN")) return true;

        // 2. Load domain object and check domain-level permissions
        LabApplication app = queryUseCase.getApplicationById((Long) targetId);
        User user = userQueryUseCase.getUserById(userId);

        // 3. Ask the domain if this user can perform this action
        switch (permission) {
            case "APPROVE":
                return user.canManageLabApplications(app.getLab());
            case "CANCEL":
                return user.isAdmin() || app.isOwnedBy(userId);
            // ...
        }
    }
}

// User domain model knows its own permissions
@Entity public class User {
    public boolean canManageLabApplications(Lab lab) {
        return (role == Role.LAB_LEADER || role == Role.LAB_MANAGER)
            && this.lab.equals(lab);
    }
}
```

### Why This Matters

| Aspect | Phase 1 | Phase 2 | Phase 3 |
|--------|---------|---------|---------|
| **Testability** | ✗ Hard to mock | ✓ Use @MockBean | ✓ Pure functions |
| **Domain Understanding** | ✗ None | ✓ Limited | ✓ Complete |
| **Reusability** | ✗ Copy-paste | ✓ @PreAuthorize | ✓ Handlers |
| **Auditability** | ✗ Scattered code | ✓ Annotations | ✓ Explicit logic |
| **Security** | ✗ Easy to bypass | ✓ Decent | ✓ Defense in depth |

---

## 3. JWT Refresh Tokens: The Hard Way to Learn

### The Journey (3 Attempts)

#### Attempt 1: Testing Phase (Aug 7)
```yaml
jwt:
  access-token-expiration: 60000      # 1 minute
  refresh-token-expiration: 600000    # 10 minutes
```
**Result:** Too short for development. ❌

#### Attempt 2: Quick Fix (Aug 7) - WRONG
```yaml
jwt:
  access-token-expiration: 604800000  # 7 days
  refresh-token-expiration: 604800000 # 7 days
```
**Problem:** This DEFEATS the purpose of refresh tokens—no rotation happens. ❌

#### Attempt 3: Correct Implementation (Aug 9) ✓
```java
// Server maintains refresh tokens in database
@Entity @Table(name = "refresh_tokens")
public class RefreshToken {
    @Id private Long id;
    @Column(nullable = false, unique = true)
    private String token;  // ✓ Stored and tracked
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

// Logout revokes tokens
@Entity @Table(name = "blacklisted_tokens")
public class BlacklistedToken {
    @Column(nullable = false, unique = true)
    private String token;  // ✓ Blacklisted tokens can't be used
}

// Generate short-lived access tokens
@Component public class JwtTokenProvider {
    public String generateAccessToken(User user) {
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))  // ✓ 1 hour
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    // Rotation endpoint
    public String refreshAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new TokenException("Invalid refresh token"));

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenException("Refresh token expired");
        }

        // Generate new access token from stored refresh token
        return generateAccessToken(token.getUser());
    }
}
```

### What We Learned

1. **Stateless JWT needs stateful refresh tokens**
   - Access tokens: Short-lived, stateless, fast validation
   - Refresh tokens: Long-lived, stateful (DB), revocable

2. **Rotation beats long expiry**
   - Wrong: 7-day tokens (vulnerable, can't revoke mid-session)
   - Right: 1-hour tokens renewed via refresh token

3. **Logout requires revocation**
   - Without blacklist: Old tokens still valid until expiry
   - With blacklist: Immediate logout on all devices

4. **Security vs Usability trade-off**
   - Very short access tokens (5 min): Secure but annoying UX
   - 1-hour tokens: Balance of security and usability
   - Refresh endpoint must be efficient to avoid latency

---

## 4. Concurrency Control: Race Conditions Are Real

### The Problem (July 22, 2025)

Three students try to book the last interview slot simultaneously:

```
TIME    STUDENT 1           STUDENT 2           STUDENT 3
────────────────────────────────────────────────────────────
T1      SELECT * FROM interview_slots WHERE id=1
        Result: booked=2, capacity=3 ✓ available
                    │
T2                          SELECT * FROM interview_slots WHERE id=1
                            Result: booked=2, capacity=3 ✓ available
                                        │
T3                                                  SELECT * FROM interview_slots WHERE id=1
                                                    Result: booked=2, capacity=3 ✓ available

T4      UPDATE interview_slots SET booked=3 WHERE id=1
        ✓ Success
                    │
T5                          UPDATE interview_slots SET booked=3 WHERE id=1
                            ✓ Success (overwrites!)
                                        │
T6                                                  UPDATE interview_slots SET booked=3 WHERE id=1
                                                    ✓ Success (overwrites again!)

RESULT: All 3 students booked the same slot! ❌
        Database shows booked=3 (not 4!)
        Overbooking by 2 students
```

### The Solution (Pessimistic Locking)

```java
// MySQL: SELECT ... FOR UPDATE
@Repository
public interface SpringDataInterviewSlotRepository extends JpaRepository<InterviewSlot, Long> {

    @Query("SELECT s FROM InterviewSlot s WHERE s.slotId = :slotId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)  // ✓ Row-level lock
    Optional<InterviewSlot> findByIdForUpdate(@Param("slotId") Long slotId);
}

// With retry logic for lock contention
@Service @RequiredArgsConstructor
@Transactional
public class LabApplicationCommandService {

    @Retryable(
        value = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)  // ✓ Exponential backoff
    )
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LabApplication applyToLabWithSlot(Long labId, Long userId, Long slotId) {

        // ✓ Row lock acquired here - other threads wait
        InterviewSlot slot = slotRepositoryPort.findByIdForUpdate(slotId)
            .orElseThrow(() -> new InterviewNotFoundException("Not found"));

        // ✓ Re-check after acquiring lock (crucial!)
        if (!slot.isAvailable()) {
            throw new LabApplicationValidationException("Slot full");
        }

        // ✓ Update within lock scope - atomic
        slot.book();
        LabApplication app = new LabApplication(lab, user, slot);
        return labApplicationRepositoryPort.save(app);

        // ✓ Lock released when transaction commits
    }
}
```

### With This Fix:

```
TIME    STUDENT 1                   STUDENT 2                   STUDENT 3
────────────────────────────────────────────────────────────────────────────
T1      SELECT * ... FOR UPDATE
        ✓ Lock acquired
                        │
T2                      SELECT * ... FOR UPDATE
                        ⏳ Waiting for lock...
                                        │
T3                                      SELECT * ... FOR UPDATE
                                        ⏳ Waiting for lock...

T4      Check: booked=2, capacity=3 ✓ available
        UPDATE: booked=3
        COMMIT (lock released)
                        │
T5                      Lock acquired (got T1's old lock)
                        Check: booked=3, capacity=3 ✗ NOT available
                        THROW: SlotNotAvailableException
                        ROLLBACK
                                        │
T6                      Lock acquired (got T2's old lock)
                        Check: booked=3, capacity=3 ✗ NOT available
                        THROW: SlotNotAvailableException
                        ROLLBACK

RESULT: Only STUDENT 1 books slot, others get error ✓
        Database: booked=3 (correct count)
```

### Key Insights on Concurrency

1. **Optimistic locking insufficient for scarce resources**
   - Good for: General data conflicts (rare)
   - Bad for: Limited inventory (booking, registration)

2. **Pessimistic locking trades latency for correctness**
   - Slower: Threads wait for lock
   - Safer: Impossible to double-book

3. **Retry with exponential backoff prevents thundering herd**
   - Without: All 100 users retry immediately → DOS
   - With: Staggered retries (100ms, 200ms, 400ms)

4. **Re-check after acquiring lock**
   - Between SELECT and FOR UPDATE, another thread might modify
   - Always verify condition after lock acquired

---

## 5. QR Token Security: From Readable to Cryptographically Secure

### The Evolution

#### Legacy Format (July-Sept 2025): VULNERABLE ❌
```java
public class QRToken {
    // Plain concatenation - anyone can read
    public String toQRString() {
        return sessionId + "|" + labId + "|" + generatedAt + "|" + expiresAt;
    }

    public static QRToken fromString(String token) {
        String[] parts = token.split("\\|");
        return new QRToken(
            Long.parseLong(parts[0]),  // Can see session ID
            Long.parseLong(parts[1]),  // Can see lab ID
            LocalDateTime.parse(parts[2]),
            LocalDateTime.parse(parts[3])
        );
    }
}
```

**Problems:**
1. **No confidentiality** - Anyone scanning sees lab ID and session ID
2. **No integrity** - Can modify numbers in QR (change session ID)
3. **No authentication** - Can forge completely fake QR tokens
4. **Replay attacks** - Old tokens still valid until expiry

#### Secure Format (Oct 2025): CRYPTOGRAPHICALLY SOUND ✓
```java
public class SecureQRToken {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    // Encrypt with AES-256-GCM
    public String encrypt(QRTokenPayload payload, String encryptionKey) throws Exception {
        // 1. Generate random IV for this encryption
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[GCM_IV_LENGTH];
        random.nextBytes(iv);

        // 2. Prepare plaintext JSON
        String plaintext = String.format(
            "{\"labId\":%d,\"sessionId\":%d,\"generatedAt\":\"%s\",\"expiresAt\":\"%s\"}",
            payload.getLabId(),
            payload.getSessionId(),
            payload.getGeneratedAt(),
            payload.getExpiresAt()
        );

        // 3. Encrypt with AES-256-GCM
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(
            encryptionKey.getBytes(StandardCharsets.UTF_8), 0, 32, "AES"  // ✓ 256-bit key
        );
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);  // ✓ 128-bit tag

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // 4. Return: IV + Ciphertext + Authentication Tag (GCM adds tag automatically)
        byte[] encryptedData = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedData);
    }

    // Decrypt with verification
    public QRTokenPayload decrypt(String encryptedToken, String encryptionKey) throws Exception {
        // 1. Decode from Base64
        byte[] decodedData = Base64.getUrlDecoder().decode(encryptedToken);

        // 2. Extract IV (first 12 bytes) - unique for each encryption
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, GCM_IV_LENGTH);

        // 3. Extract ciphertext (remaining bytes including GCM tag)
        byte[] ciphertext = new byte[decodedData.length - GCM_IV_LENGTH];
        System.arraycopy(decodedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        // 4. Decrypt (GCM automatically verifies tag)
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(
            encryptionKey.getBytes(StandardCharsets.UTF_8), 0, 32, "AES"
        );
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        byte[] plaintext = cipher.doFinal(ciphertext);  // ✓ Throws if tag doesn't match

        // 5. Parse and return
        String json = new String(plaintext, StandardCharsets.UTF_8);
        JSONObject obj = new JSONObject(json);
        return new QRTokenPayload(
            obj.getLong("labId"),
            obj.getLong("sessionId"),
            LocalDateTime.parse(obj.getString("generatedAt")),
            LocalDateTime.parse(obj.getString("expiresAt"))
        );
    }
}

// Dual-format support with fallback
@Component @RequiredArgsConstructor
public class AttendanceQRController {

    @GetMapping("/qr/resolve")
    public ResponseEntity<?> resolve(@RequestParam("token") String token) {
        // Try SECURE format first
        try {
            SecureQRToken.QRTokenPayload payload = qrService.decrypt(token);
            return buildResponse("SECURE", payload);
        } catch (Exception e) {
            // Fall back to LEGACY for backwards compatibility
        }

        // Try LEGACY format
        try {
            QRToken legacy = QRToken.fromString(token);
            return buildResponse("LEGACY", legacy.toPayload());
        } catch (Exception e) {
            return buildErrorResponse("Invalid token");
        }
    }
}
```

### Security Properties Achieved

| Property | Legacy | Secure |
|----------|--------|--------|
| **Confidentiality** | ✗ (readable) | ✓ (AES-256) |
| **Integrity** | ✗ (modifiable) | ✓ (GCM tag) |
| **Authenticity** | ✗ (forgeable) | ✓ (authenticated encryption) |
| **Replay Protection** | ✗ | ✓ (expiration checked at decrypt) |
| **Non-repudiation** | ✗ | ✓ (token tied to server key) |
| **Key Rotation** | ✗ | ✓ (with new key) |

### Why GCM (Galois/Counter Mode)?

1. **Authenticated Encryption**
   - Automatically authenticates ciphertext
   - Decryption fails if even 1 bit is tampered

2. **No Padding Oracle Attacks**
   - GCM doesn't use padding
   - Immune to padding oracle vulnerabilities

3. **Parallelizable**
   - Can encrypt multiple blocks concurrently
   - Better performance than CBC

4. **IV Requirements**
   - Random IV for each encryption (✓ we do this)
   - Never reuse same (key, IV) pair (✓ random IV ensures this)

---

## 6. Test Factory Pattern for Consistency

### The Pattern (Evolved During Project)

```java
// Domain Factory - creates test domain objects
@Component
public class DomainLabFactory {

    public Lab createLab() {
        return new Lab(
            "테스트 랩실",
            "This is a test lab",
            "컴퓨터과학과",
            LabCategory.AI
        );
    }

    public Lab createLabWithApplications(int count) {
        Lab lab = createLab();
        for (int i = 0; i < count; i++) {
            LabApplication app = new LabApplication(
                lab,
                createUser(),
                createInterviewSlot()
            );
            // Can't directly add due to JPA, would need save
        }
        return lab;
    }

    public User createUser() {
        return new User(
            "테스트사용자",
            "test" + System.nanoTime() + "@example.com",  // ✓ Unique email
            Password.fromRaw("password123", passwordEncoder)
        );
    }

    public User createAdmin() {
        User admin = createUser();
        admin.changeRole(Role.ADMIN);
        return admin;
    }

    public InterviewSlot createInterviewSlot() {
        Interview interview = new Interview(
            "테스트 면접",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2)
        );
        return new InterviewSlot(
            interview,
            LocalDateTime.now().plusDays(1),
            3  // capacity
        );
    }
}

// Usage in tests
@Test
public void testLabApplicationApproval() {
    // Arrange
    Lab lab = labFactory.createLab();
    User applicant = labFactory.createUser();
    User labLeader = labFactory.createAdmin();
    labLeader.changeRole(Role.LAB_LEADER);
    labLeader.assignLab(lab);

    InterviewSlot slot = labFactory.createInterviewSlot();
    LabApplication app = new LabApplication(lab, applicant, slot);

    // Act
    service.approveApplication(app.getApplicationId());

    // Assert
    assertTrue(app.isApproved());
}
```

### Why This Matters

1. **Consistency**: All test labs have same properties
2. **DRY**: Don't repeat setup code in 100 tests
3. **Readability**: `labFactory.createAdmin()` is clear
4. **Maintainability**: Change one factory, all tests benefit
5. **Flexibility**: Can create variations easily

---

## Key Takeaways

| Lesson | Context | Application |
|--------|---------|-------------|
| **Architecture shapes testability** | Hexagonal vs Layered | Ports make domain testable independently |
| **Security is layered** | Permission system | URL level + Method level + Domain level |
| **Stateless JWT needs stateful refresh** | Token management | Short-lived tokens + long-lived refresh tokens |
| **Concurrency requires pessimism** | Race conditions | SELECT FOR UPDATE for scarce resources |
| **Encryption is necessary** | QR tokens | AES-256-GCM provides confidentiality + integrity + authenticity |
| **Test infrastructure enables quality** | Test factories | Consistent, readable, maintainable tests |

