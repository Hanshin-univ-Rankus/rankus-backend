# Response DTO 컨벤션

> 📋 **네이밍 규칙**: @core/conventions.md#dto  
> 🏗️ **기본 패턴**: @core/patterns.md#response-템플릿

## 🎯 핵심 규칙

### 네이밍 매트릭스
| 타입 | 패턴 | 예시 |
|------|------|------|
| 기본 | `{Domain}ResponseDto` | `UserResponseDto`, `LabResponseDto` |
| 인증 | `AuthResponseDto` | 토큰 + 사용자 정보 |
| 페이징 | `PageResponse<T>` | 제네릭 활용 |
| 공통 | `ApiResponse<T>` | 모든 응답 래퍼 |

### 필드 패턴
| 타입 | 네이밍 | 예시 |
|------|--------|------|
| 기본 | camelCase | `firstName`, `lastName` |
| 시간 | `{verb}At` | `createdAt`, `updatedAt` |
| 관계 | `{domain}Id`, `{domain}Name` | `labId`, `labName` |
| Boolean | `isActive`, `hasPermission` | 상태 표현 |

## 🏗️ 구조 패턴

### 기본 ResponseDto 템플릿
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class {Domain}ResponseDto {
    // 1. 식별자 + 2. 비즈니스 데이터 + 3. 관계 데이터 + 4. 메타데이터
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    
    // 팩토리 메서드 (필수)
    public static {Domain}ResponseDto from({Domain} domain) {
        // null 체크 포함한 변환 로직
    }
    
    public static List<{Domain}ResponseDto> fromList(List<{Domain}> list) {
        return list.stream().map({Domain}ResponseDto::from).collect(toList());
    }
}
```

## 🎁 공통 래퍼 패턴

### ApiResponse 팩토리 메서드 매트릭스
| 상황 | 메서드 | 상태코드 | 사용 케이스 |
|------|--------|----------|-------------|
| 성공 | `success(data)` | 200 | 조회/수정 성공 |
| 생성 | `created(data)` | 201 | 리소스 생성 |
| 삭제 | `deleted()` | 200 | 삭제 완료 |
| 에러 | `error(status, message)` | 4xx/5xx | 예외 응답 |

### PageResponse 구조
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page, size, totalPages;
    private long totalElements;
    private boolean first, last, hasNext, hasPrevious;
    
    public static <T> PageResponse<T> of(Page<T> page) { /* 구현 */ }
    public static <T,R> PageResponse<R> of(Page<T> page, Function<T,R> converter) { /* 구현 */ }
}
```

## 🔄 복잡한 Response DTO 패턴

### 인증 응답 DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponseDto user;
    
    public static AuthResponseDto of(String accessToken, long expiresIn, User user) {
        return AuthResponseDto.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .user(UserResponseDto.from(user))
            .build();
    }
}
```

### 중첩 관계 포함 Response DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabResponseDto {
    
    private Long id;
    private String name;
    private LabCategory category;
    private String description;
    private Integer ranking;
    private String professorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 관계 데이터
    private List<LabImageResponseDto> images;
    private Long applicationCount;
    private Long memberCount;
    
    public static LabResponseDto from(Lab lab) {
        LabResponseDtoBuilder builder = LabResponseDto.builder()
            .id(lab.getId())
            .name(lab.getName())
            .category(lab.getCategory())
            .description(lab.getDescription())
            .ranking(lab.getRanking())
            .professorName(lab.getProfessorName())
            .createdAt(lab.getCreatedAt())
            .updatedAt(lab.getUpdatedAt());
        
        return builder.build();
    }
    
    // 관계 정보 포함 팩토리 메서드
    public static LabResponseDto fromWithRelations(Lab lab, 
                                                  List<LabImage> images,
                                                  long applicationCount,
                                                  long memberCount) {
        return from(lab).toBuilder()
            .images(LabImageResponseDto.fromList(images))
            .applicationCount(applicationCount)
            .memberCount(memberCount)
            .build();
    }
}
```

