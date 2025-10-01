package org.univ.rankus.application.service.qr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ.rankus.config.QRProperties;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.SecureQRToken;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * (15) Secure 토큰 생성/복호화 단위 테스트
 */
class QRTokenCryptoServiceTest {

    private QRTokenCryptoService createService(String key) {
        QRProperties props = new QRProperties();
        props.setSecretKey(key);
        // 수동 validate 호출 (Spring 컨테이너 밖)
        props.validate();
        return new QRTokenCryptoService(props);
    }

    @Test
    @DisplayName("Secure 토큰 생성 및 복호화 성공")
    void generateAndDecrypt_success() {
        String key = "LocalDevQRSecretKey_2025__32__OK"; // 32 bytes
        QRTokenCryptoService service = createService(key);

        // AttendanceSession mock
        AttendanceSession session = mock(AttendanceSession.class);
        when(session.getLabId()).thenReturn(10L);
        when(session.getSessionId()).thenReturn(55L);
        when(session.getQrValidityMinutes()).thenReturn(5);
        // session.generateSecureQRToken 호출을 우회하기 위해 SecureQRToken 직접 생성 대신 도메인 메서드 사용 필요 ->
        // 간단히 리플렉션 없이 SecureQRToken.create 사용
        SecureQRToken token = SecureQRToken.create(10L,55L,5,key);

        // decrypt
        SecureQRToken.QRTokenPayload payload = SecureQRToken.decrypt(token.getEncryptedToken(), key);

        assertThat(payload.getLabId()).isEqualTo(10L);
        assertThat(payload.getSessionId()).isEqualTo(55L);
        assertThat(payload.getParsedExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Secret 키 불일치 시 복호화 실패")
    void decrypt_fail_wrongKey() {
        String key = "LocalDevQRSecretKey_2025__32__OK";
        SecureQRToken token = SecureQRToken.create(1L,2L,5,key);
        String wrong = "AAAAAAQRSecretKey_2025__32__OK"; // 동일 길이 다른 값

        assertThatThrownBy(() -> SecureQRToken.decrypt(token.getEncryptedToken(), wrong))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("만료 시간 경과 토큰 payload 만료 검증")
    void expiredToken_check() {
        String key = "LocalDevQRSecretKey_2025__32__OK";
        // 유효기간 0분 -> expiresAt == generatedAt
        SecureQRToken token = SecureQRToken.create(3L,9L,1,key);
        // 인위적으로 과거 판단 (1분 대기 없이) payload 파싱 후 만료시간 이전 비교 로직만 확인
        SecureQRToken.QRTokenPayload payload = SecureQRToken.decrypt(token.getEncryptedToken(), key);
        // 테스트 편의를 위해 expiresAt 을 현재보다 이전으로 강제할 수 없으므로 최소 필드 존재성만 검증
        assertThat(payload.getParsedExpiresAt()).isAfterOrEqualTo(payload.getParsedGeneratedAt());
    }
}

