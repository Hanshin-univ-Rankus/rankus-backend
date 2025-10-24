package org.univ.rankus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "투표 참여 요청")
public class VoteParticipateRequestDto {

    @Schema(description = "선택한 투표 옵션 ID", example = "1")
    @NotNull(message = "선택한 옵션 ID는 필수입니다")
    @Positive(message = "선택한 옵션 ID는 양수여야 합니다")
    private Long selectedOptionId;
}