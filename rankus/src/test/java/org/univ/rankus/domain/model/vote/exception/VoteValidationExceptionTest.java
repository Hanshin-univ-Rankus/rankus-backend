package org.univ.rankus.domain.model.vote.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VoteValidationException 테스트")
class VoteValidationExceptionTest {

    @Test
    @DisplayName("VoteErrorCode를 받아 예외 생성")
    void createWithErrorCode() {
        // given
        VoteErrorCode errorCode = VoteErrorCode.VOTE_ALREADY_CLOSED;

        // when
        VoteValidationException exception = new VoteValidationException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("BaseCustomException을 상속받는다")
    void extendsBaseCustomException() {
        // given
        VoteErrorCode errorCode = VoteErrorCode.VOTE_ALREADY_CLOSED;

        // when
        VoteValidationException exception = new VoteValidationException(errorCode);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("다양한 에러 코드로 예외 생성 가능")
    void createWithDifferentErrorCodes() {
        // given & when & then
        VoteValidationException exception1 = new VoteValidationException(VoteErrorCode.VOTE_ALREADY_CLOSED);
        assertThat(exception1.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_ALREADY_CLOSED);

        VoteValidationException exception2 = new VoteValidationException(VoteErrorCode.VOTE_ALREADY_PARTICIPATED);
        assertThat(exception2.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_ALREADY_PARTICIPATED);

        VoteValidationException exception3 = new VoteValidationException(VoteErrorCode.VOTE_OPTION_NOT_FOUND);
        assertThat(exception3.getErrorCode()).isEqualTo(VoteErrorCode.VOTE_OPTION_NOT_FOUND);
    }
}