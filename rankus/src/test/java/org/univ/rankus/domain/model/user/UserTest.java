package org.univ.rankus.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserValidationException;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;
import org.univ.rankus.testutil.mock.TestPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 도메인 단위 테스트")
class UserTest {

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("비밀번호 null 입력 시 INVALID_CREDENTIALS 예외 발생")
        void constructor_nullPassword_throwsInvalidCredentials() {
            // given
            String name = "홍길동";
            String email = "test@hs.ac.kr";
            Password pwd = null;

            // when & then
            UserValidationException ex = assertThrows(
                    UserValidationException.class,
                    () -> new User(name, email, pwd, "20201001", "010-1234-5678", 3, EnrollmentStatus.ENROLLED)
            );

            assertEquals(UserErrorCode.PASSWORD_REQUIRED, ex.getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] name=''{0}'' → NAME_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("이름이 null 또는 blank일 때 NAME_REQUIRED 예외 발생")
        void constructor_nullOrBlankName_throwsNameRequired(String name) {
            // given
            Password pwd = Password.fromRaw("Password!23", new TestPasswordEncoder());

            // when & then
            UserValidationException ex = assertThrows(
                    UserValidationException.class,
                    () -> new User(name, "a@hs.ac.kr", pwd, "20201001", "010-1234-5678", 3, EnrollmentStatus.ENROLLED)
            );
            assertEquals(UserErrorCode.NAME_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("이름 길이 30 초과 시 NAME_TOO_LONG 예외 발생")
        void constructor_nameTooLong_throwsNameTooLong() {
            // given
            String longName = "a".repeat(31);
            Password pwd = Password.fromRaw("Password!23", new TestPasswordEncoder());

            // when & then
            UserValidationException ex = assertThrows(
                    UserValidationException.class,
                    () -> new User(longName, "a@hs.ac.kr", pwd, "20201001", "010-1234-5678", 3, EnrollmentStatus.ENROLLED)
            );
            assertEquals(UserErrorCode.NAME_TOO_LONG, ex.getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] email=''{0}'' → EMAIL_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("이메일이 null 또는 blank일 때 EMAIL_REQUIRED 예외 발생")
        void constructor_nullOrBlankEmail_throwsEmailRequired(String email) {
            // given
            Password pwd = Password.fromRaw("Password!23", new TestPasswordEncoder());

            // when & then
            UserValidationException ex = assertThrows(
                    UserValidationException.class,
                    () -> new User("홍길동", email, pwd, "20201001", "010-1234-5678", 3, EnrollmentStatus.ENROLLED)
            );
            assertEquals(UserErrorCode.EMAIL_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("잘못된 이메일 형식 시 EMAIL_INVALID 예외 발생")
        void constructor_invalidEmail_throwsEmailInvalid() {
            // when & then
            UserValidationException ex = assertThrows(
                    UserValidationException.class,
                    DomainUserFactory::buildInvalidUser_Email
            );
            assertEquals(UserErrorCode.EMAIL_INVALID, ex.getErrorCode());
        }

        @Test
        @DisplayName("유효한 입력 시 trim 및 기본 필드 설정")
        void constructor_validInput_setsFields() {
            // given
            String name = " 홍길동 ";
            String email = " test@hs.ac.kr ";
            Password pwd = Password.fromRaw("Password!23", new TestPasswordEncoder());

            // when
            User user = new User(name, email, pwd, "20201001", "010-1234-5678", 3, EnrollmentStatus.ENROLLED);

            // then
            assertNull(user.getId(), "생성 전에는 ID가 null이어야 한다");
            assertEquals("홍길동", user.getName());
            assertEquals("test@hs.ac.kr", user.getEmail());
            assertEquals("20201001", user.getStudentNumber());
            assertEquals("010-1234-5678", user.getPhoneNumber());
            assertEquals(3, user.getGrade());
            assertEquals(EnrollmentStatus.ENROLLED, user.getEnrollmentStatus());
            assertEquals(Role.STUDENT, user.getRole());
            assertTrue(user.checkPassword("Password!23", new TestPasswordEncoder()));
        }
    }

    @Nested
    @DisplayName("암호 검증(checkPassword)")
    class CheckPasswordTests {

        @ParameterizedTest(name = "[{index}] raw=''{0}'' → false")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("raw가 null 또는 blank일 경우 false 반환")
        void nullOrBlankRaw_returnsFalse(String raw) {
            // given
            User user = DomainUserFactory.buildValidUser();

            // when
            boolean result = user.checkPassword(raw, new TestPasswordEncoder());

            // then
            assertFalse(result);
        }

        @Test
        @DisplayName("올바른 raw일 경우 true 반환")
        void correctRaw_returnsTrue() {
            // given
            User user = DomainUserFactory.buildValidUser();
            String raw = "Password!23";

            // when
            boolean result = user.checkPassword(raw, new TestPasswordEncoder());

            // then
            assertTrue(result);
        }

        @Test
        @DisplayName("잘못된 raw일 경우 false 반환")
        void incorrectRaw_returnsFalse() {
            // given
            User user = DomainUserFactory.buildValidUser();

            // when
            boolean result = user.checkPassword("WrongPass", new TestPasswordEncoder());

            // then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("이름 변경(changeName)")
    class ChangeNameTests {

        @ParameterizedTest(name = "[{index}] newName=''{0}'' → NAME_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void changeName_nullOrBlank_throwsNameRequired(String newName) {
            // given
            User user = DomainUserFactory.buildValidUser();

            // when & then
            UserValidationException ex = assertThrows(
                    UserValidationException.class,
                    () -> user.changeName(newName)
            );
            assertEquals(UserErrorCode.NAME_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("newName 길이 30 초과 시 NAME_TOO_LONG 예외 발생")
        void changeName_tooLong_throwsNameTooLong() {
            // given
            User user = DomainUserFactory.buildValidUser();
            String longName = "a".repeat(31);

            // when & then
            UserValidationException ex = assertThrows(
                    UserValidationException.class,
                    () -> user.changeName(longName)
            );
            assertEquals(UserErrorCode.NAME_TOO_LONG, ex.getErrorCode());
        }

        @Test
        @DisplayName("유효한 newName 시 trim 및 변경")
        void changeName_valid_setsName() {
            // given
            User user = DomainUserFactory.buildValidUser();
            String newName = " NewName ";

            // when
            user.changeName(newName);

            // then
            assertEquals("NewName", user.getName());
        }
    }

    @Nested
    @DisplayName("역할 변경(changeRole)")
    class ChangeRoleTests {

        @Test
        @DisplayName("유효한 Role로 변경 가능")
        void changeRole_valid_setsRole() {
            // given
            User user = DomainUserFactory.buildValidUser();

            // when
            user.changeRole(Role.LAB_MANAGER);

            // then
            assertEquals(Role.LAB_MANAGER, user.getRole());
        }

        @Test
        @DisplayName("null Role 입력 시 UserValidationException 예외 발생")
        void changeRole_null_throwsException() {
            // given
            User user = DomainUserFactory.buildValidUser();

            // when & then
            assertThrows(
                    UserValidationException.class,
                    () -> user.changeRole(null)
            );
        }
    }

    @Nested
    @DisplayName("지원서 관리 권한 확인(canManageLabApplications)")
    class CanManageLabApplicationsTests {

        @Test
        @DisplayName("LAB_LEADER 역할이고 해당 랩실에 소속된 경우 권한 있음")
        void labLeader_inSameLab_hasPermission() {
            // given
            User user = DomainUserFactory.buildValidUser();
            user.changeRole(Role.LAB_LEADER);
            // Lab 객체를 Mock 대신 실제 객체로 생성
            Lab lab = createTestLab(1L);
            user.assignLab(lab);

            // when
            boolean result = user.canManageLabApplications(lab);

            // then
            assertTrue(result);
        }

        @Test
        @DisplayName("LAB_MANAGER 역할이고 해당 랩실에 소속되어도 권한 없음")
        void labManager_inSameLab_hasNoPermission() {
            // given
            User user = DomainUserFactory.buildValidUser();
            user.changeRole(Role.LAB_MANAGER);
            Lab lab = createTestLab(1L);
            user.assignLab(lab);

            // when
            boolean result = user.canManageLabApplications(lab);

            // then
            assertFalse(result);
        }

        @Test
        @DisplayName("PROFESSOR 역할이고 해당 랩실에 소속된 경우 권한 있음")
        void professor_inSameLab_hasPermission() {
            // given
            User user = DomainUserFactory.buildValidUser();
            user.changeRole(Role.PROFESSOR);
            Lab lab = createTestLab(1L);
            user.assignLab(lab);

            // when
            boolean result = user.canManageLabApplications(lab);

            // then
            assertTrue(result);
        }

        @Test
        @DisplayName("STUDENT 역할인 경우 권한 없음")
        void student_hasNoPermission() {
            // given
            User user = DomainUserFactory.buildValidUser(); // 기본 STUDENT 역할
            Lab lab = createTestLab(1L);
            user.assignLab(lab);

            // when
            boolean result = user.canManageLabApplications(lab);

            // then
            assertFalse(result);
        }

        @Test
        @DisplayName("LAB_LEADER지만 다른 랩실에 소속된 경우 권한 없음")
        void labLeader_inDifferentLab_hasNoPermission() {
            // given
            User user = DomainUserFactory.buildValidUser();
            user.changeRole(Role.LAB_LEADER);
            Lab userLab = createTestLab(1L);
            Lab targetLab = createTestLab(2L);
            user.assignLab(userLab);

            // when
            boolean result = user.canManageLabApplications(targetLab);

            // then
            assertFalse(result);
        }

        @Test
        @DisplayName("랩실에 소속되지 않은 경우 권한 없음")
        void userNotInLab_hasNoPermission() {
            // given
            User user = DomainUserFactory.buildValidUser();
            user.changeRole(Role.LAB_LEADER);
            Lab lab = createTestLab(1L);
            // user는 어느 랩실에도 소속되지 않음

            // when
            boolean result = user.canManageLabApplications(lab);

            // then
            assertFalse(result);
        }

        @Test
        @DisplayName("ADMIN 역할은 모든 랩실의 지원서 관리 권한이 있음")
        void admin_hasPermissionForAllLabs() {
            // given
            User admin = DomainUserFactory.buildValidUser();
            admin.changeRole(Role.ADMIN);
            Lab lab = createTestLab(1L);
            // admin은 어느 랩실에도 소속되지 않아도 됨

            // when
            boolean result = admin.canManageLabApplications(lab);

            // then
            assertTrue(result);
        }

        @Test
        @DisplayName("ADMIN 역할은 랩실 소속 여부와 관계없이 권한이 있음")
        void admin_hasPermissionRegardlessOfLabMembership() {
            // given
            User admin = DomainUserFactory.buildValidUser();
            admin.changeRole(Role.ADMIN);
            Lab userLab = createTestLab(1L);
            Lab targetLab = createTestLab(2L);
            admin.assignLab(userLab); // 다른 랩실에 소속

            // when
            boolean result = admin.canManageLabApplications(targetLab);

            // then
            assertTrue(result);
        }

        private Lab createTestLab(Long id) {
            // Lab 객체 생성을 위한 헬퍼 메서드 - 실제 구현에 맞게 수정 필요
            try {
                // Lab 클래스의 실제 생성자나 팩토리 메서드를 사용
                java.lang.reflect.Constructor<Lab> constructor = Lab.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                Lab lab = constructor.newInstance();

                // ID 설정을 위한 리플렉션
                java.lang.reflect.Field idField = Lab.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(lab, id);

                return lab;
            } catch (Exception e) {
                throw new RuntimeException("테스트용 Lab 객체 생성 실패", e);
            }
        }
    }

    @Nested
    @DisplayName("투표 권한 확인 메서드")
    class VotePermissionTests {

        @Nested
        @DisplayName("투표 조회 권한(canViewVotes)")
        class CanViewVotesTests {

            @Test
            @DisplayName("ADMIN은 모든 랩실의 투표 조회 권한이 있음")
            void admin_canViewAllVotes() {
                // given
                User admin = DomainUserFactory.buildAdminUser();
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);

                // when & then
                assertTrue(admin.canViewVotes(lab));
            }

            @Test
            @DisplayName("PROFESSOR는 모든 랩실의 투표 조회 권한이 있음")
            void professor_canViewAllVotes() {
                // given
                User professor = DomainUserFactory.buildProfessorUser();
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);

                // when & then
                assertTrue(professor.canViewVotes(lab));
            }

            @Test
            @DisplayName("LAB_MEMBER는 소속 랩실의 투표 조회 권한이 있음")
            void labMember_canViewOwnLabVotes() {
                // given
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);
                User labMember = DomainUserFactory.buildLabMemberWithLab(lab);

                // when & then
                assertTrue(labMember.canViewVotes(lab));
            }

            @Test
            @DisplayName("LAB_MANAGER는 소속 랩실의 투표 조회 권한이 있음")
            void labManager_canViewOwnLabVotes() {
                // given
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);
                User labManager = DomainUserFactory.buildLabManagerWithLab(lab);

                // when & then
                assertTrue(labManager.canViewVotes(lab));
            }

            @Test
            @DisplayName("LAB_LEADER는 소속 랩실의 투표 조회 권한이 있음")
            void labLeader_canViewOwnLabVotes() {
                // given
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);
                User labLeader = DomainUserFactory.buildLabLeaderWithLab(lab);

                // when & then
                assertTrue(labLeader.canViewVotes(lab));
            }

            @Test
            @DisplayName("다른 랩실 소속 사용자는 투표 조회 권한이 없음")
            void differentLabMember_cannotViewVotes() {
                // given
                Lab lab1 = DomainLabFactory.buildValidLabWithId(1L);
                Lab lab2 = DomainLabFactory.buildValidLabWithId(2L);
                User labMember = DomainUserFactory.buildLabMemberWithLab(lab1);

                // when & then
                assertFalse(labMember.canViewVotes(lab2));
            }

            @Test
            @DisplayName("랩실에 소속되지 않은 사용자는 투표 조회 권한이 없음")
            void unassignedUser_cannotViewVotes() {
                // given
                User student = DomainUserFactory.buildStudentUser();
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);

                // when & then
                assertFalse(student.canViewVotes(lab));
            }
        }

        @Nested
        @DisplayName("투표 생성 권한(canCreateVotes)")
        class CanCreateVotesTests {

            @Test
            @DisplayName("ADMIN은 모든 랩실의 투표 생성 권한이 있음")
            void admin_canCreateAllVotes() {
                // given
                User admin = DomainUserFactory.buildAdminUser();
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);

                // when & then
                assertTrue(admin.canCreateVotes(lab));
            }

            @Test
            @DisplayName("PROFESSOR는 모든 랩실의 투표 생성 권한이 있음")
            void professor_canCreateAllVotes() {
                // given
                User professor = DomainUserFactory.buildProfessorUser();
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);

                // when & then
                assertTrue(professor.canCreateVotes(lab));
            }

            @Test
            @DisplayName("LAB_MANAGER는 소속 랩실의 투표 생성 권한이 있음")
            void labManager_canCreateOwnLabVotes() {
                // given
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);
                User labManager = DomainUserFactory.buildLabManagerWithLab(lab);

                // when & then
                assertTrue(labManager.canCreateVotes(lab));
            }

            @Test
            @DisplayName("LAB_LEADER는 소속 랩실의 투표 생성 권한이 있음")
            void labLeader_canCreateOwnLabVotes() {
                // given
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);
                User labLeader = DomainUserFactory.buildLabLeaderWithLab(lab);

                // when & then
                assertTrue(labLeader.canCreateVotes(lab));
            }

            @Test
            @DisplayName("LAB_MEMBER는 투표 생성 권한이 없음")
            void labMember_cannotCreateVotes() {
                // given
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);
                User labMember = DomainUserFactory.buildLabMemberWithLab(lab);

                // when & then
                assertFalse(labMember.canCreateVotes(lab));
            }

