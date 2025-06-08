package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.ImageType;

@Getter @Builder
public class LabImageResponseDto {
    private final Long      id;
    private final Long      labId;
    private final String    imageUrl;
    private final ImageType type;

    public static LabImageResponseDto from(Long id, Long labId, String imageUrl, ImageType type) {
        return LabImageResponseDto.builder()
                .id(id)
                .labId(labId)
                .imageUrl(imageUrl)
                .type(type)
                .build();
    }
}