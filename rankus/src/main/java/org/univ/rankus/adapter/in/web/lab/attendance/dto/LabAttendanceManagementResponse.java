package org.univ.rankus.adapter.in.web.lab.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.univ.rankus.domain.model.attendance.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 랩실 출석 관리 통합 뷰 응답 DTO
 */
@Getter
@AllArgsConstructor
public class LabAttendanceManagementResponse {

    private List<AttendanceSessionSummary> sessions;
    private AttendanceOverallStats overallStats;

    /**
     * 출석 세션 요약 정보
     */
    @Getter
    @AllArgsConstructor
    public static class AttendanceSessionSummary {
        private Long sessionId;
        private String title;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private SessionStatus status;
        private int totalParticipants;
        private int presentCount;
        private int absentCount;
        private int lateCount;
        private double attendanceRate;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    /**
     * 전체 출석 통계
     */
    @Getter
    @AllArgsConstructor
    public static class AttendanceOverallStats {
        private int totalSessions;
        private int totalRecords;
        private int totalPresentRecords;
        private int totalAbsentRecords;
        private int totalLateRecords;
        private double overallAttendanceRate;
        private int activeMembers;
        private LocalDateTime lastSessionDate;
    }
}