### 통계 정보 Response DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabStatisticsResponseDto {
    
    private Long labId;
    private String labName;
    
    // 지원 관련 통계
    private long totalApplications;
    private long pendingApplications;
    private long approvedApplications;
    private long rejectedApplications;
    
    // 멤버 관련 통계
    private long totalMembers;
    private long activeMembers;
    
    // 랭킹 관련 통계
    private int currentRanking;
    private int totalScore;
    private int monthlyScore;
    
    // 시간 정보
    private LocalDateTime lastUpdated;
    
    public static LabStatisticsResponseDto of(Lab lab, LabStatistics statistics) {
        return LabStatisticsResponseDto.builder()
            .labId(lab.getId())
            .labName(lab.getName())
            .totalApplications(statistics.getTotalApplications())
            .pendingApplications(statistics.getPendingApplications())
            .approvedApplications(statistics.getApprovedApplications())
            .rejectedApplications(statistics.getRejectedApplications())
            .totalMembers(statistics.getTotalMembers())
            .activeMembers(statistics.getActiveMembers())
            .currentRanking(lab.getRanking())
            .totalScore(statistics.getTotalScore())
            .monthlyScore(statistics.getMonthlyScore())
            .lastUpdated(LocalDateTime.now())
            .build();
    }
}
```

## 📊 JSON 직렬화 설정

### 날짜/시간 형식 설정
```java
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
private LocalDateTime createdAt;

// 또는 application.yml에서 전역 설정
spring:
  jackson:
    date-format: yyyy-MM-dd'T'HH:mm:ss
    time-zone: Asia/Seoul
```

### 필드 제외/포함 설정
```java
public class UserResponseDto {
    
    private Long id;
    private String name;
    private String email;
    
    // 응답에서 제외
    @JsonIgnore
    private String internalField;
    
    // null일 때 제외
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String optionalField;
    
    // 다른 이름으로 직렬화
    @JsonProperty("user_role")
    private Role role;
}
```

### Enum 직렬화 설정
```java
public enum Role {
    STUDENT("학생"),
    LAB_MEMBER("랩실 멤버");
    
    private final String description;
    
    Role(String description) {
        this.description = description;
    }
    
    @JsonValue  // JSON으로 직렬화할 때 사용할 값
    public String getDescription() {
        return description;
    }
    
    @JsonCreator  // JSON에서 역직렬화할 때 사용
    public static Role fromDescription(String description) {
        for (Role role : Role.values()) {
            if (role.description.equals(description)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + description);
    }
}
```

## 🔍 성능 최적화 패턴

### Lazy Loading 처리
```java
public class LabResponseDto {
    
    // 기본 정보만 포함하는 경량 팩토리 메서드
    public static LabResponseDto fromBasic(Lab lab) {
        return LabResponseDto.builder()
            .id(lab.getId())
            .name(lab.getName())
            .category(lab.getCategory())
            .ranking(lab.getRanking())
            .createdAt(lab.getCreatedAt())
            .build();
    }
    
    // 관계 정보까지 포함하는 완전한 팩토리 메서드
    public static LabResponseDto fromFull(Lab lab) {
        LabResponseDto dto = fromBasic(lab);
        
        // 필요시에만 관계 데이터 로드
        if (lab.getImages() != null && !lab.getImages().isEmpty()) {
            dto.setImages(LabImageResponseDto.fromList(lab.getImages()));
        }
        
        return dto;
    }
}
```

### 투영(Projection) 활용
```java
// 특정 필드만 포함하는 간단한 Response DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabSummaryResponseDto {
    
    private Long id;
    private String name;
    private LabCategory category;
    private Integer ranking;
    
    // 목록 조회용 간단한 변환
    public static LabSummaryResponseDto from(Lab lab) {
        return LabSummaryResponseDto.builder()
            .id(lab.getId())
            .name(lab.getName())
            .category(lab.getCategory())
            .ranking(lab.getRanking())
            .build();
    }
}
```

## 🧪 Response DTO 테스트 패턴

### 변환 로직 테스트
```java
class UserResponseDtoTest {
    
