package org.univ.rankus.domain.model.lab.resource.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * 랩실 자료 권한 관련 예외
 */
public class LabResourcePermissionException extends BaseCustomException {

    public LabResourcePermissionException(LabResourceErrorCode errorCode) {
        super(errorCode);
    }
}