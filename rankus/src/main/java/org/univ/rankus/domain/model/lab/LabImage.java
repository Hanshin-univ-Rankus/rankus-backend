package org.univ.rankus.domain.model.lab;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.lab.exception.LabImageErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabImageValidationException;

@Getter
@Entity
@Table(name = "lab_images")
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
    private String imageUrl;

    // 이미지 타입 (필수)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageType type;

    /**
     * 생성자: 필수 필드(lab, imageUrl, type) 검증 후 세팅
     */
    public LabImage(Lab lab, String imageUrl, ImageType type) {
        this.lab = validateLab(lab);
        this.imageUrl = validateImageUrl(imageUrl);
        this.type = validateType(type);
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = validateImageUrl(imageUrl);
    }

    private Lab validateLab(Lab lab) {
        if (lab == null) {
            throw new LabImageValidationException(LabImageErrorCode.IMAGE_NOT_FOUND);
        }
        return lab;
    }

    private String validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new LabImageValidationException(LabImageErrorCode.IMAGE_URL_REQUIRED);
        }
        String trimmed = imageUrl.trim();
        if (trimmed.length() > 255) {
            throw new LabImageValidationException(LabImageErrorCode.IMAGE_URL_TOO_LONG);
        }
        // URL 패턴 검증 (http, https, ftp로 시작)
        if (!trimmed.matches("^(https?|ftp)://[a-zA-Z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=]+$")) {
            throw new LabImageValidationException(LabImageErrorCode.IMAGE_URL_INVALID);
        }
        return trimmed;
    }

    private ImageType validateType(ImageType type) {
        if (type == null) {
            throw new LabImageValidationException(LabImageErrorCode.INVALID_IMAGE_TYPE);
        }
        return type;
    }

}
