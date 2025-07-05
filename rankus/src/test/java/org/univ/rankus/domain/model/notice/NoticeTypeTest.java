package org.univ.rankus.domain.model.notice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ.rankus.domain.model.lab.notice.NoticeType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("NoticeType Enum 테스트")
class NoticeTypeTest {

    @Test
    @DisplayName("모든 공지사항 타입이 정의되어 있음")
    void allNoticeTypes_areDefined() {
        // given & when
        NoticeType[] types = NoticeType.values();

        // then
        assertThat(types).hasSize(2);
        assertThat(types).contains(NoticeType.NORMAL, NoticeType.URGENT);
    }

    @Test
    @DisplayName("NORMAL 타입은 긴급하지 않음")
    void normalType_isNotUrgent() {
        // when
        boolean isUrgent = NoticeType.NORMAL.isUrgent();

        // then
        assertThat(isUrgent).isFalse();
    }

    @Test
    @DisplayName("URGENT 타입은 긴급함")
    void urgentType_isUrgent() {
        // when
        boolean isUrgent = NoticeType.URGENT.isUrgent();

        // then
        assertThat(isUrgent).isTrue();
    }

    @Test
    @DisplayName("NoticeType의 description 확인")
    void noticeType_hasCorrectDescription() {
        // when & then
        assertThat(NoticeType.NORMAL.getDescription()).isEqualTo("일반 공지");
        assertThat(NoticeType.URGENT.getDescription()).isEqualTo("긴급 공지");
    }

    @Test
    @DisplayName("NoticeType의 toString은 name을 반환")
    void noticeType_toString_returnsName() {
        // when & then
        assertThat(NoticeType.NORMAL.toString()).isEqualTo("NORMAL");
        assertThat(NoticeType.URGENT.toString()).isEqualTo("URGENT");
    }

    @Test
    @DisplayName("NoticeType의 name() 메서드 확인")
    void noticeType_name_returnsCorrectName() {
        // when & then
        assertThat(NoticeType.NORMAL.name()).isEqualTo("NORMAL");
        assertThat(NoticeType.URGENT.name()).isEqualTo("URGENT");
    }

    @Test
    @DisplayName("NoticeType의 ordinal 확인")
    void noticeType_ordinal_returnsCorrectOrder() {
        // when & then
        assertThat(NoticeType.NORMAL.ordinal()).isEqualTo(0);
        assertThat(NoticeType.URGENT.ordinal()).isEqualTo(1);
    }

    @Test
    @DisplayName("valueOf를 통한 NoticeType 생성")
    void noticeType_valueOf_returnsCorrectType() {
        // when & then
        assertThat(NoticeType.valueOf("NORMAL")).isEqualTo(NoticeType.NORMAL);
        assertThat(NoticeType.valueOf("URGENT")).isEqualTo(NoticeType.URGENT);
    }

    @Test
    @DisplayName("잘못된 문자열로 valueOf 호출시 예외 발생")
    void noticeType_valueOf_invalidString_throwsException() {
        // when & then
        assertThatThrownBy(() -> NoticeType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}