package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.LabAttendanceManagementResponse;
import org.univ.rankus.application.port.in.query.GetLabAttendanceManagementQuery;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.AttendanceSessionRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 랩실 출석 관리 뷰 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLabAttendanceManagementService implements GetLabAttendanceManagementQuery {

    private final AttendanceSessionRepositoryPort attendanceSessionRepositoryPort;
    private final AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public LabAttendanceManagementResponse getLabAttendanceManagement(Long labId, Long requesterId) {
        // 1. 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("Lab not found with id: " + labId));

        // 2. 요청자 권한 확인
        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + requesterId));

        // 3. 출석 조회 권한 확인
        if (!requester.canViewLabAttendance(lab)) {
            throw new IllegalStateException("You don't have permission to view lab attendance");
        }

        // 4. 해당 랩실의 모든 출석 세션 조회
        List<AttendanceSession> sessions = attendanceSessionRepositoryPort.findByLabId(labId);

        // 5. 각 세션의 출석 기록 조회 및 통계 계산
        List<LabAttendanceManagementResponse.AttendanceSessionSummary> sessionSummaries = sessions.stream()
                .map(session -> {
                    // 세션별 출석 기록 조회
                    List<AttendanceRecord> records = attendanceRecordRepositoryPort.findBySessionId(session.getSessionId());

                    // 통계 계산
                    int totalParticipants = records.size();
                    int presentCount = (int) records.stream()
                            .filter(record -> record.getStatus() == AttendanceStatus.PRESENT)
                            .count();
                    int absentCount = (int) records.stream()
                            .filter(record -> record.getStatus() == AttendanceStatus.ABSENT)
                            .count();
                    int lateCount = (int) records.stream()
                            .filter(record -> record.getStatus() == AttendanceStatus.LATE)
                            .count();

                    double attendanceRate = totalParticipants > 0
                            ? (double) presentCount / totalParticipants * 100
                            : 0.0;

                    // 생성자 이름 조회
                    String createdByName = userRepositoryPort.findById(session.getCreatedBy())
                            .map(User::getName)
                            .orElse("Unknown");

                    return new LabAttendanceManagementResponse.AttendanceSessionSummary(
                            session.getSessionId(),
                            session.getTitle(),
                            session.getStartTime(),
                            session.getEndTime(),
                            session.getStatus(),
                            totalParticipants,
                            presentCount,
                            absentCount,
                            lateCount,
                            Math.round(attendanceRate * 100.0) / 100.0,
                            session.getCreatedAt(),
                            createdByName
                    );
                })
                .collect(Collectors.toList());

        // 6. 전체 통계 계산
        LabAttendanceManagementResponse.AttendanceOverallStats overallStats = calculateOverallStats(sessions, labId);

        return new LabAttendanceManagementResponse(sessionSummaries, overallStats);
    }

    /**
     * 전체 출석 통계 계산
     */
    private LabAttendanceManagementResponse.AttendanceOverallStats calculateOverallStats(List<AttendanceSession> sessions, Long labId) {
        int totalSessions = sessions.size();

        // 모든 세션의 출석 기록 조회
        List<AttendanceRecord> allRecords = sessions.stream()
                .flatMap(session -> attendanceRecordRepositoryPort.findBySessionId(session.getSessionId()).stream())
                .collect(Collectors.toList());

        int totalRecords = allRecords.size();
        int totalPresentRecords = (int) allRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.PRESENT)
                .count();
        int totalAbsentRecords = (int) allRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.ABSENT)
                .count();
        int totalLateRecords = (int) allRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.LATE)
                .count();

        double overallAttendanceRate = totalRecords > 0
                ? (double) totalPresentRecords / totalRecords * 100
                : 0.0;

        // 활성 멤버 수 (출석 기록이 있는 고유 사용자 수)
        int activeMembers = (int) allRecords.stream()
                .map(AttendanceRecord::getUserId)
                .distinct()
                .count();

        // 마지막 세션 날짜
        LocalDateTime lastSessionDate = sessions.stream()
                .map(AttendanceSession::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new LabAttendanceManagementResponse.AttendanceOverallStats(
                totalSessions,
                totalRecords,
                totalPresentRecords,
                totalAbsentRecords,
                totalLateRecords,
                Math.round(overallAttendanceRate * 100.0) / 100.0,
                activeMembers,
                lastSessionDate
        );
    }
}