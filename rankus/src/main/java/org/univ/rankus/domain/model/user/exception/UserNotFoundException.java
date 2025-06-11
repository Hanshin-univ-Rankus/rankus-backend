package org.univ.rankus.domain.model.user.exception;


import org.univ.rankus.common.exception.BaseCustomException;

/**
 * User 도메인 조회 실패(404 Not Found) 예외
 * - 반드시 UserErrorCode.USER_NOT_FOUND를 넘겨야 합니다.
 */
public class UserNotFoundException extends BaseCustomException {

    public UserNotFoundException(UserErrorCode errorCode) {
        super(errorCode);
        // errorCode.getStatus()가 HttpStatus.NOT_FOUND인지 확인(optional)
    }
}