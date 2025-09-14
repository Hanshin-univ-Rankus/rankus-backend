package org.univ.rankus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 출석 세션 생성 요청 DTO
 */
@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "출석 세션 생성 요청 정보")
public class AttendanceSessionCreateRequestDto {

    @Schema(
            description = "출석 세션 제목",
            example = "2024-02-15 정기 미팅",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title;

    @Schema(
            description = "QR 코드 유효시간(분)",
            example = "10",
            minimum = "1",
            maximum = "10",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "QR 유효시간은 최소 1분입니다")
    @Max(value = 10, message = "QR 유효시간은 최대 10분입니다")
    private Integer qrValidityMinutes;
}