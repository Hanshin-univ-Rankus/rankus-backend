package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;

/**
 * 출석 상태 수정 요청 DTO
 */
@Data
@NoArgsConstructor
public class AttendanceStatusUpdateRequestDto {

    @NotNull(message = "출석 상태는 필수입니다")
    private AttendanceStatus status;

    @Size(min = 1, max = 200, message = "수정 사유는 1자 이상 200자 이하여야 합니다")
    private String reason;

    public AttendanceStatusUpdateRequestDto(AttendanceStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }
}