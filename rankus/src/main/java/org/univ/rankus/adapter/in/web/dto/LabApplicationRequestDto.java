package org.univ.rankus.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class LabApplicationRequestDto {

    @NotNull(message = "userId는 필수입니다.")
    private Long userId;

    @NotBlank(message = "userName은 필수입니다.")
    private String userName;

    @NotNull(message = "interviewTime은 필수입니다.")
    // 초 정보 없이도 파싱 가능하도록 패턴을 "yyyy-MM-dd'T'HH:mm" 으로 변경
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime interviewTime;
}
