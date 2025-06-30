package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
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
    private final String role;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Long labId;
    private final String labName;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .labId(null) // TODO: Lab 관계 구현 후 추가
                .labName(null) // TODO: Lab 관계 구현 후 추가
                .build();
    }

    public static List<UserResponseDto> fromList(List<User> users) {
        return users.stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }
}