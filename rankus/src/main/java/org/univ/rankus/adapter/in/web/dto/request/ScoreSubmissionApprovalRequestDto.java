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

    // Phase 3: 상태 변경을 위한 필드 추가
    @Size(max = 20, message = "상태값은 20자 이하여야 합니다")
    private String status;
}