            @Test
            @DisplayName("다른 랩실 소속 매니저는 투표 생성 권한이 없음")
            void differentLabManager_cannotCreateVotes() {
                // given
                Lab lab1 = DomainLabFactory.buildValidLabWithId(1L);
                Lab lab2 = DomainLabFactory.buildValidLabWithId(2L);
                User labManager = DomainUserFactory.buildLabManagerWithLab(lab1);

                // when & then
                assertFalse(labManager.canCreateVotes(lab2));
            }
        }

        @Nested
        @DisplayName("투표 관리 권한(canManageVotes)")
        class CanManageVotesTests {

            @Test
            @DisplayName("ADMIN은 모든 랩실의 투표 관리 권한이 있음")
            void admin_canManageAllVotes() {
                // given
                User admin = DomainUserFactory.buildAdminUser();
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);

                // when & then
                assertTrue(admin.canManageVotes(lab));
            }

            @Test
            @DisplayName("PROFESSOR는 모든 랩실의 투표 관리 권한이 있음")
            void professor_canManageAllVotes() {
                // given
                User professor = DomainUserFactory.buildProfessorUser();
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);

                // when & then
                assertTrue(professor.canManageVotes(lab));
            }

            @Test
            @DisplayName("LAB_MANAGER는 소속 랩실의 투표 관리 권한이 있음")
            void labManager_canManageOwnLabVotes() {
                // given
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);
                User labManager = DomainUserFactory.buildLabManagerWithLab(lab);

                // when & then
                assertTrue(labManager.canManageVotes(lab));
            }

            @Test
            @DisplayName("LAB_LEADER는 소속 랩실의 투표 관리 권한이 있음")
            void labLeader_canManageOwnLabVotes() {
                // given
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);
                User labLeader = DomainUserFactory.buildLabLeaderWithLab(lab);

                // when & then
                assertTrue(labLeader.canManageVotes(lab));
            }

            @Test
            @DisplayName("LAB_MEMBER는 투표 관리 권한이 없음")
            void labMember_cannotManageVotes() {
                // given
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);
                User labMember = DomainUserFactory.buildLabMemberWithLab(lab);

                // when & then
                assertFalse(labMember.canManageVotes(lab));
            }

            @Test
            @DisplayName("다른 랩실 소속 리더는 투표 관리 권한이 없음")
            void differentLabLeader_cannotManageVotes() {
                // given
                Lab lab1 = DomainLabFactory.buildValidLabWithId(1L);
                Lab lab2 = DomainLabFactory.buildValidLabWithId(2L);
                User labLeader = DomainUserFactory.buildLabLeaderWithLab(lab1);

                // when & then
                assertFalse(labLeader.canManageVotes(lab2));
            }

            @Test
            @DisplayName("랩실에 소속되지 않은 사용자는 투표 관리 권한이 없음")
            void unassignedUser_cannotManageVotes() {
                // given
                User student = DomainUserFactory.buildStudentUser();
                Lab lab = DomainLabFactory.buildValidLabWithId(1L);

                // when & then
                assertFalse(student.canManageVotes(lab));
            }
        }
    }
}