package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class UserResponseDto {
    private final Long   id;
    private final String name;
    private final String email;
    private final String role;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }

    public static List<UserResponseDto> fromList(List<User> users) {
        return users.stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }
}