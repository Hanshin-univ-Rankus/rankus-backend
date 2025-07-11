package org.univ.rankus.domain.model.interview;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainInterviewFactory;
import org.univ.rankus.testutil.factory.domain.DomainInterviewSlotFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Interview 도메인 테스트")
class InterviewTest {

    @Nested
    @DisplayName("면접 설정 생성 시")
    class CreateInterviewTests {

        @Test
        @DisplayName("유효한 정보로 면접 설정을 생성할 수 있다")
        void createInterview_ValidInfo_Success() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            LocalDate startDate = LocalDate.now().plusDays(7);
            LocalDate endDate = LocalDate.now().plusDays(14);
            Integer duration = 60;
            Integer maxApplicants = 5;

            // when
            Interview interview = new Interview(lab, startDate, endDate, duration, maxApplicants);

            // then
            assertThat(interview.getLab()).isEqualTo(lab);
            assertThat(interview.getStartDate()).isEqualTo(startDate);
            assertThat(interview.getEndDate()).isEqualTo(endDate);
            assertThat(interview.getDurationMinutes()).isEqualTo(duration);
            assertThat(interview.getMaxApplicantsPerSlot()).isEqualTo(maxApplicants);
            assertThat(interview.getStatus()).isEqualTo(InterviewStatus.INACTIVE);
            assertThat(interview.isActive()).isFalse();
            assertThat(interview.isClosed()).isFalse();
        }

