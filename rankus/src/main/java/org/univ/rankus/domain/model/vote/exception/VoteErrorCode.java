package org.univ.rankus.domain.model.vote.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * Vote 도메인 관련 예외 코드 모음
 * - 코드 형식: "VOTE_XXX"
 * - 필수 입력 검증, 조회 실패, 상태 오류 등을 관리
 */
public enum VoteErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 오류
    // ------------------------------------------------------------------------
    VOTE_TITLE_REQUIRED("VOTE_001", HttpStatus.BAD_REQUEST, "투표 제목은 필수입니다."),
    VOTE_TITLE_TOO_LONG("VOTE_002", HttpStatus.BAD_REQUEST, "투표 제목이 너무 깁니다. 최대 200자까지 가능합니다."),
    VOTE_DESCRIPTION_TOO_LONG("VOTE_003", HttpStatus.BAD_REQUEST, "투표 설명이 너무 깁니다. 최대 1000자까지 가능합니다."),
    VOTE_DEADLINE_REQUIRED("VOTE_004", HttpStatus.BAD_REQUEST, "투표 마감일은 필수입니다."),
    VOTE_DEADLINE_PAST("VOTE_005", HttpStatus.BAD_REQUEST, "투표 마감일은 현재 시간보다 미래여야 합니다."),
    VOTE_OPTIONS_REQUIRED("VOTE_006", HttpStatus.BAD_REQUEST, "투표 선택지는 최소 2개 이상이어야 합니다."),
    VOTE_OPTIONS_TOO_MANY("VOTE_007", HttpStatus.BAD_REQUEST, "투표 선택지는 최대 5개까지 가능합니다."),
    VOTE_OPTION_TEXT_REQUIRED("VOTE_008", HttpStatus.BAD_REQUEST, "투표 선택지 내용은 필수입니다."),
    VOTE_OPTION_TEXT_TOO_LONG("VOTE_009", HttpStatus.BAD_REQUEST, "투표 선택지 내용이 너무 깁니다. 최대 100자까지 가능합니다."),

    // ------------------------------------------------------------------------
    // 403 Forbidden: 권한 오류
    // ------------------------------------------------------------------------
    VOTE_CREATE_PERMISSION_DENIED("VOTE_011", HttpStatus.FORBIDDEN, "투표 생성 권한이 없습니다."),
    VOTE_UPDATE_PERMISSION_DENIED("VOTE_012", HttpStatus.FORBIDDEN, "투표 수정 권한이 없습니다."),
    VOTE_DELETE_PERMISSION_DENIED("VOTE_013", HttpStatus.FORBIDDEN, "투표 삭제 권한이 없습니다."),
    VOTE_PARTICIPATION_PERMISSION_DENIED("VOTE_014", HttpStatus.FORBIDDEN, "투표 참여 권한이 없습니다."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    VOTE_NOT_FOUND("VOTE_015", HttpStatus.NOT_FOUND, "해당 투표를 찾을 수 없습니다."),
    VOTE_OPTION_NOT_FOUND("VOTE_016", HttpStatus.NOT_FOUND, "해당 투표 선택지를 찾을 수 없습니다."),

    // ------------------------------------------------------------------------
    // 409 Conflict: 상태 오류
    // ------------------------------------------------------------------------
    VOTE_ALREADY_CLOSED("VOTE_017", HttpStatus.CONFLICT, "이미 종료된 투표입니다."),
    VOTE_ALREADY_CANCELED("VOTE_018", HttpStatus.CONFLICT, "이미 취소된 투표입니다."),
    VOTE_ALREADY_PARTICIPATED("VOTE_019", HttpStatus.CONFLICT, "이미 참여한 투표입니다."),
    VOTE_INVALID_STATUS_TRANSITION("VOTE_020", HttpStatus.CONFLICT, "유효하지 않은 상태 전환입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    VoteErrorCode(String code, HttpStatus status, String message) {
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