package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.lab.core.Lab;

import java.time.LocalDate;
import java.util.List;

/**
 * 랩실 대시보드 응답 DTO
 * 랩실 기본 정보와 각종 위젯(공지사항, 투표, 자료, 일정, 멤버)의 미리보기 데이터를 포함
 */
@Getter
@Builder
@Schema(description = "랩실 대시보드 응답")
public class LabDashboardResponseDto {

    @Schema(description = "랩실 기본 정보")
    private final LabBasicInfo labInfo;

    @Schema(description = "최근 공지사항 (최대 3개)")
    private final List<DashboardPreviewItem> notices;

    @Schema(description = "현재 투표 (최대 3개)")
    private final List<DashboardPreviewItem> votes;

    @Schema(description = "최근 자료 (최대 3개)")
    private final List<DashboardPreviewItem> resources;

    @Schema(description = "일정 (최대 3개)")
    private final List<DashboardPreviewItem> schedules;

    @Schema(description = "랩실 멤버 (최대 3개)")
    private final List<DashboardPreviewItem> members;

    /**
     * 랩실 기본 정보 DTO
     */
    @Getter
    @Builder
    @Schema(description = "랩실 기본 정보")
    public static class LabBasicInfo {
        @Schema(description = "랩실 ID", example = "1")
        private final Long id;

        @Schema(description = "랩실 이름", example = "AI 연구실")
        private final String name;

        @Schema(description = "담당 교수", example = "김교수")
        private final String professorName;

        @Schema(description = "랩실 설명", example = "인공지능과 머신러닝을 연구하는 랩실입니다")
        private final String description;

        @Schema(description = "랩실 개설일", example = "2024-01-01")
        private final LocalDate establishedDate;

        @Schema(description = "대표 사진")
        private final String representativeImage;

        public static LabBasicInfo from(Lab lab) {
            return LabBasicInfo.builder()
                    .id(lab.getId())
                    .name(lab.getName())
                    .professorName(lab.getProfessorName())
                    .description(lab.getDescription() != null ? lab.getDescription() : "")
                    .establishedDate(lab.getCreatedAt().toLocalDate())
                    .representativeImage(null) // TODO: 랩실 이미지 시스템 구현 시 추가
                    .build();
        }
    }

    /**
     * 대시보드 미리보기 아이템 DTO
     * 각 위젯의 미리보기 데이터를 표현
     */
    @Getter
    @Builder
    @Schema(description = "대시보드 미리보기 아이템")
    public static class DashboardPreviewItem {
        @Schema(description = "항목 ID", example = "1")
        private final Long id;

        @Schema(description = "제목", example = "공지사항 제목")
        private final String title;

        @Schema(description = "작성일", example = "2024-07-25T10:30:00")
        private final String createdAt;

        @Schema(description = "타입 (NOTICE, VOTE, RESOURCE, SCHEDULE, MEMBER)", example = "NOTICE")
        private final String type;

        @Schema(description = "추가 정보 (선택적)", example = "진행중")
        private final String additionalInfo;

        public static DashboardPreviewItem createNotice(Long id, String title, String createdAt) {
            return DashboardPreviewItem.builder()
                    .id(id)
                    .title(title)
                    .createdAt(createdAt)
                    .type("NOTICE")
                    .build();
        }

        public static DashboardPreviewItem createVote(Long id, String title, String createdAt, String status) {
            return DashboardPreviewItem.builder()
                    .id(id)
                    .title(title)
                    .createdAt(createdAt)
                    .type("VOTE")
                    .additionalInfo(status)
                    .build();
        }

        public static DashboardPreviewItem createResource(Long id, String title, String createdAt) {
            return DashboardPreviewItem.builder()
                    .id(id)
                    .title(title)
                    .createdAt(createdAt)
                    .type("RESOURCE")
                    .build();
        }

        public static DashboardPreviewItem createSchedule(Long id, String title, String createdAt) {
            return DashboardPreviewItem.builder()
                    .id(id)
                    .title(title)
                    .createdAt(createdAt)
                    .type("SCHEDULE")
                    .build();
        }

        public static DashboardPreviewItem createMember(Long id, String name, String createdAt, String role) {
            return DashboardPreviewItem.builder()
                    .id(id)
                    .title(name)
                    .createdAt(createdAt)
                    .type("MEMBER")
                    .additionalInfo(role)
                    .build();
        }
    }

    /**
     * 전체 대시보드 데이터 생성
     */
    public static LabDashboardResponseDto create(
            Lab lab,
            List<DashboardPreviewItem> notices,
            List<DashboardPreviewItem> votes,
            List<DashboardPreviewItem> resources,
            List<DashboardPreviewItem> schedules,
            List<DashboardPreviewItem> members
    ) {
        return LabDashboardResponseDto.builder()
                .labInfo(LabBasicInfo.from(lab))
                .notices(notices)
                .votes(votes)
                .resources(resources)
                .schedules(schedules)
                .members(members)
                .build();
    }
}