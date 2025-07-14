package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.attendance.QRToken;

/**
 * QR 코드 생성을 위한 아웃바운드 포트
 * <p>
 * QR 토큰을 실제 QR 코드 이미지로 변환하는 기능을 제공합니다.
 *
 * @since 1.0
 */
public interface QRCodeGenerationPort {

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
    byte[] generateQRCodeImage(QRToken qrToken, int width, int height);

    /**
     * 기본 크기(300x300)로 QR 코드 이미지를 생성합니다.
     *
     * @param qrToken QR 토큰 객체
     * @return QR 코드 이미지 바이트 배열 (PNG 형식)
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     * @throws RuntimeException         QR 코드 생성 실패 시
     */
    default byte[] generateQRCodeImage(QRToken qrToken) {
        return generateQRCodeImage(qrToken, 300, 300);
    }

    /**
     * 프로젝터 투사용 대형 QR 코드 이미지를 생성합니다.
     *
     * @param qrToken QR 토큰 객체
     * @return QR 코드 이미지 바이트 배열 (PNG 형식, 600x600 크기)
     * @throws IllegalArgumentException 잘못된 매개변수가 전달된 경우
     * @throws RuntimeException         QR 코드 생성 실패 시
     */
    default byte[] generateLargeQRCodeImage(QRToken qrToken) {
        return generateQRCodeImage(qrToken, 600, 600);
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
    byte[] generateQRCodeImage(String tokenString, int width, int height);
}