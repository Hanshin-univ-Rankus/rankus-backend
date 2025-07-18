package org.univ.rankus.domain.model.calendar.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * CalendarEvent 도메인 조회 실패(404 Not Found) 예외
 * - 반드시 CalendarEventErrorCode.CALENDAR_EVENT_NOT_FOUND를 넘겨야 합니다.
 */
public class CalendarEventNotFoundException extends BaseCustomException {

    public CalendarEventNotFoundException(CalendarEventErrorCode errorCode) {
        super(errorCode);
        // errorCode.getStatus()가 HttpStatus.NOT_FOUND인지 확인(optional)
    }
}