package org.univ.rankus.domain.model.vote.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * Vote 도메인의 권한 오류를 나타내는 예외
 * - 반드시 VoteErrorCode(FORBIDDEN)만 넘겨야 합니다.
 */
public class VotePermissionException extends BaseCustomException {
    public VotePermissionException(VoteErrorCode errorCode) {
        super(errorCode);
    }
}