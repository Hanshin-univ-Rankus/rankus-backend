package org.univ.rankus.adapter.in.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.application.service.UserService;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService signUp 단위 테스트")
class UserServiceSignUpTest {

    @Mock
    private UserRepositoryPort userRepo;

    @Mock
    private SpringDataLabRepository labRepo;

    @Mock
    private AuthTokenPort authTokenPort;

    @InjectMocks
    private UserService service;

    private final String validName = "홍길동";
    private final String validEmail = "user@univ.ac.kr";
    private final String validPassword = "password123";

    @Nested
    @DisplayName("signUp 성공 시나리오")
    class Success {
        @Test
        @DisplayName("중복 이메일 없으면 User 생성 후 반환")
        void signUp_success() {
            // given
            given(userRepo.existsByEmail(validEmail)).willReturn(false);
            given(userRepo.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            User created = service.signUp(validName, validEmail, validPassword);

            // then
            assertAll("생성된 User 검증",
                    () -> assertEquals(validName, created.getName()),
                    () -> assertEquals(validEmail, created.getEmail()),
                    () -> assertTrue(created.matchesPassword(validPassword)),
                    () -> assertNull(created.getLab(), "랩실이 null로 설정되어야 함")
            );
            then(userRepo).should().existsByEmail(validEmail);
            then(userRepo).should().save(any(User.class));
            // labRepo는 호출되지 않아야 함
            then(labRepo).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("signUp 실패 시나리오")
    class Failure {

        @Test
        @DisplayName("중복 이메일이 있으면 IllegalStateException 발생")
        void signUp_duplicateEmail_throws() {
            // given
            given(userRepo.existsByEmail(validEmail)).willReturn(true);

            // when & then
            assertThrows(IllegalStateException.class, () ->
                    service.signUp(validName, validEmail, validPassword)
            );
            then(userRepo).should().existsByEmail(validEmail);
            then(userRepo).should(never()).save(any());
            // labRepo는 호출되지 않아야 함
            then(labRepo).shouldHaveNoInteractions();
        }

        @ParameterizedTest(name = "이름이 유효하지 않을 때: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("이름이 null 또는 빈 문자열이면 IllegalArgumentException 발생")
        void signUp_invalidName_throws(String invalidName) {
            // given
            given(userRepo.existsByEmail(validEmail)).willReturn(false);

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    service.signUp(invalidName, validEmail, validPassword)
            );
            then(userRepo).should().existsByEmail(validEmail);
            // labRepo는 호출되지 않아야 함
            then(labRepo).shouldHaveNoInteractions();
        }

        @ParameterizedTest(name = "이메일 포맷이 유효하지 않을 때: \"{0}\"")
        @ValueSource(strings = {"invalid", "user@.com", "user.com"})
        @DisplayName("유효하지 않은 이메일이면 IllegalArgumentException 발생")
        void signUp_invalidEmail_throws(String invalidEmail) {
            // given
            given(userRepo.existsByEmail(invalidEmail)).willReturn(false);

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    service.signUp(validName, invalidEmail, validPassword)
            );
            then(userRepo).should().existsByEmail(invalidEmail);
            // labRepo는 호출되지 않아야 함
            then(labRepo).shouldHaveNoInteractions();
        }

        @ParameterizedTest(name = "비밀번호가 유효하지 않을 때: \"{0}\"")
        @ValueSource(strings = {"short", "1234567"})
        @DisplayName("비밀번호가 8자 미만이면 IllegalArgumentException 발생")
        void signUp_shortPassword_throws(String shortPwd) {
            // given
            given(userRepo.existsByEmail(validEmail)).willReturn(false);

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    service.signUp(validName, validEmail, shortPwd)
            );
            then(userRepo).should().existsByEmail(validEmail);
            // labRepo는 호출되지 않아야 함
            then(labRepo).shouldHaveNoInteractions();
        }
    }
}
