package org.univ.rankus.domain.model.notice.exception;

public class NoticeValidationException extends NoticeException {

    public NoticeValidationException(NoticeErrorCode errorCode) {
        super(errorCode);
    }
}