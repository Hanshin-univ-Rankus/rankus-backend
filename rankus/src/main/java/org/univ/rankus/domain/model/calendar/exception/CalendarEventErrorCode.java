package org.univ.rankus.domain.model.calendar.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * CalendarEvent 도메인 관련 예외 코드 모음
 * - 코드 형식: "CALENDAR_EVENT_XXX"
 * - status, message를 한 곳에서 관리하여 일관된 응답을 생성할 수 있도록 함
 */
public enum CalendarEventErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 오류
    // ------------------------------------------------------------------------
    TITLE_REQUIRED("CALENDAR_EVENT_001", HttpStatus.BAD_REQUEST, "제목은 필수입니다."),
    TITLE_TOO_LONG("CALENDAR_EVENT_002", HttpStatus.BAD_REQUEST, "제목은 100자 이하여야 합니다."),
    DESCRIPTION_TOO_LONG("CALENDAR_EVENT_003", HttpStatus.BAD_REQUEST, "설명은 500자 이하여야 합니다."),
    EVENT_DATE_REQUIRED("CALENDAR_EVENT_004", HttpStatus.BAD_REQUEST, "날짜는 필수입니다."),
    EVENT_DATE_INVALID("CALENDAR_EVENT_005", HttpStatus.BAD_REQUEST, "날짜 형식이 올바르지 않습니다."),
    EVENT_DATE_PAST("CALENDAR_EVENT_006", HttpStatus.BAD_REQUEST, "과거 날짜는 설정할 수 없습니다."),
    EVENT_TYPE_REQUIRED("CALENDAR_EVENT_007", HttpStatus.BAD_REQUEST, "일정 타입은 필수입니다."),
    START_TIME_REQUIRED("CALENDAR_EVENT_008", HttpStatus.BAD_REQUEST, "시작 시간은 필수입니다."),
    END_TIME_REQUIRED("CALENDAR_EVENT_009", HttpStatus.BAD_REQUEST, "종료 시간은 필수입니다."),
    INVALID_TIME_RANGE("CALENDAR_EVENT_010", HttpStatus.BAD_REQUEST, "종료 시간은 시작 시간보다 늦어야 합니다."),
    LAB_REQUIRED("CALENDAR_EVENT_011", HttpStatus.BAD_REQUEST, "소속 랩은 필수입니다."),

    // ------------------------------------------------------------------------
    // 403 Forbidden: 권한 오류
    // ------------------------------------------------------------------------
    PERMISSION_DENIED("CALENDAR_EVENT_012", HttpStatus.FORBIDDEN, "해당 랩의 캘린더를 관리할 권한이 없습니다."),
    VIEW_PERMISSION_DENIED("CALENDAR_EVENT_013", HttpStatus.FORBIDDEN, "해당 랩의 캘린더를 조회할 권한이 없습니다."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    CALENDAR_EVENT_NOT_FOUND("CALENDAR_EVENT_014", HttpStatus.NOT_FOUND, "해당 캘린더 이벤트를 찾을 수 없습니다."),

    // ------------------------------------------------------------------------
    // 409 Conflict: 데이터 중복/충돌
    // ------------------------------------------------------------------------
    TIME_CONFLICT("CALENDAR_EVENT_015", HttpStatus.CONFLICT, "해당 시간대에 이미 다른 일정이 있습니다."),
    INTERVIEW_SYNC_CONFLICT("CALENDAR_EVENT_016", HttpStatus.CONFLICT, "면접 일정과 동기화 중 충돌이 발생했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    CalendarEventErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}