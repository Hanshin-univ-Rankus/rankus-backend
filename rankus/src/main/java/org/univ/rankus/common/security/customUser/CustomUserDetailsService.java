package org.univ.rankus.common.security.customUser;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.UserStatus;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepositoryPort userRepo;

    public CustomUserDetailsService(UserRepositoryPort userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (user.getStatus() == UserStatus.PENDING) {
            throw new DisabledException("이메일 인증이 완료되지 않았습니다.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("비활성화된 계정입니다.");
        }

        return new CustomUserDetails(user);
    }
}