# Response DTO 코딩 컨벤션

> HTTP 응답 데이터를 제공하는 Response DTO 클래스의 네이밍, 구조, 변환 패턴

## 📛 네이밍 컨벤션

### 클래스 네이밍
- **기본 패턴**: `{Domain}ResponseDto`
- **단일 응답**: `UserResponseDto`, `LabResponseDto`
- **인증 응답**: `AuthResponseDto` (토큰 + 사용자 정보)
- **페이징 응답**: `PageResponse<T>` (제네릭 활용)
- **공통 응답**: `ApiResponse<T>` (모든 응답의 래퍼)

### 필드 네이밍
- **camelCase 사용**: `firstName`, `lastName`, `createdAt`
- **boolean 필드**: `isActive`, `hasPermission`
- **시간 필드**: `createdAt`, `updatedAt` (ISO 8601 형식)
- **관계 필드**: `labId`, `labName` (필요한 정보만 포함)

## 🏗️ 클래스 구조 패턴

### 기본 Response DTO 구조
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {Domain}ResponseDto {
    
    // 1. 기본 식별자
    private Long id;
    
    // 2. 주요 비즈니스 데이터
    private String name;
    private String email;
    private Role role;
    
    // 3. 관계 데이터 (필요한 정보만)
    private Long labId;
    private String labName;
    
    // 4. 메타데이터
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 5. 도메인 객체에서 DTO 생성하는 팩토리 메서드
    public static {Domain}ResponseDto from({Domain} {domain}) {
        {Domain}ResponseDtoBuilder builder = {Domain}ResponseDto.builder()
            .id({domain}.getId())
            .name({domain}.getName())
            .email({domain}.getEmail())
            .role({domain}.getRole())
            .createdAt({domain}.getCreatedAt())
            .updatedAt({domain}.getUpdatedAt());
            
        // 관계 정보 포함 (null 체크)
        if ({domain}.getLab() != null) {
            builder.labId({domain}.getLab().getId())
                   .labName({domain}.getLab().getName());
        }
        
        return builder.build();
    }
    
    // 6. 리스트 변환을 위한 정적 메서드
    public static List<{Domain}ResponseDto> fromList(List<{Domain}> {domain}List) {
        return {domain}List.stream()
            .map({Domain}ResponseDto::from)
            .collect(Collectors.toList());
    }
}
```

### 간단한 Response DTO 구조
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabImageResponseDto {
    
    private Long id;
    private String imageUrl;
    private ImageType type;
    private LocalDateTime createdAt;
    
    public static LabImageResponseDto from(LabImage labImage) {
        return LabImageResponseDto.builder()
            .id(labImage.getId())
            .imageUrl(labImage.getImageUrl())
            .type(labImage.getType())
            .createdAt(labImage.getCreatedAt())
            .build();
    }
    
    public static List<LabImageResponseDto> fromList(List<LabImage> images) {
        return images.stream()
            .map(LabImageResponseDto::from)
            .collect(Collectors.toList());
    }
}
```

## 🎁 공통 응답 래퍼 패턴

### ApiResponse 표준화된 구조
```java
@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 공통 응답 래퍼")
public class ApiResponse<T> {
    
    @Schema(description = "HTTP 상태 코드", example = "200")
    private int status;
    
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;
    
    @Schema(description = "응답 데이터", nullable = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    
    @Schema(description = "에러 상세 정보(검증 실패 등)", nullable = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object errors;
    
    @Schema(description = "응답 발생 시각", example = "2025-06-17T07:30:15.123Z")
    @Builder.Default
    private Instant timestamp = Instant.now();

    // === 정적 팩토리 메서드들 ===
    
    /**
     * 성공 응답 (200 OK, 데이터 있음)
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }
    
    /**
     * 성공 응답 (200 OK, 커스텀 메시지)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 생성 성공 응답 (201 Created)
     */
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message("리소스가 성공적으로 생성되었습니다.")
                .data(data)
                .build();
    }
    
    /**
     * 생성 성공 응답 (201 Created, 커스텀 메시지)
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .status(201)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 삭제 성공 응답 (200 OK, 데이터 없음)
     */
    public static <T> ApiResponse<T> deleted() {
        return ApiResponse.<T>builder()
                .status(200)
                .message("리소스가 성공적으로 삭제되었습니다.")
                .build();
    }
    
    /**
     * 에러 응답 (커스텀 상태코드)
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .build();
    }
    
    /**
     * 에러 응답 (커스텀 상태코드, 에러 상세정보 포함)
     */
    public static <T> ApiResponse<T> error(int status, String message, Object errors) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .errors(errors)
                .build();
    }
}
```

### PageResponse 구조
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
    
    // DTO 변환과 함께 페이징 응답 생성
    public static <T, R> PageResponse<R> of(Page<T> page, Function<T, R> converter) {
        List<R> convertedContent = page.getContent().stream()
            .map(converter)
            .collect(Collectors.toList());
            
        return PageResponse.<R>builder()
            .content(convertedContent)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
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