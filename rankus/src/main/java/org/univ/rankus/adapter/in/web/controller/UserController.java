package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.user.User;

@Tag(name = "User", description = "사용자 정보 조회 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserQueryUseCase userQueryUseCase;

    /**
     * GET /api/users/me
     * - 인증된 사용자의 정보를 조회하여 반환
     */
    @Operation(
            summary = "내 정보 조회",
            description = """
                    현재 로그인한 사용자의 정보를 조회합니다.
                    
                    ## 기능 설명
                    - JWT 토큰을 통해 인증된 사용자의 상세 정보를 반환
                    - 개인정보 보호를 위해 비밀번호는 제외하고 반환
                    - 사용자 프로필 페이지나 설정 페이지에서 활용
                    
                    ## 인증 필요
                    - Bearer 토큰이 필요합니다
                    - 유효하지 않은 토큰의 경우 401 에러 반환
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "사용자 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "사용자 정보 조회 성공",
                                              "data": {
                                                "id": 1,
                                                "name": "홍길동",
                                                "email": "hong@example.com",
                                                "studentNumber": "20210001",
                                                "phoneNumber": "010-1234-5678",
                                                "grade": 3,
                                                "enrollmentStatus": "ENROLLED",
                                                "role": "STUDENT",
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
                    responseCode = "401", 
                    description = "인증되지 않음 - 유효하지 않은 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "인증이 필요합니다",
                                              "data": null,
                                              "timestamp": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        User user = userQueryUseCase.getUserById(principal.getUserId());
        UserResponseDto dto = UserResponseDto.from(user);
        ApiResponse<UserResponseDto> body = ApiResponse.success(dto, "사용자 정보 조회 성공");
        return ResponseEntity.ok(body);
    }
}
