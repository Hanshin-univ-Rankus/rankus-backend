package org.univ.rankus.domain.model.lab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.univ.rankus.domain.model.lab.application.ApplicationStatus;
import org.univ.rankus.domain.model.lab.application.LabApplication;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabApplicationErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabApplicationValidationException;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;
import org.univ.rankus.domain.model.interview.exception.InterviewNotFoundException;
import org.univ.rankus.domain.model.interview.exception.InterviewValidationException;
import org.univ.rankus.testutil.factory.domain.DomainLabApplicationFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;
import org.univ.rankus.testutil.factory.domain.DomainInterviewSlotFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabApplication 도메인 단위 테스트")
class LabApplicationTest {

    private final Lab lab = createLabWithId(1L);
    private final User user = createUserWithId(1L);
    private final LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
    
    private User createUserWithId(Long id) {
        User user = DomainUserFactory.buildValidUser();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
    
    private Lab createLabWithId(Long id) {
        Lab lab = DomainLabFactory.buildValidLab();
        org.springframework.test.util.ReflectionTestUtils.setField(lab, "id", id);
        return lab;
    }

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("null Lab 입력 시 LabNotFoundException 발생")
        void constructor_nullLab_throwsLabNotFound() {
            assertThrows(LabNotFoundException.class,
                    () -> DomainLabApplicationFactory.buildInvalidApp_NullLab(user, DomainInterviewSlotFactory.buildValidSlot())
            );
        }

        @Test
        @DisplayName("null User 입력 시 UserNotFoundException 발생")
        void constructor_nullUser_throwsUserNotFound() {
            Lab validLab = lab;
            LocalDateTime time = futureTime;
            assertThrows(UserNotFoundException.class,
                    () -> DomainLabApplicationFactory.buildInvalidApp_NullUser(validLab, DomainInterviewSlotFactory.buildValidSlot())
            );
        }

        @Test
        @DisplayName("null interviewSlot 입력 시 InterviewNotFoundException 예외 발생")
        void constructor_nullSlot_throwsInterviewNotFound() {
            assertThrows(InterviewNotFoundException.class,
                    () -> DomainLabApplicationFactory.buildInvalidApp_NullSlot(lab, user)
            );
        }

        @Test
        @DisplayName("과거 interviewTime 입력 시 InterviewValidationException 예외 발생")
        void constructor_pastTime_throwsInterviewValidation() {
            assertThrows(InterviewValidationException.class,
                    () -> DomainLabApplicationFactory.buildInvalidApp_PastTime(lab, user)
            );
        }

        @Test
        @DisplayName("유효한 입력 시 상태 PENDING, interviewTime 설정")
        void constructor_valid_setsPendingAndTime() {
            LabApplication app = DomainLabApplicationFactory.buildValidPendingApplication(lab, user, DomainInterviewSlotFactory.buildValidSlot());
            assertEquals(ApplicationStatus.PENDING, app.getStatus(), "기본 상태는 PENDING이어야 한다");
            assertNotNull(app.getInterviewSlot(), "interviewSlot이 설정되어야 한다");
        }
    }

    @Nested
    @DisplayName("승인 기능 검증 (approve)")
    class ApproveTests {

        @Test
        @DisplayName("PENDING 상태에서 approve 호출 시 APPROVED로 변경")
        void approve_fromPending_setsApproved() {
            LabApplication app = DomainLabApplicationFactory.buildValidPendingApplication(lab, user, DomainInterviewSlotFactory.buildValidSlot());
            app.approve();
            assertEquals(ApplicationStatus.APPROVED, app.getStatus());
        }

        @ParameterizedTest(name = "[{index}] 상태={0}에서 approve 호출 시 예외 발생")
        @EnumSource(value = ApplicationStatus.class, names = {"APPROVED", "REJECTED"})
        @DisplayName("PENDING이 아닌 상태에서 approve 호출 시 ALREADY_PROCESSED 예외 발생")
        void approve_nonPending_throwsAlreadyProcessed(ApplicationStatus status) {
            LabApplication app = DomainLabApplicationFactory.buildWithStatus(lab, user, status);
            LabApplicationValidationException ex = assertThrows(LabApplicationValidationException.class, app::approve);
            assertEquals(LabApplicationErrorCode.ALREADY_PROCESSED, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("거절 기능 검증 (reject)")
    class RejectTests {

        @Test
        @DisplayName("PENDING 상태에서 reject 호출 시 REJECTED로 변경")
        void reject_fromPending_setsRejected() {
            LabApplication app = DomainLabApplicationFactory.buildValidPendingApplication(lab, user, DomainInterviewSlotFactory.buildValidSlot());
            app.reject();
            assertEquals(ApplicationStatus.REJECTED, app.getStatus());
        }

        @ParameterizedTest(name = "[{index}] 상태={0}에서 reject 호출 시 예외 발생")
        @EnumSource(value = ApplicationStatus.class, names = {"APPROVED", "REJECTED"})
        @DisplayName("PENDING이 아닌 상태에서 reject 호출 시 ALREADY_PROCESSED 예외 발생")
        void reject_nonPending_throwsAlreadyProcessed(ApplicationStatus status) {
            LabApplication app = DomainLabApplicationFactory.buildWithStatus(lab, user, status);
            LabApplicationValidationException ex = assertThrows(LabApplicationValidationException.class, app::reject);
            assertEquals(LabApplicationErrorCode.ALREADY_PROCESSED, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("소유자 검증 (isOwnedBy)")
    class OwnershipTests {

        @Test
        @DisplayName("소유자 ID 일치 시 true 반환")
        void isOwnedBy_matchingId_returnsTrue() {
            LabApplication app = DomainLabApplicationFactory.buildValidPendingApplication(lab, user, DomainInterviewSlotFactory.buildValidSlot());
            assertTrue(app.isOwnedBy(1L)); // user의 ID는 1L로 설정됨
        }

        @Test
        @DisplayName("소유자 ID 불일치 시 false 반환")
        void isOwnedBy_nonMatching_returnsFalse() {
            LabApplication app = DomainLabApplicationFactory.buildValidPendingApplication(lab, user, DomainInterviewSlotFactory.buildValidSlot());
            assertFalse(app.isOwnedBy(Long.MAX_VALUE));
        }
    }
}