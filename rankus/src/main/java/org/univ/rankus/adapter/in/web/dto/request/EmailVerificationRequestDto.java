package org.univ.rankus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 인증번호 발송 요청 DTO
 */
@Schema(description = "이메일 인증번호 발송 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequestDto {

    @Schema(
            description = "@hs.ac.kr 도메인 이메일 주소",
            example = "student123@hs.ac.kr",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@hs\\.ac\\.kr$",
            message = "@hs.ac.kr 도메인 이메일만 허용됩니다.")
    private String email;
}