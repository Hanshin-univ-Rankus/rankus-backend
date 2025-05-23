package org.univ.rankus.domain.model.lab;

import jakarta.persistence.*;
import lombok.*;
import org.univ.rankus.common.BaseTimeEntity;

import java.util.Objects;

@Getter
@Entity
@Table(name = "labs")
public class Lab extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;              // 랩실 이름

    @Column(columnDefinition = "TEXT")
    private String description;       // 랩실 소개글

    @Column(nullable = false, length = 50)
    private String department;        // 소속 학과

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LabCategory category;     // 연구 분야(VO 형태: enum)

    @Column(nullable = false)
    private int ranking;              // 랭킹 점수 (초기값 0)

    // JPA 스펙을 위한 기본 생성자 (protected 권한)
    protected Lab() {
    }

    /**
     * 도메인 불변 조건 검증을 포함한 생성자.
     */
    public Lab(String name, String description, String department, LabCategory category) {
        // 필수 값 검증
        this.name = Objects.requireNonNull(name, "랩실 이름은 필수입니다.");
        this.department = Objects.requireNonNull(department, "소속 학과는 필수입니다.");
        this.category = Objects.requireNonNull(category, "연구 분야는 필수입니다.");

        this.description = description;
        this.ranking = 0; // 신규 랩실은 기본 랭킹 0
    }

    /**
     * 랭킹을 업데이트 하는 도메인 메서드 예시
     */
    public void updateRanking(int newRanking) {
        if (newRanking < 0) {
            throw new IllegalArgumentException("랭킹은 0 이상이어야 합니다.");
        }
        this.ranking = newRanking;
    }
}