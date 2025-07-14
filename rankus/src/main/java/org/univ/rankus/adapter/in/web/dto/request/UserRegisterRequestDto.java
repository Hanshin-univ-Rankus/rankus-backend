package org.univ.rankus.adapter.in.web.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.user.EnrollmentStatus;

/**
 * 회원 가입 요청을 받을 때 사용하는 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 30, message = "이름은 30자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상 255자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "학번은 필수입니다.")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "학번은 8-20자리 숫자여야 합니다.")
    private String studentNumber;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^[0-9-]{10,15}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;

    @NotNull(message = "학년은 필수입니다.")
    @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    @Max(value = 8, message = "학년은 8 이하여야 합니다.")
    private Integer grade;

    @NotNull(message = "재학상태는 필수입니다.")
    private EnrollmentStatus enrollmentStatus;
}