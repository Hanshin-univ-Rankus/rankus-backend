package org.univ.rankus.domain.model.lab.exception;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * LabImage(랩실 이미지) 도메인 조회 실패(404 Not Found) 예외
 * - 반드시 LabImageErrorCode.IMAGE_NOT_FOUND를 넘겨야 합니다.
 */
public class LabImageNotFoundException extends BaseCustomException {

    public LabImageNotFoundException(LabImageErrorCode errorCode) {
        super(errorCode);
        // errorCode.getStatus()가 HttpStatus.NOT_FOUND인지 확인(optional)
    }
}