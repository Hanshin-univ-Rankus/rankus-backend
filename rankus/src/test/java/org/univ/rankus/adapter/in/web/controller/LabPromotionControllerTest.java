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

import java.time.LocalDateTime;
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
            // given: 2개의 랩실 mock 객체가 반환되도록 설정
            Lab lab1 = mock(Lab.class);
            Lab lab2 = mock(Lab.class);
            given(queryUseCase.listLabs()).willReturn(List.of(lab1, lab2));
            given(lab1.getCreatedAt()).willReturn(LocalDateTime.now());
            given(lab2.getCreatedAt()).willReturn(LocalDateTime.now());

            // when: /api/labs 엔드포인트로 GET 요청을 보냄
            // then: 200 OK, status=200, message, data 배열 길이 2 검증
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
            // given: 특정 labId에 대해 mockLab 객체가 반환되도록 설정

            Long labId = 5L;
            Lab mockLab = mock(Lab.class);
            given(queryUseCase.getLabById(labId)).willReturn(mockLab);
            given(mockLab.getId()).willReturn(labId);
            given(mockLab.getCreatedAt()).willReturn(LocalDateTime.now());
            // DTO mapping of name/category/etc. assumed correct

            // when: /api/labs/{labId} 엔드포인트로 GET 요청을 보냄
            // then: 200 OK, status=200, message, data.id=labId 검증
            mockMvc.perform(get("/api/labs/{labId}", labId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("랩실 정보 조회 성공"))
                    .andExpect(jsonPath("$.data.id").value(labId));
        }

        @Test
        @DisplayName("랩실 미존재 → 404 Not Found + LAB_006")
        void getLabNotFound() throws Exception {
            // given: 존재하지 않는 labId에 대해 예외가 발생하도록 설정
            Long labId = 99L;
            given(queryUseCase.getLabById(labId))
                    .willThrow(new LabNotFoundException(LabErrorCode.LAB_NOT_FOUND));

            // when: /api/labs/{labId} 엔드포인트로 GET 요청을 보냄
            // then: 404 Not Found, code=LAB_006, message 검증
            mockMvc.perform(get("/api/labs/{labId}", labId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"))
                    .andExpect(jsonPath("$.message").value("해당 랩실을 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("DTO 검증 실패 (음수 ID) → 400 Bad Request + GLOBAL_001")
        void getLabValidationError() throws Exception {
            // given: 음수 labId 사용

            // when: /api/labs/{labId} 엔드포인트로 GET 요청을 보냄
            // then: 400 Bad Request, code=GLOBAL_001, message 검증
            mockMvc.perform(get("/api/labs/{labId}", -1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("GLOBAL_001"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."));
        }
    }
}