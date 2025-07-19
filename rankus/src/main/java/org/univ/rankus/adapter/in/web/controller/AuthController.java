package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
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
            summary = "회원가입",
            description = """
                    새로운 사용자를 등록합니다.
                    
                    ## 회원가입 절차
                    1. 사용자 정보 입력 (이름, 이메일, 비밀번호, 학번, 전화번호, 학년, 재학상태)
                    2. 이메일 중복 검사 및 유효성 검증
                    3. 비밀번호 암호화 저장
                    4. 사용자 계정 생성 완료
                    
                    ## 주의사항
                    - 이메일은 중복될 수 없습니다
                    - 학번은 8-20자리 숫자여야 합니다
                    - 비밀번호는 8자 이상이어야 합니다
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "회원가입 성공",
                                              "data": {
                                                "id": 1,
                                                "name": "홍길동",
                                                "email": "hong@example.com",
                                                "studentNumber": "20210001",
                                                "phoneNumber": "010-1234-5678",
                                                "grade": 3,
                                                "enrollmentStatus": "ENROLLED",
                                                "role": "STUDENT"
                                              },
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "검증 실패",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "입력값 검증 실패",
                                              "data": null,
                                              "errors": [
                                                "이메일 형식이 올바르지 않습니다",
                                                "비밀번호는 8자 이상이어야 합니다"
                                              ],
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이메일 중복 등 회원가입 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "이메일 중복",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "이미 존재하는 이메일입니다",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(
            @Valid @RequestBody UserRegisterRequestDto requestDto
    ) {
        User created = authUseCase.signUp(requestDto);
        UserResponseDto dto = UserResponseDto.from(created);
        ApiResponse<UserResponseDto> body = ApiResponse.created(dto, "회원가입 성공");
        URI location = URI.create("/api/users/" + created.getId());
        return ResponseEntity.created(location)
                .cacheControl(CacheControl.noStore())
                .body(body);
    }


    @Operation(
            summary = "로그인",
            description = """
                    이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.
                    
                    ## 로그인 절차
                    1. 이메일과 비밀번호 입력
                    2. 사용자 인증 및 검증
                    3. JWT 토큰 생성 및 반환
                    4. 사용자 정보와 함께 응답
                    
                    ## 토큰 사용법
                    - 반환받은 `accessToken`을 요청 헤더에 포함하여 API 호출
                    - 헤더 형식: `Authorization: Bearer {accessToken}`
                    - 토큰 만료 시 재로그인 필요
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "로그인 성공",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 3600,
                                                "user": {
                                                  "id": 1,
                                                  "name": "홍길동",
                                                  "email": "hong@example.com",
                                                  "role": "STUDENT"
                                                }
                                              },
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "검증 실패",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "입력값 검증 실패",
                                              "data": null,
                                              "errors": [
                                                "이메일은 필수입니다",
                                                "비밀번호는 필수입니다"
                                              ],
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패(잘못된 이메일/비밀번호)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "이메일 또는 비밀번호가 올바르지 않습니다",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "사용자 없음",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "존재하지 않는 사용자입니다",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody UserLoginRequestDto dto
    ) {
        AuthResponseDto authDto = authUseCase.login(dto);
        ApiResponse<AuthResponseDto> body = ApiResponse.success(authDto, "로그인 성공");
        return ResponseEntity.ok().body(body);
    }
}
