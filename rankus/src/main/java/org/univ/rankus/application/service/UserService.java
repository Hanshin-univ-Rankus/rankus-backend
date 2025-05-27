package org.univ.rankus.application.service;

import org.springframework.stereotype.Service;
import org.univ.rankus.application.port.in.UserUseCase;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;
import org.univ.rankus.domain.model.user.User;

import java.util.NoSuchElementException;

@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepo;
    private final SpringDataLabRepository labRepo;
    private final AuthTokenPort authTokenPort;

    public UserService(UserRepositoryPort userRepo,
                       SpringDataLabRepository labRepo,
                       AuthTokenPort authTokenPort) {
        this.userRepo       = userRepo;
        this.labRepo        = labRepo;
        this.authTokenPort  = authTokenPort;
    }


    /**
     * 회원가입 로직:
     * 1) 이메일 중복 확인 → 중복 시 IllegalStateException
     * 2) User 생성 → 도메인 생성자에서 필드 방어로 IllegalArgumentException 처리
     * 3) 저장 → 저장된 User 반환
     */
    @Override
    public User signUp(String name, String email, String rawPassword) {
        // 1) 중복 이메일 검사
        if (userRepo.existsByEmail(email)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다: " + email);
        }

        // 비밀번호 유효성 검사: 8자 이상
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        // 2) User 생성 (도메인에서 유효성 검사)
        User user = new User(name, email, rawPassword);

        // 3) 저장 및 반환
        return userRepo.save(user);
    }

    /**
     * 로그인 로직:
     * 1) 이메일로 User 조회 → 없으면 예외
     * 2) 비밀번호 일치 확인 → 틀리면 예외
     * 3) 토큰 발급 및 반환
     */
    @Override
    public String login(String email, String rawPassword) {
        // 입력값 검증 추가
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 이메일입니다: " + email));

        if (!user.matchesPassword(rawPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 메서드명 변경: issueToken → generateToken
        return authTokenPort.generateToken(user);
    }
}