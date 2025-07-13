package org.univ.rankus.domain.model.attendance.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

import static org.springframework.http.HttpStatus.*;

public enum AttendanceErrorCode implements ErrorCode {
    // 400 Bad Request - 입력값 검증
    TITLE_REQUIRED("ATT_001", BAD_REQUEST, "출석 세션 제목은 필수입니다"),
    TITLE_TOO_LONG("ATT_002", BAD_REQUEST, "출석 세션 제목은 100자 이하여야 합니다"),
    QR_VALIDITY_REQUIRED("ATT_003", BAD_REQUEST, "QR 코드 유효시간은 필수입니다"),
    QR_VALIDITY_INVALID("ATT_004", BAD_REQUEST, "QR 코드 유효시간은 1~10분 사이여야 합니다"),
    SESSION_ALREADY_ENDED("ATT_005", BAD_REQUEST, "이미 종료된 출석 세션입니다"),
    SESSION_NOT_ACTIVE("ATT_006", BAD_REQUEST, "활성화된 출석 세션이 아닙니다"),
    ALREADY_CHECKED_IN("ATT_007", BAD_REQUEST, "이미 출석 체크되었습니다"),
    QR_TOKEN_EXPIRED("ATT_008", BAD_REQUEST, "QR 코드가 만료되었습니다"),
    QR_TOKEN_INVALID("ATT_009", BAD_REQUEST, "유효하지 않은 QR 코드입니다"),
    INVALID_STATUS_TRANSITION("ATT_010", BAD_REQUEST, "현재 상태에서는 변경할 수 없습니다"),
    STATUS_REQUIRED("ATT_019", BAD_REQUEST, "출석 상태는 필수입니다"),

    // 추가 입력값 검증 ErrorCode
    LAB_ID_REQUIRED("ATT_011", BAD_REQUEST, "랩실 ID는 필수이며 양수여야 합니다"),
    SESSION_ID_REQUIRED("ATT_012", BAD_REQUEST, "세션 ID는 필수이며 양수여야 합니다"),
    USER_ID_REQUIRED("ATT_013", BAD_REQUEST, "사용자 ID는 필수이며 양수여야 합니다"),
    CHECKED_AT_REQUIRED("ATT_014", BAD_REQUEST, "체크인 시간은 필수입니다"),
    ADJUSTED_BY_REQUIRED("ATT_015", BAD_REQUEST, "수정자 ID는 필수이며 양수여야 합니다"),
    ADJUSTMENT_REASON_REQUIRED("ATT_016", BAD_REQUEST, "수정 사유는 필수입니다"),
    ADJUSTMENT_REASON_TOO_LONG("ATT_017", BAD_REQUEST, "수정 사유는 200자 이하여야 합니다"),
    CREATOR_ID_REQUIRED("ATT_018", BAD_REQUEST, "생성자 ID는 필수이며 양수여야 합니다"),

    // 403 Forbidden - 권한 오류
    INSUFFICIENT_PERMISSION_FOR_ATTENDANCE("ATT_403", FORBIDDEN, "출석 관리 권한이 없습니다"),
    CANNOT_MODIFY_SESSION("ATT_403_MOD", FORBIDDEN, "출석 세션을 수정할 권한이 없습니다"),
    CANNOT_MANAGE_ATTENDANCE("ATT_403_MAN", FORBIDDEN, "출석을 관리할 권한이 없습니다"),

    // 404 Not Found - 조회 실패
    SESSION_NOT_FOUND("ATT_404", NOT_FOUND, "출석 세션을 찾을 수 없습니다"),
    RECORD_NOT_FOUND("ATT_404_REC", NOT_FOUND, "출석 기록을 찾을 수 없습니다"),

    // 409 Conflict - 충돌 오류
    DUPLICATE_SESSION("ATT_409", CONFLICT, "이미 진행 중인 출석 세션이 있습니다"),
    DUPLICATE_ATTENDANCE("ATT_409_DUP", CONFLICT, "이미 출석 체크된 사용자입니다"),

    // 422 Unprocessable Entity - 비즈니스 규칙 위반
    CANNOT_END_INACTIVE_SESSION("ATT_422", UNPROCESSABLE_ENTITY, "비활성화된 세션은 종료할 수 없습니다"),
    CANNOT_GENERATE_QR_FOR_INACTIVE_SESSION("ATT_422_QR", UNPROCESSABLE_ENTITY, "비활성화된 세션의 QR 코드를 생성할 수 없습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;

    AttendanceErrorCode(String code, HttpStatus status, String message) {
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