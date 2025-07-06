package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewStatus;

import java.util.List;

/**
 * 순수 JPA 기반 Interview 저장소 인터페이스
 * - JpaRepository<Interview, Long>을 상속하여 기본 CRUD 메서드 제공
 * - Interview 조회·검증을 위한 커스텀 메서드 선언
 */
@Repository
public interface SpringDataInterviewRepository extends JpaRepository<Interview, Long> {

    /**
     * 랩실 ID로 Interview 리스트를 조회합니다.
     */
    List<Interview> findByLabId(Long labId);

    /**
     * 랩실 ID와 상태로 Interview 리스트를 조회합니다.
     */
    List<Interview> findByLabIdAndStatus(Long labId, InterviewStatus status);

    /**
     * 랩실 ID와 상태로 Interview 존재 여부를 확인합니다.
     */
    boolean existsByLabIdAndStatus(Long labId, InterviewStatus status);

    /**
     * 상태별 Interview 리스트를 조회합니다.
     */
    List<Interview> findByStatus(InterviewStatus status);

    /**
     * 특정 랩실의 활성화된 면접을 조회합니다.
     * - 편의 메서드로 자주 사용되는 조회 패턴
     */
    @Query("SELECT i FROM Interview i WHERE i.lab.id = :labId AND i.status = 'ACTIVE'")
    List<Interview> findActiveInterviewsByLabId(@Param("labId") Long labId);

    /**
     * 랩실별 면접 개수를 조회합니다.
     */
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.lab.id = :labId")
    long countByLabId(@Param("labId") Long labId);

    /**
     * 특정 랩실에 특정 상태의 면접이 있는지 확인합니다.
     * - existsByLabIdAndStatus와 동일하지만 명시적 쿼리로 구현
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Interview i WHERE i.lab.id = :labId AND i.status = :status")
    boolean hasInterviewWithStatus(@Param("labId") Long labId, @Param("status") InterviewStatus status);
}