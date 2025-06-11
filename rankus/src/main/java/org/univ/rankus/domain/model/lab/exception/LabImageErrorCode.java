package org.univ.rankus.domain.model.lab.exception;

import org.univ.rankus.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * LabImage(랩실 이미지) 도메인 관련 예외 코드 모음
 * - 코드 형식: "IMG_XXX"
 * - 대표 이미지 중복, 조회 실패 등을 관리
 */
public enum LabImageErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 / 비즈니스 상태 오류
    // ------------------------------------------------------------------------
    IMAGE_URL_REQUIRED          ("IMG_001", HttpStatus.BAD_REQUEST,    "이미지 URL은 필수입니다."),
    IMAGE_URL_INVALID           ("IMG_002", HttpStatus.BAD_REQUEST,    "이미지 URL이 유효하지 않습니다."),
    INVALID_IMAGE_TYPE          ("IMG_003", HttpStatus.BAD_REQUEST,    "유효하지 않은 이미지 타입입니다."),
    DUPLICATE_REPRESENTATIVE    ("IMG_004", HttpStatus.CONFLICT,       "이미 대표 이미지가 존재합니다."),
    IMAGE_NOT_BELONG_TO_LAB   ("IMG_005", HttpStatus.BAD_REQUEST, "해당 랩실에 속하지 않는 이미지입니다."),
    // (예: 동일한 랩실에 같은 타입 이미지 중복 허용되지 않을 때)


    IMAGE_DELETE_FAILED        ("IMG_006", HttpStatus.BAD_REQUEST, "이미지 삭제에 실패했습니다. 권한을 확인하세요."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    IMAGE_NOT_FOUND             ("IMG_005", HttpStatus.NOT_FOUND,      "해당 랩실 이미지를 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    LabImageErrorCode(String code, HttpStatus status, String message) {
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