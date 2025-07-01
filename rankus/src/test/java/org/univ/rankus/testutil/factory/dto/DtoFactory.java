package org.univ.rankus.testutil.factory.dto;

import org.univ.rankus.adapter.in.web.dto.request.LabApplicationRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.LabApplicationResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.LabResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.domain.model.lab.ApplicationStatus;
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
                .email("test@example.com")
                .password("Password!123")
                .build();
    }

    public static UserRegisterRequestDto buildUserRegisterRequest(String name, String email, String password) {
        return UserRegisterRequestDto.builder()
                .name(name)
                .email(email)
                .password(password)
                .build();
    }

    public static UserLoginRequestDto buildUserLoginRequest() {
        return UserLoginRequestDto.builder()
                .email("test@example.com")
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
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserResponseDto buildUserResponseDto(Long id, String name, String email) {
        return UserResponseDto.builder()
                .id(id)
                .name(name)
                .email(email)
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
                .token("mock-jwt-token")
                .user(buildUserResponseDto())
                .build();
    }

    public static AuthResponseDto buildAuthResponseDto(String token, UserResponseDto user) {
        return AuthResponseDto.builder()
                .token(token)
                .user(user)
                .build();
    }
}