package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.user.User;

import java.util.Optional;

/**
 * 순수 JPA 기반 User 저장소
 * - JpaRepository 에서 기본 CRUD 메서드를 모두 제공
 * - 이메일로 조회하는 커스텀 메서드는 아래에 선언
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 User 조회 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 중복 체크
     */
    boolean existsByEmail(String email);

    /**
     * 학번 중복 체크
     */
    boolean existsByStudentNumber(String studentNumber);
}