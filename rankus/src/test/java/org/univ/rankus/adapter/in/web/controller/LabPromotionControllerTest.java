package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.query.LabPromotionQueryUseCase;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabNotFoundException;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabPromotionController.class)
@AutoConfigureMockMvc(addFilters = false)
class LabPromotionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private LabPromotionQueryUseCase queryUseCase;

    @Nested
    @DisplayName("GET /api/labs")
    class ListLabsTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + ApiResponse<List>")
        void listSuccess() throws Exception {
            Lab lab1 = mock(Lab.class);
            Lab lab2 = mock(Lab.class);
            given(queryUseCase.listLabs()).willReturn(List.of(lab1, lab2));

            mockMvc.perform(get("/api/labs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 목록 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}")
    class GetLabTests {

        @Test
        @DisplayName("정상 조회 → 200 OK + ApiResponse body")
        void getLabSuccess() throws Exception {
            Long labId = 5L;
            Lab mockLab = mock(Lab.class);
            given(queryUseCase.getLabById(labId)).willReturn(mockLab);
            given(mockLab.getId()).willReturn(labId);
            // DTO mapping of name/category/etc. assumed correct

            mockMvc.perform(get("/api/labs/{labId}", labId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 정보 조회 성공"))
                    .andExpect(jsonPath("$.data.id").value(labId));
        }

        @Test
        @DisplayName("랩실 미존재 → 404 Not Found + LAB_006")
        void getLabNotFound() throws Exception {
            Long labId = 99L;
            given(queryUseCase.getLabById(labId))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            mockMvc.perform(get("/api/labs/{labId}", labId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"))
                    .andExpect(jsonPath("$.message").value("해당 랩실을 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("DTO 검증 실패 (음수 ID) → 400 Bad Request + GLOBAL_001")
        void getLabValidationError() throws Exception {
            mockMvc.perform(get("/api/labs/{labId}", -1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("GLOBAL_001"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."));
        }
    }
}
