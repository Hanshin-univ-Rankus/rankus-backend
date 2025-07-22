package org.univ.rankus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상태 변경 요청 DTO")
public class StatusChangeRequestDto {

    @NotBlank(message = "변경할 상태는 필수입니다")
    @Schema(description = "변경할 상태 값", example = "ACTIVE", required = true)
    private String status;

    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다")
    @Schema(description = "상태 변경 사유 (선택사항)", example = "정기 점검을 위한 세션 종료")
    private String reason;
}