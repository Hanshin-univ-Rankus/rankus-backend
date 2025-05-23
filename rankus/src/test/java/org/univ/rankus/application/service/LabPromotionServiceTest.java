package org.univ.rankus.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabCategory;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LabPromotionService 단위 테스트")
class LabPromotionServiceTest {

    @Mock
    private SpringDataLabRepository repository;

    @InjectMocks
    private LabPromotionService service;

    @Test
    @DisplayName("저장된 랩실이 있을 때 listLabs 호출 시 랭킹 내림차순으로 반환된다")
    void listLabs_success() {
        // given
        Lab lowRankLab  = new Lab("Low",  "desc", "dep", LabCategory.DB);
        Lab highRankLab = new Lab("High", "desc", "dep", LabCategory.AI);
        // 랭킹 값 세팅
        highRankLab.updateRanking(10);
        lowRankLab.updateRanking(1);
        List<Lab> sorted = Arrays.asList(highRankLab, lowRankLab);
        when(repository.findAll(Sort.by(Sort.Direction.DESC, "ranking")))
                .thenReturn(sorted);

        // when
        List<Lab> result = service.listLabs();

        // then
        assertEquals(2, result.size(), "listLabs 결과 크기가 일치해야 한다");
        assertEquals(highRankLab, result.get(0), "첫 번째 요소는 랭킹이 높은 랩실이어야 한다");
        assertEquals(lowRankLab, result.get(1),  "두 번째 요소는 랭킹이 낮은 랩실이어야 한다");
        verify(repository).findAll(Sort.by(Sort.Direction.DESC, "ranking"));
    }

    @Test
    @DisplayName("존재하는 ID로 getLabById 호출 시 해당 Lab이 반환된다")
    void getLabById_success() {
        // given
        Long id = 1L;
        Lab lab = new Lab("Test", "desc", "dep", LabCategory.ETC);
        when(repository.findById(id)).thenReturn(Optional.of(lab));

        // when
        Lab result = service.getLabById(id);

        // then
        assertEquals(lab, result, "getLabById 결과가 저장된 랩실과 같아야 한다");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 getLabById 호출 시 NoSuchElementException이 발생한다")
    void getLabById_notFound_throws() {
        // given
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // when & then
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> service.getLabById(id),
                "ID가 없으면 NoSuchElementException이 발생해야 한다");
        assertTrue(ex.getMessage().contains(String.valueOf(id)),
                "예외 메시지에 조회한 ID가 포함되어야 한다");
    }
}
