package org.univ.rankus.application.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.univ.rankus.application.port.in.command.FileUploadUseCase;
import org.univ.rankus.domain.model.file.FileUploadPort;
import org.univ.rankus.domain.model.file.FileValidationPolicy;
import org.univ.rankus.domain.model.ranking.exception.RankingErrorCode;
import org.univ.rankus.domain.model.ranking.exception.RankingValidationException;

/**
 * 파일 업로드 서비스 구현체
 * <p>
 * 파일 업로드 관련 비즈니스 로직을 처리합니다.
 * 기존 ScoreSubmissionCommandService와 동일한 패턴을 따릅니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileUploadService implements FileUploadUseCase {

    private final FileUploadPort fileUploadPort;
    private final FileValidationPolicy fileValidationPolicy;

    /**
     * 증빙서류 파일을 업로드합니다.
     */
    @Override
    public String uploadProofFile(MultipartFile file, Long userId) {
        log.info("증빙서류 파일 업로드 시작 - 사용자: {}, 파일명: {}",
                userId, file.getOriginalFilename());

        try {
            // 1. 파일 유효성 검증
            fileValidationPolicy.validateFile(file);

            // 2. 파일 업로드
            String fileUrl = fileUploadPort.uploadFile(file, userId, "proof-file");

            log.info("증빙서류 파일 업로드 완료 - 사용자: {}, URL: {}", userId, fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("증빙서류 파일 업로드 실패 - 사용자: {}, 파일명: {}, 오류: {}",
                    userId, file.getOriginalFilename(), e.getMessage());
            throw new RankingValidationException(RankingErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 프로필 이미지 파일을 업로드합니다.
     */
    @Override
    public String uploadProfileImage(MultipartFile file, Long userId) {
        log.info("프로필 이미지 파일 업로드 시작 - 사용자: {}, 파일명: {}",
                userId, file.getOriginalFilename());

        try {
            // 1. 파일 유효성 검증
            fileValidationPolicy.validateFile(file);

            // 2. 이미지 파일만 허용 (추가 검증)
            validateImageFile(file);

            // 3. 파일 업로드
            String fileUrl = fileUploadPort.uploadFile(file, userId, "profile-image");

            log.info("프로필 이미지 파일 업로드 완료 - 사용자: {}, URL: {}", userId, fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("프로필 이미지 파일 업로드 실패 - 사용자: {}, 파일명: {}, 오류: {}",
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
            // 1. 파일 존재 여부 확인
            if (!fileUploadPort.fileExists(fileUrl)) {
                log.warn("삭제할 파일이 존재하지 않음 - 사용자: {}, URL: {}", userId, fileUrl);
                return false;
            }

            // 2. 파일 삭제
            boolean deleted = fileUploadPort.deleteFile(fileUrl, userId);

            if (deleted) {
                log.info("파일 삭제 완료 - 사용자: {}, URL: {}", userId, fileUrl);
            } else {
                log.warn("파일 삭제 실패 - 사용자: {}, URL: {}", userId, fileUrl);
            }

            return deleted;

        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생 - 사용자: {}, URL: {}, 오류: {}",
                    userId, fileUrl, e.getMessage());
            throw new RankingValidationException(RankingErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일의 존재 여부를 확인합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFileExists(String fileUrl) {
        log.debug("파일 존재 여부 확인 - URL: {}", fileUrl);

        try {
            return fileUploadPort.fileExists(fileUrl);
        } catch (Exception e) {
            log.error("파일 존재 여부 확인 중 오류 발생 - URL: {}, 오류: {}", fileUrl, e.getMessage());
            return false;
        }
    }

    /**
     * 파일의 크기를 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public long getFileSize(String fileUrl) {
        log.debug("파일 크기 조회 - URL: {}", fileUrl);

        try {
            return fileUploadPort.getFileSize(fileUrl);
        } catch (Exception e) {
            log.error("파일 크기 조회 중 오류 발생 - URL: {}, 오류: {}", fileUrl, e.getMessage());
            return -1;
        }
    }

    /**
     * 이미지 파일 전용 검증을 수행합니다.
     */
    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RankingValidationException(RankingErrorCode.UNSUPPORTED_FILE_TYPE);
        }
    }
}