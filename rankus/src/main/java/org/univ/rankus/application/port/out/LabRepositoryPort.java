package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.lab.core.Lab;

import java.util.List;
import java.util.Optional;

/**
 * Lab 도메인 퍼시스턴스 포트 인터페이스
 * - 서비스는 이 인터페이스만 통해서 Lab을 저장·조회합니다.
 */
public interface LabRepositoryPort {

    /**
     * 새로운 Lab을 저장하거나 수정합니다.
     *
     * @param lab 저장할 Lab 엔티티
     * @return 저장된 Lab (영속화 후 ID 포함)
     */
    Lab save(Lab lab);

    /**
     * ID로 Lab을 조회합니다.
     *
     * @param id 조회할 Lab ID
     * @return Optional.of(Lab) or Optional.empty()
     */
    Optional<Lab> findById(Long id);

    /**
     * 모든 Lab을 랭킹 내림차순으로 조회합니다.
     *
     * @return 랭킹 내림차순으로 정렬된 Lab 리스트
     */
    List<Lab> findAllByRankingDesc();

    /**
     * 특정 Lab을 삭제합니다.
     *
     * @param lab 삭제할 Lab 엔티티
     */
    void delete(Lab lab);
}