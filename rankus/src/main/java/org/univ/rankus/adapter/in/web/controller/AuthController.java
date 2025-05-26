package org.univ.rankus.adapter.in.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.application.port.in.UserUseCase;
import org.univ.rankus.domain.model.user.User;

@RestController
@Validated
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final UserUseCase userUseCase;

    public AuthController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping(path = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> signUp(
            @Valid @RequestBody SignUpRequestDto request
    ) {
        User user = userUseCase.signUp(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getLabId()
        );
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        String token = userUseCase.login(
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    // --- DTOs ---

    @Getter
    @NoArgsConstructor
    public static class SignUpRequestDto {
        @NotBlank(message = "name은 필수입니다.")
        private String name;

        @NotBlank(message = "email은 필수입니다.")
        @Email(message = "유효한 이메일을 입력하세요.")
        private String email;

        @NotBlank(message = "password는 필수입니다.")
        private String password;

        @NotNull(message = "labId는 필수입니다.")
        private Long labId;
    }

    @Getter
    @AllArgsConstructor
    public static class UserResponseDto {
        private Long id;
        private String name;
        private String email;
        private Long labId;

        public static UserResponseDto from(User user) {
            Long labId = user.getLab() != null ? user.getLab().getId() : null;
            return new UserResponseDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    labId
            );
        }
    }

    @Getter
    @NoArgsConstructor
    public static class LoginRequestDto {
        @NotBlank(message = "email은 필수입니다.")
        @Email(message = "유효한 이메일을 입력하세요.")
        private String email;

        @NotBlank(message = "password는 필수입니다.")
        private String password;
    }

    @Getter
    @AllArgsConstructor
    public static class LoginResponseDto {
        private String token;
    }
}
