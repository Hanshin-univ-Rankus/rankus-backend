package org.univ.rankus.application.port.out;

/**
 * 이메일 발송을 위한 포트 인터페이스
 * - 인증번호 이메일 발송
 * - HTML 형식 이메일 발송 지원
 */
public interface EmailSendPort {

    /**
     * 이메일 인증번호 발송
     *
     * @param toEmail          수신자 이메일 (@hs.ac.kr 도메인)
     * @param verificationCode 6자리 인증번호
     * @throws RuntimeException 이메일 발송 실패 시
     */
    void sendVerificationCode(String toEmail, String verificationCode);

    /**
     * 일반 이메일 발송 (향후 확장용)
     *
     * @param toEmail 수신자 이메일
     * @param subject 제목
     * @param content 내용 (HTML 지원)
     * @throws RuntimeException 이메일 발송 실패 시
     */
    void sendEmail(String toEmail, String subject, String content);
}