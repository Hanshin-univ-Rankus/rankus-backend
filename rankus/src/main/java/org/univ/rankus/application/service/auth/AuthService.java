package org.univ.rankus.application.service.auth;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.in.web.dto.request.*;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.in.command.AuthUseCase;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.Password;
import org.univ.rankus.domain.model.user.PasswordEncoder;
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
    public AuthResponseDto login(UserLoginRequestDto request) {
        // 1) 이메일로 조회 → 없으면 404
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        // 2) 비밀번호 검증 → 틀리면 401
        if (!user.getPassword().matches(request.getPassword(), passwordEncoder)) {
            throw new UserValidationException(UserErrorCode.INVALID_CREDENTIALS);
        }
        // 3) 토큰 생성
        String token = authTokenPort.generateToken(user);
        // 4) 사용자 정보를 DTO로 변환
        UserResponseDto userDto = UserResponseDto.from(user);
        // 5) AuthResponseDto로 반환
        return AuthResponseDto.from(token, userDto);
    }

    @Override
    public User signUp(UserRegisterRequestDto request) {
        // 1) 이메일 중복 검증 → 중복 시 400
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new UserValidationException(UserErrorCode.EMAIL_DUPLICATED);
        }
        // 2) 엔티티 생성·저장 (Password 검증 & 암호화 포함)
        User newUser = new User(
                request.getName(),
                request.getEmail(),
                Password.fromRaw(request.getPassword(), passwordEncoder)
        );
        return userRepo.save(newUser);
    }
}