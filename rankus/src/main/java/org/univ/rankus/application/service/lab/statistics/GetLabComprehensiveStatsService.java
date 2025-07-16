package org.univ.rankus.application.service.lab.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.in.web.lab.statistics.dto.LabComprehensiveStatsResponse;
import org.univ.rankus.application.port.in.lab.statistics.GetLabComprehensiveStatsQuery;
import org.univ.rankus.application.port.out.*;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 랩실 종합 통계 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLabComprehensiveStatsService implements GetLabComprehensiveStatsQuery {

    private final LabRepositoryPort labRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final AttendanceSessionRepositoryPort attendanceSessionRepositoryPort;
    private final AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;
    private final ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;
    private final LabApplicationRepositoryPort labApplicationRepositoryPort;
    private final LabNoticeRepositoryPort labNoticeRepositoryPort;
    private final InterviewRepositoryPort interviewRepositoryPort;

    @Override
    public LabComprehensiveStatsResponse getLabComprehensiveStats(Long requesterId) {
        // 1. 권한 확인 (ADMIN, PROFESSOR만 가능)
        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        if (requester.getRole() != Role.ADMIN && requester.getRole() != Role.PROFESSOR) {
            throw new LabPermissionException(LabErrorCode.LAB_STATISTICS_VIEW_PERMISSION_DENIED);
        }

        // 2. 모든 랩실 조회
        List<Lab> labs = labRepositoryPort.findAll();

        // 3. 각 랩실의 통계 계산
        List<LabComprehensiveStatsResponse.LabStats> labStats = labs.stream()
                .map(this::calculateLabStats)
                .collect(Collectors.toList());

        // 4. 전체 요약 통계 계산
        LabComprehensiveStatsResponse.OverallSummary overallSummary = calculateOverallSummary(labStats);

        return new LabComprehensiveStatsResponse(labStats, overallSummary);
    }

    @Override
    public LabComprehensiveStatsResponse.LabStats getLabDetailedStats(Long labId, Long requesterId) {
        // 1. 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 2. 권한 확인
        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        if (!requester.canViewLabAttendance(lab)) {
            throw new LabPermissionException(LabErrorCode.LAB_STATISTICS_VIEW_PERMISSION_DENIED);
        }

        // 3. 랩실 통계 계산
        return calculateLabStats(lab);
    }

    /**
     * 개별 랩실 통계 계산
     */
    private LabComprehensiveStatsResponse.LabStats calculateLabStats(Lab lab) {
        Long labId = lab.getId();

        // 멤버 통계
        List<User> members = userRepositoryPort.findByLab(lab);
        int totalMembers = members.size();
        int labLeaderCount = (int) members.stream()
                .filter(user -> user.getRole() == Role.LAB_LEADER)
                .count();
        int labManagerCount = (int) members.stream()
                .filter(user -> user.getRole() == Role.LAB_MANAGER)
                .count();
        int labMemberCount = (int) members.stream()
                .filter(user -> user.getRole() == Role.LAB_MEMBER)
                .count();

        // 출석 통계
        List<AttendanceSession> sessions = attendanceSessionRepositoryPort.findByLabId(labId);
        int totalSessions = sessions.size();

        // N+1 쿼리 방지: 랩실 ID로 모든 출석 기록을 한 번에 조회
        // 페이징 없이 모든 기록을 가져오기 위해 충분히 큰 size 사용
        List<AttendanceRecord> allRecords = attendanceRecordRepositoryPort.findByLabId(labId, 0, Integer.MAX_VALUE);

        double averageAttendanceRate = allRecords.isEmpty() ? 0.0 :
                (double) allRecords.stream()
                        .mapToInt(record -> record.getStatus() == AttendanceStatus.PRESENT ? 1 : 0)
                        .sum() / allRecords.size() * 100;

        int activeMembers = (int) allRecords.stream()
                .map(AttendanceRecord::getUserId)
                .distinct()
                .count();

        LocalDateTime lastSessionDate = sessions.stream()
                .map(AttendanceSession::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // 활동 통계 (점수 제출)
        int totalScoreSubmissions = scoreSubmissionRepositoryPort
                .findByLabIdAndStatus(labId, SubmissionStatus.APPROVED)
                .size();

        Integer totalScore = scoreSubmissionRepositoryPort
                .sumScoresByLabIdAndStatus(labId, SubmissionStatus.APPROVED);
        if (totalScore == null) totalScore = 0;

        int approvedSubmissions = scoreSubmissionRepositoryPort
                .findByLabIdAndStatus(labId, SubmissionStatus.APPROVED)
                .size();

        int pendingSubmissions = scoreSubmissionRepositoryPort
                .findByLabIdAndStatus(labId, SubmissionStatus.PENDING)
                .size();

        // 지원 통계 - 간단하게 구현
        int totalApplications = labApplicationRepositoryPort.findByLabId(labId).size();
        int pendingApplications = 0; // 구현 시 수정 필요
        int approvedApplications = 0; // 구현 시 수정 필요

        // 공지사항 통계 - 간단하게 구현
        int totalNotices = labNoticeRepositoryPort.findByLabId(labId).size();
        LocalDateTime lastNoticeDate = labNoticeRepositoryPort.findByLabId(labId).stream()
                .map(notice -> notice.getCreatedAt())
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // 면접 통계 - 간단하게 구현
        int totalInterviews = 0; // 구현 시 수정 필요
        int completedInterviews = 0; // 구현 시 수정 필요
        int pendingInterviews = 0; // 구현 시 수정 필요

        return new LabComprehensiveStatsResponse.LabStats(
                labId,
                lab.getName(),
                lab.getCategory(),
                lab.getProfessorName(),
                lab.getRanking(),
                totalMembers,
                labLeaderCount,
                labManagerCount,
                labMemberCount,
                activeMembers,
                totalSessions,
                Math.round(averageAttendanceRate * 100.0) / 100.0,
                lastSessionDate,
                totalScoreSubmissions,
                totalScore,
                approvedSubmissions,
                pendingSubmissions,
                totalApplications,
                pendingApplications,
                approvedApplications,
                totalNotices,
                lastNoticeDate,
                totalInterviews,
                completedInterviews,
                pendingInterviews
        );
    }

    /**
     * 전체 요약 통계 계산
     */
    private LabComprehensiveStatsResponse.OverallSummary calculateOverallSummary(List<LabComprehensiveStatsResponse.LabStats> labStats) {
        int totalLabs = labStats.size();
        int totalMembers = labStats.stream().mapToInt(LabComprehensiveStatsResponse.LabStats::getTotalMembers).sum();
        int totalSessions = labStats.stream().mapToInt(LabComprehensiveStatsResponse.LabStats::getTotalSessions).sum();
        int totalScoreSubmissions = labStats.stream().mapToInt(LabComprehensiveStatsResponse.LabStats::getTotalScoreSubmissions).sum();
        int totalApplications = labStats.stream().mapToInt(LabComprehensiveStatsResponse.LabStats::getTotalApplications).sum();
        int totalNotices = labStats.stream().mapToInt(LabComprehensiveStatsResponse.LabStats::getTotalNotices).sum();
        int totalInterviews = labStats.stream().mapToInt(LabComprehensiveStatsResponse.LabStats::getTotalInterviews).sum();

        double averageAttendanceRate = labStats.stream()
                .mapToDouble(LabComprehensiveStatsResponse.LabStats::getAverageAttendanceRate)
                .average()
                .orElse(0.0);

        int activeLabs = (int) labStats.stream()
                .filter(lab -> lab.getTotalSessions() > 0)
                .count();

        // 최고 출석률 랩실
        LabComprehensiveStatsResponse.LabStats mostActiveLabByAttendance = labStats.stream()
                .max(Comparator.comparingDouble(LabComprehensiveStatsResponse.LabStats::getAverageAttendanceRate))
                .orElse(null);

        // 최고 점수 랩실
        LabComprehensiveStatsResponse.LabStats mostActiveLabByScore = labStats.stream()
                .max(Comparator.comparingInt(LabComprehensiveStatsResponse.LabStats::getTotalScore))
                .orElse(null);

        // 최대 규모 랩실
        LabComprehensiveStatsResponse.LabStats largestLab = labStats.stream()
                .max(Comparator.comparingInt(LabComprehensiveStatsResponse.LabStats::getTotalMembers))
                .orElse(null);

        return new LabComprehensiveStatsResponse.OverallSummary(
                totalLabs,
                totalMembers,
                totalSessions,
                totalScoreSubmissions,
                totalApplications,
                totalNotices,
                totalInterviews,
                Math.round(averageAttendanceRate * 100.0) / 100.0,
                activeLabs,
                mostActiveLabByAttendance,
                mostActiveLabByScore,
                largestLab
        );
    }
}