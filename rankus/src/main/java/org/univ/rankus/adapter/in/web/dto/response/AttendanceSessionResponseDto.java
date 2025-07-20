package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "출석 세션 응답 정보")
public class AttendanceSessionResponseDto {

    @Schema(description = "출석 세션 ID", example = "1")
    private Long sessionId;

    @Schema(description = "랩실 ID", example = "1")
    private Long labId;

    @Schema(description = "세션 제목", example = "2024-02-15 정기 미팅")
    private String title;

    @Schema(description = "세션 상태")
    private SessionStatus status;

    @Schema(description = "QR 코드 유효시간(분)", example = "10")
    private Integer qrValidityMinutes;

    @Schema(description = "세션 생성자 ID", example = "1")
    private Long createdBy;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "종료일시", nullable = true)
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