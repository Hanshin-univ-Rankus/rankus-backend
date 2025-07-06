package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Interview 수정 시 요청 파라미터를 받기 위한 DTO
 */
@Getter
@Builder
public class InterviewUpdateRequestDto {

    @NotNull(message = "면접 시작일은 필수입니다.")
    @Future(message = "면접 시작일은 미래여야 합니다.")
    private final LocalDate startDate;

    @NotNull(message = "면접 종료일은 필수입니다.")
    @Future(message = "면접 종료일은 미래여야 합니다.")
    private final LocalDate endDate;

    @NotNull(message = "면접 소요 시간은 필수입니다.")
    @Min(value = 1, message = "면접 소요 시간은 1분 이상이어야 합니다.")
    private final Integer durationMinutes;

    @NotNull(message = "슬롯당 최대 지원자 수는 필수입니다.")
    @Min(value = 1, message = "슬롯당 최대 지원자 수는 1명 이상이어야 합니다.")
    private final Integer maxApplicantsPerSlot;
}