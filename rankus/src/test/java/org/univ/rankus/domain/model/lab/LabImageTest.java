package org.univ.rankus.domain.model.lab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabImage 도메인 단위 테스트")
class LabImageTest {

    @Test
    @DisplayName("Lab과 URL, 타입을 올바르게 주면 LabImage가 생성된다")
    void createLabImage_success() {
        // given
        Lab lab = new Lab("AI Lab", "설명", "컴퓨터공학과", LabCategory.AI);
        String url = "http://example.com/img.png";
        ImageType type = ImageType.REPRESENTATIVE;

        // when
        LabImage img = new LabImage(lab, url, type);

        // then
        assertAll("필드 검증",
                () -> assertEquals(lab, img.getLab(),        "Lab이 설정되어야 한다"),
                () -> assertEquals(url, img.getImageUrl(),   "imageUrl이 설정되어야 한다"),
                () -> assertEquals(type, img.getType(),      "ImageType이 설정되어야 한다")
        );
    }

    @Test
    @DisplayName("Lab이 null이면 생성 시 NullPointerException이 발생한다")
    void createLabImage_labNull_throws() {
        // when & then
        assertThrows(NullPointerException.class, () ->
                        new LabImage(null, "url", ImageType.ADDITIONAL),
                "Lab이 null이면 예외가 발생해야 한다"
        );
    }

    @Test
    @DisplayName("imageUrl이 null이면 생성 시 NullPointerException이 발생한다")
    void createLabImage_urlNull_throws() {
        // given
        Lab lab = new Lab("AI Lab", "설명", "컴퓨터공학과", LabCategory.AI);

        // when & then
        assertThrows(NullPointerException.class, () ->
                        new LabImage(lab, null, ImageType.ADDITIONAL),
                "imageUrl이 null이면 예외가 발생해야 한다"
        );
    }

    @Test
    @DisplayName("ImageType이 null이면 생성 시 NullPointerException이 발생한다")
    void createLabImage_typeNull_throws() {
        // given
        Lab lab = new Lab("AI Lab", "설명", "컴퓨터공학과", LabCategory.AI);

        // when & then
        assertThrows(NullPointerException.class, () ->
                        new LabImage(lab, "url", null),
                "ImageType이 null이면 예외가 발생해야 한다"
        );
    }
}
