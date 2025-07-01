package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.lab.LabCreationRequest;
import org.univ.rankus.domain.model.lab.LabCreationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class LabCreationRequestResponseDto {
    private final Long id;
    private final String requestedLabName;
    private final LabCategory requestedCategory;
    private final String requestedDescription;
    private final UserResponseDto requester;
    private final LabCreationStatus status;
    private final LocalDateTime requestedAt;
    private final LocalDateTime processedAt;
    private final UserResponseDto processedBy;
    private final String rejectionReason;

    public static LabCreationRequestResponseDto from(LabCreationRequest request) {
        return LabCreationRequestResponseDto.builder()
                .id(request.getId())
                .requestedLabName(request.getRequestedLabName())
                .requestedCategory(request.getRequestedCategory())
                .requestedDescription(request.getRequestedDescription())
                .requester(UserResponseDto.from(request.getRequester()))
                .status(request.getStatus())
                .requestedAt(request.getCreatedAt())
                .processedAt(request.getProcessedAt())
                .processedBy(request.getProcessedBy() != null ? UserResponseDto.from(request.getProcessedBy()) : null)
                .rejectionReason(request.getRejectionReason())
                .build();
    }

    public static List<LabCreationRequestResponseDto> fromList(List<LabCreationRequest> requests) {
        return requests.stream()
                .map(LabCreationRequestResponseDto::from)
                .collect(Collectors.toList());
    }
}