package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.lab.creation.LabCreationStatus;
import org.univ.rankus.domain.model.user.User;

import java.util.List;
import java.util.Optional;

/**
 * LabCreationRequest 도메인 퍼시스턴스 포트 인터페이스
 * - 서비스 계층은 이 인터페이스를 통해서만 LabCreationRequest를 저장·조회·삭제합니다.
 */
public interface LabCreationRequestRepositoryPort {

    /**
     * 새로운 LabCreationRequest를 저장하거나 수정합니다.
     *
     * @param request 저장할 LabCreationRequest 엔티티
     * @return 영속화된 LabCreationRequest (저장 후 ID 포함)
     */
    LabCreationRequest save(LabCreationRequest request);

    /**
     * ID로 LabCreationRequest를 조회합니다.
     *
     * @param id 조회할 LabCreationRequest ID
     * @return Optional.of(LabCreationRequest) 또는 Optional.empty()
     */
    Optional<LabCreationRequest> findById(Long id);

    /**
     * 모든 LabCreationRequest를 생성일시 내림차순으로 조회합니다.
     *
     * @return 생성일시 내림차순으로 정렬된 LabCreationRequest 리스트
     */
    List<LabCreationRequest> findAllByCreatedAtDesc();

    /**
     * 특정 상태의 LabCreationRequest를 생성일시 내림차순으로 조회합니다.
     *
     * @param status 조회할 상태
     * @return 해당 상태의 LabCreationRequest 리스트
     */
    List<LabCreationRequest> findByStatusOrderByCreatedAtDesc(LabCreationStatus status);

    /**
     * 특정 신청자의 LabCreationRequest를 생성일시 내림차순으로 조회합니다.
     *
     * @param requester 신청자
     * @return 해당 신청자의 LabCreationRequest 리스트
     */
    List<LabCreationRequest> findByRequesterOrderByCreatedAtDesc(User requester);

    /**
     * 특정 신청자가 특정 랩실 이름으로 대기 중인 신청이 있는지 확인합니다.
     *
     * @param requester        신청자
     * @param requestedLabName 신청할 랩실 이름
     * @return 대기 중인 신청이 있으면 true
     */
    boolean existsByRequesterAndRequestedLabNameAndStatus(User requester, String requestedLabName, LabCreationStatus status);

    /**
     * 특정 랩실 이름으로 대기 중인 신청이 있는지 확인합니다.
     *
     * @param requestedLabName 신청할 랩실 이름
     * @return 대기 중인 신청이 있으면 true
     */
    boolean existsByRequestedLabNameAndStatus(String requestedLabName, LabCreationStatus status);

    /**
     * LabCreationRequest를 삭제합니다.
     *
     * @param request 삭제할 LabCreationRequest 엔티티
     */
    void delete(LabCreationRequest request);

    /**
     * ID로 LabCreationRequest를 삭제합니다.
     *
     * @param id 삭제할 LabCreationRequest ID
     */
    void deleteById(Long id);

    /**
     * ID로 LabCreationRequest 존재 여부를 확인합니다.
     *
     * @param id 확인할 LabCreationRequest ID
     * @return 존재하면 true
     */
    boolean existsById(Long id);
}