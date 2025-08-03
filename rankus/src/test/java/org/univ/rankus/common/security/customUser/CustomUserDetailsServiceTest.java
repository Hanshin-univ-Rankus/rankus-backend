package org.univ.rankus.common.security.customUser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.UserStatus;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("ACTIVE 상태의 사용자는 정상적으로 조회된다")
    void loadUserByUsername_ActiveUser_Success() {
        // given
        User activeUser = DomainUserFactory.buildUserWithStatus(UserStatus.ACTIVE);
        String email = activeUser.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(activeUser));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("PENDING 상태의 사용자는 DisabledException을 던진다")
    void loadUserByUsername_PendingUser_ThrowsDisabledException() {
        // given
        User pendingUser = DomainUserFactory.buildUserWithStatus(UserStatus.PENDING);
        String email = pendingUser.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(pendingUser));

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(DisabledException.class)
                .hasMessage("이메일 인증이 완료되지 않았습니다.");
    }

    @Test
    @DisplayName("BANNED 상태의 사용자는 DisabledException을 던진다")
    void loadUserByUsername_BannedUser_ThrowsDisabledException() {
        // given
        User bannedUser = DomainUserFactory.buildUserWithStatus(UserStatus.BANNED);
        String email = bannedUser.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(bannedUser));

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(DisabledException.class)
                .hasMessage("비활성화된 계정입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 UsernameNotFoundException을 던진다")
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        // given
        String email = "notfound@hs.ac.kr";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: " + email);
    }
}
