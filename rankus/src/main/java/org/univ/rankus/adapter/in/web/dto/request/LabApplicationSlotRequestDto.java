package org.univ.rankus.adapter.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * LabApplication 신청 시 면접 슬롯 기반 요청 파라미터 DTO (record)
 * - Jackson 단일 필드 record 역직렬화 안정화를 위해 @JsonCreator(mode = PROPERTIES) 명시
 */
public record LabApplicationSlotRequestDto(
        @NotNull(message = "면접 슬롯 ID는 필수입니다.")
        @JsonProperty("slotId")
        Long slotId
) {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public LabApplicationSlotRequestDto(@JsonProperty("slotId") Long slotId) {
        this.slotId = slotId;
    }
}