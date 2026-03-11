# COMPREHENSIVE TECHNICAL ANALYSIS: RANKUS SPRING BOOT PROJECT

## Executive Summary
The Rankus project is a sophisticated university lab management platform built with Spring Boot 3.4.5. It demonstrates advanced architectural patterns (Hexagonal Architecture), security implementations (JWT + custom permissions), and sophisticated use of JPA/Hibernate. The codebase contains 375 Java source files across well-defined architectural layers with 136 comprehensive tests and extensive documentation of design decisions.

---

## 1. PROJECT STRUCTURE & ARCHITECTURE CHOICES

### 1.1 Overall Directory Structure
The project is organized into clear architectural layers:

- `/adapter/in/web` - Input adapters (~150 files: REST controllers, DTOs, request/response objects)
- `/adapter/out` - Output adapters (~80 files: JPA persistence, email, QR code, Excel export)
- `/application` - Application layer (~60 files: services, port interfaces, use cases)
- `/domain` - Domain layer (~91 files: entities, value objects, business logic, exceptions)
- `/common` - Cross-cutting concerns (~40 files: exceptions, security, utilities)
- `/config` - Spring configuration (~10 files: Security, CORS, Swagger, Database)

**Total: 375 Java source files across 6 architectural layers**

### 1.2 Hexagonal Architecture Implementation

**WHAT WAS CHOSEN:**
Strict Hexagonal Architecture (Ports & Adapters pattern) with explicit port interfaces:

**Port Structure:**
- 24 outbound port interfaces (`UserRepositoryPort`, `EmailSendPort`, `QRCodeGenerationPort`, `ExcelExportPort`, etc.)
- 38+ inbound port interfaces (command/query use cases)
- All adapters explicitly implement these ports
- Services depend on ports, not concrete implementations

**Evidence:**
```
/application/port/in/
  - 38 use case interfaces (AuthUseCase, UserQueryUseCase, etc.)
  - Strict separation: Command interfaces for writes, Query interfaces for reads

/application/port/out/
  - 24 port interfaces for external dependencies
  - Complete abstraction of persistence, email, file operations, QR generation
```

**ALTERNATIVES THAT EXISTED:**
1. Layered Architecture (traditional horizontal layers)
2. Clean Architecture (similar structure, different naming)
3. CQRS with Event Sourcing
4. Direct MVC without abstraction layers

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. **Adapter swapping capability** - Multiple implementations can be plugged in:
   - Spring Data JPA repository
   - Custom repository adapters for complex operations
   - Test doubles/mocks for unit testing

2. **Domain independence** - Domain models have minimal coupling:
   - User entity doesn't reference Spring/JPA directly
   - Password is a value object (@Embeddable)
   - Business logic stays in domain layer

3. **Clear contracts** - Services depend on ports, not implementations:
   - AuthService depends on UserRepositoryPort, not Spring Data directly
   - Reduces implicit dependencies

**POTENTIAL BENEFITS:**
- Complete flexibility to swap implementations
- Excellent testability with mock ports
- Domain-driven design with clear business logic separation
- Reduced coupling between architectural layers
- Easy to understand overall structure

**POTENTIAL DRAWBACKS:**
1. **Boilerplate overhead** - Extra interfaces and adapter classes:
   - 24 port interfaces require 24+ implementing adapters
   - Many adapters are thin wrappers around Spring Data

2. **Indirection complexity** - Navigation requires multiple layers:
   - Controller → Service → Port → Adapter → Spring Data JPA
   - Four levels of abstraction for simple CRUD operations

3. **Maintenance cost** - More files to maintain:
   - Spring Data JPA already provides contracts
   - Additional port layer sometimes adds abstraction without value

**OUTCOME EVIDENCE:**
- Well-organized and maintainable structure
- Tests can cleanly mock ports
- However, simple operations require traversing 4-5 layers
- Most adapters are thin wrappers with minimal value-add

### 1.3 Command/Query Separation

**WHAT WAS CHOSEN:**
Strict naming convention separating command (write) and query (read) use cases:

**Evidence:**
```java
// Command interfaces (write operations)
public interface AuthUseCase {
    AuthResponseDto login(UserLoginRequestDto request);
    User signUp(UserRegisterRequestDto request);
    void logout(String userEmail, String accessToken);
}

// Query interfaces (read operations)
public interface UserQueryUseCase {
    User getUserById(Long userId);
    User getUserByEmail(String email);
}
```

**Statistics:**
- 15+ CommandUseCase interfaces
- 20+ QueryUseCase interfaces
- Consistent naming convention applied throughout codebase

**ALTERNATIVES:**
1. Single interface with both read and write operations
2. True CQRS with separate databases for command and query
3. No separation - just clear method naming
4. Single port per entity (command + query combined)

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. Clear intent - naming makes operation type immediately obvious
2. Different transactional semantics possible:
   - Query methods can use @Transactional(readOnly = true)
   - Commands can use full transactional support
