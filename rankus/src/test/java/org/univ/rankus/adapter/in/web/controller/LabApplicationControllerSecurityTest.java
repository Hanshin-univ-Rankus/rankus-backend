package org.univ.rankus.adapter.in.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.command.LabApplicationCommandUseCase;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security Smoke Test: 인증 없는 요청 시 403 Forbidden 반환 확인
 */
@WebMvcTest(LabApplicationController.class)
@AutoConfigureMockMvc
@ActiveProfiles("default")  // DevSecurityConfig 적용
class LabApplicationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LabApplicationCommandUseCase commandUseCase;

    @MockitoBean
    private LabApplicationQueryUseCase queryUseCase;

    private static final Long LAB_ID = 1L;
    private static final Long APP_ID = 100L;

    @Nested
    @DisplayName("POST /api/labs/{labId}/applications - 인증 필요")
    class ApplySecurity {
        @Test
        @DisplayName("인증 없으면 403 Forbidden")
        void applyWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/labs/{labId}/applications", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"interviewTime\": \"2025-06-20T10:00:00\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/applications - 인증 필요")
    class ListSecurity {
        @Test
        @DisplayName("인증 없으면 401 isUnauthorized")
        void listWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/labs/{labId}/applications", LAB_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/applications/{appId} - 인증 필요")
    class GetSecurity {
        @Test
        @DisplayName("인증 없으면 401 isUnauthorized")
        void getWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/labs/{labId}/applications/{appId}", LAB_ID, APP_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/labs/{labId}/applications/{appId}/approve - 인증 필요")
    class ApproveSecurity {
        @Test
        @DisplayName("인증 없으면 403 Forbidden")
        void approveWithoutAuth() throws Exception {
            mockMvc.perform(put("/api/labs/{labId}/applications/{appId}/approve", LAB_ID, APP_ID))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/labs/{labId}/applications/{appId}/reject - 인증 필요")
    class RejectSecurity {
        @Test
        @DisplayName("인증 없으면 403 Forbidden")
        void rejectWithoutAuth() throws Exception {
            mockMvc.perform(put("/api/labs/{labId}/applications/{appId}/reject", LAB_ID, APP_ID))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/labs/{labId}/applications/{appId} - 인증 필요")
    class CancelSecurity {
        @Test
        @DisplayName("인증 없으면 403 Forbidden")
        void cancelWithoutAuth() throws Exception {
            mockMvc.perform(delete("/api/labs/{labId}/applications/{appId}", LAB_ID, APP_ID))
                    .andExpect(status().isForbidden());
        }
    }
}
