package org.univ.rankus.application.port.in.query;

import org.univ.rankus.domain.model.lab.LabCreationRequest;
import org.univ.rankus.domain.model.lab.LabCreationStatus;

import java.util.List;

/**
 * LabCreationRequest 조회를 위한 UseCase 인터페이스
 * - 랩실 생성 신청 목록 조회, 단일 조회 등의 작업을 정의
 */
public interface LabCreationRequestQueryUseCase {

    /**
     * 모든 랩실 생성 신청을 최신순으로 조회합니다.
     * (관리자 전용)
     *
     * @return 최신순으로 정렬된 LabCreationRequest 리스트
     */
    List<LabCreationRequest> getAllLabCreationRequests();

    /**
     * 특정 상태의 랩실 생성 신청을 최신순으로 조회합니다.
     * (관리자 전용)
     *
     * @param status 조회할 상태
     * @return 해당 상태의 LabCreationRequest 리스트
     */
    List<LabCreationRequest> getLabCreationRequestsByStatus(LabCreationStatus status);

    /**
     * 특정 신청자의 랩실 생성 신청을 최신순으로 조회합니다.
     *
     * @param requesterId 신청자 ID
     * @return 해당 신청자의 LabCreationRequest 리스트
     * @throws org.univ.rankus.domain.model.user.exception.UserNotFoundException 신청자를 찾을 수 없는 경우
     */
    List<LabCreationRequest> getLabCreationRequestsByRequester(Long requesterId);

    /**
     * ID로 특정 랩실 생성 신청을 조회합니다.
     *
     * @param requestId 조회할 신청 ID
     * @return LabCreationRequest 엔티티
     * @throws org.univ.rankus.domain.model.lab.exception.LabCreationRequestNotFoundException 신청을 찾을 수 없는 경우
     */
    LabCreationRequest getLabCreationRequestById(Long requestId);

    /**
     * 대기 중인(PENDING) 랩실 생성 신청 목록을 조회합니다.
     * (관리자 전용 - 승인 대기 목록)
     *
     * @return 대기 중인 LabCreationRequest 리스트
     */
    List<LabCreationRequest> getPendingLabCreationRequests();
}