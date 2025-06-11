package org.univ.rankus.domain.model.lab.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * LabImage(랩실 이미지) 도메인의 입력 검증(validation) 오류 예외
 * - 반드시 LabImageErrorCode(BAD_REQUEST 또는 CONFLICT)만 넘겨야 합니다.
 */
public class LabImageValidationException extends BaseCustomException {

    public LabImageValidationException(LabImageErrorCode errorCode) {
        super(errorCode);
        // errorCode.getStatus()가 HttpStatus.BAD_REQUEST/CONFLICT인지 확인(optional)
    }
}