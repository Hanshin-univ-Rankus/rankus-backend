package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

/**
 * LabApplication 신청 시 면접 슬롯 기반 요청 파라미터를 받기 위한 DTO
 * - 기존 interviewTime 대신 slotId 사용
 */
@Getter
@Builder
public class LabApplicationSlotRequestDto {
    
    @NotNull(message = "면접 슬롯 ID는 필수입니다.")
    private final Long slotId;
}