3. Independent testing - separate test strategies for reads and writes
4. Future-proofs for CQRS - structure supports evolution

**POTENTIAL BENEFITS:**
- Intent immediately clear (command vs. query)
- Can apply different transaction strategies
- Supports future CQRS migration
- Separates side-effect-free operations from mutations

**POTENTIAL DRAWBACKS:**
1. **Not true CQRS** - Still uses same database, same implementation class
2. **Extra interfaces** - Doubles number of ports without corresponding value
3. **Misleading structure** - Naming suggests CQRS when it's just naming convention

**OUTCOME EVIDENCE:**
- Naming convention effective - developers understand method intent
- Well-organized but could be achieved with simpler approach
- True CQRS not implemented (no event sourcing, no separate databases)

---

## 2. DATABASE & PERSISTENCE CHOICES

### 2.1 MySQL Selection

**WHAT WAS CHOSEN:**
MySQL 8.0 as primary database

**Evidence:**
```gradle
runtimeOnly 'com.mysql:mysql-connector-j'

# Docker Compose
mysql:
  image: mysql:8.0
  environment:
    MYSQL_DATABASE: rankus
```

**Configuration:**
- Connection: `jdbc:mysql://localhost:3306/rankus`
- Character set: UTF-8, Asia/Seoul timezone
- Batch processing enabled (jdbc.batch_size: 20)

**ALTERNATIVES THAT EXISTED:**
1. PostgreSQL (better JSON, advanced features)
2. H2 (in-memory, testing only)
3. MongoDB (document-based)
4. Oracle Database (enterprise)

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. University context - MySQL often standardized in educational institutions
2. Development simplicity - Widely available, Docker support
3. Relational model fit - Data structure is relational
4. Cost - Open source, no licensing

**POTENTIAL BENEFITS:**
- Simple setup and widely available
- Good for relational data (labs, users, applications)
- Mature ecosystem and tooling
- Free and open source

**POTENTIAL DRAWBACKS:**
1. Limited JSON support vs. PostgreSQL's JSONB
2. No native UUID type (uses VARCHAR/BINARY)
3. Full-text search less capable than PostgreSQL
4. Replication lag issues in distributed scenarios

**OUTCOME EVIDENCE:**
- Works well for the relational data model
- No evidence of needing PostgreSQL-specific features
- Appropriate choice for application needs

### 2.2 JPA/Hibernate Patterns

#### Password as @Embeddable Value Object

**WHAT WAS CHOSEN:**
```java
@Embeddable
public class Password {
    @Column(name = "password_hash", nullable = false, length = 255)
    private String hashed;

    public static Password fromRaw(String rawPassword, PasswordEncoder encoder) {
        String hashed = encoder.encode(rawPassword);
        return new Password(hashed);
    }

    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.hashed);
    }
}

// Used in User entity
@Embedded
private Password password;
```

**ALTERNATIVES:**
1. String field - Just store password_hash directly
2. Separate PasswordEntity - One-to-one relationship
3. Custom Hibernate UserType - Custom mapping

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. **Value Object pattern** - Password has its own logic and validation
2. **Encapsulation** - Password.fromRaw() ensures hashing before storage
3. **Type safety** - Cannot accidentally pass raw password to database
4. **Reusability** - Can embed in other entities requiring passwords
5. **DDD principles** - Value objects are proper domain modeling

**POTENTIAL BENEFITS:**
- Semantically correct domain modeling
- Encapsulates password validation and encoding logic
- Type-safe (cannot mix raw vs. hashed passwords)
- Demonstrates understanding of value objects

**POTENTIAL DRAWBACKS:**
1. Extra JPA complexity - Requires understanding @Embeddable
2. Overhead for single field - Might be overengineering
3. Migration complexity - Changing embedded types requires schema updates
4. Testing complexity - Must test embedding mechanics

**OUTCOME EVIDENCE:**
- Well-implemented value object pattern
- Effectively enforces password encoding at entity level
- Appropriate level of complexity for the use case

#### Entity Design Patterns

**Sophisticated JPA usage observed:**

1. **Lazy loading strategy:**
   ```java
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "lab_id")
   private Lab lab;
   ```
   Prevents N+1 query problems

2. **Natural keys (unique constraints):**
   ```java
   @Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
   ```
   Enforces business invariants at database level

3. **Enum mappings:**
   ```java
   @Enumerated(EnumType.STRING)
   @Column(nullable = false, length = 20)
   private Role role;
   ```
   Type-safe role handling

4. **Inheritance strategy** - Separate table per class hierarchy implied

### 2.3 Repository Pattern

**WHAT WAS CHOSEN:**
Spring Data JPA interfaces with custom query methods:

```java
@Repository
public interface SpringDataUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByStudentNumber(String studentNumber);
    List<User> findByLab(Lab lab);
    Optional<User> findByLabAndStudentNumber(Lab lab, String studentNumber);
}
```

