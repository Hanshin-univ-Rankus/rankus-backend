package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.user.User;

import java.util.Optional;

/**
 * User 도메인 퍼시스턴스 포트 인터페이스
 * - 서비스는 이 인터페이스만 통해서 User를 저장·조회함
 */
public interface UserRepositoryPort {

    /**
     * 새로운 유저를 저장합니다.
     *
     * @param user 저장할 User 엔티티
     * @return 저장된 User (영속화 후 ID 포함)
     */
    User save(User user);

    /**
     * ID로 User를 조회합니다.
     *
     * @param id 조회할 User ID
     * @return Optional.of(User) or Optional.empty()
     */
    Optional<User> findById(Long id);

    /**
     * 이메일로 User를 조회합니다.
     *
     * @param email 조회할 이메일
     * @return Optional.of(User) or Optional.empty()
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 중복 여부를 확인합니다.
     *
     * @param email 체크할 이메일
     * @return 이미 존재하면 true
     */
    boolean existsByEmail(String email);

    /**
     * 학번 중복 여부를 확인합니다.
     *
     * @param studentNumber 체크할 학번
     * @return 이미 존재하면 true
     */
    boolean existsByStudentNumber(String studentNumber);

    /**
     * ID로 User를 삭제합니다.
     *
     * @param id 삭제할 User ID
     */
    void deleteById(Long id);
}