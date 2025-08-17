package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.application.port.out.LabApplicationRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.lab.application.ApplicationStatus;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.exception.LabApplicationErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabApplicationNotFoundException;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 읽기 전용 최적화
public class LabApplicationQueryService implements LabApplicationQueryUseCase {

    private final LabRepositoryPort labRepositoryPort;
    private final LabApplicationRepositoryPort labApplicationRepositoryPort;

    @Override
    public List<LabApplication> listApplicationsByLab(Long labId) {
        labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));
        return labApplicationRepositoryPort.findByLabId(labId);
    }

    @Override
    public LabApplication getApplicationById(Long appId) {
        return labApplicationRepositoryPort.findById(appId)
                .orElseThrow(() ->
                        new LabApplicationNotFoundException(LabApplicationErrorCode.APPLICATION_NOT_FOUND)
                );
    }

    @Override
    public List<LabApplication> listMyApplications(Long userId, Long labIdNullable, ApplicationStatus statusNullable) {
        if (labIdNullable != null) {
            labRepositoryPort.findById(labIdNullable)
                    .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));
        }
        if (labIdNullable != null && statusNullable != null) {
            return labApplicationRepositoryPort.findAllByUserIdAndLabIdAndStatus(userId, labIdNullable, statusNullable);
        }
        if (labIdNullable != null) {
            return labApplicationRepositoryPort.findAllByUserIdAndLabId(userId, labIdNullable);
        }
        if (statusNullable != null) {
            return labApplicationRepositoryPort.findAllByUserIdAndStatus(userId, statusNullable);
        }
        return labApplicationRepositoryPort.findAllByUserId(userId);
    }
}