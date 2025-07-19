package org.univ.rankus.adapter.in.web.dto.request;

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
public class VoteParticipateRequestDto {

    @NotNull(message = "선택한 옵션 ID는 필수입니다")
    @Positive(message = "선택한 옵션 ID는 양수여야 합니다")
    private Long selectedOptionId;
}