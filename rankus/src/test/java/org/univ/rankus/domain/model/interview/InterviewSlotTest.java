package org.univ.rankus.domain.model.interview;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewValidationException;
import org.univ.rankus.testutil.factory.domain.DomainInterviewFactory;
import org.univ.rankus.testutil.factory.domain.DomainInterviewSlotFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InterviewSlot 도메인 테스트")
class InterviewSlotTest {

    @Nested
    @DisplayName("면접 슬롯 생성 시")
    class CreateInterviewSlotTests {

        @Test
        @DisplayName("유효한 정보로 면접 슬롯을 생성할 수 있다")
        void createInterviewSlot_ValidInfo_Success() {
            // given
            Interview interview = DomainInterviewFactory.buildValidInterview();
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusHours(1);
            Integer maxApplicants = 3;

            // when
            InterviewSlot slot = new InterviewSlot(interview, startTime, endTime, maxApplicants);

            // then
            assertThat(slot.getInterview()).isEqualTo(interview);
            assertThat(slot.getStartTime()).isEqualTo(startTime);
            assertThat(slot.getEndTime()).isEqualTo(endTime);
            assertThat(slot.getMaxApplicants()).isEqualTo(maxApplicants);
            assertThat(slot.getCurrentApplicants()).isZero();
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
            assertThat(slot.isAvailable()).isTrue();
            assertThat(slot.isFull()).isFalse();
            assertThat(slot.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("null 면접으로 슬롯 생성 시 예외가 발생한다")
        void createInterviewSlot_NullInterview_ThrowsException() {
            // given
            LocalDateTime startTime = LocalDateTime.now().plusDays(10);
            LocalDateTime endTime = startTime.plusHours(1);

            // when & then
            assertThatThrownBy(() -> new InterviewSlot(
                    null, startTime, endTime, 3
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.INTERVIEW_REQUIRED);
                    });
        }

        @Test
        @DisplayName("null 시작 시간으로 슬롯 생성 시 예외가 발생한다")
        void createInterviewSlot_NullStartTime_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildValidInterview();
            LocalDateTime endTime = LocalDateTime.now().plusDays(10).plusHours(1);

            // when & then
            assertThatThrownBy(() -> new InterviewSlot(
                    interview, null, endTime, 3
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.TIME_REQUIRED);
                    });
        }

