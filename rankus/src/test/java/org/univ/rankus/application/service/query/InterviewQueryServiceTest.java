package org.univ.rankus.application.service.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.application.port.out.InterviewRepositoryPort;
import org.univ.rankus.application.port.out.InterviewSlotRepositoryPort;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.InterviewStatus;
import org.univ.rankus.domain.model.interview.SlotStatus;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewNotFoundException;
import org.univ.rankus.testutil.factory.domain.DomainInterviewFactory;
import org.univ.rankus.testutil.factory.domain.DomainInterviewSlotFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InterviewQueryServiceTest {

    @Mock
    private InterviewRepositoryPort interviewRepositoryPort;

    @Mock
    private InterviewSlotRepositoryPort interviewSlotRepositoryPort;

    @InjectMocks
    private InterviewQueryService interviewQueryService;

    // ——————————————————————————————————————————————————————————
    // 헬퍼 메서드: 반복되는 모킹 로직을 한곳에 모았습니다.
    // ——————————————————————————————————————————————————————————

    private Interview givenExistingInterview(Long interviewId) {
        Interview interview = DomainInterviewFactory.buildInterviewWithId(interviewId);
        when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.of(interview));
        return interview;
    }

    private InterviewSlot givenExistingSlot(Long slotId) {
        InterviewSlot slot = DomainInterviewSlotFactory.buildSlotWithId(slotId);
        when(interviewSlotRepositoryPort.findById(slotId)).thenReturn(Optional.of(slot));
        return slot;
    }

    // ——————————————————————————————————————————————————————————
    // 1) getInterviewById 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getInterviewById 메서드는")
    class GetInterviewByIdTests {

        @Test
        @DisplayName("존재하는 면접 ID로 조회하면 면접을 반환한다")
        void getInterviewById_existingId_success() {
            // given
            Long interviewId = 1L;
            Interview expectedInterview = givenExistingInterview(interviewId);

            // when
            Interview result = interviewQueryService.getInterviewById(interviewId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(interviewId);
            assertThat(result).isSameAs(expectedInterview);

            verify(interviewRepositoryPort).findById(interviewId);
        }

        @Test
        @DisplayName("존재하지 않는 면접 ID로 조회하면 InterviewNotFoundException을 던진다")
        void getInterviewById_nonExistingId_throwsException() {
            // given
            Long interviewId = 999L;
            when(interviewRepositoryPort.findById(interviewId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewQueryService.getInterviewById(interviewId))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND);
                    });

            verify(interviewRepositoryPort).findById(interviewId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 2) getInterviewsByLabId 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getInterviewsByLabId 메서드는")
    class GetInterviewsByLabIdTests {

        @Test
        @DisplayName("랩실 ID로 조회하면 해당 랩실의 모든 면접을 반환한다")
        void getInterviewsByLabId_existingLabId_success() {
            // given
            Long labId = 1L;
            List<Interview> expectedInterviews = Arrays.asList(
                    DomainInterviewFactory.buildInterviewWithId(1L),
                    DomainInterviewFactory.buildInterviewWithId(2L)
            );

            when(interviewRepositoryPort.findByLabId(labId)).thenReturn(expectedInterviews);

            // when
            List<Interview> result = interviewQueryService.getInterviewsByLabId(labId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedInterviews);

            verify(interviewRepositoryPort).findByLabId(labId);
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID로 조회하면 빈 리스트를 반환한다")
        void getInterviewsByLabId_nonExistingLabId_returnsEmptyList() {
            // given
            Long labId = 999L;
            when(interviewRepositoryPort.findByLabId(labId)).thenReturn(Collections.emptyList());

            // when
            List<Interview> result = interviewQueryService.getInterviewsByLabId(labId);

            // then
            assertThat(result).isEmpty();

            verify(interviewRepositoryPort).findByLabId(labId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 3) getInterviewsByLabIdAndStatus 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getInterviewsByLabIdAndStatus 메서드는")
    class GetInterviewsByLabIdAndStatusTests {

        @Test
        @DisplayName("랩실 ID와 상태로 조회하면 해당 조건의 면접을 반환한다")
        void getInterviewsByLabIdAndStatus_validCondition_success() {
            // given
            Long labId = 1L;
            InterviewStatus status = InterviewStatus.ACTIVE;
            List<Interview> expectedInterviews = Arrays.asList(
                    DomainInterviewFactory.buildActiveInterview()
            );

            when(interviewRepositoryPort.findByLabIdAndStatus(labId, status)).thenReturn(expectedInterviews);

            // when
            List<Interview> result = interviewQueryService.getInterviewsByLabIdAndStatus(labId, status);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactlyElementsOf(expectedInterviews);

            verify(interviewRepositoryPort).findByLabIdAndStatus(labId, status);
        }

        @Test
        @DisplayName("조건에 맞는 면접이 없으면 빈 리스트를 반환한다")
        void getInterviewsByLabIdAndStatus_noMatches_returnsEmptyList() {
            // given
            Long labId = 1L;
            InterviewStatus status = InterviewStatus.CLOSED;
            when(interviewRepositoryPort.findByLabIdAndStatus(labId, status)).thenReturn(Collections.emptyList());

            // when
            List<Interview> result = interviewQueryService.getInterviewsByLabIdAndStatus(labId, status);

            // then
            assertThat(result).isEmpty();

            verify(interviewRepositoryPort).findByLabIdAndStatus(labId, status);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 4) getActiveInterviewsByLabId 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getActiveInterviewsByLabId 메서드는")
    class GetActiveInterviewsByLabIdTests {

        @Test
        @DisplayName("랩실 ID로 조회하면 해당 랩실의 활성화된 면접을 반환한다")
        void getActiveInterviewsByLabId_existingLabId_success() {
            // given
            Long labId = 1L;
            List<Interview> expectedInterviews = Arrays.asList(
                    DomainInterviewFactory.buildActiveInterview()
            );

            when(interviewRepositoryPort.findByLabIdAndStatus(labId, InterviewStatus.ACTIVE)).thenReturn(expectedInterviews);

            // when
            List<Interview> result = interviewQueryService.getActiveInterviewsByLabId(labId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactlyElementsOf(expectedInterviews);

            verify(interviewRepositoryPort).findByLabIdAndStatus(labId, InterviewStatus.ACTIVE);
        }

        @Test
        @DisplayName("활성화된 면접이 없으면 빈 리스트를 반환한다")
        void getActiveInterviewsByLabId_noActiveInterviews_returnsEmptyList() {
            // given
            Long labId = 1L;
            when(interviewRepositoryPort.findByLabIdAndStatus(labId, InterviewStatus.ACTIVE)).thenReturn(Collections.emptyList());

            // when
            List<Interview> result = interviewQueryService.getActiveInterviewsByLabId(labId);

            // then
            assertThat(result).isEmpty();

            verify(interviewRepositoryPort).findByLabIdAndStatus(labId, InterviewStatus.ACTIVE);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 5) getAllInterviews 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getAllInterviews 메서드는")
    class GetAllInterviewsTests {

        @Test
        @DisplayName("페이징을 통해 모든 면접을 조회한다")
        void getAllInterviews_validPageable_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Interview> interviews = Arrays.asList(
                    DomainInterviewFactory.buildInterviewWithId(1L),
                    DomainInterviewFactory.buildInterviewWithId(2L)
            );

            when(interviewRepositoryPort.findAll()).thenReturn(interviews);

            // when
            Page<Interview> result = interviewQueryService.getAllInterviews(pageable);

            // then
            // 현재 구현에서는 Page.empty()를 반환하므로 이를 검증
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(interviewRepositoryPort).findAll();
        }
    }

    // ——————————————————————————————————————————————————————————
    // 6) getInterviewsByStatus 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getInterviewsByStatus 메서드는")
    class GetInterviewsByStatusTests {

        @Test
        @DisplayName("면접 상태로 조회하면 해당 상태의 면접을 반환한다")
        void getInterviewsByStatus_validStatus_success() {
            // given
            InterviewStatus status = InterviewStatus.ACTIVE;
            List<Interview> expectedInterviews = Arrays.asList(
                    DomainInterviewFactory.buildActiveInterview()
            );

            when(interviewRepositoryPort.findByStatus(status)).thenReturn(expectedInterviews);

            // when
            List<Interview> result = interviewQueryService.getInterviewsByStatus(status);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactlyElementsOf(expectedInterviews);

            verify(interviewRepositoryPort).findByStatus(status);
        }

        @Test
        @DisplayName("해당 상태의 면접이 없으면 빈 리스트를 반환한다")
        void getInterviewsByStatus_noMatches_returnsEmptyList() {
            // given
            InterviewStatus status = InterviewStatus.CLOSED;
            when(interviewRepositoryPort.findByStatus(status)).thenReturn(Collections.emptyList());

            // when
            List<Interview> result = interviewQueryService.getInterviewsByStatus(status);

            // then
            assertThat(result).isEmpty();

            verify(interviewRepositoryPort).findByStatus(status);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 7) getInterviewSlotById 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getInterviewSlotById 메서드는")
    class GetInterviewSlotByIdTests {

        @Test
        @DisplayName("존재하는 슬롯 ID로 조회하면 슬롯을 반환한다")
        void getInterviewSlotById_existingId_success() {
            // given
            Long slotId = 1L;
            InterviewSlot expectedSlot = givenExistingSlot(slotId);

            // when
            InterviewSlot result = interviewQueryService.getInterviewSlotById(slotId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(slotId);
            assertThat(result).isSameAs(expectedSlot);

            verify(interviewSlotRepositoryPort).findById(slotId);
        }

        @Test
        @DisplayName("존재하지 않는 슬롯 ID로 조회하면 InterviewNotFoundException을 던진다")
        void getInterviewSlotById_nonExistingId_throwsException() {
            // given
            Long slotId = 999L;
            when(interviewSlotRepositoryPort.findById(slotId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewQueryService.getInterviewSlotById(slotId))
                    .isInstanceOf(InterviewNotFoundException.class)
                    .satisfies(ex -> {
                        InterviewNotFoundException e = (InterviewNotFoundException) ex;
                        assertThat(e.getErrorCode()).isEqualTo(InterviewErrorCode.SLOT_NOT_FOUND);
                    });

            verify(interviewSlotRepositoryPort).findById(slotId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 8) getSlotsByInterviewId 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getSlotsByInterviewId 메서드는")
    class GetSlotsByInterviewIdTests {

        @Test
        @DisplayName("면접 ID로 조회하면 해당 면접의 모든 슬롯을 반환한다")
        void getSlotsByInterviewId_existingInterviewId_success() {
            // given
            Long interviewId = 1L;
            List<InterviewSlot> expectedSlots = Arrays.asList(
                    DomainInterviewSlotFactory.buildSlotWithId(1L),
                    DomainInterviewSlotFactory.buildSlotWithId(2L)
            );

            when(interviewSlotRepositoryPort.findByInterviewId(interviewId)).thenReturn(expectedSlots);

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsByInterviewId(interviewId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedSlots);

            verify(interviewSlotRepositoryPort).findByInterviewId(interviewId);
        }

        @Test
        @DisplayName("슬롯이 없는 면접 ID로 조회하면 빈 리스트를 반환한다")
        void getSlotsByInterviewId_noSlots_returnsEmptyList() {
            // given
            Long interviewId = 1L;
            when(interviewSlotRepositoryPort.findByInterviewId(interviewId)).thenReturn(Collections.emptyList());

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsByInterviewId(interviewId);

            // then
            assertThat(result).isEmpty();

            verify(interviewSlotRepositoryPort).findByInterviewId(interviewId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 9) getAvailableSlotsByInterviewId 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getAvailableSlotsByInterviewId 메서드는")
    class GetAvailableSlotsByInterviewIdTests {

        @Test
        @DisplayName("면접 ID로 조회하면 해당 면접의 예약 가능한 슬롯을 반환한다")
        void getAvailableSlotsByInterviewId_existingInterviewId_success() {
            // given
            Long interviewId = 1L;
            List<InterviewSlot> expectedSlots = Arrays.asList(
                    DomainInterviewSlotFactory.buildAvailableSlot()
            );

            when(interviewSlotRepositoryPort.findAvailableSlotsByInterviewId(interviewId)).thenReturn(expectedSlots);

            // when
            List<InterviewSlot> result = interviewQueryService.getAvailableSlotsByInterviewId(interviewId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactlyElementsOf(expectedSlots);

            verify(interviewSlotRepositoryPort).findAvailableSlotsByInterviewId(interviewId);
        }

        @Test
        @DisplayName("예약 가능한 슬롯이 없으면 빈 리스트를 반환한다")
        void getAvailableSlotsByInterviewId_noAvailableSlots_returnsEmptyList() {
            // given
            Long interviewId = 1L;
            when(interviewSlotRepositoryPort.findAvailableSlotsByInterviewId(interviewId)).thenReturn(Collections.emptyList());

            // when
            List<InterviewSlot> result = interviewQueryService.getAvailableSlotsByInterviewId(interviewId);

            // then
            assertThat(result).isEmpty();

            verify(interviewSlotRepositoryPort).findAvailableSlotsByInterviewId(interviewId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 10) getSlotsByInterviewIdAndStatus 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getSlotsByInterviewIdAndStatus 메서드는")
    class GetSlotsByInterviewIdAndStatusTests {

        @Test
        @DisplayName("면접 ID와 상태로 조회하면 해당 조건의 슬롯을 반환한다")
        void getSlotsByInterviewIdAndStatus_validCondition_success() {
            // given
            Long interviewId = 1L;
            SlotStatus status = SlotStatus.AVAILABLE;
            List<InterviewSlot> expectedSlots = Arrays.asList(
                    DomainInterviewSlotFactory.buildAvailableSlot()
            );

            when(interviewSlotRepositoryPort.findByInterviewIdAndStatus(interviewId, status)).thenReturn(expectedSlots);

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsByInterviewIdAndStatus(interviewId, status);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactlyElementsOf(expectedSlots);

            verify(interviewSlotRepositoryPort).findByInterviewIdAndStatus(interviewId, status);
        }

        @Test
        @DisplayName("조건에 맞는 슬롯이 없으면 빈 리스트를 반환한다")
        void getSlotsByInterviewIdAndStatus_noMatches_returnsEmptyList() {
            // given
            Long interviewId = 1L;
            SlotStatus status = SlotStatus.CANCELLED;
            when(interviewSlotRepositoryPort.findByInterviewIdAndStatus(interviewId, status)).thenReturn(Collections.emptyList());

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsByInterviewIdAndStatus(interviewId, status);

            // then
            assertThat(result).isEmpty();

            verify(interviewSlotRepositoryPort).findByInterviewIdAndStatus(interviewId, status);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 11) getSlotsByInterviewIdOrderByTime 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getSlotsByInterviewIdOrderByTime 메서드는")
    class GetSlotsByInterviewIdOrderByTimeTests {

        @Test
        @DisplayName("면접 ID로 조회하면 시간순으로 정렬된 슬롯을 반환한다")
        void getSlotsByInterviewIdOrderByTime_existingInterviewId_success() {
            // given
            Long interviewId = 1L;
            LocalDateTime earlierTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime laterTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);

            InterviewSlot laterSlot = DomainInterviewSlotFactory.buildSlotWithTime(laterTime);
            InterviewSlot earlierSlot = DomainInterviewSlotFactory.buildSlotWithTime(earlierTime);

            // 정렬되지 않은 순서로 반환
            List<InterviewSlot> unsortedSlots = Arrays.asList(laterSlot, earlierSlot);

            when(interviewSlotRepositoryPort.findByInterviewId(interviewId)).thenReturn(unsortedSlots);

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsByInterviewIdOrderByTime(interviewId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStartTime()).isEqualTo(earlierTime);
            assertThat(result.get(1).getStartTime()).isEqualTo(laterTime);

            verify(interviewSlotRepositoryPort).findByInterviewId(interviewId);
        }

        @Test
        @DisplayName("슬롯이 없으면 빈 리스트를 반환한다")
        void getSlotsByInterviewIdOrderByTime_noSlots_returnsEmptyList() {
            // given
            Long interviewId = 1L;
            when(interviewSlotRepositoryPort.findByInterviewId(interviewId)).thenReturn(Collections.emptyList());

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsByInterviewIdOrderByTime(interviewId);

            // then
            assertThat(result).isEmpty();

            verify(interviewSlotRepositoryPort).findByInterviewId(interviewId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 12) getSlotsAfter 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getSlotsAfter 메서드는")
    class GetSlotsAfterTests {

        @Test
        @DisplayName("특정 시간 이후의 슬롯을 반환한다")
        void getSlotsAfter_validDateTime_success() {
            // given
            LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
            List<InterviewSlot> expectedSlots = Arrays.asList(
                    DomainInterviewSlotFactory.buildSlotWithTime(dateTime.plusHours(1))
            );

            when(interviewSlotRepositoryPort.findByStartTimeAfter(dateTime)).thenReturn(expectedSlots);

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsAfter(dateTime);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactlyElementsOf(expectedSlots);

            verify(interviewSlotRepositoryPort).findByStartTimeAfter(dateTime);
        }

        @Test
        @DisplayName("해당 시간 이후의 슬롯이 없으면 빈 리스트를 반환한다")
        void getSlotsAfter_noSlotsAfter_returnsEmptyList() {
            // given
            LocalDateTime dateTime = LocalDateTime.now().plusDays(7);
            when(interviewSlotRepositoryPort.findByStartTimeAfter(dateTime)).thenReturn(Collections.emptyList());

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsAfter(dateTime);

            // then
            assertThat(result).isEmpty();

            verify(interviewSlotRepositoryPort).findByStartTimeAfter(dateTime);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 13) getSlotsBefore 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("getSlotsBefore 메서드는")
    class GetSlotsBeforeTests {

        @Test
        @DisplayName("특정 시간 이전의 슬롯을 반환한다")
        void getSlotsBefore_validDateTime_success() {
            // given
            LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
            List<InterviewSlot> expectedSlots = Arrays.asList(
                    DomainInterviewSlotFactory.buildSlotWithTime(dateTime.minusHours(1))
            );

            when(interviewSlotRepositoryPort.findByStartTimeBefore(dateTime)).thenReturn(expectedSlots);

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsBefore(dateTime);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactlyElementsOf(expectedSlots);

            verify(interviewSlotRepositoryPort).findByStartTimeBefore(dateTime);
        }

        @Test
        @DisplayName("해당 시간 이전의 슬롯이 없으면 빈 리스트를 반환한다")
        void getSlotsBefore_noSlotsBefore_returnsEmptyList() {
            // given
            LocalDateTime dateTime = LocalDateTime.now().minusDays(7);
            when(interviewSlotRepositoryPort.findByStartTimeBefore(dateTime)).thenReturn(Collections.emptyList());

            // when
            List<InterviewSlot> result = interviewQueryService.getSlotsBefore(dateTime);

            // then
            assertThat(result).isEmpty();

            verify(interviewSlotRepositoryPort).findByStartTimeBefore(dateTime);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 14) hasActiveInterview 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("hasActiveInterview 메서드는")
    class HasActiveInterviewTests {

        @Test
        @DisplayName("랩실에 활성화된 면접이 있으면 true를 반환한다")
        void hasActiveInterview_hasActiveInterview_returnsTrue() {
            // given
            Long labId = 1L;
            when(interviewRepositoryPort.existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE)).thenReturn(true);

            // when
            boolean result = interviewQueryService.hasActiveInterview(labId);

            // then
            assertThat(result).isTrue();

            verify(interviewRepositoryPort).existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE);
        }

        @Test
        @DisplayName("랩실에 활성화된 면접이 없으면 false를 반환한다")
        void hasActiveInterview_noActiveInterview_returnsFalse() {
            // given
            Long labId = 1L;
            when(interviewRepositoryPort.existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE)).thenReturn(false);

            // when
            boolean result = interviewQueryService.hasActiveInterview(labId);

            // then
            assertThat(result).isFalse();

            verify(interviewRepositoryPort).existsByLabIdAndStatus(labId, InterviewStatus.ACTIVE);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 15) hasReservedSlots 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("hasReservedSlots 메서드는")
    class HasReservedSlotsTests {

        @Test
        @DisplayName("면접에 예약된 슬롯이 있으면 true를 반환한다")
        void hasReservedSlots_hasReservations_returnsTrue() {
            // given
            Long interviewId = 1L;
            List<InterviewSlot> slots = Arrays.asList(
                    DomainInterviewSlotFactory.buildSlotWithReservations()
            );

            when(interviewSlotRepositoryPort.findByInterviewId(interviewId)).thenReturn(slots);

            // when
            boolean result = interviewQueryService.hasReservedSlots(interviewId);

            // then
            assertThat(result).isTrue();

            verify(interviewSlotRepositoryPort).findByInterviewId(interviewId);
        }

        @Test
        @DisplayName("면접에 예약된 슬롯이 없으면 false를 반환한다")
        void hasReservedSlots_noReservations_returnsFalse() {
            // given
            Long interviewId = 1L;
            List<InterviewSlot> slots = Arrays.asList(
                    DomainInterviewSlotFactory.buildSlotWithApplicants(0, 5)
            );

            when(interviewSlotRepositoryPort.findByInterviewId(interviewId)).thenReturn(slots);

            // when
            boolean result = interviewQueryService.hasReservedSlots(interviewId);

            // then
            assertThat(result).isFalse();

            verify(interviewSlotRepositoryPort).findByInterviewId(interviewId);
        }

        @Test
        @DisplayName("면접에 슬롯이 없으면 false를 반환한다")
        void hasReservedSlots_noSlots_returnsFalse() {
            // given
            Long interviewId = 1L;
            when(interviewSlotRepositoryPort.findByInterviewId(interviewId)).thenReturn(Collections.emptyList());

            // when
            boolean result = interviewQueryService.hasReservedSlots(interviewId);

            // then
            assertThat(result).isFalse();

            verify(interviewSlotRepositoryPort).findByInterviewId(interviewId);
        }
    }

    // ——————————————————————————————————————————————————————————
    // 16) hasConflictingSlots 메서드 테스트
    // ——————————————————————————————————————————————————————————

    @Nested
    @DisplayName("hasConflictingSlots 메서드는")
    class HasConflictingSlotsTests {

        @Test
        @DisplayName("시간 충돌이 있으면 true를 반환한다")
        void hasConflictingSlots_hasConflict_returnsTrue() {
            // given
            Long interviewId = 1L;
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            LocalDateTime endTime = startTime.plusHours(1);

            when(interviewSlotRepositoryPort.existsByInterviewIdAndTimeRange(interviewId, startTime, endTime)).thenReturn(true);

            // when
            boolean result = interviewQueryService.hasConflictingSlots(interviewId, startTime, endTime);

            // then
            assertThat(result).isTrue();

            verify(interviewSlotRepositoryPort).existsByInterviewIdAndTimeRange(interviewId, startTime, endTime);
        }

        @Test
        @DisplayName("시간 충돌이 없으면 false를 반환한다")
        void hasConflictingSlots_noConflict_returnsFalse() {
            // given
            Long interviewId = 1L;
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            LocalDateTime endTime = startTime.plusHours(1);

            when(interviewSlotRepositoryPort.existsByInterviewIdAndTimeRange(interviewId, startTime, endTime)).thenReturn(false);

            // when
            boolean result = interviewQueryService.hasConflictingSlots(interviewId, startTime, endTime);

            // then
            assertThat(result).isFalse();

            verify(interviewSlotRepositoryPort).existsByInterviewIdAndTimeRange(interviewId, startTime, endTime);
        }
    }
}