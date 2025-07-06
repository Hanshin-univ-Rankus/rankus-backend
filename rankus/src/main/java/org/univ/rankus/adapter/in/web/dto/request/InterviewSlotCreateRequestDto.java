package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * InterviewSlot 생성 시 요청 파라미터를 받기 위한 DTO
 */
@Getter
@Builder
public class InterviewSlotCreateRequestDto {

    @NotNull(message = "슬롯 시작 시간은 필수입니다.")
    @Future(message = "슬롯 시작 시간은 미래여야 합니다.")
    private final LocalDateTime startTime;

    @NotNull(message = "슬롯 종료 시간은 필수입니다.")
    @Future(message = "슬롯 종료 시간은 미래여야 합니다.")
    private final LocalDateTime endTime;

    @Min(value = 1, message = "최대 지원자 수는 1명 이상이어야 합니다.")
    private final Integer maxApplicants;
}