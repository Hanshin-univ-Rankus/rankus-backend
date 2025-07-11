package org.univ.rankus.adapter.out.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.univ.rankus.domain.model.file.FileUploadPort;
import org.univ.rankus.domain.model.file.FileValidationPolicy;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

/**
 * 로컬 파일 시스템 기반 파일 업로드 어댑터
 * <p>
 * FileUploadPort 인터페이스를 구현하여 로컬 파일 시스템에 파일을 저장합니다.
 * 기존 SpringSecurityPasswordEncoder와 동일한 어댑터 패턴을 따릅니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileUploadAdapter implements FileUploadPort {

    private final FileValidationPolicy fileValidationPolicy;

    @Value("${file.upload.path:./uploads}")
    private String uploadBasePath;

    @Value("${server.port:8080}")
    private String serverPort;

    // 파일 URL에서 파일명 추출 패턴
    private static final Pattern FILE_URL_PATTERN =
            Pattern.compile(".*/api/files/(.+)$");

    /**
     * 파일을 업로드하고 접근 가능한 URL을 반환합니다.
     */
    @Override
    public String uploadFile(MultipartFile file, Long userId, String category) {
        log.info("파일 업로드 시작 - 사용자: {}, 카테고리: {}, 파일명: {}",
                userId, category, file.getOriginalFilename());

        try {
            // 1. 업로드 디렉토리 생성
            Path uploadDir = createUploadDirectory(category);

            // 2. 안전한 파일명 생성
            String safeFileName = fileValidationPolicy.generateSafeFileName(
                    file.getOriginalFilename(), userId);

            // 3. 파일 저장
            Path filePath = uploadDir.resolve(safeFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. 접근 URL 생성
            String fileUrl = generateFileUrl(safeFileName);

            log.info("파일 업로드 완료 - 사용자: {}, 경로: {}, URL: {}",
                    userId, filePath.toAbsolutePath(), fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("파일 업로드 실패 - 사용자: {}, 파일명: {}, 오류: {}",
                    userId, file.getOriginalFilename(), e.getMessage());
            throw new RankingValidationException(RankingErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일을 삭제합니다.
     */
    @Override
    public boolean deleteFile(String fileUrl, Long userId) {
        log.info("파일 삭제 시작 - 사용자: {}, URL: {}", userId, fileUrl);

        try {
            // 1. URL에서 파일명 추출
            String fileName = extractFileNameFromUrl(fileUrl);
            if (!StringUtils.hasText(fileName)) {
                log.warn("파일명 추출 실패 - URL: {}", fileUrl);
                return false;
            }

            // 2. 파일 권한 확인 (파일명에 userId가 포함되어 있는지 확인)
            if (!isFileOwnedByUser(fileName, userId)) {
                log.warn("파일 삭제 권한 없음 - 사용자: {}, 파일명: {}", userId, fileName);
                throw new SecurityException("파일 삭제 권한이 없습니다.");
            }

            // 3. 파일 삭제
            Path filePath = getFilePathFromName(fileName);
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("파일 삭제 완료 - 사용자: {}, 경로: {}", userId, filePath.toAbsolutePath());
            } else {
                log.warn("파일이 존재하지 않음 - 사용자: {}, 경로: {}", userId, filePath.toAbsolutePath());
            }

            return deleted;

        } catch (IOException e) {
            log.error("파일 삭제 실패 - 사용자: {}, URL: {}, 오류: {}",
                    userId, fileUrl, e.getMessage());
            throw new RankingValidationException(RankingErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일명으로부터 접근 가능한 URL을 생성합니다.
     */
    @Override
    public String generateFileUrl(String fileName) {
        return String.format("http://localhost:%s/api/files/%s", serverPort, fileName);
    }

    /**
     * 파일이 존재하는지 확인합니다.
     */
    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (!StringUtils.hasText(fileName)) {
                return false;
            }

            Path filePath = getFilePathFromName(fileName);
            return Files.exists(filePath);

        } catch (Exception e) {
            log.error("파일 존재 확인 실패 - URL: {}, 오류: {}", fileUrl, e.getMessage());
            return false;
        }
    }

    /**
     * 파일 크기를 반환합니다.
     */
    @Override
    public long getFileSize(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (!StringUtils.hasText(fileName)) {
                return -1;
            }

            Path filePath = getFilePathFromName(fileName);
            if (!Files.exists(filePath)) {
                return -1;
            }

            return Files.size(filePath);

        } catch (Exception e) {
            log.error("파일 크기 조회 실패 - URL: {}, 오류: {}", fileUrl, e.getMessage());
            return -1;
        }
    }

    /**
     * 업로드 디렉토리를 생성합니다.
     */
    private Path createUploadDirectory(String category) throws IOException {
        Path uploadDir = Paths.get(uploadBasePath, category);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            log.info("업로드 디렉토리 생성 - 경로: {}", uploadDir.toAbsolutePath());
        }

        return uploadDir;
    }

    /**
     * URL에서 파일명을 추출합니다.
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }

        var matcher = FILE_URL_PATTERN.matcher(fileUrl);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * 파일이 해당 사용자의 소유인지 확인합니다.
     */
    private boolean isFileOwnedByUser(String fileName, Long userId) {
        if (!StringUtils.hasText(fileName) || userId == null) {
            return false;
        }

        // 파일명 형식: {userId}_{timestamp}_{originalName}.{extension}
        return fileName.startsWith(userId + "_");
    }

    /**
     * 파일명으로부터 실제 파일 경로를 구합니다.
     */
    private Path getFilePathFromName(String fileName) {
        // 파일명에서 카테고리 추정 (proof-file 또는 profile-image)
        String category = fileName.contains("profile") ? "profile-image" : "proof-file";
        return Paths.get(uploadBasePath, category, fileName);
    }
}