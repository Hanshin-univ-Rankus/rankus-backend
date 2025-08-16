package org.univ.rankus.common.security.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements ErrorCode {

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "J001", "토큰이 만료되었습니다."),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "J002", "토큰 서명이 유효하지 않습니다."),
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "J003", "토큰 형식이 올바르지 않습니다."),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "J004", "지원하지 않는 토큰입니다."),
    TOKEN_ILLEGAL_ARGUMENT(HttpStatus.UNAUTHORIZED, "J005", "토큰 인자값이 올바르지 않습니다."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "J006", "로그아웃 처리된 토큰입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
