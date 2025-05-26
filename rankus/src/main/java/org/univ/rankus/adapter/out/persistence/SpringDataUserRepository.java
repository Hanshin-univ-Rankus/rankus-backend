// src/main/java/org/univ/rankus/adapter/out/persistence/SpringDataUserRepository.java
package org.univ.rankus.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.user.User;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     * @param email 조회할 이메일
     * @return 해당 이메일을 가진 User (없으면 Optional.empty())
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 중복 체크
     * @param email 체크할 이메일
     * @return 해당 이메일이 이미 존재하면 true
     */
    boolean existsByEmail(String email);
}
