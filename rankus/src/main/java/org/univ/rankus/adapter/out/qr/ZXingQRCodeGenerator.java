package org.univ.rankus.adapter.out.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.out.QRCodeGenerationPort;
import org.univ.rankus.domain.model.attendance.QRToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ZXing 라이브러리를 사용한 QR 코드 생성 어댑터
 * <p>
 * QRCodeGenerationPort의 구현체로서, Google ZXing 라이브러리를 사용하여
 * QR 토큰을 실제 QR 코드 이미지로 변환합니다.
 *
 * @since 1.0
 */
@Slf4j
@Component
public class ZXingQRCodeGenerator implements QRCodeGenerationPort {

    private static final String IMAGE_FORMAT = "PNG";
    private static final int MIN_SIZE = 100;
    private static final int MAX_SIZE = 1000;

    /**
     * QR 토큰을 기반으로 QR 코드 이미지를 생성합니다.
     *
     * @param qrToken QR 토큰 객체
     * @param width   QR 코드 이미지 너비 (픽셀)
     * @param height  QR 코드 이미지 높이 (픽셀)
     * @return QR 코드 이미지 바이트 배열 (PNG 형식)
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     * @throws RuntimeException         QR 코드 생성 실패 시
     */
    @Override
    public byte[] generateQRCodeImage(QRToken qrToken, int width, int height) {
        if (qrToken == null || qrToken.getToken() == null) {
            throw new IllegalArgumentException("QR 토큰이 null이거나 토큰 문자열이 없습니다.");
        }

        return generateQRCodeImage(qrToken.getToken(), width, height);
    }

    /**
     * QR 토큰 문자열을 기반으로 QR 코드 이미지를 생성합니다.
     *
     * @param tokenString QR 토큰 문자열
     * @param width       QR 코드 이미지 너비 (픽셀)
     * @param height      QR 코드 이미지 높이 (픽셀)
     * @return QR 코드 이미지 바이트 배열 (PNG 형식)
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     * @throws RuntimeException         QR 코드 생성 실패 시
     */
    @Override
    public byte[] generateQRCodeImage(String tokenString, int width, int height) {
        validateInputs(tokenString, width, height);

        try {
            log.debug("QR 코드 생성 시작 - 토큰: {}, 크기: {}x{}", tokenString, width, height);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = createEncodeHints();

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    tokenString,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );

            byte[] imageBytes = convertToByteArray(bitMatrix);

            log.debug("QR 코드 생성 완료 - 이미지 크기: {} bytes", imageBytes.length);
            return imageBytes;

        } catch (WriterException e) {
            log.error("QR 코드 생성 실패 - 토큰: {}, 오류: {}", tokenString, e.getMessage(), e);
            throw new RuntimeException("QR 코드 생성에 실패했습니다: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("QR 코드 이미지 변환 실패 - 토큰: {}, 오류: {}", tokenString, e.getMessage(), e);
            throw new RuntimeException("QR 코드 이미지 변환에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * QR 코드 인코딩 힌트를 생성합니다.
     *
     * @return 인코딩 힌트 맵
     */
    private Map<EncodeHintType, Object> createEncodeHints() {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // 중간 수준 오류 정정
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // UTF-8 인코딩
        hints.put(EncodeHintType.MARGIN, 1); // 여백 최소화 (프로젝터 투사용)
        return hints;
    }

    /**
     * BitMatrix를 PNG 바이트 배열로 변환합니다.
     *
     * @param bitMatrix QR 코드 비트 매트릭스
     * @return PNG 형식의 바이트 배열
     * @throws IOException 이미지 변환 실패 시
     */
    private byte[] convertToByteArray(BitMatrix bitMatrix) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 입력 매개변수의 유효성을 검사합니다.
     *
     * @param tokenString QR 토큰 문자열
     * @param width       이미지 너비
     * @param height      이미지 높이
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     */
    private void validateInputs(String tokenString, int width, int height) {
        if (tokenString == null || tokenString.trim().isEmpty()) {
            throw new IllegalArgumentException("QR 토큰 문자열이 null이거나 비어있습니다.");
        }

        if (width < MIN_SIZE || width > MAX_SIZE) {
            throw new IllegalArgumentException(
                    String.format("이미지 너비는 %d-%d 픽셀 범위여야 합니다. 입력값: %d", MIN_SIZE, MAX_SIZE, width)
            );
        }

        if (height < MIN_SIZE || height > MAX_SIZE) {
            throw new IllegalArgumentException(
                    String.format("이미지 높이는 %d-%d 픽셀 범위여야 합니다. 입력값: %d", MIN_SIZE, MAX_SIZE, height)
            );
        }
    }
}