package org.univ.rankus.domain.model.lab.resource;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourceErrorCode;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourceValidationException;
import org.univ.rankus.domain.model.user.User;

/**
 * 랩실 자료 엔티티
 */
@Getter
@Entity
@Table(name = "lab_resources")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabResource extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceCategory category;

    @Column(nullable = false)
    private Boolean isPublic = true;

    @Column(nullable = false)
    private Integer downloadCount = 0;

    /**
     * 기본 생성자
     */
    public LabResource(String title, String description, String fileName, String fileUrl,
                       Long fileSize, ResourceCategory category, Lab lab, User uploader) {
        this.title = validateTitle(title);
        this.description = validateDescription(description);
        this.fileName = validateFileName(fileName);
        this.fileUrl = validateFileUrl(fileUrl);
        this.fileSize = validateFileSize(fileSize);
        this.category = validateCategory(category);
        this.lab = validateLab(lab);
        this.uploader = validateUploader(uploader);
        this.isPublic = true;
        this.downloadCount = 0;
    }

    /**
     * 공개 여부 포함 생성자
     */
    public LabResource(String title, String description, String fileName, String fileUrl,
                       Long fileSize, ResourceCategory category, Boolean isPublic, Lab lab, User uploader) {
        this.title = validateTitle(title);
        this.description = validateDescription(description);
        this.fileName = validateFileName(fileName);
        this.fileUrl = validateFileUrl(fileUrl);
        this.fileSize = validateFileSize(fileSize);
        this.category = validateCategory(category);
        this.isPublic = isPublic != null ? isPublic : true;
        this.lab = validateLab(lab);
        this.uploader = validateUploader(uploader);
        this.downloadCount = 0;
    }

    /**
     * 제목 유효성 검증
     */
    private String validateTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_TITLE_REQUIRED);
        }
        String trimmed = title.trim();
        if (trimmed.length() > 100) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_TITLE_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 설명 유효성 검증
     */
    private String validateDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > 500) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_DESCRIPTION_TOO_LONG);
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 파일명 유효성 검증
     */
    private String validateFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_FILE_REQUIRED);
        }
        return fileName.trim();
    }

    /**
     * 파일 URL 유효성 검증
     */
    private String validateFileUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_FILE_REQUIRED);
        }
        return fileUrl.trim();
    }

    /**
     * 파일 크기 유효성 검증
     */
    private Long validateFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_FILE_REQUIRED);
        }
        // 50MB 제한
        if (fileSize > 50 * 1024 * 1024) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_FILE_SIZE_EXCEEDED);
        }
        return fileSize;
    }

    /**
     * 카테고리 유효성 검증
     */
    private ResourceCategory validateCategory(ResourceCategory category) {
        if (category == null) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_CATEGORY_REQUIRED);
        }
        return category;
    }

    /**
     * 랩실 유효성 검증
     */
    private Lab validateLab(Lab lab) {
        if (lab == null) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_LAB_REQUIRED);
        }
        return lab;
    }

    /**
     * 업로더 유효성 검증
     */
    private User validateUploader(User uploader) {
        if (uploader == null) {
            throw new LabResourceValidationException(LabResourceErrorCode.RESOURCE_UPLOADER_REQUIRED);
        }
        return uploader;
    }

    /**
     * 제목 업데이트
     */
    public void updateTitle(String newTitle) {
        this.title = validateTitle(newTitle);
    }

    /**
     * 설명 업데이트
     */
    public void updateDescription(String newDescription) {
        this.description = validateDescription(newDescription);
    }

    /**
     * 카테고리 업데이트
     */
    public void updateCategory(ResourceCategory newCategory) {
        this.category = validateCategory(newCategory);
    }

    /**
     * 공개 여부 토글
     */
    public void togglePublic() {
        this.isPublic = !this.isPublic;
    }

    /**
     * 공개 설정
     */
    public void setPublic(Boolean isPublic) {
        this.isPublic = isPublic != null ? isPublic : true;
    }

    /**
     * 다운로드 횟수 증가
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    /**
     * 업로더 확인
     */
    public boolean isUploadedBy(User user) {
        return this.uploader != null && this.uploader.equals(user);
    }

    /**
     * 랩실 소속 확인
     */
    public boolean belongsToLab(Lab lab) {
        return this.lab != null && this.lab.equals(lab);
    }

    /**
     * 공개 자료인지 확인
     */
    public boolean isPublic() {
        return this.isPublic;
    }

    /**
     * 파일 크기를 MB 단위로 반환
     */
    public double getFileSizeInMB() {
        return this.fileSize / (1024.0 * 1024.0);
    }

    /**
     * 파일 확장자 반환
     */
    public String getFileExtension() {
        if (!StringUtils.hasText(this.fileName)) {
            return "";
        }
        int lastDotIndex = this.fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == this.fileName.length() - 1) {
            return "";
        }
        return this.fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}