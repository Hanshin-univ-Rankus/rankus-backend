package org.univ.rankus.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.univ.rankus.config.DomainConfig;
import org.univ.rankus.domain.model.user.User;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")  // application-test.yml 에 MySQL 설정이 있어야 합니다
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DomainConfig.class)
@DisplayName("SpringDataUserRepository MySQL 통합 테스트")
class SpringDataUserRepositoryTest {

    @Autowired
    private SpringDataLabRepository labRepo;

    @Autowired
    private SpringDataUserRepository userRepo;

    @Nested
    @DisplayName("저장 및 기본 조회")
    class SaveAndFindTests {

        @Test
        @DisplayName("User를 저장하면 ID가 생성되고 findById로 조회 가능하다")
        void saveAndFindById() {
            // given
            User user = new User("이철수", "lee@univ.ac.kr", "password123");

            // when
            User saved = userRepo.saveAndFlush(user);
            Optional<User> found = userRepo.findById(saved.getId());

            // then
            assertAll("기본 저장·조회",
                    () -> assertTrue(found.isPresent()),
                    () -> assertEquals("이철수", found.get().getName()),
                    () -> assertEquals("lee@univ.ac.kr", found.get().getEmail()),
                    () -> assertTrue(found.get().matchesPassword("password123")),
                    () -> assertNull(found.get().getLab(), "랩실이 설정되지 않았으므로 null이어야 함")
            );
        }

        @Test
        @DisplayName("lab이 null인 User도 저장할 수 있다")
        void save_nullLab() {
            // given
            User user = new User("김영희", "kim@univ.ac.kr", "securePass1");

            // when
            User saved = userRepo.saveAndFlush(user);
            Optional<User> found = userRepo.findById(saved.getId());

            // then
            assertTrue(found.isPresent());
            assertNull(found.get().getLab());
        }
    }

    @Nested
    @DisplayName("이메일 조회 및 존재 여부")
    class EmailLookupTests {

        @Test
        @DisplayName("findByEmail로 User 조회가 가능하다")
        void findByEmail_success() {
            // given
            userRepo.save(new User("박수민", "park@univ.ac.kr", "pass1234"));

            // when
            Optional<User> found = userRepo.findByEmail("park@univ.ac.kr");

            // then
            assertTrue(found.isPresent());
            assertEquals("박수민", found.get().getName());
        }

        @Test
        @DisplayName("existsByEmail로 중복 체크가 가능하다")
        void existsByEmail() {
            // given
            userRepo.save(new User("최민수", "choi@univ.ac.kr", "mypassword"));

            // when & then
            assertTrue(userRepo.existsByEmail("choi@univ.ac.kr"));
            assertFalse(userRepo.existsByEmail("nope@univ.ac.kr"));
        }
    }

    @Nested
    @DisplayName("삭제 동작")
    class UpdateAndDeleteTests {

        @Test
        @DisplayName("삭제 후 findById가 empty를 반환한다")
        void deleteUser_success() {
            // given
            User user = userRepo.save(new User("오지훈", "oh@univ.ac.kr", "delete123"));
            Long id = user.getId();

            // when
            userRepo.deleteById(id);

            // then
            assertTrue(userRepo.findById(id).isEmpty());
        }
    }

    @Nested
    @DisplayName("제약 조건 테스트")
    class ConstraintTests {

        @Test
        @DisplayName("중복 이메일 저장 시 DataIntegrityViolationException이 발생한다")
        void duplicateEmail_throws() {
            // given
            userRepo.save(new User("장민호", "jang@univ.ac.kr", "dup12345"));

            // when & then
            assertThrows(DataIntegrityViolationException.class, () ->
                    userRepo.saveAndFlush(new User("장민호2", "jang@univ.ac.kr", "dup67890"))
            );
        }
    }
}
