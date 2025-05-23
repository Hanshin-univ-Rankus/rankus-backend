package org.univ.rankus.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.univ.rankus.domain.model.lab.Lab;

/**
 * Lab Entity에 대한 CRUD를 제공하는 Spring Data JPA 리포지토리
 */
public interface SpringDataLabRepository extends JpaRepository<Lab, Long> {
    // 추가 커스텀 쿼리 메서드는 여기에 선언
}
