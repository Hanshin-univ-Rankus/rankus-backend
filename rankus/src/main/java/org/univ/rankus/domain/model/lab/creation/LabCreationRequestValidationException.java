package org.univ.rankus.domain.model.lab.creation;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * LabCreationRequest 도메인의 입력 검증(validation) 오류를 나타내는 예외
 * - 반드시 LabCreationRequestErrorCode(BAD_REQUEST)만 넘겨야 합니다.
 */
public class LabCreationRequestValidationException extends BaseCustomException {

    public LabCreationRequestValidationException(LabCreationRequestErrorCode errorCode) {
        super(errorCode);
        // errorCode.getStatus()가 HttpStatus.BAD_REQUEST인지 확인(optional)
    }
}