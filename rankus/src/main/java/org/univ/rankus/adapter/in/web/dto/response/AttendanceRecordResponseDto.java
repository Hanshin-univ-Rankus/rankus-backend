package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;

import java.time.LocalDateTime;

/**
 * 출석 기록 응답 DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "출석 기록 응답 정보")
public class AttendanceRecordResponseDto {

    @Schema(description = "출석 기록 ID", example = "1")
    private Long recordId;

    @Schema(description = "출석 세션 ID", example = "1")
    private Long sessionId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "출석 상태")
    private AttendanceStatus status;

    @Schema(description = "출석 체크 시간")
    private LocalDateTime checkedAt;

    @Schema(description = "수동 조정 여부", example = "false")
    private Boolean isManuallyAdjusted;

    @Schema(description = "조정 사유", nullable = true)
    private String adjustmentReason;

    @Schema(description = "조정한 사용자 ID", nullable = true)
    private Long adjustedBy;

    @Schema(description = "조정 시간", nullable = true)
    private LocalDateTime adjustedAt;

    public AttendanceRecordResponseDto(AttendanceRecord record) {
        this.recordId = record.getRecordId();
        this.sessionId = record.getSessionId();
        this.userId = record.getUserId();
        this.status = record.getStatus();
        this.checkedAt = record.getCheckedAt();
        this.isManuallyAdjusted = record.getIsManuallyAdjusted();
        this.adjustmentReason = record.getAdjustmentReason();
        this.adjustedBy = record.getAdjustedBy();
        this.adjustedAt = record.getAdjustedAt();
    }

    public static AttendanceRecordResponseDto from(AttendanceRecord record) {
        return new AttendanceRecordResponseDto(record);
    }
}