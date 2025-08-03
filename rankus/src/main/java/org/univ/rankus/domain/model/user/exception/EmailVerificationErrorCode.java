package org.univ.rankus.domain.model.user.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * 이메일 인증 관련 예외 코드 모음
 * - 코드 형식: "EMAIL_XXX"
 * - @hs.ac.kr 도메인 검증 및 인증번호 처리 관련 오류
 */
public enum EmailVerificationErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 오류
    // ------------------------------------------------------------------------
    EMAIL_DOMAIN_INVALID("EMAIL_001", HttpStatus.BAD_REQUEST, "@hs.ac.kr 도메인 이메일만 허용됩니다."),
    VERIFICATION_CODE_REQUIRED("EMAIL_002", HttpStatus.BAD_REQUEST, "인증번호는 필수입니다."),
    VERIFICATION_CODE_INVALID("EMAIL_003", HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    VERIFICATION_CODE_EXPIRED("EMAIL_004", HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    EMAIL_ALREADY_VERIFIED("EMAIL_005", HttpStatus.BAD_REQUEST, "이미 인증된 이메일입니다."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    VERIFICATION_NOT_FOUND("EMAIL_006", HttpStatus.NOT_FOUND, "해당 이메일의 인증 정보를 찾을 수 없습니다."),
    USER_NOT_FOUND_FOR_VERIFICATION("EMAIL_009", HttpStatus.NOT_FOUND, "인증할 사용자를 찾을 수 없습니다."),

    // ------------------------------------------------------------------------
    // 429 Too Many Requests: 요청 제한
    // ------------------------------------------------------------------------
    VERIFICATION_SEND_LIMIT_EXCEEDED("EMAIL_007", HttpStatus.TOO_MANY_REQUESTS, "인증번호 발송 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),

    // ------------------------------------------------------------------------
    // 500 Internal Server Error: 시스템 오류
    // ------------------------------------------------------------------------
    EMAIL_SEND_FAILED("EMAIL_008", HttpStatus.INTERNAL_SERVER_ERROR, "인증번호 이메일 발송에 실패했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    EmailVerificationErrorCode(String code, HttpStatus status, String message) {
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