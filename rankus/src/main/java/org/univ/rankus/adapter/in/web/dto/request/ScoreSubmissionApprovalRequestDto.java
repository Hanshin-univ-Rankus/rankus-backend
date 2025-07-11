package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSubmissionApprovalRequestDto {

    @Size(max = 200, message = "거부 사유는 200자 이하여야 합니다")
    private String rejectionReason;
}