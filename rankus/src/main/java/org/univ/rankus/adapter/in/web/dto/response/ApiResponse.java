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
}
