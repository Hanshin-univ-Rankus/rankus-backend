package org.univ.rankus.domain.model.lab.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;


/**
 * LabApplication(가입신청) 도메인 관련 예외 코드 모음
 * - 코드 형식: "APP_XXX"
 * - 중복 신청, 면접 시간 검증, 상태 오류 등을 관리
 */
public enum LabApplicationErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 / 비즈니스 상태 오류
    // ------------------------------------------------------------------------
    USER_ID_REQUIRED("APP_001", HttpStatus.BAD_REQUEST, "사용자 ID는 필수입니다."),
    INTERVIEW_TIME_REQUIRED("APP_002", HttpStatus.BAD_REQUEST, "면접 시간은 필수입니다."),
    INVALID_INTERVIEW_TIME("APP_003", HttpStatus.BAD_REQUEST, "유효하지 않은 면접 시간을 입력하였습니다."),
    DUPLICATE_APPLICATION("APP_004", HttpStatus.CONFLICT, "이미 동일한 랩실에 가입신청을 했습니다."),
    ALREADY_PROCESSED("APP_005", HttpStatus.BAD_REQUEST, "이미 처리된 신청입니다."),
    INVALID_STATUS("APP_006", HttpStatus.BAD_REQUEST, "유효하지 않은 신청 상태입니다. 신청 상태는 PENDING, APPROVED, REJECTED 중 하나여야 합니다."),
    SLOT_NOT_AVAILABLE("APP_009", HttpStatus.BAD_REQUEST, "면접 슬롯이 가득 찼거나 예약할 수 없는 상태입니다."),

    // ------------------------------------------------------------------------
    // 401 Unauthorized: 인증/권한 오류
    // ------------------------------------------------------------------------
    UNAUTHORIZED_CANCEL_ATTEMPT("APP_007", HttpStatus.UNAUTHORIZED, "본인의 신청만 취소할 수 있습니다."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    APPLICATION_NOT_FOUND("APP_008", HttpStatus.NOT_FOUND, "해당 가입신청을 찾을 수 없습니다."),
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;

    LabApplicationErrorCode(String code, HttpStatus status, String message) {
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