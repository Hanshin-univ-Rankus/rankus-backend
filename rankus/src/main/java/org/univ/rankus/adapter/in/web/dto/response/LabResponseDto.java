package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.Lab;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class LabResponseDto {
    private final Long id;
    private final String name;
    private final String description;
    private final Integer ranking;
    private final String professorName;
    private final String createdAt;

    public static LabResponseDto from(Long id, String name, String description, Integer ranking, String professorName, String createdAt) {
        return LabResponseDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .ranking(ranking)
                .professorName(professorName)
                .createdAt(createdAt)
                .build();
    }

    public static LabResponseDto from(Lab lab) {
        return LabResponseDto.builder()
                .id(lab.getId())
                .name(lab.getName())
                .description(lab.getDescription())
                .ranking(lab.getRanking())
                .professorName(lab.getProfessorName())
                .createdAt(lab.getCreatedAt().toString()) // Assuming createdAt is a LocalDateTime
                .build();
    }

    public static List<LabResponseDto> fromList(List<Lab> labs) {
        return labs.stream()
                .map(LabResponseDto::from)
                .collect(Collectors.toList());
    }
}