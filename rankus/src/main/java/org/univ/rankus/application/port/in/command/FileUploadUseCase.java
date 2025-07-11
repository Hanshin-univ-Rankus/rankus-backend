package org.univ.rankus.application.port.in.command;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 유스케이스 인터페이스
 * <p>
 * 파일 업로드 관련 비즈니스 로직의 진입점을 정의합니다.
 * 기존 ScoreSubmissionCommandUseCase와 동일한 패턴을 따릅니다.
 */
public interface FileUploadUseCase {

    /**
     * 증빙서류 파일을 업로드합니다.
     *
     * @param file   업로드할 파일
     * @param userId 업로드하는 사용자 ID
     * @return 업로드된 파일의 접근 URL
     * @throws IllegalArgumentException 파일이 유효하지 않을 때
     * @throws RuntimeException         파일 업로드 실패 시
     */
    String uploadProofFile(MultipartFile file, Long userId);

    /**
     * 프로필 이미지 파일을 업로드합니다.
     *
     * @param file   업로드할 파일
     * @param userId 업로드하는 사용자 ID
     * @return 업로드된 파일의 접근 URL
     * @throws IllegalArgumentException 파일이 유효하지 않을 때
     * @throws RuntimeException         파일 업로드 실패 시
     */
    String uploadProfileImage(MultipartFile file, Long userId);

    /**
     * 파일을 삭제합니다.
     *
     * @param fileUrl 삭제할 파일의 URL
     * @param userId  삭제를 요청한 사용자 ID
     * @return 삭제 성공 여부
     * @throws IllegalArgumentException 파일 URL이 유효하지 않을 때
     * @throws SecurityException        사용자가 파일 삭제 권한이 없을 때
     */
    boolean deleteFile(String fileUrl, Long userId);

    /**
     * 파일의 존재 여부를 확인합니다.
     *
     * @param fileUrl 확인할 파일의 URL
     * @return 파일 존재 여부
     */
    boolean isFileExists(String fileUrl);

    /**
     * 파일의 크기를 조회합니다.
     *
     * @param fileUrl 조회할 파일의 URL
     * @return 파일 크기 (bytes), 파일이 없으면 -1
     */
    long getFileSize(String fileUrl);
}