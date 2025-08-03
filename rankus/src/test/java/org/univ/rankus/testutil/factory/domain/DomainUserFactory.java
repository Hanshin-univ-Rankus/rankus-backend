package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.*;
import org.univ.rankus.testutil.mock.TestPasswordEncoder;


/**
 * DomainUserFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 User 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainUserFactory {
    private static final PasswordEncoder ENCODER = new TestPasswordEncoder();

    private DomainUserFactory() {
    }

    private static long nameCounter = 1L;

    public static User buildValidUser() {
        String suffix = String.valueOf(nameCounter++);
        return new User(
                "TestUser" + suffix,
                "testuser" + suffix + "@hs.ac.kr",
                Password.fromRaw("Password!23", ENCODER),
                "2020100" + suffix,
                "010-1234-567" + suffix,
                3,
                EnrollmentStatus.ENROLLED
        );
    }

    public static User buildValidUserWithId(Long id) {
        User user = buildValidUser();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static User buildUserWithStatus(UserStatus status) {
        User user = buildValidUser();
        ReflectionTestUtils.setField(user, "status", status);
        return user;
    }

    public static User buildInvalidUser_Name() {
        return new User("", "user@hs.ac.kr", Password.fromRaw("Password!23", ENCODER),
                "20201001", "010-1234-5678", 3, EnrollmentStatus.ENROLLED);
    }

    public static User buildInvalidUser_Email() {
        return new User("NoEmailUser", "invalid-email", Password.fromRaw("Password!23", ENCODER),
                "20201001", "010-1234-5678", 3, EnrollmentStatus.ENROLLED);
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
        String suffix = String.valueOf(nameCounter++);
        return new User(name, email, Password.fromRaw(rawPassword, ENCODER),
                "2020100" + suffix, "010-1234-567" + suffix, 3, EnrollmentStatus.ENROLLED);
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

    private static long sequentialIdCounter = 1000L;

    private static Long generateUserId() {
        return sequentialIdCounter++;
    }
}