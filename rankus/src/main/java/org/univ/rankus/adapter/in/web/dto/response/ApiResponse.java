package org.univ.rankus.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;              // HTTP status code
    private String message;          // 상황 설명
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;                  // 실제 응답 페이로드
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object errors;           // validation 등 에러 세부 정보

    @Builder.Default
    private Instant timestamp = Instant.now();
}