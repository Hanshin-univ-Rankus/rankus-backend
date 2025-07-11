package org.univ.rankus.domain.model.file;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 파일 검증 정책 클래스
 * <p>
 * 파일 업로드 시 보안 및 비즈니스 규칙을 검증합니다.
 * 기존 DuplicateCheckPolicy와 동일한 패턴을 따릅니다.
 */
@Component
public class FileValidationPolicy {

    // 파일 크기 제한 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 허용되는 파일 타입
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    // 허용되는 파일 확장자
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png"
    );

    // 안전하지 않은 파일명 패턴
    private static final Pattern UNSAFE_FILENAME_PATTERN =
            Pattern.compile(".*[/\\\\:*?\"<>|].*");

    // 스크립트 파일 확장자 (차단)
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar", "sh"
    );

    /**
     * 파일의 전체적인 유효성을 검증합니다.
     *
     * @param file 검증할 파일
     * @throws RankingValidationException 검증 실패 시
     */
    public void validateFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file);
        validateFileName(file);
        validateFileType(file);
        validateFileContent(file);
    }

    /**
     * 파일이 비어있지 않은지 검증합니다.
     */
    private void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RankingValidationException(RankingErrorCode.PROOF_FILE_URL_REQUIRED);
        }
    }

    /**
     * 파일 크기를 검증합니다.
     */
    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RankingValidationException(RankingErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    /**
     * 파일명의 안전성을 검증합니다.
     */
    private void validateFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();

        if (!StringUtils.hasText(originalFileName)) {
            throw new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        // 경로 탐색 공격 방지
        if (UNSAFE_FILENAME_PATTERN.matcher(originalFileName).matches()) {
            throw new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        // 파일 확장자 검증
        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        // 위험한 확장자 차단
        if (DANGEROUS_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }

    /**
     * 파일 타입(MIME Type)을 검증합니다.
     */
    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();

        if (!StringUtils.hasText(contentType) ||
                !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }

    /**
     * 파일 내용을 검증합니다 (기본적인 악성 파일 검사).
     */
    private void validateFileContent(MultipartFile file) {
        try {
            // 파일 헤더 검증 (매직 넘버 체크)
            byte[] fileHeader = new byte[10];
            int bytesRead = file.getInputStream().read(fileHeader);

            if (bytesRead > 0) {
                validateFileHeader(fileHeader, file.getOriginalFilename());
            }
        } catch (IOException e) {
            throw new RankingValidationException(RankingErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일 헤더를 검증합니다 (매직 넘버 체크).
     */
    private void validateFileHeader(byte[] header, String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();

        switch (extension) {
            case "pdf":
                if (!isPdfHeader(header)) {
                    throw new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE);
                }
                break;
            case "jpg":
            case "jpeg":
                if (!isJpegHeader(header)) {
                    throw new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE);
                }
                break;
            case "png":
                if (!isPngHeader(header)) {
                    throw new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE);
                }
                break;
        }
    }

    /**
     * PDF 파일 헤더를 검증합니다.
     */
    private boolean isPdfHeader(byte[] header) {
        return header.length >= 4 &&
                header[0] == (byte) 0x25 && // %
                header[1] == (byte) 0x50 && // P
                header[2] == (byte) 0x44 && // D
                header[3] == (byte) 0x46;   // F
    }

    /**
     * JPEG 파일 헤더를 검증합니다.
     */
    private boolean isJpegHeader(byte[] header) {
        return header.length >= 2 &&
                header[0] == (byte) 0xFF &&
                header[1] == (byte) 0xD8;
    }

    /**
     * PNG 파일 헤더를 검증합니다.
     */
    private boolean isPngHeader(byte[] header) {
        return header.length >= 8 &&
                header[0] == (byte) 0x89 &&
                header[1] == (byte) 0x50 &&
                header[2] == (byte) 0x4E &&
                header[3] == (byte) 0x47 &&
                header[4] == (byte) 0x0D &&
                header[5] == (byte) 0x0A &&
                header[6] == (byte) 0x1A &&
                header[7] == (byte) 0x0A;
    }

    /**
     * 파일 확장자를 추출합니다.
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 안전한 파일명을 생성합니다.
     *
     * @param originalFileName 원본 파일명
     * @param userId           사용자 ID
     * @return 안전한 파일명
     */
    public String generateSafeFileName(String originalFileName, Long userId) {
        String extension = getFileExtension(originalFileName);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));

        // 특수문자 제거 및 안전한 문자만 유지
        String sanitizedBaseName = baseName.replaceAll("[^a-zA-Z0-9가-힣_-]", "_");

        return String.format("%d_%s_%s.%s",
                userId, timestamp, sanitizedBaseName, extension);
    }

    /**
     * 허용되는 최대 파일 크기를 반환합니다.
     */
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * 허용되는 파일 확장자 목록을 반환합니다.
     */
    public Set<String> getAllowedExtensions() {
        return Set.copyOf(ALLOWED_EXTENSIONS);
    }
}