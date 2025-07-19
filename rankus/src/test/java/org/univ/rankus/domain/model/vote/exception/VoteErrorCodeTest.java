package org.univ.rankus.domain.model.vote.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VoteErrorCode 테스트")
class VoteErrorCodeTest {

    @Test
    @DisplayName("BAD_REQUEST 에러 코드들의 속성 확인")
    void badRequestErrorCodes() {
        // when & then
        assertThat(VoteErrorCode.VOTE_TITLE_REQUIRED.getCode()).isEqualTo("VOTE_001");
        assertThat(VoteErrorCode.VOTE_TITLE_REQUIRED.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(VoteErrorCode.VOTE_TITLE_REQUIRED.getMessage()).isEqualTo("투표 제목은 필수입니다.");

        assertThat(VoteErrorCode.VOTE_DEADLINE_PAST.getCode()).isEqualTo("VOTE_005");
        assertThat(VoteErrorCode.VOTE_DEADLINE_PAST.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(VoteErrorCode.VOTE_DEADLINE_PAST.getMessage()).isEqualTo("투표 마감일은 현재 시간보다 미래여야 합니다.");
    }

    @Test
    @DisplayName("FORBIDDEN 에러 코드들의 속성 확인")
    void forbiddenErrorCodes() {
        // when & then
        assertThat(VoteErrorCode.VOTE_CREATE_PERMISSION_DENIED.getCode()).isEqualTo("VOTE_011");
        assertThat(VoteErrorCode.VOTE_CREATE_PERMISSION_DENIED.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(VoteErrorCode.VOTE_CREATE_PERMISSION_DENIED.getMessage()).isEqualTo("투표 생성 권한이 없습니다.");

        assertThat(VoteErrorCode.VOTE_PARTICIPATION_PERMISSION_DENIED.getCode()).isEqualTo("VOTE_014");
        assertThat(VoteErrorCode.VOTE_PARTICIPATION_PERMISSION_DENIED.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(VoteErrorCode.VOTE_PARTICIPATION_PERMISSION_DENIED.getMessage()).isEqualTo("투표 참여 권한이 없습니다.");
    }

    @Test
    @DisplayName("NOT_FOUND 에러 코드들의 속성 확인")
    void notFoundErrorCodes() {
        // when & then
        assertThat(VoteErrorCode.VOTE_NOT_FOUND.getCode()).isEqualTo("VOTE_015");
        assertThat(VoteErrorCode.VOTE_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(VoteErrorCode.VOTE_NOT_FOUND.getMessage()).isEqualTo("해당 투표를 찾을 수 없습니다.");

        assertThat(VoteErrorCode.VOTE_OPTION_NOT_FOUND.getCode()).isEqualTo("VOTE_016");
        assertThat(VoteErrorCode.VOTE_OPTION_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(VoteErrorCode.VOTE_OPTION_NOT_FOUND.getMessage()).isEqualTo("해당 투표 선택지를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("CONFLICT 에러 코드들의 속성 확인")
    void conflictErrorCodes() {
        // when & then
        assertThat(VoteErrorCode.VOTE_ALREADY_CLOSED.getCode()).isEqualTo("VOTE_017");
        assertThat(VoteErrorCode.VOTE_ALREADY_CLOSED.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(VoteErrorCode.VOTE_ALREADY_CLOSED.getMessage()).isEqualTo("이미 종료된 투표입니다.");

        assertThat(VoteErrorCode.VOTE_ALREADY_PARTICIPATED.getCode()).isEqualTo("VOTE_019");
        assertThat(VoteErrorCode.VOTE_ALREADY_PARTICIPATED.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(VoteErrorCode.VOTE_ALREADY_PARTICIPATED.getMessage()).isEqualTo("이미 참여한 투표입니다.");
    }

    @Test
    @DisplayName("모든 에러 코드가 유일한 코드값을 가진다")
    void allErrorCodesHaveUniqueValues() {
        // given
        VoteErrorCode[] allCodes = VoteErrorCode.values();

        // when & then
        long uniqueCodeCount = java.util.Arrays.stream(allCodes)
                .map(VoteErrorCode::getCode)
                .distinct()
                .count();

        assertThat(uniqueCodeCount).isEqualTo(allCodes.length);
    }

    @Test
    @DisplayName("모든 에러 코드가 null이 아닌 속성들을 가진다")
    void allErrorCodesHaveNonNullProperties() {
        // given
        VoteErrorCode[] allCodes = VoteErrorCode.values();

        // when & then
        for (VoteErrorCode errorCode : allCodes) {
            assertThat(errorCode.getCode()).isNotNull().isNotEmpty();
            assertThat(errorCode.getStatus()).isNotNull();
            assertThat(errorCode.getMessage()).isNotNull().isNotEmpty();
        }
    }
}