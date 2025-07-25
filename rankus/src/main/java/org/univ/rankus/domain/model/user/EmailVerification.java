package org.univ.rankus.domain.model.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.user.exception.EmailVerificationErrorCode;
import org.univ.rankus.domain.model.user.exception.EmailVerificationException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * EmailVerification 엔티티 - 이메일 인증 정보를 나타내는 도메인 모델
 *
 * @hs.ac.kr 도메인 이메일 인증을 위한 6자리 인증번호 관리
 */
@Getter
@Entity
@Table(name = "email_verifications", indexes = {
        @Index(name = "idx_email_verification_email", columnList = "email"),
        @Index(name = "idx_email_verification_status", columnList = "verified")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseTimeEntity {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 5;
    private static final String HS_EMAIL_DOMAIN = "@hs.ac.kr";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email; // 인증할 이메일 (@hs.ac.kr 도메인만 허용)

    @Column(nullable = false, length = 6)
    private String verificationCode; // 6자리 인증번호

    @Column(nullable = false)
    private LocalDateTime expiryTime; // 인증번호 만료 시간

    @Column(nullable = false)
    private boolean verified; // 인증 완료 여부

    @Column(nullable = false)
    private int sendCount; // 인증번호 발송 횟수 (스팸 방지)

    /**
     * 이메일 인증 생성자
     *
     * @param email @hs.ac.kr 도메인 이메일
     */
    public EmailVerification(String email) {
        this.email = validateHsEmail(email);
        this.verificationCode = generateVerificationCode();
        this.expiryTime = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
        this.verified = false;
        this.sendCount = 1;
    }

    /**
     * @hs.ac.kr 도메인 이메일 검증
     */
    private String validateHsEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new EmailVerificationException(EmailVerificationErrorCode.EMAIL_DOMAIN_INVALID);
        }

        String trimmedEmail = email.trim().toLowerCase();

        if (!trimmedEmail.endsWith(HS_EMAIL_DOMAIN)) {
            throw new EmailVerificationException(EmailVerificationErrorCode.EMAIL_DOMAIN_INVALID);
        }

        // 기본 이메일 형식 검증
        if (!trimmedEmail.matches("^[A-Za-z0-9._%+-]+@hs\\.ac\\.kr$")) {
            throw new EmailVerificationException(EmailVerificationErrorCode.EMAIL_DOMAIN_INVALID);
        }

        return trimmedEmail;
    }

    /**
     * 6자리 인증번호 생성
     */
    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 인증번호 재생성 (재발송 시 사용)
     */
    public void regenerateCode() {
        if (this.verified) {
            throw new EmailVerificationException(EmailVerificationErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        // 발송 횟수 제한 체크 (1시간 내 5회 제한)
        if (this.sendCount >= 5) {
            throw new EmailVerificationException(EmailVerificationErrorCode.VERIFICATION_SEND_LIMIT_EXCEEDED);
        }

        this.verificationCode = generateVerificationCode();
        this.expiryTime = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
        this.sendCount++;
    }

    /**
     * 인증번호 검증
     *
     * @param inputCode 사용자가 입력한 인증번호
     * @return 인증 성공 여부
     */
    public boolean verifyCode(String inputCode) {
        if (!StringUtils.hasText(inputCode)) {
            throw new EmailVerificationException(EmailVerificationErrorCode.VERIFICATION_CODE_REQUIRED);
        }

        if (this.verified) {
            throw new EmailVerificationException(EmailVerificationErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        if (isExpired()) {
            throw new EmailVerificationException(EmailVerificationErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        String trimmedCode = inputCode.trim();
        if (!this.verificationCode.equals(trimmedCode)) {
            throw new EmailVerificationException(EmailVerificationErrorCode.VERIFICATION_CODE_INVALID);
        }

        this.verified = true;
        return true;
    }

    /**
     * 인증번호 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryTime);
    }

    /**
     * 인증 완료 여부 확인
     */
    public boolean isVerified() {
        return this.verified;
    }

    /**
     * 이메일 도메인이 @hs.ac.kr인지 정적으로 검증하는 유틸리티 메서드
     */
    public static boolean isHsEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        String trimmedEmail = email.trim().toLowerCase();
        return trimmedEmail.endsWith(HS_EMAIL_DOMAIN) &&
                trimmedEmail.matches("^[A-Za-z0-9._%+-]+@hs\\.ac\\.kr$");
    }

    /**
     * 발송 가능한 상태인지 확인
     */
    public boolean canSendVerification() {
        return !this.verified && this.sendCount < 5;
    }
}