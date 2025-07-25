package org.univ.rankus.domain.model.lab.resource.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * 랩실 자료를 찾을 수 없는 경우 발생하는 예외
 */
public class LabResourceNotFoundException extends BaseCustomException {

    public LabResourceNotFoundException() {
        super(LabResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    public LabResourceNotFoundException(LabResourceErrorCode errorCode) {
        super(errorCode);
    }
}