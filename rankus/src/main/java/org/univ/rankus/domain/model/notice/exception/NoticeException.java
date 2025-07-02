package org.univ.rankus.domain.model.notice.exception;

import org.univ.rankus.common.exception.BaseCustomException;

public class NoticeException extends BaseCustomException {

    public NoticeException(NoticeErrorCode errorCode) {
        super(errorCode);
    }
}