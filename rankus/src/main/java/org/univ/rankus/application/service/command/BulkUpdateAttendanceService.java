package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateRequest;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateResponse;
import org.univ.rankus.application.port.in.AttendanceRecordCommandUseCase;
import org.univ.rankus.application.port.in.command.BulkUpdateAttendanceCommand;
import org.univ.rankus.application.port.out.AttendanceRecordRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabPermissionException;
import org.univ.rankus.domain.model.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 출석 일괄 수정 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BulkUpdateAttendanceService implements BulkUpdateAttendanceCommand {

    private final AttendanceRecordCommandUseCase attendanceRecordCommandUseCase;
    private final AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public BulkAttendanceUpdateResponse bulkUpdateAttendance(Long labId, BulkAttendanceUpdateRequest request, Long managerId) {
        // 1. 랩실 존재 확인
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 2. 관리자 권한 확인
        User manager = userRepositoryPort.findById(managerId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        if (!manager.canManageLabAttendance(lab)) {
            throw new LabPermissionException(LabErrorCode.LAB_ATTENDANCE_MANAGE_PERMISSION_DENIED);
        }

        // 3. 각 출석 기록 수정 처리
        List<BulkAttendanceUpdateResponse.UpdateResult> results = new ArrayList<>();
        int successfulUpdates = 0;
        int failedUpdates = 0;

        for (BulkAttendanceUpdateRequest.AttendanceUpdateItem item : request.getUpdates()) {
            try {
                // 기존 출석 기록 조회
                AttendanceRecord existingRecord = attendanceRecordRepositoryPort.findById(item.getRecordId())
                        .orElseThrow(() -> new IllegalArgumentException("Attendance record not found with id: " + item.getRecordId()));

                AttendanceStatus oldStatus = existingRecord.getStatus();

                // 동일한 상태로 변경하려는 경우 스킵
                if (oldStatus == item.getNewStatus()) {
                    results.add(new BulkAttendanceUpdateResponse.UpdateResult(
                            item.getRecordId(),
                            false,
                            oldStatus,
                            item.getNewStatus(),
                            "Status is already " + item.getNewStatus()
                    ));
                    failedUpdates++;
                    continue;
                }

                // 기존 출석 수정 유스케이스 활용
                AttendanceRecord updatedRecord = attendanceRecordCommandUseCase.updateAttendanceStatus(
                        item.getRecordId(),
                        item.getNewStatus(),
                        managerId,
                        request.getReason()
                );

                results.add(new BulkAttendanceUpdateResponse.UpdateResult(
                        item.getRecordId(),
                        true,
                        oldStatus,
                        updatedRecord.getStatus(),
                        null
                ));
                successfulUpdates++;

                log.info("Successfully updated attendance record {} from {} to {} by user {}",
                        item.getRecordId(), oldStatus, item.getNewStatus(), managerId);

            } catch (Exception e) {
                results.add(new BulkAttendanceUpdateResponse.UpdateResult(
                        item.getRecordId(),
                        false,
                        null,
                        item.getNewStatus(),
                        e.getMessage()
                ));
                failedUpdates++;

                log.error("Failed to update attendance record {}: {}", item.getRecordId(), e.getMessage());
            }
        }

        return new BulkAttendanceUpdateResponse(
                request.getUpdates().size(),
                successfulUpdates,
                failedUpdates,
                results
        );
    }
}