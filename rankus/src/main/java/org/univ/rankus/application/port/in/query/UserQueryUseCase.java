package org.univ.rankus.application.port.in.query;

import org.univ.rankus.domain.model.user.User;



public interface UserQueryUseCase {
    /**
     * ID로 사용자 조회
     */
    User getUserById(Long userId);

    /**
     * 이메일로 사용자 조회
     */
    User getUserByEmail(String email);

}