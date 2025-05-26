package org.univ.rankus.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.univ.rankus.domain.model.lab.LabApplication;

import java.util.List;

/**
 * LabApplication 엔티티에 대한 Spring Data JPA 리포지토리
 */
public interface SpringDataLabApplicationRepository
        extends JpaRepository<LabApplication, Long> {

    /**
     * 주어진 랩실 ID에 등록된 모든 가입신청을 반환한다.
     */
    List<LabApplication> findByLabId(Long labId);
}