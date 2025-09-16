package org.univ.rankus.application.service.auth;


import org.univ.rankus.application.port.in.command.EmailVerificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthTokens;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.in.command.AuthUseCase;
import org.univ.rankus.application.port.out.AuthTokenPort;
import org.univ.rankus.application.port.out.RefreshTokenRepositoryPort;
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
    private final RefreshTokenRepositoryPort refreshTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationUseCase emailVerificationUseCase;

    @Override
    @Transactional
    public AuthResponseDto login(UserLoginRequestDto request) {
        // 1) 이메일로 조회 → 없으면 404
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        // 2) 비밀번호 검증 → 틀리면 401
        if (!user.getPassword().matches(request.getPassword(), passwordEncoder)) {
            throw new UserValidationException(UserErrorCode.INVALID_CREDENTIALS);
        }
        // 3) Access Token, Refresh Token 생성
        AuthTokens tokens = authTokenPort.generateTokens(user);
        // 4) AuthResponseDto로 변환하여 반환
        return AuthResponseDto.from(tokens);
    }

    @Override
    public User signUp(UserRegisterRequestDto request) {
        // 1) 이메일 중복 검증 → 중복 시 400
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new UserValidationException(UserErrorCode.EMAIL_DUPLICATED);
        }
        // 2) 학번 중복 검증 → 중복 시 409
        if (userRepo.existsByStudentNumber(request.getStudentNumber())) {
            throw new UserValidationException(UserErrorCode.STUDENT_NUMBER_DUPLICATED);
        }
        // 3) 엔티티 생성·저장 (Password 검증 & 암호화 포함)
        User newUser = new User(
                request.getName(),
                request.getEmail(),
                Password.fromRaw(request.getPassword(), passwordEncoder),
                request.getStudentNumber(),
                request.getPhoneNumber(),
                request.getGrade(),
                request.getEnrollmentStatus()
        );
        userRepo.save(newUser);

        // 4) 이메일 인증 코드 발송
        emailVerificationUseCase.sendVerificationCode(newUser.getEmail());

        return newUser;
    }

    

    @Override
    public void logout(String userEmail, String accessToken) {
        try {
            // 1) 액세스 토큰을 블랙리스트에 등록
            java.time.LocalDateTime expiryTime = authTokenPort.getAccessTokenExpiryTime(accessToken);

            // JWT에서 토큰 ID 추출 (JTI)
            String tokenId = authTokenPort.extractTokenId(accessToken);
            if (tokenId != null) {
                authTokenPort.blacklistToken(tokenId, expiryTime);
            }

            // 2) 해당 사용자의 모든 리프레시 토큰 비활성화
            refreshTokenRepo.deactivateAllByUserEmail(userEmail);

        } catch (Exception e) {
            // 토큰 파싱 실패 등의 경우에도 리프레시 토큰은 비활성화
            refreshTokenRepo.deactivateAllByUserEmail(userEmail);
        }
    }
}
