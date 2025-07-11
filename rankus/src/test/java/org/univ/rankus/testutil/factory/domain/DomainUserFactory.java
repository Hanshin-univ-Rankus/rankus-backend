package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.lab.core.Lab;
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
        User user = buildValidUserWithRole(Role.PROFESSOR);
        ReflectionTestUtils.setField(user, "id", generateUserId());
        return user;
    }

    public static User buildStudentUser() {
        User user = buildValidUserWithRole(Role.STUDENT);
        ReflectionTestUtils.setField(user, "id", generateUserId());
        return user;
    }

    public static User buildLabLeaderUser() {
        User user = buildValidUserWithRole(Role.LAB_LEADER);
        ReflectionTestUtils.setField(user, "id", generateUserId());
        return user;
    }

    public static User buildLabManagerUser() {
        User user = buildValidUserWithRole(Role.LAB_MANAGER);
        ReflectionTestUtils.setField(user, "id", generateUserId());
        return user;
    }

    public static User buildLabMemberUser() {
        User user = buildValidUserWithRole(Role.LAB_MEMBER);
        ReflectionTestUtils.setField(user, "id", generateUserId());
        return user;
    }

    public static User buildCustomUser(String name, String email, String rawPassword) {
        return new User(name, email, Password.fromRaw(rawPassword, ENCODER));
    }

    public static User buildUserWithLab(Lab lab) {
        User user = buildValidUser();
        user.assignLab(lab);
        return user;
    }

    public static User buildUserWithLabAndRole(Lab lab, Role role) {
        User user = buildValidUserWithRole(role);
        user.assignLab(lab);
        return user;
    }

    public static User buildLabMemberWithLab(Lab lab) {
        User user = buildUserWithLabAndRole(lab, Role.LAB_MEMBER);
        ReflectionTestUtils.setField(user, "id", generateUserId());
        return user;
    }

    public static User buildLabLeaderWithLab(Lab lab) {
        User user = buildUserWithLabAndRole(lab, Role.LAB_LEADER);
        ReflectionTestUtils.setField(user, "id", generateUserId());
        return user;
    }

    public static User buildLabManagerWithLab(Lab lab) {
        User user = buildUserWithLabAndRole(lab, Role.LAB_MANAGER);
        ReflectionTestUtils.setField(user, "id", generateUserId());
        return user;
    }

    public static User buildProfessorWithLab(Lab lab) {
        return buildUserWithLabAndRole(lab, Role.PROFESSOR);
    }

    public static User buildAdminUser() {
        User user = buildValidUserWithRole(Role.ADMIN);
        ReflectionTestUtils.setField(user, "id", generateUserId());
        return user;
    }

    private static Long generateUserId() {
        return (long) (Math.random() * 1000000) + 1;
    }
}