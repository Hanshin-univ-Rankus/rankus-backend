package org.univ.rankus.adapter.in.web.dto.response;

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
public class AttendanceRecordResponseDto {

    private Long recordId;
    private Long sessionId;
    private Long userId;
    private AttendanceStatus status;
    private LocalDateTime checkedAt;
    private Boolean isManuallyAdjusted;
    private String adjustmentReason;
    private Long adjustedBy;
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