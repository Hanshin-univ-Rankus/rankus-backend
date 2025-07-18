package org.univ.rankus.domain.model.calendar.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * CalendarEvent 도메인 유효성 검증 실패 시 던져지는 예외
 */
public class CalendarEventValidationException extends BaseCustomException {
    public CalendarEventValidationException(CalendarEventErrorCode errorCode) {
        super(errorCode);
    }
}