package org.univ.rankus.common.exception;


import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 오류 응답의 공통 포맷
 */
@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;      // HTTP 상태 코드
    private final String error;    // HTTP 에러 명칭
    private final String message;  // 사용자용 에러 메시지
    private final String path;     // 요청 URI

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}