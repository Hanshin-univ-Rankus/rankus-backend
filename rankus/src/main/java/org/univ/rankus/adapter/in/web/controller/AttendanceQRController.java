package org.univ.rankus.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.adapter.in.web.dto.response.ApiResponse;
import org.univ.rankus.application.port.in.AttendanceSessionCommandUseCase;
import org.univ.rankus.application.port.in.AttendanceSessionQueryUseCase;
import org.univ.rankus.application.service.qr.QRTokenCryptoService;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.QRToken;
import org.univ.rankus.domain.model.attendance.SecureQRToken;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceNotFoundException;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;
import org.univ.rankus.domain.model.attendance.SessionStatus;

import java.time.LocalDateTime;

/**
 * 글로벌 출석 QR 엔드포인트 (새 흐름)
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Validated
@Tag(name = "AttendanceQR", description = "글로벌 출석 QR API")
public class AttendanceQRController {

    private final QRTokenCryptoService qrTokenCryptoService;
    private final AttendanceSessionQueryUseCase attendanceSessionQueryUseCase;
    private final AttendanceSessionCommandUseCase attendanceSessionCommandUseCase;

    @Operation(summary = "QR 토큰 해석", description = "암호화/레거시 QR 토큰을 해석하여 메타정보 및 유효성을 반환합니다.")
    @GetMapping("/qr/resolve")
    public ResponseEntity<ApiResponse<ResolveResponse>> resolve(@RequestParam("token") String token) {
        LocalDateTime now = LocalDateTime.now();
        ResolveResponse resp = new ResolveResponse();
        resp.setOriginalToken(token);
        // 1) Secure 우선
        try {
            SecureQRToken.QRTokenPayload payload = qrTokenCryptoService.decrypt(token);
            resp.setType("SECURE");
            resp.setLabId(payload.getLabId());
            resp.setSessionId(payload.getSessionId());
            resp.setExpiresAt(payload.getParsedExpiresAt());
            resp.setGeneratedAt(payload.getParsedGeneratedAt());
            if (now.isAfter(payload.getParsedExpiresAt())) {
                resp.expired("EXPIRED", "QR이 만료되었습니다");
                return ResponseEntity.ok(ApiResponse.success(resp, "QR 토큰 해석 완료"));
            }
            // 권한 없이 세션 공개 정보 조회
            AttendanceSession session = attendanceSessionQueryUseCase.findSessionByIdPublic(payload.getSessionId());
            resp.setSessionStatus(session.getStatus().name());
            resp.setSessionTitle(session.getTitle());
            if (session.getStatus() != SessionStatus.ACTIVE) {
                resp.invalid("SESSION_INACTIVE", "활성화된 출석 세션이 아닙니다");
            } else {
                resp.valid();
            }
            return ResponseEntity.ok(ApiResponse.success(resp, "QR 토큰 해석 완료"));
        } catch (Exception secureFail) {
            // secure 실패시 legacy 시도
        }
        // 2) Legacy 포맷
        try {
            QRToken legacy = QRToken.fromString(token);
            resp.setType("LEGACY");
            resp.setSessionId(legacy.getSessionId());
            resp.setGeneratedAt(legacy.getGeneratedAt());
            resp.setExpiresAt(legacy.getExpiresAt());
            resp.setLabId(legacy.getLabId());
            if (legacy.isExpired()) {
                resp.expired("EXPIRED", "QR이 만료되었습니다");
                return ResponseEntity.ok(ApiResponse.success(resp, "QR 토큰 해석 완료"));
            }
            AttendanceSession session = attendanceSessionQueryUseCase.findSessionByIdPublic(legacy.getSessionId());
            resp.setSessionStatus(session.getStatus().name());
            resp.setSessionTitle(session.getTitle());
            if (session.getStatus() != SessionStatus.ACTIVE) {
                resp.invalid("SESSION_INACTIVE", AttendanceErrorCode.SESSION_NOT_ACTIVE.getMessage());
            } else {
                resp.valid();
            }
            return ResponseEntity.ok(ApiResponse.success(resp, "QR 토큰 해석 완료"));
        } catch (AttendanceValidationException e) {
            resp.invalid("INVALID", AttendanceErrorCode.QR_TOKEN_INVALID.getMessage());
            return ResponseEntity.ok(ApiResponse.success(resp, "QR 토큰 해석 완료"));
        } catch (AttendanceNotFoundException e) {
            resp.invalid("SESSION_NOT_FOUND", AttendanceErrorCode.SESSION_NOT_FOUND.getMessage());
            return ResponseEntity.ok(ApiResponse.success(resp, "QR 토큰 해석 완료"));
        }
    }

    @Operation(summary = "출석 체크 (글로벌)", description = "토큰만 전달하여 출석을 체크합니다. (기존 labId 경로 기반 엔드포인트 대체)")
    @PostMapping("/check")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceRecordGlobalResponse>> globalCheck(
            @Valid @RequestBody GlobalCheckRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long labId = extractLabId(request.getToken());
        AttendanceRecord record = attendanceSessionCommandUseCase.checkAttendance(labId, request.getToken(), userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(AttendanceRecordGlobalResponse.from(record), "출석 체크가 완료되었습니다"));
    }

    /** 토큰에서 labId 추출 (secure 우선, 실패 시 legacy) */
    private Long extractLabId(String token) {
        try {
            SecureQRToken.QRTokenPayload payload = qrTokenCryptoService.decrypt(token);
            return payload.getLabId();
        } catch (Exception e) {
            QRToken legacy = QRToken.fromString(token);
            return legacy.getLabId();
        }
    }

    // ================= DTOs =================
    @Data
    public static class GlobalCheckRequest {
        @NotBlank
        private String token;
    }

    @Data
    public static class AttendanceRecordGlobalResponse {
        private Long recordId;
        private Long sessionId;
        private Long userId;
        private LocalDateTime checkedAt;
        public static AttendanceRecordGlobalResponse from(AttendanceRecord r) {
            AttendanceRecordGlobalResponse dto = new AttendanceRecordGlobalResponse();
            dto.recordId = r.getRecordId();
            dto.sessionId = r.getAttendanceSession().getSessionId();
            dto.userId = r.getUserId();
            dto.checkedAt = r.getCheckedAt();
            return dto;
        }
    }

    @Data
    public static class ResolveResponse {
        private String originalToken;
        private String type; // SECURE / LEGACY
        private boolean valid;
        private String reason; // INVALID / EXPIRED / SESSION_INACTIVE / SESSION_NOT_FOUND 등
        private String message;
        private Long labId;
        private Long sessionId;
        private String sessionTitle;
        private String sessionStatus;
        private LocalDateTime generatedAt;
        private LocalDateTime expiresAt;
        public void valid() { this.valid = true; this.reason = "OK"; }
        public void invalid(String reason, String message) { this.valid = false; this.reason = reason; this.message = message; }
        public void expired(String reason, String message) { invalid(reason, message); }
    }
}
