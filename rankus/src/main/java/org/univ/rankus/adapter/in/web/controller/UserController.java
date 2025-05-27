package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.adapter.in.web.controller.AuthController.UserResponseDto;
import org.univ.rankus.application.port.in.UserQueryUseCase;
import org.univ.rankus.domain.model.user.User;

import java.util.NoSuchElementException;

@RestController
@RequestMapping(path = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "사용자 API", description = "사용자 정보 조회 관련 API")
public class UserController {

    private final UserQueryUseCase queryUseCase;

    public UserController(UserQueryUseCase queryUseCase) {
        this.queryUseCase = queryUseCase;
    }

    @Operation(
        summary = "내 프로필 조회",
        description = "현재 인증된 사용자의 프로필 정보를 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getProfile(Authentication authentication) {
        String email = authentication.getName();
        try {
            User user = queryUseCase.getProfile(email);
            return ResponseEntity.ok(UserResponseDto.from(user));
        } catch (NoSuchElementException ex) {
            // 사용자 없으면 404 직접 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }
}
