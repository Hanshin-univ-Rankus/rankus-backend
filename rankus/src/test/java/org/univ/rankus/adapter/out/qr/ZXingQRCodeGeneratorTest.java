package org.univ.rankus.adapter.out.qr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.domain.model.attendance.QRToken;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ZXingQRCodeGenerator 단위 테스트
 * 
 * QR 코드 생성 기능의 정확성과 예외 처리를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ZXingQRCodeGenerator 단위 테스트")
class ZXingQRCodeGeneratorTest {

    private ZXingQRCodeGenerator qrCodeGenerator;

    @BeforeEach
    void setUp() {
        qrCodeGenerator = new ZXingQRCodeGenerator();
    }

    @Nested
    @DisplayName("QR 코드 생성 (QRToken 기반)")
    class GenerateQRCodeImageWithQRToken {

        @Test
        @DisplayName("정상적인 QR 토큰으로 QR 코드 이미지를 생성한다")
        void generateQRCodeImage_ValidQRToken_Success() throws IOException {
            // Given
            QRToken qrToken = QRToken.create(1L, 100L, 5);
            int width = 300;
            int height = 300;

            // When
            byte[] imageBytes = qrCodeGenerator.generateQRCodeImage(qrToken, width, height);

            // Then
            assertThat(imageBytes).isNotNull().isNotEmpty();
            
            // 이미지가 유효한 PNG 형식인지 확인
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(width);
            assertThat(image.getHeight()).isEqualTo(height);
        }

