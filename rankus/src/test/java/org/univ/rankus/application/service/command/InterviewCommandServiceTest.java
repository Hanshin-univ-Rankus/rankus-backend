package org.univ.rankus.application.service.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.application.port.out.InterviewRepositoryPort;
import org.univ.rankus.application.port.out.InterviewSlotRepositoryPort;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.InterviewStatus;
import org.univ.rankus.domain.model.interview.SlotStatus;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewNotFoundException;
import org.univ.rankus.domain.model.interview.exception.InterviewValidationException;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainInterviewFactory;
import org.univ.rankus.testutil.factory.domain.DomainInterviewSlotFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InterviewCommandServiceTest {

    @Mock
    private InterviewRepositoryPort interviewRepositoryPort;

    @Mock
    private InterviewSlotRepositoryPort interviewSlotRepositoryPort;

    @Mock
    private LabRepositoryPort labRepositoryPort;

    @InjectMocks
    private InterviewCommandService interviewCommandService;

    // ——————————————————————————————————————————————————————————
    // 헬퍼 메서드: 반복되는 모킹 로직을 한곳에 모았습니다.
    // ——————————————————————————————————————————————————————————

    private Lab givenExistingLab(Long labId) {
        Lab lab = DomainLabFactory.buildValidLabWithId(labId);
        when(labRepositoryPort.findById(labId)).thenReturn(Optional.of(lab));
        return lab;
    }

    private Interview givenExistingInterview(Long interviewId) {
        Interview interview = DomainInterviewFactory.buildInterviewWithId(interviewId);
        when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.of(interview));
        return interview;
    }

    private Interview givenActiveInterview(Long interviewId) {
        Interview interview = DomainInterviewFactory.buildActiveInterview();
        ReflectionTestUtils.setField(interview, "id", interviewId);
        when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.of(interview));
        return interview;
    }

    private InterviewSlot givenExistingSlot(Long slotId) {
        InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithId(slotId);
        when(interviewSlotRepositoryPort.findById(slotId)).thenReturn(Optional.of(slot));
        return slot;
    }

    private InterviewSlot givenCancelledSlot(Long slotId) {
        InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithId(slotId);
        ReflectionTestUtils.setField(slot, "status", SlotStatus.CANCELLED);
        when(interviewSlotRepositoryPort.findById(slotId)).thenReturn(Optional.of(slot));
        return slot;
    }

    // ——————————————————————————————————————————————————————————
    // 1) createInterview 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("createInterview 메서드는")
    class CreateInterviewTests {

        @Test
        @DisplayName("정상 입력 시 면접을 생성하고 저장된 면접을 반환한다")
        void createInterview_validInput_success() {
            // given
            Long labId = 1L;
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = startDate.plusDays(7);
            Integer durationMinutes = 60;
            Integer maxApplicantsPerSlot = 5;

            Lab lab = givenExistingLab(labId);
            Interview expectedInterview = new Interview(lab, startDate, endDate, durationMinutes, maxApplicantsPerSlot);
            ReflectionTestUtils.setField(expectedInterview, "id", 1L);

            when(interviewRepositoryPort.existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE))
                    .thenReturn(false);
            when(interviewRepositoryPort.save(any(Interview.class)))
                    .thenReturn(expectedInterview);

            // when
            Interview result = interviewCommandService.createInterview(labId, startDate, endDate, durationMinutes, maxApplicantsPerSlot);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getLab()).isEqualTo(lab);
            assertThat(result.getStartDate()).isEqualTo(startDate);
            assertThat(result.getEndDate()).isEqualTo(endDate);
            assertThat(result.getDurationMinutes()).isEqualTo(durationMinutes);
            assertThat(result.getMaxApplicantsPerSlot()).isEqualTo(maxApplicantsPerSlot);

            verify(labRepositoryPort).findById(labId);
            verify(interviewRepositoryPort).existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE);
            verify(interviewRepositoryPort).save(any(Interview.class));
        }

        @Test
        @DisplayName("랩실이 존재하지 않으면 LabNotFoundException을 던진다")
        void createInterview_labNotFound_throwsException() {
            // given
            Long labId = 999L;
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = startDate.plusDays(7);

            when(labRepositoryPort.findById(labId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.createInterview(labId, startDate, endDate, 60, 5))
                    .isInstanceOf(LabNotFoundException.class)
                    .satisfies(ex -> {
                        LabNotFoundException e = (LabNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(LabErrorCode.LAB_NOT_FOUND);
                    });

            verify(labRepositoryPort).findById(labId);
            verify(interviewRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("이미 활성화된 면접이 있으면 InterviewValidationException을 던진다")
        void createInterview_duplicateActiveInterview_throwsException() {
            // given
            Long labId = 1L;
            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = startDate.plusDays(7);

            givenExistingLab(labId);
            when(interviewRepositoryPort.existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE))
                    .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> interviewCommandService.createInterview(labId, startDate, endDate, 60, 5))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException e = (InterviewValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.DUPLICATE_INTERVIEW);
                    });

            verify(labRepositoryPort).findById(labId);
            verify(interviewRepositoryPort).existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE);
            verify(interviewRepositoryPort, never()).save(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 2) activateInterview 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("activateInterview 메서드는")
    class ActivateInterviewTests {

        @Test
        @DisplayName("정상 입력 시 면접을 활성화하고 저장된 면접을 반환한다")
        void activateInterview_validInput_success() {
            // given
            Long interviewId = 1L;
            Interview interview = givenExistingInterview(interviewId);
            Lab lab = interview.getLab();

            when(interviewRepositoryPort.existsByLabIdAndStatus(lab.getId(), InterviewStatus.ACTIVE))
                    .thenReturn(false);
            when(interviewRepositoryPort.save(any(Interview.class)))
                    .thenReturn(interview);

            // when
            Interview result = interviewCommandService.activateInterview(interviewId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(interviewId);

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort).existsByLabIdAndStatus(lab.getId(), InterviewStatus.ACTIVE);
            verify(interviewRepositoryPort).save(interview);
        }

        @Test
        @DisplayName("면접이 존재하지 않으면 InterviewNotFoundException을 던진다")
        void activateInterview_interviewNotFound_throwsException() {
            // given
            Long interviewId = 999L;
            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.activateInterview(interviewId))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("이미 활성화된 다른 면접이 있으면 InterviewValidationException을 던진다")
        void activateInterview_duplicateActiveInterview_throwsException() {
            // given
            Long interviewId = 1L;
            Interview interview = givenExistingInterview(interviewId);
            Lab lab = interview.getLab();

            when(interviewRepositoryPort.existsByLabIdAndStatus(lab.getId(), InterviewStatus.ACTIVE))
                    .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> interviewCommandService.activateInterview(interviewId))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException e = (InterviewValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.DUPLICATE_INTERVIEW);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort).existsByLabIdAndStatus(lab.getId(), InterviewStatus.ACTIVE);
            verify(interviewRepositoryPort, never()).save(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 3) deactivateInterview 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("deactivateInterview 메서드는")
    class DeactivateInterviewTests {

        @Test
        @DisplayName("정상 입력 시 면접을 비활성화하고 저장된 면접을 반환한다")
        void deactivateInterview_validInput_success() {
            // given
            Long interviewId = 1L;
            Interview interview = givenActiveInterview(interviewId);
            when(interviewRepositoryPort.save(any(Interview.class))).thenReturn(interview);

            // when
            Interview result = interviewCommandService.deactivateInterview(interviewId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(interviewId);

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort).save(interview);
        }

        @Test
        @DisplayName("면접이 존재하지 않으면 InterviewNotFoundException을 던진다")
        void deactivateInterview_interviewNotFound_throwsException() {
            // given
            Long interviewId = 999L;
            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.deactivateInterview(interviewId))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort, never()).save(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 4) closeInterview 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("closeInterview 메서드는")
    class CloseInterviewTests {

        @Test
        @DisplayName("정상 입력 시 면접을 종료하고 저장된 면접을 반환한다")
        void closeInterview_validInput_success() {
            // given
            Long interviewId = 1L;
            Interview interview = givenExistingInterview(interviewId);
            when(interviewRepositoryPort.save(any(Interview.class))).thenReturn(interview);

            // when
            Interview result = interviewCommandService.closeInterview(interviewId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(interviewId);

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort).save(interview);
        }

        @Test
        @DisplayName("면접이 존재하지 않으면 InterviewNotFoundException을 던진다")
        void closeInterview_interviewNotFound_throwsException() {
            // given
            Long interviewId = 999L;
            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.closeInterview(interviewId))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort, never()).save(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 5) createInterviewSlot 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("createInterviewSlot 메서드는")
    class CreateInterviewSlotTests {

        @Test
        @DisplayName("정상 입력 시 면접 슬롯을 생성하고 저장된 슬롯을 반환한다")
        void createInterviewSlot_validInput_success() {
            // given
            Long interviewId = 1L;
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            LocalDateTime endTime = startTime.plusHours(1);
            Integer maxApplicants = 3;

            Interview interview = givenExistingInterview(interviewId);
            InterviewSlot expectedSlot = new InterviewSlot(interview, startTime, endTime, maxApplicants);
            ReflectionTestUtils.setField(expectedSlot, "id", 1L);

            when(interviewSlotRepositoryPort.existsByInterviewIdAndTimeRange(interviewId, startTime, endTime))
                    .thenReturn(false);
            when(interviewSlotRepositoryPort.save(any(InterviewSlot.class)))
                    .thenReturn(expectedSlot);
            when(interviewRepositoryPort.save(any(Interview.class)))
                    .thenReturn(interview);

            // when
            InterviewSlot result = interviewCommandService.createInterviewSlot(interviewId, startTime, endTime, maxApplicants);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getInterview()).isEqualTo(interview);
            assertThat(result.getStartTime()).isEqualTo(startTime);
            assertThat(result.getEndTime()).isEqualTo(endTime);
            assertThat(result.getMaxApplicants()).isEqualTo(maxApplicants);

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewSlotRepositoryPort).existsByInterviewIdAndTimeRange(interviewId, startTime, endTime);
            verify(interviewSlotRepositoryPort).save(any(InterviewSlot.class));
            verify(interviewRepositoryPort).save(interview);
        }

        @Test
        @DisplayName("최대 지원자 수가 null이면 면접 설정값을 사용한다")
        void createInterviewSlot_maxApplicantsNull_useInterviewDefault() {
            // given
            Long interviewId = 1L;
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            LocalDateTime endTime = startTime.plusHours(1);
            Integer maxApplicants = null;

            Interview interview = givenExistingInterview(interviewId);
            InterviewSlot expectedSlot = new InterviewSlot(interview, startTime, endTime, interview.getMaxApplicantsPerSlot());
            ReflectionTestUtils.setField(expectedSlot, "id", 1L);

            when(interviewSlotRepositoryPort.existsByInterviewIdAndTimeRange(interviewId, startTime, endTime))
                    .thenReturn(false);
            when(interviewSlotRepositoryPort.save(any(InterviewSlot.class)))
                    .thenReturn(expectedSlot);
            when(interviewRepositoryPort.save(any(Interview.class)))
                    .thenReturn(interview);

            // when
            InterviewSlot result = interviewCommandService.createInterviewSlot(interviewId, startTime, endTime, maxApplicants);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMaxApplicants()).isEqualTo(interview.getMaxApplicantsPerSlot());

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewSlotRepositoryPort).save(any(InterviewSlot.class));
        }

        @Test
        @DisplayName("면접이 존재하지 않으면 InterviewNotFoundException을 던진다")
        void createInterviewSlot_interviewNotFound_throwsException() {
            // given
            Long interviewId = 999L;
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            LocalDateTime endTime = startTime.plusHours(1);

            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.createInterviewSlot(interviewId, startTime, endTime, 3))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewSlotRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("시간 충돌이 있으면 InterviewValidationException을 던진다")
        void createInterviewSlot_timeConflict_throwsException() {
            // given
            Long interviewId = 1L;
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            LocalDateTime endTime = startTime.plusHours(1);

            givenExistingInterview(interviewId);
            when(interviewSlotRepositoryPort.existsByInterviewIdAndTimeRange(interviewId, startTime, endTime))
                    .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> interviewCommandService.createInterviewSlot(interviewId, startTime, endTime, 3))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException e = (InterviewValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_TIME_CONFLICT);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewSlotRepositoryPort).existsByInterviewIdAndTimeRange(interviewId, startTime, endTime);
            verify(interviewSlotRepositoryPort, never()).save(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 6) createMultipleInterviewSlots 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("createMultipleInterviewSlots 메서드는")
    class CreateMultipleInterviewSlotsTests {

        @Test
        @DisplayName("정상 입력 시 여러 면접 슬롯을 생성하고 저장된 슬롯 목록을 반환한다")
        void createMultipleInterviewSlots_validInput_success() {
            // given
            Long interviewId = 1L;
            Interview interview = givenExistingInterview(interviewId);

            InterviewCommandService.SlotCreationInfo info1 = new InterviewCommandService.SlotCreationInfo(
                    LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                    LocalDateTime.now().plusDays(1).withHour(15).withMinute(0),
                    3
            );
            InterviewCommandService.SlotCreationInfo info2 = new InterviewCommandService.SlotCreationInfo(
                    LocalDateTime.now().plusDays(1).withHour(16).withMinute(0),
                    LocalDateTime.now().plusDays(1).withHour(17).withMinute(0),
                    null // 면접 기본값 사용
            );

            List<InterviewCommandService.SlotCreationInfo> slotInfos = Arrays.asList(info1, info2);

            InterviewSlot slot1 = new InterviewSlot(interview, info1.startTime(), info1.endTime(), info1.maxApplicants());
            InterviewSlot slot2 = new InterviewSlot(interview, info2.startTime(), info2.endTime(), interview.getMaxApplicantsPerSlot());
            ReflectionTestUtils.setField(slot1, "id", 1L);
            ReflectionTestUtils.setField(slot2, "id", 2L);

            when(interviewSlotRepositoryPort.existsByInterviewIdAndTimeRange(anyLong(), any(), any()))
                    .thenReturn(false);
            when(interviewSlotRepositoryPort.save(any(InterviewSlot.class)))
                    .thenReturn(slot1, slot2);
            when(interviewRepositoryPort.save(any(Interview.class)))
                    .thenReturn(interview);

            // when
            List<InterviewSlot> result = interviewCommandService.createMultipleInterviewSlots(interviewId, slotInfos);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewSlotRepositoryPort, times(2)).existsByInterviewIdAndTimeRange(anyLong(), any(), any());
            verify(interviewSlotRepositoryPort, times(2)).save(any(InterviewSlot.class));
            verify(interviewRepositoryPort).save(interview);
        }

        @Test
        @DisplayName("슬롯 생성 중 시간 충돌이 있으면 InterviewValidationException을 던진다")
        void createMultipleInterviewSlots_timeConflict_throwsException() {
            // given
            Long interviewId = 1L;
            givenExistingInterview(interviewId);

            InterviewCommandService.SlotCreationInfo info1 = new InterviewCommandService.SlotCreationInfo(
                    LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                    LocalDateTime.now().plusDays(1).withHour(15).withMinute(0),
                    3
            );
            List<InterviewCommandService.SlotCreationInfo> slotInfos = Arrays.asList(info1);

            when(interviewSlotRepositoryPort.existsByInterviewIdAndTimeRange(interviewId, info1.startTime(), info1.endTime()))
                    .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> interviewCommandService.createMultipleInterviewSlots(interviewId, slotInfos))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException e = (InterviewValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_TIME_CONFLICT);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewSlotRepositoryPort).existsByInterviewIdAndTimeRange(interviewId, info1.startTime(), info1.endTime());
            verify(interviewSlotRepositoryPort, never()).save(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 7) cancelInterviewSlot 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("cancelInterviewSlot 메서드는")
    class CancelInterviewSlotTests {

        @Test
        @DisplayName("정상 입력 시 슬롯을 취소하고 저장된 슬롯을 반환한다")
        void cancelInterviewSlot_validInput_success() {
            // given
            Long slotId = 1L;
            InterviewSlot slot = givenExistingSlot(slotId);
            when(interviewSlotRepositoryPort.save(any(InterviewSlot.class))).thenReturn(slot);

            // when
            InterviewSlot result = interviewCommandService.cancelInterviewSlot(slotId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(slotId);

            verify(interviewSlotRepositoryPort).findById(slotId);
            verify(interviewSlotRepositoryPort).save(slot);
        }

        @Test
        @DisplayName("슬롯이 존재하지 않으면 InterviewNotFoundException을 던진다")
        void cancelInterviewSlot_slotNotFound_throwsException() {
            // given
            Long slotId = 999L;
            when(interviewSlotRepositoryPort.findById(slotId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.cancelInterviewSlot(slotId))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_NOT_FOUND);
                    });

            verify(interviewSlotRepositoryPort).findById(slotId);
            verify(interviewSlotRepositoryPort, never()).save(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 8) reactivateInterviewSlot 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("reactivateInterviewSlot 메서드는")
    class ReactivateInterviewSlotTests {

        @Test
        @DisplayName("정상 입력 시 슬롯을 재활성화하고 저장된 슬롯을 반환한다")
        void reactivateInterviewSlot_validInput_success() {
            // given
            Long slotId = 1L;
            InterviewSlot slot = givenCancelledSlot(slotId);
            when(interviewSlotRepositoryPort.save(any(InterviewSlot.class))).thenReturn(slot);

            // when
            InterviewSlot result = interviewCommandService.reactivateInterviewSlot(slotId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(slotId);

            verify(interviewSlotRepositoryPort).findById(slotId);
            verify(interviewSlotRepositoryPort).save(slot);
        }

        @Test
        @DisplayName("슬롯이 존재하지 않으면 InterviewNotFoundException을 던진다")
        void reactivateInterviewSlot_slotNotFound_throwsException() {
            // given
            Long slotId = 999L;
            when(interviewSlotRepositoryPort.findById(slotId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.reactivateInterviewSlot(slotId))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_NOT_FOUND);
                    });

            verify(interviewSlotRepositoryPort).findById(slotId);
            verify(interviewSlotRepositoryPort, never()).save(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 9) deleteInterviewSlot 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("deleteInterviewSlot 메서드는")
    class DeleteInterviewSlotTests {

        @Test
        @DisplayName("예약이 없는 슬롯을 정상적으로 삭제한다")
        void deleteInterviewSlot_noReservations_success() {
            // given
            Long slotId = 1L;
            InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithId(slotId);
            // 예약이 없는 상태로 설정
            ReflectionTestUtils.setField(slot, "currentApplicants", 0);

            when(interviewSlotRepositoryPort.findById(slotId)).thenReturn(Optional.of(slot));

            // when
            interviewCommandService.deleteInterviewSlot(slotId);

            // then
            verify(interviewSlotRepositoryPort).findById(slotId);
            verify(interviewSlotRepositoryPort).delete(slot);
        }

        @Test
        @DisplayName("예약이 있는 슬롯 삭제 시 InterviewValidationException을 던진다")
        void deleteInterviewSlot_hasReservations_throwsException() {
            // given
            Long slotId = 1L;
            InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithId(slotId);
            // 예약이 있는 상태로 설정
            ReflectionTestUtils.setField(slot, "currentApplicants", 2);

            when(interviewSlotRepositoryPort.findById(slotId)).thenReturn(Optional.of(slot));

            // when & then
            assertThatThrownBy(() -> interviewCommandService.deleteInterviewSlot(slotId))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException e = (InterviewValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.CANNOT_CANCEL_SLOT_WITH_APPLICANTS);
                    });

            verify(interviewSlotRepositoryPort).findById(slotId);
            verify(interviewSlotRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("슬롯이 존재하지 않으면 InterviewNotFoundException을 던진다")
        void deleteInterviewSlot_slotNotFound_throwsException() {
            // given
            Long slotId = 999L;
            when(interviewSlotRepositoryPort.findById(slotId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.deleteInterviewSlot(slotId))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_NOT_FOUND);
                    });

            verify(interviewSlotRepositoryPort).findById(slotId);
            verify(interviewSlotRepositoryPort, never()).delete(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 10) deleteInterview 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("deleteInterview 메서드는")
    class DeleteInterviewTests {

        @Test
        @DisplayName("예약이 없는 비활성화된 면접을 정상적으로 삭제한다")
        void deleteInterview_inactiveWithoutReservations_success() {
            // given
            Long interviewId = 1L;
            Interview interview = DomainInterviewFactory.buildInterviewWithId(interviewId);
            // 비활성화 상태로 설정
            ReflectionTestUtils.setField(interview, "status", InterviewStatus.INACTIVE);

            List<InterviewSlot> slots = Arrays.asList(
                    DomainInterviewSlotFactory.buildSlotWithApplicants(0, 5)
            );

            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.of(interview));
            when(interviewSlotRepositoryPort.findByInterviewId(interviewId)).thenReturn(slots);

            // when
            interviewCommandService.deleteInterview(interviewId);

            // then
            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewSlotRepositoryPort).findByInterviewId(interviewId);
            verify(interviewSlotRepositoryPort).deleteByInterviewId(interviewId);
            verify(interviewRepositoryPort).delete(interview);
        }

        @Test
        @DisplayName("활성화된 면접 삭제 시 InterviewValidationException을 던진다")
        void deleteInterview_activeInterview_throwsException() {
            // given
            Long interviewId = 1L;
            Interview interview = DomainInterviewFactory.buildInterviewWithId(interviewId);
            // 활성화 상태로 설정
            ReflectionTestUtils.setField(interview, "status", InterviewStatus.ACTIVE);

            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.of(interview));

            // when & then
            assertThatThrownBy(() -> interviewCommandService.deleteInterview(interviewId))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException e = (InterviewValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.CANNOT_DEACTIVATE_CLOSED);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("예약된 슬롯이 있는 면접 삭제 시 InterviewValidationException을 던진다")
        void deleteInterview_hasReservations_throwsException() {
            // given
            Long interviewId = 1L;
            Interview interview = DomainInterviewFactory.buildInterviewWithId(interviewId);
            // 비활성화 상태로 설정
            ReflectionTestUtils.setField(interview, "status", InterviewStatus.INACTIVE);

            List<InterviewSlot> slots = Arrays.asList(
                    DomainInterviewSlotFactory.buildSlotWithApplicants(2, 5) // 예약이 있는 슬롯
            );

            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.of(interview));
            when(interviewSlotRepositoryPort.findByInterviewId(interviewId)).thenReturn(slots);

            // when & then
            assertThatThrownBy(() -> interviewCommandService.deleteInterview(interviewId))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException e = (InterviewValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.CANNOT_CANCEL_SLOT_WITH_APPLICANTS);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewSlotRepositoryPort).findByInterviewId(interviewId);
            verify(interviewRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("면접이 존재하지 않으면 InterviewNotFoundException을 던진다")
        void deleteInterview_interviewNotFound_throwsException() {
            // given
            Long interviewId = 999L;
            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.deleteInterview(interviewId))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort, never()).delete(any());
        }
    }

    // ——————————————————————————————————————————————————————————
    // 11) updateInterview 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("updateInterview 메서드는")
    class UpdateInterviewTests {

        @Test
        @DisplayName("비활성화된 면접을 정상적으로 업데이트한다")
        void updateInterview_inactiveInterview_success() {
            // given
            Long interviewId = 1L;
            LocalDate newStartDate = LocalDate.now().plusDays(2);
            LocalDate newEndDate = newStartDate.plusDays(5);
            Integer newDurationMinutes = 90;
            Integer newMaxApplicantsPerSlot = 3;

            Interview interview = DomainInterviewFactory.buildInterviewWithId(interviewId);
            // 비활성화 상태로 설정
            ReflectionTestUtils.setField(interview, "status", InterviewStatus.INACTIVE);
            Lab lab = interview.getLab();

            Interview updatedInterview = new Interview(lab, newStartDate, newEndDate, newDurationMinutes, newMaxApplicantsPerSlot);
            ReflectionTestUtils.setField(updatedInterview, "id", 2L);

            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.of(interview));
            when(interviewRepositoryPort.save(any(Interview.class))).thenReturn(updatedInterview);

            // when
            Interview result = interviewCommandService.updateInterview(interviewId, newStartDate, newEndDate, newDurationMinutes, newMaxApplicantsPerSlot);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getStartDate()).isEqualTo(newStartDate);
            assertThat(result.getEndDate()).isEqualTo(newEndDate);
            assertThat(result.getDurationMinutes()).isEqualTo(newDurationMinutes);
            assertThat(result.getMaxApplicantsPerSlot()).isEqualTo(newMaxApplicantsPerSlot);

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort).delete(interview);
            verify(interviewRepositoryPort).save(any(Interview.class));
        }

        @Test
        @DisplayName("활성화된 면접 업데이트 시 InterviewValidationException을 던진다")
        void updateInterview_activeInterview_throwsException() {
            // given
            Long interviewId = 1L;
            LocalDate newStartDate = LocalDate.now().plusDays(2);
            LocalDate newEndDate = newStartDate.plusDays(5);

            Interview interview = DomainInterviewFactory.buildInterviewWithId(interviewId);
            // 활성화 상태로 설정
            ReflectionTestUtils.setField(interview, "status", InterviewStatus.ACTIVE);

            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.of(interview));

            // when & then
            assertThatThrownBy(() -> interviewCommandService.updateInterview(interviewId, newStartDate, newEndDate, 90, 3))
                    .isInstanceOf(InterviewValidationException.class)
                    .satisfies(ex -> {
                        InterviewValidationException e = (InterviewValidationException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.ALREADY_ACTIVATED);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort, never()).save(any());
            verify(interviewRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("면접이 존재하지 않으면 InterviewNotFoundException을 던진다")
        void updateInterview_interviewNotFound_throwsException() {
            // given
            Long interviewId = 999L;
            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewCommandService.updateInterview(interviewId, LocalDate.now(), LocalDate.now().plusDays(7), 60, 5))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
            verify(interviewRepositoryPort, never()).save(any());
            verify(interviewRepositoryPort, never()).delete(any());
        }
    }
}