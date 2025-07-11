package org.univ.rankus.domain.model.ranking.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * Ranking 도메인 유효성 검증 실패 시 던져지는 예외
 */
public class RankingValidationException extends BaseCustomException {
    public RankingValidationException(RankingErrorCode errorCode) {
        super(errorCode);
    }
}