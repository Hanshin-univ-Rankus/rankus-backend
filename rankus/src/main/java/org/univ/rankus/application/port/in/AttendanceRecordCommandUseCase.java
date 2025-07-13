package org.univ.rankus.application.port.in;

import org.univ.rankus.domain.model.attendance.AttendanceRecord;
import org.univ.rankus.domain.model.attendance.AttendanceStatus;

/**
 * 출석 기록 명령 유스케이스
 * - 출석 상태 수정
 * - 출석 기록 관리
 */
public interface AttendanceRecordCommandUseCase {

    /**
     * 출석 상태를 결석으로 변경
     *
     * @param recordId   출석 기록 ID
     * @param adjustedBy 수정자 ID
     * @param reason     수정 사유
     * @return 수정된 출석 기록
     */
    AttendanceRecord markAsAbsent(Long recordId, Long adjustedBy, String reason);

    /**
     * 출석 상태를 지각으로 변경
     *
     * @param recordId   출석 기록 ID
     * @param adjustedBy 수정자 ID
     * @param reason     수정 사유
     * @return 수정된 출석 기록
     */
    AttendanceRecord markAsLate(Long recordId, Long adjustedBy, String reason);

    /**
     * 출석 상태를 출석으로 변경
     *
     * @param recordId   출석 기록 ID
     * @param adjustedBy 수정자 ID
     * @param reason     수정 사유
     * @return 수정된 출석 기록
     */
    AttendanceRecord markAsPresent(Long recordId, Long adjustedBy, String reason);

    /**
     * 출석 상태 직접 변경
     *
     * @param recordId   출석 기록 ID
     * @param newStatus  새 출석 상태
     * @param adjustedBy 수정자 ID
     * @param reason     수정 사유
     * @return 수정된 출석 기록
     */
    AttendanceRecord updateAttendanceStatus(Long recordId, AttendanceStatus newStatus, Long adjustedBy, String reason);
}