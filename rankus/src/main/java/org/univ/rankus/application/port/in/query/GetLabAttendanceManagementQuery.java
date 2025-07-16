package org.univ.rankus.application.port.in.query;

import org.univ.rankus.adapter.in.web.lab.attendance.dto.LabAttendanceManagementResponse;

/**
 * 랩실 출석 관리 뷰 조회 쿼리 인터페이스
 */
public interface GetLabAttendanceManagementQuery {

    /**
     * 특정 랩실의 출석 관리 통합 뷰를 조회합니다.
     *
     * @param labId       랩실 ID
     * @param requesterId 요청자 ID (권한 검증용)
     * @return 출석 관리 통합 뷰 정보
     */
    LabAttendanceManagementResponse getLabAttendanceManagement(Long labId, Long requesterId);
}