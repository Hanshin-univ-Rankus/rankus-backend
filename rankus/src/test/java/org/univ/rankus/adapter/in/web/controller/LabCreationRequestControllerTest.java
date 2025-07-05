package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.dto.request.LabCreationRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.LabCreationRequestRejectDto;
import org.univ.rankus.application.port.in.command.LabCreationRequestCommandUseCase;
import org.univ.rankus.application.port.in.query.LabCreationRequestQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.LabCategory;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequest;
import org.univ.rankus.domain.model.lab.creation.LabCreationStatus;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequestErrorCode;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequestNotFoundException;
import org.univ.rankus.domain.model.lab.creation.LabCreationRequestValidationException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabCreationRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class LabCreationRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LabCreationRequestCommandUseCase commandUseCase;

    @MockitoBean
    private LabCreationRequestQueryUseCase queryUseCase;

    private static final Long REQUEST_ID = 123L;
    private static final Long USER_ID = 42L;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("POST /api/lab-creation-requests")
    class CreateLabCreationRequestTests {

        @Test
        @DisplayName("랩실 생성 신청 → 201 Created + Location 헤더")
        void createLabCreationRequestSuccess() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LabCreationRequestDto request = new LabCreationRequestDto(
                    "AI 연구실",
                    LabCategory.AI,
                    "인공지능 관련 연구를 수행하는 연구실입니다."
            );

            LabCreationRequest createdRequest = createMockLabCreationRequest(LabCreationStatus.PENDING);
            given(commandUseCase.createLabCreationRequest(
                    eq("AI 연구실"),
                    eq(LabCategory.AI),
                    eq("인공지능 관련 연구를 수행하는 연구실입니다."),
                    eq(USER_ID)
            )).willReturn(createdRequest);

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(post("/api/lab-creation-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/lab-creation-requests/" + REQUEST_ID))
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("랩실 생성 신청 성공"))
                    .andExpect(jsonPath("$.data.id").value(REQUEST_ID))
                    .andExpect(jsonPath("$.data.requestedLabName").value("AI 연구실"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("잘못된 입력으로 신청 → 400 Bad Request")
        void createLabCreationRequestValidationError() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LabCreationRequestDto request = new LabCreationRequestDto(
                    "", // 빈 랩실명
                    LabCategory.AI,
                    "설명"
            );

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(post("/api/lab-creation-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("GLOBAL_001"));
        }

        @Test
        @DisplayName("중복 신청 → 409 Conflict")
        void createLabCreationRequestDuplicate() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LabCreationRequestDto request = new LabCreationRequestDto(
                    "AI 연구실",
                    LabCategory.AI,
                    "인공지능 관련 연구를 수행하는 연구실입니다."
            );

            given(commandUseCase.createLabCreationRequest(
                    anyString(),
                    any(LabCategory.class),
                    anyString(),
                    eq(USER_ID)
            )).willThrow(new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.DUPLICATE_LAB_NAME_REQUEST
            ));

            String json = objectMapper.writeValueAsString(request);

            // when & then
            mockMvc.perform(post("/api/lab-creation-requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("LCR_006"))
                    .andExpect(jsonPath("$.message").value("이미 동일한 이름의 랩실 생성 신청이 존재합니다."));
        }
    }

    @Nested
    @DisplayName("GET /api/lab-creation-requests/my")
    class GetCurrentUserLabCreationRequestsTests {

        @Test
        @DisplayName("내 신청 목록 조회 → 200 OK")
        void getCurrentUserLabCreationRequestsSuccess() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            List<LabCreationRequest> myRequests = Arrays.asList(
                    createMockLabCreationRequest(LabCreationStatus.PENDING),
                    createMockLabCreationRequest(LabCreationStatus.APPROVED)
            );
            given(queryUseCase.getLabCreationRequestsByRequester(USER_ID)).willReturn(myRequests);

            // when & then
            mockMvc.perform(get("/api/lab-creation-requests/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("내 신청 목록 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/lab-creation-requests/{requestId}")
    class GetLabCreationRequestTests {

        @Test
        @DisplayName("신청 상세 조회 → 200 OK")
        void getLabCreationRequestSuccess() throws Exception {
            // given
            LabCreationRequest request = createMockLabCreationRequest(LabCreationStatus.PENDING);
            given(queryUseCase.getLabCreationRequestById(REQUEST_ID)).willReturn(request);

            // when & then
            mockMvc.perform(get("/api/lab-creation-requests/{requestId}", REQUEST_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 생성 신청 조회 성공"))
                    .andExpect(jsonPath("$.data.id").value(REQUEST_ID))
                    .andExpect(jsonPath("$.data.requestedLabName").value("AI 연구실"));
        }

        @Test
        @DisplayName("존재하지 않는 신청 조회 → 404 Not Found")
        void getLabCreationRequestNotFound() throws Exception {
            // given
            given(queryUseCase.getLabCreationRequestById(REQUEST_ID))
                    .willThrow(new LabCreationRequestNotFoundException());

            // when & then
            mockMvc.perform(get("/api/lab-creation-requests/{requestId}", REQUEST_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LCR_009"))
                    .andExpect(jsonPath("$.message").value("해당 랩실 생성 신청을 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("DELETE /api/lab-creation-requests/{requestId}")
    class CancelLabCreationRequestTests {

        @Test
        @DisplayName("신청 취소 → 204 No Content")
        void cancelLabCreationRequestSuccess() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // when & then
            mockMvc.perform(delete("/api/lab-creation-requests/{requestId}", REQUEST_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("처리된 신청 취소 시도 → 422 Unprocessable Entity")
        void cancelProcessedLabCreationRequest() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            doThrow(new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION
            )).when(commandUseCase).cancelLabCreationRequest(REQUEST_ID, USER_ID);

            // when & then
            mockMvc.perform(delete("/api/lab-creation-requests/{requestId}", REQUEST_ID))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("LCR_007"))
                    .andExpect(jsonPath("$.message").value("이미 처리된 신청은 상태를 변경할 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("GET /api/lab-creation-requests/admin/all")
    class GetAllLabCreationRequestsTests {

        @Test
        @DisplayName("관리자 전체 신청 목록 조회 → 200 OK")
        void getAllLabCreationRequestsSuccess() throws Exception {
            // given
            List<LabCreationRequest> allRequests = Arrays.asList(
                    createMockLabCreationRequest(LabCreationStatus.PENDING),
                    createMockLabCreationRequest(LabCreationStatus.APPROVED),
                    createMockLabCreationRequest(LabCreationStatus.REJECTED)
            );
            given(queryUseCase.getAllLabCreationRequests()).willReturn(allRequests);

            // when & then
            mockMvc.perform(get("/api/lab-creation-requests/admin/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("전체 신청 목록 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));
        }
    }

    @Nested
    @DisplayName("GET /api/lab-creation-requests/admin/pending")
    class GetPendingLabCreationRequestsTests {

        @Test
        @DisplayName("관리자 대기 중인 신청 목록 조회 → 200 OK")
        void getPendingLabCreationRequestsSuccess() throws Exception {
            // given
            List<LabCreationRequest> pendingRequests = List.of(
                    createMockLabCreationRequest(LabCreationStatus.PENDING)
            );
            given(queryUseCase.getPendingLabCreationRequests()).willReturn(pendingRequests);

            // when & then
            mockMvc.perform(get("/api/lab-creation-requests/admin/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("대기 중인 신청 목록 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /api/lab-creation-requests/{requestId}/approve")
    class ApproveLabCreationRequestTests {

        @Test
        @DisplayName("신청 승인 → 204 No Content")
        void approveLabCreationRequestSuccess() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            // when & then
            mockMvc.perform(put("/api/lab-creation-requests/{requestId}/approve", REQUEST_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("이미 처리된 신청 승인 시도 → 422 Unprocessable Entity")
        void approveProcessedLabCreationRequest() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            doThrow(new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION
            )).when(commandUseCase).approveLabCreationRequest(REQUEST_ID, USER_ID);

            // when & then
            mockMvc.perform(put("/api/lab-creation-requests/{requestId}/approve", REQUEST_ID))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("LCR_007"))
                    .andExpect(jsonPath("$.message").value("이미 처리된 신청은 상태를 변경할 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("PUT /api/lab-creation-requests/{requestId}/reject")
    class RejectLabCreationRequestTests {

        @Test
        @DisplayName("신청 거절 → 204 No Content")
        void rejectLabCreationRequestSuccess() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LabCreationRequestRejectDto rejectDto = new LabCreationRequestRejectDto(
                    "요청하신 연구실의 연구 분야가 명확하지 않습니다."
            );

            String json = objectMapper.writeValueAsString(rejectDto);

            // when & then
            mockMvc.perform(put("/api/lab-creation-requests/{requestId}/reject", REQUEST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("이미 처리된 신청 거절 시도 → 422 Unprocessable Entity")
        void rejectProcessedLabCreationRequest() throws Exception {
            // given
            CustomUserDetails principal = mock(CustomUserDetails.class);
            given(principal.getUserId()).willReturn(USER_ID);
            SecurityContextHolder.getContext()
                    .setAuthentication(new TestingAuthenticationToken(principal, null));

            LabCreationRequestRejectDto rejectDto = new LabCreationRequestRejectDto("거절 사유");
            String json = objectMapper.writeValueAsString(rejectDto);

            doThrow(new LabCreationRequestValidationException(
                    LabCreationRequestErrorCode.CANNOT_CHANGE_STATUS_AFTER_DECISION
            )).when(commandUseCase).rejectLabCreationRequest(eq(REQUEST_ID), eq(USER_ID), anyString());

            // when & then
            mockMvc.perform(put("/api/lab-creation-requests/{requestId}/reject", REQUEST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("LCR_007"));
        }
    }

    // Helper methods
    private LabCreationRequest createMockLabCreationRequest(LabCreationStatus status) {
        LabCreationRequest request = mock(LabCreationRequest.class);
        User requestor = DomainUserFactory.buildStudentUser();

        given(request.getId()).willReturn(REQUEST_ID);
        given(request.getRequestedLabName()).willReturn("AI 연구실");
        given(request.getRequestedCategory()).willReturn(LabCategory.AI);
        given(request.getRequestedDescription()).willReturn("인공지능 관련 연구를 수행하는 연구실입니다.");
        given(request.getStatus()).willReturn(status);
        given(request.getRequester()).willReturn(requestor);
        given(request.getCreatedAt()).willReturn(LocalDateTime.now().minusDays(1));
        given(request.getUpdatedAt()).willReturn(LocalDateTime.now());

        return request;
    }
}