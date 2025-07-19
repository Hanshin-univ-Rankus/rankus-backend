package org.univ.rankus.domain.model.vote.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * Vote 도메인 조회 실패(404 Not Found) 예외
 * - 반드시 VoteErrorCode(NOT_FOUND)만 넘겨야 합니다.
 */
public class VoteNotFoundException extends BaseCustomException {
    public VoteNotFoundException() {
        super(VoteErrorCode.VOTE_NOT_FOUND);
    }

    public VoteNotFoundException(VoteErrorCode errorCode) {
        super(errorCode);
    }
}