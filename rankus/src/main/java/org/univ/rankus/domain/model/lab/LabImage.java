package org.univ.rankus.domain.model.lab;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.univ.rankus.common.BaseTimeEntity;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "lab_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lab_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)  // DB FK에 ON DELETE CASCADE
    private Lab lab;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageType type;

    /**
     * 도메인 불변 조건 검증을 포함한 생성자
     */
    public LabImage(Lab lab, String imageUrl, ImageType type) {
        this.lab      = Objects.requireNonNull(lab, "Lab은 필수입니다.");
        this.imageUrl = Objects.requireNonNull(imageUrl, "imageUrl은 필수입니다.");
        this.type     = Objects.requireNonNull(type, "ImageType은 필수입니다.");
    }

    // setter for 양방향 연관관계
    public void setLab(Lab lab) {
        this.lab = lab;
        if (lab != null && !lab.getImages().contains(this)) {
            lab.getImages().add(this);
        }
    }
}
