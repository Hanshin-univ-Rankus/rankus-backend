package org.univ.rankus.domain.model.vote.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VotePermissionException 테스트")
class VotePermissionExceptionTest {

    @Test
    @DisplayName("VoteErrorCode를 받아 예외 생성")
    void createWithErrorCode() {
        // given
        VoteErrorCode errorCode = VoteErrorCode.VOTE_CREATE_PERMISSION_DENIED;

        // when
        VotePermissionException exception = new VotePermissionException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("BaseCustomException을 상속받는다")
    void extendsBaseCustomException() {
        // given
        VoteErrorCode errorCode = VoteErrorCode.VOTE_CREATE_PERMISSION_DENIED;

        // when
        VotePermissionException exception = new VotePermissionException(errorCode);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}