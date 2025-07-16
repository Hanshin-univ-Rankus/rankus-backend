package org.univ.rankus.adapter.in.web.lab.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 역할 관리 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "역할 관리 요청")
public class RoleManagementRequest {

    @NotNull(message = "Member ID is required")
    @Schema(description = "멤버 ID", example = "1")
    private Long memberId;

    @Schema(description = "역할 변경 사유", example = "우수한 활동으로 인한 승급")
    private String reason;
}