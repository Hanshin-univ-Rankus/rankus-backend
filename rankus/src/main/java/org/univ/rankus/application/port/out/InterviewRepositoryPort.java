package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewStatus;

import java.util.List;
import java.util.Optional;

/**
 * Interview 도메인 퍼시스턴스 포트 인터페이스
 * - 서비스 계층은 이 인터페이스를 통해서만 Interview를 저장·조회·삭제합니다.
 */
public interface InterviewRepositoryPort {

    /**
     * 새로운 Interview를 저장 또는 업데이트합니다.
     *
     * @param interview 저장할 Interview 엔티티
     * @return 영속화된 Interview (저장 후 ID 포함)
     */
    Interview save(Interview interview);

    /**
     * ID로 Interview를 조회합니다.
     *
     * @param id 조회할 Interview ID
     * @return Optional.of(Interview) 또는 Optional.empty()
     */
    Optional<Interview> findById(Long id);

    /**
     * 특정 랩실 ID에 속한 모든 Interview를 조회합니다.
     *
     * @param labId 조회할 랩실 ID
     * @return 해당 랩실에 속한 Interview 리스트 (빈 리스트 가능)
     */
    List<Interview> findByLabId(Long labId);

    /**
     * 특정 랩실의 특정 상태인 Interview를 조회합니다.
     *
     * @param labId  랩실 ID
     * @param status 면접 상태
     * @return 해당 조건에 맞는 Interview 리스트
     */
    List<Interview> findByLabIdAndStatus(Long labId, InterviewStatus status);

    /**
     * 특정 랩실에 활성화된 면접이 있는지 확인합니다.
     *
     * @param labId 랩실 ID
     * @return 활성화된 면접이 있으면 true
     */
    boolean existsByLabIdAndStatus(Long labId, InterviewStatus status);

    /**
     * Interview를 삭제합니다.
     *
     * @param interview 삭제할 Interview 엔티티
     */
    void delete(Interview interview);

    /**
     * ID로 Interview를 삭제합니다.
     *
     * @param id 삭제할 Interview ID
     */
    void deleteById(Long id);

    /**
     * 모든 Interview를 조회합니다.
     *
     * @return 모든 Interview 리스트
     */
    List<Interview> findAll();

    /**
     * 특정 상태의 모든 Interview를 조회합니다.
     *
     * @param status 면접 상태
     * @return 해당 상태의 Interview 리스트
     */
    List<Interview> findByStatus(InterviewStatus status);
}