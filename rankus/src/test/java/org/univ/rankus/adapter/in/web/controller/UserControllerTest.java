package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.UserQueryUseCase;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.config.SecurityConfig;
import org.univ.rankus.adapter.out.security.JwtTokenProvider;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController /api/users 슬라이스 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserQueryUseCase queryUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE_URL = "/api/users/me";
    private final String validToken = "valid.jwt.token";
    private final String invalidToken = "invalid.token";
    private final String email = "user@univ.ac.kr";

    @Nested
    @DisplayName("인증 헤더 없을 때")
    class NoAuthHeader {
        @Test
        @DisplayName("401 Unauthorized 반환")
        void noHeader_unauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("잘못된 Authorization 헤더 포맷")
    class BadHeaderFormat {
        @Test
        @DisplayName("토큰 접두사가 Bearer 아니면 401 반환")
        void wrongPrefix_unauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Basic " + validToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
            verify(jwtTokenProvider, never()).validateToken(anyString());
        }
    }

    @Nested
    @DisplayName("유효하지 않은 토큰")
    class InvalidToken {
        @Test
        @DisplayName("validateToken이 false면 401 Unauthorized")
        void invalidToken_unauthorized() throws Exception {
            given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

            mockMvc.perform(get(BASE_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
            verify(jwtTokenProvider).validateToken(invalidToken);
            verify(queryUseCase, never()).getProfile(anyString());
        }
    }

    @Nested
    @DisplayName("유효한 토큰")
    class ValidToken {
        @Test
        @DisplayName("토큰이 유효하면 200 OK와 사용자 정보 반환")
        void validToken_success() throws Exception {
            // given
            Claims claims = Mockito.mock(Claims.class);
            given(claims.getSubject()).willReturn(email);
            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.parseClaims(validToken)).willReturn(claims);

            // 준비된 User
            Lab lab = new Lab("TestLab", "desc", "CS", LabCategory.AI);
            User user = new User("홍길동", email, "password12345");
            // id 설정
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 42L);

            given(queryUseCase.getProfile(email)).willReturn(user);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(42))
                    .andExpect(jsonPath("$.name").value("홍길동"))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.labId").value(lab.getId()));

            verify(jwtTokenProvider).validateToken(validToken);
            verify(jwtTokenProvider).parseClaims(validToken);
            verify(queryUseCase).getProfile(email);
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 때 404 Not Found")
        void userNotFound_notFound() throws Exception {
            // given
            Claims claims = Mockito.mock(Claims.class);
            given(claims.getSubject()).willReturn(email);
            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.parseClaims(validToken)).willReturn(claims);
            given(queryUseCase.getProfile(email)).willThrow(new NoSuchElementException("not found"));

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(jwtTokenProvider).validateToken(validToken);
            verify(queryUseCase).getProfile(email);
        }
    }
}
