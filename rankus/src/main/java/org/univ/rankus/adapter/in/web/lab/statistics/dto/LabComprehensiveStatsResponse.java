package org.univ.rankus.adapter.in.web.lab.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.core.LabCategory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 랩실 종합 통계 응답 DTO
 */
@Getter
@AllArgsConstructor
public class LabComprehensiveStatsResponse {

    private List<LabStats> labStats;
    private OverallSummary overallSummary;

    /**
     * 개별 랩실 통계
     */
    @Getter
    @AllArgsConstructor
    public static class LabStats {
        private Long labId;
        private String labName;
        private LabCategory category;
        private String professorName;
        private int ranking;

        // 멤버 통계
        private int totalMembers;
        private int labLeaderCount;
        private int labManagerCount;
        private int labMemberCount;
        private int activeMembers;

        // 출석 통계
        private int totalSessions;
        private double averageAttendanceRate;
        private LocalDateTime lastSessionDate;

        // 활동 통계
        private int totalScoreSubmissions;
        private int totalScore;
        private int approvedSubmissions;
        private int pendingSubmissions;

        // 지원 통계
        private int totalApplications;
        private int pendingApplications;
        private int approvedApplications;

        // 공지사항 통계
        private int totalNotices;
        private LocalDateTime lastNoticeDate;

        // 면접 통계
        private int totalInterviews;
        private int completedInterviews;
        private int pendingInterviews;
    }

    /**
     * 전체 요약 통계
     */
    @Getter
    @AllArgsConstructor
    public static class OverallSummary {
        private int totalLabs;
        private int totalMembers;
        private int totalSessions;
        private int totalScoreSubmissions;
        private int totalApplications;
        private int totalNotices;
        private int totalInterviews;
        private double averageAttendanceRate;
        private int activeLabs;
        private LabStats mostActiveLabByAttendance;
        private LabStats mostActiveLabByScore;
        private LabStats largestLab;
    }
}