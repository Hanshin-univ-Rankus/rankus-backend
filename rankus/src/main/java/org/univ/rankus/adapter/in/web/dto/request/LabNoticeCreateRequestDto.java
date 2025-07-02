package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.notice.NoticeType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabNoticeCreateRequestDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자 이하여야 합니다")
    private String content;

    @NotNull(message = "공지사항 타입은 필수입니다")
    private NoticeType type;

    @Builder.Default
    private boolean pinned = false;

    public boolean isPinned() {
        return pinned;
    }
}