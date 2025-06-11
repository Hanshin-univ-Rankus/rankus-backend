package org.univ.rankus.domain.model.user.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * User 도메인 유효성 검증 실패 시 던져지는 예외
 */
public class UserValidationException extends BaseCustomException {
    public UserValidationException(UserErrorCode errorCode) {
        super(errorCode);
    }
}