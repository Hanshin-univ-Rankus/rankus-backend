package org.univ.rankus.adapter.in.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.common.exception.GlobalExceptionHandler;
import org.univ.rankus.application.port.in.LabPromotionUseCase;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = LabController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        properties = {
                "spring.mvc.trailing-slash.match=false",       // 트레일링 슬래시 다른 경로로 취급
                "spring.web.resources.add-mappings=false"      // 정적 리소스 매핑 완전 비활성화
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("LabController 단위 테스트 (Security 제외)")
@ActiveProfiles("test")
class LabControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LabPromotionUseCase useCase;

    @Nested
    @DisplayName("GET /api/labs (랩실 목록 조회)")
    class ListLabs {

        @Test
        @DisplayName("랩실 목록을 랭킹 내림차순으로 반환한다")
        void 랩실목록_정상조회() throws Exception {
            // given
            Lab lab1 = new Lab("Lab1", "설명1", "학과1", LabCategory.AI);
            Lab lab2 = new Lab("Lab2", "설명2", "학과2", LabCategory.DB);
            lab1.assignProfessor("Prof. Kim");
            lab2.assignProfessor("Prof. Lee");
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
                    .andExpect(jsonPath("$[1].ranking").value(5))
                    .andExpect(jsonPath("$[0].professorName").value("Prof. Kim"))
                    .andExpect(jsonPath("$[1].professorName").value("Prof. Lee"));

        }

        @Test
        @DisplayName("랩실이 없을 때 빈 리스트를 반환한다")
        void 랩실목록_빈리스트() throws Exception {
            // given
            given(useCase.listLabs()).willReturn(Collections.emptyList());

            // when & then
            mockMvc.perform(get("/api/labs")
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{id} (랩실 상세 조회)")
    class GetLabById {

        @Test
        @DisplayName("존재하는 랩실의 상세 정보를 반환한다")
        void 랩실상세_정상조회() throws Exception {
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
        @DisplayName("존재하지 않는 랩실 ID로 조회 시 500 내부 서버 오류와 표준 에러 메시지를 반환한다")
        void 랩실상세_없는랩실() throws Exception {
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

        @Test
        @DisplayName("ID 파라미터가 숫자가 아니면 400 Bad Request를 반환한다")
        void 랩실상세_잘못된ID파라미터() throws Exception {
            // when & then
            mockMvc.perform(get("/api/labs/{id}", "abc")
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("ID 파라미터가 null일 때 404 Not Found를 반환한다")
        void 랩실상세_ID누락() throws Exception {
            // when & then
            mockMvc.perform(get("/api/labs/")
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
