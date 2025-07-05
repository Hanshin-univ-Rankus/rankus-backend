package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.core.ImageType;

/**
 * LabImage 등록 시 요청 파라미터를 받기 위한 DTO
 */
@Getter
public class LabImageRequestDto {

    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String imageUrl;

    @NotNull(message = "이미지 타입은 필수입니다.")
    private ImageType type;
}