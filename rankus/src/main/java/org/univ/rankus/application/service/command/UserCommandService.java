package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.UserCommandUseCase;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.PasswordEncoder;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.domain.model.user.exception.UserValidationException;

/**
 * User 도메인 서비스 구현체
 * - 트랜잭션: 쓰기 메서드(@Transactional), 읽기 메서드(readOnly=true)
 * - 도메인 예외 흐름에 맞춰 예외 던짐
 */

@Service
@RequiredArgsConstructor
@Transactional  // 쓰기 트랜잭션
public class UserCommandService implements UserCommandUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(String name, String email, String rawPassword, Role role) {
        // 1. 이메일 중복 검증
        if (userRepositoryPort.existsByEmail(email)) {
            throw new UserValidationException(UserErrorCode.EMAIL_DUPLICATED);
        }
        
        // 2. 도메인 객체 생성
        User user = User.create(name, email, rawPassword, role, passwordEncoder);
        
        // 3. 저장
        return userRepositoryPort.save(user);
    }

    @Override
    public User updateUser(Long userId, String name, String email, Role role) {
        // 1. 사용자 조회
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        
        // 2. 이메일 변경시 중복 검증 (현재 사용자 제외)
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepositoryPort.existsByEmail(email)) {
                throw new UserValidationException(UserErrorCode.EMAIL_DUPLICATED);
            }
        }
        
        // 3. 도메인 메서드로 정보 변경
        if (name != null) {
            user.changeName(name);
        }
        if (email != null) {
            user.changeEmail(email);
        }
        if (role != null) {
            user.changeRole(role);
        }
        
        // 4. 저장
        return userRepositoryPort.save(user);
    }

    @Override
    public void changeName(Long userId, String newName) {
        // 1. 사용자 조회 → 없으면 404
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        // 2. 도메인 메서드로 이름 검증·변경
        user.changeName(newName);
        // 3. 저장 (Dirty Checking 으로도 가능)
        userRepositoryPort.save(user);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        // 1. 사용자 조회
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        
        // 2. 현재 비밀번호 확인
        if (!user.getPassword().matches(currentPassword, passwordEncoder)) {
            throw new UserValidationException(UserErrorCode.INVALID_CREDENTIALS);
        }
        
        // 3. 새 비밀번호로 변경
        user.changePassword(newPassword, passwordEncoder);
        
        // 4. 저장
        userRepositoryPort.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        // 1. 사용자 존재 확인
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        
        // 2. 삭제 가능 여부 검증 (비즈니스 로직)
        // 예: 활성 상태의 랩실 지원이 있는지, 랩 리더인지 등 확인
        // 현재는 단순 삭제로 구현
        
        // 3. 삭제
        userRepositoryPort.deleteById(userId);
    }
}