package org.univ.rankus.adapter.in.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserQueryUseCase userQueryUseCase;

    /**
     * GET /api/v1/users/me
     * - 인증된 사용자의 정보를 조회하여 반환
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        User user = userQueryUseCase.getUserById(principal.getUserId());
        UserResponseDto dto = UserResponseDto.from(user);
        ApiResponse<UserResponseDto> body = ApiResponse.<UserResponseDto>builder()
                .status(200)
                .message("사용자 정보 조회 성공")
                .data(dto)
                .build();
        return ResponseEntity.ok(body);
    }
}
