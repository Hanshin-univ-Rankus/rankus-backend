package org.univ.rankus.domain.model.lab.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * LabApplication(가입신청) 도메인의 입력 검증(validation) 오류 예외
 * - 반드시 LabApplicationErrorCode(BAD_REQUEST)만 넘겨야 합니다.
 */
public class LabApplicationValidationException extends BaseCustomException {

    public LabApplicationValidationException(LabApplicationErrorCode errorCode) {
        super(errorCode);
        // errorCode.getStatus()가 HttpStatus.BAD_REQUEST인지 확인(optional)
    }
}