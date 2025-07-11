package org.univ.rankus.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 파일 업로드 응답 DTO
 * <p>
 * 파일 업로드 결과를 클라이언트에게 전달하는 데이터 전송 객체입니다.
 * 기존 ScoreSubmissionResponseDto와 동일한 패턴을 따릅니다.
 */
@Getter
@Builder
@AllArgsConstructor
public class FileUploadResponseDto {

    /**
     * 업로드된 파일의 접근 URL
     */
    private final String fileUrl;

    /**
     * 원본 파일명
     */
    private final String originalFileName;

    /**
     * 저장된 파일명 (시스템에서 생성한 안전한 파일명)
     */
    private final String fileName;

    /**
     * 파일 크기 (bytes)
     */
    private final long fileSize;

    /**
     * 파일 타입 (MIME Type)
     */
    private final String contentType;

    /**
     * 파일 카테고리 (proof-file, profile-image 등)
     */
    private final String category;

    /**
     * 업로드한 사용자 ID
     */
    private final Long userId;

    /**
     * 파일 업로드 성공 여부
     */
    private final boolean success;

    /**
     * 파일 업로드 결과 메시지
     */
    private final String message;

    /**
     * 성공 응답을 위한 팩토리 메서드
     *
     * @param fileUrl          파일 접근 URL
     * @param originalFileName 원본 파일명
     * @param fileName         저장된 파일명
     * @param fileSize         파일 크기
     * @param contentType      파일 타입
     * @param category         파일 카테고리
     * @param userId           업로드한 사용자 ID
     * @return 성공 응답 DTO
     */
    public static FileUploadResponseDto success(
            String fileUrl, String originalFileName, String fileName,
            long fileSize, String contentType, String category, Long userId) {
        return FileUploadResponseDto.builder()
                .fileUrl(fileUrl)
                .originalFileName(originalFileName)
                .fileName(fileName)
                .fileSize(fileSize)
                .contentType(contentType)
                .category(category)
                .userId(userId)
                .success(true)
                .message("파일 업로드가 완료되었습니다.")
                .build();
    }

    /**
     * 실패 응답을 위한 팩토리 메서드
     *
     * @param originalFileName 원본 파일명
     * @param message          실패 메시지
     * @return 실패 응답 DTO
     */
    public static FileUploadResponseDto failure(String originalFileName, String message) {
        return FileUploadResponseDto.builder()
                .fileUrl(null)
                .originalFileName(originalFileName)
                .fileName(null)
                .fileSize(0)
                .contentType(null)
                .category(null)
                .userId(null)
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 파일 삭제 성공 응답을 위한 팩토리 메서드
     *
     * @param fileUrl 삭제된 파일의 URL
     * @param userId  삭제한 사용자 ID
     * @return 삭제 성공 응답 DTO
     */
    public static FileUploadResponseDto deleteSuccess(String fileUrl, Long userId) {
        return FileUploadResponseDto.builder()
                .fileUrl(fileUrl)
                .originalFileName(null)
                .fileName(null)
                .fileSize(0)
                .contentType(null)
                .category(null)
                .userId(userId)
                .success(true)
                .message("파일 삭제가 완료되었습니다.")
                .build();
    }

    /**
     * 파일 정보 조회 응답을 위한 팩토리 메서드
     *
     * @param fileUrl  파일 접근 URL
     * @param fileSize 파일 크기
     * @param exists   파일 존재 여부
     * @return 파일 정보 응답 DTO
     */
    public static FileUploadResponseDto fileInfo(String fileUrl, long fileSize, boolean exists) {
        return FileUploadResponseDto.builder()
                .fileUrl(fileUrl)
                .originalFileName(null)
                .fileName(null)
                .fileSize(fileSize)
                .contentType(null)
                .category(null)
                .userId(null)
                .success(exists)
                .message(exists ? "파일이 존재합니다." : "파일이 존재하지 않습니다.")
                .build();
    }
}