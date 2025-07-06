package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * InterviewSlot 일괄 생성 시 요청 파라미터를 받기 위한 DTO
 */
@Getter
@Builder
public class InterviewSlotBatchCreateRequestDto {

    @NotEmpty(message = "슬롯 정보는 하나 이상 필요합니다.")
    @Valid
    private final List<InterviewSlotCreateRequestDto> slots;
}