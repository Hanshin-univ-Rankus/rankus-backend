package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.lab.creation.LabCreationStatus;
import org.univ.rankus.domain.model.user.User;

import java.util.List;

/**
 * 순수 JPA 기반 LabCreationRequest 저장소 인터페이스
 * - JpaRepository<LabCreationRequest, Long>을 상속하면 기본 CRUD 메서드를 모두 제공
 * - 추가로 LabCreationRequest 조회·검증을 위한 커스텀 메서드를 선언
 */
@Repository
public interface SpringDataLabCreationRequestRepository extends JpaRepository<LabCreationRequest, Long> {

    /**
     * 모든 LabCreationRequest를 생성일시 내림차순으로 조회합니다.
     */
    List<LabCreationRequest> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 상태의 LabCreationRequest를 생성일시 내림차순으로 조회합니다.
     */
    List<LabCreationRequest> findByStatusOrderByCreatedAtDesc(LabCreationStatus status);

    /**
     * 특정 신청자의 LabCreationRequest를 생성일시 내림차순으로 조회합니다.
     */
    List<LabCreationRequest> findByRequesterOrderByCreatedAtDesc(User requester);

    /**
     * 특정 신청자가 특정 랩실 이름으로 특정 상태의 신청이 있는지 확인합니다.
     */
    boolean existsByRequesterAndRequestedLabNameAndStatus(User requester, String requestedLabName, LabCreationStatus status);

    /**
     * 특정 랩실 이름으로 특정 상태의 신청이 있는지 확인합니다.
     */
    boolean existsByRequestedLabNameAndStatus(String requestedLabName, LabCreationStatus status);
}