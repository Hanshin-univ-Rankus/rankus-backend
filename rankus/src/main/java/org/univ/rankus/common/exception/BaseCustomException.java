package org.univ.rankus.common.exception;

import lombok.Getter;

@Getter
public abstract class BaseCustomException extends RuntimeException {
    private final ErrorCode errorCode;

    protected BaseCustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 기본 메시지는 ErrorCode에서 제공
        this.errorCode = errorCode;
    }

}