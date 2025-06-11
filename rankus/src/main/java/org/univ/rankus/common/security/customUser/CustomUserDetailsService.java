package org.univ.rankus.common.security.customUser;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;

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
        return new CustomUserDetails(user);
    }
}