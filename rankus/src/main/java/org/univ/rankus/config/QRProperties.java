package org.univ.rankus.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Secure QR 토큰 암복호화를 위한 비밀키 설정
 */
@Configuration
@ConfigurationProperties(prefix = "rankus.qr")
@Getter
@Setter
public class QRProperties {

    /**
     * AES-256-GCM 용 32바이트 비밀키 (고정 길이)
     */
    @NotBlank
    private String secretKey;

    /** 레거시 단순 조합 QRToken 허용 여부 (이행 기간용) */
    private boolean legacyEnabled = true;

    @PostConstruct
    public void validate() {
        if (secretKey == null || secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8).length != 32) {
            throw new IllegalStateException("rankus.qr.secret-key 는 32바이트여야 합니다. 현재 길이=" +
                    (secretKey == null ? 0 : secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8).length));
        }
    }
}
