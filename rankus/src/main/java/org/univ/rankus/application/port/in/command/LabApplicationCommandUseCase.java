package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.lab.application.LabApplication;

import java.time.LocalDateTime;

public interface LabApplicationCommandUseCase {

    LabApplication applyToLab(Long labId, Long userId, LocalDateTime interviewTime);

    void approveApplication(Long appId);

    void rejectApplication(Long appId);

    void cancelApplication(Long appId, Long userId);
}