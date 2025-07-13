package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.attendance.AttendanceSession;

/**
 * 출석 통계 응답 DTO
 */
@Data
@NoArgsConstructor
public class AttendanceStatisticsResponseDto {

    private int totalMembers;
    private int presentCount;
    private int absentCount;
    private int lateCount;
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