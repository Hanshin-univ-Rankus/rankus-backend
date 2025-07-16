package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.in.web.lab.member.dto.LabMemberDetailResponse;
import org.univ.rankus.application.port.in.query.GetLabMemberActivityQuery;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.ScoreSubmissionRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;

import java.util.List;

/**
 * 랩원 활동 통계 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLabMemberActivityService implements GetLabMemberActivityQuery {

    private final AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;
    private final ScoreSubmissionRepositoryPort scoreSubmissionRepositoryPort;

    @Override
    public LabMemberDetailResponse.LabActivityStats getLabMemberActivityStats(Long labId, Long memberId) {
        // 1. 출석 기록 조회
        List<AttendanceRecord> attendanceRecords = attendanceRecordRepositoryPort.findByUserId(memberId);

        // 2. 출석 통계 계산
        int totalAttendanceSessions = attendanceRecords.size();
        int attendedSessions = (int) attendanceRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.PRESENT)
                .count();
        int absentSessions = (int) attendanceRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.ABSENT)
                .count();
        int lateSessions = (int) attendanceRecords.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.LATE)
                .count();

        double attendanceRate = totalAttendanceSessions > 0
                ? (double) attendedSessions / totalAttendanceSessions * 100
                : 0.0;

        // 3. 점수 제출 통계 계산
        int scoreSubmissions = scoreSubmissionRepositoryPort
                .findByUserIdAndLabIdAndStatus(memberId, labId, SubmissionStatus.APPROVED)
                .size();

        Integer totalScore = scoreSubmissionRepositoryPort
                .sumScoresByUserIdAndLabIdAndStatus(memberId, labId, SubmissionStatus.APPROVED);
        if (totalScore == null) {
            totalScore = 0;
        }

        // 4. 랭킹 기여도 계산 (승인된 점수 제출의 총합)
        int rankingContribution = totalScore;

        return new LabMemberDetailResponse.LabActivityStats(
                totalAttendanceSessions,
                attendedSessions,
                absentSessions,
                lateSessions,
                Math.round(attendanceRate * 100.0) / 100.0, // 소수점 2자리로 반올림
                scoreSubmissions,
                totalScore,
                rankingContribution
        );
    }
}