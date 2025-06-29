package org.univ.rankus.testutil.mock;

import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.domain.model.user.Password;
import org.univ.rankus.domain.model.user.PasswordEncoder;
import org.univ.rankus.domain.model.user.User;

import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * AuthService 관련 공통 테스트 유틸리티 클래스입니다.
 * - 로그인/토큰 발급 모킹 로직을 재사용할 수 있습니다.
 */
public class AuthMockUtil {

    public static final String VALID_EMAIL = "user@example.com";
    public static final String RAW_PASSWORD = "password123";
    public static final String TOKEN = "jwt-token";

    /**
     * findByEmail(), getPassword().matches() 설정을 통해 mock User를 준비합니다.
     *
     * @param userRepo UserRepositoryPort mock
     * @param passwordEncoder PasswordEncoder mock
     * @param passwordMatches 비밀번호 일치 여부
     * @return 모킹된 User 객체
     */
    public static User mockExistingUser(UserRepositoryPort userRepo,
                                        PasswordEncoder passwordEncoder,
                                        boolean passwordMatches) {
        User mockUser = mock(User.class);
        Password mockPassword = mock(Password.class);
        
        when(userRepo.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(mockUser));
        when(mockUser.getPassword()).thenReturn(mockPassword);
        when(mockPassword.matches(eq(RAW_PASSWORD), eq(passwordEncoder))).thenReturn(passwordMatches);
        
        return mockUser;
    }

    /**
     * 토큰 생성 결과를 설정합니다.
     *
     * @param authTokenPort AuthTokenPort mock
     * @param user mock User
     */
    public static void configureToken(AuthTokenPort authTokenPort, User user) {
        when(authTokenPort.generateToken(user)).thenReturn(TOKEN);
    }

    /**
     * 비밀번호 불일치 시나리오를 위한 mock 설정입니다.
     */
    public static User mockInvalidPassword(UserRepositoryPort userRepo,
                                           PasswordEncoder passwordEncoder) {
        return mockExistingUser(userRepo, passwordEncoder, false);
    }

    /**
     * 이메일 미존재 시나리오를 위한 mock 설정입니다.
     *
     * @param userRepo UserRepositoryPort mock
     * @param email 조회할 이메일
     */
    public static void mockUserNotFound(UserRepositoryPort userRepo, String email) {
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());
    }
}