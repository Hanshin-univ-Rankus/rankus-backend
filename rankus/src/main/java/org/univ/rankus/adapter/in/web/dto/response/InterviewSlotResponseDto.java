package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.interview.InterviewSlot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class InterviewSlotResponseDto {
    private final Long id;
    private final Long interviewId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Integer maxApplicants;
    private final Integer currentApplicants;
    private final Integer availableSpots;
    private final String status;
    private final Boolean isAvailable;
    private final Boolean isFull;
    private final Boolean isPast;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static InterviewSlotResponseDto from(InterviewSlot slot) {
        return InterviewSlotResponseDto.builder()
                .id(slot.getId())
                .interviewId(slot.getInterview().getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .maxApplicants(slot.getMaxApplicants())
                .currentApplicants(slot.getCurrentApplicants())
                .availableSpots(slot.getAvailableSpots())
                .status(slot.getStatus().name())
                .isAvailable(slot.isAvailable())
                .isFull(slot.isFull())
                .isPast(slot.isPast())
                .createdAt(slot.getCreatedAt())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }

    public static List<InterviewSlotResponseDto> fromList(List<InterviewSlot> slots) {
        return slots.stream()
                .map(InterviewSlotResponseDto::from)
                .collect(Collectors.toList());
    }
}