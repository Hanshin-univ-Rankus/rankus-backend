package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이름 변경 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeNameRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 30, message = "이름은 30자 이하여야 합니다.")
    private String newName;
}