package org.univ.rankus.domain.model.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SubmissionStatus Enum 테스트")
class SubmissionStatusTest {

    @Test
    @DisplayName("모든 SubmissionStatus 값이 정의되어 있다")
    void allSubmissionStatusValues_AreDefined() {
        // given & when
        SubmissionStatus[] statuses = SubmissionStatus.values();

        // then
        assertThat(statuses).hasSize(3);
        assertThat(statuses).contains(
                SubmissionStatus.PENDING,
                SubmissionStatus.APPROVED,
                SubmissionStatus.REJECTED
        );
    }

    @ParameterizedTest
    @EnumSource(SubmissionStatus.class)
    @DisplayName("모든 SubmissionStatus는 비어있지 않은 표시 이름을 가진다")
    void allSubmissionStatuses_HaveNonEmptyDisplayName(SubmissionStatus status) {
        // when
        String displayName = status.getDisplayName();

        // then
        assertThat(displayName).isNotBlank();
    }

    @Test
    @DisplayName("표시 이름이 한국어로 되어 있다")
    void displayNames_AreInKorean() {
        // when & then
        assertThat(SubmissionStatus.PENDING.getDisplayName()).isEqualTo("심사 대기");
        assertThat(SubmissionStatus.APPROVED.getDisplayName()).isEqualTo("승인됨");
        assertThat(SubmissionStatus.REJECTED.getDisplayName()).isEqualTo("거부됨");
    }

