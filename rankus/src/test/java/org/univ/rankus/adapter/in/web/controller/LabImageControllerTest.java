package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.common.exception.GlobalExceptionHandler;
import org.univ.rankus.application.port.in.LabImageUseCase;
import org.univ.rankus.domain.model.lab.ImageType;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.lab.LabImage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = LabImageController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("LabImageController 슬라이스 테스트 (@MockitoBean)")
@ActiveProfiles("test")
class LabImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LabImageUseCase imageUseCase;

    @Nested
    @DisplayName("POST /api/labs/{labId}/images (랩실 이미지 등록)")
    class RegisterImage {

        @Test
        @DisplayName("정상 요청 시 LabImage DTO를 반환한다")
        void 정상등록() throws Exception {
            // given
            Long labId = 1L;
            String url = "http://example.com/img.png";
            ImageType type = ImageType.REPRESENTATIVE;

            Lab lab = new Lab("AI Lab", "desc", "CS", LabCategory.AI);
            LabImage savedImage = new LabImage(lab, url, type);

            given(imageUseCase.registerImage(labId, url, type))
                    .willReturn(savedImage);

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
        @DisplayName("잘못된 타입 값으로 요청 시 400 Bad Request를 반환한다")
        void 타입_잘못된값() throws Exception {
            // given
            Long labId = 1L;
            String url = "http://example.com/img.png";
            String invalidType = "NOT_VALID_TYPE";
            String body = objectMapper.writeValueAsString(
                    Map.of("imageUrl", url, "type", invalidType)
            );

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/images", labId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("imageUrl 누락 시 400 Bad Request를 반환한다")
        void imageUrl_누락() throws Exception {
            // given
            Long labId = 1L;
            String body = objectMapper.writeValueAsString(
                    Map.of("type", ImageType.REPRESENTATIVE.name())
            );

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/images", labId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("labId가 존재하지 않을 경우 500 에러 반환")
        void 랩실없음_등록실패() throws Exception {
            // given
            Long labId = 999L;
            String url = "http://example.com/img.png";
            ImageType type = ImageType.REPRESENTATIVE;

            given(imageUseCase.registerImage(labId, url, type))
                    .willThrow(new NoSuchElementException("랩실을 찾을 수 없습니다: " + labId));

            String body = objectMapper.writeValueAsString(
                    Map.of("imageUrl", url, "type", type.name())
            );

            // when & then
            mockMvc.perform(post("/api/labs/{labId}/images", labId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("내부 서버 오류가 발생했습니다."));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/images (랩실 이미지 목록)")
    class ListImages {

        @Test
        @DisplayName("정상적으로 이미지 리스트를 반환한다")
        void 정상조회() throws Exception {
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
        @DisplayName("이미지가 없을 때 빈 배열을 반환한다")
        void 이미지없음() throws Exception {
            // given
            Long labId = 1L;
            given(imageUseCase.listImages(labId))
                    .willReturn(Collections.emptyList());

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/images", labId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("없는 랩실 ID로 조회 시 500 에러 반환")
        void 없는랩실() throws Exception {
            // given
            Long labId = 99L;
            given(imageUseCase.listImages(labId))
                    .willThrow(new NoSuchElementException("랩실을 찾을 수 없습니다: " + labId));

            // when & then
            mockMvc.perform(get("/api/labs/{labId}/images", labId))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("내부 서버 오류가 발생했습니다."));
        }

        @Test
        @DisplayName("labId 파라미터가 숫자가 아니면 400 Bad Request를 반환한다")
        void 잘못된labId() throws Exception {
            // when & then
            mockMvc.perform(get("/api/labs/{labId}/images", "not_number"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("labId 파라미터가 누락될 경우 404 Not Found를 반환한다")
        void labId_누락() throws Exception {
            // when & then
            mockMvc.perform(get("/api/labs//images"))
                    .andExpect(status().isNotFound());
        }
    }
}
