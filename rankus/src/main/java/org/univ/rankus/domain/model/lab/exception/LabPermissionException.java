package org.univ.rankus.domain.model.lab.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * Lab 도메인의 권한 오류를 나타내는 예외
 * - 반드시 LabErrorCode(FORBIDDEN)만 넘겨야 합니다.
 */
public class LabPermissionException extends BaseCustomException {

    public LabPermissionException(LabErrorCode errorCode) {
        super(errorCode);
        // errorCode.getStatus()가 HttpStatus.FORBIDDEN인지 확인(optional)
    }
}