package org.univ.rankus.adapter.in.web.lab.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;

import java.util.List;

/**
 * 출석 일괄 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "출석 일괄 수정 요청")
public class BulkAttendanceUpdateRequest {

    @NotEmpty(message = "Attendance updates list cannot be empty")
    @Schema(description = "출석 수정 목록")
    private List<AttendanceUpdateItem> updates;

    @NotNull(message = "Reason is required")
    @Schema(description = "일괄 수정 사유", example = "출석 점검 후 일괄 수정")
    private String reason;

    /**
     * 개별 출석 수정 항목
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "개별 출석 수정 항목")
    public static class AttendanceUpdateItem {

        @NotNull(message = "Record ID is required")
        @Schema(description = "출석 기록 ID", example = "1")
        private Long recordId;

        @NotNull(message = "New status is required")
        @Schema(description = "새로운 출석 상태", example = "PRESENT")
        private AttendanceStatus newStatus;
    }
}