package org.univ.rankus.adapter.in.web.dto;

import lombok.Getter;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.lab.ImageType;

import java.time.LocalDateTime;

@Getter
public class LabImageResponseDto {
    private Long id;
    private String imageUrl;
    private ImageType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LabImageResponseDto() { }

    public static LabImageResponseDto from(LabImage img) {
        LabImageResponseDto dto = new LabImageResponseDto();
        dto.id        = img.getId();
        dto.imageUrl  = img.getImageUrl();
        dto.type      = img.getType();
        dto.createdAt = img.getCreatedAt();
        dto.updatedAt = img.getUpdatedAt();
        return dto;
    }
}
