package org.univ.rankus.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공통 에러 응답 DTO
 */
@Getter @Setter
public class ErrorResponse {

    private String code;               // ErrorCode.getCode()
    private String message;            // ErrorCode.getMessage()
    private int status;                // HttpStatus 값
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;   // 에러 발생 시각
    private String path;               // 요청 URI
    private List<FieldError> errors;   // 필드별 에러 상세 목록 (Validation)

    public ErrorResponse() { }

    /**
     * ErrorCode 기반으로 기본 필드 세팅 (status, path, errors 포함)
     */
    public static ErrorResponse of(ErrorCode errorCode, String path, List<FieldError> errors) {
        ErrorResponse response = new ErrorResponse();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());
        response.setStatus(errorCode.getStatus().value());
        response.setTimestamp(LocalDateTime.now());
        response.setPath(path);
        response.setErrors(errors);
        return response;
    }

    @Getter
    public static class FieldError {
        private final String field;   // 잘못된 필드명
        private final String message; // 필드에 대한 오류 메시지

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}