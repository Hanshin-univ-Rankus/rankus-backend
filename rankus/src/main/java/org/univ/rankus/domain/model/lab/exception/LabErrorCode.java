package org.univ.rankus.domain.model.lab.exception;

import org.univ.rankus.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Lab 도메인 관련 예외 코드 모음
 * - 코드 형식: "LAB_XXX"
 * - 필수 입력 검증, 조회 실패, 상태 오류 등을 관리
 */
public enum LabErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 오류
    // ------------------------------------------------------------------------
    LAB_NAME_REQUIRED      ("LAB_001", HttpStatus.BAD_REQUEST,    "랩실 이름은 필수입니다."),
    LAB_CATEGORY_REQUIRED  ("LAB_002", HttpStatus.BAD_REQUEST,    "랩실 카테고리는 필수입니다."),
    LAB_DESCRIPTION_TOO_LONG("LAB_003", HttpStatus.BAD_REQUEST,   "랩실 설명이 너무 깁니다. 최대 255자까지 가능합니다."),
    LAB_RANKING_INVALID    ("LAB_004", HttpStatus.BAD_REQUEST,    "랩실 랭킹은 0 이상의 정수여야 합니다."),
    LAB_PROFESSOR_NAME_TOO_LONG("LAB_005", HttpStatus.BAD_REQUEST, "랩실 교수 이름이 너무 깁니다. 최대 10자까지 가능합니다."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    LAB_NOT_FOUND          ("LAB_006", HttpStatus.NOT_FOUND,      "해당 랩실을 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    LabErrorCode(String code, HttpStatus status, String message) {
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