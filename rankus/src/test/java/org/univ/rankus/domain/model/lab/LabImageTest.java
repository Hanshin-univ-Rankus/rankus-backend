package org.univ.rankus.domain.model.lab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.lab.exception.LabImageValidationException;
import org.univ.rankus.domain.model.lab.exception.LabImageErrorCode;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;
import org.univ.rankus.testutil.factory.domain.DomainLabImageFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabImage 도메인 단위 테스트")
class LabImageTest {

    private final Lab validLab = DomainLabFactory.buildValidLab();
    private final String rawUrl = " https://example.com/image.png ";

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @Test
        @DisplayName("lab이 null일 때 IMAGE_NOT_FOUND 예외 발생")
        void nullLab_throwsImageNotFound() {
            // when & then
            LabImageValidationException ex = assertThrows(
                    LabImageValidationException.class,
                    () -> DomainLabImageFactory.buildInvalidLabImage_NullLab(rawUrl, ImageType.ADDITIONAL)
            );
            assertEquals(LabImageErrorCode.IMAGE_NOT_FOUND, ex.getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] url=''{0}'' → IMAGE_URL_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("url이 null 또는 blank일 때 IMAGE_URL_REQUIRED 예외 발생")
        void nullOrBlankUrl_throwsUrlRequired(String url) {
            // when & then
            LabImageValidationException ex = assertThrows(
                    LabImageValidationException.class,
                    () -> DomainLabImageFactory.buildInvalidLabImage_NullOrBlankUrl(validLab)
            );
            assertEquals(LabImageErrorCode.IMAGE_URL_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("url 길이가 255 초과일 때 IMAGE_URL_TOO_LONG 예외 발생")
        void longUrl_throwsInvalidImageType() {
            // given
            String base = "http://ex.com/";
            StringBuilder sb = new StringBuilder(base);
            while (sb.length() <= 255) sb.append('a');
            String longUrl = sb.toString();

            // when & then
            LabImageValidationException ex = assertThrows(
                    LabImageValidationException.class,
                    () -> DomainLabImageFactory.buildInvalidLabImage_LongUrl(validLab, base)
            );
            assertEquals(LabImageErrorCode.IMAGE_URL_TOO_LONG, ex.getErrorCode());
        }

        @Test
        @DisplayName("type이 null일 때 INVALID_IMAGE_TYPE 예외 발생")
        void nullType_throwsInvalidImageType() {
            // when & then
            LabImageValidationException ex = assertThrows(
                    LabImageValidationException.class,
                    () -> DomainLabImageFactory.buildInvalidLabImage_NullType(validLab, rawUrl)
            );
            assertEquals(LabImageErrorCode.INVALID_IMAGE_TYPE, ex.getErrorCode());
        }

        @Test
        @DisplayName("유효하지 않은 URL 형식일 때 IMAGE_URL_INVALID 예외 발생")
        void invalidUrl_throwsImageUrlInvalid() {
            // when & then
            LabImageValidationException ex = assertThrows(
                    LabImageValidationException.class,
                    () -> DomainLabImageFactory.buildInvalidLabImage_InvalidUrl(validLab)
            );
            assertEquals(LabImageErrorCode.IMAGE_URL_INVALID, ex.getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] type={0} → 정상 생성")
        @EnumSource(ImageType.class)
        @DisplayName("유효한 파라미터로 Normal LabImage 객체 생성")
        void validParameters_createsInstance(ImageType type) {
            // when
            LabImage img = DomainLabImageFactory.buildValidLabImage(validLab, rawUrl, type);

            // then
            assertNotNull(img, "LabImage 객체는 null이 아니어야 한다");
            assertEquals(validLab, img.getLab(), "lab 필드가 설정되어야 한다");
            assertEquals("https://example.com/image.png", img.getImageUrl(), "imageUrl은 trim되어야 한다");
            assertEquals(type, img.getType(), "type 필드가 설정되어야 한다");
            assertNull(img.getId(), "id는 생성 전 null이어야 한다");
        }

        @Test
        @DisplayName("buildValidLabImageWithId로 id 주입 시 id가 설정됨")
        void buildWithId_setsId() {
            // when
            LabImage img = DomainLabImageFactory.buildValidLabImageWithId(123L, validLab, rawUrl, ImageType.ADDITIONAL);

            // then
            assertEquals(123L, img.getId(), "Reflection을 통해 id가 설정되어야 한다");
        }
    }
}