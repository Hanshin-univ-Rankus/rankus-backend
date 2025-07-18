package org.univ.rankus.common.security.permission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.in.query.CalendarEventQueryUseCase;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.calendar.CalendarEvent;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainCalendarEventFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarPermissionHandler 테스트")
class CalendarPermissionHandlerTest {

    @Mock
    private CalendarEventQueryUseCase calendarEventQueryUseCase;

    @Mock
    private UserQueryUseCase userQueryUseCase;

    @Mock
    private LabPromotionQueryUseCase labPromotionQueryUseCase;

    @InjectMocks
    private CalendarPermissionHandler calendarPermissionHandler;

    @Mock
    private CustomUserDetails userDetails;

    private static final Long USER_ID = 1L;
    private static final Long LAB_ID = 1L;
    private static final Long EVENT_ID = 1L;

    @Nested
    @DisplayName("hasPermissionForLab 메서드는")
    class HasPermissionForLabTests {

        @Test
        @DisplayName("MANAGE_CALENDAR 권한 - 랩 관리자인 경우 true 반환")
        void manageCalendarPermission_LabManager_ReturnsTrue() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User labManager = DomainUserFactory.buildLabManagerWithLab(lab);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(labManager);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.hasPermissionForLab(userDetails, LAB_ID, "MANAGE_CALENDAR");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("MANAGE_CALENDAR 권한 - 교수인 경우 true 반환")
        void manageCalendarPermission_Professor_ReturnsTrue() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User professor = DomainUserFactory.buildProfessorUser();

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(professor);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.hasPermissionForLab(userDetails, LAB_ID, "MANAGE_CALENDAR");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("MANAGE_CALENDAR 권한 - 관리자인 경우 true 반환")
        void manageCalendarPermission_Admin_ReturnsTrue() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User admin = DomainUserFactory.buildAdminUser();

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(admin);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.hasPermissionForLab(userDetails, LAB_ID, "MANAGE_CALENDAR");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("MANAGE_CALENDAR 권한 - 일반 사용자인 경우 false 반환")
        void manageCalendarPermission_RegularUser_ReturnsFalse() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User regularUser = DomainUserFactory.buildStudentUser();

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(regularUser);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.hasPermissionForLab(userDetails, LAB_ID, "MANAGE_CALENDAR");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("VIEW_CALENDAR 권한 - 모든 인증된 사용자에게 true 반환")
        void viewCalendarPermission_AuthenticatedUser_ReturnsTrue() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User anyUser = DomainUserFactory.buildStudentUser();

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(anyUser);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.hasPermissionForLab(userDetails, LAB_ID, "VIEW_CALENDAR");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("VIEW_APPLICANTS 권한 - 랩 관리자인 경우 true 반환")
        void viewApplicantsPermission_LabManager_ReturnsTrue() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User labManager = DomainUserFactory.buildLabManagerWithLab(lab);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(labManager);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.hasPermissionForLab(userDetails, LAB_ID, "VIEW_APPLICANTS");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("알 수 없는 권한 - false 반환")
        void unknownPermission_ReturnsFalse() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User anyUser = DomainUserFactory.buildStudentUser();

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(anyUser);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.hasPermissionForLab(userDetails, LAB_ID, "UNKNOWN_PERMISSION");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null userDetails - false 반환")
        void nullUserDetails_ReturnsFalse() {
            // When
            boolean result = calendarPermissionHandler.hasPermissionForLab(null, LAB_ID, "MANAGE_CALENDAR");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null labId - false 반환")
        void nullLabId_ReturnsFalse() {
            // When
            boolean result = calendarPermissionHandler.hasPermissionForLab(userDetails, null, "MANAGE_CALENDAR");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPermissionForEvent 메서드는")
    class HasPermissionForEventTests {

        @Test
        @DisplayName("MANAGE_CALENDAR 권한 - 이벤트 랩의 관리자인 경우 true 반환")
        void manageCalendarPermission_EventLabManager_ReturnsTrue() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User labManager = DomainUserFactory.buildLabManagerWithLab(lab);
            CalendarEvent event = DomainCalendarEventFactory.buildScheduleWithLab(lab);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(labManager);
            given(calendarEventQueryUseCase.getEventById(EVENT_ID)).willReturn(event);

            // When
            boolean result = calendarPermissionHandler.hasPermissionForEvent(userDetails, EVENT_ID, "MANAGE_CALENDAR");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("VIEW_CALENDAR 권한 - 모든 인증된 사용자에게 true 반환")
        void viewCalendarPermission_AuthenticatedUser_ReturnsTrue() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User anyUser = DomainUserFactory.buildStudentUser();
            CalendarEvent event = DomainCalendarEventFactory.buildScheduleWithLab(lab);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(anyUser);
            given(calendarEventQueryUseCase.getEventById(EVENT_ID)).willReturn(event);

            // When
            boolean result = calendarPermissionHandler.hasPermissionForEvent(userDetails, EVENT_ID, "VIEW_CALENDAR");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null userDetails - false 반환")
        void nullUserDetails_ReturnsFalse() {
            // When
            boolean result = calendarPermissionHandler.hasPermissionForEvent(null, EVENT_ID, "MANAGE_CALENDAR");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null eventId - false 반환")
        void nullEventId_ReturnsFalse() {
            // When
            boolean result = calendarPermissionHandler.hasPermissionForEvent(userDetails, null, "MANAGE_CALENDAR");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isLabMember 메서드는")
    class IsLabMemberTests {

        @Test
        @DisplayName("사용자가 해당 랩의 멤버인 경우 true 반환")
        void userIsLabMember_ReturnsTrue() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User labMember = DomainUserFactory.buildLabMemberWithLab(lab);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(labMember);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.isLabMember(userDetails, LAB_ID);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("사용자가 다른 랩의 멤버인 경우 false 반환")
        void userIsNotLabMember_ReturnsFalse() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            Lab otherLab = DomainLabFactory.buildValidLabWithId(2L);
            User otherLabMember = DomainUserFactory.buildLabMemberWithLab(otherLab);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(otherLabMember);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.isLabMember(userDetails, LAB_ID);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("사용자가 랩에 속하지 않은 경우 false 반환")
        void userHasNoLab_ReturnsFalse() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User userWithoutLab = DomainUserFactory.buildStudentUser();

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(userWithoutLab);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.isLabMember(userDetails, LAB_ID);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null userDetails - false 반환")
        void nullUserDetails_ReturnsFalse() {
            // When
            boolean result = calendarPermissionHandler.isLabMember(null, LAB_ID);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null labId - false 반환")
        void nullLabId_ReturnsFalse() {
            // When
            boolean result = calendarPermissionHandler.isLabMember(userDetails, null);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("canManageLab 메서드는")
    class CanManageLabTests {

        @Test
        @DisplayName("사용자가 랩 관리자인 경우 true 반환")
        void userCanManageLab_ReturnsTrue() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User labManager = DomainUserFactory.buildLabManagerWithLab(lab);

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(labManager);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.canManageLab(userDetails, LAB_ID);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("사용자가 일반 사용자인 경우 false 반환")
        void userCannotManageLab_ReturnsFalse() {
            // Given
            given(userDetails.getUserId()).willReturn(USER_ID);

            Lab lab = DomainLabFactory.buildValidLabWithId(LAB_ID);
            User regularUser = DomainUserFactory.buildStudentUser();

            given(userQueryUseCase.getUserById(USER_ID)).willReturn(regularUser);
            given(labPromotionQueryUseCase.getLabById(LAB_ID)).willReturn(lab);

            // When
            boolean result = calendarPermissionHandler.canManageLab(userDetails, LAB_ID);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null userDetails - false 반환")
        void nullUserDetails_ReturnsFalse() {
            // When
            boolean result = calendarPermissionHandler.canManageLab(null, LAB_ID);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null labId - false 반환")
        void nullLabId_ReturnsFalse() {
            // When
            boolean result = calendarPermissionHandler.canManageLab(userDetails, null);

            // Then
            assertThat(result).isFalse();
        }
    }
}