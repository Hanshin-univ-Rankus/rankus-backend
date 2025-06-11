package org.univ.rankus.domain.model.lab.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * Lab 도메인의 입력 검증(validation) 오류를 나타내는 예외
 * - 반드시 LabErrorCode(BAD_REQUEST)만 넘겨야 합니다.
 */
public class LabValidationException extends BaseCustomException {

    public LabValidationException(LabErrorCode errorCode) {
        super(errorCode);
        // errorCode.getStatus()가 HttpStatus.BAD_REQUEST인지 확인(optional)
    }
}