        @Test
        @DisplayName("시작 시간이 종료 시간보다 늦을 때 예외가 발생한다")
        void createInterviewSlot_StartTimeAfterEndTime_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildValidInterview();
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).withHour(11).withMinute(0);
            LocalDateTime endTime = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0);

            // when & then
            assertThatThrownBy(() -> new InterviewSlot(
                    interview, startTime, endTime, 3
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.INVALID_TIME_RANGE);
                    });
        }

        @Test
        @DisplayName("과거 시간으로 슬롯 생성 시 예외가 발생한다")
        void createInterviewSlot_PastTime_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildValidInterview();
            LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
            LocalDateTime endTime = LocalDateTime.now().plusHours(1);

            // when & then
            assertThatThrownBy(() -> new InterviewSlot(
                    interview, pastTime, endTime, 3
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.PAST_TIME_NOT_ALLOWED);
                    });
        }

        @Test
        @DisplayName("잘못된 최대 지원자 수로 슬롯 생성 시 예외가 발생한다")
        void createInterviewSlot_InvalidMaxApplicants_ThrowsException() {
            // given
            Interview interview = DomainInterviewFactory.buildValidInterview();
            LocalDateTime startTime = LocalDateTime.now().plusDays(10);
            LocalDateTime endTime = startTime.plusHours(1);

            // when & then - 0명
            assertThatThrownBy(() -> new InterviewSlot(
                    interview, startTime, endTime, 0
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.INVALID_MAX_APPLICANTS);
                    });

            // when & then - 너무 많은 지원자 (10명 초과)
            assertThatThrownBy(() -> new InterviewSlot(
                    interview, startTime, endTime, 15
            ))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.TOO_MANY_APPLICANTS);
                    });
        }
    }

    @Nested
    @DisplayName("슬롯 예약 시")
    class ReserveSlotTests {

        @Test
        @DisplayName("사용 가능한 슬롯에 예약할 수 있다")
        void reserveSlot_AvailableSlot_Success() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildAvailableSlot();
            int initialApplicants = slot.getCurrentApplicants();

            // when
            slot.reserve();

            // then
            assertThat(slot.getCurrentApplicants()).isEqualTo(initialApplicants + 1);
            assertThat(slot.getAvailableSpots()).isEqualTo(slot.getMaxApplicants() - slot.getCurrentApplicants());
        }

        @Test
        @DisplayName("예약으로 인해 슬롯이 가득 차면 상태가 FULL로 변경된다")
        void reserveSlot_BecomesFull_StatusChangesToFull() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithOneSpotLeft();

            // when
            slot.reserve();

            // then
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.FULL);
            assertThat(slot.isFull()).isTrue();
            assertThat(slot.isAvailable()).isFalse();
            assertThat(slot.getAvailableSpots()).isZero();
        }

        @Test
        @DisplayName("이미 가득 찬 슬롯에 예약하면 예외가 발생한다")
        void reserveSlot_FullSlot_ThrowsException() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildFullSlot();

            // when & then
            assertThatThrownBy(() -> slot.reserve())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_FULL);
                    });
        }

        @Test
        @DisplayName("취소된 슬롯에 예약하면 예외가 발생한다")
        void reserveSlot_CancelledSlot_ThrowsException() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildCancelledSlot();

            // when & then
            assertThatThrownBy(() -> slot.reserve())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_CANCELLED);
                    });
        }
    }

    @Nested
    @DisplayName("슬롯 예약 취소 시")
    class CancelReservationTests {

        @Test
        @DisplayName("예약이 있는 슬롯에서 예약을 취소할 수 있다")
        void cancelReservation_HasReservation_Success() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithReservations();
            int initialApplicants = slot.getCurrentApplicants();

            // when
            slot.cancelReservation();

            // then
            assertThat(slot.getCurrentApplicants()).isEqualTo(initialApplicants - 1);
            assertThat(slot.getAvailableSpots()).isEqualTo(slot.getMaxApplicants() - slot.getCurrentApplicants());
        }

        @Test
        @DisplayName("가득 찬 슬롯에서 예약 취소 시 상태가 AVAILABLE로 변경된다")
        void cancelReservation_FromFullSlot_StatusChangesToAvailable() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildFullSlot();

            // when
            slot.cancelReservation();

            // then
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
            assertThat(slot.isAvailable()).isTrue();
            assertThat(slot.isFull()).isFalse();
        }

        @Test
        @DisplayName("예약이 없는 슬롯에서 예약 취소 시 예외가 발생한다")
        void cancelReservation_NoReservation_ThrowsException() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildAvailableSlot();

            // when & then
            assertThatThrownBy(() -> slot.cancelReservation())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.NO_RESERVATION_TO_CANCEL);
                    });
        }
    }

    @Nested
    @DisplayName("슬롯 취소 시")
    class CancelSlotTests {

        @Test
        @DisplayName("예약이 없는 슬롯을 취소할 수 있다")
        void cancelSlot_NoReservations_Success() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildAvailableSlot();

            // when
            slot.cancel();

            // then
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.CANCELLED);
            assertThat(slot.isCancelled()).isTrue();
            assertThat(slot.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("예약이 있는 슬롯을 취소하면 예외가 발생한다")
        void cancelSlot_HasReservations_ThrowsException() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithReservations();

            // when & then
            assertThatThrownBy(() -> slot.cancel())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.CANNOT_CANCEL_SLOT_WITH_APPLICANTS);
                    });
        }

        @Test
        @DisplayName("이미 취소된 슬롯을 다시 취소하면 예외가 발생한다")
        void cancelSlot_AlreadyCancelled_ThrowsException() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildCancelledSlot();

            // when & then
            assertThatThrownBy(() -> slot.cancel())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_ALREADY_CANCELLED);
                    });
        }
    }

    @Nested
    @DisplayName("슬롯 재활성화 시")
    class ReactivateSlotTests {

        @Test
        @DisplayName("취소된 슬롯을 재활성화할 수 있다")
        void reactivateSlot_CancelledSlot_Success() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildCancelledSlot();

            // when
            slot.reactivate();

            // then
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
            assertThat(slot.isAvailable()).isTrue();
            assertThat(slot.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("취소되지 않은 슬롯을 재활성화하면 예외가 발생한다")
        void reactivateSlot_NotCancelled_ThrowsException() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildAvailableSlot();

            // when & then
            assertThatThrownBy(() -> slot.reactivate())
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException exception = (InterviewValidationException) ex;
                        assertThat(exception.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_NOT_CANCELLED);
                    });
        }
    }

    @Nested
    @DisplayName("슬롯 상태 확인 시")
    class SlotStatusCheckTests {

        @Test
        @DisplayName("빈 슬롯은 예약 가능하다")
        void isAvailable_EmptySlot_True() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildAvailableSlot();

            // when & then
            assertThat(slot.isAvailable()).isTrue();
            assertThat(slot.isFull()).isFalse();
            assertThat(slot.getAvailableSpots()).isEqualTo(slot.getMaxApplicants());
        }

        @Test
        @DisplayName("가득 찬 슬롯은 예약 불가능하다")
        void isAvailable_FullSlot_False() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildFullSlot();

            // when & then
            assertThat(slot.isAvailable()).isFalse();
            assertThat(slot.isFull()).isTrue();
            assertThat(slot.getAvailableSpots()).isZero();
        }

        @Test
        @DisplayName("취소된 슬롯은 예약 불가능하다")
        void isAvailable_CancelledSlot_False() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildCancelledSlot();

            // when & then
            assertThat(slot.isAvailable()).isFalse();
            assertThat(slot.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("시간이 지난 슬롯은 예약 불가능하다")
        void isPast_PastSlot_True() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildPastSlot();

            // when & then
            assertThat(slot.isPast()).isTrue();
        }

        @Test
        @DisplayName("미래 시간의 슬롯은 예약 가능하다")
        void isPast_FutureSlot_False() {
            // given
            InterviewSlot slot = DomainInterviewSlotFactory.buildAvailableSlot();

            // when & then
            assertThat(slot.isPast()).isFalse();
        }
    }
}