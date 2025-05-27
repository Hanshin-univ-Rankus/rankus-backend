package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.univ.rankus.application.port.in.UserUseCase;
import org.univ.rankus.domain.model.user.User;

import java.util.NoSuchElementException;

@RestController
@Validated
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "인증 API", description = "회원가입 및 로그인 관련 API")
public class AuthController {

    private final UserUseCase userUseCase;

    public AuthController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    /**
     * 회원가입
     */
    @Operation(
        summary = "회원가입",
        description = "사용자 정보를 입력받아 회원가입을 처리합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "중복된 이메일 또는 유효하지 않은 입력값"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 랩실 ID")
    })
    @PostMapping(path = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDto> signUp(
            @Valid @RequestBody SignUpRequestDto request
    ) {
        try {
            User user = userUseCase.signUp(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getLabId()
            );
            return ResponseEntity.ok(UserResponseDto.from(user));

        } catch (IllegalStateException ex) {
            // 중복 이메일 등 BadRequest
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);

        } catch (NoSuchElementException ex) {
            // 존재하지 않는 랩실 ID
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    /**
     * 로그인
     */
    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호를 통해 인증하고 JWT 토큰을 발급합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공 및 토큰 발급"),
        @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 이메일")
    })
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        try {
            String token = userUseCase.login(
                    request.getEmail(),
                    request.getPassword()
            );
            return ResponseEntity.ok(new LoginResponseDto(token));

        } catch (NoSuchElementException ex) {
            // 존재하지 않는 이메일
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);

        } catch (IllegalArgumentException ex) {
            // 비밀번호 불일치 등 BadRequest
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
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
