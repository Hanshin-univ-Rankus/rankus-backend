package org.univ.rankus.application.service.auth;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.AuthUseCase;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.Password;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.domain.model.user.exception.UserValidationException;

@Service
@RequiredArgsConstructor
@Transactional  // signUp은 쓰기, login은 readOnly로 오버라이드
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepo;
    private final AuthTokenPort authTokenPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public String login(String email, String rawPassword) {
        // 1) 이메일로 조회 → 없으면 404
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        // 2) 비밀번호 검증 → 틀리면 401
        if (!user.checkPassword(rawPassword, passwordEncoder)) {
            throw new UserValidationException(UserErrorCode.INVALID_CREDENTIALS);
        }
        // 3) 토큰 생성 후 반환
        return authTokenPort.generateToken(user);
    }

    @Override
    public User signUp(String name, String email, String rawPassword) {
        // 1) 이메일 중복 검증 → 중복 시 400
        if (userRepo.existsByEmail(email)) {
            throw new UserValidationException(UserErrorCode.EMAIL_DUPLICATED);
        }
        // 2) Password 도메인에서 검증 & 암호화
        Password password = Password.fromRaw(rawPassword, passwordEncoder);
        // 3) 엔티티 생성·저장
        User newUser = new User(name, email, password);
        return userRepo.save(newUser);
    }
}