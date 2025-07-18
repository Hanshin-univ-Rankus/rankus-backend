# 캘린더 시스템 구현 가이드

> 캘린더 시스템의 아키텍처, API, 연동 방식 정리

## 🎯 개요

캘린더 시스템은 **일반 일정**과 **면접 일정**을 분리하여 관리하는 시스템입니다.

### 핵심 특징
- **일반 일정**: 사용자가 직접 CRUD 관리 (날짜만 포함)
- **면접 일정**: Interview 시스템과 자동 연동 (날짜+시간 포함)
- **권한 기반**: 랩실 기반 권한 체계 완전 통합
- **분리된 API**: 별도의 컨트롤러로 명확한 책임 분리

## 🏗️ 아키텍처 구조

### 도메인 모델
```java
@Entity
@Table(name = "calendar_events")
public class CalendarEvent extends BaseTimeEntity {
    private EventType type;        // SCHEDULE, INTERVIEW
    private String title;          // 일정 제목
    private String description;    // 일정 설명 (optional)
    private LocalDate eventDate;   // 일정 날짜
    private LocalTime startTime;   // 시작 시간 (INTERVIEW만)
    private LocalTime endTime;     // 종료 시간 (INTERVIEW만)
    private Long interviewId;      // 연결된 면접 ID (INTERVIEW만)
    private Lab lab;               // 소속 랩실
}
```

### 서비스 계층
```java
CalendarEventQueryService    // 조회 서비스
CalendarEventCommandService  // 명령 서비스
```

### API 계층
```java
CalendarScheduleController   // 일반 일정 API
CalendarInterviewController  // 면접 일정 API (조회만)
```

## 🔗 Interview 시스템 연동

### 자동 연동 시점
1. **면접 생성**: `InterviewCommandService.createInterview()`
2. **면접 수정**: `InterviewCommandService.updateInterview()`
3. **면접 삭제**: `InterviewCommandService.deleteInterview()`

### 연동 구현 방식
```java
@Service
public class InterviewCommandService {
    private final CalendarEventCommandUseCase calendarEventCommandUseCase;
    
    @Override
    public Interview createInterview(...) {
        Interview savedInterview = interviewRepositoryPort.save(interview);
        
        // 캘린더 이벤트 자동 생성
        calendarEventCommandUseCase.createInterviewEvent(
            labId, "면접 기간",
            "면접 기간: " + startDate + " ~ " + endDate,
            startDate, LocalTime.of(9, 0), LocalTime.of(18, 0),
            savedInterview.getId()
        );
        
        return savedInterview;
    }
}
```

## 🔐 권한 체계

### CalendarPermissionHandler
```java
@Component
public class CalendarPermissionHandler {
    // MANAGE_CALENDAR: 랩장/매니저, 교수, 관리자
    // VIEW_CALENDAR: 모든 인증된 사용자
    // VIEW_APPLICANTS: 랩장/매니저, 교수, 관리자
    
    public boolean hasPermissionForLab(CustomUserDetails userDetails, 
                                      Long labId, String permission) {
        return switch (permission) {
            case "MANAGE_CALENDAR" -> user.canManageLabNotices(lab);
            case "VIEW_CALENDAR" -> true;
            case "VIEW_APPLICANTS" -> user.canManageLabNotices(lab);
            default -> false;
        };
    }
}
```

### 권한 적용 예시
```java
@PreAuthorize("@calendarPermissionHandler.hasPermissionForLab(authentication.principal, #labId, 'MANAGE_CALENDAR')")
public ResponseEntity<...> createSchedule(@PathVariable Long labId, ...) {
    // 일정 생성 로직
}
```

## 📊 API 설계

### 일반 일정 API
```
GET    /api/labs/{labId}/calendar/schedules?startDate=2025-01-01&endDate=2025-01-31
POST   /api/labs/{labId}/calendar/schedules
PUT    /api/labs/{labId}/calendar/schedules/{eventId}
DELETE /api/labs/{labId}/calendar/schedules/{eventId}
```

