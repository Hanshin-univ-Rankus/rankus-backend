package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.user.Password;
import org.univ.rankus.domain.model.user.PasswordEncoder;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.mock.TestPasswordEncoder;

import java.util.UUID;

/**
 * DomainUserFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 User 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainUserFactory {
    private static final PasswordEncoder ENCODER = new TestPasswordEncoder();

    private DomainUserFactory() {
    }

    public static User buildValidUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        return new User(
                "User-" + uid,
                uid + "@example.com",
                Password.fromRaw("Password!23", ENCODER)
        );
    }

    public static User buildValidUserWithId(Long id) {
        User u = buildValidUser();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    public static User buildInvalidUser_Name() {
        return new User("", "user@example.com", Password.fromRaw("Password!23", ENCODER));
    }

    public static User buildInvalidUser_Email() {
        return new User("NoEmailUser", "invalid-email", Password.fromRaw("Password!23", ENCODER));
    }

    public static User buildValidUserWithRole(Role role) {
        User u = buildValidUser();
        u.changeRole(role);
        return u;
    }

    public static User buildProfessorUser() {
        return buildValidUserWithRole(Role.PROFESSOR);
    }

    public static User buildStudentUser() {
        return buildValidUserWithRole(Role.STUDENT);
    }

    public static User buildLabLeaderUser() {
        return buildValidUserWithRole(Role.LAB_LEADER);
    }

    public static User buildLabManagerUser() {
        return buildValidUserWithRole(Role.LAB_MANAGER);
    }

    public static User buildLabMemberUser() {
        return buildValidUserWithRole(Role.LAB_MEMBER);
    }

    public static User buildCustomUser(String name, String email, String rawPassword) {
        return new User(name, email, Password.fromRaw(rawPassword, ENCODER));
    }
}