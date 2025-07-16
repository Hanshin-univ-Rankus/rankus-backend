package org.univ.rankus.application.service.lab.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.lab.export.ExportLabAttendanceQuery;
import org.univ.rankus.application.port.out.*;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 랩실 출석 Excel 내보내기 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportLabAttendanceService implements ExportLabAttendanceQuery {

    private final AttendanceSessionRepositoryPort attendanceSessionRepositoryPort;
    private final AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;
    private final ExcelExportPort excelExportPort;
    private final LabRepositoryPort labRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public byte[] exportLabAttendance(ExportLabAttendanceCommand command) {
        return exportLabAttendanceToExcel(command.labId(), command.requesterId());
    }

    @Override
    public byte[] exportLabAttendanceToExcel(Long labId, Long requesterId) {
        // 1. 권한 확인
        validatePermission(labId, requesterId);

        // 2. 랩실 정보 조회
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("Lab not found with id: " + labId));

        // 3. 해당 랩실의 모든 출석 세션 조회
        List<AttendanceSession> sessions = attendanceSessionRepositoryPort.findByLabId(labId);

        // 4. 모든 출석 기록 조회
        List<AttendanceRecord> records = sessions.stream()
                .flatMap(session -> attendanceRecordRepositoryPort.findBySessionId(session.getSessionId()).stream())
                .collect(Collectors.toList());

        // 5. Excel 파일 생성
        String fileName = lab.getName() + "_출석데이터_" + LocalDate.now();
        return excelExportPort.exportAttendanceToExcel(sessions, records, fileName);
    }

    @Override
    public byte[] exportLabAttendanceStatistics(Long labId, LocalDate fromDate, LocalDate toDate, Long requesterId) {
        // 1. 권한 확인
        validatePermission(labId, requesterId);

        // 2. 해당 랩실의 출석 세션 조회 (기간 필터링은 간단하게 구현)
        List<AttendanceSession> sessions = attendanceSessionRepositoryPort.findByLabId(labId).stream()
                .filter(session -> {
                    LocalDate sessionDate = session.getCreatedAt().toLocalDate();
                    return !sessionDate.isBefore(fromDate) && !sessionDate.isAfter(toDate);
                })
                .collect(Collectors.toList());

        // 3. 해당 세션들의 출석 기록 조회
        List<AttendanceRecord> records = sessions.stream()
                .flatMap(session -> attendanceRecordRepositoryPort.findBySessionId(session.getSessionId()).stream())
                .collect(Collectors.toList());

        // 4. Excel 파일 생성
        return excelExportPort.exportLabAttendanceStatistics(labId, fromDate, toDate, records);
    }

    @Override
    public byte[] exportMemberAttendanceHistory(Long labId, Long memberId, Long requesterId) {
        // 1. 권한 확인
        validatePermission(labId, requesterId);

        // 2. 멤버 정보 조회
        User member = userRepositoryPort.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        // 3. 멤버가 해당 랩실에 속하는지 확인
        if (member.getLab() == null || !member.getLab().getId().equals(labId)) {
            throw new IllegalArgumentException("Member does not belong to the specified lab");
        }

        // 4. 해당 멤버의 출석 기록 조회
        List<AttendanceRecord> records = attendanceRecordRepositoryPort.findByUserId(memberId);

        // 5. Excel 파일 생성
        return excelExportPort.exportUserAttendanceHistory(memberId, member.getName(), records);
    }

    @Override
    public byte[] generateAttendanceCertificate(Long labId, Long memberId, LocalDate fromDate, LocalDate toDate, Long requesterId) {
        // 1. 권한 확인
        validatePermission(labId, requesterId);

        // 2. 랩실 및 멤버 정보 조회
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("Lab not found with id: " + labId));

        User member = userRepositoryPort.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        // 3. 멤버가 해당 랩실에 속하는지 확인
        if (member.getLab() == null || !member.getLab().getId().equals(labId)) {
            throw new IllegalArgumentException("Member does not belong to the specified lab");
        }

        // 4. 해당 기간의 출석 기록 조회
        List<AttendanceRecord> records = attendanceRecordRepositoryPort.findByUserId(memberId).stream()
                .filter(record -> {
                    LocalDate recordDate = record.getCheckedAt().toLocalDate();
                    return !recordDate.isBefore(fromDate) && !recordDate.isAfter(toDate);
                })
                .collect(Collectors.toList());

        // 5. 출석 통계 계산
        int totalSessions = records.size();
        int attendedSessions = (int) records.stream()
                .filter(record -> record.getStatus() == AttendanceStatus.PRESENT)
                .count();

        double attendanceRate = totalSessions > 0
                ? (double) attendedSessions / totalSessions * 100
                : 0.0;

        // 6. Excel 파일 생성
        return excelExportPort.generateAttendanceCertificate(
                memberId,
                member.getName(),
                lab.getName(),
                fromDate,
                toDate,
                attendanceRate,
                totalSessions,
                attendedSessions
        );
    }

    /**
     * 권한 확인 공통 메서드
     */
    private void validatePermission(Long labId, Long requesterId) {
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new IllegalArgumentException("Lab not found with id: " + labId));

        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + requesterId));

        if (!requester.canViewLabAttendance(lab)) {
            throw new IllegalStateException("You don't have permission to view lab attendance");
        }
    }
}