package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.user.EmailVerification;

/**
 * 이메일 인증 유스케이스 인터페이스
 * - @hs.ac.kr 도메인 이메일 인증번호 발송
 * - 인증번호 검증 및 인증 완료 처리
 */
public interface EmailVerificationUseCase {

    /**
     * 이메일 인증번호 발송
     * - @hs.ac.kr 도메인 검증
     * - 6자리 인증번호 생성 및 발송
     * - 기존 인증 정보가 있는 경우 새로운 인증번호로 갱신
     *
     * @param email @hs.ac.kr 도메인 이메일
     * @return 생성된 이메일 인증 정보
     */
    EmailVerification sendVerificationCode(String email);

    /**
     * 인증번호 검증 및 인증 완료 처리
     * - 인증번호 일치 여부 확인
     * - 만료 시간 검증
     * - 인증 완료 상태 업데이트
     *
     * @param email            이메일 주소
     * @param verificationCode 사용자 입력 인증번호
     * @return 인증 완료된 이메일 인증 정보
     */
    EmailVerification verifyCode(String email, String verificationCode);

    /**
     * 이메일 인증 상태 확인
     * - 해당 이메일의 인증 완료 여부 확인
     *
     * @param email 이메일 주소
     * @return 인증 완료 여부
     */
    boolean isEmailVerified(String email);

    /**
     * 인증번호 재발송
     * - 기존 인증 정보의 인증번호 갱신
     * - 발송 횟수 제한 확인
     *
     * @param email 이메일 주소
     * @return 갱신된 이메일 인증 정보
     */
    EmailVerification resendVerificationCode(String email);
}