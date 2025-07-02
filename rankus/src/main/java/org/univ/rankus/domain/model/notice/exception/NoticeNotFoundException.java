package org.univ.rankus.domain.model.notice.exception;

public class NoticeNotFoundException extends NoticeException {

    public NoticeNotFoundException() {
        super(NoticeErrorCode.NOTICE_NOT_FOUND);
    }

    public NoticeNotFoundException(Long noticeId) {
        super(NoticeErrorCode.NOTICE_NOT_FOUND);
    }
}