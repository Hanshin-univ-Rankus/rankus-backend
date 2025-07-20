package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.user.EnrollmentStatus;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "사용자 응답 정보")
public class UserResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private final Long id;

    @Schema(description = "사용자 이름", example = "홍길동")
    private final String name;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    private final String email;

    @Schema(description = "학번", example = "20240001")
    private final String studentNumber;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private final String phoneNumber;

    @Schema(description = "학년", example = "3")
    private final Integer grade;

    @Schema(description = "재학 상태")
    private final EnrollmentStatus enrollmentStatus;

    @Schema(description = "소속 연구실 ID", example = "1", nullable = true)
    private final Long labId;

    @Schema(description = "생성일시")
    private final LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private final LocalDateTime updatedAt;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .studentNumber(user.getStudentNumber())
                .phoneNumber(user.getPhoneNumber())
                .grade(user.getGrade())
                .enrollmentStatus(user.getEnrollmentStatus())
                .labId(user.getLab() != null ? user.getLab().getId() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static List<UserResponseDto> fromList(List<User> users) {
        return users.stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }
}