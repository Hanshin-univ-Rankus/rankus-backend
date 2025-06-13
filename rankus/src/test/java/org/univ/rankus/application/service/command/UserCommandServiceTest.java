package org.univ.rankus.application.service.command;


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
import org.univ.rankus.domain.model.user.exception.UserValidationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserRepositoryPort userRepo;

    @InjectMocks
    private UserCommandService userService;

    /**
     * helper: 주어진 ID로 조회되는 mock User를 준비합니다.
     */
    private User givenExistingUser(Long userId) {
        User mockUser = mock(User.class);
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        return mockUser;
    }

    @Nested
    @DisplayName("changeName 메서드는")
    class ChangeNameTests {

        @Test
        @DisplayName("정상적으로 이름을 변경하고 저장한다")
        void changeNameSuccess() {
            // given
            Long userId = 1L;
            String newName = "김철수";
            User mockUser = givenExistingUser(userId);

            // when
            userService.changeName(userId, newName);

            // then
            verify(userRepo).findById(userId);
            verify(mockUser).changeName(newName);
            verify(userRepo).save(mockUser);
        }

        @Test
        @DisplayName("존재하지 않는 유저 ID 전달 시 UserNotFoundException을 던진다")
        void changeNameUserNotFound() {
            // given
            Long userId = 99L;
            when(userRepo.findById(userId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.changeName(userId, "어느누구"))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });

            verify(userRepo).findById(userId);
            verify(userRepo, never()).save(any());
        }

        @Test
        @DisplayName("null 또는 빈 문자열을 새 이름으로 전달 시 UserValidationException(NAME_REQUIRED)을 던진다")
        void changeNameBlankName() {
            // given
            Long userId = 2L;
            User mockUser = givenExistingUser(userId);

            // 도메인 changeName 내부에서 예외 발생하도록 모킹
            doThrow(new UserValidationException(UserErrorCode.NAME_REQUIRED))
                    .when(mockUser).changeName(null);

            // when & then
            assertThatThrownBy(() -> userService.changeName(userId, null))
                    .isInstanceOf(UserValidationException.class)
                    .satisfies(ex -> {
                        UserValidationException e = (UserValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(UserErrorCode.NAME_REQUIRED);
                    });

            verify(mockUser).changeName(null);
            verify(userRepo, never()).save(any());
        }

        @Test
        @DisplayName("30자를 초과하는 새 이름을 전달 시 UserValidationException(NAME_TOO_LONG)을 던진다")
        void changeNameTooLong() {
            // given
            Long userId = 3L;
            String longName = "가".repeat(31); // 31자
            User mockUser = givenExistingUser(userId);

            doThrow(new UserValidationException(UserErrorCode.NAME_TOO_LONG))
                    .when(mockUser).changeName(longName);

            // when & then
            assertThatThrownBy(() -> userService.changeName(userId, longName))
                    .isInstanceOf(UserValidationException.class)
                    .satisfies(ex -> {
                        UserValidationException e = (UserValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(UserErrorCode.NAME_TOO_LONG);
                    });

            verify(mockUser).changeName(longName);
            verify(userRepo, never()).save(any());
        }
    }
}