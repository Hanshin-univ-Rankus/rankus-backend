package org.univ.rankus.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.univ.rankus.domain.model.notice.LabNotice;
import org.univ.rankus.domain.model.notice.NoticeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class LabNoticeResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final boolean pinned;
    private final NoticeType type;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 작성자 정보
    private final Long authorId;
    private final String authorName;

    // 랩실 정보
    private final Long labId;
    private final String labName;

    public static LabNoticeResponseDto from(LabNotice notice) {
        return LabNoticeResponseDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .pinned(notice.isPinned())
                .type(notice.getType())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .authorId(notice.getAuthor() != null ? notice.getAuthor().getId() : null)
                .authorName(notice.getAuthor() != null ? notice.getAuthor().getName() : null)
                .labId(notice.getLab() != null ? notice.getLab().getId() : null)
                .labName(notice.getLab() != null ? notice.getLab().getName() : null)
                .build();
    }

    public static List<LabNoticeResponseDto> fromList(List<LabNotice> notices) {
        return notices.stream()
                .map(LabNoticeResponseDto::from)
                .collect(Collectors.toList());
    }
}