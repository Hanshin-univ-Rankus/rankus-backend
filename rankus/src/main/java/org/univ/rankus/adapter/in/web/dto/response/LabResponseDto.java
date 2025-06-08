package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.Lab;

@Getter @Builder
public class LabResponseDto {
    private final Long    id;
    private final String  name;
    private final String  description;
    private final Integer ranking;

    public static LabResponseDto from(Long id, String name, String description, Integer ranking) {
        return LabResponseDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .ranking(ranking)
                .build();
    }

    public static LabResponseDto from(Lab lab) {
        return LabResponseDto.builder()
                .id(lab.getId())
                .name(lab.getName())
                .description(lab.getDescription())
                .ranking(lab.getRanking())
                .build();
    }
}