package org.univ.rankus.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.univ.rankus.application.port.in.command.LabImageCommandUseCase;
import org.univ.rankus.application.port.in.query.LabImageQueryUseCase;
import org.univ.rankus.domain.model.lab.core.ImageType;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.core.LabImage;
import org.univ.rankus.domain.model.lab.exception.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class LabImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LabImageCommandUseCase commandUseCase;

    @MockitoBean
    private LabImageQueryUseCase queryUseCase;

    private static final Long LAB_ID = 10L;
    private static final Long IMAGE_ID = 99L;

    @Nested
    @DisplayName("POST /api/labs/{labId}/images")
    class CreateImage {

        @Test
        @DisplayName("정상 등록 → 201 Created + Location, Cache-Control + ApiResponse body")
        void createSuccess() throws Exception {
            Map<String, Object> req = Map.of(
                    "imageUrl", "http://img.png",
                    "type", ImageType.REPRESENTATIVE
            );
            String json = objectMapper.writeValueAsString(req);

            LabImage mockImage = mock(LabImage.class);
            Lab mockLab = mock(Lab.class);
            given(mockImage.getLab()).willReturn(mockLab);
            given(mockLab.getId()).willReturn(LAB_ID);
            given(mockImage.getId()).willReturn(IMAGE_ID);
            given(mockImage.getImageUrl()).willReturn("http://img.png");
            given(mockImage.getType()).willReturn(ImageType.REPRESENTATIVE);

            given(commandUseCase.addImage(eq(LAB_ID), eq("http://img.png"), eq(ImageType.REPRESENTATIVE)))
                    .willReturn(mockImage);

            mockMvc.perform(post("/api/labs/{labId}/images", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION,
                            "/api/labs/" + LAB_ID + "/images/" + IMAGE_ID))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("이미지 등록 성공"))
                    .andExpect(jsonPath("$.data.id").value(IMAGE_ID))
                    .andExpect(jsonPath("$.data.labId").value(LAB_ID))
                    .andExpect(jsonPath("$.data.imageUrl").value("http://img.png"))
                    .andExpect(jsonPath("$.data.type").value("REPRESENTATIVE"));
        }

        @Test
        @DisplayName("DTO 검증 실패 (빈 URL, null 타입) → 400 Bad Request + GLOBAL_001")
        void createValidationError() throws Exception {
            // Map.of()는 null 값을 허용하지 않으므로 HashMap을 사용
            Map<String, Object> req = new HashMap<>();
            req.put("imageUrl", "");
            req.put("type", null);

            String json = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/labs/{labId}/images", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("GLOBAL_001"))
                    .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors.length()").value(2));
        }

        @Test
        @DisplayName("도메인 검증 실패 (유효하지 않은 URL) → 400 Bad Request + IMG_002")
        void createDomainError() throws Exception {
            Map<String, Object> req = Map.of(
                    "imageUrl", "bad-url",
                    "type", ImageType.REPRESENTATIVE
            );
            String json = objectMapper.writeValueAsString(req);

            given(commandUseCase.addImage(eq(LAB_ID), anyString(), any()))
                    .willThrow(new LabImageValidationException(
                            LabImageErrorCode.IMAGE_URL_INVALID
                    ));

            mockMvc.perform(post("/api/labs/{labId}/images", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("IMG_002"))
                    .andExpect(jsonPath("$.message").value("이미지 URL이 유효하지 않습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("랩실이 없으면 → 404 Not Found + LAB_006")
        void createLabNotFound() throws Exception {
            Map<String, Object> req = Map.of(
                    "imageUrl", "http://img.png",
                    "type", ImageType.REPRESENTATIVE
            );
            String json = objectMapper.writeValueAsString(req);

            given(commandUseCase.addImage(eq(LAB_ID), anyString(), any()))
                    .willThrow(new LabNotFoundException(
                            LabErrorCode.LAB_NOT_FOUND
                    ));

            mockMvc.perform(post("/api/labs/{labId}/images", LAB_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"))
                    .andExpect(jsonPath("$.message").value("해당 랩실을 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/images")
    class ListImages {

        @Test
        @DisplayName("정상 조회 → 200 OK + ApiResponse<List>")
        void listSuccess() throws Exception {
            // mock 이미지와 lab 객체 생성 및 연결
            LabImage img1 = mock(LabImage.class);
            LabImage img2 = mock(LabImage.class);
            Lab mockLab = mock(Lab.class);

            // Lab ID 설정
            given(mockLab.getId()).willReturn(LAB_ID);

            // 각 이미지에 Lab 연결
            given(img1.getLab()).willReturn(mockLab);
            given(img2.getLab()).willReturn(mockLab);

            // 이미지 기타 속성 설정
            given(img1.getId()).willReturn(1L);
            given(img1.getImageUrl()).willReturn("url1.jpg");
            given(img1.getType()).willReturn(ImageType.REPRESENTATIVE);

            given(img2.getId()).willReturn(2L);
            given(img2.getImageUrl()).willReturn("url2.jpg");
            given(img2.getType()).willReturn(ImageType.ADDITIONAL);

            given(queryUseCase.listImagesByLab(LAB_ID))
                    .willReturn(List.of(img1, img2));

            mockMvc.perform(get("/api/labs/{labId}/images", LAB_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("이미지 목록 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").exists())
                    .andExpect(jsonPath("$.data[0].labId").value(LAB_ID))
                    .andExpect(jsonPath("$.data[1].labId").value(LAB_ID));
        }

        @Test
        @DisplayName("랩실 미존재 → 404 Not Found + LAB_006")
        void listLabNotFound() throws Exception {
            given(queryUseCase.listImagesByLab(LAB_ID))
                    .willThrow(new LabNotFoundException(
                            LabErrorCode.LAB_NOT_FOUND
                    ));

            mockMvc.perform(get("/api/labs/{labId}/images", LAB_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LAB_006"))
                    .andExpect(jsonPath("$.message").value("해당 랩실을 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("GET /api/labs/{labId}/images/{imageId}")
    class GetImageById {

        @Test
        @DisplayName("정상 조회 → 200 OK + ApiResponse body")
        void getSuccess() throws Exception {
            LabImage mockImage = mock(LabImage.class);
            Lab mockLab = mock(Lab.class);
            given(mockImage.getLab()).willReturn(mockLab);
            given(mockLab.getId()).willReturn(LAB_ID);
            given(mockImage.getId()).willReturn(IMAGE_ID);
            given(mockImage.getImageUrl()).willReturn("u.png");
            given(mockImage.getType()).willReturn(ImageType.ADDITIONAL);

            given(queryUseCase.getImageById(IMAGE_ID))
                    .willReturn(mockImage);

            mockMvc.perform(get("/api/labs/{labId}/images/{imageId}", LAB_ID, IMAGE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("이미지 조회 성공"))
                    .andExpect(jsonPath("$.data.id").value(IMAGE_ID))
                    .andExpect(jsonPath("$.data.type").value("ADDITIONAL"));
        }

        @Test
        @DisplayName("이미지 미존재 → 404 Not Found + IMG_008")
        void getNotFound() throws Exception {
            doThrow(new LabImageNotFoundException(
                    LabImageErrorCode.IMAGE_NOT_FOUND
            ))
                    .when(queryUseCase).getImageById(IMAGE_ID);

            mockMvc.perform(get("/api/labs/{labId}/images/{imageId}", LAB_ID, IMAGE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("IMG_008"))
                    .andExpect(jsonPath("$.message").value("해당 랩실 이미지를 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("DELETE /api/labs/{labId}/images/{imageId}")
    class DeleteImage {

        @Test
        @DisplayName("정상 삭제 → 204 No Content")
        void deleteSuccess() throws Exception {
            mockMvc.perform(delete("/api/labs/{labId}/images/{imageId}", LAB_ID, IMAGE_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("권한 없음 → 403 Forbidden + GLOBAL_004")
        void deleteForbidden() throws Exception {
            doThrow(new AccessDeniedException("no access"))
                    .when(commandUseCase).deleteImage(LAB_ID, IMAGE_ID);

            mockMvc.perform(delete("/api/labs/{labId}/images/{imageId}", LAB_ID, IMAGE_ID))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("GLOBAL_004"))
                    .andExpect(jsonPath("$.message").value("접근이 거부되었습니다."));
        }

        @Test
        @DisplayName("이미지 미존재 → 404 Not Found + IMG_008")
        void deleteNotFound() throws Exception {
            doThrow(new LabImageNotFoundException(
                    LabImageErrorCode.IMAGE_NOT_FOUND
            ))
                    .when(commandUseCase).deleteImage(LAB_ID, IMAGE_ID);

            mockMvc.perform(delete("/api/labs/{labId}/images/{imageId}", LAB_ID, IMAGE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("IMG_008"))
                    .andExpect(jsonPath("$.message").value("해당 랩실 이미지를 찾을 수 없습니다."));
        }
    }
}
