package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.request.EmailVerificationConfirmRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.EmailVerificationRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.adapter.in.web.dto.response.EmailVerificationResponseDto;
import org.univ.rankus.application.port.in.command.EmailVerificationUseCase;
import org.univ.rankus.domain.model.user.EmailVerification;

/**
 * 이메일 인증 컨트롤러
 * - @hs.ac.kr 도메인 이메일 인증번호 발송/검증 API
 * - RESTful API 설계 및 Swagger 문서화
 */
@Slf4j
@Tag(name = "Email Verification", description = "이메일 인증 API")
@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationUseCase emailVerificationUseCase;

    @Operation(
            summary = "이메일 인증번호 발송",
            description = "@hs.ac.kr 도메인 이메일로 6자리 인증번호를 발송합니다."
    )
    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<EmailVerificationResponseDto>> sendVerificationCode(
            @Valid @RequestBody EmailVerificationRequestDto request) {

        log.info("이메일 인증번호 발송 요청: {}", request.getEmail());

        EmailVerification emailVerification = emailVerificationUseCase.sendVerificationCode(request.getEmail());
        EmailVerificationResponseDto response = EmailVerificationResponseDto.from(emailVerification);

        return ResponseEntity.ok(ApiResponse.success(response, "인증번호가 발송되었습니다."));
    }

    @Operation(
            summary = "이메일 인증번호 검증",
            description = "발송된 6자리 인증번호를 검증하여 이메일 인증을 완료합니다."
    )
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<EmailVerificationResponseDto>> verifyCode(
            @Valid @RequestBody EmailVerificationConfirmRequestDto request) {

        log.info("이메일 인증번호 검증 요청: {}", request.getEmail());

        EmailVerification emailVerification = emailVerificationUseCase.verifyCode(
                request.getEmail(),
                request.getVerificationCode()
        );
        EmailVerificationResponseDto response = EmailVerificationResponseDto.from(emailVerification);

        return ResponseEntity.ok(ApiResponse.success(response, "이메일 인증이 완료되었습니다."));
    }

    @Operation(
            summary = "이메일 인증번호 재발송",
            description = "기존 인증번호의 재발송을 요청합니다. 발송 횟수 제한이 있습니다."
    )
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<EmailVerificationResponseDto>> resendVerificationCode(
            @Valid @RequestBody EmailVerificationRequestDto request) {

        log.info("이메일 인증번호 재발송 요청: {}", request.getEmail());

        EmailVerification emailVerification = emailVerificationUseCase.resendVerificationCode(request.getEmail());
        EmailVerificationResponseDto response = EmailVerificationResponseDto.from(emailVerification);

        return ResponseEntity.ok(ApiResponse.success(response, "인증번호가 재발송되었습니다."));
    }

    @Operation(
            summary = "이메일 인증 상태 확인",
            description = "해당 이메일의 인증 완료 여부를 확인합니다."
    )
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkVerificationStatus(
            @RequestParam String email) {

        log.info("이메일 인증 상태 확인 요청: {}", email);

        boolean isVerified = emailVerificationUseCase.isEmailVerified(email);

        return ResponseEntity.ok(ApiResponse.success(isVerified, "인증 상태 조회가 완료되었습니다."));
    }
}