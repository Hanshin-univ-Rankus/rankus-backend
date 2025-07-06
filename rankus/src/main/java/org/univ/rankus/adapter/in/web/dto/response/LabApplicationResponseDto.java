package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.application.LabApplication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class LabApplicationResponseDto {
    private final Long id;
    private final Long labId;
    private final UserResponseDto applicant;
    private final InterviewSlotResponseDto interviewSlot;
    private final LocalDateTime interviewTime; // 호환성을 위한 필드
    private final String status;

    public static LabApplicationResponseDto from(LabApplication labApplication) {
        return LabApplicationResponseDto.builder()
                .id(labApplication.getId())
                .labId(labApplication.getLab().getId())
                .applicant(UserResponseDto.from(labApplication.getUser()))
                .interviewSlot(labApplication.getInterviewSlot() != null ?
                        InterviewSlotResponseDto.from(labApplication.getInterviewSlot()) : null)
                .interviewTime(labApplication.getInterviewTime()) // 호환성을 위한 필드
                .status(labApplication.getStatus().name())
                .build();
    }

    public static List<LabApplicationResponseDto> fromList(List<LabApplication> labApplications) {
        return labApplications.stream()
                .map(LabApplicationResponseDto::from)
                .collect(Collectors.toList());
    }
}