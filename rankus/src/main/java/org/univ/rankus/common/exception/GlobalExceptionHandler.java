package org.univ.rankus.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기(Global Exception Handler)
 *
 * 1) BaseCustomException (도메인별 Validation/NotFound 예외 포함)
 *    → errorCode.getStatus() 에 따라 400·404·409 등 처리
 *
 * 2) MethodArgumentNotValidException (@Valid 바인딩 오류) → 400 + fieldErrors
 * 3) ConstraintViolationException (@RequestParam·@PathVariable 제약 위반) → 400 + fieldErrors
 * 4) AuthenticationException (인증 오류) → 401 Unauthorized
 * 5) AccessDeniedException (인가 오류) → 403 Forbidden
 * 6) HttpMediaTypeNotSupportedException (지원하지 않는 미디어 타입) → 415 Unsupported Media Type
 * 7) 그 외 Exception → 500 Internal Server Error
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 1) 도메인 예외(BaseCustomException 하위) 처리
     *    예: UserValidationException, UserNotFoundException, LabValidationException, LabNotFoundException 등
     */
    @ExceptionHandler(BaseCustomException.class)
    protected ResponseEntity<ErrorResponse> handleBaseCustomException(
            BaseCustomException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(errorResponse);
    }

    /**
     * 2) DTO 검증(@Valid) 실패 시 발생하는 예외 처리 (400 Bad Request)
     *    BindingResult 에 담긴 모든 필드 오류를 FieldError 리스트로 변환
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> new ErrorResponse.FieldError(
                        err.getField(),
                        err.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
                GlobalErrorCode.INVALID_INPUT,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity
                .status(GlobalErrorCode.INVALID_INPUT.getStatus())
                .body(errorResponse);
    }

    /**
     * 3) @RequestParam, @PathVariable 등에 붙은 제약 조건 위반 시 발생 (400 Bad Request)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorResponse.FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
                GlobalErrorCode.INVALID_INPUT,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity
                .status(GlobalErrorCode.INVALID_INPUT.getStatus())
                .body(errorResponse);
    }

    /**
     * 4) 인증 실패(AuthenticationException) 시 처리 (401 Unauthorized)
     */
    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("인증 실패: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                GlobalErrorCode.UNAUTHORIZED,
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(GlobalErrorCode.UNAUTHORIZED.getStatus())
                .body(errorResponse);
    }

    /**
     * 5) 권한 부족(AccessDeniedException) 시 처리 (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("접근 거부: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                GlobalErrorCode.FORBIDDEN,
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(GlobalErrorCode.FORBIDDEN.getStatus())
                .body(errorResponse);
    }

    /**
     * 6) 지원하지 않는 미디어 타입(HttpMediaTypeNotSupportedException) 시 처리 (415 Unsupported Media Type)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        log.warn("지원하지 않는 미디어 타입: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE,
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus())
                .body(errorResponse);
    }


    /**
     * 7) 그 외 모든 예외 (예측하지 못한 서버 오류) 처리 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleAllException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.of(
                GlobalErrorCode.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                null
        );
        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(errorResponse);
    }
}