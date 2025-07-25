package org.univ.rankus.domain.model.lab.resource.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * 랩실 자료 유효성 검증 실패 시 발생하는 예외
 */
public class LabResourceValidationException extends BaseCustomException {

    public LabResourceValidationException(LabResourceErrorCode errorCode) {
        super(errorCode);
    }
}