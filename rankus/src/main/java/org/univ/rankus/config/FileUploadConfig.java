package org.univ.rankus.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * 파일 업로드 설정 클래스
 * <p>
 * 파일 업로드 관련 설정을 관리합니다.
 * 기존 SecurityConfig와 동일한 패턴을 따릅니다.
 */
@Configuration
public class FileUploadConfig {

    /**
     * Multipart 파일 업로드 리졸버 설정
     * <p>
     * Spring Boot에서 기본적으로 제공하는 StandardServletMultipartResolver를 사용합니다.
     * 파일 크기 제한 등은 application.yml에서 설정합니다.
     */
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        return resolver;
    }

    /**
     * 파일 업로드 관련 프로퍼티 설정
     * <p>
     * application.yml의 file.upload 하위 속성들을 매핑합니다.
     */
    @ConfigurationProperties(prefix = "file.upload")
    @Configuration
    @Getter
    @Setter
    public static class FileUploadProperties {

        /**
         * 파일 업로드 기본 경로
         */
        private String path = "./uploads";

        /**
         * 허용되는 최대 파일 크기 (bytes)
         */
        private long maxFileSize = 10 * 1024 * 1024; // 10MB

        /**
         * 허용되는 최대 요청 크기 (bytes)
         */
        private long maxRequestSize = 10 * 1024 * 1024; // 10MB

        /**
         * 허용되는 파일 확장자들
         */
        private String[] allowedExtensions = {"pdf", "jpg", "jpeg", "png"};

        /**
         * 허용되는 MIME 타입들
         */
        private String[] allowedContentTypes = {
                "application/pdf",
                "image/jpeg",
                "image/jpg",
                "image/png"
        };

        /**
         * 파일 업로드 시 임시 디렉토리 경로
         */
        private String tempDir = "./temp";

        /**
         * 파일 업로드 임계값 (이 크기를 초과하면 임시 파일로 저장)
         */
        private int fileSizeThreshold = 1024 * 1024; // 1MB

        /**
         * 파일 URL 접두사
         */
        private String urlPrefix = "/api/files";

        /**
         * 파일 업로드 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 파일 업로드 시 디렉토리 자동 생성 여부
         */
        private boolean autoCreateDirectories = true;

        /**
         * 파일 덮어쓰기 허용 여부
         */
        private boolean allowOverwrite = true;

        /**
         * 파일 업로드 로그 활성화 여부
         */
        private boolean logEnabled = true;

        /**
         * 증빙서류 저장 디렉토리명
         */
        private String proofFileDir = "proof-files";

        /**
         * 프로필 이미지 저장 디렉토리명
         */
        private String profileImageDir = "profile-images";

        /**
         * 파일 업로드 완료 후 임시 파일 삭제 여부
         */
        private boolean cleanupTempFiles = true;

        /**
         * 파일 업로드 완료 후 임시 파일 삭제 지연 시간 (초)
         */
        private int cleanupDelaySeconds = 300; // 5분

        /**
         * 파일명 중복 처리 방식 (OVERWRITE, RENAME, ERROR)
         */
        private String duplicateFileNameStrategy = "RENAME";

        /**
         * 파일 업로드 시 바이러스 검사 활성화 여부
         */
        private boolean virusScanEnabled = false;

        /**
         * 파일 업로드 시 썸네일 생성 여부 (이미지 파일만)
         */
        private boolean generateThumbnail = false;

        /**
         * 썸네일 크기 (픽셀)
         */
        private int thumbnailSize = 200;

        /**
         * 파일 업로드 시 이미지 압축 여부
         */
        private boolean compressImages = false;

        /**
         * 이미지 압축 품질 (0.0 ~ 1.0)
         */
        private float imageCompressionQuality = 0.8f;

        /**
         * 파일 업로드 시 메타데이터 추출 여부
         */
        private boolean extractMetadata = false;

        /**
         * 파일 업로드 시 해시 계산 여부 (중복 파일 검사용)
         */
        private boolean calculateFileHash = false;

        /**
         * 파일 업로드 시 사용할 해시 알고리즘
         */
        private String hashAlgorithm = "SHA-256";
    }
}