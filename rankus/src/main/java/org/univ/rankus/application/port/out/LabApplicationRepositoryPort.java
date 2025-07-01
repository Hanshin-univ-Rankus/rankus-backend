package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.lab.LabApplication;
import org.univ.rankus.domain.model.user.User;

import java.util.List;
import java.util.Optional;

/**
 * LabApplication 도메인 퍼시스턴스 포트 인터페이스
 * - 서비스 계층은 이 인터페이스를 통해서만 LabApplication을 저장·조회·삭제합니다.
 */
public interface LabApplicationRepositoryPort {

    /**
     * 새로운 LabApplication을 저장 또는 업데이트합니다.
     *
     * @param application 저장할 LabApplication 엔티티
     * @return 영속화된 LabApplication (저장 후 ID 포함)
     */
    LabApplication save(LabApplication application);

    /**
     * ID로 LabApplication을 조회합니다.
     *
     * @param id 조회할 LabApplication ID
     * @return Optional.of(LabApplication) 또는 Optional.empty()
     */
    Optional<LabApplication> findById(Long id);

    /**
     * 특정 랩실 ID에 속한 모든 LabApplication을 조회합니다.
     *
     * @param labId 조회할 랩실 ID
     * @return 해당 랩실에 속한 LabApplication 리스트 (빈 리스트 가능)
     */
    List<LabApplication> findByLabId(Long labId);

    /**
     * 특정 랩실 ID와 특정 유저 ID가 중복 신청했는지 여부를 확인합니다.
     *
     * @param labId 랩실 ID
     * @param user  유저 ID
     * @return 이미 존재하면 true
     */
    boolean existsByLabIdAndUser(Long labId, User user);

    /**
     * LabApplication을 삭제합니다.
     *
     * @param application 삭제할 LabApplication 엔티티
     */
    void delete(LabApplication application);

    boolean existsByLabIdAndUserId(Long labId, Long userId);
}