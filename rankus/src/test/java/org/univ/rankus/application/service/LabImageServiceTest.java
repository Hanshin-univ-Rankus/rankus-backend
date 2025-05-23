package org.univ.rankus.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.adapter.out.persistence.SpringDataLabImageRepository;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.lab.ImageType;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("LabImageService 단위 테스트")
class LabImageServiceTest {

    @Mock
    private SpringDataLabRepository labRepo;

    @Mock
    private SpringDataLabImageRepository imageRepo;

    @InjectMocks
    private LabImageService service;

    @Test
    @DisplayName("given: 유효한 랩실 ID와 이미지 정보 when: registerImage 호출 then: LabImage가 저장되고 반환된다")
    void registerImage_success() {
        // given
        Long labId = 1L;
        Lab lab = new Lab("AI Lab", "설명", "컴퓨터공학과", LabCategory.AI);
        given(labRepo.findById(labId)).willReturn(Optional.of(lab));

        String url = "http://example.com/img.png";
        ImageType type = ImageType.REPRESENTATIVE;

        // any(LabImage.class)로 stub
        given(imageRepo.save(any(LabImage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        LabImage result = service.registerImage(labId, url, type);

        // then
        assertAll("등록된 LabImage 검증",
                () -> assertEquals(lab, result.getLab(),      "Lab 연관관계가 설정되어야 한다"),
                () -> assertEquals(url, result.getImageUrl(), "URL이 설정되어야 한다"),
                () -> assertEquals(type, result.getType(),    "ImageType이 설정되어야 한다")
        );
    }

    @Test
    @DisplayName("given: 존재하지 않는 랩실 ID when: registerImage 호출 then: NoSuchElementException 발생")
    void registerImage_labNotFound_throws() {
        // given
        Long labId = 99L;
        given(labRepo.findById(labId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () ->
                        service.registerImage(labId, "url", ImageType.ADDITIONAL),
                "존재하지 않는 랩실 ID인 경우 예외가 발생해야 한다"
        );
    }

    @Test
    @DisplayName("given: 랩실 ID when: listImages 호출 then: 해당 랩실의 모든 이미지 목록이 반환된다")
    void listImages_success() {
        // given
        Long labId = 1L;
        Lab lab = new Lab("TestLab", "설명", "컴퓨터공학과", LabCategory.DB);
        LabImage img1 = new LabImage(lab, "url1", ImageType.REPRESENTATIVE);
        LabImage img2 = new LabImage(lab, "url2", ImageType.ADDITIONAL);
        List<LabImage> images = Arrays.asList(img1, img2);

        given(imageRepo.findByLabId(labId)).willReturn(images);

        // when
        List<LabImage> result = service.listImages(labId);

        // then
        assertEquals(2, result.size(), "저장된 이미지 수만큼 반환되어야 한다");
        assertTrue(result.containsAll(images), "이미지 목록이 정확히 반환되어야 한다");
    }
}
