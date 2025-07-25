package org.univ.rankus.adapter.in.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.dto.response.LabDashboardResponseDto;
import org.univ.rankus.application.port.in.query.LabDashboardQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class LabDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LabDashboardQueryUseCase labDashboardQueryUseCase;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("성공: 랩실 대시보드 조회")
    void getDashboard_Success() throws Exception {
        // given
        Long labId = 1L;

        LabDashboardResponseDto.LabBasicInfo labInfo = LabDashboardResponseDto.LabBasicInfo.builder()
                .id(labId)
                .name("AI 연구실")
                .professorName("김교수")
                .description("인공지능과 머신러닝을 연구하는 랩실입니다")
                .establishedDate(LocalDate.of(2024, 1, 1))
                .representativeImage(null)
                .build();

        LabDashboardResponseDto expectedResponse = LabDashboardResponseDto.builder()
                .labInfo(labInfo)
                .notices(Collections.emptyList())
                .votes(Collections.emptyList())
                .resources(Collections.emptyList())
                .schedules(Collections.emptyList())
                .members(Collections.emptyList())
                .build();

        given(labDashboardQueryUseCase.getDashboardByLabId(labId))
                .willReturn(expectedResponse);

        // 인증된 사용자 설정
        User user = DomainUserFactory.buildValidUserWithId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mockMvc.perform(get("/api/labs/{labId}/dashboard", labId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("랩실 대시보드 조회 성공"))
                .andExpect(jsonPath("$.data.labInfo.id").value(labId))
                .andExpect(jsonPath("$.data.labInfo.name").value("AI 연구실"))
                .andExpect(jsonPath("$.data.labInfo.professorName").value("김교수"))
                .andExpect(jsonPath("$.data.notices").isArray())
                .andExpect(jsonPath("$.data.votes").isArray())
                .andExpect(jsonPath("$.data.resources").isArray())
                .andExpect(jsonPath("$.data.schedules").isArray())
                .andExpect(jsonPath("$.data.members").isArray());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 랩실")
    void getDashboard_LabNotFound() throws Exception {
        // given
        Long labId = 999L;
        given(labDashboardQueryUseCase.getDashboardByLabId(labId))
                .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

        // 인증된 사용자 설정
        User user = DomainUserFactory.buildValidUserWithId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mockMvc.perform(get("/api/labs/{labId}/dashboard", labId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("실패: 잘못된 랩실 ID (0)")
    void getDashboard_InvalidLabId_Zero() throws Exception {
        // when & then
        mockMvc.perform(get("/api/labs/{labId}/dashboard", 0))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 잘못된 랩실 ID (음수)")
    void getDashboard_InvalidLabId_Negative() throws Exception {
        // when & then
        mockMvc.perform(get("/api/labs/{labId}/dashboard", -1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공: 응답 구조 검증")
    void getDashboard_ResponseStructure() throws Exception {
        // given
        Long labId = 1L;

        LabDashboardResponseDto.LabBasicInfo labInfo = LabDashboardResponseDto.LabBasicInfo.builder()
                .id(labId)
                .name("테스트 랩")
                .professorName("테스트 교수")
                .description("테스트 설명")
                .establishedDate(LocalDate.of(2024, 1, 1))
                .representativeImage(null)
                .build();

        LabDashboardResponseDto expectedResponse = LabDashboardResponseDto.builder()
                .labInfo(labInfo)
                .notices(Collections.emptyList())
                .votes(Collections.emptyList())
                .resources(Collections.emptyList())
                .schedules(Collections.emptyList())
                .members(Collections.emptyList())
                .build();

        given(labDashboardQueryUseCase.getDashboardByLabId(labId))
                .willReturn(expectedResponse);

        // 인증된 사용자 설정
        User user = DomainUserFactory.buildValidUserWithId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        mockMvc.perform(get("/api/labs/{labId}/dashboard", labId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.labInfo").exists())
                .andExpect(jsonPath("$.data.notices").isArray())
                .andExpect(jsonPath("$.data.votes").isArray())
                .andExpect(jsonPath("$.data.resources").isArray())
                .andExpect(jsonPath("$.data.schedules").isArray())
                .andExpect(jsonPath("$.data.members").isArray());
    }
}