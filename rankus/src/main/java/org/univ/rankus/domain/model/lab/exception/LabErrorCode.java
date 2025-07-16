package org.univ.rankus.domain.model.lab.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * Lab 도메인 관련 예외 코드 모음
 * - 코드 형식: "LAB_XXX"
 * - 필수 입력 검증, 조회 실패, 상태 오류 등을 관리
 */
public enum LabErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 오류
    // ------------------------------------------------------------------------
    LAB_NAME_REQUIRED("LAB_001", HttpStatus.BAD_REQUEST, "랩실 이름은 필수입니다."),
    LAB_CATEGORY_REQUIRED("LAB_002", HttpStatus.BAD_REQUEST, "랩실 카테고리는 필수입니다."),
    LAB_DESCRIPTION_TOO_LONG("LAB_003", HttpStatus.BAD_REQUEST, "랩실 설명이 너무 깁니다. 최대 255자까지 가능합니다."),
    LAB_RANKING_INVALID("LAB_004", HttpStatus.BAD_REQUEST, "랩실 랭킹은 0 이상의 정수여야 합니다."),
    LAB_PROFESSOR_NAME_TOO_LONG("LAB_005", HttpStatus.BAD_REQUEST, "랩실 교수 이름이 너무 깁니다. 최대 10자까지 가능합니다."),

    // ------------------------------------------------------------------------
    // 403 Forbidden: 권한 오류
    // ------------------------------------------------------------------------
    LAB_MEMBER_VIEW_PERMISSION_DENIED("LAB_007", HttpStatus.FORBIDDEN, "랩실 멤버 조회 권한이 없습니다."),
    LAB_MEMBER_MANAGE_PERMISSION_DENIED("LAB_008", HttpStatus.FORBIDDEN, "랩실 멤버 관리 권한이 없습니다."),
    LAB_LEADERSHIP_TRANSFER_PERMISSION_DENIED("LAB_010", HttpStatus.FORBIDDEN, "랩실 리더십 위임 권한이 없습니다."),
    LAB_ATTENDANCE_MANAGE_PERMISSION_DENIED("LAB_012", HttpStatus.FORBIDDEN, "출석 일괄 수정 권한이 없습니다."),
    LAB_STATISTICS_VIEW_PERMISSION_DENIED("LAB_013", HttpStatus.FORBIDDEN, "랩실 통계 조회 권한이 없습니다."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    LAB_NOT_FOUND("LAB_006", HttpStatus.NOT_FOUND, "해당 랩실을 찾을 수 없습니다."),

    // ------------------------------------------------------------------------
    // 409 Conflict: 상태 오류
    // ------------------------------------------------------------------------
    LAB_MEMBER_NOT_FOUND("LAB_009", HttpStatus.BAD_REQUEST, "해당 멤버는 이 랩실에 속하지 않습니다."),
    LAB_ROLE_CHANGE_NOT_ALLOWED("LAB_011", HttpStatus.CONFLICT, "현재 역할에서는 해당 역할로 변경할 수 없습니다.");

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