// src/main/java/org/univ/rankus/adapter/out/persistence/UserRepositoryAdapter.java
package org.univ.rankus.adapter.out.persistence;

import org.springframework.stereotype.Repository;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;

import java.util.Optional;

/**
 * UserRepositoryPort 어댑터 구현체
 * - Spring Data JPA 리포지토리를 내부에서 호출해 저장·조회 기능을 위임한다.
 */
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springRepo;

    public UserRepositoryAdapter(SpringDataUserRepository springRepo) {
        this.springRepo = springRepo;
    }

    /**
     * User 저장
     * @param user 저장할 User 엔티티
     * @return 저장된 User (영속화 후 id 포함)
     */
    @Override
    public User save(User user) {
        return springRepo.save(user);
    }

    /**
     * ID로 User 조회
     * @param id 조회할 User ID
     * @return Optional.of(User) or Optional.empty()
     */
    @Override
    public Optional<User> findById(Long id) {
        return springRepo.findById(id);
    }

    /**
     * 이메일로 User 조회
     * @param email 조회할 이메일
     * @return Optional.of(User) or Optional.empty()
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return springRepo.findByEmail(email);
    }

    /**
     * 이메일 중복 체크
     * @param email 체크할 이메일
     * @return 이미 존재하면 true
     */
    @Override
    public boolean existsByEmail(String email) {
        return springRepo.existsByEmail(email);
    }
}
