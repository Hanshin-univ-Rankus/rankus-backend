package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.dto.request.UserLoginRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.UserRegisterRequestDto;
import org.univ.rankus.adapter.in.web.dto.response.AuthResponseDto;
import org.univ.rankus.adapter.in.web.dto.response.UserResponseDto;
import org.univ.rankus.application.port.in.command.AuthUseCase;
import org.univ.rankus.domain.model.user.Role;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserValidationException;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;
import org.univ.rankus.testutil.factory.dto.DtoFactory;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthUseCase authUseCase;

    @Nested
    @DisplayName("POST /api/auth/signup")
    class SignupTests {

        @Test
        @DisplayName("정상 요청 → 201 Created + Location, Cache-Control 헤더 + ApiResponse body")
        void signupSuccess() throws Exception {
            // given
            UserRegisterRequestDto request = DtoFactory.buildUserRegisterRequest("홍길동", "new@example.com", "password123");
            String json = objectMapper.writeValueAsString(request);

            User savedUser = DomainUserFactory.buildValidUserWithId(123L);
            given(authUseCase.signUp(any(UserRegisterRequestDto.class)))
                    .willReturn(savedUser);

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "/api/users/123"))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("회원가입 성공"))
                    .andExpect(jsonPath("$.data.id").value(123))
                    .andExpect(jsonPath("$.data.name").value(savedUser.getName()))
                    .andExpect(jsonPath("$.data.email").value(savedUser.getEmail()));
        }

        @Test
        @DisplayName("이메일 중복 시 UserValidationException → 409 Conflict + ErrorResponse body")
        void signupDuplicateEmail() throws Exception {
            // given
            UserRegisterRequestDto request = DtoFactory.buildUserRegisterRequest("홍길동", "exist@example.com", "password123");
            String json = objectMapper.writeValueAsString(request);

            given(authUseCase.signUp(any(UserRegisterRequestDto.class)))
                    .willThrow(new UserValidationException(UserErrorCode.EMAIL_DUPLICATED));

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value(UserErrorCode.EMAIL_DUPLICATED.getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.EMAIL_DUPLICATED.getMessage()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("DTO 검증 실패 (빈 필드) → 400 Bad Request")
        void signupValidationError() throws Exception {
            // name이 빈 문자열, email 형식 불일치, password 너무 짧음, role이 null
            Map<String, Object> req = Map.of(
                    "name", "",
                    "email", "bad-email",
                    "password", "123"
                    // 새로 추가된 필드들(studentNumber, phoneNumber, grade, enrollmentStatus)을 빠뜨려서 null로 만듦
            );
            String json = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    // 검증 오류 메시지가 JSON 배열로 반환됨
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors.length()").value(7)); // name, email, password, studentNumber, phoneNumber, grade, enrollmentStatus 총 7개 에러
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("정상 요청 → 200 OK + ApiResponse body")
        void loginSuccess() throws Exception {
            // given
            UserLoginRequestDto request = DtoFactory.buildUserLoginRequest("user@example.com", "password");
            String json = objectMapper.writeValueAsString(request);

            UserResponseDto userDto = DtoFactory.buildUserResponseDto(10L, "테스터", "user@example.com", Role.STUDENT);
            AuthResponseDto authDto = DtoFactory.buildAuthResponseDto("jwt-token", userDto);

            given(authUseCase.login(any(UserLoginRequestDto.class)))
                    .willReturn(authDto);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("로그인 성공"))
                    .andExpect(jsonPath("$.data.token").value("jwt-token"))
                    .andExpect(jsonPath("$.data.user.id").value(10))
                    .andExpect(jsonPath("$.data.user.name").value("테스터"))
                    .andExpect(jsonPath("$.data.user.email").value("user@example.com"));
        }

        @Test
        @DisplayName("인증 실패 → 401 Unauthorized + ErrorResponse body")
        void loginFailure() throws Exception {
            // given
            UserLoginRequestDto request = DtoFactory.buildUserLoginRequest("user@example.com", "wrong");
            String json = objectMapper.writeValueAsString(request);

            given(authUseCase.login(any(UserLoginRequestDto.class)))
                    .willThrow(new UserValidationException(UserErrorCode.INVALID_CREDENTIALS));

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value(UserErrorCode.INVALID_CREDENTIALS.getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.INVALID_CREDENTIALS.getMessage()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("DTO 검증 실패 (잘못된 이메일 형식) → 400 Bad Request")
        void loginValidationError() throws Exception {
            Map<String, String> req = Map.of(
                    "email", "not-an-email",
                    "password", "password"
            );
            String json = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].field").value("email"));
        }
    }
}