package org.univ.rankus.domain.model.lab.core;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.lab.exception.LabErrorCode;
import org.univ.rankus.domain.model.lab.exception.LabValidationException;

/**
 * Lab 엔티티
 */
@Getter
@Entity
@Table(name = "labs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lab extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 랩실 고유 ID

    // 랩실 이름(필수, 최대 10자)
    @Column(nullable = false, length = 10)
    private String name;

    // 랩실 카테고리(필수, 최대 10자)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LabCategory category;

    // 랩실 설명(최대 255자)
    private String description;

    // 랭킹(0이상 정수)
    @Setter
    private Integer ranking;

    // Optional: 교수님 이름
    @Column(name = "professor_name", length = 10)
    private String professorName;

    /**
     * 생성자: 필수 필드(name, category)와 선택 필드(description, professorName) 검증 후 세팅
     */
    public Lab(String name, LabCategory category, String description, String professorName) {
        this.name = validateName(name);
        this.category = validateCategory(category);
        this.description = validateDescription(description);
        this.ranking = 0;
        this.professorName = (professorName != null && !professorName.isBlank())
                ? professorName.trim()
                : null;
    }

    /**
     * 랩실 이름 검증 (필수)
     */
    private String validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new LabValidationException(LabErrorCode.LAB_NAME_REQUIRED);
        }
        return name.trim();
    }

    /**
     * 랩실 카테고리 검증 (필수)
     */
    private LabCategory validateCategory(LabCategory category) {
        if (category == null) {
            throw new LabValidationException(LabErrorCode.LAB_CATEGORY_REQUIRED);
        }
        return category;
    }

    /**
     * 랩실 설명 검증 (최대 200자)
     */
    private String validateDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > 255) {
            throw new LabValidationException(LabErrorCode.LAB_DESCRIPTION_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 랭킹 검증 (0 이상)
     */
    private Integer validateRanking(Integer ranking) {
        if (ranking == null) {
            return 0; // 랭킹이 없으면 기본값 0으로 설정
        }
        if (ranking < 0) {
            throw new LabValidationException(LabErrorCode.LAB_RANKING_INVALID);
        }
        return ranking;
    }

    /**
     * 신청 처리 중, 신청자 이름이 교수님 이름과 같으면 교수님으로 자동 배정
     */
    public void autoAssignProfessorIfMatches(String applicantName) {
        // this.professorName이 null인지 먼저 체크하여 NPE 방지
        if (this.professorName == null && StringUtils.hasText(applicantName)) {
            this.professorName = applicantName.trim();
        }
    }

    public void setProfessorName(String professorName) {
        if (professorName == null || professorName.isBlank()) {
            this.professorName = null; // null은 null로 설정
            return;
        }
        // 교수님 이름은 최대 10자
        if (professorName.length() > 10) {
            throw new LabValidationException(LabErrorCode.LAB_PROFESSOR_NAME_TOO_LONG);
        } else {
            this.professorName = professorName.trim();
        }
    }

    /**
     * 랭킹 업데이트
     *
     * @param newRanking 새로운 랭킹 값
     */
    public void updateRanking(Integer newRanking) {
        this.ranking = validateRanking(newRanking);
    }
}
