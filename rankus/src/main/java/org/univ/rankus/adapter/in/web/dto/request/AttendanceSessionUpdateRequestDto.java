package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 출석 세션 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSessionUpdateRequestDto {

    @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하여야 합니다")
    private String title;

    @Min(value = 1, message = "QR 유효시간은 최소 1분입니다")
    @Max(value = 10, message = "QR 유효시간은 최대 10분입니다")
    private Integer qrValidityMinutes;

    @Size(max = 20, message = "상태값은 20자 이하여야 합니다")
    private String status;

    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다")
    private String reason;

}