        @Test
        @DisplayName("null 랩실로 면접 설정 생성 시 예외가 발생한다")
        void createInterview_NullLab_ThrowsException() {
            // given
            LocalDate startDate = LocalDate.now().plusDays(7);
            LocalDate endDate = LocalDate.now().plusDays(14);

            // when & then
            assertThatThrownBy(() -> new Interview(
                    null, startDate, endDate, 60, 5
            ))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException exception = (LabNotFoundException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("null 시작일로 면접 설정 생성 시 예외가 발생한다")
        void createInterview_NullStartDate_ThrowsException() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            LocalDate endDate = LocalDate.now().plusDays(14);

            // when & then
            assertThatThrownBy(() -> new Interview(
                    lab, null, endDate, 60, 5
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.DATE_REQUIRED);
                    });
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦을 때 예외가 발생한다")
        void createInterview_StartDateAfterEndDate_ThrowsException() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            LocalDate startDate = LocalDate.now().plusDays(14);
            LocalDate endDate = LocalDate.now().plusDays(7);

            // when & then
            assertThatThrownBy(() -> new Interview(
                    lab, startDate, endDate, 60, 5
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.INVALID_DATE_RANGE);
                    });
        }

        @Test
        @DisplayName("과거 시작일로 면접 설정 생성 시 예외가 발생한다")
        void createInterview_PastStartDate_ThrowsException() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            LocalDate pastDate = LocalDate.now().minusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(7);

            // when & then
            assertThatThrownBy(() -> new Interview(
                    lab, pastDate, endDate, 60, 5
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.PAST_DATE_NOT_ALLOWED);
                    });
        }

        @Test
        @DisplayName("잘못된 면접 소요 시간으로 생성 시 예외가 발생한다")
        void createInterview_InvalidDuration_ThrowsException() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            LocalDate startDate = LocalDate.now().plusDays(7);
            LocalDate endDate = LocalDate.now().plusDays(14);

            // when & then - 0분
            assertThatThrownBy(() -> new Interview(
                    lab, startDate, endDate, 0, 5
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.INVALID_DURATION);
                    });

            // when & then - 너무 긴 시간 (3시간 초과)
            assertThatThrownBy(() -> new Interview(
                    lab, startDate, endDate, 200, 5
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.DURATION_TOO_LONG);
                    });
        }

        @Test
        @DisplayName("잘못된 최대 지원자 수로 생성 시 예외가 발생한다")
        void createInterview_InvalidMaxApplicants_ThrowsException() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            LocalDate startDate = LocalDate.now().plusDays(7);
            LocalDate endDate = LocalDate.now().plusDays(14);

            // when & then - 0명
            assertThatThrownBy(() -> new Interview(
                    lab, startDate, endDate, 60, 0
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.INVALID_MAX_APPLICANTS);
                    });

            // when & then - 너무 많은 지원자 (10명 초과)
            assertThatThrownBy(() -> new Interview(
                    lab, startDate, endDate, 60, 15
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.TOO_MANY_APPLICANTS);
                    });
        }
    }

    @Nested
    @DisplayName("면접 활성화 시")
    class ActivateInterviewTests {

        @Test
        @DisplayName("비활성화 상태에서 면접을 활성화할 수 있다")
        void activateInterview_FromInactive_Success() {
            // given
            Interview interview = DomainInterviewFactory.buildInactiveInterview();

            // when
            interview.activate();

            // then
            assertThat(interview.getStatus()).isEqualTo(InterviewStatus.ACTIVE);
            assertThat(interview.isActive()).isTrue();
            assertThat(interview.isApplicationAvailable()).isTrue();
        }

        @Test
        @DisplayName("이미 활성화된 면접을 다시 활성화하면 예외가 발생한다")
        void activateInterview_AlreadyActive_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildActiveInterview();

            // when & then
            assertThatThrownBy(() -> interview.activate())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.ALREADY_ACTIVATED);
                    });
        }

        @Test
        @DisplayName("종료된 면접을 활성화하면 예외가 발생한다")
        void activateInterview_Closed_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildClosedInterview();

            // when & then
            assertThatThrownBy(() -> interview.activate())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.CANNOT_ACTIVATE_CLOSED);
                    });
        }

        @Test
        @DisplayName("만료된 면접을 활성화하면 예외가 발생한다")
        void activateInterview_Expired_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildExpiredInterview();

            // when & then
            assertThatThrownBy(() -> interview.activate())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.EXPIRED_INTERVIEW_PERIOD);
                    });
        }
    }

    @Nested
    @DisplayName("면접 비활성화 시")
    class DeactivateInterviewTests {

        @Test
        @DisplayName("활성화 상태에서 면접을 비활성화할 수 있다")
        void deactivateInterview_FromActive_Success() {
            // given
            Interview interview = DomainInterviewFactory.buildActiveInterview();

            // when
            interview.deactivate();

            // then
            assertThat(interview.getStatus()).isEqualTo(InterviewStatus.INACTIVE);
            assertThat(interview.isActive()).isFalse();
            assertThat(interview.isApplicationAvailable()).isFalse();
        }

        @Test
        @DisplayName("이미 비활성화된 면접을 다시 비활성화하면 예외가 발생한다")
        void deactivateInterview_AlreadyInactive_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildInactiveInterview();

            // when & then
            assertThatThrownBy(() -> interview.deactivate())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.ALREADY_DEACTIVATED);
                    });
        }

        @Test
        @DisplayName("종료된 면접을 비활성화하면 예외가 발생한다")
        void deactivateInterview_Closed_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildClosedInterview();

            // when & then
            assertThatThrownBy(() -> interview.deactivate())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.CANNOT_DEACTIVATE_CLOSED);
                    });
        }
    }

    @Nested
    @DisplayName("면접 종료 시")
    class CloseInterviewTests {

        @Test
        @DisplayName("활성화 상태에서 면접을 종료할 수 있다")
        void closeInterview_FromActive_Success() {
            // given
            Interview interview = DomainInterviewFactory.buildActiveInterview();

            // when
            interview.close();

            // then
            assertThat(interview.getStatus()).isEqualTo(InterviewStatus.CLOSED);
            assertThat(interview.isClosed()).isTrue();
            assertThat(interview.isApplicationAvailable()).isFalse();
        }

        @Test
        @DisplayName("비활성화 상태에서 면접을 종료할 수 있다")
        void closeInterview_FromInactive_Success() {
            // given
            Interview interview = DomainInterviewFactory.buildInactiveInterview();

            // when
            interview.close();

            // then
            assertThat(interview.getStatus()).isEqualTo(InterviewStatus.CLOSED);
            assertThat(interview.isClosed()).isTrue();
        }

        @Test
        @DisplayName("이미 종료된 면접을 다시 종료하면 예외가 발생한다")
        void closeInterview_AlreadyClosed_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildClosedInterview();

            // when & then
            assertThatThrownBy(() -> interview.close())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.ALREADY_CLOSED);
                    });
        }
    }

    @Nested
    @DisplayName("면접 슬롯 추가 시")
    class AddSlotTests {

        @Test
        @DisplayName("유효한 슬롯을 면접에 추가할 수 있다")
        void addSlot_ValidSlot_Success() {
            // given
            Interview interview = DomainInterviewFactory.buildInactiveInterview();
            InterviewSlot slot = DomainInterviewSlotFactory.buildValidSlotForInterview(interview);

            // when
            interview.addSlot(slot);

            // then
            assertThat(interview.getSlots()).hasSize(1);
            assertThat(interview.getSlots()).contains(slot);
        }

        @Test
        @DisplayName("null 슬롯을 추가하면 예외가 발생한다")
        void addSlot_NullSlot_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildInactiveInterview();

            // when & then
            assertThatThrownBy(() -> interview.addSlot(null))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_REQUIRED);
                    });
        }

        @Test
        @DisplayName("면접 기간 밖의 슬롯을 추가하면 예외가 발생한다")
        void addSlot_SlotOutsideInterviewPeriod_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildInactiveInterview();
            InterviewSlot slotOutsidePeriod = DomainInterviewSlotFactory.buildSlotOutsideInterview(interview);

            // when & then
            assertThatThrownBy(() -> interview.addSlot(slotOutsidePeriod))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_OUTSIDE_INTERVIEW_PERIOD);
                    });
        }
    }

    @Nested
    @DisplayName("면접 상태 확인 시")
    class StatusCheckTests {

        @Test
        @DisplayName("면접 기간 내에서 활성화된 면접은 지원 가능하다")
        void isApplicationAvailable_ActiveAndWithinPeriod_True() {
            // given
            Interview interview = DomainInterviewFactory.buildActiveInterview();

            // when & then
            assertThat(interview.isApplicationAvailable()).isTrue();
        }

        @Test
        @DisplayName("비활성화된 면접은 지원 불가능하다")
        void isApplicationAvailable_Inactive_False() {
            // given
            Interview interview = DomainInterviewFactory.buildInactiveInterview();

            // when & then
            assertThat(interview.isApplicationAvailable()).isFalse();
        }

        @Test
        @DisplayName("종료된 면접은 지원 불가능하다")
        void isApplicationAvailable_Closed_False() {
            // given
            Interview interview = DomainInterviewFactory.buildClosedInterview();

            // when & then
            assertThat(interview.isApplicationAvailable()).isFalse();
        }

        @Test
        @DisplayName("면접 기간이 지난 면접은 지원 불가능하다")
        void isApplicationAvailable_ExpiredPeriod_False() {
            // given
            Interview interview = DomainInterviewFactory.buildExpiredInterview();

            // when & then
            assertThat(interview.isApplicationAvailable()).isFalse();
        }
    }
}