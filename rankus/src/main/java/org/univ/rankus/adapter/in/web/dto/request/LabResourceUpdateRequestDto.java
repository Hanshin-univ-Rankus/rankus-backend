package org.univ.rankus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.lab.resource.ResourceCategory;

/**
 * 랩실 자료 수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "랩실 자료 수정 요청")
public class LabResourceUpdateRequestDto {

    @NotBlank(message = "자료 제목은 필수입니다")
    @Size(max = 100, message = "자료 제목은 100자 이하여야 합니다")
    @Schema(description = "자료 제목", example = "알고리즘 강의자료 (수정됨)")
    private String title;

    @Size(max = 500, message = "자료 설명은 500자 이하여야 합니다")
    @Schema(description = "자료 설명", example = "정렬 알고리즘에 대한 상세한 설명 자료입니다 (업데이트됨)")
    private String description;

    @NotNull(message = "자료 카테고리는 필수입니다")
    @Schema(description = "자료 카테고리", example = "RESEARCH")
    private ResourceCategory category;

    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;
}