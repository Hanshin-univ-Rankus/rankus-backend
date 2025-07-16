package org.univ.rankus.adapter.in.web.lab.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 랩장 위임 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "랩장 위임 요청")
public class TransferLeadershipRequest {

    @NotNull(message = "New leader ID is required")
    @Schema(description = "새로운 랩장 ID", example = "2")
    private Long newLeaderId;

    @Schema(description = "위임 사유", example = "졸업으로 인한 랩장 위임")
    private String reason;
}