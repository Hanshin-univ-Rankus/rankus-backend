package org.univ.rankus.testutil.factory.dto;

import org.univ.rankus.adapter.in.web.dto.request.*;
import org.univ.rankus.adapter.in.web.dto.response.*;
import org.univ.rankus.domain.model.lab.application.ApplicationStatus;
import org.univ.rankus.domain.model.lab.core.ImageType;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.lab.creation.LabCreationStatus;
import org.univ.rankus.domain.model.lab.notice.NoticeType;
import org.univ.rankus.domain.model.user.EnrollmentStatus;
import org.univ.rankus.domain.model.user.Role;

import java.time.LocalDateTime;

/**
 * DtoFactory - DTO 객체 생성을 위한 팩토리
 * Controller 테스트에서 Request/Response DTO 생성에 활용
 */
public final class DtoFactory {
    private DtoFactory() {
    }

    // User Request DTOs
    public static UserRegisterRequestDto buildUserRegisterRequest() {
        return UserRegisterRequestDto.builder()
                .name("테스트사용자")
                .email("test@hs.ac.kr")
                .password("Password!123")
                .studentNumber("20201001")
                .phoneNumber("010-1234-5678")
                .grade(3)
                .enrollmentStatus(EnrollmentStatus.ENROLLED)
                .build();
    }

    public static UserRegisterRequestDto buildUserRegisterRequest(String name, String email, String password) {
        return UserRegisterRequestDto.builder()
                .name(name)
                .email(email)
                .password(password)
                .studentNumber("20201001")
                .phoneNumber("010-1234-5678")
                .grade(3)
                .enrollmentStatus(EnrollmentStatus.ENROLLED)
                .build();
    }

    public static UserLoginRequestDto buildUserLoginRequest() {
        return UserLoginRequestDto.builder()
                .email("test@hs.ac.kr")
                .password("Password!123")
                .build();
    }

    public static UserLoginRequestDto buildUserLoginRequest(String email, String password) {
        return UserLoginRequestDto.builder()
                .email(email)
                .password(password)
                .build();
    }

    // Lab Request DTOs - 존재하지 않는 DTO들 제거
    // LabCreateRequestDto, LabUpdateRequestDto는 실제로 존재하지 않음

    // Lab Application Request DTOs - Record 타입이므로 생성자 방식 사용
    public static LabApplicationRequestDto buildLabApplicationRequest() {
        return new LabApplicationRequestDto(
                LocalDateTime.now().plusDays(1)
        );
    }

    public static LabApplicationRequestDto buildLabApplicationRequest(LocalDateTime interviewTime) {
        return new LabApplicationRequestDto(interviewTime);
    }

    // Lab Image Request DTOs - 실제 DTO는 getter만 있는 클래스이므로 리플렉션 사용 필요
    // 하지만 테스트에서 사용하지 않으므로 주석 처리
    // public static LabImageRequestDto buildLabImageRequest() {
    //     // LabImageRequestDto는 @Getter만 있어서 builder() 메서드가 없음
    //     // 실제 테스트에서 필요시 별도 구현 필요
    // }

