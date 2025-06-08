package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.LabApplication;

import java.time.LocalDateTime;

@Getter @Builder
public class LabApplicationResponseDto {
    private final Long               id;
    private final Long               labId;
    private final UserResponseDto    applicant;
    private final LocalDateTime      interviewTime;
    private final String             status;

    public static LabApplicationResponseDto from(Long id, Long labId, UserResponseDto applicant, LocalDateTime interviewTime, String status) {
        return LabApplicationResponseDto.builder()
                .id(id)
                .labId(labId)
                .applicant(applicant)
                .interviewTime(interviewTime)
                .status(status)
                .build();
    }

    public static LabApplicationResponseDto from(LabApplication labApplication) {
        return from(
                labApplication.getId(),
                labApplication.getLab().getId(),
                UserResponseDto.from(labApplication.getUser()),
                labApplication.getInterviewTime(),
                labApplication.getStatus().name()
        );
    }
}