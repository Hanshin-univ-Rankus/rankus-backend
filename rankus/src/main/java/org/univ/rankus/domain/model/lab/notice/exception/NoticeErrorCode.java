package org.univ.rankus.domain.model.lab.notice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum NoticeErrorCode implements ErrorCode {

    // 400 Bad Request
    TITLE_REQUIRED("LNT_001", HttpStatus.BAD_REQUEST, "공지사항 제목은 필수입니다"),
    TITLE_TOO_LONG("LNT_002", HttpStatus.BAD_REQUEST, "공지사항 제목은 100자를 초과할 수 없습니다"),
    CONTENT_REQUIRED("LNT_003", HttpStatus.BAD_REQUEST, "공지사항 내용은 필수입니다"),
    CONTENT_TOO_LONG("LNT_004", HttpStatus.BAD_REQUEST, "공지사항 내용은 2000자를 초과할 수 없습니다"),
    AUTHOR_REQUIRED("LNT_005", HttpStatus.BAD_REQUEST, "공지사항 작성자는 필수입니다"),
    LAB_REQUIRED("LNT_006", HttpStatus.BAD_REQUEST, "공지사항이 속할 랩실은 필수입니다"),
    TYPE_REQUIRED("LNT_007", HttpStatus.BAD_REQUEST, "공지사항 타입은 필수입니다"),

    // 403 Forbidden
    INSUFFICIENT_PERMISSION_FOR_NOTICE("LNT_403", HttpStatus.FORBIDDEN, "공지사항에 대한 권한이 없습니다"),
    NOT_NOTICE_AUTHOR("LNT_403_AUTH", HttpStatus.FORBIDDEN, "본인이 작성한 공지사항이 아닙니다"),

    // 404 Not Found
    NOTICE_NOT_FOUND("LNT_404", HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}