package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.univ.rankus.application.port.in.command.UserCommandUseCase;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

/**
 * User 도메인 서비스 구현체
 * - 트랜잭션: 쓰기 메서드(@Transactional), 읽기 메서드(readOnly=true)
 * - 도메인 예외 흐름에 맞춰 예외 던짐
 */

@Service
@RequiredArgsConstructor
@Transactional  // 쓰기 트랜잭션
public class UserCommandService implements UserCommandUseCase {

    private final UserRepositoryPort userRepo;

    @Override
    public void changeName(Long userId, String newName) {
        // 1) 사용자 조회 → 없으면 404
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
        // 2) 도메인 메서드로 이름 검증·변경
        user.changeName(newName);
        // 3) 저장 (Dirty Checking 으로도 가능)
        userRepo.save(user);
    }
}