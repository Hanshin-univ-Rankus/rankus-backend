package org.univ.rankus.domain.model.user.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * 이메일 인증 관련 예외 클래스
 * - @hs.ac.kr 도메인 검증 실패
 * - 인증번호 생성/검증 실패
 * - 인증번호 만료 등의 상황에서 발생
 */
public class EmailVerificationException extends BaseCustomException {

    public EmailVerificationException(EmailVerificationErrorCode errorCode) {
        super(errorCode);
    }
}