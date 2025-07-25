package org.univ.rankus.domain.model.lab.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourceErrorCode;
import org.univ.rankus.domain.model.lab.resource.exception.LabResourceValidationException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LabResource 도메인 모델 테스트
 */
@DisplayName("LabResource 도메인 모델 테스트")
class LabResourceTest {

    @Test
    @DisplayName("유효한 정보로 LabResource를 생성할 수 있다")
    void 유효한_정보로_LabResource를_생성할_수_있다() {
        // given
        Lab lab = createTestLab();
        User uploader = createTestUser();
        String title = "테스트 자료";
        String description = "테스트 설명";
        String fileName = "test.pdf";
        String fileUrl = "http://example.com/test.pdf";
        Long fileSize = 1024L;
        ResourceCategory category = ResourceCategory.LECTURE_NOTE;

        // when
        LabResource labResource = new LabResource(title, description, fileName, fileUrl, fileSize, category, lab, uploader);

        // then
        assertThat(labResource.getTitle()).isEqualTo(title);
        assertThat(labResource.getDescription()).isEqualTo(description);
        assertThat(labResource.getFileName()).isEqualTo(fileName);
        assertThat(labResource.getFileUrl()).isEqualTo(fileUrl);
        assertThat(labResource.getFileSize()).isEqualTo(fileSize);
        assertThat(labResource.getCategory()).isEqualTo(category);
        assertThat(labResource.getLab()).isEqualTo(lab);
        assertThat(labResource.getUploader()).isEqualTo(uploader);
        assertThat(labResource.getIsPublic()).isTrue();
        assertThat(labResource.getDownloadCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("제목이 null이면 예외가 발생한다")
    void 제목이_null이면_예외가_발생한다() {
        // given
        Lab lab = createTestLab();
        User uploader = createTestUser();

        // when & then
        assertThatThrownBy(() -> new LabResource(null, "설명", "test.pdf", "http://example.com/test.pdf", 1024L, ResourceCategory.LECTURE_NOTE, lab, uploader))
                .isInstanceOf(LabResourceValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", LabResourceErrorCode.RESOURCE_TITLE_REQUIRED);
    }

    @Test
    @DisplayName("제목이 100자를 초과하면 예외가 발생한다")
    void 제목이_100자를_초과하면_예외가_발생한다() {
        // given
        Lab lab = createTestLab();
        User uploader = createTestUser();
        String longTitle = "a".repeat(101);

        // when & then
        assertThatThrownBy(() -> new LabResource(longTitle, "설명", "test.pdf", "http://example.com/test.pdf", 1024L, ResourceCategory.LECTURE_NOTE, lab, uploader))
                .isInstanceOf(LabResourceValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", LabResourceErrorCode.RESOURCE_TITLE_TOO_LONG);
    }

    @Test
    @DisplayName("파일 크기가 50MB를 초과하면 예외가 발생한다")
    void 파일_크기가_50MB를_초과하면_예외가_발생한다() {
        // given
        Lab lab = createTestLab();
        User uploader = createTestUser();
        Long oversizeFile = 51 * 1024 * 1024L; // 51MB

        // when & then
        assertThatThrownBy(() -> new LabResource("제목", "설명", "test.pdf", "http://example.com/test.pdf", oversizeFile, ResourceCategory.LECTURE_NOTE, lab, uploader))
                .isInstanceOf(LabResourceValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", LabResourceErrorCode.RESOURCE_FILE_SIZE_EXCEEDED);
    }

    @Test
    @DisplayName("다운로드 횟수를 증가시킬 수 있다")
    void 다운로드_횟수를_증가시킬_수_있다() {
        // given
        LabResource labResource = createTestLabResource();
        int initialCount = labResource.getDownloadCount();

        // when
        labResource.incrementDownloadCount();

        // then
        assertThat(labResource.getDownloadCount()).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("공개 여부를 토글할 수 있다")
    void 공개_여부를_토글할_수_있다() {
        // given
        LabResource labResource = createTestLabResource();
        boolean initialPublicStatus = labResource.getIsPublic();

        // when
        labResource.togglePublic();

        // then
        assertThat(labResource.getIsPublic()).isEqualTo(!initialPublicStatus);
    }

    @Test
    @DisplayName("업로더 확인이 정확하게 동작한다")
    void 업로더_확인이_정확하게_동작한다() {
        // given
        User uploader = createTestUser();
        User otherUser = createTestUser2();
        LabResource labResource = new LabResource("제목", "설명", "test.pdf", "http://example.com/test.pdf", 1024L, ResourceCategory.LECTURE_NOTE, createTestLab(), uploader);

        // when & then
        assertThat(labResource.isUploadedBy(uploader)).isTrue();
        assertThat(labResource.isUploadedBy(otherUser)).isFalse();
    }

    @Test
    @DisplayName("파일 확장자를 올바르게 반환한다")
    void 파일_확장자를_올바르게_반환한다() {
        // given
        LabResource labResource = createTestLabResource();

        // when
        String extension = labResource.getFileExtension();

        // then
        assertThat(extension).isEqualTo("pdf");
    }

    @Test
    @DisplayName("파일 크기를 MB 단위로 올바르게 반환한다")
    void 파일_크기를_MB_단위로_올바르게_반환한다() {
        // given
        Long fileSizeInBytes = 2 * 1024 * 1024L; // 2MB
        LabResource labResource = new LabResource("제목", "설명", "test.pdf", "http://example.com/test.pdf", fileSizeInBytes, ResourceCategory.LECTURE_NOTE, createTestLab(), createTestUser());

        // when
        double fileSizeInMB = labResource.getFileSizeInMB();

        // then
        assertThat(fileSizeInMB).isEqualTo(2.0);
    }

    private LabResource createTestLabResource() {
        return new LabResource("테스트 자료", "테스트 설명", "test.pdf", "http://example.com/test.pdf", 1024L, ResourceCategory.LECTURE_NOTE, createTestLab(), createTestUser());
    }

    private Lab createTestLab() {
        return DomainLabFactory.buildValidLab();
    }

    private User createTestUser() {
        return DomainUserFactory.buildValidUser();
    }

    private User createTestUser2() {
        return DomainUserFactory.buildValidUser();
    }
}