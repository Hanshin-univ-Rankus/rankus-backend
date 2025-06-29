package org.univ.rankus.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 공통 응답 래퍼")
public class ApiResponse<T> {
    @Schema(description = "HTTP 상태 코드", example = "200")
    private int status;              // HTTP status code

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;          // 상황 설명

    @Schema(description = "응답 데이터", nullable = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;                  // 실제 응답 페이로드

    @Schema(description = "에러 상세 정보(검증 실패 등)", nullable = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object errors;           // validation 등 에러 세부 정보

    @Schema(description = "응답 발생 시각", example = "2025-06-17T07:30:15.123Z")
    @Builder.Default
    private Instant timestamp = Instant.now();

    // === 정적 팩토리 메서드들 ===

    /**
     * 성공 응답 (200 OK, 데이터 있음)
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    /**
     * 성공 응답 (200 OK, 커스텀 메시지)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 생성 성공 응답 (201 Created)
     */
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message("리소스가 성공적으로 생성되었습니다.")
                .data(data)
                .build();
    }

    /**
     * 생성 성공 응답 (201 Created, 커스텀 메시지)
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .status(201)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 삭제 성공 응답 (200 OK, 데이터 없음)
     */
    public static <T> ApiResponse<T> deleted() {
        return ApiResponse.<T>builder()
                .status(200)
                .message("리소스가 성공적으로 삭제되었습니다.")
                .build();
    }

    /**
     * 삭제 성공 응답 (200 OK, 커스텀 메시지)
     */
    public static <T> ApiResponse<T> deleted(String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .build();
    }

    /**
     * 에러 응답 (커스텀 상태코드)
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .build();
    }

    /**
     * 에러 응답 (커스텀 상태코드, 에러 상세정보 포함)
     */
    public static <T> ApiResponse<T> error(int status, String message, Object errors) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .errors(errors)
                .build();
    }
}