    @Test
    @DisplayName("PENDING에서 APPROVED로 전이 가능하다")
    void pending_CanTransitionTo_Approved() {
        // when
        boolean canTransition = SubmissionStatus.PENDING.canTransitionTo(SubmissionStatus.APPROVED);

        // then
        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("PENDING에서 REJECTED로 전이 가능하다")
    void pending_CanTransitionTo_Rejected() {
        // when
        boolean canTransition = SubmissionStatus.PENDING.canTransitionTo(SubmissionStatus.REJECTED);

        // then
        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("PENDING에서 PENDING으로 전이 불가능하다")
    void pending_CannotTransitionTo_Pending() {
        // when
        boolean canTransition = SubmissionStatus.PENDING.canTransitionTo(SubmissionStatus.PENDING);

        // then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("APPROVED에서 다른 상태로 전이 불가능하다")
    void approved_CannotTransitionToAnyStatus() {
        // when & then
        assertThat(SubmissionStatus.APPROVED.canTransitionTo(SubmissionStatus.PENDING)).isFalse();
        assertThat(SubmissionStatus.APPROVED.canTransitionTo(SubmissionStatus.APPROVED)).isFalse();
        assertThat(SubmissionStatus.APPROVED.canTransitionTo(SubmissionStatus.REJECTED)).isFalse();
    }

    @Test
    @DisplayName("REJECTED에서 다른 상태로 전이 불가능하다")
    void rejected_CannotTransitionToAnyStatus() {
        // when & then
        assertThat(SubmissionStatus.REJECTED.canTransitionTo(SubmissionStatus.PENDING)).isFalse();
        assertThat(SubmissionStatus.REJECTED.canTransitionTo(SubmissionStatus.APPROVED)).isFalse();
        assertThat(SubmissionStatus.REJECTED.canTransitionTo(SubmissionStatus.REJECTED)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "PENDING, APPROVED, true",
            "PENDING, REJECTED, true",
            "PENDING, PENDING, false",
            "APPROVED, PENDING, false",
            "APPROVED, APPROVED, false",
            "APPROVED, REJECTED, false",
            "REJECTED, PENDING, false",
            "REJECTED, APPROVED, false",
            "REJECTED, REJECTED, false"
    })
    @DisplayName("상태 전이 매트릭스 검증")
    void statusTransitionMatrix_IsCorrect(SubmissionStatus fromStatus, SubmissionStatus toStatus, boolean expected) {
        // when
        boolean canTransition = fromStatus.canTransitionTo(toStatus);

        // then
        assertThat(canTransition).isEqualTo(expected);
    }

    @Test
    @DisplayName("null 상태로 전이 시도시 false를 반환한다")
    void canTransitionTo_NullStatus_ReturnsFalse() {
        // when & then
        assertThat(SubmissionStatus.PENDING.canTransitionTo(null)).isFalse();
        assertThat(SubmissionStatus.APPROVED.canTransitionTo(null)).isFalse();
        assertThat(SubmissionStatus.REJECTED.canTransitionTo(null)).isFalse();
    }

    @Test
    @DisplayName("PENDING은 초기 상태이다")
    void pending_IsInitialStatus() {
        // when
        SubmissionStatus initialStatus = SubmissionStatus.PENDING;

        // then
        assertThat(initialStatus.getDisplayName()).isEqualTo("심사 대기");
        assertThat(initialStatus.canTransitionTo(SubmissionStatus.APPROVED)).isTrue();
        assertThat(initialStatus.canTransitionTo(SubmissionStatus.REJECTED)).isTrue();
    }

    @Test
    @DisplayName("APPROVED와 REJECTED는 최종 상태이다")
    void approvedAndRejected_AreFinalStatuses() {
        // when
        SubmissionStatus approvedStatus = SubmissionStatus.APPROVED;
        SubmissionStatus rejectedStatus = SubmissionStatus.REJECTED;

        // then
        // APPROVED는 최종 상태 (다른 상태로 전이 불가)
        assertThat(approvedStatus.canTransitionTo(SubmissionStatus.PENDING)).isFalse();
        assertThat(approvedStatus.canTransitionTo(SubmissionStatus.APPROVED)).isFalse();
        assertThat(approvedStatus.canTransitionTo(SubmissionStatus.REJECTED)).isFalse();

        // REJECTED도 최종 상태 (다른 상태로 전이 불가)
        assertThat(rejectedStatus.canTransitionTo(SubmissionStatus.PENDING)).isFalse();
        assertThat(rejectedStatus.canTransitionTo(SubmissionStatus.APPROVED)).isFalse();
        assertThat(rejectedStatus.canTransitionTo(SubmissionStatus.REJECTED)).isFalse();
    }

    @Test
    @DisplayName("상태 전이 로직이 비즈니스 규칙과 일치한다")
    void statusTransitionLogic_MatchesBusinessRules() {
        // given
        SubmissionStatus pendingStatus = SubmissionStatus.PENDING;

        // when & then
        // 비즈니스 규칙: PENDING 상태에서만 승인/거부 가능
        assertThat(pendingStatus.canTransitionTo(SubmissionStatus.APPROVED))
                .as("PENDING 상태에서 APPROVED로 전이 가능해야 함")
                .isTrue();

        assertThat(pendingStatus.canTransitionTo(SubmissionStatus.REJECTED))
                .as("PENDING 상태에서 REJECTED로 전이 가능해야 함")
                .isTrue();

        // 비즈니스 규칙: 승인/거부 후에는 상태 변경 불가 (정정 제외)
        assertThat(SubmissionStatus.APPROVED.canTransitionTo(SubmissionStatus.REJECTED))
                .as("APPROVED 상태에서 REJECTED로 전이 불가해야 함")
                .isFalse();

        assertThat(SubmissionStatus.REJECTED.canTransitionTo(SubmissionStatus.APPROVED))
                .as("REJECTED 상태에서 APPROVED로 전이 불가해야 함")
                .isFalse();
    }

    @Test
    @DisplayName("상태 전이 검증 메서드가 대칭적이지 않다")
    void statusTransition_IsNotSymmetric() {
        // when & then
        // PENDING -> APPROVED는 가능하지만 APPROVED -> PENDING은 불가
        assertThat(SubmissionStatus.PENDING.canTransitionTo(SubmissionStatus.APPROVED)).isTrue();
        assertThat(SubmissionStatus.APPROVED.canTransitionTo(SubmissionStatus.PENDING)).isFalse();

        // PENDING -> REJECTED는 가능하지만 REJECTED -> PENDING은 불가
        assertThat(SubmissionStatus.PENDING.canTransitionTo(SubmissionStatus.REJECTED)).isTrue();
        assertThat(SubmissionStatus.REJECTED.canTransitionTo(SubmissionStatus.PENDING)).isFalse();
    }

    @Test
    @DisplayName("상태 전이 검증 메서드가 재귀적이지 않다")
    void statusTransition_IsNotReflexive() {
        // when & then
        // 모든 상태에서 자기 자신으로의 전이는 불가
        assertThat(SubmissionStatus.PENDING.canTransitionTo(SubmissionStatus.PENDING)).isFalse();
        assertThat(SubmissionStatus.APPROVED.canTransitionTo(SubmissionStatus.APPROVED)).isFalse();
        assertThat(SubmissionStatus.REJECTED.canTransitionTo(SubmissionStatus.REJECTED)).isFalse();
    }

    @Test
    @DisplayName("상태 전이가 일방향성을 가진다")
    void statusTransition_IsUnidirectional() {
        // when & then
        // PENDING -> APPROVED/REJECTED는 가능하지만 역방향은 불가
        assertThat(SubmissionStatus.PENDING.canTransitionTo(SubmissionStatus.APPROVED)).isTrue();
        assertThat(SubmissionStatus.APPROVED.canTransitionTo(SubmissionStatus.PENDING)).isFalse();

        assertThat(SubmissionStatus.PENDING.canTransitionTo(SubmissionStatus.REJECTED)).isTrue();
        assertThat(SubmissionStatus.REJECTED.canTransitionTo(SubmissionStatus.PENDING)).isFalse();

        // APPROVED <-> REJECTED 간의 직접 전이는 불가
        assertThat(SubmissionStatus.APPROVED.canTransitionTo(SubmissionStatus.REJECTED)).isFalse();
        assertThat(SubmissionStatus.REJECTED.canTransitionTo(SubmissionStatus.APPROVED)).isFalse();
    }

    @Test
    @DisplayName("상태 순서가 의미상 올바르다")
    void statusOrder_IsMeaningful() {
        // when
        SubmissionStatus[] statuses = SubmissionStatus.values();

        // then
        assertThat(statuses[0]).isEqualTo(SubmissionStatus.PENDING);   // 첫 번째: 대기
        assertThat(statuses[1]).isEqualTo(SubmissionStatus.APPROVED); // 두 번째: 승인
        assertThat(statuses[2]).isEqualTo(SubmissionStatus.REJECTED); // 세 번째: 거부
    }

    @Test
    @DisplayName("enum 이름과 표시 이름이 일관성을 가진다")
    void enumNames_AreConsistentWithDisplayNames() {
        // when & then
        assertThat(SubmissionStatus.PENDING.name()).isEqualTo("PENDING");
        assertThat(SubmissionStatus.PENDING.getDisplayName()).contains("대기");

        assertThat(SubmissionStatus.APPROVED.name()).isEqualTo("APPROVED");
        assertThat(SubmissionStatus.APPROVED.getDisplayName()).contains("승인");

        assertThat(SubmissionStatus.REJECTED.name()).isEqualTo("REJECTED");
        assertThat(SubmissionStatus.REJECTED.getDisplayName()).contains("거부");
    }
}