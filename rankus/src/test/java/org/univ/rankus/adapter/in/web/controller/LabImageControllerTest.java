package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.common.exception.GlobalExceptionHandler;
import org.univ.rankus.application.port.in.LabImageUseCase;
import org.univ.rankus.domain.model.lab.ImageType;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.lab.LabImage;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SecurityAutoConfiguration을 제외하고, 컨트롤러만 테스트합니다.
 * LabImageUseCase는 @MockitoBean으로 모킹합니다.
 */
@WebMvcTest(
        controllers = LabImageController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("LabImageController 슬라이스 테스트 (@MockitoBean)")
class LabImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LabImageUseCase imageUseCase;

    @Test
    @DisplayName("POST /api/labs/{labId}/images 요청 시 올바른 DTO가 반환된다")
    void registerImage_success() throws Exception {
        // given
        Long labId = 1L;
        String url = "http://example.com/img.png";
        ImageType type = ImageType.REPRESENTATIVE;

        Lab lab = new Lab("AI Lab", "desc", "CS", LabCategory.AI);
        LabImage savedImage = new LabImage(lab, url, type);

        given(imageUseCase.registerImage(labId, url, type))
                .willReturn(savedImage);

        // 요청 JSON 생성
        String body = objectMapper.writeValueAsString(
                Map.of("imageUrl", url, "type", type.name())
        );

        // when & then
        mockMvc.perform(post("/api/labs/{labId}/images", labId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.imageUrl").value(url))
                .andExpect(jsonPath("$.type").value(type.name()));
    }

    @Test
    @DisplayName("GET /api/labs/{labId}/images 요청 시 등록된 모든 이미지를 반환한다")
    void listImages_success() throws Exception {
        // given
        Long labId = 1L;
        Lab lab = new Lab("AI Lab", "desc", "CS", LabCategory.AI);
        LabImage img1 = new LabImage(lab, "u1", ImageType.REPRESENTATIVE);
        LabImage img2 = new LabImage(lab, "u2", ImageType.ADDITIONAL);

        given(imageUseCase.listImages(labId))
                .willReturn(Arrays.asList(img1, img2));

        // when & then
        mockMvc.perform(get("/api/labs/{labId}/images", labId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].imageUrl").value("u1"))
                .andExpect(jsonPath("$[0].type").value(ImageType.REPRESENTATIVE.name()))
                .andExpect(jsonPath("$[1].imageUrl").value("u2"))
                .andExpect(jsonPath("$[1].type").value(ImageType.ADDITIONAL.name()));
    }

    @Test
    @DisplayName("GET /api/labs/{labId}/images 요청 시 없는 랩실이면 500 오류를 반환한다")
    void listImages_notFound() throws Exception {
        // given
        Long labId = 99L;
        given(imageUseCase.listImages(labId))
                .willThrow(new NoSuchElementException("랩실을 찾을 수 없습니다: " + labId));

        // when & then
        mockMvc.perform(get("/api/labs/{labId}/images", labId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("내부 서버 오류가 발생했습니다."));
    }
}