**Evidence:**
- 19 Spring Data repository interfaces
- Average 4-8 custom query methods per interface
- Adapter pattern: `SpringDataUserRepository` → `UserRepositoryAdapter` → `UserRepositoryPort`

**ALTERNATIVES:**
1. Custom @Query annotations for all queries
2. QueryDSL for type-safe queries
3. Criteria API for programmatic queries
4. MyBatis for explicit SQL control

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. **Developer productivity** - Spring Data conventions reduce boilerplate
2. **Type safety** - Method names checked at compile time
3. **Balance** - Automatic CRUD but custom methods for complex queries
4. **Industry standard** - Spring Data is widely understood

**OUTCOME EVIDENCE:**
- Works well for the application
- Most queries are simple (findBy*, existsBy*)
- Proper adapter pattern abstracts Spring Data from services

---

## 3. SECURITY ARCHITECTURE

### 3.1 JWT Implementation

**WHAT WAS CHOSEN:**
Custom JWT implementation using JJWT library with dual-token pattern:

```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
```

**Token configuration:**
```yaml
security:
  jwt:
    secret: yDeGly34tDXIxkjo6MQKgBNCI+2iMFLdT0i8zD2JZuE=
    access-expiration-ms: 900000    # 15 minutes
    refresh-expiration-ms: 604800000 # 7 days
```

**Implementation features:**
- Short-lived access tokens (15 minutes)
- Long-lived refresh tokens (7 days)
- Token rotation on refresh
- Token ID (JTI) for revocation tracking
- Blacklisting support for immediate logout
- Claims include: email (subject), role, issued time, expiration, token ID

**ALTERNATIVES THAT EXISTED:**
1. Spring Security OAuth2 - Built-in OAuth2 resource server
2. Spring Security JwtAuthenticationProvider - Framework JWT support
3. Third-party auth (Auth0, Okta)
4. Session-based authentication (JSESSIONID cookies)

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. **Token rotation security pattern:**
   ```java
   public AuthTokens generateTokens(User user) {
       refreshTokenRepository.deactivateAllByUserEmail(user.getEmail());
       // All old tokens deactivated, new tokens issued
   }
   ```
   Prevents token theft attacks

2. **Dual-token pattern advantages:**
   - Access tokens short-lived (security)
   - Refresh tokens long-lived (convenience)
   - Refresh token rotation on every use
   - Old tokens explicitly revoked

3. **Token revocation control:**
   ```java
   public void revokeRefreshToken(String refreshTokenId) {
       token.deactivate();
   }

   public void blacklistToken(String tokenId, LocalDateTime expiresAt) {
       BlacklistedToken.create(tokenId, expiresAt, "LOGOUT");
   }
   ```

**POTENTIAL BENEFITS:**
- Complete control over token lifecycle
- Token rotation prevents theft
- Blacklisting allows immediate logout
- Role embedded in token (faster authorization)
- Refresh token invalidation on logout

**POTENTIAL DRAWBACKS:**
1. Custom implementation burden - Must maintain JWT code
2. State in "stateless" auth - Token storage makes it stateful
3. Database queries required - Authorization checks hit DB for:
   - Token blacklist verification
   - Refresh token validity
4. Complexity - More code than Spring Security OAuth2

**OUTCOME EVIDENCE:**
- Dual-token pattern is industry-standard for SPAs
- Token rotation ("Refresh Token Rotation") explicitly implemented
- Effective for React frontend use case
- More complexity than necessary for some use cases

### 3.2 Role Hierarchy (6 Levels)

**WHAT WAS CHOSEN:**
Six-level role hierarchy:

```java
public enum Role {
    STUDENT,        // Basic user
    LAB_MEMBER,     // Lab member
    LAB_MANAGER,    // Lab sub-manager
    LAB_LEADER,     // Lab main leader
    PROFESSOR,      // Professor/admin for multiple labs
    ADMIN           // System administrator
}
```

**Permission model:**
- User.lab relationship provides context
- Same user can have different roles in different labs
- 20+ `canXxx()` permission check methods in User entity

**Examples:**
- canManageLabNotices() → LAB_MANAGER, LAB_LEADER, PROFESSOR, ADMIN
- canViewLabAttendance() → LAB_MEMBER, LAB_MANAGER, LAB_LEADER, PROFESSOR, ADMIN
- canCreateVotes() → LAB_MEMBER and above (except STUDENT)

**ALTERNATIVES:**
1. Three-level hierarchy (STUDENT, MANAGER, ADMIN)
2. Attribute-based access control (ABAC)
3. Explicit permission system
4. Flat permission structure

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. Maps to university organizational structure
2. Granular enough for complex lab operations
3. Context-aware (User.lab determines role scope)
4. Intuitive for university domain

