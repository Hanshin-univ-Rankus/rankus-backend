package org.univ.rankus.common.exception;

import org.springframework.http.HttpStatus;

public enum GlobalErrorCode implements ErrorCode {

    INVALID_INPUT("GLOBAL_001", HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR("GLOBAL_002", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    UNAUTHORIZED("GLOBAL_003", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN("GLOBAL_004", HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),
    NOT_FOUND("GLOBAL_005", HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED("GLOBAL_006", HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않는 HTTP 메소드입니다."),
    CONFLICT("GLOBAL_007", HttpStatus.CONFLICT, "요청이 충돌을 일으켰습니다."),
    UNSUPPORTED_MEDIA_TYPE("GLOBAL_008", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    GlobalErrorCode(String code, HttpStatus status, String message) {
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