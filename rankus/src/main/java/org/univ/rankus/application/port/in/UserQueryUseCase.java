package org.univ.rankus.application.port.in;

import org.univ.rankus.domain.model.user.User;

public interface UserQueryUseCase {
    /**
     * 토큰에서 추출된 이메일을 기반으로
     * 로그인한 사용자 정보를 조회한다.
     */
    User getProfile(String email);
}
