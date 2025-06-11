package org.univ.rankus.application.port.in.query;

import org.univ.rankus.domain.model.lab.LabApplication;

import java.util.List;

public interface LabApplicationQueryUseCase {

    List<LabApplication> listApplicationsByLab(Long labId);
    LabApplication getApplicationById(Long appId);

}