package org.univ.rankus.domain.model.user.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * 비밀번호 검증 전용 에러 코드(enum)
 * - 모두 400 Bad Request 상태
 */
public enum PasswordErrorCode implements ErrorCode {

    PASSWORD_REQUIRED   ("PASS_001", HttpStatus.BAD_REQUEST, "비밀번호는 필수 입력 항목입니다."),
    PASSWORD_TOO_SHORT  ("PASS_002", HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이어야 합니다."),
    PASSWORD_TOO_LONG   ("PASS_003", HttpStatus.BAD_REQUEST, "비밀번호는 255자 이하여야 합니다."),
    TOO_COMMON          ("PASS_004", HttpStatus.BAD_REQUEST, "너무 흔한 비밀번호입니다."),
    INVALID_FORMAT      ("PASS_005", HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다."),
    HASH_REQUIRED       ("PASS_006", HttpStatus.BAD_REQUEST, "비밀번호 해시가 필요합니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    PasswordErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override public String getCode()    { return code; }
    @Override public HttpStatus getStatus()  { return status; }
    @Override public String getMessage() { return message; }
}