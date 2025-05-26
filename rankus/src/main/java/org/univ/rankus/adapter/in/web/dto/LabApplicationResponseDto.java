package org.univ.rankus.adapter.in.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.lab.ApplicationStatus;
import org.univ.rankus.domain.model.lab.LabApplication;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class LabApplicationResponseDto {

    private Long userId;
    private LocalDateTime interviewTime;
    private ApplicationStatus status;

    public static LabApplicationResponseDto from(LabApplication app) {
        LabApplicationResponseDto dto = new LabApplicationResponseDto();
        dto.userId = app.getUserId();
        dto.interviewTime = app.getInterviewTime();
        dto.status = app.getStatus();
        return dto;
    }
}