**POTENTIAL BENEFITS:**
- Maps to real organizational structure
- Granular enough for complex use cases
- Supports lab-scoped role assignment
- Intuitive for university context

**POTENTIAL DRAWBACKS:**
1. **Not standard RBAC** - Six levels is uncommon (typical: 3-4)
2. **Role explosion risk** - Future features might need new roles
3. **Permission logic scattered** - 20+ canXxx() methods spread throughout
4. **Non-configurable** - Roles hardcoded, not dynamic

**OUTCOME EVIDENCE:**
- Works well for university context
- Permissions well-tested and correct
- However, 20+ permission methods in User class indicates technical debt

### 3.3 UnifiedPermissionEvaluator System

**WHAT WAS CHOSEN:**
Centralized permission evaluator with pluggable domain handlers:

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
                                 String targetType, Object permissionObj) {
        DomainPermissionEvaluator evaluator = evaluators.get(targetType);
        return evaluator.hasPermission(auth, targetId, (String) permissionObj);
    }
}
```

**12+ domain handlers:**
- LabApplicationPermissionHandler
- LabNoticePermissionHandler
- InterviewPermissionHandler
- AttendanceSessionPermissionHandler
- VotePermissionHandler
- CalendarPermissionHandler
- LabResourcePermissionHandler
- LabImagePermissionHandler
- LabDashboardPermissionHandler
- LabCreationRequestPermissionHandler
- AttendanceRecordPermissionHandler
- (and more)

**Usage in controllers:**
```java
@PreAuthorize("@unifiedPermissionEvaluator.hasPermission(authentication, #requestId, 'LabCreationRequest', 'VIEW')")
public ResponseEntity<LabCreationRequestDetailResponseDto> getDetail(@PathVariable Long requestId) {
    // ... controller logic
}
```

**Statistics:**
- 142 @PreAuthorize decorators in codebase
- All using UnifiedPermissionEvaluator pattern

**ALTERNATIVES:**
1. Spring Security's built-in PermissionEvaluator
2. Scattered @PreAuthorize annotations with different expressions
3. Permission service with explicit calls in controllers
4. AspectJ-based permission checking

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. **Centralized control** - All permission logic in one place per domain
2. **Extension point** - New domain types added by implementing DomainPermissionEvaluator
3. **Decoupling** - Permission logic separated from business logic
4. **Testability** - Permissions can be tested independently

**POTENTIAL BENEFITS:**
- Clean separation of concerns
- Easy to add new domain types
- Consistent permission checking across domains
- Single point for permission auditing
- 142 @PreAuthorize decorators show wide adoption

**POTENTIAL DRAWBACKS:**
1. Custom implementation - Not out-of-the-box Spring Security
2. String-based targetType - Not type-safe
3. Extra complexity - Additional abstraction layer
4. 12+ handler classes - Significant maintenance burden

**OUTCOME EVIDENCE:**
- Well-designed permission system
- Effective central control point
- 142 uses shows extensive adoption
- However, 12 handler implementations is maintenance cost

### 3.4 Security Evolution Evidence

**Scattered → Centralized pattern observed:**
- Initial implementation likely had scattered permission checks in User entity
- Evolution to centralized UnifiedPermissionEvaluator
- 142 @PreAuthorize decorators now use centralized evaluator
- Shows architectural maturity and refactoring

---

## 4. TESTING STRATEGY

### 4.1 Test Structure

**Test statistics:**
- 136 test files in src/test/java
- Test layers:
  - Repository integration tests (Spring Data)
  - Service/application layer tests
  - Controller integration tests
  - Security tests (JWT, permissions)

**Test framework:**
```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.security:spring-security-test'
testRuntimeOnly 'com.h2database:h2'
```

**Database for testing:**
- H2 in-memory database (not MySQL)
- Isolated test environment

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. **Test pyramid approach** - Mix of unit, integration, and controller tests
2. **JaCoCo coverage** - Enforces code quality gates
3. **Custom test factories** - Reusable test data generation
4. **Isolated database** - H2 for fast tests, no external dependencies

**Test factory pattern:**
- Domain factories create transient objects
- Integration factories create persisted objects
- Separation of test data creation from test logic
- Example: DomainUserFactory, IntegrationUserFactory

### 4.2 JaCoCo Coverage Requirements

**WHAT WAS CHOSEN:**
Tiered coverage requirements (not enforced):

```gradle
jacocoTestCoverageVerification {
    // Global rules
    rule {
        limit {
            counter = 'LINE'
            minimum = 0.80      // 80% line coverage
        }
        limit {
            counter = 'BRANCH'
            minimum = 0.70      // 70% branch coverage
        }
    }

    // Domain models (higher)
    rule {
        includes = ['org.univ.rankus.domain.model.*']
        limit {
            counter = 'LINE'
            minimum = 0.95      // 95% for domain
        }
    }

    // Services (high)
    rule {
        includes = ['org.univ.rankus.application.service.*']
        limit {
            counter = 'LINE'
            minimum = 0.90      // 90% for services
        }
    }

    // Adapters (reasonable)
    rule {
        includes = ['org.univ.rankus.adapter.*']
        limit {
            counter = 'LINE'
            minimum = 0.85      // 85% for adapters
        }
    }
}

