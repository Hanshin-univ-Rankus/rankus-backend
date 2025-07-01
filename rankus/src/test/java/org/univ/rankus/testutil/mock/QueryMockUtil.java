package org.univ.rankus.testutil.mock;

import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.User;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * QueryService 계층 테스트에서
 * findById / findByEmail 모킹 로직을 재사용하기 위한 헬퍼 클래스
 */
public class QueryMockUtil {

    /**
     * ID로 조회 성공 시 mock User를 생성하고 반환, findById stub 설정
     */
    public static User mockExistingUserById(UserRepositoryPort repo, Long userId) {
        User user = mock(User.class);
        when(repo.findById(userId)).thenReturn(Optional.of(user));
        return user;
    }

    /**
     * ID로 조회 실패 시 Optional.empty() 반환 stub 설정
     */
    public static void mockUserNotFoundById(UserRepositoryPort repo, Long userId) {
        when(repo.findById(userId)).thenReturn(Optional.empty());
    }

    /**
     * 이메일로 조회 성공 시 mock User를 생성하고 반환, findByEmail stub 설정
     */
    public static User mockExistingUserByEmail(UserRepositoryPort repo, String email) {
        User user = mock(User.class);
        when(repo.findByEmail(email)).thenReturn(Optional.of(user));
        return user;
    }

    /**
     * 이메일로 조회 실패 시 Optional.empty() 반환 stub 설정
     */
    public static void mockUserNotFoundByEmail(UserRepositoryPort repo, String email) {
        when(repo.findByEmail(email)).thenReturn(Optional.empty());
    }
}