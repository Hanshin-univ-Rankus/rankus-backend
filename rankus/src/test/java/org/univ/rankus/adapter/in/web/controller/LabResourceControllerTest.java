package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.adapter.in.web.dto.response.LabResourceResponseDto;
import org.univ.rankus.application.port.in.command.LabResourceCommandUseCase;
import org.univ.rankus.application.port.in.query.LabResourceQueryUseCase;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.resource.ResourceCategory;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LabResourceController 테스트
 */
@WebMvcTest(LabResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LabResourceController 테스트")
class LabResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LabResourceQueryUseCase labResourceQueryUseCase;

    @MockitoBean
    private LabResourceCommandUseCase labResourceCommandUseCase;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 테스트용 SecurityContext 설정 헬퍼 메서드
     *
     * @param userId 사용자 ID
     */
    private void setupSecurityContext(Long userId) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        given(principal.getUserId()).willReturn(userId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        authentication.setAuthenticated(true); // 중요: 인증 상태를 true로 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("랩실 자료 목록을 조회할 수 있다")
    void 랩실_자료_목록을_조회할_수_있다() throws Exception {
        // given
        setupSecurityContext(1L);
        Long labId = 1L;
        List<LabResourceResponseDto> resources = List.of(createTestResourceResponseDto());
        Page<LabResourceResponseDto> resourcePage = new PageImpl<>(resources);

        when(labResourceQueryUseCase.getLabResources(eq(labId), any(), any(), any(Pageable.class), any()))
                .thenReturn(resourcePage);

        // when & then
        mockMvc.perform(get("/api/labs/{labId}/resources", labId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("자료 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("테스트 자료"))
                .andExpect(jsonPath("$.data.content[0].category").value("LECTURE_NOTE"));
    }

    @Test
    @DisplayName("특정 랩실 자료를 조회할 수 있다")
    void 특정_랩실_자료를_조회할_수_있다() throws Exception {
        // given
        setupSecurityContext(1L);
        Long labId = 1L;
        Long resourceId = 1L;
        LabResourceResponseDto resource = createTestResourceResponseDto();

        when(labResourceQueryUseCase.getLabResource(eq(resourceId), any()))
                .thenReturn(resource);

        // when & then
        mockMvc.perform(get("/api/labs/{labId}/resources/{resourceId}", labId, resourceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("자료 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("테스트 자료"))
                .andExpect(jsonPath("$.data.category").value("LECTURE_NOTE"));
    }

    @Test
    @DisplayName("자료 카테고리 목록을 조회할 수 있다")
    void 자료_카테고리_목록을_조회할_수_있다() throws Exception {
        // given
        setupSecurityContext(1L);
        Long labId = 1L;
        List<ResourceCategory> categories = List.of(ResourceCategory.values());

        when(labResourceQueryUseCase.getResourceCategories())
                .thenReturn(categories);

        // when & then
        mockMvc.perform(get("/api/labs/{labId}/resources/categories", labId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("카테고리 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(ResourceCategory.values().length));
    }

    @Test
    @DisplayName("랩실 자료 통계를 조회할 수 있다")
    void 랩실_자료_통계를_조회할_수_있다() throws Exception {
        // given
        setupSecurityContext(1L);
        Long labId = 1L;
        var stats = new LabResourceQueryUseCase.LabResourceStatsDto(
                10L, 8L, 2L,
                java.util.Map.of(ResourceCategory.LECTURE_NOTE, 5L, ResourceCategory.RESEARCH, 3L)
        );

        when(labResourceQueryUseCase.getLabResourceStats(eq(labId), any()))
                .thenReturn(stats);

        // when & then
        mockMvc.perform(get("/api/labs/{labId}/resources/stats", labId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("자료 통계 조회 성공"))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.publicCount").value(8))
                .andExpect(jsonPath("$.data.privateCount").value(2));
    }

    private LabResourceResponseDto createTestResourceResponseDto() {
        return LabResourceResponseDto.builder()
                .id(1L)
                .title("테스트 자료")
                .description("테스트 설명")
                .fileName("test.pdf")
                .fileSize(1024L)
                .fileSizeInMB(0.001)
                .fileExtension("pdf")
                .category(ResourceCategory.LECTURE_NOTE)
                .categoryDisplayName("강의자료")
                .isPublic(true)
                .downloadCount(0)
                .labId(1L)
                .labName("테스트 랩")
                .uploaderId(1L)
                .uploaderName("김철수")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}