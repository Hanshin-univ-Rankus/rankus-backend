package org.univ.rankus.domain.model.lab;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.lab.exception.LabImageErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabImageValidationException;

@Getter
@Entity
@Table(name = "lab_images",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lab_id", "type"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 Lab에 속하는 이미지인지 (필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lab_id")
    private Lab lab;

    // 이미지 URL (필수, 최대 255자)
    @Column(name = "image_url", nullable = false, length = 255)
    @Pattern(regexp = "^(https?|ftp)://[a-zA-Z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=]+$",
             message = "Invalid image URL format")
    private String imageUrl;

    // 이미지 타입 (필수)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageType type;

    /**
     * 생성자: 필수 필드(lab, imageUrl, type) 검증 후 세팅
     */
    public LabImage(Lab lab, String imageUrl, ImageType type) {
        if (lab == null) {
            throw new LabImageValidationException(LabImageErrorCode.IMAGE_NOT_FOUND);
        }
        this.lab = lab;
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new LabImageValidationException(LabImageErrorCode.IMAGE_URL_REQUIRED);
        }
        if (imageUrl.length() > 255) {
            throw new LabImageValidationException(LabImageErrorCode.IMAGE_URL_INVALID);
        }
        this.imageUrl = validateImageUrl(imageUrl);
        if (type == null) {
            throw new LabImageValidationException(LabImageErrorCode.INVALID_IMAGE_TYPE);
        }
        this.type = type;
    }

    private String validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new LabImageValidationException(LabImageErrorCode.IMAGE_URL_REQUIRED);
        }
        // (Optional) URL 형식 검증 로직이 필요하면 추가
        String trimmed = imageUrl.trim();
        if (trimmed.length() > 255) {
            throw new LabImageValidationException(LabImageErrorCode.INVALID_IMAGE_TYPE);
            // 또는 별도 에러코드 추가 가능
        }
        return trimmed;
    }

}