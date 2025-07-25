package org.univ.rankus.application.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.testutil.mock.QueryMockUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserRepositoryPort userRepo;

    @InjectMocks
    private UserQueryService userService;

    @Nested
    @DisplayName("getUserById 메서드는")
    class GetUserByIdTests {

        @Test
        @DisplayName("존재하는 ID인 경우 User를 반환한다")
        void getUserByIdSuccess() {
            // given
            Long userId = 1L;
            User mockUser = QueryMockUtil.mockExistingUserById(userRepo, userId);

            // when
            User result = userService.getUserById(userId);

            // then
            assertThat(result).isSameAs(mockUser);
            verify(userRepo).findById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 ID인 경우 UserNotFoundException을 던진다")
        void getUserByIdNotFound() {
            // given
            Long userId = 2L;
            QueryMockUtil.mockUserNotFoundById(userRepo, userId);

            // when & then
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepo).findById(userId);
        }
    }

    @Nested
    @DisplayName("getUserByEmail 메서드는")
    class GetUserByEmailTests {

        @Test
        @DisplayName("존재하는 이메일인 경우 User를 반환한다")
        void getUserByEmailSuccess() {
            // given
            String email = "user@hs.ac.kr";
            User mockUser = QueryMockUtil.mockExistingUserByEmail(userRepo, email);

            // when
            User result = userService.getUserByEmail(email);

            // then
            assertThat(result).isSameAs(mockUser);
            verify(userRepo).findByEmail(email);
        }

        @Test
        @DisplayName("존재하지 않는 이메일인 경우 UserNotFoundException을 던진다")
        void getUserByEmailNotFound() {
            // given
            String email = "notfound@hs.ac.kr";
            QueryMockUtil.mockUserNotFoundByEmail(userRepo, email);

            // when & then
            assertThatThrownBy(() -> userService.getUserByEmail(email))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepo).findByEmail(email);
        }
    }
}