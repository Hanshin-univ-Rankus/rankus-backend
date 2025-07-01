package org.univ.rankus.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 공통 에러코드 정의: 클라이언트에 전달할 식별자 + 메시지 + 상태코드 포함
 */

public interface ErrorCode {
    String getCode();

    HttpStatus getStatus();

    String getMessage();
}