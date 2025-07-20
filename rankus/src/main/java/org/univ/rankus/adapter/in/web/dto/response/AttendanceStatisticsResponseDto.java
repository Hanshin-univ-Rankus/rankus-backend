package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.AttendanceSession;

/**
 * 출석 통계 응답 DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "출석 통계 응답 정보")
public class AttendanceStatisticsResponseDto {

    @Schema(description = "전체 멤버 수", example = "10")
    private int totalMembers;

    @Schema(description = "출석 수", example = "8")
    private int presentCount;

    @Schema(description = "결석 수", example = "2")
    private int absentCount;

    @Schema(description = "지각 수", example = "1")
    private int lateCount;

    @Schema(description = "출석률", example = "0.8")
    private double attendanceRate;

    public AttendanceStatisticsResponseDto(AttendanceSession.AttendanceStatistics statistics) {
        this.totalMembers = statistics.getTotalMembers();
        this.presentCount = statistics.getPresentCount();
        this.absentCount = statistics.getAbsentCount();
        this.lateCount = statistics.getLateCount();
        this.attendanceRate = statistics.getAttendanceRate();
    }

    public static AttendanceStatisticsResponseDto from(AttendanceSession.AttendanceStatistics statistics) {
        return new AttendanceStatisticsResponseDto(statistics);
    }
}