package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.LabApplicationUseCase;
import org.univ.rankus.common.exception.GlobalExceptionHandler;
import org.univ.rankus.config.SecurityConfig;
import org.univ.rankus.domain.model.lab.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LabApplicationController 슬라이스 테스트
 * - 가입신청 등록·조회
 * - 가입신청 승인·거절
 */
@WebMvcTest(
        controllers = LabApplicationController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        ),
        properties = "spring.mvc.trailing-slash.match=false"
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("LabApplicationController 슬라이스 테스트")
class LabApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LabApplicationUseCase applicationUseCase;

    private static final MediaType JSON = MediaType.APPLICATION_JSON;
    private static final String BASE = "/api/labs/{labId}/applications";

    @Nested
    @DisplayName("가입신청 등록 API [POST /applications]")
    class RegisterTests {

        @Test
        @DisplayName("올바른 요청이면 가입신청을 등록하고 DTO를 반환한다")
        void registerApplication_success() throws Exception {
            Long labId = 1L;
            Long userId = 10L;
            String userName = "홍길동";
            LocalDateTime interview = LocalDateTime.of(2025, 5, 30, 14, 0);

            Lab dummyLab = new Lab("Dummy", "설명", "학과", LabCategory.AI);
            LabApplication app = new LabApplication(dummyLab, userId, interview);

            given(applicationUseCase.registerApplication(labId, userId, userName, interview))
                    .willReturn(app);

            String body = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "userId", userId,
                            "userName", userName,
                            "interviewTime", interview.toString()
                    )
            );

            mockMvc.perform(post(BASE, labId)
                            .contentType(JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(JSON))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.interviewTime").value("2025-05-30T14:00:00"))
                    .andExpect(jsonPath("$.status").value(ApplicationStatus.PENDING.name()));
        }

        @Test
        @DisplayName("존재하지 않는 랩실 ID면 404 Not Found를 반환한다")
        void registerApplication_labNotFound() throws Exception {
            Long labId = 99L;
            Long userId = 5L;
            String userName = "테스트";
            LocalDateTime interview = LocalDateTime.now();

            given(applicationUseCase.registerApplication(eq(labId), anyLong(), anyString(), any()))
                    .willThrow(new NoSuchElementException("랩실을 찾을 수 없습니다."));

            String body = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "userId", userId,
                            "userName", userName,
                            "interviewTime", interview.toString()
                    )
            );

            mockMvc.perform(post(BASE, labId)
                            .contentType(JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("userId가 누락되면 400 Bad Request를 반환한다")
        void registerApplication_missingUserId() throws Exception {
            Long labId = 1L;
            String body = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "userName", "홍길동",
                            "interviewTime", LocalDateTime.now().toString()
                    )
            );

            mockMvc.perform(post(BASE, labId)
                            .contentType(JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("userName이 누락되면 400 Bad Request를 반환한다")
        void registerApplication_missingUserName() throws Exception {
            Long labId = 1L;
            String body = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "userId", 10L,
                            "interviewTime", LocalDateTime.now().toString()
                    )
            );

            mockMvc.perform(post(BASE, labId)
                            .contentType(JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("interviewTime이 누락되면 400 Bad Request를 반환한다")
        void registerApplication_missingInterviewTime() throws Exception {
            Long labId = 1L;
            String body = objectMapper.writeValueAsString(
                    java.util.Map.of(
                            "userId", 10L,
                            "userName", "홍길동"
                    )
            );

            mockMvc.perform(post(BASE, labId)
                            .contentType(JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("가입신청 목록 조회 API [GET /applications]")
    class ListTests {

        @Test
        @DisplayName("등록된 가입신청이 있으면 모든 신청을 반환한다")
        void listApplications_success() throws Exception {
            Long labId = 2L;
            LocalDateTime t1 = LocalDateTime.of(2025, 6, 1, 10, 0);
            LocalDateTime t2 = t1.plusHours(1);

            Lab dummyLab = new Lab("Dummy", "desc", "학과", LabCategory.DB);
            LabApplication app1 = new LabApplication(dummyLab, 5L, t1);
            LabApplication app2 = new LabApplication(dummyLab, 6L, t2);

            given(applicationUseCase.listApplications(labId))
                    .willReturn(List.of(app1, app2));

            mockMvc.perform(get(BASE, labId)
                            .accept(JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].userId").value(5))
                    .andExpect(jsonPath("$[1].status").value(ApplicationStatus.PENDING.name()));
        }

        @Test
        @DisplayName("등록된 가입신청이 없으면 빈 배열을 반환한다")
        void listApplications_empty() throws Exception {
            Long labId = 3L;
            given(applicationUseCase.listApplications(labId))
                    .willReturn(List.of());

            mockMvc.perform(get(BASE, labId)
                            .accept(JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("ID 파라미터가 숫자가 아니면 400 Bad Request를 반환한다")
        void listApplications_invalidId() throws Exception {
            mockMvc.perform(get("/api/labs/{labId}/applications", "abc")
                            .accept(JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("가입신청 승인 API [POST /applications/{appId}/approve]")
    class ApproveEndpoint {

        @Test
        @DisplayName("올바른 요청이면 204 No Content를 반환한다")
        void approve_success() throws Exception {
            Long labId = 1L;
            Long appId = 5L;

            willDoNothing().given(applicationUseCase)
                    .approveApplication(labId, appId);

            mockMvc.perform(post(BASE + "/{appId}/approve", labId, appId)
                            .contentType(JSON))
                    .andExpect(status().isNoContent());

            then(applicationUseCase).should().approveApplication(labId, appId);
        }

        @Test
        @DisplayName("존재하지 않는 applicationId면 404 Not Found를 반환한다")
        void approve_notFound() throws Exception {
            Long labId = 1L;
            Long appId = 99L;

            willThrow(new NoSuchElementException("해당 랩실의 가입신청을 찾을 수 없습니다."))
                    .given(applicationUseCase)
                    .approveApplication(labId, appId);

            mockMvc.perform(post(BASE + "/{appId}/approve", labId, appId)
                            .contentType(JSON))
                    .andExpect(status().isNotFound());

            then(applicationUseCase).should().approveApplication(labId, appId);
        }

        @Test
        @DisplayName("이미 처리된 신청이면 400 Bad Request를 반환한다")
        void approve_alreadyProcessed() throws Exception {
            Long labId = 1L;
            Long appId = 5L;

            willThrow(new IllegalStateException("이미 처리된 신청입니다."))
                    .given(applicationUseCase)
                    .approveApplication(labId, appId);

            mockMvc.perform(post(BASE + "/{appId}/approve", labId, appId)
                            .contentType(JSON))
                    .andExpect(status().isBadRequest());

            then(applicationUseCase).should().approveApplication(labId, appId);
        }
    }

    @Nested
    @DisplayName("가입신청 거절 API [POST /applications/{appId}/reject]")
    class RejectEndpoint {

        @Test
        @DisplayName("올바른 요청이면 204 No Content를 반환한다")
        void reject_success() throws Exception {
            Long labId = 2L;
            Long appId = 7L;

            willDoNothing().given(applicationUseCase)
                    .rejectApplication(labId, appId);

            mockMvc.perform(post(BASE + "/{appId}/reject", labId, appId)
                            .contentType(JSON))
                    .andExpect(status().isNoContent());

            then(applicationUseCase).should().rejectApplication(labId, appId);
        }

        @Test
        @DisplayName("존재하지 않는 applicationId면 404 Not Found를 반환한다")
        void reject_notFound() throws Exception {
            Long labId = 2L;
            Long appId = 99L;

            willThrow(new NoSuchElementException("해당 랩실의 가입신청을 찾을 수 없습니다."))
                    .given(applicationUseCase)
                    .rejectApplication(labId, appId);

            mockMvc.perform(post(BASE + "/{appId}/reject", labId, appId)
                            .contentType(JSON))
                    .andExpect(status().isNotFound());

            then(applicationUseCase).should().rejectApplication(labId, appId);
        }

        @Test
        @DisplayName("이미 처리된 신청이면 400 Bad Request를 반환한다")
        void reject_alreadyProcessed() throws Exception {
            Long labId = 2L;
            Long appId = 7L;

            willThrow(new IllegalStateException("이미 처리된 신청입니다."))
                    .given(applicationUseCase)
                    .rejectApplication(labId, appId);

            mockMvc.perform(post(BASE + "/{appId}/reject", labId, appId)
                            .contentType(JSON))
                    .andExpect(status().isBadRequest());

            then(applicationUseCase).should().rejectApplication(labId, appId);
        }
    }
}