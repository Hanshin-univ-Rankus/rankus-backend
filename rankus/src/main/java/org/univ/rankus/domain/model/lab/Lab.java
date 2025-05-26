package org.univ.rankus.domain.model.lab;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.univ.rankus.common.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@Table(name = "labs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lab extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(name = "professor", length = 100)
    private String professorName;    // 교수님 이름 저장용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "field")
    private LabCategory category;

    @Column(nullable = false)
    private int ranking;

    // ★ 추가: Lab ↔ LabImage 1:N 연관관계 정의
    @OneToMany(
            mappedBy = "lab",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<LabImage> images = new ArrayList<>();

    // 편의 메서드: LabImage를 추가하고 양쪽 연관관계 설정
    public void addImage(LabImage img) {
        images.add(img);
        img.setLab(this);
    }

    // 편의 메서드: LabImage를 제거하고 양쪽 연관관계 해제
    public void removeImage(LabImage img) {
        images.remove(img);
        img.setLab(null);
    }
    public Lab(String name, String description, String department, LabCategory category) {
        this.name        = Objects.requireNonNull(name,        "랩실 이름은 필수입니다.");
        this.department  = Objects.requireNonNull(department,  "소속 학과는 필수입니다.");
        this.category    = Objects.requireNonNull(category,    "연구 분야는 필수입니다.");
        this.description = description;
        this.ranking     = 0;
    }
    public Lab(String name, String department, String field,
               String description, Integer ranking, String professorName) {
        this.name = name;
        this.department = department;
        this.category = getCategory();
        this.description = description;
        this.ranking = ranking;
        this.professorName = professorName;
    }

    public void updateRanking(int newRanking) {
        if (newRanking < 0) {
            throw new IllegalArgumentException("랭킹은 0 이상이어야 합니다.");
        }
        this.ranking = newRanking;
    }

    /**
     * 랩장(또는 관리자가) 교수님을 수동으로 설정할 때 사용합니다.
     */
    public void assignProfessor(String professorName) {
        this.professorName = Objects.requireNonNull(professorName, "professorName은 필수입니다.");
    }

    /**
     * 신청 처리 중, 신청자 이름이 교수님 이름과 같으면 교수님으로 자동 배정
     */
    public void autoAssignProfessorIfMatches(String applicantName) {
        if (this.professorName == null && applicantName.equals(this.professorName)) {
            // 이미 professorName이 설정되어 있으면 건너뜀
            this.professorName = applicantName;
        }
    }
}
