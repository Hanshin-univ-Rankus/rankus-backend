package org.univ.rankus.adapter.in.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.common.exception.GlobalExceptionHandler;
import org.univ.rankus.application.port.in.LabPromotionUseCase;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SecurityAutoConfiguration을 제외하고 컨트롤러만 슬라이스 테스트.
 */
@WebMvcTest(
        controllers = LabController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("LabController 단위 테스트 (Security 제외)")
class LabControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LabPromotionUseCase useCase;

    @Test
    @DisplayName("GET /api/labs — 랩실 목록을 랭킹 내림차순으로 반환한다.")
    void listLabs_success() throws Exception {
        // given
        Lab lab1 = new Lab("Lab1", "설명1", "학과1", LabCategory.AI);
        Lab lab2 = new Lab("Lab2", "설명2", "학과2", LabCategory.DB);
        lab1.updateRanking(10);
        lab2.updateRanking(5);
        given(useCase.listLabs()).willReturn(Arrays.asList(lab1, lab2));

        // when & then
        mockMvc.perform(get("/api/labs")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Lab1"))
                .andExpect(jsonPath("$[0].ranking").value(10))
                .andExpect(jsonPath("$[1].name").value("Lab2"))
                .andExpect(jsonPath("$[1].ranking").value(5));
    }

    @Test
    @DisplayName("GET /api/labs/{id} — 존재하는 랩실 상세 조회 시 JSON 반환")
    void getLabById_success() throws Exception {
        // given
        Long id = 1L;
        Lab lab = new Lab("LabDetail", "상세설명", "학과", LabCategory.AI);
        lab.updateRanking(7);
        given(useCase.getLabById(id)).willReturn(lab);

        // when & then
        mockMvc.perform(get("/api/labs/{id}", id)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("LabDetail"))
                .andExpect(jsonPath("$.ranking").value(7));
    }

    @Test
    @DisplayName("GET /api/labs/{id} — 없는 랩실 조회 시 500 내부 서버 오류 반환")
    void getLabById_notFound() throws Exception {
        // given
        Long id = 999L;
        given(useCase.getLabById(id))
                .willThrow(new NoSuchElementException("해당 ID의 랩실을 찾을 수 없습니다: " + id));

        // when & then
        mockMvc.perform(get("/api/labs/{id}", id)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("내부 서버 오류가 발생했습니다."));
    }
}
