package org.univ.rankus.application.port.in.command;

import org.univ.rankus.adapter.in.web.dto.request.ChangeNameRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.ChangePasswordRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserUpdateRequestDto;
import org.univ.rankus.domain.model.user.User;

public interface UserCommandUseCase {

    /**
     * 새로운 사용자 생성 (관리자용)
     */
    User createUser(UserCreateRequestDto request);

    /**
     * 사용자 정보 수정
     */
    User updateUser(Long userId, UserUpdateRequestDto request);

    /**
     * 회원 이름 변경
     */
    void changeName(Long userId, ChangeNameRequestDto request);

    /**
     * 비밀번호 변경
     */
    void changePassword(Long userId, ChangePasswordRequestDto request);

    /**
     * 사용자 삭제
     */
    void deleteUser(Long userId);
}