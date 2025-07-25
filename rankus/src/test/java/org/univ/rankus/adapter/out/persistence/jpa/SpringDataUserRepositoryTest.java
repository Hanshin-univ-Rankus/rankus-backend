package org.univ.rankus.adapter.out.persistence.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.univ.rankus.adapter.out.persistence.impl.UserRepositoryAdapter;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.config.BaseRepositoryTest;
import org.univ.rankus.testutil.factory.integration.IntegrationUserFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import({UserRepositoryAdapter.class})
@DisplayName("Spring Data JPA UserRepository 통합 테스트")
class SpringDataUserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepositoryPort userRepo;

    @Test
    @DisplayName("save: User 저장 후 ID 자동 생성")
    void save_generatesId() {
        // when
        User saved = IntegrationUserFactory.persistValidUser(userRepo);
        // then
        assertNotNull(saved.getId(), "저장된 User는 ID가 자동 생성되어야 한다");
        assertTrue(saved.getId() > 0, "생성된 ID는 양수여야 한다");
    }

    @Test
    @DisplayName("findByEmail: 존재하는 이메일 조회 성공")
    void findByEmail_existingEmail_returnsOptional() {
        // given
        User saved = IntegrationUserFactory.persistValidUser(userRepo);
        // when
        Optional<User> found = userRepo.findByEmail(saved.getEmail());
        // then
        assertTrue(found.isPresent(), "저장된 이메일로 조회하면 Optional이 non-empty여야 한다");
        assertEquals(saved.getEmail(), found.get().getEmail(), "조회된 User의 이메일이 일치해야 한다");
    }

    @Test
    @DisplayName("findByEmail: 존재하지 않는 이메일 조회 시 Optional.empty() 반환")
    void findByEmail_nonExistingEmail_returnsEmpty() {
        // when
        Optional<User> found = userRepo.findByEmail("no_such_user@hs.ac.kr");
        // then
        assertTrue(found.isEmpty(), "존재하지 않는 이메일 조회 시 빈 Optional을 반환해야 한다");
    }

    @Test
    @DisplayName("existsByEmail: 저장된 이메일에 대해 true 반환")
    void existsByEmail_existingEmail_returnsTrue() {
        // given
        User saved = IntegrationUserFactory.persistValidUser(userRepo);
        // when & then
        assertTrue(userRepo.existsByEmail(saved.getEmail()), "저장된 이메일에 대해 existsByEmail은 true를 반환해야 한다");
    }

    @Test
    @DisplayName("existsByEmail: 미존재 이메일에 대해 false 반환")
    void existsByEmail_nonExistingEmail_returnsFalse() {
        // when & then
        assertFalse(userRepo.existsByEmail("no_such_user@hs.ac.kr"), "미존재 이메일에 대해 existsByEmail은 false를 반환해야 한다");
    }
}