package org.univ.rankus.domain.model.attendance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * 보안 강화된 QR 토큰 값 객체
 * - AES-256-GCM 암호화 기반
 * - 예측 불가능한 토큰 생성
 * - 재사용 공격 방지
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SecureQRToken {
    private String encryptedToken;
    private Long sessionId;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
    private String nonce; // 재사용 방지용 일회성 값

    private SecureQRToken(String encryptedToken, Long sessionId, LocalDateTime generatedAt,
                          LocalDateTime expiresAt, String nonce) {
        this.encryptedToken = encryptedToken;
        this.sessionId = sessionId;
        this.generatedAt = generatedAt;
        this.expiresAt = expiresAt;
        this.nonce = nonce;
    }

    /**
     * 보안 QR 토큰 생성
     *
     * @param labId           랩실 ID
     * @param sessionId       세션 ID
     * @param validityMinutes 유효시간 (분)
     * @param secretKey       암호화 키 (32바이트)
     * @return 생성된 보안 QR 토큰
     */
    public static SecureQRToken create(Long labId, Long sessionId, Integer validityMinutes, String secretKey) {
        validateInputs(labId, sessionId, validityMinutes, secretKey);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(validityMinutes);
        String nonce = UUID.randomUUID().toString();

        // 토큰 페이로드 생성 (JSON 형식)
        QRTokenPayload payload = new QRTokenPayload(
                labId,
                sessionId,
                now.toString(),
                expiresAt.toString(),
                nonce
        );

        String encryptedToken = encryptPayload(payload.toJson(), secretKey);

        return new SecureQRToken(encryptedToken, sessionId, now, expiresAt, nonce);
    }

    /**
     * 암호화된 토큰 복호화
     *
     * @param encryptedToken 암호화된 토큰
     * @param secretKey      복호화 키
     * @return 복호화된 페이로드
     */
    public static QRTokenPayload decrypt(String encryptedToken, String secretKey) {
        try {
            String decryptedJson = decryptPayload(encryptedToken, secretKey);
            return QRTokenPayload.fromJson(decryptedJson);
        } catch (Exception e) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_CORRUPTED);
        }
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean isValid(LocalDateTime currentTime) {
        return currentTime.isBefore(expiresAt) || currentTime.equals(expiresAt);
    }

    /**
     * AES-256-GCM 암호화
     */
    private static String encryptPayload(String payload, String secretKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // 랜덤 IV 생성
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encryptedData = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // IV와 암호화된 데이터를 함께 Base64 인코딩
            byte[] result = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encryptedData, 0, result, iv.length, encryptedData.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(result);
        } catch (Exception e) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_INVALID);
        }
    }

    /**
     * AES-256-GCM 복호화
     */
    private static String decryptPayload(String encryptedToken, String secretKey) {
        try {
            byte[] data = Base64.getUrlDecoder().decode(encryptedToken);

            // IV와 암호화된 데이터 분리
            byte[] iv = new byte[12];
            byte[] encryptedData = new byte[data.length - 12];
            System.arraycopy(data, 0, iv, 0, 12);
            System.arraycopy(data, 12, encryptedData, 0, encryptedData.length);

            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decryptedData = cipher.doFinal(encryptedData);

            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_CORRUPTED);
        }
    }

    private static void validateInputs(Long labId, Long sessionId, Integer validityMinutes, String secretKey) {
        if (labId == null || labId <= 0) {
            throw new AttendanceValidationException(AttendanceErrorCode.LAB_ID_REQUIRED);
        }
        if (sessionId == null || sessionId <= 0) {
            throw new AttendanceValidationException(AttendanceErrorCode.SESSION_ID_REQUIRED);
        }
        if (validityMinutes == null || validityMinutes < 1 || validityMinutes > 10) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_VALIDITY_INVALID);
        }
        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length != 32) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_INVALID);
        }
    }

    /**
     * QR 토큰 페이로드 내부 클래스
     */
    @Getter
    public static class QRTokenPayload {
        private final Long labId;
        private final Long sessionId;
        private final String generatedAt;
        private final String expiresAt;
        private final String nonce;

        public QRTokenPayload(Long labId, Long sessionId, String generatedAt, String expiresAt, String nonce) {
            this.labId = labId;
            this.sessionId = sessionId;
            this.generatedAt = generatedAt;
            this.expiresAt = expiresAt;
            this.nonce = nonce;
        }

        public String toJson() {
            return String.format("{\"labId\":%d,\"sessionId\":%d,\"generatedAt\":\"%s\",\"expiresAt\":\"%s\",\"nonce\":\"%s\"}",
                    labId, sessionId, generatedAt, expiresAt, nonce);
        }

        public static QRTokenPayload fromJson(String json) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(json);
                Long labId = node.get("labId").asLong();
                Long sessionId = node.get("sessionId").asLong();
                String generatedAt = node.get("generatedAt").asText();
                String expiresAt = node.get("expiresAt").asText();
                String nonce = node.get("nonce").asText();
                return new QRTokenPayload(labId, sessionId, generatedAt, expiresAt, nonce);
            } catch (Exception e) {
                throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_CORRUPTED);
            }
        }

        public LocalDateTime getParsedGeneratedAt() {
            return LocalDateTime.parse(generatedAt);
        }

        public LocalDateTime getParsedExpiresAt() {
            return LocalDateTime.parse(expiresAt);
        }
    }
}