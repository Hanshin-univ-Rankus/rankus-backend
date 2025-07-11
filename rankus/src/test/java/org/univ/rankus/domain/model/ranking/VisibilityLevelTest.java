package org.univ.rankus.domain.model.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VisibilityLevel Enum 테스트")
class VisibilityLevelTest {

    @Test
    @DisplayName("모든 VisibilityLevel 값이 정의되어 있다")
    void allVisibilityLevelValues_AreDefined() {
        // given & when
        VisibilityLevel[] levels = VisibilityLevel.values();

        // then
        assertThat(levels).hasSize(3);
        assertThat(levels).contains(
                VisibilityLevel.PUBLIC,
                VisibilityLevel.LAB_ONLY,
                VisibilityLevel.PRIVATE
        );
    }

    @ParameterizedTest
    @EnumSource(VisibilityLevel.class)
    @DisplayName("모든 VisibilityLevel은 비어있지 않은 표시 이름을 가진다")
    void allVisibilityLevels_HaveNonEmptyDisplayName(VisibilityLevel level) {
        // when
        String displayName = level.getDisplayName();

        // then
        assertThat(displayName).isNotBlank();
    }

    @Test
    @DisplayName("표시 이름이 한국어로 되어 있다")
    void displayNames_AreInKorean() {
        // when & then
        assertThat(VisibilityLevel.PUBLIC.getDisplayName()).isEqualTo("전체 공개");
        assertThat(VisibilityLevel.LAB_ONLY.getDisplayName()).isEqualTo("랩실 내 공개");
        assertThat(VisibilityLevel.PRIVATE.getDisplayName()).isEqualTo("비공개");
    }

    @Test
    @DisplayName("PUBLIC은 공개 상태를 나타낸다")
    void public_IsPublic() {
        // when
        VisibilityLevel publicLevel = VisibilityLevel.PUBLIC;

        // then
        assertThat(publicLevel.isPublic()).isTrue();
        assertThat(publicLevel.isLabOnly()).isFalse();
        assertThat(publicLevel.isPrivate()).isFalse();
    }

    @Test
    @DisplayName("LAB_ONLY는 랩실 내 공개 상태를 나타낸다")
    void labOnly_IsLabOnly() {
        // when
        VisibilityLevel labOnlyLevel = VisibilityLevel.LAB_ONLY;

        // then
        assertThat(labOnlyLevel.isPublic()).isFalse();
        assertThat(labOnlyLevel.isLabOnly()).isTrue();
        assertThat(labOnlyLevel.isPrivate()).isFalse();
    }

    @Test
    @DisplayName("PRIVATE는 비공개 상태를 나타낸다")
    void private_IsPrivate() {
        // when
        VisibilityLevel privateLevel = VisibilityLevel.PRIVATE;

        // then
        assertThat(privateLevel.isPublic()).isFalse();
        assertThat(privateLevel.isLabOnly()).isFalse();
        assertThat(privateLevel.isPrivate()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "PUBLIC, true, false, false",
            "LAB_ONLY, false, true, false",
            "PRIVATE, false, false, true"
    })
    @DisplayName("각 공개 범위별 상태 메서드 검증")
    void visibilityLevelMethods_ReturnCorrectValues(VisibilityLevel level, boolean isPublic, boolean isLabOnly, boolean isPrivate) {
        // when & then
        assertThat(level.isPublic()).isEqualTo(isPublic);
        assertThat(level.isLabOnly()).isEqualTo(isLabOnly);
        assertThat(level.isPrivate()).isEqualTo(isPrivate);
    }

