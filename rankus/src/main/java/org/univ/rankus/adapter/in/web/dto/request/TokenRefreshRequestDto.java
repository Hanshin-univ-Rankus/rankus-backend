package org.univ.rankus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 갱신 요청을 받을 때 사용하는 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 갱신 요청 정보")
public class TokenRefreshRequestDto {

    @Schema(
            description = "리프레시 토큰",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}