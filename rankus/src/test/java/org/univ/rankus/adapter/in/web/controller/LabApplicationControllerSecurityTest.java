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
import org.univ.rankus.application.port.in.LabApplicationUseCase;
import org.univ.rankus.config.SecurityConfig;
import org.univ.rankus.adapter.out.security.JwtTokenProvider;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.lab.LabApplication;
import org.univ.rankus.domain.model.lab.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LabApplicationController.class)
@Import(SecurityConfig.class)  // Security 설정 포함
@DisplayName("LabApplicationController 보안 슬라이스 테스트")
class LabApplicationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LabApplicationUseCase applicationUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE = "/api/labs/{labId}/applications";
    private final String validToken = "valid.jwt.token";
    private final String email = "user@univ.ac.kr";

    // 샘플 DTO 바디
    private String sampleBody(Long userId, String userName, LocalDateTime interview) throws Exception {
        return objectMapper.writeValueAsString(
                java.util.Map.of(
                        "userId", userId,
                        "userName", userName,
                        "interviewTime", interview.toString()
                )
        );
    }

    @Nested
    @DisplayName("회원가입(POST) - 인증 필요")
    class RegisterAuth {
        @Test
        @DisplayName("헤더 없으면 401 Unauthorized")
        void register_noAuthHeader() throws Exception {
            mockMvc.perform(post(BASE, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(sampleBody(10L, "홍길동", LocalDateTime.now().plusDays(1))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효한 토큰이면 200 OK and DTO 반환")
        void register_withAuth_success() throws Exception {
            // given
            Long labId = 1L;
            Long userId = 10L;
            String userName = "홍길동";
            LocalDateTime interview = LocalDateTime.of(2025,5,30,14,0);
            Lab dummyLab = new Lab("Dummy", "desc", "CS", LabCategory.AI);
            LabApplication app = new LabApplication(dummyLab, userId, interview);
            // set id via reflection
            var idField = app.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(app, 5L);

            // token validation + claims
            Claims claims = Mockito.mock(Claims.class);
            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.parseClaims(validToken)).willReturn(claims);
            given(claims.getSubject()).willReturn(email);

            given(applicationUseCase.registerApplication(eq(labId), eq(userId), eq(userName), any(LocalDateTime.class)))
                    .willReturn(app);

            // when & then
            mockMvc.perform(post(BASE, labId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(sampleBody(userId, userName, interview)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.status").value(ApplicationStatus.PENDING.name()));
        }
    }

    @Nested
    @DisplayName("목록 조회(GET) - 인증 필요")
    class ListAuth {
        @Test
        @DisplayName("헤더 없으면 401 Unauthorized")
        void list_noAuthHeader() throws Exception {
            mockMvc.perform(get(BASE, 1L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효한 토큰이면 200 OK and List 반환")
        void list_withAuth_success() throws Exception {
            // given
            Long labId = 1L;
            Lab dummyLab = new Lab("Dummy", "desc", "CS", LabCategory.AI);
            LabApplication app = new LabApplication(dummyLab, 10L, LocalDateTime.now().plusDays(1));
            var idField = app.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(app, 5L);

            Claims claims = Mockito.mock(Claims.class);
            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.parseClaims(validToken)).willReturn(claims);
            given(claims.getSubject()).willReturn(email);

            given(applicationUseCase.listApplications(labId)).willReturn(List.of(app));

            // when & then
            mockMvc.perform(get(BASE, labId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(5))
                    .andExpect(jsonPath("$[0].userId").value(10));
        }
    }
}
