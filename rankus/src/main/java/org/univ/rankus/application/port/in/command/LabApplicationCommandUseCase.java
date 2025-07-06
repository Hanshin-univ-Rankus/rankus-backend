package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.lab.application.LabApplication;

import java.time.LocalDateTime;

public interface LabApplicationCommandUseCase {

    /**
     * 랩실 지원 신청 (레거시 - 시간 기반)
     *
     * @deprecated 면접 슬롯 시스템으로 대체됨. applyToLabWithSlot() 사용 권장
     */
    @Deprecated
    LabApplication applyToLab(Long labId, Long userId, LocalDateTime interviewTime);

    /**
     * 랩실 지원 신청 (슬롯 기반)
     */
    LabApplication applyToLabWithSlot(Long labId, Long userId, Long slotId);

    void approveApplication(Long appId);

    void rejectApplication(Long appId);

    void cancelApplication(Long appId, Long userId);
}