package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.univ.rankus.domain.model.lab.Lab;

/**
 * 순수 JPA 기반 Lab 저장소
 * - JpaRepository<Lab, Long>을 상속하면 기본 CRUD 메서드가 모두 제공됩니다.
 * - findAllByRankingDesc() 메서드는 아래와 같이 커스텀으로 정의할 수도 있지만,
 *   Sort.by("ranking").descending()을 서비스 단에서 직접 전달해도 무방합니다.
 */
public interface SpringDataLabRepository extends JpaRepository<Lab, Long> {

    /**
     * 랭킹 내림차순으로 모든 Lab을 조회하는 커스텀 메서드 예시
     * (만약 서비스 단에서 Sort 객체를 전달하지 않으려면 이 메서드를 사용)
     */
    default java.util.List<Lab> findAllByRankingDesc() {
        return findAll(Sort.by(Sort.Direction.DESC, "ranking"));
    }
}