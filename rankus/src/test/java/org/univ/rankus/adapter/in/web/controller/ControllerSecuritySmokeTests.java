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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security smoke tests for endpoints requiring authentication:
 * - GET /api/users/me
 * - GET /api/labs/{labId}/images
 * - GET /api/labs/{labId}/images/{imageId}
 */
@WebMvcTest({UserController.class, LabImageController.class})
@AutoConfigureMockMvc
@ActiveProfiles("default")  // DevSecurityConfig active: CSRF disabled, but endpoints still require auth
class ControllerSecuritySmokeTests {

    @Autowired
    private MockMvc mockMvc;

    // UserController dependencies
    @MockitoBean
    private org.univ.rankus.application.port.in.command.AuthUseCase authUseCase;

    @MockitoBean
    private org.univ.rankus.application.port.in.query.UserQueryUseCase userQueryUseCase;

    // LabImageController dependencies
    @MockitoBean
    private org.univ.rankus.application.port.in.command.LabImageCommandUseCase labImageCommandUseCase;

    @MockitoBean
    private org.univ.rankus.application.port.in.query.LabImageQueryUseCase labImageQueryUseCase;

    private static final Long LAB_ID = 10L;
    private static final Long IMAGE_ID = 99L;

    @Nested
    @DisplayName("UserController security")
    class UserControllerSecurity {
        @Test
        @DisplayName("GET /api/users/me requires authentication (401)")
        void getMyInfoWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/users/me").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("LabImageController security")
    class LabImageControllerSecurity {
        @Test
        @DisplayName("GET /api/labs/{labId}/images requires authentication (401)")
        void listImagesWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/labs/{labId}/images", LAB_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/labs/{labId}/images/{imageId} requires authentication (401)")
        void getImageWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/labs/{labId}/images/{imageId}", LAB_ID, IMAGE_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