    // Response DTOs - 실제 DTO 구조에 맞게 수정
    public static UserResponseDto buildUserResponseDto() {
        return UserResponseDto.builder()
                .id(1L)
                .name("테스트사용자")
                .email("test@hs.ac.kr")
                .studentNumber("20201001")
                .phoneNumber("010-1234-5678")
                .grade(3)
                .enrollmentStatus(EnrollmentStatus.ENROLLED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserResponseDto buildUserResponseDto(Long id, String name, String email) {
        return UserResponseDto.builder()
                .id(id)
                .name(name)
                .email(email)
                .studentNumber("20201001")
                .phoneNumber("010-1234-5678")
                .grade(3)
                .enrollmentStatus(EnrollmentStatus.ENROLLED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Role을 받는 오버로드 메서드는 실제 UserResponseDto에 role 필드가 없으므로 제거
    public static UserResponseDto buildUserResponseDto(Long id, String name, String email, Role role) {
        // role 필드는 실제 UserResponseDto에 없으므로 무시하고 기본 메서드 호출
        return buildUserResponseDto(id, name, email);
    }

    public static LabResponseDto buildLabResponseDto() {
        return LabResponseDto.builder()
                .id(1L)
                .name("AI 연구실")
                .description("인공지능 연구실")
                .professorName("김교수")
                .ranking(0)
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    public static LabResponseDto buildLabResponseDto(Long id, String name, String description, String professorName) {
        return LabResponseDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .professorName(professorName)
                .ranking(0)
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    public static LabApplicationResponseDto buildLabApplicationResponseDto() {
        UserResponseDto applicant = buildUserResponseDto();
        return LabApplicationResponseDto.builder()
                .id(1L)
                .labId(1L)
                .applicant(applicant)
                .status(ApplicationStatus.PENDING.name())
                .interviewTime(LocalDateTime.now().plusDays(1))
                .build();
    }

    public static LabApplicationResponseDto buildLabApplicationResponseDto(Long id, Long labId, ApplicationStatus status) {
        UserResponseDto applicant = buildUserResponseDto();
        return LabApplicationResponseDto.builder()
                .id(id)
                .labId(labId)
                .applicant(applicant)
                .status(status.name())
                .interviewTime(LocalDateTime.now().plusDays(1))
                .build();
    }

    public static AuthResponseDto buildAuthResponseDto() {
        return AuthResponseDto.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .user(buildUserResponseDto())
                .build();
    }

    public static AuthResponseDto buildAuthResponseDto(String accessToken, String refreshToken, UserResponseDto user) {
        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .build();
    }

    // Lab Notice Request DTOs
    public static LabNoticeCreateRequestDto buildLabNoticeCreateRequest() {
        return LabNoticeCreateRequestDto.builder()
                .title("테스트 공지사항")
                .content("테스트 공지사항 내용입니다.")
                .type(NoticeType.NORMAL)
                .pinned(false)
                .build();
    }

    public static LabNoticeCreateRequestDto buildLabNoticeCreateRequest(String title, String content, NoticeType type, boolean pinned) {
        return LabNoticeCreateRequestDto.builder()
                .title(title)
                .content(content)
                .type(type)
                .pinned(pinned)
                .build();
    }

    public static LabNoticeUpdateRequestDto buildLabNoticeUpdateRequest() {
        return LabNoticeUpdateRequestDto.builder()
                .title("수정된 공지사항")
                .content("수정된 공지사항 내용입니다.")
                .type(NoticeType.URGENT)
                .pinned(true)
                .build();
    }

    public static LabNoticeUpdateRequestDto buildLabNoticeUpdateRequest(String title, String content, NoticeType type, boolean pinned) {
        return LabNoticeUpdateRequestDto.builder()
                .title(title)
                .content(content)
                .type(type)
                .pinned(pinned)
                .build();
    }

    // Lab Creation Request DTOs - Record 타입이므로 생성자 방식 사용
    public static LabCreationRequestDto buildLabCreationRequest() {
        return new LabCreationRequestDto(
                "AI 연구실",
                LabCategory.AI,
                "인공지능 관련 연구를 수행하는 연구실입니다."
        );
    }

    public static LabCreationRequestDto buildLabCreationRequest(String labName, LabCategory category, String description) {
        return new LabCreationRequestDto(labName, category, description);
    }

    public static LabCreationRequestRejectDto buildLabCreationRequestRejectDto() {
        return new LabCreationRequestRejectDto(
                "요청하신 연구실의 연구 분야가 명확하지 않습니다."
        );
    }

    public static LabCreationRequestRejectDto buildLabCreationRequestRejectDto(String rejectionReason) {
        return new LabCreationRequestRejectDto(rejectionReason);
    }

    // Lab Notice Response DTOs
    public static LabNoticeResponseDto buildLabNoticeResponseDto() {
        return LabNoticeResponseDto.builder()
                .id(1L)
                .title("테스트 공지사항")
                .content("테스트 공지사항 내용")
                .type(NoticeType.NORMAL)
                .pinned(false)
                .authorId(1L)
                .authorName("작성자")
                .labId(1L)
                .labName("AI 연구실")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static LabNoticeResponseDto buildLabNoticeResponseDto(Long id, String title, NoticeType type, boolean pinned) {
        return LabNoticeResponseDto.builder()
                .id(id)
                .title(title)
                .content("테스트 공지사항 내용")
                .type(type)
                .pinned(pinned)
                .authorId(1L)
                .authorName("작성자")
                .labId(1L)
                .labName("AI 연구실")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Lab Creation Request Response DTOs
    public static LabCreationRequestResponseDto buildLabCreationRequestResponseDto() {
        return LabCreationRequestResponseDto.builder()
                .id(1L)
                .requestedLabName("AI 연구실")
                .requestedCategory(LabCategory.AI)
                .requestedDescription("인공지능 관련 연구를 수행하는 연구실입니다.")
                .status(LabCreationStatus.PENDING)
                .requester(buildUserResponseDto())
                .requestedAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .build();
    }

    public static LabCreationRequestResponseDto buildLabCreationRequestResponseDto(Long id, String labName, String status) {
        return LabCreationRequestResponseDto.builder()
                .id(id)
                .requestedLabName(labName)
                .requestedCategory(LabCategory.AI)
                .requestedDescription("인공지능 관련 연구를 수행하는 연구실입니다.")
                .status(LabCreationStatus.PENDING)  // 기본값으로 설정
                .requester(buildUserResponseDto())
                .requestedAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .build();
    }

    // Lab Image Request DTOs
    public static LabImageRequestDto buildLabImageRequest() {
        // LabImageRequestDto는 @Builder가 없으므로 생성자 사용 불가
        // 실제 사용 시에는 리플렉션 또는 다른 방법 필요
        throw new UnsupportedOperationException("LabImageRequestDto는 builder 패턴을 지원하지 않습니다");
    }

    public static LabImageRequestDto buildLabImageRequest(String imageUrl, String imageType, String description) {
        // LabImageRequestDto는 @Builder가 없으므로 생성자 사용 불가
        throw new UnsupportedOperationException("LabImageRequestDto는 builder 패턴을 지원하지 않습니다");
    }

    // Calendar Event Request DTOs
    public static CalendarEventCreateRequestDto buildCalendarEventCreateRequest() {
        return CalendarEventCreateRequestDto.builder()
                .title("테스트 일정")
                .description("테스트 일정 설명")
                .eventDate(java.time.LocalDate.now().plusDays(1))
                .build();
    }

    public static CalendarEventCreateRequestDto buildCalendarEventCreateRequest(String title, String description, java.time.LocalDate eventDate) {
        return CalendarEventCreateRequestDto.builder()
                .title(title)
                .description(description)
                .eventDate(eventDate)
                .build();
    }

    public static CalendarEventUpdateRequestDto buildCalendarEventUpdateRequest() {
        return CalendarEventUpdateRequestDto.builder()
                .title("수정된 일정")
                .description("수정된 일정 설명")
                .eventDate(java.time.LocalDate.now().plusDays(2))
                .build();
    }

    public static CalendarEventUpdateRequestDto buildCalendarEventUpdateRequest(String title, String description, java.time.LocalDate eventDate) {
        return CalendarEventUpdateRequestDto.builder()
                .title(title)
                .description(description)
                .eventDate(eventDate)
                .build();
    }

    // Calendar Event Response DTOs
    public static CalendarEventResponseDto buildCalendarEventResponseDto() {
        return CalendarEventResponseDto.builder()
                .id(1L)
                .labId(1L)
                .labName("AI 연구실")
                .type("SCHEDULE")
                .title("테스트 일정")
                .description("테스트 일정 설명")
                .eventDate(java.time.LocalDate.now().plusDays(1))
                .startTime(null)
                .endTime(null)
                .interviewId(null)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }

    public static CalendarEventResponseDto buildCalendarEventResponseDto(Long id, String title, String type) {
        return CalendarEventResponseDto.builder()
                .id(id)
                .labId(1L)
                .labName("AI 연구실")
                .type(type)
                .title(title)
                .description("테스트 일정 설명")
                .eventDate(java.time.LocalDate.now().plusDays(1))
                .startTime("INTERVIEW".equals(type) ? java.time.LocalTime.of(9, 0) : null)
                .endTime("INTERVIEW".equals(type) ? java.time.LocalTime.of(10, 0) : null)
                .interviewId("INTERVIEW".equals(type) ? 100L : null)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }

    // Lab Image Response DTOs
    public static LabImageResponseDto buildLabImageResponseDto() {
        return LabImageResponseDto.builder()
                .id(1L)
                .imageUrl("https://example.com/test-image.jpg")
                .type(ImageType.REPRESENTATIVE)
                .labId(1L)
                .build();
    }

    public static LabImageResponseDto buildLabImageResponseDto(Long id, String imageUrl, String imageType) {
        return LabImageResponseDto.builder()
                .id(id)
                .imageUrl(imageUrl)
                .type(ImageType.valueOf(imageType))
                .labId(1L)
                .build();
    }
}