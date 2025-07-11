package org.univ.rankus.domain.model.interview;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SlotStatus 도메인 테스트")
class SlotStatusTest {

    @Test
    @DisplayName("모든 슬롯 상태 값이 정의되어 있음")
    void allSlotStatusValues_AreDefined() {
        // given & when
        SlotStatus[] statuses = SlotStatus.values();

        // then
        assertThat(statuses).hasSize(3);
        assertThat(statuses).contains(
                SlotStatus.AVAILABLE,
                SlotStatus.FULL,
                SlotStatus.CANCELLED
        );
    }

    @Test
    @DisplayName("슬롯 상태 순서 및 의미 검증")
    void slotStatusOrder_AndMeaning() {
        // given & when & then
        assertThat(SlotStatus.AVAILABLE.toString()).isEqualTo("AVAILABLE");
        assertThat(SlotStatus.FULL.toString()).isEqualTo("FULL");
        assertThat(SlotStatus.CANCELLED.toString()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("슬롯 상태 비교 및 동등성 검증")
    void slotStatusComparison() {
        // given & when & then
        assertThat(SlotStatus.AVAILABLE).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(SlotStatus.FULL).isEqualTo(SlotStatus.FULL);
        assertThat(SlotStatus.CANCELLED).isEqualTo(SlotStatus.CANCELLED);

        assertThat(SlotStatus.AVAILABLE).isNotEqualTo(SlotStatus.FULL);
        assertThat(SlotStatus.FULL).isNotEqualTo(SlotStatus.CANCELLED);
        assertThat(SlotStatus.AVAILABLE).isNotEqualTo(SlotStatus.CANCELLED);
    }
}