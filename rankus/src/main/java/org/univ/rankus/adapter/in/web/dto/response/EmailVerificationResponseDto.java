package org.univ.rankus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.user.EmailVerification;

import java.time.LocalDateTime;

/**
 * 이메일 인증 응답 DTO
 */
@Schema(description = "이메일 인증 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationResponseDto {

    @Schema(description = "인증 ID", example = "1")
    private Long id;

    @Schema(description = "이메일 주소", example = "student123@hs.ac.kr")
    private String email;

    @Schema(description = "인증 완료 여부", example = "true")
    private boolean verified;

    @Schema(description = "만료 시간", example = "2024-07-25T10:30:00")
    private LocalDateTime expiryTime;

    @Schema(description = "발송 횟수", example = "1")
    private int sendCount;

    @Schema(description = "생성 시간", example = "2024-07-25T10:25:00")
    private LocalDateTime createdAt;

    /**
     * EmailVerification 엔티티를 DTO로 변환
     */
    public static EmailVerificationResponseDto from(EmailVerification emailVerification) {
        return EmailVerificationResponseDto.builder()
                .id(emailVerification.getId())
                .email(emailVerification.getEmail())
                .verified(emailVerification.isVerified())
                .expiryTime(emailVerification.getExpiryTime())
                .sendCount(emailVerification.getSendCount())
                .createdAt(emailVerification.getCreatedAt())
                .build();
    }
}