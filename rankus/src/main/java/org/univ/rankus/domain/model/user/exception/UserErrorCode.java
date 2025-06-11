package org.univ.rankus.domain.model.user.exception;

import org.univ.rankus.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * User 도메인 관련 예외 코드 모음
 * - 코드 형식: "USER_XXX"
 * - status, message를 한 곳에서 관리하여 일관된 응답을 생성할 수 있도록 함
 */
public enum UserErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 오류
    // ------------------------------------------------------------------------
    NAME_REQUIRED        ("USER_001", HttpStatus.BAD_REQUEST,    "이름은 필수입니다."),
    EMAIL_REQUIRED       ("USER_002", HttpStatus.BAD_REQUEST,    "이메일은 필수입니다."),
    EMAIL_INVALID        ("USER_003", HttpStatus.BAD_REQUEST,    "이메일 형식이 올바르지 않습니다."),
    NAME_TOO_LONG        ("USER_004", HttpStatus.BAD_REQUEST,    "이름은 10자 이하여야 합니다."),
    LAB_REQUIRED        ("USER_005", HttpStatus.BAD_REQUEST,    "소속 랩은 필수입니다."),
    ROLE_REQUIRED       ("USER_006", HttpStatus.BAD_REQUEST,    "권한(role)은 필수입니다."),
    PASSWORD_REQUIRED    ("USER_007", HttpStatus.BAD_REQUEST,    "비밀번호는 필수입니다."),

    // ------------------------------------------------------------------------
    // 409 Conflict: 데이터 중복/충돌
    // ------------------------------------------------------------------------
    EMAIL_DUPLICATED     ("USER_008", HttpStatus.CONFLICT,       "이미 사용 중인 이메일입니다."),

    // ------------------------------------------------------------------------
    // 401 Unauthorized: 인증 오류
    // ------------------------------------------------------------------------
    INVALID_CREDENTIALS  ("USER_009", HttpStatus.UNAUTHORIZED,   "이메일 또는 비밀번호가 일치하지 않습니다."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    USER_NOT_FOUND       ("USER_010", HttpStatus.NOT_FOUND,      "해당 사용자를 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    UserErrorCode(String code, HttpStatus status, String message) {
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