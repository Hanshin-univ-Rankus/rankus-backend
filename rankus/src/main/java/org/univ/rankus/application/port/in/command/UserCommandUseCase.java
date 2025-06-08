package org.univ.rankus.application.port.in.command;

public interface UserCommandUseCase {
    /**
     * 회원 이름 변경
     */
    void changeName(Long userId, String newName);
}