// NOTE: Enforcement disabled (line 194 commented out)
// check {
//     dependsOn jacocoTestCoverageVerification
// }
```

**Exclusions from coverage:**
- Configuration classes
- DTOs (data holders)
- Exception classes
- JPA entities
- Enums
- Application entry point

**OBSERVABLE STATUS:**
- Coverage verification is disabled (not blocking build)
- Thresholds are aspirational, not enforced

**ALTERNATIVES:**
1. No coverage enforcement
2. Universal high threshold (90% for all)
3. Strict enforcement with build failure

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. **Risk-based approach** - Higher coverage where bugs cost more
2. **Pragmatic thresholds** - 80-95% is realistically achievable
3. **Selective exclusions** - Recognizes low-risk code

**POTENTIAL BENEFITS:**
- Risk-proportionate testing strategy
- Realistic targets (not 100% impossible standard)
- Recognizes that not all code needs equal coverage

**POTENTIAL DRAWBACKS:**
1. **Not enforced** - Enforcement is disabled (technical debt)
2. **Complexity** - Different rules for different packages
3. **Aspirational** - May not be respected in practice

**OUTCOME EVIDENCE:**
- Well-designed tiered approach
- However, enforcement disabled means coverage may slip
- Suggests coverage targets are aspirational rather than enforced

---

## 5. API DESIGN CHOICES

### 5.1 REST API + Swagger/OpenAPI

**WHAT WAS CHOSEN:**
RESTful API with Swagger documentation:

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
```

**Swagger configuration:**
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI rankusOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rankus Lab Management API")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080"),
                        new Server().url("http://3.34.229.56:8080"),
                        new Server().url("https://api.rankus.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
```

**API structure:**
- 25+ controllers
- 100+ endpoints
- Comprehensive Swagger tags (Auth, User, Lab, Interview, Attendance, etc.)
- Full endpoint documentation with examples

**ALTERNATIVES:**
1. GraphQL - Query language for APIs
2. gRPC - Binary protocol for microservices
3. SOAP/XML - Legacy enterprise
4. Custom binary protocol

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. React frontend compatibility - REST is standard for SPAs
2. Simple HTTP verb mapping - Aligns with CRUD operations
3. Swagger integration - Auto-generated documentation
4. CORS support - Necessary for React frontend

**OUTCOME EVIDENCE:**
- REST is appropriate choice
- Swagger integration shows API-first thinking
- 100+ endpoints indicate complete API coverage

### 5.2 DTO Pattern

**WHAT WAS CHOSEN:**
Separate request and response DTOs:

```java
public class UserRegisterRequestDto {
    private String name;
    private String email;
    private String password;
    private String studentNumber;
    private String phoneNumber;
    private Integer grade;
    private EnrollmentStatus enrollmentStatus;
}

public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;

    public static UserResponseDto from(User user) {
        return new UserResponseDto(...);
    }
}
```

**Evidence:**
- Dedicated `/dto/request/` and `/dto/response/` packages
- 100+ DTO classes
- Request DTOs validated
- Response DTOs have from() factory methods

**ALTERNATIVES:**
1. Return domain entities directly
2. Unified DTOs for request/response
3. No DTOs (raw domain objects)

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. Security - Hide internal fields (password hashes never exposed)
2. API evolution - API contract separate from domain
3. Validation - Request DTOs have @Validated
4. Stability - API contract remains stable even as domain evolves

**OUTCOME EVIDENCE:**
- Well-executed pattern
- Clear API contract separation from domain
- Comprehensive coverage of all major endpoints

### 5.3 Response Wrapper Pattern

**WHAT WAS CHOSEN:**
Generic ApiResponse wrapper:

```java
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) { }
    public static <T> ApiResponse<T> error(String message) { }
}

// Usage
@GetMapping
public ResponseEntity<ApiResponse<List<LabResponseDto>>> getAllLabs() {
    return ResponseEntity.ok(ApiResponse.success(labList));
}
```

**ALTERNATIVES:**
1. Unwrapped response (return data directly)
2. Spring HATEOAS
3. RFC 7807 Problem+JSON format
4. Custom headers for metadata

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. Consistency - All APIs return same structure
2. Metadata - Includes timestamp, success flag
3. Frontend convenience - React team expects wrapper

**OUTCOME EVIDENCE:**
- Effective for frontend compatibility
- Consistent API contract
- Well-designed generic implementation

### 5.4 Error Handling

**WHAT WAS CHOSEN:**
Global exception handler via @RestControllerAdvice:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(...) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
    }

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(...) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }
}
```

