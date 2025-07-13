package org.univ.rankus.domain.model.attendance.exception;

import org.univ.rankus.common.exception.BaseCustomException;

public class AttendancePermissionException extends BaseCustomException {
    public AttendancePermissionException() {
        super(AttendanceErrorCode.INSUFFICIENT_PERMISSION_FOR_ATTENDANCE);
    }

    public AttendancePermissionException(AttendanceErrorCode errorCode) {
        super(errorCode);
    }

    public AttendancePermissionException(AttendanceErrorCode errorCode, String message) {
        super(errorCode);
    }
}