package org.univ.rankus.domain.model.interview;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InterviewStatus 도메인 테스트")
class InterviewStatusTest {

    @Test
    @DisplayName("모든 면접 상태 값이 정의되어 있음")
    void allInterviewStatusValues_AreDefined() {
        // given & when
        InterviewStatus[] statuses = InterviewStatus.values();

        // then
        assertThat(statuses).hasSize(3);
        assertThat(statuses).contains(
                InterviewStatus.INACTIVE,
                InterviewStatus.ACTIVE,
                InterviewStatus.CLOSED
        );
    }

    @Test
    @DisplayName("면접 상태 순서 및 의미 검증")
    void interviewStatusOrder_AndMeaning() {
        // given & when & then
        assertThat(InterviewStatus.INACTIVE.toString()).isEqualTo("INACTIVE");
        assertThat(InterviewStatus.ACTIVE.toString()).isEqualTo("ACTIVE");
        assertThat(InterviewStatus.CLOSED.toString()).isEqualTo("CLOSED");
    }

    @Test
    @DisplayName("면접 상태 비교 및 동등성 검증")
    void interviewStatusComparison() {
        // given & when & then
        assertThat(InterviewStatus.INACTIVE).isEqualTo(InterviewStatus.INACTIVE);
        assertThat(InterviewStatus.ACTIVE).isEqualTo(InterviewStatus.ACTIVE);
        assertThat(InterviewStatus.CLOSED).isEqualTo(InterviewStatus.CLOSED);

        assertThat(InterviewStatus.INACTIVE).isNotEqualTo(InterviewStatus.ACTIVE);
        assertThat(InterviewStatus.ACTIVE).isNotEqualTo(InterviewStatus.CLOSED);
        assertThat(InterviewStatus.INACTIVE).isNotEqualTo(InterviewStatus.CLOSED);
    }
}