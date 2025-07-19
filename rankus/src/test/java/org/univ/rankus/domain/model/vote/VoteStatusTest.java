package org.univ.rankus.domain.model.vote;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VoteStatus Enum 단위 테스트")
class VoteStatusTest {

    @Test
    @DisplayName("ACTIVE 상태는 isActive()가 true를 반환")
    void active_isActive_returnsTrue() {
        assertTrue(VoteStatus.ACTIVE.isActive());
    }

    @Test
    @DisplayName("CLOSED 상태는 isClosed()가 true를 반환")
    void closed_isClosed_returnsTrue() {
        assertTrue(VoteStatus.CLOSED.isClosed());
    }

    @Test
    @DisplayName("CANCELED 상태는 isCanceled()가 true를 반환")
    void canceled_isCanceled_returnsTrue() {
        assertTrue(VoteStatus.CANCELED.isCanceled());
    }

    @Test
    @DisplayName("ACTIVE에서 CLOSED로 전환 가능")
    void active_canTransitionTo_closed_returnsTrue() {
        assertTrue(VoteStatus.ACTIVE.canTransitionTo(VoteStatus.CLOSED));
    }

    @Test
    @DisplayName("ACTIVE에서 CANCELED로 전환 가능")
    void active_canTransitionTo_canceled_returnsTrue() {
        assertTrue(VoteStatus.ACTIVE.canTransitionTo(VoteStatus.CANCELED));
    }

    @ParameterizedTest
    @EnumSource(value = VoteStatus.class, names = {"CLOSED", "CANCELED"})
    @DisplayName("CLOSED, CANCELED 상태에서는 다른 상태로 전환 불가")
    void finalStates_cannotTransition(VoteStatus finalStatus) {
        assertFalse(finalStatus.canTransitionTo(VoteStatus.ACTIVE));
        assertFalse(finalStatus.canTransitionTo(VoteStatus.CLOSED));
        assertFalse(finalStatus.canTransitionTo(VoteStatus.CANCELED));
    }

    @Test
    @DisplayName("모든 상태의 description이 존재")
    void allStatuses_haveDescription() {
        for (VoteStatus status : VoteStatus.values()) {
            assertNotNull(status.getDescription());
            assertFalse(status.getDescription().isEmpty());
        }
    }
}