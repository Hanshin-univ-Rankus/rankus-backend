package org.univ.rankus.domain.model.lab.creation;

import org.univ.rankus.common.exception.BaseCustomException;

/**
 * LabCreationRequest 도메인 조회 실패(404 Not Found) 예외
 * - 반드시 LabCreationRequestErrorCode.LAB_CREATION_REQUEST_NOT_FOUND를 넘겨야 합니다.
 */
public class LabCreationRequestNotFoundException extends BaseCustomException {

    public LabCreationRequestNotFoundException() {
        super(LabCreationRequestErrorCode.LAB_CREATION_REQUEST_NOT_FOUND);
        // errorCode.getStatus()가 HttpStatus.NOT_FOUND인지 확인(optional)
    }
}