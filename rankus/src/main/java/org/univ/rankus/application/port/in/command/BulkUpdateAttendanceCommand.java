package org.univ.rankus.application.port.in.command;

import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateRequest;
import org.univ.rankus.adapter.in.web.lab.attendance.dto.BulkAttendanceUpdateResponse;

/**
 * 출석 일괄 수정 명령 인터페이스
 */
public interface BulkUpdateAttendanceCommand {

    /**
     * 출석 기록을 일괄 수정합니다.
     *
     * @param labId     랩실 ID
     * @param request   일괄 수정 요청
     * @param managerId 수정자 ID
     * @return 수정 결과
     */
    BulkAttendanceUpdateResponse bulkUpdateAttendance(Long labId, BulkAttendanceUpdateRequest request, Long managerId);
}