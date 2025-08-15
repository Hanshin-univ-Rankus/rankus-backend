package org.univ.rankus.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.application.port.out.BlacklistedTokenRepositoryPort;
import org.univ.rankus.application.port.out.RefreshTokenRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.user.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    // Unused mocks, but needed for the provider's constructor
    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock
    private BlacklistedTokenRepositoryPort blacklistedTokenRepository;
    @Mock
    private UserRepositoryPort userRepository;

    private User adminUser;
    private User studentUser;

    // A simple stub for the PasswordEncoder interface
    private static class TestPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(String rawPassword) {
            return new StringBuilder(rawPassword).reverse().toString();
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", "c3ByaW5nYm9vdC1qd3QtdHV0b3JpYWwtc3ByaW5nYm9vdC1qd3QtdHV0b3JpYWwK");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessExpirationMs", 3600000L); // 1 hour

        adminUser = mock(User.class);
        when(adminUser.getEmail()).thenReturn("admin@hs.ac.kr");
        when(adminUser.getRole()).thenReturn(Role.ADMIN);

        studentUser = mock(User.class);
        when(studentUser.getEmail()).thenReturn("student@hs.ac.kr");
        when(studentUser.getRole()).thenReturn(Role.STUDENT);
        // Also mock the CustomUserDetails which needs a non-mock user to be created
        User realStudentUser = new User(
                "학생",
                "student@hs.ac.kr",
                Password.fromRaw("password123", new TestPasswordEncoder()),
                "20210002",
                "010-8765-4321",
                2,
                EnrollmentStatus.ENROLLED
        );
        ReflectionTestUtils.setField(realStudentUser, "id", 2L);
    }

    @Test
    @DisplayName("액세스 토큰 생성 시 role 클레임이 정확히 포함된다")
    void generateAccessToken_Should_IncludeRoleClaim() {
        // when
        String token = jwtTokenProvider.generateToken(adminUser);
        Jws<Claims> claims = jwtTokenProvider.parseClaims(token);

        // then
        assertThat(claims.getBody().getSubject()).isEqualTo("admin@hs.ac.kr");
        assertThat(claims.getBody().get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("getAuthentication 호출 시 DB가 아닌 토큰의 role 클레임으로 권한을 생성한다")
    void getAuthentication_Should_CreateAuthoritiesFromTokenClaim() {
        // given
        User realStudentUser = new User(
                "학생",
                "student@hs.ac.kr",
                Password.fromRaw("password123", new TestPasswordEncoder()),
                "20210002",
                "010-8765-4321",
                2,
                EnrollmentStatus.ENROLLED
        );
        ReflectionTestUtils.setField(realStudentUser, "id", 2L);

        CustomUserDetails userDetailsFromDb = new CustomUserDetails(realStudentUser);
        when(userDetailsService.loadUserByUsername("admin@hs.ac.kr")).thenReturn(userDetailsFromDb);

        // 하지만 토큰에는 ADMIN 역할이 들어있음
        String adminToken = jwtTokenProvider.generateToken(adminUser);

        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(adminToken, userDetailsService);

        // then
        // Principal은 DB에서 조회한 UserDetails여야 함 (studentUser 정보)
        assertThat(authentication.getPrincipal()).isEqualTo(userDetailsFromDb);
        // 권한은 DB의 ROLE_STUDENT가 아닌, 토큰에 있던 ROLE_ADMIN 이어야 함
        assertThat(authentication.getAuthorities())
                .map(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }
}