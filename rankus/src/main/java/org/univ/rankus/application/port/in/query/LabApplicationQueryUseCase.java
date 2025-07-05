package org.univ.rankus.application.port.in.query;

import org.univ.rankus.domain.model.lab.application.LabApplication;

import java.util.List;

public interface LabApplicationQueryUseCase {

    List<LabApplication> listApplicationsByLab(Long labId);

    LabApplication getApplicationById(Long appId);

}