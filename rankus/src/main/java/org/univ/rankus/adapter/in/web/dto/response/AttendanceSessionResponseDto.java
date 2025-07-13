package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.SessionStatus;

import java.time.LocalDateTime;

/**
 * 출석 세션 응답 DTO
 */
@Data
@NoArgsConstructor
public class AttendanceSessionResponseDto {

    private Long sessionId;
    private Long labId;
    private String title;
    private SessionStatus status;
    private Integer qrValidityMinutes;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime endTime;

    public AttendanceSessionResponseDto(AttendanceSession session) {
        this.sessionId = session.getSessionId();
        this.labId = session.getLabId();
        this.title = session.getTitle();
        this.status = session.getStatus();
        this.qrValidityMinutes = session.getQrValidityMinutes();
        this.createdBy = session.getCreatedBy();
        this.createdAt = session.getCreatedAt();
        this.updatedAt = session.getUpdatedAt();
        this.endTime = session.getEndTime();
    }

    public static AttendanceSessionResponseDto from(AttendanceSession session) {
        return new AttendanceSessionResponseDto(session);
    }
}