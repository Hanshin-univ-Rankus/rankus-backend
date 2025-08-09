package org.univ.rankus.application.service.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthTokens;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.in.command.EmailVerificationUseCase;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.PasswordEncoder;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.UserStatus;
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

    @Mock
    private EmailVerificationUseCase emailVerificationUseCase;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("login 메서드는")
    class LoginTests {

        @Test
        @DisplayName("정상 로그인 시 토큰을 반환한다")
        void loginSuccess() {
            // given: 사용자 존재 + 비밀번호 일치
            User user = AuthMockUtil.mockExistingUser(userRepo, passwordEncoder, true);

            // given: generateTokens가 mock AuthTokens 객체를 반환하도록 설정
            AuthTokens mockTokens = mock(AuthTokens.class);
            UserResponseDto mockUserDto = mock(UserResponseDto.class);

            when(authTokenPort.generateTokens(user)).thenReturn(mockTokens);
            when(mockTokens.getAccessToken()).thenReturn("mock-access-token");
            when(mockTokens.getRefreshToken()).thenReturn("mock-refresh-token");
            when(mockTokens.getUser()).thenReturn(mockUserDto);

            // when: 서비스 호출
            UserLoginRequestDto request = UserLoginRequestDto.builder()
                    .email(AuthMockUtil.VALID_EMAIL)
                    .password(AuthMockUtil.RAW_PASSWORD)
                    .build();
            AuthResponseDto response = authService.login(request);

            // then: AuthResponseDto 리턴 및 포트 호출 검증
            assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
            assertThat(response.getUser()).isEqualTo(mockUserDto);

            verify(userRepo).findByEmail(AuthMockUtil.VALID_EMAIL);
            verify(user).getPassword();
            verify(authTokenPort).generateTokens(user); // generateTokens 호출을 검증
        }

        @Test
        @DisplayName("등록되지 않은 이메일로 로그인 시 UserNotFoundException을 던진다")
        void loginUserNotFound() {
            // given: 이메일 미존재
            AuthMockUtil.mockUserNotFound(userRepo, "noone@hs.ac.kr");

            // when & then: 예외 및 에러코드 검증
            UserLoginRequestDto request = UserLoginRequestDto.builder()
                    .email("noone@hs.ac.kr")
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
        @DisplayName("정상 회원가입 시 User를 PENDING 상태로 저장하고 인증 메일을 발송한다")
        void signUpSuccess() {
            // given: 이메일 중복 없음
            String name = "홍길동";
            String email = "new@hs.ac.kr";
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
                    .studentNumber("20201001")
                    .phoneNumber("010-1234-5678")
                    .grade(3)
                    .enrollmentStatus(org.univ.rankus.domain.model.user.EnrollmentStatus.ENROLLED)
                    .build();
            authService.signUp(request);

            // then: User 저장 및 이메일 발송 검증
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepo).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getName()).isEqualTo(name);
            assertThat(savedUser.getEmail()).isEqualTo(email);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING);

            verify(emailVerificationUseCase).sendVerificationCode(email);
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 UserValidationException을 던진다")
        void signUpDuplicateEmail() {
            // given: 이메일 중복
            String email = "exist@hs.ac.kr";
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
            verify(emailVerificationUseCase, never()).sendVerificationCode(anyString());
        }
    }
}
