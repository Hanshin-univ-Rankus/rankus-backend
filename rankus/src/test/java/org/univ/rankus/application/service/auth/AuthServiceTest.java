package org.univ.rankus.application.service.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.PasswordEncoder;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.domain.model.user.exception.UserValidationException;
import org.univ.rankus.testutil.mock.AuthMockUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * AuthService의 login, signUp 메서드 단위 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepo;

    @Mock
    private AuthTokenPort authTokenPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("login 메서드는")
    class LoginTests {

        @Test
        @DisplayName("정상 로그인 시 토큰을 반환한다")
        void loginSuccess() {
            // given: 사용자 존재 + 비밀번호 일치 + 토큰 설정
            User user = AuthMockUtil.mockExistingUser(userRepo, passwordEncoder, true);
            AuthMockUtil.configureToken(authTokenPort, user);

            // when: 서비스 호출
            UserLoginRequestDto request = UserLoginRequestDto.builder()
                    .email(AuthMockUtil.VALID_EMAIL)
                    .password(AuthMockUtil.RAW_PASSWORD)
                    .build();
            AuthResponseDto response = authService.login(request);

            // then: AuthResponseDto 리턴 및 포트 호출 검증
            assertThat(response.getToken()).isEqualTo(AuthMockUtil.TOKEN);
            assertThat(response.getUser().getName()).isEqualTo(user.getName());
            assertThat(response.getUser().getEmail()).isEqualTo(user.getEmail());
            verify(userRepo).findByEmail(AuthMockUtil.VALID_EMAIL);
            verify(user).getPassword();
            verify(authTokenPort).generateToken(user);
        }

        @Test
        @DisplayName("등록되지 않은 이메일로 로그인 시 UserNotFoundException을 던진다")
        void loginUserNotFound() {
            // given: 이메일 미존재
            AuthMockUtil.mockUserNotFound(userRepo, "noone@example.com");

            // when & then: 예외 및 에러코드 검증
            UserLoginRequestDto request = UserLoginRequestDto.builder()
                    .email("noone@example.com")
                    .password(AuthMockUtil.RAW_PASSWORD)
                    .build();
            assertThatThrownBy(() ->
                    authService.login(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .satisfies(ex -> {
                        UserNotFoundException e = (UserNotFoundException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("비밀번호 불일치 시 UserValidationException을 던진다")
        void loginInvalidPassword() {
            // given: 사용자 존재 + 비밀번호 불일치
            AuthMockUtil.mockInvalidPassword(userRepo, passwordEncoder);

            // when & then: 예외 및 에러코드 검증
            UserLoginRequestDto request = UserLoginRequestDto.builder()
                    .email(AuthMockUtil.VALID_EMAIL)
                    .password(AuthMockUtil.RAW_PASSWORD)
                    .build();
            assertThatThrownBy(() ->
                    authService.login(request))
                    .isInstanceOf(UserValidationException.class)
                    .satisfies(ex -> {
                        UserValidationException e = (UserValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(UserErrorCode.INVALID_CREDENTIALS);
                    });
        }
    }

    @Nested
    @DisplayName("signUp 메서드는")
    class SignUpTests {

        @Test
        @DisplayName("정상 회원가입 시 User를 저장하고 반환한다")
        void signUpSuccess() {
            // given: 이메일 중복 없음
            String name = "홍길동";
            String email = "new@example.com";
            when(userRepo.existsByEmail(email)).thenReturn(false);

            // 비밀번호 해시 생성 설정
            String hashed = "hashedSecret";
            when(passwordEncoder.encode(AuthMockUtil.RAW_PASSWORD))
                    .thenReturn(hashed);

            // save 호출 시 전달된 User 엔티티 그대로 반환
            when(userRepo.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when: 서비스 호출
            UserRegisterRequestDto request = UserRegisterRequestDto.builder()
                    .name(name)
                    .email(email)
                    .password(AuthMockUtil.RAW_PASSWORD)
                    .build();
            User saved = authService.signUp(request);

            // then: 반환된 User 필드 검증 및 포트 호출 검증
            assertThat(saved.getName()).isEqualTo(name);
            assertThat(saved.getEmail()).isEqualTo(email);
            // Password VO에서 해시 값을 꺼내 비교는 별도 Password 테스트에서 확인
            assertThat(saved.getPassword()).isNotNull();

            verify(userRepo).existsByEmail(email);
            verify(passwordEncoder).encode(AuthMockUtil.RAW_PASSWORD);
            verify(userRepo).save(any(User.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 UserValidationException을 던진다")
        void signUpDuplicateEmail() {
            // given: 이메일 중복
            String email = "exist@example.com";
            when(userRepo.existsByEmail(email)).thenReturn(true);

            // when & then: 예외 및 에러코드 검증
            UserRegisterRequestDto request = UserRegisterRequestDto.builder()
                    .name("anyName")
                    .email(email)
                    .password(AuthMockUtil.RAW_PASSWORD)
                    .build();
            assertThatThrownBy(() ->
                    authService.signUp(request))
                    .isInstanceOf(UserValidationException.class)
                    .satisfies(ex -> {
                        UserValidationException e = (UserValidationException) ex;
                        assertThat(e.getErrorCode())
                                .isEqualTo(UserErrorCode.EMAIL_DUPLICATED);
                    });

            verify(userRepo).existsByEmail(email);
            verify(userRepo, never()).save(any());
        }
    }
}