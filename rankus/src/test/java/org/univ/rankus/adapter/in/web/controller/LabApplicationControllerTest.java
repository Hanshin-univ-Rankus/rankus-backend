package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.command.LabApplicationCommandUseCase;
import org.univ.rankus.application.port.in.query.LabApplicationQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.ApplicationStatus;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabApplication;
import org.univ.rankus.domain.model.lab.exception.*;
import org.univ.rankus.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "test", roles = {"USER", "ADMIN"})
class LabApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LabApplicationCommandUseCase commandUseCase;

    @MockitoBean
    private LabApplicationQueryUseCase queryUseCase;

    private static final Long LAB_ID = 1L;
    private static final Long APP_ID = 123L;
    private static final Long USER_ID = 42L;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("POST /api/labs/{labId}/applications")
    class ApplyTests {

        @Test
        @DisplayName("정상 신청 → 201 Created + Location, Cache-Control + ApiResponse body")
        void applySuccess() throws Exception {
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDateTime time = LocalDateTime.now().plusDays(1);
            Map<String, Object> req = new HashMap<>();
            req.put("interviewTime", time.toString());
            String json = objectMapper.writeValueAsString(req);

            // 더 완전한 mock 객체 설정
            LabApplication mockApp = mock(LabApplication.class);
            Lab mockLab = mock(Lab.class);
            User mockUser = mock(User.class);

            given(mockApp.getId()).willReturn(APP_ID);
            given(mockApp.getLab()).willReturn(mockLab);
            given(mockLab.getId()).willReturn(LAB_ID);
            given(mockApp.getUser()).willReturn(mockUser);
            given(mockUser.getId()).willReturn(USER_ID);
            given(mockUser.getName()).willReturn("테스트유저");
            given(mockUser.getEmail()).willReturn("test@example.com");
            given(mockApp.getInterviewTime()).willReturn(time);
            given(mockApp.getStatus()).willReturn(ApplicationStatus.PENDING);

            given(commandUseCase.applyToLab(eq(LAB_ID), eq(USER_ID), any(LocalDateTime.class)))
                    .willReturn(mockApp);

            mockMvc.perform(post("/api/labs/{labId}/applications", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION,
                            "/api/labs/" + LAB_ID + "/applications/" + APP_ID))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("가입 신청 성공"))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("DTO 검증 실패 (면접시간 누락) → 400 Bad Request + GLOBAL_001")
        void applyValidationError() throws Exception {
            CustomUserDetails principal = mock(CustomUserDetails.class);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            Map<String, Object> req = new HashMap<>();
            String json = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/labs/{labId}/applications", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("GLOBAL_001"))
                    .andExpect(jsonPath("$.errors[0].field").value("interviewTime"));
        }

        @Test
        @DisplayName("면접 시간 과거 입력 → 400 Bad Request + GLOBAL_001 + 메시지 검증")
        void applyInvalidInterviewTime() throws Exception {
            CustomUserDetails principal = mock(CustomUserDetails.class);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDateTime past = LocalDateTime.now().minusDays(1);
            Map<String, Object> req = new HashMap<>();
            req.put("interviewTime", past.toString());
            String json = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/labs/{labId}/applications", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("GLOBAL_001"))
                    .andExpect(jsonPath("$.errors[0].message").value("인터뷰 시간은 미래여야 합니다."));
        }

        @Test
        @DisplayName("랩실 미존재 → 404 Not Found + LAB_006")
        void applyLabNotFound() throws Exception {
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDateTime time = LocalDateTime.now().plusDays(1);
            Map<String, Object> req = new HashMap<>();
            req.put("interviewTime", time.toString());
            String json = objectMapper.writeValueAsString(req);

            given(commandUseCase.applyToLab(eq(LAB_ID), eq(USER_ID), any(LocalDateTime.class)))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            mockMvc.perform(post("/api/labs/{labId}/applications", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"))
                    .andExpect(jsonPath("$.message").value("해당 랩실을 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("중복 신청 → 409 Conflict + APP_004")
        void applyDuplicate() throws Exception {
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LocalDateTime time = LocalDateTime.now().plusDays(1);
            Map<String, Object> req = new HashMap<>();
            req.put("interviewTime", time.toString());
            String json = objectMapper.writeValueAsString(req);

            given(commandUseCase.applyToLab(eq(LAB_ID), eq(USER_ID), any(LocalDateTime.class)))
                    .willThrow(new LabApplicationValidationException(
                            LabApplicationErrorCode.DUPLICATE_APPLICATION
                    ));

            mockMvc.perform(post("/api/labs/{labId}/applications", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("APP_004"))
                    .andExpect(jsonPath("$.message").value("이미 동일한 랩실에 가입신청을 했습니다."));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/applications")
    class ListTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + 신청서 목록 조회 성공")
        void listSuccess() throws Exception {
            LabApplication app1 = mock(LabApplication.class);
            LabApplication app2 = mock(LabApplication.class);
            Lab mockLab = mock(Lab.class);
            User mockUser = mock(User.class);
            LocalDateTime t = LocalDateTime.now().plusDays(1);
            // stub for each application
            for (LabApplication app : List.of(app1, app2)) {
                given(app.getId()).willReturn(app == app1 ? 101L : 102L);
                given(app.getLab()).willReturn(mockLab);
                given(mockLab.getId()).willReturn(LAB_ID);
                given(app.getUser()).willReturn(mockUser);
                given(mockUser.getId()).willReturn(USER_ID);
                given(mockUser.getName()).willReturn("테스트유저");
                given(mockUser.getEmail()).willReturn("test@example.com");
                given(app.getInterviewTime()).willReturn(t);
                given(app.getStatus()).willReturn(ApplicationStatus.PENDING);
            }
            given(queryUseCase.listApplicationsByLab(LAB_ID))
                    .willReturn(List.of(app1, app2));

            mockMvc.perform(get("/api/labs/{labId}/applications", LAB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("신청서 목록 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("랩실 미존재 → 404 Not Found + LAB_006")
        void listLabNotFound() throws Exception {
            given(queryUseCase.listApplicationsByLab(LAB_ID))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            mockMvc.perform(get("/api/labs/{labId}/applications", LAB_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"))
                    .andExpect(jsonPath("$.message").value("해당 랩실을 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/applications/{appId}")
    class GetTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + 신청서 조회 성공")
        void getSuccess() throws Exception {
            LabApplication mockApp = mock(LabApplication.class);
            Lab mockLab = mock(Lab.class);
            User mockUser = mock(User.class);
            LocalDateTime t = LocalDateTime.now().plusDays(1);
            given(mockApp.getId()).willReturn(APP_ID);
            given(mockApp.getLab()).willReturn(mockLab);
            given(mockLab.getId()).willReturn(LAB_ID);
            given(mockApp.getUser()).willReturn(mockUser);
            given(mockUser.getId()).willReturn(USER_ID);
            given(mockUser.getName()).willReturn("테스트유저");
            given(mockUser.getEmail()).willReturn("test@example.com");
            given(mockApp.getInterviewTime()).willReturn(t);
            given(mockApp.getStatus()).willReturn(ApplicationStatus.APPROVED);
            given(queryUseCase.getApplicationById(APP_ID))
                    .willReturn(mockApp);

            mockMvc.perform(get("/api/labs/{labId}/applications/{appId}", LAB_ID, APP_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("신청서 조회 성공"))
                    .andExpect(jsonPath("$.data.id").value(APP_ID))
                    .andExpect(jsonPath("$.data.labId").value(LAB_ID))
                    .andExpect(jsonPath("$.data.applicant.id").value(USER_ID))
                    .andExpect(jsonPath("$.data.interviewTime").exists())
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));
        }

        @Test
        @DisplayName("신청서 없음 → 404 Not Found + APP_008")
        void getNotFound() throws Exception {
            given(queryUseCase.getApplicationById(APP_ID))
                    .willThrow(new LabApplicationNotFoundException(
                            LabApplicationErrorCode.APPLICATION_NOT_FOUND
                    ));

            mockMvc.perform(get("/api/labs/{labId}/applications/{appId}", LAB_ID, APP_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("APP_008"))
                    .andExpect(jsonPath("$.message").value("해당 가입신청을 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("PUT /api/labs/{labId}/applications/{appId}/approve")
    class ApproveTests {

        @Test
        @DisplayName("정상 승인 → 204 No Content")
        void approveSuccess() throws Exception {
            mockMvc.perform(put("/api/labs/{labId}/applications/{appId}/approve", LAB_ID, APP_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("신청서 없음 → 404 Not Found + APP_008")
        void approveNotFound() throws Exception {
            doThrow(new LabApplicationNotFoundException(
                    LabApplicationErrorCode.APPLICATION_NOT_FOUND
            ))
                    .when(commandUseCase).approveApplication(APP_ID);

            mockMvc.perform(put("/api/labs/{labId}/applications/{appId}/approve", LAB_ID, APP_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("APP_008"))
                    .andExpect(jsonPath("$.message").value("해당 가입신청을 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("PUT /api/labs/{labId}/applications/{appId}/reject")
    class RejectTests {

        @Test
        @DisplayName("정상 거절 → 204 No Content")
        void rejectSuccess() throws Exception {
            mockMvc.perform(put("/api/labs/{labId}/applications/{appId}/reject", LAB_ID, APP_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("신청서 없음 → 404 Not Found + APP_008")
        void rejectNotFound() throws Exception {
            doThrow(new LabApplicationNotFoundException(
                    LabApplicationErrorCode.APPLICATION_NOT_FOUND
            ))
                    .when(commandUseCase).rejectApplication(APP_ID);

            mockMvc.perform(put("/api/labs/{labId}/applications/{appId}/reject", LAB_ID, APP_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("APP_008"))
                    .andExpect(jsonPath("$.message").value("해당 가입신청을 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("DELETE /api/labs/{labId}/applications/{appId}")
    class CancelTests {

        @Test
        @DisplayName("정상 취소 → 204 No Content")
        void cancelSuccess() throws Exception {
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            mockMvc.perform(delete("/api/labs/{labId}/applications/{appId}", LAB_ID, APP_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("신청서 없음 → 404 Not Found + APP_008")
        void cancelNotFound() throws Exception {
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            doThrow(new LabApplicationNotFoundException(
                    LabApplicationErrorCode.APPLICATION_NOT_FOUND
            ))
                    .when(commandUseCase).cancelApplication(APP_ID, USER_ID);

            mockMvc.perform(delete("/api/labs/{labId}/applications/{appId}", LAB_ID, APP_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("APP_008"))
                    .andExpect(jsonPath("$.message").value("해당 가입신청을 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("권한 없음 → 401 Unauthorized + APP_007")
        void cancelUnauthorized() throws Exception {
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            doThrow(new LabApplicationValidationException(
                    LabApplicationErrorCode.UNAUTHORIZED_CANCEL_ATTEMPT
            ))
                    .when(commandUseCase).cancelApplication(APP_ID, USER_ID);

            mockMvc.perform(delete("/api/labs/{labId}/applications/{appId}", LAB_ID, APP_ID))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("APP_007"))
                    .andExpect(jsonPath("$.message").value("본인의 신청만 취소할 수 있습니다."));
        }
    }
}
