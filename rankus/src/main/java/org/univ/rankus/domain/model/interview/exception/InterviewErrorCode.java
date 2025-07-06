package org.univ.rankus.domain.model.interview.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * Interview 도메인 관련 예외 코드 모음
 * - 코드 형식: "INT_XXX"
 * - 면접 설정, 슬롯 관리, 상태 변경 등 관련 예외 관리
 */
@Getter
@AllArgsConstructor
public enum InterviewErrorCode implements ErrorCode {
    
    // ------------------------------------------------------------------------
    // 400 Bad Request: 입력값 검증 / 비즈니스 상태 오류
    // ------------------------------------------------------------------------
    DATE_REQUIRED("INT_001", HttpStatus.BAD_REQUEST, "면접 시작일과 종료일은 필수입니다."),
    INVALID_DATE_RANGE("INT_002", HttpStatus.BAD_REQUEST, "면접 시작일은 종료일보다 이전이어야 합니다."),
    PAST_DATE_NOT_ALLOWED("INT_003", HttpStatus.BAD_REQUEST, "과거 날짜로 면접을 설정할 수 없습니다."),
    INVALID_DURATION("INT_004", HttpStatus.BAD_REQUEST, "면접 소요 시간은 1분 이상이어야 합니다."),
    DURATION_TOO_LONG("INT_005", HttpStatus.BAD_REQUEST, "면접 소요 시간은 180분을 초과할 수 없습니다."),
    INVALID_MAX_APPLICANTS("INT_006", HttpStatus.BAD_REQUEST, "최대 지원자 수는 1명 이상이어야 합니다."),
    TOO_MANY_APPLICANTS("INT_007", HttpStatus.BAD_REQUEST, "슬롯당 최대 지원자 수는 10명을 초과할 수 없습니다."),
    
    // 면접 상태 관련
    ALREADY_ACTIVATED("INT_008", HttpStatus.BAD_REQUEST, "이미 활성화된 면접입니다."),
    ALREADY_DEACTIVATED("INT_009", HttpStatus.BAD_REQUEST, "이미 비활성화된 면접입니다."),
    ALREADY_CLOSED("INT_010", HttpStatus.BAD_REQUEST, "이미 종료된 면접입니다."),
    CANNOT_ACTIVATE_CLOSED("INT_011", HttpStatus.BAD_REQUEST, "종료된 면접은 활성화할 수 없습니다."),
    CANNOT_DEACTIVATE_CLOSED("INT_012", HttpStatus.BAD_REQUEST, "종료된 면접은 비활성화할 수 없습니다."),
    EXPIRED_INTERVIEW_PERIOD("INT_013", HttpStatus.BAD_REQUEST, "면접 기간이 만료되었습니다."),
    
    // 슬롯 관련
    SLOT_REQUIRED("INT_014", HttpStatus.BAD_REQUEST, "면접 슬롯은 필수입니다."),
    TIME_REQUIRED("INT_015", HttpStatus.BAD_REQUEST, "면접 시작 시간과 종료 시간은 필수입니다."),
    INVALID_TIME_RANGE("INT_016", HttpStatus.BAD_REQUEST, "면접 시작 시간은 종료 시간보다 이전이어야 합니다."),
    PAST_TIME_NOT_ALLOWED("INT_017", HttpStatus.BAD_REQUEST, "과거 시간으로 면접 슬롯을 설정할 수 없습니다."),
    SLOT_OUTSIDE_INTERVIEW_PERIOD("INT_018", HttpStatus.BAD_REQUEST, "면접 슬롯은 면접 기간 내에 있어야 합니다."),
    
    // 슬롯 상태 관련
    SLOT_FULL("INT_019", HttpStatus.BAD_REQUEST, "면접 슬롯이 가득 찼습니다."),
    SLOT_CANCELLED("INT_020", HttpStatus.BAD_REQUEST, "취소된 면접 슬롯입니다."),
    SLOT_ALREADY_CANCELLED("INT_021", HttpStatus.BAD_REQUEST, "이미 취소된 면접 슬롯입니다."),
    SLOT_NOT_CANCELLED("INT_022", HttpStatus.BAD_REQUEST, "취소되지 않은 면접 슬롯입니다."),
    NO_RESERVATION_TO_CANCEL("INT_023", HttpStatus.BAD_REQUEST, "취소할 예약이 없습니다."),
    CANNOT_CANCEL_SLOT_WITH_APPLICANTS("INT_024", HttpStatus.BAD_REQUEST, "지원자가 있는 슬롯은 취소할 수 없습니다."),
    
    // 지원 관련
    INTERVIEW_NOT_ACTIVE("INT_025", HttpStatus.BAD_REQUEST, "활성화되지 않은 면접입니다."),
    INTERVIEW_CLOSED("INT_026", HttpStatus.BAD_REQUEST, "종료된 면접입니다."),
    INTERVIEW_NOT_AVAILABLE("INT_027", HttpStatus.BAD_REQUEST, "지원 불가능한 면접입니다."),
    
    // ------------------------------------------------------------------------
    // 401 Unauthorized: 인증/권한 오류
    // ------------------------------------------------------------------------
    UNAUTHORIZED_INTERVIEW_ACCESS("INT_028", HttpStatus.UNAUTHORIZED, "면접 관리 권한이 없습니다."),
    UNAUTHORIZED_SLOT_ACCESS("INT_029", HttpStatus.UNAUTHORIZED, "면접 슬롯 관리 권한이 없습니다."),
    
    // ------------------------------------------------------------------------
    // 404 Not Found: 조회 실패
    // ------------------------------------------------------------------------
    INTERVIEW_NOT_FOUND("INT_030", HttpStatus.NOT_FOUND, "해당 면접을 찾을 수 없습니다."),
    SLOT_NOT_FOUND("INT_031", HttpStatus.NOT_FOUND, "해당 면접 슬롯을 찾을 수 없습니다."),
    INTERVIEW_REQUIRED("INT_032", HttpStatus.BAD_REQUEST, "면접 정보는 필수입니다."),
    
    // ------------------------------------------------------------------------
    // 409 Conflict: 중복/충돌 오류
    // ------------------------------------------------------------------------
    DUPLICATE_INTERVIEW("INT_033", HttpStatus.CONFLICT, "해당 랩실에 이미 활성화된 면접이 있습니다."),
    SLOT_TIME_CONFLICT("INT_034", HttpStatus.CONFLICT, "중복된 시간대의 면접 슬롯이 있습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}