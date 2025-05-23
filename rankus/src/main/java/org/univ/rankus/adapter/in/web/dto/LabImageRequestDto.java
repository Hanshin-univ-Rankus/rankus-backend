package org.univ.rankus.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.ImageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
public class LabImageRequestDto {

    @Schema(description = "업로드할 이미지 URL", example = "https://example.com/img.png")
    @NotBlank(message = "imageUrl은 필수입니다.")
    private String imageUrl;

    @Schema(description = "이미지 종류 (REPRESENTATIVE, ADDITIONAL)", example = "REPRESENTATIVE")
    @NotNull(message = "type은 필수입니다.")
    private ImageType type;
}