### 면접 일정 API (읽기 전용)
```
GET    /api/labs/{labId}/calendar/interviews?startDate=2025-01-01&endDate=2025-01-31
GET    /api/labs/{labId}/calendar/interviews/{eventId}
GET    /api/labs/{labId}/calendar/interviews/by-interview/{interviewId}
```

## 🎨 DTO 구조

### Request DTO
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CalendarEventCreateRequestDto {
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title;
    
    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    private String description;
    
    @NotNull(message = "날짜는 필수입니다")
    @Future(message = "날짜는 미래여야 합니다")
    private LocalDate eventDate;
}
```

### Response DTO
```java
@Getter @Builder
public class CalendarEventResponseDto {
    private Long id;
    private String type;           // "SCHEDULE" or "INTERVIEW"
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalTime startTime;   // INTERVIEW만
    private LocalTime endTime;     // INTERVIEW만
    private Long interviewId;      // INTERVIEW만
    private Long labId;
    private String labName;
    
    public static CalendarEventResponseDto from(CalendarEvent event) {
        return CalendarEventResponseDto.builder()
            .id(event.getId())
            .type(event.getType().name())
            .title(event.getTitle())
            // ... 매핑
            .build();
    }
}
```

## 🔄 사용 시나리오

### 1. 일반 일정 생성
```java
// 사용자가 직접 생성
POST /api/labs/1/calendar/schedules
{
    "title": "연구실 정기 미팅",
    "description": "매주 화요일 정기 미팅",
    "eventDate": "2025-01-15"
}
```

### 2. 면접 일정 자동 생성
```java
// Interview 시스템에서 자동 생성
POST /api/labs/1/interviews
{
    "startDate": "2025-01-20",
    "endDate": "2025-01-25",
    "durationMinutes": 30,
    "maxApplicantsPerSlot": 3
}
// → 캘린더 이벤트 자동 생성됨
```

### 3. 캘린더 조회
```java
// 월간 일정 조회 (일반 + 면접)
GET /api/labs/1/calendar/schedules?startDate=2025-01-01&endDate=2025-01-31
GET /api/labs/1/calendar/interviews?startDate=2025-01-01&endDate=2025-01-31
```

## 🧪 테스트 가이드

### 도메인 테스트
```java
@Test
void 일반_일정_생성_성공() {
    // given
    Lab lab = LabFactory.createValidLab();
    
    // when
    CalendarEvent event = new CalendarEvent(
        lab, "테스트 일정", "설명", LocalDate.now().plusDays(1)
    );
    
    // then
    assertThat(event.getType()).isEqualTo(EventType.SCHEDULE);
    assertThat(event.hasTime()).isFalse();
}
```

### 서비스 테스트
```java
@Test
void 면접_일정_자동_생성_성공() {
    // given
    Lab lab = LabFactory.createValidLab();
    Interview interview = InterviewFactory.createValidInterview(lab);
    
    // when
    CalendarEvent event = commandUseCase.createInterviewEvent(
        lab.getId(), "면접", "설명", 
        LocalDate.now().plusDays(1),
        LocalTime.of(10, 0), LocalTime.of(12, 0),
        interview.getId()
    );
    
    // then
    assertThat(event.getType()).isEqualTo(EventType.INTERVIEW);
    assertThat(event.hasTime()).isTrue();
}
```

## 📋 주요 특징 요약

1. **분리된 책임**: 일반 일정과 면접 일정의 명확한 분리
2. **자동 연동**: Interview 시스템과 완벽한 동기화
3. **권한 통합**: 기존 랩실 권한 체계 완전 활용
4. **RESTful API**: 명확하고 일관된 API 설계
5. **헥사고날 아키텍처**: 기존 프로젝트 패턴 완벽 준수

이 가이드를 참고하여 캘린더 시스템을 확장하거나 유지보수할 수 있습니다.