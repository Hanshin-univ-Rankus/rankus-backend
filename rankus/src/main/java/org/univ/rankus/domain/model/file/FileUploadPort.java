package org.univ.rankus.domain.model.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 도메인 인터페이스
 * <p>
 * 도메인 계층이 외부 파일 저장소에 의존하지 않도록
 * 의존성 역전 원칙을 적용한 도메인 인터페이스입니다.
 * <p>
 * 이 인터페이스는 PasswordEncoder와 동일한 패턴을 따릅니다.
 */
public interface FileUploadPort {

    /**
     * 파일을 업로드하고 접근 가능한 URL을 반환합니다.
     *
     * @param file     업로드할 파일
     * @param userId   업로드하는 사용자 ID
     * @param category 파일 카테고리 (예: "proof-file", "profile-image")
     * @return 업로드된 파일의 접근 URL
     * @throws IllegalArgumentException 파일이 유효하지 않을 때
     * @throws RuntimeException         파일 업로드 실패 시
     */
    String uploadFile(MultipartFile file, Long userId, String category);

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
     * 파일명으로부터 접근 가능한 URL을 생성합니다.
     *
     * @param fileName 파일명
     * @return 파일 접근 URL
     */
    String generateFileUrl(String fileName);

    /**
     * 파일이 존재하는지 확인합니다.
     *
     * @param fileUrl 확인할 파일의 URL
     * @return 파일 존재 여부
     */
    boolean fileExists(String fileUrl);

    /**
     * 파일 크기를 반환합니다.
     *
     * @param fileUrl 확인할 파일의 URL
     * @return 파일 크기 (bytes), 파일이 없으면 -1
     */
    long getFileSize(String fileUrl);
}