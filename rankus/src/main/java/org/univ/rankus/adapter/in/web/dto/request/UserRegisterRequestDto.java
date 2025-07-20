package org.univ.rankus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.user.EnrollmentStatus;

/**
 * 회원 가입 요청을 받을 때 사용하는 DTO
 */
@Schema(description = "회원가입 요청 정보")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequestDto {

    @Schema(
            description = "사용자 이름",
            example = "홍길동",
            maxLength = 30,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 30, message = "이름은 30자 이하여야 합니다.")
    private String name;

    @Schema(
            description = "이메일 주소 (로그인 시 사용)",
            example = "hong@example.com",
            format = "email",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    private String email;

    @Schema(
            description = "비밀번호 (8자 이상)",
            example = "password123!",
            format = "password",
            minLength = 8,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상 255자 이하여야 합니다.")
    private String password;

    @Schema(
            description = "학번 (8-20자리 숫자)",
            example = "20210001",
            pattern = "^[0-9]{8,20}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "학번은 필수입니다.")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "학번은 8-20자리 숫자여야 합니다.")
    private String studentNumber;

    @Schema(
            description = "전화번호 (하이픈 포함 가능)",
            example = "010-1234-5678",
            pattern = "^[0-9-]{10,15}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^[0-9-]{10,15}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;

    @Schema(
            description = "학년 (1-8)",
            example = "3",
            minimum = "1",
            maximum = "8",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "학년은 필수입니다.")
    @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    @Max(value = 8, message = "학년은 8 이하여야 합니다.")
    private Integer grade;

    @Schema(
            description = "재학 상태",
            example = "ENROLLED",
            allowableValues = {"ENROLLED", "LEAVE_OF_ABSENCE", "GRADUATED", "DROPPED_OUT"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "재학상태는 필수입니다.")
    private EnrollmentStatus enrollmentStatus;
}