        @Test
        @DisplayName("기본 크기로 QR 코드 이미지를 생성한다")
        void generateQRCodeImage_DefaultSize_Success() throws IOException {
            // Given
            QRToken qrToken = QRToken.create(1L, 100L, 5);

            // When
            byte[] imageBytes = qrCodeGenerator.generateQRCodeImage(qrToken);

            // Then
            assertThat(imageBytes).isNotNull().isNotEmpty();
            
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(300);
            assertThat(image.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("대형 QR 코드 이미지를 생성한다")
        void generateLargeQRCodeImage_Success() throws IOException {
            // Given
            QRToken qrToken = QRToken.create(1L, 100L, 5);

            // When
            byte[] imageBytes = qrCodeGenerator.generateLargeQRCodeImage(qrToken);

            // Then
            assertThat(imageBytes).isNotNull().isNotEmpty();
            
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(600);
            assertThat(image.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("null QR 토큰 전달 시 IllegalArgumentException을 던진다")
        void generateQRCodeImage_NullQRToken_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> qrCodeGenerator.generateQRCodeImage((QRToken) null, 300, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("QR 토큰이 null이거나 토큰 문자열이 없습니다");
        }
    }

    @Nested
    @DisplayName("QR 코드 생성 (문자열 기반)")
    class GenerateQRCodeImageWithString {

        @Test
        @DisplayName("정상적인 토큰 문자열로 QR 코드 이미지를 생성한다")
        void generateQRCodeImage_ValidTokenString_Success() throws IOException {
            // Given
            String tokenString = "1-100-1640995200";
            int width = 250;
            int height = 250;

            // When
            byte[] imageBytes = qrCodeGenerator.generateQRCodeImage(tokenString, width, height);

            // Then
            assertThat(imageBytes).isNotNull().isNotEmpty();
            
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(width);
            assertThat(image.getHeight()).isEqualTo(height);
        }

        @Test
        @DisplayName("최소 크기로 QR 코드 이미지를 생성한다")
        void generateQRCodeImage_MinimumSize_Success() throws IOException {
            // Given
            String tokenString = "1-100-1640995200";
            int size = 100; // 최소 크기

            // When
            byte[] imageBytes = qrCodeGenerator.generateQRCodeImage(tokenString, size, size);

            // Then
            assertThat(imageBytes).isNotNull().isNotEmpty();
            
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(size);
            assertThat(image.getHeight()).isEqualTo(size);
        }

        @Test
        @DisplayName("최대 크기로 QR 코드 이미지를 생성한다")
        void generateQRCodeImage_MaximumSize_Success() throws IOException {
            // Given
            String tokenString = "1-100-1640995200";
            int size = 1000; // 최대 크기

            // When
            byte[] imageBytes = qrCodeGenerator.generateQRCodeImage(tokenString, size, size);

            // Then
            assertThat(imageBytes).isNotNull().isNotEmpty();
            
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(size);
            assertThat(image.getHeight()).isEqualTo(size);
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("null 토큰 문자열 전달 시 IllegalArgumentException을 던진다")
        void generateQRCodeImage_NullTokenString_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> qrCodeGenerator.generateQRCodeImage((String) null, 300, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("QR 토큰 문자열이 null이거나 비어있습니다");
        }

        @Test
        @DisplayName("빈 토큰 문자열 전달 시 IllegalArgumentException을 던진다")
        void generateQRCodeImage_EmptyTokenString_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> qrCodeGenerator.generateQRCodeImage("", 300, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("QR 토큰 문자열이 null이거나 비어있습니다");
        }

        @Test
        @DisplayName("공백만 있는 토큰 문자열 전달 시 IllegalArgumentException을 던진다")
        void generateQRCodeImage_BlankTokenString_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> qrCodeGenerator.generateQRCodeImage("   ", 300, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("QR 토큰 문자열이 null이거나 비어있습니다");
        }

        @Test
        @DisplayName("너무 작은 이미지 크기 전달 시 IllegalArgumentException을 던진다")
        void generateQRCodeImage_TooSmallSize_ThrowsException() {
            // Given
            String tokenString = "1-100-1640995200";

            // When & Then
            assertThatThrownBy(() -> qrCodeGenerator.generateQRCodeImage(tokenString, 50, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미지 너비는 100-1000 픽셀 범위여야 합니다");

            assertThatThrownBy(() -> qrCodeGenerator.generateQRCodeImage(tokenString, 300, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미지 높이는 100-1000 픽셀 범위여야 합니다");
        }

        @Test
        @DisplayName("너무 큰 이미지 크기 전달 시 IllegalArgumentException을 던진다")
        void generateQRCodeImage_TooLargeSize_ThrowsException() {
            // Given
            String tokenString = "1-100-1640995200";

            // When & Then
            assertThatThrownBy(() -> qrCodeGenerator.generateQRCodeImage(tokenString, 1500, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미지 너비는 100-1000 픽셀 범위여야 합니다");

            assertThatThrownBy(() -> qrCodeGenerator.generateQRCodeImage(tokenString, 300, 1500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미지 높이는 100-1000 픽셀 범위여야 합니다");
        }
    }

    @Nested
    @DisplayName("이미지 품질 검증")
    class ImageQualityValidation {

        @Test
        @DisplayName("생성된 QR 코드 이미지가 PNG 형식이다")
        void generateQRCodeImage_ValidFormat_PNG() throws IOException {
            // Given
            String tokenString = "test-token-12345";

            // When
            byte[] imageBytes = qrCodeGenerator.generateQRCodeImage(tokenString, 200, 200);

            // Then
            assertThat(imageBytes).isNotNull().isNotEmpty();
            
            // PNG 매직 넘버 확인 (89 50 4E 47)
            assertThat(imageBytes[0] & 0xFF).isEqualTo(0x89);
            assertThat(imageBytes[1] & 0xFF).isEqualTo(0x50);
            assertThat(imageBytes[2] & 0xFF).isEqualTo(0x4E);
            assertThat(imageBytes[3] & 0xFF).isEqualTo(0x47);
        }

        @Test
        @DisplayName("동일한 토큰으로 생성한 QR 코드는 동일하다")
        void generateQRCodeImage_SameToken_SameImage() {
            // Given
            String tokenString = "same-token-123";

            // When
            byte[] imageBytes1 = qrCodeGenerator.generateQRCodeImage(tokenString, 200, 200);
            byte[] imageBytes2 = qrCodeGenerator.generateQRCodeImage(tokenString, 200, 200);

            // Then
            assertThat(imageBytes1).isEqualTo(imageBytes2);
        }

        @Test
        @DisplayName("다른 토큰으로 생성한 QR 코드는 다르다")
        void generateQRCodeImage_DifferentToken_DifferentImage() {
            // Given
            String tokenString1 = "token-1";
            String tokenString2 = "token-2";

            // When
            byte[] imageBytes1 = qrCodeGenerator.generateQRCodeImage(tokenString1, 200, 200);
            byte[] imageBytes2 = qrCodeGenerator.generateQRCodeImage(tokenString2, 200, 200);

            // Then
            assertThat(imageBytes1).isNotEqualTo(imageBytes2);
        }
    }
}