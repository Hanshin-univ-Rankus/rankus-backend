package org.univ.rankus.domain.model.attendance.exception;

import org.univ.rankus.common.exception.BaseCustomException;

public class AttendanceNotFoundException extends BaseCustomException {
    public AttendanceNotFoundException() {
        super(AttendanceErrorCode.SESSION_NOT_FOUND);
    }

    public AttendanceNotFoundException(Long id) {
        super(AttendanceErrorCode.SESSION_NOT_FOUND);
    }

    public AttendanceNotFoundException(AttendanceErrorCode errorCode) {
        super(errorCode);
    }

    public AttendanceNotFoundException(AttendanceErrorCode errorCode, String message) {
        super(errorCode);
    }
}