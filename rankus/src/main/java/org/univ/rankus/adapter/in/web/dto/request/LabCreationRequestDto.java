package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.univ.rankus.domain.model.lab.core.LabCategory;

/**
 * 랩실 생성 신청 시 요청 파라미터를 받기 위한 DTO
 */
public record LabCreationRequestDto(
        @NotBlank(message = "신청할 랩실 이름은 필수입니다")
        @Size(max = 10, message = "랩실 이름은 10자 이하여야 합니다")
        String requestedLabName,

        @NotNull(message = "신청할 랩실 카테고리는 필수입니다")
        LabCategory requestedCategory,

        @Size(max = 255, message = "랩실 설명은 255자 이하여야 합니다")
        String requestedDescription
) {
}