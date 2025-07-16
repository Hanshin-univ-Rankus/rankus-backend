package org.univ.rankus.adapter.in.web.lab.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;

import java.util.List;

/**
 * 출석 일괄 수정 응답 DTO
 */
@Getter
@AllArgsConstructor
public class BulkAttendanceUpdateResponse {

    private int totalUpdates;
    private int successfulUpdates;
    private int failedUpdates;
    private List<UpdateResult> results;

    public int totalRequested() {
        return totalUpdates;
    }

    public int successfulUpdates() {
        return successfulUpdates;
    }

    public int failedUpdates() {
        return failedUpdates;
    }

    public boolean isCompleteSuccess() {
        return failedUpdates == 0;
    }

    /**
     * 개별 수정 결과
     */
    @Getter
    @AllArgsConstructor
    public static class UpdateResult {
        private Long recordId;
        private boolean success;
        private AttendanceStatus oldStatus;
        private AttendanceStatus newStatus;
        private String errorMessage;
    }
}