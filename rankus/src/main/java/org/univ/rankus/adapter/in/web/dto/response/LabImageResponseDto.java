package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.core.ImageType;
import org.univ.rankus.domain.model.lab.core.LabImage;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class LabImageResponseDto {
    private final Long id;
    private final Long labId;
    private final String imageUrl;
    private final ImageType type;

    public static LabImageResponseDto from(Long id, Long labId, String imageUrl, ImageType type) {
        return LabImageResponseDto.builder()
                .id(id)
                .labId(labId)
                .imageUrl(imageUrl)
                .type(type)
                .build();
    }

    public static LabImageResponseDto from(LabImage labImage) {
        return LabImageResponseDto.builder()
                .id(labImage.getId())
                .labId(labImage.getLab().getId())
                .imageUrl(labImage.getImageUrl())
                .type(labImage.getType())
                .build();
    }

    public static List<LabImageResponseDto> fromList(List<LabImage> labImages) {
        return labImages.stream()
                .map(LabImageResponseDto::from)
                .collect(Collectors.toList());
    }
}