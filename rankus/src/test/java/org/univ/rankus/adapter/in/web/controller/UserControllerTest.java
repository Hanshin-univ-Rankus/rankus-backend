package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.query.UserQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserNotFoundException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserQueryUseCase userQueryUseCase;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/users/me")
    class GetMyInfoTests {

        @Test
        @DisplayName("정상 요청 → 200 OK + ApiResponse<UserResponseDto> body")
        void getMyInfoSuccess() throws Exception {
            // given: principal 세팅
            Long userId = 42L;
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(userId);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // and: UserQueryUseCase.getUserById 모킹
            User mockUser = mock(User.class);
            given(userQueryUseCase.getUserById(userId)).willReturn(mockUser);
            given(mockUser.getId()).willReturn(userId);
            given(mockUser.getName()).willReturn("테스트유저");
            given(mockUser.getEmail()).willReturn("test@example.com");

            // when & then
            mockMvc.perform(get("/api/users/me")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("사용자 정보 조회 성공"))
                    .andExpect(jsonPath("$.data.id").value(42))
                    .andExpect(jsonPath("$.data.name").value("테스트유저"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        @DisplayName("사용자 조회 실패 → 404 Not Found + ErrorResponse body")
        void getMyInfoUserNotFound() throws Exception {
            // given: principal 세팅
            Long userId = 99L;
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(userId);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // and: getUserById 예외 모킹
            given(userQueryUseCase.getUserById(userId))
                    .willThrow(new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/users/me")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value(UserErrorCode.USER_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.getMessage()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }
}