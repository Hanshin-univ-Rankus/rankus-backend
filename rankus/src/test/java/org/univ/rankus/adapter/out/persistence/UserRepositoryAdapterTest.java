package org.univ.rankus.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.univ.rankus.domain.model.user.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepositoryAdapter 단위 테스트")
class UserRepositoryAdapterTest {

    @Mock
    private SpringDataUserRepository springRepo;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Nested
    @DisplayName("save 메서드 동작")
    class SaveTests {

        @Test
        @DisplayName("저장 시 SpringDataUserRepository.save가 호출되고 결과가 반환된다")
        void save_delegatesToSpringRepo() {
            User user = mock(User.class);
            when(springRepo.save(user)).thenReturn(user);

            User result = adapter.save(user);

            assertEquals(user, result);
            verify(springRepo).save(user);
        }
    }

    @Nested
    @DisplayName("findById 메서드 동작")
    class FindByIdTests {

        @Test
        @DisplayName("존재하는 ID 조회 시 Optional.of(User) 반환")
        void findById_exists() {
            Long id = 1L;
            User user = mock(User.class);
            when(springRepo.findById(id)).thenReturn(Optional.of(user));

            Optional<User> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertEquals(user, result.get());
            verify(springRepo).findById(id);
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 Optional.empty() 반환")
        void findById_notExists() {
            Long id = 2L;
            when(springRepo.findById(id)).thenReturn(Optional.empty());

            Optional<User> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(springRepo).findById(id);
        }
    }

    @Nested
    @DisplayName("findByEmail 메서드 동작")
    class FindByEmailTests {

        @Test
        @DisplayName("존재하는 이메일 조회 시 Optional.of(User) 반환")
        void findByEmail_exists() {
            String email = "test@univ.ac.kr";
            User user = mock(User.class);
            when(springRepo.findByEmail(email)).thenReturn(Optional.of(user));

            Optional<User> result = adapter.findByEmail(email);

            assertTrue(result.isPresent());
            assertEquals(user, result.get());
            verify(springRepo).findByEmail(email);
        }

        @Test
        @DisplayName("존재하지 않는 이메일 조회 시 Optional.empty() 반환")
        void findByEmail_notExists() {
            String email = "none@univ.ac.kr";
            when(springRepo.findByEmail(email)).thenReturn(Optional.empty());

            Optional<User> result = adapter.findByEmail(email);

            assertTrue(result.isEmpty());
            verify(springRepo).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("existsByEmail 메서드 동작")
    class ExistsByEmailTests {

        @Test
        @DisplayName("이미 존재하는 이메일이면 true 반환")
        void existsByEmail_true() {
            String email = "test@univ.ac.kr";
            when(springRepo.existsByEmail(email)).thenReturn(true);

            boolean result = adapter.existsByEmail(email);

            assertTrue(result);
            verify(springRepo).existsByEmail(email);
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 false 반환")
        void existsByEmail_false() {
            String email = "none@univ.ac.kr";
            when(springRepo.existsByEmail(email)).thenReturn(false);

            boolean result = adapter.existsByEmail(email);

            assertFalse(result);
            verify(springRepo).existsByEmail(email);
        }
    }
}
