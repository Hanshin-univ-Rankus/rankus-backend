package org.univ.rankus.common.security.jwt;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import org.univ.rankus.common.exception.ErrorCode;

@Getter
public class JwtAuthenticationException extends AuthenticationException {

    private final ErrorCode errorCode;

    public JwtAuthenticationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public JwtAuthenticationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
