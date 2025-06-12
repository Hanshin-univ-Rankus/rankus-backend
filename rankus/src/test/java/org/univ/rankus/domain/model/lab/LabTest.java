package org.univ.rankus.domain.model.lab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.univ.rankus.domain.model.lab.exception.LabValidationException;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.testutil.factory.domain.DomainLabFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lab 도메인 단위 테스트")
class LabTest {

    private final String validName = "TestLab";
    private final LabCategory validCategory = LabCategory.AI;
    private final String validDesc = "Description";
    private final String validProf = "ProfX";

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorTests {

        @ParameterizedTest(name = "[{index}] name=''{0}'' → LAB_NAME_REQUIRED 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("name이 null 또는 blank일 경우 LAB_NAME_REQUIRED 예외 발생")
        void nameNullOrBlank_throwsNameRequired(String name) {
            // when & then
            LabValidationException ex = assertThrows(
                    LabValidationException.class,
                    () -> new Lab(name, validCategory, validDesc, validProf)
            );
            assertEquals(LabErrorCode.LAB_NAME_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("category가 null일 경우 LAB_CATEGORY_REQUIRED 예외 발생")
        void categoryNull_throwsCategoryRequired() {
            // when & then
            LabValidationException ex = assertThrows(
                    LabValidationException.class,
                    () -> new Lab(validName, null, validDesc, validProf)
            );
            assertEquals(LabErrorCode.LAB_CATEGORY_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("description이 null일 경우 null로 설정")
        void descriptionNull_setsNull() {
            // when
            Lab lab = new Lab(validName, validCategory, null, validProf);
            // then
            assertNull(lab.getDescription());
        }

        @ParameterizedTest(name = "[{index}] desc=''{0}'' → 빈 문자열로 설정")
        @ValueSource(strings = {"   "})
        @DisplayName("description이 blank일 경우 빈 문자열로 설정")
        void descriptionBlank_setsEmpty(String desc) {
            // when
            Lab lab = new Lab(validName, validCategory, desc, validProf);
            // then
            assertEquals("", lab.getDescription());
        }

        @Test
        @DisplayName("description 길이 초과 시 LAB_DESCRIPTION_TOO_LONG 예외 발생")
        void descriptionTooLong_throwsDescriptionTooLong() {
            // given
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1001; i++) sb.append('a');
            String longDesc = sb.toString();
            // when & then
            LabValidationException ex = assertThrows(
                    LabValidationException.class,
                    () -> new Lab(validName, validCategory, longDesc, validProf)
            );
            assertEquals(LabErrorCode.LAB_DESCRIPTION_TOO_LONG, ex.getErrorCode());
        }

        @Test
        @DisplayName("professorName이 null일 경우 null로 설정")
        void professorNull_setsNull() {
            // when
            Lab lab = new Lab(validName, validCategory, validDesc, null);
            // then
            assertNull(lab.getProfessorName());
        }

        @ParameterizedTest(name = "[{index}] prof=''{0}'' → null로 설정")
        @ValueSource(strings = {"   "})
        @DisplayName("professorName이 blank일 경우 null로 설정")
        void professorBlank_setsNull(String prof) {
            // when
            Lab lab = new Lab(validName, validCategory, validDesc, prof);
            // then
            assertNull(lab.getProfessorName());
        }

        @Test
        @DisplayName("professorName trim 적용")
        void professorTrim_setsTrimmed() {
            // when
            Lab lab = new Lab(validName, validCategory, validDesc, " ProfX ");
            // then
            assertEquals("ProfX", lab.getProfessorName());
        }

        @Test
        @DisplayName("유효한 값 입력 시 필드 설정 및 ranking 기본값 0")
        void validLab_setsFields() {
            // when
            Lab lab = DomainLabFactory.buildValidLab();
            // then
            assertNull(lab.getId());
            assertEquals(validName, lab.getName());
            assertEquals(validCategory, lab.getCategory());
            assertEquals(validDesc, lab.getDescription());
            assertEquals(validProf, lab.getProfessorName());
            assertEquals(0, lab.getRanking());
        }
    }

    @Nested
    @DisplayName("autoAssignProfessorIfMatches 메서드 검증")
    class AutoAssignTests {

        @Test
        @DisplayName("professorName이 null이고 일치하는 applicantName 입력 시 설정")
        void assign_whenNullProfessor_setsProfessor() {
            // given
            Lab lab = DomainLabFactory.buildLab_NoProfessor();
            // when
            lab.autoAssignProfessorIfMatches("ProfX");
            // then
            assertEquals("ProfX", lab.getProfessorName());
        }

        @Test
        @DisplayName("professorName이 이미 설정된 경우 변경되지 않음")
        void assign_whenProfessorExists_noChange() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            // when
            lab.autoAssignProfessorIfMatches("NewProf");
            // then
            assertEquals(validProf, lab.getProfessorName());
        }

        @ParameterizedTest(name = "[{index}] applicant=''{0}'' → 변경되지 않음")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("applicantName이 null 또는 blank일 때 변경되지 않음")
        void assign_nullOrBlankApplicant_noChange(String applicant) {
            // given
            Lab lab = DomainLabFactory.buildLab_NoProfessor();
            // when
            lab.autoAssignProfessorIfMatches(applicant);
            // then
            assertNull(lab.getProfessorName());
        }
    }

    @Nested
    @DisplayName("ProfessorName 설정 검증")
    class ProfessorNameTests {
        @Test
        @DisplayName("null 입력 시 null로 설정")
        void setProfessorName_null_setsNull() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            // when
            lab.setProfessorName(null);
            // then
            assertNull(lab.getProfessorName());
        }

        @ParameterizedTest(name = "[{index}] name=''{0}'' → null로 설정")
        @ValueSource(strings = {"   "})
        @DisplayName("blank 입력 시 null로 설정")
        void setProfessorName_blank_setsNull(String name) {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            // when
            lab.setProfessorName(name);
            // then
            assertNull(lab.getProfessorName());
        }

        @Test
        @DisplayName("유효한 이름 입력 시 trim 적용")
        void setProfessorName_valid_setsTrimmed() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            // when
            lab.setProfessorName(" ProfX ");
            // then
            assertEquals("ProfX", lab.getProfessorName());
        }

        @Test
        @DisplayName("10자 초과 입력 시 LAB_PROFESSOR_NAME_TOO_LONG 예외 발생")
        void setProfessorName_tooLong_throwsException() {
            // given
            Lab lab = DomainLabFactory.buildValidLab();
            String longName = "a".repeat(11); // 11자 이름
            // when & then
            LabValidationException ex = assertThrows(
                    LabValidationException.class,
                    () -> lab.setProfessorName(longName)
            );
            assertEquals(LabErrorCode.LAB_PROFESSOR_NAME_TOO_LONG, ex.getErrorCode());
        }
    }
}