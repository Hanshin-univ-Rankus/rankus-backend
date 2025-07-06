package org.univ.rankus.domain.model.interview.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * Interview 도메인 검증 예외 클래스
 * - 면접 생성, 상태 변경, 슬롯 관리 시 검증 실패 예외
 * - 비즈니스 규칙 위반 시 발생
 */
public class InterviewValidationException extends BaseCustomException {

    public InterviewValidationException(InterviewErrorCode errorCode) {
        super(errorCode);
    }
}