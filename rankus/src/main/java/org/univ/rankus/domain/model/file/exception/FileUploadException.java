package org.univ.rankus.domain.model.file.exception;

import org.univ.rankus.common.exception.BaseCustomException;
import org.univ.rankus.common.exception.ErrorCode;

/**
 * 파일 업로드 관련 예외 클래스
 * <p>
 * 파일 업로드 도메인에서 발생하는 비즈니스 예외를 나타냅니다.
 * 기존 RankingValidationException과 동일한 패턴을 따릅니다.
 */
public class FileUploadException extends BaseCustomException {

    /**
     * 기본 생성자
     *
     * @param errorCode 에러 코드
     */
    public FileUploadException(ErrorCode errorCode) {
        super(errorCode);
    }
}