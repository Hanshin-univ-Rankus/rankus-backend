package org.univ.rankus.domain.model.attendance.exception;

import org.univ.rankus.common.exception.BaseCustomException;

public class AttendanceValidationException extends BaseCustomException {
    public AttendanceValidationException(AttendanceErrorCode errorCode) {
        super(errorCode);
    }

    public AttendanceValidationException(AttendanceErrorCode errorCode, String message) {
        super(errorCode);
    }
}