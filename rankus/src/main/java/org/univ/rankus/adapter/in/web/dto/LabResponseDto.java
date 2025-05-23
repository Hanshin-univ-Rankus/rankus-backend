package org.univ.rankus.adapter.in.web.dto;

import lombok.Getter;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;

import java.time.LocalDateTime;

@Getter
public class LabResponseDto {
    private Long id;
    private String name;
    private String description;
    private String department;
    private LabCategory category;
    private int ranking;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 도메인 → DTO 변환용 팩토리 메서드
    public static LabResponseDto from(Lab lab) {
        LabResponseDto dto = new LabResponseDto();
        dto.id          = lab.getId();
        dto.name        = lab.getName();
        dto.description = lab.getDescription();
        dto.department  = lab.getDepartment();
        dto.category    = lab.getCategory();
        dto.ranking     = lab.getRanking();
        dto.createdAt   = lab.getCreatedAt();
        dto.updatedAt   = lab.getUpdatedAt();
        return dto;
    }

    // Jackson용 빈 생성자
    private LabResponseDto() { }
}