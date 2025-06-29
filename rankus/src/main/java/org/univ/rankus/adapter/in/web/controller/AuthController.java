package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;          // Swagger @ApiResponses
import io.swagger.v3.oas.annotations.media.Content;                    // Swagger @Content
import io.swagger.v3.oas.annotations.media.Schema;                     // Swagger @Schema
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;         // DTO 래퍼 클래스
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.in.command.AuthUseCase;
import org.univ.rankus.domain.model.user.User;

import java.net.URI;

/**
 * 인증(회원가입·로그인) 관련 REST API 컨트롤러
 */
@Tag(name = "Auth", description = "인증 API (회원가입·로그인)")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    @Operation(
            summary     = "회원가입",
            description = "새로운 사용자를 가입 처리하고, ApiResponse<UserResponseDto> 형태로 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description  = "회원가입 성공",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(allOf = { ApiResponse.class, UserResponseDto.class })
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description  = "입력값 검증 실패",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(allOf = { ApiResponse.class })
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description  = "이메일 중복 등 회원가입 실패",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(allOf = { ApiResponse.class })
                    )
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(
            @Valid @RequestBody UserRegisterRequestDto requestDto
    ) {
        User created = authUseCase.signUp(
                requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPassword(),
                requestDto.getRole()
        );
        UserResponseDto dto = UserResponseDto.from(created);
        ApiResponse<UserResponseDto> body = ApiResponse.created(dto, "회원가입 성공");
        URI location = URI.create("/api/users/" + created.getId());
        return ResponseEntity
                .created(location)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }

    @Operation(
            summary     = "로그인",
            description = "이메일과 비밀번호로 로그인하여 JWT 토큰 및 유저 정보를 ApiResponse<AuthResponseDto>로 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description  = "로그인 성공",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(allOf = { ApiResponse.class, AuthResponseDto.class })
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description  = "입력값 검증 실패",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(allOf = { ApiResponse.class })
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description  = "인증 실패(잘못된 이메일/비밀번호)",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(allOf = { ApiResponse.class })
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description  = "사용자 정보 없음",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(allOf = { ApiResponse.class })
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody UserLoginRequestDto dto
    ) {
        AuthResponseDto authDto = authUseCase.login(dto.getEmail(), dto.getPassword());
        ApiResponse<AuthResponseDto> body = ApiResponse.success(authDto, "로그인 성공");
        return ResponseEntity.ok().body(body);
    }
}