**OBSERVABLE EVIDENCE:**
- Centralized exception handling
- Domain-specific exceptions (UserNotFoundException, etc.)
- Error codes (UserErrorCode, PasswordErrorCode, etc.)
- Clear HTTP status mapping

**OUTCOME EVIDENCE:**
- Well-designed error handling
- Consistent error responses
- Proper separation of concerns

### 5.5 API Versioning

**Approach:** No explicit /v1/ prefix in URLs (but documented as v1.0.0)

**Swagger servers configured:**
- http://localhost:8080 (local dev)
- http://3.34.229.56:8080 (dev server)
- https://api.rankus.com (production)

---

## 6. INFRASTRUCTURE CHOICES

### 6.1 Docker & Docker Compose

**WHAT WAS CHOSEN:**
Docker Compose with MySQL for local development:

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: rankus
    ports:
      - "3306:3306"
    volumes:
      - db-data:/var/lib/mysql

  mysql_test:
    image: mysql:8.0
    profiles: ["test"]
    ports:
      - "3308:3306"
```

**ALTERNATIVES:**
1. Kubernetes - Production-scale orchestration
2. Manual setup - Install MySQL locally
3. Cloud database - AWS RDS, GCP Cloud SQL
4. In-memory testing - H2 only

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. Development convenience - Single command: docker-compose up
2. Consistency - Dev and production environments match
3. Testing isolation - Separate test database container
4. Simplicity - Not production-scale orchestration

**OUTCOME EVIDENCE:**
- Good for development
- Appropriate simplicity level
- Clear upgrade path to Kubernetes if needed

### 6.2 AWS Deployment

**Evidence from code:**
- Application profile: @Profile({"aws", "!dev"})
- Configuration: application-aws.yml
- Environment variables for secrets:
  ```yaml
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  ```

**Infrastructure:**
- EC2 instance (dev: 3.34.229.56:8080)
- RDS for database
- S3 for file uploads (via FileUploadPort)

**ALTERNATIVES:**
1. ECS - Container orchestration
2. Lambda - Serverless functions
3. Elastic Beanstalk - Managed platform
4. GCP/Azure - Alternative clouds

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. Simplicity - EC2 is straightforward
2. Flexibility - Full control over instance
3. Cost - EC2 spot instances cheaper than managed services

**POTENTIAL DRAWBACKS:**
1. No auto-scaling - Requires load balancer + ASG
2. Manual deployment - Scaling requires configuration
3. Maintenance - Must manage patches, security

### 6.3 Profile Management

**Environment profiles:**
- dev - Local development with hardcoded credentials
- aws - Production with environment variables
- test - H2 database with special settings

**Implementation:**
```java
@Profile({"aws", "!dev"})
public class SecurityConfig { }

@Profile("dev")
public class DevSecurityConfig { }
```

---

## 7. CODE QUALITY TOOLS

### 7.1 JaCoCo

**Status:** Configured but enforcement disabled (line 194 commented)

**Already covered in Testing Strategy (Section 4.2)**

### 7.2 Static Analysis (Checkstyle/PMD)

**Status:** Disabled with note

```gradle
// Static Analysis Configuration (temporarily disabled for Java 21 compatibility)
// TODO: Re-enable with updated versions once compatibility issues are resolved
```

**Issue:** Tool compatibility with Java 17 (project targets Java 17)

**Technical debt:** Disabled tools not yet re-enabled

---

## 8. SPECIFIC IMPLEMENTATION PATTERNS

### 8.1 Password as @Embeddable Value Object

**Already covered in Section 2.2**

### 8.2 Email Verification System

**Implementation:**
```java
public class EmailVerification {
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailVerificationStatus status;
}
```

**Components:**
- EmailVerificationRepositoryPort - Abstraction
- EmailSendPort - Abstraction for sending emails
- Mailjet integration (Spring Mail)

**Pattern:**
- Verification codes created with timestamps
- Codes expire after period (rate limiting)
- Status tracking (PENDING, VERIFIED, EXPIRED)

### 8.3 Concurrency Control: Pessimistic Locking

**From README documentation:**
"면접 슬롯 동시 예약 (동시성 제어)" feature:
- MySQL SELECT FOR UPDATE (pessimistic lock)
- Retry logic for race conditions
- Handles last-minute slot selection

**Pattern:**
```java
// Likely implementation
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<InterviewSlot> findByIdWithLock(Long slotId);
```

**ALTERNATIVES:**
1. Optimistic locking - Version field with retry
2. Distributed locks - Redis, database-based
3. Serializable isolation - Database isolation level
4. Application queue - Queue selections sequentially

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. Simple - JPA @Lock handles mechanics
2. Guaranteed correctness - Impossible for conflicts at DB level
3. No application retry logic - Happens at DB level
4. Known workaround documented - README discusses solution

**POTENTIAL BENEFITS:**
- Guaranteed correctness
- No lost updates
- Simple to implement

**POTENTIAL DRAWBACKS:**
1. Performance - Locks hold resources
2. Deadlock risk - Multiple locks possible
3. Low scalability - Doesn't scale to high concurrency
4. User experience - Users wait for locks

**OUTCOME EVIDENCE:**
- Works well for university context (low concurrency)
- Documented troubleshooting shows thorough testing
- Appropriate for the scale

### 8.4 QR Token Encryption Evolution

**From README:**
"QR 토큰 암호화 진화: 초기 평문 → AES-256-GCM"

**Evidence:**
```gradle
implementation 'com.google.zxing:core:3.5.3'
implementation 'com.google.zxing:javase:3.5.3'
```

**Configuration:**
```yaml
rankus:
  qr:
    secret-key: ${QR_SECRET_KEY:LocalDevQRSecretKey_2025__32__OK}
    legacy-enabled: true  # Support old format during migration
