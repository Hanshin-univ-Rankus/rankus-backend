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
import org.univ.rankus.application.port.in.AttendanceRecordQueryUseCase;
import org.univ.rankus.application.port.in.AttendanceSessionQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.attendance.AttendanceRecord;
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
@DisplayName("AttendanceRecordPermissionHandler 테스트")
class AttendanceRecordPermissionHandlerTest {

    @Mock
    private AttendanceRecordQueryUseCase attendanceRecordQueryUseCase;

    @Mock
    private AttendanceSessionQueryUseCase attendanceSessionQueryUseCase;

    @Mock
    private LabPromotionQueryUseCase labPromotionQueryUseCase;

    @Mock
    private UserQueryUseCase userQueryUseCase;

    @InjectMocks
    private AttendanceRecordPermissionHandler handler;

    private CustomUserDetails principal;
    private static final Long USER_ID = 10L;
    private static final Long RECORD_ID = 100L;
    private static final Long SESSION_ID = 200L;
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
        @DisplayName("AttendanceRecord를 반환한다")
        void returnsAttendanceRecord() {
            // when
            String targetType = handler.targetType();

            // then
            assertThat(targetType).isEqualTo("AttendanceRecord");
        }
    }

    @Nested
    @DisplayName("hasPermission 메서드는")
    class HasPermissionTests {

        @Test
        @DisplayName("VIEW 권한: 본인의 출석 기록이면 허용")
        void viewPermission_ownRecord_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceRecord mockRecord = mock(AttendanceRecord.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceRecordQueryUseCase.findRecordById(RECORD_ID, USER_ID)).willReturn(mockRecord);
            given(mockRecord.getSessionId()).willReturn(SESSION_ID);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockRecord.isOwnedBy(USER_ID)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, RECORD_ID, "VIEW");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("VIEW 권한: 타인의 출석 기록이지만 랩실 조회 권한이 있으면 허용")
        void viewPermission_otherRecordWithLabViewPermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceRecord mockRecord = mock(AttendanceRecord.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceRecordQueryUseCase.findRecordById(RECORD_ID, USER_ID)).willReturn(mockRecord);
            given(mockRecord.getSessionId()).willReturn(SESSION_ID);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockRecord.isOwnedBy(USER_ID)).willReturn(false);
            given(mockUser.canViewLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, RECORD_ID, "VIEW");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("VIEW 권한: 타인의 출석 기록이고 랩실 조회 권한이 없으면 거부")
        void viewPermission_otherRecordWithoutLabViewPermission_denied() {
            // given
            User mockUser = mock(User.class);
            AttendanceRecord mockRecord = mock(AttendanceRecord.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceRecordQueryUseCase.findRecordById(RECORD_ID, USER_ID)).willReturn(mockRecord);
            given(mockRecord.getSessionId()).willReturn(SESSION_ID);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockRecord.isOwnedBy(USER_ID)).willReturn(false);
            given(mockUser.canViewLabAttendance(mockLab)).willReturn(false);

            // when
            boolean allowed = handler.hasPermission(principal, RECORD_ID, "VIEW");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("MANAGE 권한: 랩실 출석 관리 권한이 있으면 허용")
        void managePermission_withLabManageAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceRecord mockRecord = mock(AttendanceRecord.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceRecordQueryUseCase.findRecordById(RECORD_ID, USER_ID)).willReturn(mockRecord);
            given(mockRecord.getSessionId()).willReturn(SESSION_ID);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, RECORD_ID, "MANAGE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("MANAGE 권한: 랩실 출석 관리 권한이 없으면 거부")
        void managePermission_withoutLabManageAttendancePermission_denied() {
            // given
            User mockUser = mock(User.class);
            AttendanceRecord mockRecord = mock(AttendanceRecord.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceRecordQueryUseCase.findRecordById(RECORD_ID, USER_ID)).willReturn(mockRecord);
            given(mockRecord.getSessionId()).willReturn(SESSION_ID);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(false);

            // when
            boolean allowed = handler.hasPermission(principal, RECORD_ID, "MANAGE");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("UPDATE 권한: 랩실 출석 관리 권한이 있으면 허용")
        void updatePermission_withLabManageAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceRecord mockRecord = mock(AttendanceRecord.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceRecordQueryUseCase.findRecordById(RECORD_ID, USER_ID)).willReturn(mockRecord);
            given(mockRecord.getSessionId()).willReturn(SESSION_ID);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, RECORD_ID, "UPDATE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("DELETE 권한: 랩실 출석 관리 권한이 있으면 허용")
        void deletePermission_withLabManageAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceRecord mockRecord = mock(AttendanceRecord.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceRecordQueryUseCase.findRecordById(RECORD_ID, USER_ID)).willReturn(mockRecord);
            given(mockRecord.getSessionId()).willReturn(SESSION_ID);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermission(principal, RECORD_ID, "DELETE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("알 수 없는 권한에 대해서는 거부")
        void unknownPermission_denied() {
            // given
            User mockUser = DomainUserFactory.buildValidUserWithId(USER_ID);
            AttendanceRecord mockRecord = DomainAttendanceFactory.buildValidRecordWithId(RECORD_ID);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceRecordQueryUseCase.findRecordById(RECORD_ID, USER_ID)).willReturn(mockRecord);
            given(attendanceSessionQueryUseCase.findSessionById(mockRecord.getSessionId(), USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);

            // when
            boolean allowed = handler.hasPermission(principal, RECORD_ID, "UNKNOWN");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("잘못된 principal 타입이면 거부")
        void invalidPrincipalType_denied() {
            // when
            boolean allowed = handler.hasPermission("invalid", RECORD_ID, "VIEW");

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
    @DisplayName("hasPermissionForSession 메서드는")
    class HasPermissionForSessionTests {

        @Test
        @DisplayName("VIEW 권한: 랩실 출석 조회 권한이 있으면 허용")
        void viewPermissionForSession_withLabViewAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canViewLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermissionForSession(principal, SESSION_ID, "VIEW");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("VIEW 권한: 랩실 출석 조회 권한이 없으면 거부")
        void viewPermissionForSession_withoutLabViewAttendancePermission_denied() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canViewLabAttendance(mockLab)).willReturn(false);

            // when
            boolean allowed = handler.hasPermissionForSession(principal, SESSION_ID, "VIEW");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("MANAGE 권한: 랩실 출석 관리 권한이 있으면 허용")
        void managePermissionForSession_withLabManageAttendancePermission_allowed() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(true);

            // when
            boolean allowed = handler.hasPermissionForSession(principal, SESSION_ID, "MANAGE");

            // then
            assertThat(allowed).isTrue();
        }

        @Test
        @DisplayName("MANAGE 권한: 랩실 출석 관리 권한이 없으면 거부")
        void managePermissionForSession_withoutLabManageAttendancePermission_denied() {
            // given
            User mockUser = mock(User.class);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);
            given(mockUser.canManageLabAttendance(mockLab)).willReturn(false);

            // when
            boolean allowed = handler.hasPermissionForSession(principal, SESSION_ID, "MANAGE");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("알 수 없는 권한에 대해서는 거부")
        void unknownPermissionForSession_denied() {
            // given
            User mockUser = DomainUserFactory.buildValidUserWithId(USER_ID);
            AttendanceSession mockSession = DomainAttendanceFactory.buildValidSessionWithId(SESSION_ID);
            Lab mockLab = DomainLabFactory.buildValidLabWithId(LAB_ID);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(mockUser);
            given(attendanceSessionQueryUseCase.findSessionById(SESSION_ID, USER_ID)).willReturn(mockSession);
            given(labPromotionQueryUseCase.getLabById(mockSession.getLabId())).willReturn(mockLab);

            // when
            boolean allowed = handler.hasPermissionForSession(principal, SESSION_ID, "UNKNOWN");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("잘못된 principal 타입이면 거부")
        void invalidPrincipalTypeForSession_denied() {
            // when
            boolean allowed = handler.hasPermissionForSession("invalid", SESSION_ID, "VIEW");

            // then
            assertThat(allowed).isFalse();
        }

        @Test
        @DisplayName("잘못된 sessionId 타입이면 거부")
        void invalidSessionIdType_denied() {
            // when
            boolean allowed = handler.hasPermissionForSession(principal, "invalid", "VIEW");

            // then
            assertThat(allowed).isFalse();
        }
    }
}