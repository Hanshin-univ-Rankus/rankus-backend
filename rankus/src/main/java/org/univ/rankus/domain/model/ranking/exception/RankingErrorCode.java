package org.univ.rankus.domain.model.ranking.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * Ranking 도메인 관련 예외 코드 모음
 * - 코드 형식: "RANKING_XXX"
 * - status, message를 한 곳에서 관리하여 일관된 응답을 생성할 수 있도록 함
 */
public enum RankingErrorCode implements ErrorCode {
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 오류
    // ------------------------------------------------------------------------
    USER_REQUIRED("RANKING_001", HttpStatus.BAD_REQUEST, "사용자는 필수입니다."),
    LAB_REQUIRED("RANKING_002", HttpStatus.BAD_REQUEST, "랩실은 필수입니다."),
    CATEGORY_REQUIRED("RANKING_003", HttpStatus.BAD_REQUEST, "카테고리는 필수입니다."),
    ACHIEVEMENT_DESCRIPTION_REQUIRED("RANKING_004", HttpStatus.BAD_REQUEST, "성과 내용은 필수입니다."),
    ACHIEVEMENT_DESCRIPTION_TOO_LONG("RANKING_005", HttpStatus.BAD_REQUEST, "성과 내용은 500자 이하여야 합니다."),
    ACHIEVEMENT_DATE_REQUIRED("RANKING_006", HttpStatus.BAD_REQUEST, "취득일자는 필수입니다."),
    ACHIEVEMENT_DATE_FUTURE("RANKING_007", HttpStatus.BAD_REQUEST, "취득일자는 미래일 수 없습니다."),
    PROOF_FILE_URL_REQUIRED("RANKING_008", HttpStatus.BAD_REQUEST, "증빙서류는 필수입니다."),
    APPLICATION_REASON_TOO_LONG("RANKING_009", HttpStatus.BAD_REQUEST, "신청 사유는 200자 이하여야 합니다."),
    RELATED_LINK_INVALID("RANKING_010", HttpStatus.BAD_REQUEST, "관련 링크 형식이 올바르지 않습니다."),
    VISIBILITY_REQUIRED("RANKING_011", HttpStatus.BAD_REQUEST, "공개 범위는 필수입니다."),

    // ------------------------------------------------------------------------
    // 403 Forbidden: 권한 오류
    // ------------------------------------------------------------------------
    INSUFFICIENT_PERMISSION("RANKING_012", HttpStatus.FORBIDDEN, "점수 승인 권한이 없습니다."),
    NOT_OWNER("RANKING_013", HttpStatus.FORBIDDEN, "본인이 신청한 점수만 수정할 수 있습니다."),
    CANNOT_APPROVE_OWN_SUBMISSION("RANKING_014", HttpStatus.FORBIDDEN, "본인이 신청한 점수는 승인할 수 없습니다."),

    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    SUBMISSION_NOT_FOUND("RANKING_015", HttpStatus.NOT_FOUND, "해당 점수 신청을 찾을 수 없습니다."),

    // ------------------------------------------------------------------------
    // 409 Conflict: 비즈니스 로직 충돌
    // ------------------------------------------------------------------------
    INVALID_STATUS_TRANSITION("RANKING_016", HttpStatus.CONFLICT, "점수 상태 변경이 불가능합니다."),
    ALREADY_PROCESSED("RANKING_017", HttpStatus.CONFLICT, "이미 처리된 점수 신청입니다."),
    SUBMISSION_EXPIRED("RANKING_018", HttpStatus.CONFLICT, "신청 기간이 만료되었습니다."),
    CORRECTION_LIMIT_EXCEEDED("RANKING_019", HttpStatus.CONFLICT, "정정 가능 횟수를 초과했습니다."),
    CORRECTION_NOT_ALLOWED("RANKING_020", HttpStatus.CONFLICT, "정정이 허용되지 않는 상태입니다."),

    // ------------------------------------------------------------------------
    // 422 Unprocessable Entity: 파일 업로드 오류
    // ------------------------------------------------------------------------
    FILE_SIZE_EXCEEDED("RANKING_021", HttpStatus.UNPROCESSABLE_ENTITY, "파일 크기는 10MB 이하여야 합니다."),
    UNSUPPORTED_FILE_TYPE("RANKING_022", HttpStatus.UNPROCESSABLE_ENTITY, "지원하지 않는 파일 형식입니다."),
    FILE_UPLOAD_FAILED("RANKING_023", HttpStatus.UNPROCESSABLE_ENTITY, "파일 업로드에 실패했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    RankingErrorCode(String code, HttpStatus status, String message) {
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