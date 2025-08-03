package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.adapter.in.web.dto.request.TokenRefreshRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthTokens;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.in.command.AuthUseCase;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
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
    private final AuthTokenPort authTokenPort;

    @Operation(
            summary = "회원가입",
            description = """
                    새로운 사용자를 등록합니다.
                                        
                    ## 회원가입 절차
                    1. 사용자 정보 입력 (이름, 이메일, 비밀번호, 학번, 전화번호, 학년, 재학상태)
                    2. 이메일 중복 검사 및 유효성 검증
                    3. 비밀번호 암호화 저장
                    4. 사용자 계정 생성 및 **인증 이메일 발송**
                    5. **이메일 인증 완료 후 로그인 가능**
                                        
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
                                                "labId": null,
                                                "createdAt": "2024-01-15T10:30:00",
                                                "updatedAt": "2024-01-15T10:30:00"
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
                    2. **이메일 인증이 완료된 사용자인지 확인**
                    3. 사용자 인증 및 검증
                    4. JWT 토큰 생성 및 반환
                    5. 사용자 정보와 함께 응답
                                        
                    ## 토큰 사용법
                    - 반환받은 `token`을 요청 헤더에 포함하여 API 호출
                    - 헤더 형식: `Authorization: Bearer {token}`
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
                                                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "user": {
                                                  "id": 1,
                                                  "name": "홍길동",
                                                  "email": "hong@example.com",
                                                  "studentNumber": "20210001",
                                                  "phoneNumber": "010-1234-5678",
                                                  "grade": 3,
                                                  "enrollmentStatus": "ENROLLED",
                                                  "labId": null,
                                                  "createdAt": "2024-01-15T10:30:00",
                                                  "updatedAt": "2024-01-15T10:30:00"
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
                    description = "인증 실패(잘못된 이메일/비밀번호 또는 이메일 미인증)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "잘못된 자격 증명",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "이메일 또는 비밀번호가 올바르지 않습니다",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T10:30:00"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이메일 미인증",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "이메일 인증이 완료되지 않았습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T10:30:00"
                                                    }
                                                    """
                                    )
                            }
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

    @Operation(
            summary = "새로운 로그인 (토큰 쌍 발급)",
            description = """
                    이메일과 비밀번호로 로그인하여 액세스 토큰과 리프레시 토큰을 함께 발급받습니다.
                                        
                    ## 개선된 로그인 절차
                    1. 이메일과 비밀번호 입력
                    2. 사용자 인증 및 검증
                    3. 액세스 토큰(15분) + 리프레시 토큰(7일) 생성
                    4. 사용자 정보와 함께 응답
                                        
                    ## 토큰 사용법
                    - `accessToken`: API 요청 시 Authorization 헤더에 사용
                    - `refreshToken`: 액세스 토큰 만료 시 갱신용
                    - 헤더 형식: `Authorization: Bearer {accessToken}`
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
                                                "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
                                                "accessTokenExpiresAt": "2024-01-15T10:45:00",
                                                "refreshTokenExpiresAt": "2024-01-22T10:30:00",
                                                "user": {
                                                  "id": 1,
                                                  "name": "홍길동",
                                                  "email": "hong@example.com",
                                                  "studentNumber": "20210001",
                                                  "phoneNumber": "010-1234-5678",
                                                  "grade": 3,
                                                  "enrollmentStatus": "ENROLLED",
                                                  "labId": null
                                                }
                                              },
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login/v2")
    public ResponseEntity<ApiResponse<AuthTokens>> loginWithTokens(
            @Valid @RequestBody UserLoginRequestDto dto
    ) {
        User user = authUseCase.authenticate(dto);
        AuthTokens authTokens = authTokenPort.generateTokens(user);
        ApiResponse<AuthTokens> body = ApiResponse.success(authTokens, "로그인 성공");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    @Operation(
            summary = "액세스 토큰 갱신",
            description = """
                    리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
                                        
                    ## 토큰 갱신 절차
                    1. 만료된 액세스 토큰 감지
                    2. 저장된 리프레시 토큰으로 요청
                    3. 리프레시 토큰 유효성 검증
                    4. 새로운 액세스 토큰 발급
                                        
                    ## 주의사항
                    - 리프레시 토큰도 만료된 경우 재로그인 필요
                    - 새로운 액세스 토큰은 15분간 유효
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "토큰 갱신 성공",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "expiresAt": "2024-01-15T10:45:00"
                                              },
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "리프레시 토큰 만료 또는 무효",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "리프레시 토큰이 만료되었습니다",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refreshToken(
            @Valid @RequestBody TokenRefreshRequestDto request
    ) {
        String newAccessToken = authTokenPort.refreshAccessToken(request.getRefreshToken());

        Object responseData = new Object() {
            public final String accessToken = newAccessToken;
            public final java.time.LocalDateTime expiresAt = authTokenPort.getAccessTokenExpiryTime(newAccessToken);
        };

        ApiResponse<Object> body = ApiResponse.success(responseData, "토큰 갱신 성공");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    @Operation(
            summary = "로그아웃",
            description = """
                    현재 로그인된 사용자를 로그아웃하고 토큰을 무효화합니다.
                                        
                    ## 로그아웃 절차
                    1. Authorization 헤더에서 액세스 토큰 추출
                    2. 토큰을 블랙리스트에 등록
                    3. 해당 사용자의 리프레시 토큰 무효화
                    4. 로그아웃 완료
                                        
                    ## 보안 효과
                    - 탈취된 토큰의 재사용 방지
                    - 모든 디바이스에서 강제 로그아웃
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "로그아웃이 완료되었습니다",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        // Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        String accessToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }

        // AuthService를 통해 로그아웃 처리
        authUseCase.logout(userDetails.getUsername(), accessToken);

        ApiResponse<Void> body = ApiResponse.success(null, "로그아웃이 완료되었습니다");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }
}
