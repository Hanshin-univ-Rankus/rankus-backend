package org.univ.rankus.application.service;

import org.springframework.stereotype.Service;
import org.univ.rankus.application.port.in.UserQueryUseCase;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;

import java.util.NoSuchElementException;

@Service
public class UserQueryService implements UserQueryUseCase {
    private final UserRepositoryPort userRepo;

    public UserQueryService(UserRepositoryPort userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User getProfile(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 사용자가 없습니다: " + email));
    }
}