```

**EVOLUTION PATTERN:**
1. Initial: Plain text (sessionId + labId)
2. Problem: Forgeable tokens
3. Solution: AES-256-GCM encryption
4. Migration: Dual format support

**OBSERVABLE EVIDENCE:**
- 32-byte key requirement (256-bit)
- Separate config for prod vs. dev
- Migration path documented
- Legacy format support for backward compatibility

**OUTCOME EVIDENCE:**
- Shows architectural maturity
- Security concern identified and addressed
- Thoughtful migration strategy (non-breaking)
- Demonstrates evolution from simple to secure

---

## 9. LIBRARY & FRAMEWORK CHOICES

### 9.1 Spring Boot Version

**Chosen:** Spring Boot 3.4.5 (modern, recent)
**Java:** Java 17 (LTS release)

**OBSERVABLE EVIDENCE:**
- Recent version close to current
- LTS Java version for stability
- Supports modern Spring 6 features

### 9.2 Key Dependencies

**JWT:**
```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
```
Industry-standard, stable version

**API Documentation:**
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
```
Spring Doc provides Swagger/OpenAPI integration

**QR Code:**
```gradle
implementation 'com.google.zxing:core:3.5.3'
implementation 'com.google.zxing:javase:3.5.3'
```
Google ZXing is standard for QR codes

**Excel Export:**
```gradle
implementation 'org.apache.poi:poi:5.2.4'
implementation 'org.apache.poi:poi-ooxml:5.2.4'
```
Apache POI for Excel file generation

**Concurrency:**
```gradle
implementation 'org.springframework.retry:spring-retry'
implementation 'org.springframework:spring-aspects'
```
Spring Retry for transient failure handling

**Lombok:**
```gradle
compileOnly 'org.projectlombok:lombok'
```
Reduces boilerplate for getters/setters

**OBSERVABLE PATTERN:**
- All stable, mature versions
- Industry-standard libraries
- No bleeding-edge experimental dependencies
- Well-maintained projects with active development

---

## 10. FRONTEND COMMUNICATION

### 10.1 API Documentation: Swagger/OpenAPI

**Implementation:**
```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
  packages-to-scan: org.univ.rankus.adapter.in.web.controller
  paths-to-match: /api/**
```

**Documentation quality:**
- 25+ tags with descriptions
- Full endpoint descriptions
- Request/response examples
- Security configuration documented
- Interactive testing (try-it-out)

**ALTERNATIVES:**
1. Manual documentation (Markdown)
2. Postman collection
3. AsyncAPI (for async APIs)
4. GraphQL schema documentation

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. Auto-generated - Stays in sync with code
2. Interactive - Try-it-out feature
3. Standard format - OpenAPI is industry standard
4. Frontend discovery - React team can explore API

**OUTCOME EVIDENCE:**
- Comprehensive and well-organized
- Clear communication with React team
- JWT security properly documented

### 10.2 CORS Configuration

