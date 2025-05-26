package org.univ.rankus.domain.model.lab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabImage 도메인 단위 테스트")
@ActiveProfiles("test")
class LabImageTest {

    @Nested
    @DisplayName("LabImage 생성 성공 테스트")
    class CreateSuccessTest {

        @Test
        @DisplayName("Lab, URL, 타입을 올바르게 주면 LabImage가 정상적으로 생성된다")
        void 생성_정상() {
            // 준비: 랩, 이미지 URL, 타입을 준비한다.
            Lab lab = new Lab("AI Lab", "설명", "컴퓨터공학과", LabCategory.AI);
            String url = "http://example.com/img.png";
            ImageType type = ImageType.REPRESENTATIVE;

            // 실행: LabImage를 생성한다.
            LabImage img = new LabImage(lab, url, type);

            // 검증: 각 필드가 올바르게 설정되었는지 확인한다.
            assertAll("LabImage 필드 검증",
                    () -> assertEquals(lab, img.getLab(), "Lab이 설정되어야 한다"),
                    () -> assertEquals(url, img.getImageUrl(), "imageUrl이 설정되어야 한다"),
                    () -> assertEquals(type, img.getType(), "ImageType이 설정되어야 한다")
            );
        }
    }

    @Nested
    @DisplayName("LabImage 생성 예외 테스트")
    class CreateExceptionTest {

        @Test
        @DisplayName("Lab이 null이면 NullPointerException이 발생한다")
        void lab_null_예외() {
            // 실행 및 검증: Lab이 null일 때 예외가 발생해야 한다.
            assertThrows(NullPointerException.class,
                    () -> new LabImage(null, "url", ImageType.ADDITIONAL),
                    "Lab이 null이면 예외가 발생해야 한다"
            );
        }

        @Test
        @DisplayName("imageUrl이 null이면 NullPointerException이 발생한다")
        void imageUrl_null_예외() {
            // 준비: 유효한 Lab 객체 생성
            Lab lab = new Lab("AI Lab", "설명", "컴퓨터공학과", LabCategory.AI);

            // 실행 및 검증: imageUrl이 null일 때 예외가 발생해야 한다.
            assertThrows(NullPointerException.class,
                    () -> new LabImage(lab, null, ImageType.ADDITIONAL),
                    "imageUrl이 null이면 예외가 발생해야 한다"
            );
        }

        @Test
        @DisplayName("ImageType이 null이면 NullPointerException이 발생한다")
        void imageType_null_예외() {
            // 준비: 유효한 Lab 객체 생성
            Lab lab = new Lab("AI Lab", "설명", "컴퓨터공학과", LabCategory.AI);

            // 실행 및 검증: ImageType이 null일 때 예외가 발생해야 한다.
            assertThrows(NullPointerException.class,
                    () -> new LabImage(lab, "url", null),
                    "ImageType이 null이면 예외가 발생해야 한다"
            );
        }
    }
}