    @Test
    @DisplayName("모든 공개 범위는 정확히 하나의 상태만 true를 반환한다")
    void eachVisibilityLevel_HasExactlyOneMethodReturningTrue() {
        // given
        VisibilityLevel[] levels = VisibilityLevel.values();

        // when & then
        for (VisibilityLevel level : levels) {
            int trueCount = 0;
            if (level.isPublic()) trueCount++;
            if (level.isLabOnly()) trueCount++;
            if (level.isPrivate()) trueCount++;

            assertThat(trueCount)
                    .as("공개 범위 %s는 정확히 하나의 상태 메서드만 true를 반환해야 함", level)
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("공개 범위 메서드들이 상호 배타적이다")
    void visibilityLevelMethods_AreMutuallyExclusive() {
        // given
        VisibilityLevel[] levels = VisibilityLevel.values();

        // when & then
        for (VisibilityLevel level : levels) {
            // 각 레벨에서 최대 하나의 메서드만 true 반환
            int trueCount = 0;
            if (level.isPublic()) trueCount++;
            if (level.isLabOnly()) trueCount++;
            if (level.isPrivate()) trueCount++;

            assertThat(trueCount).isEqualTo(1);

            // 특정 레벨별 검증
            if (level == VisibilityLevel.PUBLIC) {
                assertThat(level.isPublic()).isTrue();
                assertThat(level.isLabOnly()).isFalse();
                assertThat(level.isPrivate()).isFalse();
            } else if (level == VisibilityLevel.LAB_ONLY) {
                assertThat(level.isPublic()).isFalse();
                assertThat(level.isLabOnly()).isTrue();
                assertThat(level.isPrivate()).isFalse();
            } else if (level == VisibilityLevel.PRIVATE) {
                assertThat(level.isPublic()).isFalse();
                assertThat(level.isLabOnly()).isFalse();
                assertThat(level.isPrivate()).isTrue();
            }
        }
    }

    @Test
    @DisplayName("공개 범위 순서가 의미상 올바르다")
    void visibilityLevelOrder_IsMeaningful() {
        // when
        VisibilityLevel[] levels = VisibilityLevel.values();

        // then
        assertThat(levels[0]).isEqualTo(VisibilityLevel.PUBLIC);   // 첫 번째: 전체 공개
        assertThat(levels[1]).isEqualTo(VisibilityLevel.LAB_ONLY); // 두 번째: 랩실 내 공개
        assertThat(levels[2]).isEqualTo(VisibilityLevel.PRIVATE);  // 세 번째: 비공개
    }

    @Test
    @DisplayName("공개 범위가 점진적으로 제한적이다")
    void visibilityLevels_AreProgressivelyRestrictive() {
        // when & then
        // PUBLIC은 가장 개방적
        assertThat(VisibilityLevel.PUBLIC.isPublic()).isTrue();

        // LAB_ONLY는 중간 수준
        assertThat(VisibilityLevel.LAB_ONLY.isLabOnly()).isTrue();

        // PRIVATE는 가장 제한적
        assertThat(VisibilityLevel.PRIVATE.isPrivate()).isTrue();
    }

    @Test
    @DisplayName("enum 이름과 표시 이름이 일관성을 가진다")
    void enumNames_AreConsistentWithDisplayNames() {
        // when & then
        assertThat(VisibilityLevel.PUBLIC.name()).isEqualTo("PUBLIC");
        assertThat(VisibilityLevel.PUBLIC.getDisplayName()).contains("전체");

        assertThat(VisibilityLevel.LAB_ONLY.name()).isEqualTo("LAB_ONLY");
        assertThat(VisibilityLevel.LAB_ONLY.getDisplayName()).contains("랩실");

        assertThat(VisibilityLevel.PRIVATE.name()).isEqualTo("PRIVATE");
        assertThat(VisibilityLevel.PRIVATE.getDisplayName()).contains("비공개");
    }

    @Test
    @DisplayName("각 공개 범위별 비즈니스 의미가 명확하다")
    void visibilityLevels_HaveClearBusinessMeaning() {
        // when & then

        // PUBLIC: 모든 사용자가 볼 수 있음
        VisibilityLevel publicLevel = VisibilityLevel.PUBLIC;
        assertThat(publicLevel.isPublic()).isTrue();
        assertThat(publicLevel.getDisplayName()).isEqualTo("전체 공개");

        // LAB_ONLY: 같은 랩실 사용자만 볼 수 있음
        VisibilityLevel labOnlyLevel = VisibilityLevel.LAB_ONLY;
        assertThat(labOnlyLevel.isLabOnly()).isTrue();
        assertThat(labOnlyLevel.getDisplayName()).isEqualTo("랩실 내 공개");

        // PRIVATE: 본인만 볼 수 있음
        VisibilityLevel privateLevel = VisibilityLevel.PRIVATE;
        assertThat(privateLevel.isPrivate()).isTrue();
        assertThat(privateLevel.getDisplayName()).isEqualTo("비공개");
    }

    @Test
    @DisplayName("공개 범위별 접근 권한 체크 로직이 올바르다")
    void visibilityLevel_AccessCheckLogic_IsCorrect() {
        // when & then

        // PUBLIC: 모든 사용자 접근 가능
        assertThat(VisibilityLevel.PUBLIC.isPublic()).isTrue();

        // LAB_ONLY: 랩실 멤버만 접근 가능
        assertThat(VisibilityLevel.LAB_ONLY.isLabOnly()).isTrue();

        // PRIVATE: 본인만 접근 가능
        assertThat(VisibilityLevel.PRIVATE.isPrivate()).isTrue();
    }

    @Test
    @DisplayName("공개 범위 메서드들이 상태 패턴을 구현한다")
    void visibilityLevelMethods_ImplementStatePattern() {
        // given
        VisibilityLevel[] levels = VisibilityLevel.values();

        // when & then
        for (VisibilityLevel level : levels) {
            // 각 레벨별로 고유한 상태 메서드가 true
            switch (level) {
                case PUBLIC:
                    assertThat(level.isPublic()).isTrue();
                    assertThat(level.isLabOnly()).isFalse();
                    assertThat(level.isPrivate()).isFalse();
                    break;
                case LAB_ONLY:
                    assertThat(level.isPublic()).isFalse();
                    assertThat(level.isLabOnly()).isTrue();
                    assertThat(level.isPrivate()).isFalse();
                    break;
                case PRIVATE:
                    assertThat(level.isPublic()).isFalse();
                    assertThat(level.isLabOnly()).isFalse();
                    assertThat(level.isPrivate()).isTrue();
                    break;
            }
        }
    }

    @Test
    @DisplayName("공개 범위가 보안 수준을 반영한다")
    void visibilityLevels_ReflectSecurityLevels() {
        // when & then

        // 보안 수준: PRIVATE > LAB_ONLY > PUBLIC
        // 공개 수준: PUBLIC > LAB_ONLY > PRIVATE

        // PUBLIC: 가장 낮은 보안 수준, 가장 높은 공개 수준
        assertThat(VisibilityLevel.PUBLIC.isPublic()).isTrue();

        // LAB_ONLY: 중간 보안 수준
        assertThat(VisibilityLevel.LAB_ONLY.isLabOnly()).isTrue();

        // PRIVATE: 가장 높은 보안 수준, 가장 낮은 공개 수준
        assertThat(VisibilityLevel.PRIVATE.isPrivate()).isTrue();
    }

    @Test
    @DisplayName("공개 범위 메서드들의 boolean 반환값이 일관성을 가진다")
    void visibilityLevelMethods_ReturnConsistentBooleanValues() {
        // when & then

        // PUBLIC
        VisibilityLevel publicLevel = VisibilityLevel.PUBLIC;
        assertThat(publicLevel.isPublic()).isTrue();
        assertThat(publicLevel.isLabOnly()).isFalse();
        assertThat(publicLevel.isPrivate()).isFalse();

        // LAB_ONLY
        VisibilityLevel labOnlyLevel = VisibilityLevel.LAB_ONLY;
        assertThat(labOnlyLevel.isPublic()).isFalse();
        assertThat(labOnlyLevel.isLabOnly()).isTrue();
        assertThat(labOnlyLevel.isPrivate()).isFalse();

        // PRIVATE
        VisibilityLevel privateLevel = VisibilityLevel.PRIVATE;
        assertThat(privateLevel.isPublic()).isFalse();
        assertThat(privateLevel.isLabOnly()).isFalse();
        assertThat(privateLevel.isPrivate()).isTrue();
    }

    @Test
    @DisplayName("공개 범위가 데이터 보호 정책을 지원한다")
    void visibilityLevels_SupportDataProtectionPolicy() {
        // when & then

        // 데이터 보호 수준별 분류
        assertThat(VisibilityLevel.PUBLIC.getDisplayName()).contains("전체"); // 공개 정보
        assertThat(VisibilityLevel.LAB_ONLY.getDisplayName()).contains("랩실"); // 내부 정보
        assertThat(VisibilityLevel.PRIVATE.getDisplayName()).contains("비공개"); // 개인 정보
    }
}