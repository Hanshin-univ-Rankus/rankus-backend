package org.univ.rankus.adapter.in.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.application.port.in.command.AuthUseCase;
import org.univ.rankus.domain.model.user.User;

import java.net.URI;

/**
 * 인증(회원가입·로그인) 관련 REST API 컨트롤러
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final UserQueryUseCase userQueryUseCase;

    /**
     * POST /api/v1/auth/signup
     * - 새로운 회원 가입
     * - 201 Created + Location 헤더 + ApiResponse<UserResponseDto> 반환
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(
            @RequestBody @Valid UserRegisterRequestDto requestDto
    ) {
        // 1) 회원 가입 처리
        User created = authUseCase.signUp(
                requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPassword()
        );

        // 2) UserResponseDto 로 변환
        UserResponseDto dto = UserResponseDto.from(created);

        // 3) ApiResponse 래핑
        ApiResponse<UserResponseDto> body = ApiResponse.<UserResponseDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("회원가입 성공")
                .data(dto)
                .build();

        // 4) Location 헤더 설정 (새로 생성된 리소스 URI)
        URI location = URI.create("/api/v1/users/" + created.getId());

        // 5) 201 Created + Location + ApiResponse 바디 반환
        return ResponseEntity
                .created(location)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")  // (선택) 캐시 방지 헤더
                .body(body);
    }

    /**
     * POST /api/v1/auth/login
     * - 이메일/비밀번호로 로그인 → JWT 토큰 + 유저 정보 반환
     * - 200 OK + ApiResponse<AuthResponseDto> 반환
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @RequestBody @Valid UserLoginRequestDto dto
    ) {
        // 1) 인증 처리: 토큰 발급
        String token = authUseCase.login(dto.getEmail(), dto.getPassword());

        // 2) 토큰 발급된 사용자 정보 조회 후 DTO 변환
        User user = userQueryUseCase.getUserByEmail(dto.getEmail());
        UserResponseDto userDto = UserResponseDto.from(user);

        // 3) AuthResponseDto 생성 (token + userDto)
        AuthResponseDto authDto = AuthResponseDto.from(token, userDto);

        // 4) ApiResponse 래핑
        ApiResponse<AuthResponseDto> body = ApiResponse.<AuthResponseDto>builder()
                .status(HttpStatus.OK.value())
                .message("로그인 성공")
                .data(authDto)
                .build();

        // 5) 200 OK + ApiResponse 바디 반환
        return ResponseEntity
                .ok()
                .body(body);
    }
}