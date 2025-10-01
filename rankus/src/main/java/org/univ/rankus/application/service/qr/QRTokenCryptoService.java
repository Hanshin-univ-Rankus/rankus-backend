package org.univ.rankus.application.service.qr;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.univ.rankus.config.QRProperties;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.SecureQRToken;

/**
 * Secure QR 토큰 생성/복호화 전담 서비스
 */
@Service
@RequiredArgsConstructor
public class QRTokenCryptoService {

    private final QRProperties qrProperties;

    /**
     * 세션으로부터 Secure QR 토큰 생성 (세션 활성/유효성 검증은 도메인에서 수행)
     */
    public SecureQRToken generateSecure(AttendanceSession session) {
        return session.generateSecureQRToken(qrProperties.getSecretKey());
    }

    /**
     * 암호화된 토큰 복호화
     */
    public SecureQRToken.QRTokenPayload decrypt(String encryptedToken) {
        return SecureQRToken.decrypt(encryptedToken, qrProperties.getSecretKey());
    }
}

