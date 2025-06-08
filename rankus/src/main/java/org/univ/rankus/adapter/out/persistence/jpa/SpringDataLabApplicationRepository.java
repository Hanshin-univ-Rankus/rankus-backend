package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.univ.rankus.domain.model.lab.LabApplication;
import org.univ.rankus.domain.model.user.User;

import java.util.List;
import java.util.Optional;

/**
 * 순수 JPA 기반 LabApplication 저장소 인터페이스
 * - JpaRepository<LabApplication, Long>을 상속하면 기본 CRUD 메서드를 모두 제공
 * - 추가로 LabApplication 조회·검증을 위한 커스텀 메서드를 선언
 */
public interface SpringDataLabApplicationRepository extends JpaRepository<LabApplication, Long> {

    /**
     * 랩실 ID로 LabApplication 리스트를 조회합니다.
     */
    List<LabApplication> findByLabId(Long labId);

    /**
     * 랩실 ID와 유저 ID가 같은 LabApplication이 존재하는지 확인합니다.
     */
    boolean existsByLabIdAndUser(Long labId, User user);

    /**
     * (선택) 랩실 ID와 신청서 ID가 동시에 일치하는지 조회합니다.
     * 예를 들어, “특정 랩실의 특정 신청서인가?”를 체크할 때 유용합니다.
     */
    Optional<LabApplication> findByIdAndLabId(Long id, Long labId);

    boolean existsByLabIdAndUserId(Long labId, Long userId);
}