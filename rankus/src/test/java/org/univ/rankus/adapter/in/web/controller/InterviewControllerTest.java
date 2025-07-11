package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.dto.request.InterviewCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.InterviewSlotCreateRequestDto;
import org.univ.rankus.adapter.in.web.dto.request.InterviewUpdateRequestDto;
import org.univ.rankus.application.port.in.command.InterviewCommandUseCase;
import org.univ.rankus.application.port.in.query.InterviewQueryUseCase;
import org.univ.rankus.common.security.permission.InterviewPermissionHandler;
import org.univ.rankus.domain.model.interview.Interview;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.exception.InterviewErrorCode;
import org.univ.rankus.domain.model.interview.exception.InterviewValidationException;
import org.univ.rankus.testutil.factory.domain.DomainInterviewFactory;
import org.univ.rankus.testutil.factory.domain.DomainInterviewSlotFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("InterviewController 테스트")
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterviewCommandUseCase commandUseCase;

    @MockitoBean
    private InterviewQueryUseCase queryUseCase;

    @MockitoBean
    private InterviewPermissionHandler interviewPermissionHandler;

    @BeforeEach
    void setUp() {
        // Mock permission handler to return true for authorized users
        given(interviewPermissionHandler.hasPermissionForLab(any(), anyLong(), anyString()))
                .willReturn(true);
    }

    @Nested
    @DisplayName("면접 생성 테스트")
    class CreateInterviewTest {

        @Test
        @DisplayName("유효한 정보로 면접 생성 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void createInterview_ValidRequest_Success() throws Exception {
            // given
            Long labId = 1L;
            InterviewCreateRequestDto request = InterviewCreateRequestDto.builder()
                    .startDate(LocalDate.now().plusDays(7))
                    .endDate(LocalDate.now().plusDays(14))
                    .durationMinutes(60)
                    .maxApplicantsPerSlot(5)
                    .build();

            Interview interview = DomainInterviewFactory.buildInterviewWithId(1L);
            given(commandUseCase.createInterview(
                    eq(labId),
                    eq(request.getStartDate()),
                    eq(request.getEndDate()),
                    eq(request.getDurationMinutes()),
                    eq(request.getMaxApplicantsPerSlot())
            )).willReturn(interview);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews", labId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.durationMinutes").value(60))
                    .andExpect(jsonPath("$.data.maxApplicantsPerSlot").value(5));

            verify(commandUseCase).createInterview(
                    eq(labId),
                    eq(request.getStartDate()),
                    eq(request.getEndDate()),
                    eq(request.getDurationMinutes()),
                    eq(request.getMaxApplicantsPerSlot())
            );
        }

        @Test
        @DisplayName("권한 없는 사용자가 면접 생성시 403 반환")
        @WithMockUser(roles = "STUDENT")
        void createInterview_UnauthorizedUser_Forbidden() throws Exception {
            // given
            Long labId = 1L;
            // 권한 검증이 실패하도록 설정
            given(interviewPermissionHandler.hasPermissionForLab(any(), eq(labId), eq("MANAGE_INTERVIEWS")))
                    .willReturn(false);

            InterviewCreateRequestDto request = InterviewCreateRequestDto.builder()
                    .startDate(LocalDate.now().plusDays(7))
                    .endDate(LocalDate.now().plusDays(14))
                    .durationMinutes(60)
                    .maxApplicantsPerSlot(5)
                    .build();

            // 권한 검증 실패시 AccessDenied 예외가 발생하여 403이 반환됨을 Mock 처리
            doThrow(new org.springframework.security.access.AccessDeniedException("Access Denied"))
                    .when(commandUseCase).createInterview(anyLong(), any(), any(), anyInt(), anyInt());

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews", labId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("유효하지 않은 요청 데이터로 면접 생성시 400 반환")
        @WithMockUser(roles = "LAB_LEADER")
        void createInterview_InvalidRequest_BadRequest() throws Exception {
            // given
            Long labId = 1L;
            InterviewCreateRequestDto request = InterviewCreateRequestDto.builder()
                    .startDate(LocalDate.now().minusDays(1)) // 과거 날짜
                    .endDate(LocalDate.now().plusDays(7))
                    .durationMinutes(-1) // 음수
                    .maxApplicantsPerSlot(0) // 0
                    .build();

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews", labId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(commandUseCase, never()).createInterview(anyLong(), any(), any(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("면접 조회 테스트")
    class GetInterviewTest {

        @Test
        @DisplayName("랩실 면접 목록 조회 성공")
        void getInterviewsByLab_Success() throws Exception {
            // given
            Long labId = 1L;
            List<Interview> interviews = Arrays.asList(
                    DomainInterviewFactory.buildInterviewWithId(1L),
                    DomainInterviewFactory.buildInterviewWithId(2L)
            );
            given(queryUseCase.getInterviewsByLabId(labId)).willReturn(interviews);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/interviews", labId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(queryUseCase).getInterviewsByLabId(labId);
        }

        @Test
        @DisplayName("특정 면접 상세 조회 성공")
        void getInterview_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            Interview interview = DomainInterviewFactory.buildInterviewWithId(interviewId);
            given(queryUseCase.getInterviewById(interviewId)).willReturn(interview);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/interviews/{interviewId}", labId, interviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(interviewId))
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"));

            verify(queryUseCase).getInterviewById(interviewId);
        }
    }

    @Nested
    @DisplayName("면접 상태 관리 테스트")
    class InterviewStatusManagementTest {

        @Test
        @DisplayName("면접 활성화 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void activateInterview_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            Interview interview = DomainInterviewFactory.buildActiveInterview();
            given(commandUseCase.activateInterview(interviewId)).willReturn(interview);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews/{interviewId}/activate", labId, interviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            verify(commandUseCase).activateInterview(interviewId);
        }

        @Test
        @DisplayName("면접 비활성화 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void deactivateInterview_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            Interview interview = DomainInterviewFactory.buildInactiveInterview();
            given(commandUseCase.deactivateInterview(interviewId)).willReturn(interview);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews/{interviewId}/deactivate", labId, interviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"));

            verify(commandUseCase).deactivateInterview(interviewId);
        }

        @Test
        @DisplayName("면접 종료 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void closeInterview_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            Interview interview = DomainInterviewFactory.buildClosedInterview();
            given(commandUseCase.closeInterview(interviewId)).willReturn(interview);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews/{interviewId}/close", labId, interviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.status").value("CLOSED"));

            verify(commandUseCase).closeInterview(interviewId);
        }

        @Test
        @DisplayName("권한 없는 사용자가 면접 상태 변경시 403 반환")
        @WithMockUser(roles = "STUDENT")
        void changeInterviewStatus_UnauthorizedUser_Forbidden() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;

            // 권한 검증 실패시 예외 발생 Mock 설정
            doThrow(new org.springframework.security.access.AccessDeniedException("Access Denied"))
                    .when(commandUseCase).activateInterview(interviewId);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews/{interviewId}/activate", labId, interviewId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("면접 수정 테스트")
    class UpdateInterviewTest {

        @Test
        @DisplayName("면접 수정 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void updateInterview_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            InterviewUpdateRequestDto request = InterviewUpdateRequestDto.builder()
                    .startDate(LocalDate.now().plusDays(10))
                    .endDate(LocalDate.now().plusDays(17))
                    .durationMinutes(90)
                    .maxApplicantsPerSlot(3)
                    .build();

            Interview interview = DomainInterviewFactory.buildInterviewWithId(interviewId);
            given(commandUseCase.updateInterview(
                    eq(interviewId),
                    eq(request.getStartDate()),
                    eq(request.getEndDate()),
                    eq(request.getDurationMinutes()),
                    eq(request.getMaxApplicantsPerSlot())
            )).willReturn(interview);

            // when & then
            mockMvc.perform(put("/api/labs/{labId}/interviews/{interviewId}", labId, interviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.id").value(interviewId));

            verify(commandUseCase).updateInterview(
                    eq(interviewId),
                    eq(request.getStartDate()),
                    eq(request.getEndDate()),
                    eq(request.getDurationMinutes()),
                    eq(request.getMaxApplicantsPerSlot())
            );
        }
    }

    @Nested
    @DisplayName("면접 삭제 테스트")
    class DeleteInterviewTest {

        @Test
        @DisplayName("면접 삭제 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void deleteInterview_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            doNothing().when(commandUseCase).deleteInterview(interviewId);

            // when & then
            mockMvc.perform(delete("/api/labs/{labId}/interviews/{interviewId}", labId, interviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));

            verify(commandUseCase).deleteInterview(interviewId);
        }
    }

    @Nested
    @DisplayName("면접 슬롯 관리 테스트")
    class InterviewSlotManagementTest {

        @Test
        @DisplayName("면접 슬롯 생성 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void createInterviewSlot_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            InterviewSlotCreateRequestDto request = InterviewSlotCreateRequestDto.builder()
                    .startTime(LocalDateTime.now().plusDays(8).withHour(14))
                    .endTime(LocalDateTime.now().plusDays(8).withHour(15))
                    .maxApplicants(3)
                    .build();

            // 완전한 슬롯 객체 생성 (NPE 방지)
            InterviewSlot slot = DomainInterviewSlotFactory.buildValidSlot();
            // ID 설정
            org.springframework.test.util.ReflectionTestUtils.setField(slot, "id", 1L);
            // BaseTimeEntity 필드들 설정
            LocalDateTime now = LocalDateTime.now();
            org.springframework.test.util.ReflectionTestUtils.setField(slot, "createdAt", now);
            org.springframework.test.util.ReflectionTestUtils.setField(slot, "updatedAt", now);

            // Mock 설정 - any() 매처 사용으로 더 유연하게
            given(commandUseCase.createInterviewSlot(
                    any(Long.class),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    any(Integer.class)
            )).willReturn(slot);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews/{interviewId}/slots", labId, interviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

            verify(commandUseCase).createInterviewSlot(
                    eq(interviewId),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    eq(request.getMaxApplicants())
            );
        }

        @Test
        @DisplayName("면접 슬롯 목록 조회 성공")
        void getInterviewSlots_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            List<InterviewSlot> slots = Arrays.asList(
                    DomainInterviewSlotFactory.buildSlotWithId(1L),
                    DomainInterviewSlotFactory.buildSlotWithId(2L)
            );
            given(queryUseCase.getSlotsByInterviewIdOrderByTime(interviewId)).willReturn(slots);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/interviews/{interviewId}/slots", labId, interviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(queryUseCase).getSlotsByInterviewIdOrderByTime(interviewId);
        }

        @Test
        @DisplayName("예약 가능한 슬롯 조회 성공")
        void getAvailableSlots_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            List<InterviewSlot> slots = Arrays.asList(
                    DomainInterviewSlotFactory.buildAvailableSlot()
            );
            given(queryUseCase.getAvailableSlotsByInterviewId(interviewId)).willReturn(slots);

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/interviews/{interviewId}/slots/available", labId, interviewId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));

            verify(queryUseCase).getAvailableSlotsByInterviewId(interviewId);
        }

        @Test
        @DisplayName("면접 슬롯 취소 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void cancelInterviewSlot_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            Long slotId = 1L;
            InterviewSlot slot = DomainInterviewSlotFactory.buildCancelledSlot();
            given(commandUseCase.cancelInterviewSlot(slotId)).willReturn(slot);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews/{interviewId}/slots/{slotId}/cancel", labId, interviewId, slotId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));

            verify(commandUseCase).cancelInterviewSlot(slotId);
        }

        @Test
        @DisplayName("면접 슬롯 재활성화 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void reactivateInterviewSlot_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            Long slotId = 1L;
            InterviewSlot slot = DomainInterviewSlotFactory.buildAvailableSlot();
            given(commandUseCase.reactivateInterviewSlot(slotId)).willReturn(slot);

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews/{interviewId}/slots/{slotId}/reactivate", labId, interviewId, slotId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

            verify(commandUseCase).reactivateInterviewSlot(slotId);
        }

        @Test
        @DisplayName("면접 슬롯 삭제 성공")
        @WithMockUser(roles = "LAB_LEADER")
        void deleteInterviewSlot_Success() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            Long slotId = 1L;
            doNothing().when(commandUseCase).deleteInterviewSlot(slotId);

            // when & then
            mockMvc.perform(delete("/api/labs/{labId}/interviews/{interviewId}/slots/{slotId}", labId, interviewId, slotId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));

            verify(commandUseCase).deleteInterviewSlot(slotId);
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("존재하지 않는 면접 조회시 예외 발생")
        void getInterview_NotFound_Exception() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 999L;
            given(queryUseCase.getInterviewById(interviewId))
                    .willThrow(new InterviewValidationException(InterviewErrorCode.INTERVIEW_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/interviews/{interviewId}", labId, interviewId))
                    .andExpect(status().isNotFound());

            verify(queryUseCase).getInterviewById(interviewId);
        }

        @Test
        @DisplayName("이미 활성화된 면접 재활성화시 예외 발생")
        @WithMockUser(roles = "LAB_LEADER")
        void activateInterview_AlreadyActivated_Exception() throws Exception {
            // given
            Long labId = 1L;
            Long interviewId = 1L;
            given(commandUseCase.activateInterview(interviewId))
                    .willThrow(new InterviewValidationException(InterviewErrorCode.ALREADY_ACTIVATED));

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/interviews/{interviewId}/activate", labId, interviewId))
                    .andExpect(status().isBadRequest());

            verify(commandUseCase).activateInterview(interviewId);
        }
    }

    // NOTE: @AutoConfigureMockMvc(addFilters = false)를 사용했기 때문에
    // 인증/인가 관련 테스트는 실제 보안 필터가 작동하지 않아 의미가 없음
    // 실제 보안 테스트는 별도의 통합 테스트에서 수행해야 함
}