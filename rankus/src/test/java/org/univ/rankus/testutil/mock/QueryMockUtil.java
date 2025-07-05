package org.univ.rankus.testutil.mock;

import org.univ.rankus.application.port.out.LabApplicationRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * QueryService 계층 테스트에서
 * findById / findByEmail 모킹 로직을 재사용하기 위한 헬퍼 클래스
 */
public class QueryMockUtil {

    // ===== User Repository Mock Helpers =====

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

    // ===== Lab Repository Mock Helpers =====

    /**
     * ID로 조회 성공 시 mock Lab을 생성하고 반환, findById stub 설정
     */
    public static Lab mockExistingLabById(LabRepositoryPort repo, Long labId) {
        Lab lab = mock(Lab.class);
        when(repo.findById(labId)).thenReturn(Optional.of(lab));
        return lab;
    }

    /**
     * ID로 조회 실패 시 Optional.empty() 반환 stub 설정
     */
    public static void mockLabNotFoundById(LabRepositoryPort repo, Long labId) {
        when(repo.findById(labId)).thenReturn(Optional.empty());
    }

    // LabRepositoryPort에 findByName, existsById 메서드가 없으므로 제거됨

    // ===== LabApplication Repository Mock Helpers =====

    /**
     * ID로 조회 성공 시 mock LabApplication을 생성하고 반환, findById stub 설정
     */
    public static LabApplication mockExistingLabApplicationById(LabApplicationRepositoryPort repo, Long applicationId) {
        LabApplication application = mock(LabApplication.class);
        when(repo.findById(applicationId)).thenReturn(Optional.of(application));
        return application;
    }

    /**
     * ID로 조회 실패 시 Optional.empty() 반환 stub 설정
     */
    public static void mockLabApplicationNotFoundById(LabApplicationRepositoryPort repo, Long applicationId) {
        when(repo.findById(applicationId)).thenReturn(Optional.empty());
    }

    /**
     * 랩실 ID와 사용자 ID로 중복 지원 확인 stub 설정
     */
    public static void mockDuplicateApplication(LabApplicationRepositoryPort repo, Long labId, Long userId, boolean exists) {
        when(repo.existsByLabIdAndUserId(labId, userId)).thenReturn(exists);
    }

    // LabApplicationRepositoryPort에 existsById, findByUserId 메서드가 없으므로 제거됨

    /**
     * 랩실 ID로 조회 성공 시 빈 리스트 반환 stub 설정 (기본값)
     */
    public static void mockApplicationsByLabId(LabApplicationRepositoryPort repo, Long labId) {
        when(repo.findByLabId(labId)).thenReturn(java.util.Collections.emptyList());
    }
}