    @Test
    void 도메인_객체에서_DTO로_변환된다() {
        // given
        User user = User.create("홍길동", "hong@example.com", "password123!", Role.STUDENT);
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        
        // when
        UserResponseDto dto = UserResponseDto.from(user);
        
        // then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("홍길동");
        assertThat(dto.getEmail()).isEqualTo("hong@example.com");
        assertThat(dto.getRole()).isEqualTo(Role.STUDENT);
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
    }
    
    @Test
    void 리스트_변환이_올바르게_동작한다() {
        // given
        List<User> users = Arrays.asList(
            User.create("홍길동", "hong@example.com", "password123!", Role.STUDENT),
            User.create("김철수", "kim@example.com", "password123!", Role.LAB_MEMBER)
        );
        
        // when
        List<UserResponseDto> dtos = UserResponseDto.fromList(users);
        
        // then
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getName()).isEqualTo("홍길동");
        assertThat(dtos.get(1).getName()).isEqualTo("김철수");
    }
}
```

### JSON 직렬화 테스트
```java
@JsonTest
class UserResponseDtoJsonTest {
    
    @Autowired
    private JacksonTester<UserResponseDto> json;
    
    @Test
    void JSON_직렬화가_올바르게_동작한다() throws Exception {
        // given
        UserResponseDto dto = UserResponseDto.builder()
            .id(1L)
            .name("홍길동")
            .email("hong@example.com")
            .role(Role.STUDENT)
            .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
            .build();
        
        // when & then
        assertThat(json.write(dto))
            .hasJsonPath("$.id")
            .hasJsonPath("$.name")
            .hasJsonPath("$.email")
            .hasJsonPath("$.role")
            .hasJsonPath("$.createdAt");
            
        assertThat(json.write(dto))
            .extractingJsonPathStringValue("$.name")
            .isEqualTo("홍길동");
    }
}
```

## 📈 최근 개선사항 (2025년 6월)

### ApiResponse 표준화 완료
```java
// ApiResponse.java - 추가된 정적 팩토리 메서드들
public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .status(200)
        .message("요청이 성공적으로 처리되었습니다")
        .data(data)
        .timestamp(Instant.now())
        .build();
}

public static <T> ApiResponse<T> created(T data) {
    return ApiResponse.<T>builder()
        .status(201)
        .message("리소스가 성공적으로 생성되었습니다")
        .data(data)
        .timestamp(Instant.now())
        .build();
}

public static <T> ApiResponse<T> deleted() {
    return ApiResponse.<T>builder()
        .status(200)
        .message("리소스가 성공적으로 삭제되었습니다")
        .timestamp(Instant.now())
        .build();
}
```

### 모든 ResponseDto from() 메서드 표준화
- **UserResponseDto**: 관계 정보(labId, labName) null 체크 포함
- **LabResponseDto**: 교수 정보, 생성일/수정일 포함
- **LabApplicationResponseDto**: 지원서 상태, 면접 시간 포함
- **LabImageResponseDto**: 이미지 URL, 타입, 생성일 포함

### Controller 응답 패턴 일관성 확보
```java
// 표준화된 Controller 응답 패턴
// 생성 응답 (201 Created)
return ResponseEntity.status(HttpStatus.CREATED)
    .body(ApiResponse.created(responseDto));

// 조회 응답 (200 OK)
return ResponseEntity.ok(ApiResponse.success(responseDto));

// 삭제 응답 (200 OK, 데이터 없음)
return ResponseEntity.ok(ApiResponse.deleted());
```

## 🎯 주요 규칙 요약

1. **명확한 구조**: 기본 정보 + 관계 정보 + 메타데이터 순서로 구성
2. **팩토리 메서드**: from() 정적 메서드로 도메인 객체에서 변환
3. **null 안전성**: 관계 객체는 null 체크 후 변환
4. **성능 고려**: 필요한 데이터만 포함, 지연 로딩 고려
5. **일관된 응답**: ApiResponse로 모든 응답 래핑
6. **표준화된 팩토리**: success(), created(), deleted() 메서드 활용
7. **테스트 가능**: 변환 로직과 JSON 직렬화에 대한 테스트 작성