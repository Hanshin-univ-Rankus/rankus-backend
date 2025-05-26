package org.univ.rankus.application.service;

import org.springframework.stereotype.Service;
import org.univ.rankus.application.port.in.UserUseCase;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;
import org.univ.rankus.domain.model.lab.Lab;
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
     * 2) labId로 Lab 조회 → 없으면 NoSuchElementException
     * 3) User 생성 → 도메인 생성자에서 필드 방어로 IllegalArgumentException 처리
     * 4) 저장 → 저장된 User 반환
     */
    @Override
    public User signUp(String name, String email, String rawPassword, Long labId) {
        // 1) 중복 이메일 검사
        if (userRepo.existsByEmail(email)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다: " + email);
        }

        // 2) 랩실 조회 (없으면 404 시나리오로 NoSuchElementException)
        Lab lab = labRepo.findById(labId)
                .orElseThrow(() ->
                        new NoSuchElementException("존재하지 않는 랩실입니다: " + labId)
                );

        // 3) 도메인 엔티티 생성 (여기서 name/email/password 검증)
        User user = new User(name, email, rawPassword, lab);

        // 4) 저장 및 반환
        return userRepo.save(user);
    }

    @Override
    public String login(String email, String rawPassword) {
        // 0) 입력 검증: 이메일·비밀번호 null·빈값 방어
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email은 필수입니다.");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("password는 필수입니다.");
        }

        // 1) 사용자 조회
        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new NoSuchElementException("해당 이메일의 사용자가 없습니다: " + email)
                );

        // 2) 비밀번호 검증
        if (!user.matchesPassword(rawPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3) 토큰 발급
        return authTokenPort.generateToken(user);
    }
}
