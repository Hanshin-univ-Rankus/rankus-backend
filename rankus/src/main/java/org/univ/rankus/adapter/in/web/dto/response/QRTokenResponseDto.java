package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.QRToken;

import java.time.LocalDateTime;

/**
 * QR 토큰 응답 DTO
 */
@Data
@NoArgsConstructor
public class QRTokenResponseDto {

    private String token;
    private Long sessionId;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
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