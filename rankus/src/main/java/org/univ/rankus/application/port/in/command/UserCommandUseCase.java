package org.univ.rankus.application.port.in.command;

import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;

public interface UserCommandUseCase {
    
    /**
     * 새로운 사용자 생성 (관리자용)
     */
    User createUser(String name, String email, String rawPassword, Role role);
    
    /**
     * 사용자 정보 수정
     */
    User updateUser(Long userId, String name, String email, Role role);
    
    /**
     * 회원 이름 변경
     */
    void changeName(Long userId, String newName);
    
    /**
     * 비밀번호 변경
     */
    void changePassword(Long userId, String currentPassword, String newPassword);
    
    /**
     * 사용자 삭제
     */
    void deleteUser(Long userId);
}