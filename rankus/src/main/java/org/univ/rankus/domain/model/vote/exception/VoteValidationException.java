package org.univ.rankus.domain.model.vote.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * Vote 도메인 유효성 검증 실패 시 던져지는 예외
 */
public class VoteValidationException extends BaseCustomException {
    public VoteValidationException(VoteErrorCode errorCode) {
        super(errorCode);
    }
}