package org.univ.rankus.domain.model.user.exception;

import org.univ.rankus.common.exception.BaseCustomException;

public class PasswordValidationException extends BaseCustomException {

    public PasswordValidationException(PasswordErrorCode errorCode) {
        super(errorCode);
        // errorCode.getStatus()가 HttpStatus.BAD_REQUEST인지 확인(optional)
    }
}
