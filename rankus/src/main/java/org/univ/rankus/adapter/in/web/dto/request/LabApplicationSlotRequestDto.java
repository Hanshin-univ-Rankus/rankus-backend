package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * LabApplication 신청 시 면접 슬롯 기반 요청 파라미터 DTO (record)
 * - 기존 클래스 형태에서 record로 변경하여 Jackson 역직렬화 안정화
 */
public record LabApplicationSlotRequestDto(
        @NotNull(message = "면접 슬롯 ID는 필수입니다.")
        Long slotId
) {
}