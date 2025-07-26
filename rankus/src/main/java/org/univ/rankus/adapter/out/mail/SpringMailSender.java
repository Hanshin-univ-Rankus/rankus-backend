package org.univ.rankus.adapter.out.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.out.EmailSendPort;

/**
 * Spring Boot Mail을 활용한 이메일 발송 어댑터
 * - EmailSendPort의 구현체
 * - Mailjet SMTP를 통한 이메일 발송
 * - HTML 형식 이메일 지원
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringMailSender implements EmailSendPort {

    private final JavaMailSender mailSender;
    
    @Value("${mailjet.from.email}")
    private String fromEmail;
    
    @Value("${mailjet.from.name}")
    private String fromName;

    @Override
    public void sendVerificationCode(String toEmail, String verificationCode) {
        String subject = "[Rankus] 이메일 인증번호";
        String content = createVerificationEmailContent(verificationCode);

        try {
            sendHtmlEmail(toEmail, subject, content);
            log.info("인증번호 이메일 발송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("인증번호 이메일 발송 실패: {}, error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendEmail(String toEmail, String subject, String content) {
        try {
            sendHtmlEmail(toEmail, subject, content);
            log.info("일반 이메일 발송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("일반 이메일 발송 실패: {}, error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * HTML 형식 이메일 발송
     */
    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        try {
            helper.setFrom(fromEmail, fromName);
        } catch (UnsupportedEncodingException e) {
            log.warn("발신자 이름 인코딩 실패, 이메일만 설정: {}", e.getMessage());
            helper.setFrom(fromEmail);
        }
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // HTML 형식으로 전송

        mailSender.send(message);
        log.debug("Mailjet 이메일 발송 완료: from={} to={} subject={}", fromEmail, toEmail, subject);
    }

    /**
     * 텍스트 형식 이메일 발송 (백업용)
     */
    @SuppressWarnings("unused")
    private void sendTextEmail(String toEmail, String subject, String textContent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject(subject);
        message.setText(textContent);

        mailSender.send(message);
        log.debug("Mailjet 텍스트 이메일 발송 완료: from={} to={} subject={}", fromEmail, toEmail, subject);
    }

    /**
     * 인증번호 이메일 HTML 템플릿 생성
     */
    private String createVerificationEmailContent(String verificationCode) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Rankus 이메일 인증</title>
                    <style>
                        body { font-family: 'Malgun Gothic', sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; text-align: center; padding: 20px; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
                        .verification-code { background-color: #e8f5e8; padding: 20px; text-align: center; margin: 20px 0; border-radius: 5px; border-left: 4px solid #4CAF50; }
                        .code { font-size: 32px; font-weight: bold; color: #2e7d32; letter-spacing: 5px; font-family: monospace; }
                        .warning { color: #f44336; font-size: 14px; margin-top: 20px; }
                        .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Rankus 이메일 인증</h1>
                        </div>
                        <div class="content">
                            <h2>안녕하세요!</h2>
                            <p>Rankus 서비스 이용을 위한 이메일 인증번호를 발송해드립니다.</p>
                            
                            <div class="verification-code">
                                <p>인증번호</p>
                                <div class="code">%s</div>
                            </div>
                            
                            <p>위 인증번호를 입력하여 이메일 인증을 완료해주세요.</p>
                            
                            <div class="warning">
                                ⚠️ <strong>주의사항</strong><br>
                                • 인증번호는 발송 후 <strong>5분간</strong> 유효합니다.<br>
                                • 본인이 요청하지 않은 인증번호라면 무시하셔도 됩니다.<br>
                                • 인증번호를 타인과 공유하지 마세요.
                            </div>
                            
                            <div class="footer">
                                <p>본 메일은 발신전용입니다. 문의사항은 Rankus 서비스 내 고객센터를 이용해주세요.</p>
                                <p>&copy; 2024 Rankus. All rights reserved.</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """, verificationCode);
    }
}