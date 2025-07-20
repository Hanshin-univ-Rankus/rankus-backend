package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.QRToken;

import java.time.LocalDateTime;

/**
 * QR 토큰 응답 DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "QR 토큰 응답 정보")
public class QRTokenResponseDto {

    @Schema(description = "QR 토큰 문자열", example = "qr_token_12345abcde")
    private String token;

    @Schema(description = "출석 세션 ID", example = "1")
    private Long sessionId;

    @Schema(description = "토큰 생성일시")
    private LocalDateTime generatedAt;

    @Schema(description = "토큰 만료일시")
    private LocalDateTime expiresAt;

    @Schema(description = "만료 여부", example = "false")
    private Boolean isExpired;

    public QRTokenResponseDto(QRToken qrToken) {
        this.token = qrToken.getToken();
        this.sessionId = qrToken.getSessionId();
        this.generatedAt = qrToken.getGeneratedAt();
        this.expiresAt = qrToken.getExpiresAt();
        this.isExpired = qrToken.isExpired();
    }

    public static QRTokenResponseDto from(QRToken qrToken) {
        return new QRTokenResponseDto(qrToken);
    }
}