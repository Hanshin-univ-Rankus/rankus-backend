package org.univ.rankus.domain.model.lab.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * LabCreationRequest 도메인 관련 예외 코드 모음
 * - 코드 형식: "LCR_XXX"
 * - 랩실 생성 신청 관련 검증 오류, 조회 실패, 상태 전이 오류 등을 관리
 */
public enum LabCreationRequestErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 오류
    // ------------------------------------------------------------------------
    REQUESTED_LAB_NAME_REQUIRED("LCR_001", HttpStatus.BAD_REQUEST, "신청할 랩실 이름은 필수입니다."),
    REQUESTED_LAB_NAME_TOO_LONG("LCR_002", HttpStatus.BAD_REQUEST, "신청할 랩실 이름이 너무 깁니다. 최대 10자까지 가능합니다."),
    REQUESTED_CATEGORY_REQUIRED("LCR_003", HttpStatus.BAD_REQUEST, "신청할 랩실 카테고리는 필수입니다."),
    REQUESTED_DESCRIPTION_TOO_LONG("LCR_004", HttpStatus.BAD_REQUEST, "신청할 랩실 설명이 너무 깁니다. 최대 255자까지 가능합니다."),
    REJECTION_REASON_TOO_LONG("LCR_005", HttpStatus.BAD_REQUEST, "거절 사유가 너무 깁니다. 최대 500자까지 가능합니다."),
    DUPLICATE_LAB_NAME_REQUEST("LCR_006", HttpStatus.CONFLICT, "이미 동일한 이름의 랩실 생성 신청이 존재합니다."),
    CANNOT_CHANGE_STATUS_AFTER_DECISION("LCR_007", HttpStatus.UNPROCESSABLE_ENTITY, "이미 처리된 신청은 상태를 변경할 수 없습니다."),
    INSUFFICIENT_PERMISSION_FOR_APPROVAL("LCR_008", HttpStatus.FORBIDDEN, "랩실 생성 신청을 승인할 권한이 없습니다."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    LAB_CREATION_REQUEST_NOT_FOUND("LCR_009", HttpStatus.NOT_FOUND, "해당 랩실 생성 신청을 찾을 수 없습니다."),

    // ------------------------------------------------------------------------
    // 400 Bad Request: 신청자 관련 검증 오류
    // ------------------------------------------------------------------------
    REQUESTER_REQUIRED("LCR_010", HttpStatus.BAD_REQUEST, "신청자는 필수입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    LabCreationRequestErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}