package org.univ.rankus.application.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.query.LabCreationRequestQueryUseCase;
import org.univ.rankus.application.port.out.LabCreationRequestRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequestNotFoundException;
import org.univ.rankus.domain.model.lab.creation.LabCreationStatus;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 읽기 전용 최적화
public class LabCreationRequestQueryService implements LabCreationRequestQueryUseCase {

    private final LabCreationRequestRepositoryPort labCreationRequestRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public List<LabCreationRequest> getAllLabCreationRequests() {
        return labCreationRequestRepositoryPort.findAllByCreatedAtDesc();
    }

    @Override
    public List<LabCreationRequest> getLabCreationRequestsByStatus(LabCreationStatus status) {
        return labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<LabCreationRequest> getLabCreationRequestsByRequester(Long requesterId) {
        // 신청자 존재 여부 검증
        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        return labCreationRequestRepositoryPort.findByRequesterOrderByCreatedAtDesc(requester);
    }

    @Override
    public LabCreationRequest getLabCreationRequestById(Long requestId) {
        return labCreationRequestRepositoryPort.findById(requestId)
                .orElseThrow(() -> new LabCreationRequestNotFoundException());
    }

    @Override
    public List<LabCreationRequest> getPendingLabCreationRequests() {
        return labCreationRequestRepositoryPort.findByStatusOrderByCreatedAtDesc(LabCreationStatus.PENDING);
    }
}