package org.univ.rankus.application.service.command;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.LabApplicationCommandUseCase;
import org.univ.rankus.application.port.out.LabApplicationRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.exception.*;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional  // 쓰기 트랜잭션 관리
public class LabApplicationCommandService implements LabApplicationCommandUseCase {

    private final LabRepositoryPort labRepositoryPort;
    private final LabApplicationRepositoryPort labApplicationRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public LabApplication applyToLab(Long labId, Long userId, LocalDateTime interviewTime) {
        // 1) 랩실 검증
        Lab lab = labRepositoryPort.findById(labId)
                .orElseThrow(() -> new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));
        // 2) 유저 검증
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        // 3) 중복 신청 검증
        if (labApplicationRepositoryPort.existsByLabIdAndUserId(labId, userId)) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.DUPLICATE_APPLICATION);
        }
        // 4) 신청서 생성 및 저장
        LabApplication app = new LabApplication(lab, user, interviewTime);
        return labApplicationRepositoryPort.save(app);
    }

    @Override
    public void approveApplication(Long appId) {
        LabApplication app = labApplicationRepositoryPort.findById(appId)
                .orElseThrow(() ->
                        new LabApplicationNotFoundException(LabApplicationErrorCode.APPLICATION_NOT_FOUND)
                );
        app.approve();  // 도메인 내부에서 상태 변경 및 검증
    }

    @Override
    public void rejectApplication(Long appId) {
        LabApplication app = labApplicationRepositoryPort.findById(appId)
                .orElseThrow(() ->
                        new LabApplicationNotFoundException(LabApplicationErrorCode.APPLICATION_NOT_FOUND)
                );
        app.reject();   // 도메인 내부에서 상태 변경 및 검증
    }

    @Override
    public void cancelApplication(Long appId, Long userId) {
        LabApplication app = labApplicationRepositoryPort.findById(appId)
                .orElseThrow(() ->
                        new LabApplicationNotFoundException(LabApplicationErrorCode.APPLICATION_NOT_FOUND)
                );
        if (!app.isOwnedBy(userId)) {
            throw new LabApplicationValidationException(LabApplicationErrorCode.UNAUTHORIZED_CANCEL_ATTEMPT);
        }
        labApplicationRepositoryPort.delete(app);
    }
}