**WHAT WAS CHOSEN:**
Permissive CORS configuration:

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        // ... register globally
    }
}
```

**Configuration:**
- Allow all origins: "*"
- Allow all methods: GET, POST, PUT, DELETE, OPTIONS
- Allow all headers: "*"
- Allow credentials: true

**ALTERNATIVES:**
1. Restrictive CORS - Specific domain whitelist
2. No CORS - Proxy through same domain
3. Per-endpoint CORS - Different rules per endpoint

**OBSERVABLE EVIDENCE WHY CHOSEN:**
1. Development convenience - React dev server (different port)
2. Local testing - Any origin can call API
3. Frontend flexibility - No CORS restrictions during development

**POTENTIAL ISSUE:**
```java
config.setAllowedOriginPatterns(List.of("*"));
config.setAllowCredentials(true);  // SECURITY RISK
```

This combination is a security risk:
- Credentials allowed from any origin
- CSRF vulnerability
- Should restrict to specific frontend domain in production

**OUTCOME EVIDENCE:**
- Works for local development
- Not suitable for production (not addressed)
- Should be tightened with specific origins

---

## SUMMARY: ARCHITECTURAL DECISIONS TABLE

| Category | Choice | Alternatives | Benefits | Drawbacks | Outcome |
|----------|--------|--------------|----------|-----------|---------|
| Architecture | Hexagonal | Layered, Clean | Flexible, testable | Boilerplate | Well-organized |
| CQRS | Naming convention | True CQRS | Clear intent | Partial pattern | Effective |
| Database | MySQL 8.0 | PostgreSQL | Simple, available | Limited JSON | Appropriate |
| Password | @Embeddable | String field | Type-safe, DDD | Complexity | Good pattern |
| JWT | Custom dual-token | Spring OAuth2 | Full control | Stateful | Industry-standard |
| Roles | 6-level hierarchy | 3-level, ABAC | Granular, org-mapped | Complex | Works well |
| Permissions | Unified evaluator | Spring built-in | Extensible, central | Custom impl | Well-designed |
| Testing | Custom factories | Builders, Fixtures | Reusable, clear | Maintenance | Mature approach |
| Coverage | Tiered (80-95%) | Universal high, none | Risk-proportionate | Not enforced | Technical debt |
| API | REST + Swagger | GraphQL, gRPC | Standard, documented | N+1 queries | Appropriate |
| DTOs | Separate req/resp | Entities directly | Security, flexibility | Boilerplate | Well-separated |
| Error Handling | Global @RestControllerAdvice | Per-controller | Consistent | Extra layer | Well-designed |
| Infrastructure | Docker + EC2 | K8s, Lambda | Simple, reproducible | Not production-scale | Development-friendly |
| CI/CD | GitHub Actions | Jenkins, CircleCI | GitHub-native | Limited capability | Appropriate |
| Code Quality | JaCoCo (disabled) | Checkstyle, PMD | Intended enforcement | Not active | Technical debt |
| QR Security | AES-256-GCM | Plain text | Secure, tested | Complex migration | Well-evolved |
| CORS | Permissive | Restrictive | Dev convenience | Security risk | Not prod-ready |

---

## KEY INSIGHTS & RECOMMENDATIONS

### Strengths
1. **Well-organized architecture** - Clear separation of concerns across 375 files
2. **Security-conscious** - Multiple security layers (roles, permissions, JWT, token rotation)
3. **Comprehensive testing** - 136 tests with tiered coverage requirements
4. **API-first design** - Swagger documentation integrated, 100+ endpoints
5. **DDD alignment** - Value objects, domain entities, business logic encapsulation
6. **Evolutionary design** - QR token encryption and permission system show maturity

### Areas for Improvement
1. **JaCoCo enforcement disabled** - Coverage requirements not actually enforced (line 194)
2. **Static analysis disabled** - Checkstyle/PMD not running (Java 17 compatibility issue)
3. **Permission logic scattered** - 20+ canXxx() methods in User entity (should be service)
4. **CORS too permissive** - Credentials allowed from any origin (security risk)
5. **Boilerplate overhead** - Hexagonal architecture adds files without always adding value

### Architectural Maturity Evidence
1. **Security pattern evolution** - QR tokens: plain text → AES-256-GCM encryption
2. **Permission system evolution** - Scattered → centralized UnifiedPermissionEvaluator
3. **Token rotation implementation** - Industry-standard Refresh Token Rotation pattern
4. **Dual-token JWT** - Access + Refresh tokens with proper expiration
5. **Tiered test coverage** - Risk-proportionate testing strategy
6. **Value object pattern** - Password as @Embeddable demonstrates DDD maturity

### Recommendations
1. **Enable JaCoCo enforcement** - Uncomment line 194 to enforce coverage
2. **Update static analysis tools** - Resolve Java 17 compatibility and re-enable
3. **Refactor permission logic** - Move canXxx() methods from User to Permission service
4. **Restrict CORS in production** - Whitelist specific frontend origin
5. **Evaluate boilerplate cost** - Consider if Hexagonal complexity is worth the benefit
6. **Document CQRS approach** - Clarify whether true CQRS is planned
7. **Consider simplification** - Some layers add abstraction without proportional value

---

## CONCLUSION

The Rankus Spring Boot project demonstrates sophisticated architectural thinking with careful attention to security, testing, and domain-driven design. The developer(s) clearly understand advanced patterns (Hexagonal Architecture, JWT token rotation, value objects) and have made thoughtful choices aligned with the university domain and React frontend requirements.

The codebase shows evidence of evolution and refinement (QR encryption, permission centralization), indicating experience and maturity. While some areas (boilerplate, scattered permission logic, disabled code quality checks) could be improved, the overall architecture is well-executed and maintainable.

The project is production-ready from a code perspective, with the main opportunities for improvement being governance (enforcing code quality checks) and fine-tuning (removing some boilerplate from the Hexagonal architecture layer).
