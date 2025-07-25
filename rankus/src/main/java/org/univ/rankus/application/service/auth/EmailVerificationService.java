package org.univ.rankus.application.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.EmailVerificationUseCase;
import org.univ.rankus.application.port.out.EmailSendPort;
import org.univ.rankus.application.port.out.EmailVerificationRepositoryPort;
import org.univ.rankus.domain.model.user.EmailVerification;
import org.univ.rankus.domain.model.user.exception.EmailVerificationErrorCode;
import org.univ.rankus.domain.model.user.exception.EmailVerificationException;

/**
 * 이메일 인증 서비스 구현체
 * - @hs.ac.kr 도메인 이메일 인증번호 발송 및 검증
 * - 헥사고날 아키텍처 패턴 준수
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService implements EmailVerificationUseCase {

    private final EmailVerificationRepositoryPort emailVerificationRepository;
    private final EmailSendPort emailSendPort;

    @Override
    public EmailVerification sendVerificationCode(String email) {
        // 1. @hs.ac.kr 도메인 검증 (EmailVerification 생성자에서 자동 검증)
        log.info("이메일 인증번호 발송 요청: {}", email);

        try {
            // 2. 기존 인증 정보 확인
            var existingVerification = emailVerificationRepository.findLatestByEmail(email);

            EmailVerification emailVerification;
            if (existingVerification.isPresent() && !existingVerification.get().isVerified()) {
                // 기존 미인증 정보가 있으면 인증번호 재생성
                emailVerification = existingVerification.get();
                emailVerification.regenerateCode();
                log.info("기존 인증번호 재생성: {}", email);
            } else {
                // 새로운 인증 정보 생성
                emailVerification = new EmailVerification(email);
                log.info("새로운 인증번호 생성: {}", email);
            }

            // 3. 인증 정보 저장
            emailVerification = emailVerificationRepository.save(emailVerification);

            // 4. 이메일 발송
            try {
                emailSendPort.sendVerificationCode(email, emailVerification.getVerificationCode());
                log.info("인증번호 이메일 발송 성공: {}", email);
            } catch (Exception e) {
                log.error("인증번호 이메일 발송 실패: {}, error: {}", email, e.getMessage());
                throw new EmailVerificationException(EmailVerificationErrorCode.EMAIL_SEND_FAILED);
            }

            return emailVerification;

        } catch (EmailVerificationException e) {
            // 도메인 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("이메일 인증번호 발송 중 예상치 못한 오류: {}, error: {}", email, e.getMessage());
            throw new EmailVerificationException(EmailVerificationErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public EmailVerification verifyCode(String email, String verificationCode) {
        log.info("이메일 인증번호 검증 요청: {}", email);

        // 1. 최신 인증 정보 조회
        EmailVerification emailVerification = emailVerificationRepository.findLatestByEmail(email)
                .orElseThrow(() -> new EmailVerificationException(EmailVerificationErrorCode.VERIFICATION_NOT_FOUND));

        // 2. 인증번호 검증 (도메인 로직)
        try {
            emailVerification.verifyCode(verificationCode);
            log.info("이메일 인증 성공: {}", email);
        } catch (EmailVerificationException e) {
            log.warn("이메일 인증 실패: {}, error: {}", email, e.getMessage());
            throw e;
        }

        // 3. 인증 완료된 정보 저장
        return emailVerificationRepository.save(emailVerification);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        log.debug("이메일 인증 상태 확인: {}", email);
        return emailVerificationRepository.existsVerifiedByEmail(email);
    }

    @Override
    public EmailVerification resendVerificationCode(String email) {
        log.info("이메일 인증번호 재발송 요청: {}", email);

        // 1. 기존 인증 정보 조회
        EmailVerification emailVerification = emailVerificationRepository.findLatestByEmail(email)
                .orElseThrow(() -> new EmailVerificationException(EmailVerificationErrorCode.VERIFICATION_NOT_FOUND));

        // 2. 재발송 가능 여부 확인 및 인증번호 재생성
        try {
            emailVerification.regenerateCode();
        } catch (EmailVerificationException e) {
            log.warn("인증번호 재발송 실패: {}, error: {}", email, e.getMessage());
            throw e;
        }

        // 3. 갱신된 정보 저장
        emailVerification = emailVerificationRepository.save(emailVerification);

        // 4. 이메일 재발송
        try {
            emailSendPort.sendVerificationCode(email, emailVerification.getVerificationCode());
            log.info("인증번호 재발송 성공: {}", email);
        } catch (Exception e) {
            log.error("인증번호 재발송 중 이메일 발송 실패: {}, error: {}", email, e.getMessage());
            throw new EmailVerificationException(EmailVerificationErrorCode.EMAIL_SEND_FAILED);
        }

        return emailVerification;
    }
}