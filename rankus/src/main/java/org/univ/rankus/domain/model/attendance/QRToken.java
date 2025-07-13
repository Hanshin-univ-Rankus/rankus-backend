package org.univ.rankus.domain.model.attendance;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.exception.AttendanceErrorCode;
import org.univ.rankus.domain.model.attendance.exception.AttendanceValidationException;

import java.time.LocalDateTime;

/**
 * QR 토큰 값 객체
 * - 출석 세션의 QR 코드를 나타내는 불변 객체
 * - 단순 조합 방식: labId-sessionId-timestamp
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QRToken {
    private String token;
    private Long sessionId;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;

    private QRToken(String token, Long sessionId, LocalDateTime generatedAt, LocalDateTime expiresAt) {
        this.token = token;
        this.sessionId = sessionId;
        this.generatedAt = generatedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * QR 토큰 생성 팩토리 메서드
     *
     * @param labId           랩실 ID
     * @param sessionId       세션 ID
     * @param validityMinutes 유효시간 (분)
     * @return 생성된 QR 토큰
     */
    public static QRToken create(Long labId, Long sessionId, Integer validityMinutes) {
        validateInputs(labId, sessionId, validityMinutes);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(validityMinutes);

        // 단순 조합 방식: labId-sessionId-timestamp
        String token = String.format("%d-%d-%d", labId, sessionId, now.toEpochSecond(java.time.ZoneOffset.UTC));

        return new QRToken(token, sessionId, now, expiresAt);
    }

    /**
     * 토큰 문자열로부터 QR 토큰 복원
     *
     * @param tokenString 토큰 문자열
     * @return 복원된 QR 토큰
     */
    public static QRToken fromString(String tokenString) {
        if (tokenString == null || tokenString.isBlank()) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_INVALID);
        }

        try {
            String[] parts = tokenString.split("-");
            if (parts.length != 3) {
                throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_INVALID);
            }

            Long labId = Long.parseLong(parts[0]);
            Long sessionId = Long.parseLong(parts[1]);
            long epochSecond = Long.parseLong(parts[2]);

            LocalDateTime generatedAt = LocalDateTime.ofEpochSecond(epochSecond, 0, java.time.ZoneOffset.UTC);

            // 기본 5분 유효시간으로 복원 (실제로는 세션에서 검증)
            LocalDateTime expiresAt = generatedAt.plusMinutes(5);

            return new QRToken(tokenString, sessionId, generatedAt, expiresAt);

        } catch (NumberFormatException e) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_INVALID);
        }
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 토큰 유효성 검사
     *
     * @param currentTime 현재 시간
     * @return 유효 여부
     */
    public boolean isValid(LocalDateTime currentTime) {
        return currentTime.isBefore(expiresAt) || currentTime.equals(expiresAt);
    }

    /**
     * 토큰에서 랩실 ID 추출
     *
     * @return 랩실 ID
     */
    public Long getLabId() {
        if (token == null || token.isBlank()) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_INVALID);
        }

        try {
            String[] parts = token.split("-");
            if (parts.length != 3) {
                throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_INVALID);
            }
            return Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_TOKEN_INVALID);
        }
    }

    private static void validateInputs(Long labId, Long sessionId, Integer validityMinutes) {
        if (labId == null || labId <= 0) {
            throw new AttendanceValidationException(AttendanceErrorCode.LAB_ID_REQUIRED);
        }

        if (sessionId == null || sessionId <= 0) {
            throw new AttendanceValidationException(AttendanceErrorCode.SESSION_ID_REQUIRED);
        }

        if (validityMinutes == null || validityMinutes < 1 || validityMinutes > 10) {
            throw new AttendanceValidationException(AttendanceErrorCode.QR_VALIDITY_INVALID);
        }
    }
}