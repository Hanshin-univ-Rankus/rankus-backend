package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.interview.Interview;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class InterviewResponseDto {
    private final Long id;
    private final Long labId;
    private final String labName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer durationMinutes;
    private final Integer maxApplicantsPerSlot;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static InterviewResponseDto from(Interview interview) {
        return InterviewResponseDto.builder()
                .id(interview.getId())
                .labId(interview.getLab().getId())
                .labName(interview.getLab().getName())
                .startDate(interview.getStartDate())
                .endDate(interview.getEndDate())
                .durationMinutes(interview.getDurationMinutes())
                .maxApplicantsPerSlot(interview.getMaxApplicantsPerSlot())
                .status(interview.getStatus().name())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }

    public static List<InterviewResponseDto> fromList(List<Interview> interviews) {
        return interviews.stream()
                .map(InterviewResponseDto::from)
                .collect(Collectors.toList());
    }
}