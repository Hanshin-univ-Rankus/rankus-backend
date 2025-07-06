package org.univ.rankus.domain.model.interview.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * Interview 도메인 조회 실패 예외 클래스
 * - 면접이나 면접 슬롯을 찾을 수 없을 때 발생
 * - 404 Not Found 상태 코드와 연결
 */
public class InterviewNotFoundException extends BaseCustomException {

    public InterviewNotFoundException(InterviewErrorCode errorCode) {
        super(errorCode);
    }
}