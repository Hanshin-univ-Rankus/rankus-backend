package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.LabCreationRequestCommandUseCase;
import org.univ.rankus.application.port.out.LabCreationRequestRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.lab.creation.*;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class LabCreationRequestCommandService implements LabCreationRequestCommandUseCase {

    private final LabCreationRequestRepositoryPort labCreationRequestRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final LabRepositoryPort labRepositoryPort;

    @Override
    @Transactional
    public LabCreationRequest createLabCreationRequest(String requestedLabName, LabCategory requestedCategory,
                                                       String requestedDescription, Long requesterId) {
        // 1) 신청자 검증
        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2) 중복 신청 검증 (동일한 신청자가 동일한 랩실 이름으로 대기 중인 신청이 있는지)
        if (labCreationRequestRepositoryPort.existsByRequesterAndRequestedLabNameAndStatus(
                requester, requestedLabName, LabCreationStatus.PENDING)) {
            throw new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.DUPLICATE_LAB_NAME_REQUEST
            );
        }

        // 3) 동일한 랩실 이름으로 대기 중인 다른 신청이 있는지 검증
        if (labCreationRequestRepositoryPort.existsByRequestedLabNameAndStatus(requestedLabName, LabCreationStatus.PENDING)) {
            throw new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.DUPLICATE_LAB_NAME_REQUEST
            );
        }

        // 4) 신청서 생성 및 저장
        LabCreationRequest request = new LabCreationRequest(
                requestedLabName, requestedCategory, requestedDescription, requester
        );

        return labCreationRequestRepositoryPort.save(request);
    }

    @Override
    @Transactional
    public void approveLabCreationRequest(Long requestId, Long approverId) {
        // 1) 신청 조회
        LabCreationRequest request = labCreationRequestRepositoryPort.findById(requestId)
                .orElseThrow(() -> new LabCreationRequestNotFoundException());

        // 2) 승인자 검증
        User approver = userRepositoryPort.findById(approverId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 3) 도메인 로직으로 승인 처리 (권한 검증 포함)
        request.approve(approver);

        // 4) 실제 Lab 엔티티 생성
        Lab newLab = new Lab(
                request.getRequestedLabName(),
                request.getRequestedCategory(),
                request.getRequestedDescription(),
                null  // 교수 이름은 나중에 설정
        );

        // 5) Lab 저장
        Lab savedLab = labRepositoryPort.save(newLab);

        // 6) 신청자를 LAB_LEADER로 할당하고 Lab에 배정
        User requester = request.getRequester();
        requester.changeRole(Role.LAB_LEADER);
        requester.assignLab(savedLab);
        userRepositoryPort.save(requester);

        // 7) 신청서 상태 저장
        labCreationRequestRepositoryPort.save(request);
    }

    @Override
    @Transactional
    public void rejectLabCreationRequest(Long requestId, Long rejectorId, String rejectionReason) {
        // 1) 신청 조회
        LabCreationRequest request = labCreationRequestRepositoryPort.findById(requestId)
                .orElseThrow(() -> new LabCreationRequestNotFoundException());

        // 2) 거절자 검증
        User rejector = userRepositoryPort.findById(rejectorId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 3) 도메인 로직으로 거절 처리 (권한 검증 포함)
        request.reject(rejector, rejectionReason);

        // 4) 신청서 상태 저장
        labCreationRequestRepositoryPort.save(request);
    }

    @Override
    @Transactional
    public void cancelLabCreationRequest(Long requestId, Long requesterId) {
        // 1) 신청 조회
        LabCreationRequest request = labCreationRequestRepositoryPort.findById(requestId)
                .orElseThrow(() -> new LabCreationRequestNotFoundException());

        // 2) 신청자 검증
        User requester = userRepositoryPort.findById(requesterId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 3) 소유권 검증
        if (!request.isOwnedBy(requester)) {
            throw new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.INSUFFICIENT_PERMISSION_FOR_APPROVAL
            );
        }

        // 4) 상태 검증 (PENDING 상태에서만 취소 가능)
        if (!request.isPending()) {
            throw new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION
            );
        }

        // 5) 신청서 삭제
        labCreationRequestRepositoryPort.delete(request);
    }
}