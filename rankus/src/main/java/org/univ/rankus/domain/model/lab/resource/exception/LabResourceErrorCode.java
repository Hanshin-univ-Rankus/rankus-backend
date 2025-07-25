package org.univ.rankus.domain.model.lab.resource.exception;

import org.springframework.http.HttpStatus;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * 랩실 자료 관련 에러 코드
 */
public enum LabResourceErrorCode implements ErrorCode {

    // 자료 관련 에러 (400 Bad Request)
    RESOURCE_NOT_FOUND("RESOURCE_001", HttpStatus.NOT_FOUND, "자료를 찾을 수 없습니다"),
    RESOURCE_TITLE_REQUIRED("RESOURCE_002", HttpStatus.BAD_REQUEST, "자료 제목은 필수입니다"),
    RESOURCE_TITLE_TOO_LONG("RESOURCE_003", HttpStatus.BAD_REQUEST, "자료 제목은 100자 이하여야 합니다"),
    RESOURCE_DESCRIPTION_TOO_LONG("RESOURCE_004", HttpStatus.BAD_REQUEST, "자료 설명은 500자 이하여야 합니다"),
    RESOURCE_CATEGORY_REQUIRED("RESOURCE_005", HttpStatus.BAD_REQUEST, "자료 카테고리는 필수입니다"),
    RESOURCE_FILE_REQUIRED("RESOURCE_006", HttpStatus.BAD_REQUEST, "자료 파일은 필수입니다"),
    RESOURCE_FILE_SIZE_EXCEEDED("RESOURCE_007", HttpStatus.BAD_REQUEST, "파일 크기가 제한을 초과했습니다 (최대 50MB)"),
    RESOURCE_FILE_TYPE_NOT_ALLOWED("RESOURCE_008", HttpStatus.BAD_REQUEST, "허용되지 않는 파일 형식입니다"),
    RESOURCE_LAB_REQUIRED("RESOURCE_009", HttpStatus.BAD_REQUEST, "랩실은 필수입니다"),
    RESOURCE_UPLOADER_REQUIRED("RESOURCE_010", HttpStatus.BAD_REQUEST, "업로더는 필수입니다"),

    // 권한 관련 에러 (403 Forbidden)
    RESOURCE_ACCESS_DENIED("RESOURCE_011", HttpStatus.FORBIDDEN, "자료에 접근할 권한이 없습니다"),
    RESOURCE_MODIFICATION_DENIED("RESOURCE_012", HttpStatus.FORBIDDEN, "자료를 수정할 권한이 없습니다"),
    RESOURCE_DELETION_DENIED("RESOURCE_013", HttpStatus.FORBIDDEN, "자료를 삭제할 권한이 없습니다"),
    RESOURCE_DOWNLOAD_DENIED("RESOURCE_014", HttpStatus.FORBIDDEN, "자료를 다운로드할 권한이 없습니다"),

    // 파일 관련 에러 (500 Internal Server Error)
    RESOURCE_FILE_UPLOAD_FAILED("RESOURCE_015", HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다"),
    RESOURCE_FILE_DELETE_FAILED("RESOURCE_016", HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다"),
    RESOURCE_FILE_NOT_FOUND("RESOURCE_017", HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;

    LabResourceErrorCode(String code, HttpStatus status, String message) {
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