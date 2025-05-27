package org.univ.rankus.application.service;

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
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.user.User;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 로그인 단위 테스트")
class UserServiceLoginTest {

    @Mock
    private UserRepositoryPort userRepo;

    @Mock
    private AuthTokenPort authTokenPort;

    @InjectMocks
    private UserService service;


    @Nested
    @DisplayName("login 성공 케이스")
    class Success {

        @Test
        @DisplayName("이메일/비밀번호가 맞으면 JWT 토큰이 발급된다")
        void login_success() {
            // given
            String email = "user@univ.ac.kr";
            String rawPassword = "password123";
            String fakeToken = "jwt.token.value";
            User user = new User("홍길동", email, rawPassword);

            given(userRepo.findByEmail(email)).willReturn(Optional.of(user));
            given(authTokenPort.generateToken(user)).willReturn(fakeToken);

            // when
            String result = service.login(email, rawPassword);

            // then
            assertEquals(fakeToken, result);
            then(userRepo).should().findByEmail(email);
            then(authTokenPort).should().generateToken(user);
        }
    }

    @Nested
    @DisplayName("login 실패 케이스")
    class Failure {

        @Test
        @DisplayName("존재하지 않는 이메일이면 NoSuchElementException이 발생한다")
        void login_emailNotFound_throws() {
            // given
            String email = "nope@univ.ac.kr";
            given(userRepo.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThrows(NoSuchElementException.class, () ->
                    service.login(email, "irrelevant")
            );
            then(userRepo).should().findByEmail(email);
            then(authTokenPort).should(never()).generateToken(any());
        }

        @Test
        @DisplayName("비밀번호가 틀리면 IllegalArgumentException이 발생한다")
        void login_wrongPassword_throws() {
            // given
            String email = "user@univ.ac.kr";
            String rawPassword = "password123";
            User user = new User("홍길동", email, rawPassword);

            given(userRepo.findByEmail(email)).willReturn(Optional.of(user));

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    service.login(email, "wrongPassword")
            );
            then(userRepo).should().findByEmail(email);
            then(authTokenPort).should(never()).generateToken(any());
        }
    }



    @Nested
    @DisplayName("login 입력 검증 실패 케이스")
    class ValidationFailure {

        @ParameterizedTest(name = "이메일이 빈값·null일 때: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = { "   " })
        @DisplayName("email이 null 또는 빈 문자열이면 IllegalArgumentException 발생")
        void login_invalidEmailInput_throws(String invalidEmail) {
            // given
            // userRepo.findByEmail 호출 전단계에서 막히길 기대
            // (실제 서비스에서 email 검증 로직이 없다면 NoSuchElementException이 날 수도 있습니다)

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    service.login(invalidEmail, "anyPassword")
            );
            then(userRepo).should(never()).findByEmail(any());
            then(authTokenPort).should(never()).generateToken(any());
        }

        @ParameterizedTest(name = "비밀번호가 빈값·null일 때: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = { "   " })
        @DisplayName("rawPassword가 null 또는 빈 문자열이면 IllegalArgumentException 발생")
        void login_invalidPasswordInput_throws(String invalidPwd) {
            // given
            String email = "user@univ.ac.kr";

            // 입력 검증에서 먼저 예외가 발생하므로 userRepo.findByEmail이 호출되지 않아야 함
            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    service.login(email, invalidPwd)
            );
            then(userRepo).should(never()).findByEmail(any());
            then(authTokenPort).should(never()).generateToken(any());
        }
    }

}
