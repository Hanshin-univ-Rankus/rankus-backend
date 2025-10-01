package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.SecureQRToken;

import java.time.LocalDateTime;

/**
 * Secure QR 토큰 응답 DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "보안 QR 토큰 응답 정보")
public class SecureQRTokenResponseDto {

    @Schema(description = "암호화된 QR 토큰 문자열", example = "AbCdEfGh....")
    private String encryptedToken;

    @Schema(description = "출석 세션 ID", example = "12")
    private Long sessionId;

    @Schema(description = "토큰 생성일시")
    private LocalDateTime generatedAt;

    @Schema(description = "토큰 만료일시")
    private LocalDateTime expiresAt;

    @Schema(description = "만료 여부", example = "false")
    private Boolean isExpired;

    @Schema(description = "스캔시 이동할 출석 체크 URL", example = "https://rankus.vercel.app/attend?qt=AbCd...")
    private String attendanceUrl;

    public SecureQRTokenResponseDto(SecureQRToken token, String attendanceUrl) {
        this.encryptedToken = token.getEncryptedToken();
        this.sessionId = token.getSessionId();
        this.generatedAt = token.getGeneratedAt();
        this.expiresAt = token.getExpiresAt();
        this.isExpired = token.isExpired();
        this.attendanceUrl = attendanceUrl;
    }

    public static SecureQRTokenResponseDto from(SecureQRToken token, String url) {
        return new SecureQRTokenResponseDto(token, url);
    }
}

