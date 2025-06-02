package org.univ.rankus.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    //────────────────────────────────────────────────────────────
    // 1) 공통: ErrorResponse 생성 헬퍼
    //────────────────────────────────────────────────────────────
    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status, String message, String path) {
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return ResponseEntity.status(status).body(body);
    }

    //────────────────────────────────────────────────────────────
    // 2) 스프링 검증/파싱/매핑 예외
    //────────────────────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {

        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse(ex.getMessage());

        return buildError(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest req) {

        String msg = "잘못된 파라미터 형식입니다: " + ex.getName();
        return buildError(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParse(
            HttpMessageNotReadableException ex,
            HttpServletRequest req) {

        String msg = "잘못된 요청 메시지입니다.";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .findFirst()
                    .orElse("필드");
            msg = String.format("%s 값이 유효하지 않습니다: %s", field, ife.getValue());
        }
        return buildError(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    //────────────────────────────────────────────────────────────
    // 3) 자원·경로 관련 예외
    //────────────────────────────────────────────────────────────
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex,
            HttpServletRequest req) {

        String msg = "존재하지 않는 경로입니다: " + ex.getRequestURL();
        return buildError(HttpStatus.NOT_FOUND, msg, req.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
            NoResourceFoundException ex,
            HttpServletRequest req) {

        String msg = "존재하지 않는 경로입니다: " + req.getRequestURI();
        return buildError(HttpStatus.NOT_FOUND, msg, req.getRequestURI());
    }

    //────────────────────────────────────────────────────────────
    // 4) 도메인 비즈니스 예외
    //────────────────────────────────────────────────────────────
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoSuchElementException ex,
            HttpServletRequest req) {

        // 존재하지 않는 리소스에 대한 요청은 404 Not Found로 일관되게 처리
        String msg = ex.getMessage() != null ? ex.getMessage() : "요청한 리소스를 찾을 수 없습니다";
        return buildError(HttpStatus.NOT_FOUND, msg, req.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest req) {

        String msg = ex.getMessage() != null ? ex.getMessage() : "요청을 처리할 수 없습니다";
        return buildError(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    //────────────────────────────────────────────────────────────
    // 5) Spring Web 예외
    //────────────────────────────────────────────────────────────
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            org.springframework.web.server.ResponseStatusException ex,
            HttpServletRequest req) {

        String msg = ex.getReason() != null ? ex.getReason() : "요청을 처리할 수 없습니다";
        return buildError(ex.getStatusCode().is4xxClientError()
                        ? HttpStatus.valueOf(ex.getStatusCode().value())
                        : HttpStatus.INTERNAL_SERVER_ERROR,
                msg, req.getRequestURI());
    }

    //────────────────────────────────────────────────────────────
    // 6) 커스텀·도메인 예외
    //────────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest req) {

        String msg = ex.getMessage() != null ? ex.getMessage() : "잘못된 입력값입니다";
        return buildError(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    //────────────────────────────────────────────────────────────
    // 7) 그 외 예외(500)
    //────────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex,
            HttpServletRequest req) {

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "내부 서버 오류가 발생했습니다.",
                req.getRequestURI()
        );
    }
}

