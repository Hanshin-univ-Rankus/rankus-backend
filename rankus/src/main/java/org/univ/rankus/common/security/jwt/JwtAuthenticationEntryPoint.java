package org.univ.rankus.common.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.univ.rankus.common.exception.ErrorCode;
import org.univ.rankus.common.exception.ErrorResponse;
import org.univ.rankus.common.exception.GlobalErrorCode;

import java.io.IOException;

/**
 * JWT 인증/인가 실패 시 JSON 형태로 응답을 내려주는 EntryPoint
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    public JwtAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        ErrorCode errorCode;
        // JwtAuthenticationException 타입인 경우, 구체적인 에러 코드를 사용
        if (authException instanceof JwtAuthenticationException) {
            errorCode = ((JwtAuthenticationException) authException).getErrorCode();
        } else {
            // 그 외 인증 예외는 일반적인 UNAUTHORIZED 코드를 사용
            errorCode = GlobalErrorCode.UNAUTHORIZED;
        }

        ErrorResponse error = ErrorResponse.of(
                errorCode,
                request.getRequestURI(),
                null
        );

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), error);
    }
}
