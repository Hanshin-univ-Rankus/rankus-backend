package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequestNotFoundException;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequestValidationException;

/**
 * LabCreationRequest 명령 처리를 위한 UseCase 인터페이스
 * - 랩실 생성 신청 생성, 승인, 거절 등의 작업을 정의
 */
public interface LabCreationRequestCommandUseCase {

    /**
     * 새로운 랩실 생성 신청을 제출합니다.
     *
     * @param requestedLabName     신청할 랩실 이름
     * @param requestedCategory    신청할 랩실 카테고리
     * @param requestedDescription 신청할 랩실 설명 (선택)
     * @param requesterId          신청자 ID
     * @return 생성된 LabCreationRequest 엔티티
     * @throws org.univ.rankus.domain.model.user.exception.UserNotFoundException                신청자를 찾을 수 없는 경우
     * @throws LabCreationRequestValidationException 중복 신청 등 검증 실패 시
     */
    LabCreationRequest createLabCreationRequest(String requestedLabName, LabCategory requestedCategory,
                                                String requestedDescription, Long requesterId);

    /**
     * 랩실 생성 신청을 승인합니다.
     * 승인 시 실제 Lab 엔티티가 생성되고 신청자가 LAB_LEADER로 할당됩니다.
     *
     * @param requestId  승인할 신청 ID
     * @param approverId 승인자 ID (ADMIN 또는 PROFESSOR)
     * @throws LabCreationRequestNotFoundException   신청을 찾을 수 없는 경우
     * @throws LabCreationRequestValidationException 승인할 수 없는 상태인 경우
     * @throws org.univ.rankus.domain.model.user.exception.UserNotFoundException                승인자를 찾을 수 없는 경우
     * @throws LabCreationRequestValidationException 권한이 없는 경우
     */
    void approveLabCreationRequest(Long requestId, Long approverId);

    /**
     * 랩실 생성 신청을 거절합니다.
     *
     * @param requestId       거절할 신청 ID
     * @param rejectorId      거절자 ID (ADMIN 또는 PROFESSOR)
     * @param rejectionReason 거절 사유 (선택)
     * @throws LabCreationRequestNotFoundException   신청을 찾을 수 없는 경우
     * @throws LabCreationRequestValidationException 거절할 수 없는 상태인 경우
     * @throws org.univ.rankus.domain.model.user.exception.UserNotFoundException                거절자를 찾을 수 없는 경우
     * @throws LabCreationRequestValidationException 권한이 없는 경우
     */
    void rejectLabCreationRequest(Long requestId, Long rejectorId, String rejectionReason);

    /**
     * 랩실 생성 신청을 취소합니다.
     * 신청자 본인만 취소할 수 있으며, PENDING 상태에서만 가능합니다.
     *
     * @param requestId   취소할 신청 ID
     * @param requesterId 신청자 ID
     * @throws LabCreationRequestNotFoundException   신청을 찾을 수 없는 경우
     * @throws LabCreationRequestValidationException 취소할 수 없는 상태이거나 권한이 없는 경우
     */
    void cancelLabCreationRequest(Long requestId, Long requesterId);
}