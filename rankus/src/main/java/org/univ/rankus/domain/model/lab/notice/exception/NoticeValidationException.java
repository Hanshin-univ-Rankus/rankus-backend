package org.univ.rankus.domain.model.lab.notice.exception;

public class NoticeValidationException extends NoticeException {

    public NoticeValidationException(NoticeErrorCode errorCode) {
        super(errorCode);
    }
}