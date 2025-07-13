package org.univ.rankus.common.security.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.univ.rankus.application.port.in.AttendanceSessionQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.attendance.AttendanceSession;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainAttendanceFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceSessionPermissionHandler 테스트")
class AttendanceSessionPermissionHandlerTest {

    @Mock
    private AttendanceSessionQueryUseCase attendanceSessionQueryUseCase;

    @Mock
    private LabPromotionQueryUseCase labPromotionQueryUseCase;

    @Mock
    private UserQueryUseCase userQueryUseCase;

    @InjectMocks
    private AttendanceSessionPermissionHandler handler;

    private CustomUserDetails principal;
    private static final Long USER_ID = 10L;
    private static final Long SESSION_ID = 100L;
    private static final Long LAB_ID = 1L;

    @BeforeEach
    void setUp() {
        principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(USER_ID);
    }

    @Nested
    @DisplayName("targetType 메서드는")
    class TargetTypeTests {

        @Test
        @DisplayName("AttendanceSession을 반환한다")
        void returnsAttendanceSession() {
            // when
            String targetType = handler.targetType();

            // then
            assertThat(targetType).isEqualTo("AttendanceSession");
        }
    }

    @Nested
    @DisplayName("hasPermission 메서드는")
    class HasPermissionTests {

        @Test
        @DisplayName("VIEW 권한: 랩실 출석 조회 권한이 있으면 허용")
        void viewPermission_withLabViewAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canViewLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, SESSION_ID, "VIEW");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("VIEW 권한: 랩실 출석 조회 권한이 없으면 거부")
        void viewPermission_withoutLabViewAttendancePermission_denied() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canViewLabAttendance(mockLab)).willReturn(false);

            // when
            boolean allowed = handler.hasPermission(principal, SESSION_ID, "VIEW");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("MANAGE 권한: 랩실 출석 관리 권한이 있으면 허용")
        void managePermission_withLabManageAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, SESSION_ID, "MANAGE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("MANAGE 권한: 랩실 출석 관리 권한이 없으면 거부")
        void managePermission_withoutLabManageAttendancePermission_denied() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(false);

            // when
            boolean allowed = handler.hasPermission(principal, SESSION_ID, "MANAGE");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("CREATE 권한: 랩실 출석 관리 권한이 있으면 허용")
        void createPermission_withLabManageAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, SESSION_ID, "CREATE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("UPDATE 권한: 랩실 출석 관리 권한이 있으면 허용")
        void updatePermission_withLabManageAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, SESSION_ID, "UPDATE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("DELETE 권한: 랩실 출석 관리 권한이 있으면 허용")
        void deletePermission_withLabManageAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, SESSION_ID, "DELETE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("알 수 없는 권한에 대해서는 거부")
        void unknownPermission_denied() {
            // given
            User mockUser = DomainUserFactory.buildValidUserWithId(USER_ID);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);

            // when
            boolean allowed = handler.hasPermission(principal, SESSION_ID, "UNKNOWN");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("잘못된 principal 타입이면 거부")
        void invalidPrincipalType_denied() {
            // when
            boolean allowed = handler.hasPermission("invalid", SESSION_ID, "VIEW");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("잘못된 targetId 타입이면 거부")
        void invalidTargetIdType_denied() {
            // when
            boolean allowed = handler.hasPermission(principal, "invalid", "VIEW");

            // then
            assertThat(allowed).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPermissionForLab 메서드는")
    class HasPermissionForLabTests {

        @Test
        @DisplayName("VIEW_ATTENDANCE 권한: 랩실 출석 조회 권한이 있으면 허용")
        void viewAttendancePermission_withLabViewAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(mockLab);
            given(mockUser.canViewLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermissionForLab(principal, LAB_ID, "VIEW_ATTENDANCE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("VIEW_ATTENDANCE 권한: 랩실 출석 조회 권한이 없으면 거부")
        void viewAttendancePermission_withoutLabViewAttendancePermission_denied() {
            // given
            User mockUser = mock(User.class);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(mockLab);
            given(mockUser.canViewLabAttendance(mockLab)).willReturn(false);

            // when
            boolean allowed = handler.hasPermissionForLab(principal, LAB_ID, "VIEW_ATTENDANCE");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("MANAGE_ATTENDANCE 권한: 랩실 출석 관리 권한이 있으면 허용")
        void manageAttendancePermission_withLabManageAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermissionForLab(principal, LAB_ID, "MANAGE_ATTENDANCE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("MANAGE_ATTENDANCE 권한: 랩실 출석 관리 권한이 없으면 거부")
        void manageAttendancePermission_withoutLabManageAttendancePermission_denied() {
            // given
            User mockUser = mock(User.class);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(false);

            // when
            boolean allowed = handler.hasPermissionForLab(principal, LAB_ID, "MANAGE_ATTENDANCE");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("알 수 없는 권한에 대해서는 거부")
        void unknownPermissionForLab_denied() {
            // when
            boolean allowed = handler.hasPermissionForLab(principal, LAB_ID, "UNKNOWN");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("잘못된 principal 타입이면 거부")
        void invalidPrincipalTypeForLab_denied() {
            // when
            boolean allowed = handler.hasPermissionForLab("invalid", LAB_ID, "VIEW_ATTENDANCE");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("잘못된 labId 타입이면 거부")
        void invalidLabIdType_denied() {
            // when
            boolean allowed = handler.hasPermissionForLab(principal, "invalid", "VIEW_ATTENDANCE");

            // then
            assertThat(allowed).isFalse();
        }
    }
}