package org.univ.rankus.application.port.in.query;

import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.application.ApplicationStatus;

import java.util.List;

public interface LabApplicationQueryUseCase {

    List<LabApplication> listApplicationsByLab(Long labId);

    LabApplication getApplicationById(Long appId);

    // 내 신청 모아보기
    List<LabApplication> listMyApplications(Long userId, Long labIdNullable, ApplicationStatus statusNullable);

}