package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.user.EnrollmentStatus;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class UserResponseDto {
    private final Long id;
    private final String name;
    private final String email;
    private final String studentNumber;
    private final String phoneNumber;
    private final Integer grade;
    private final EnrollmentStatus enrollmentStatus;
    private final LocalDateTime createdAt;
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