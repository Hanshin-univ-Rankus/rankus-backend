package org.univ.rankus.testutil.factory.domain;

import org.springframework.test.util.ReflectionTestUtils;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.lab.ImageType;

/**
 * DomainLabImageFactory - 순수 도메인 단위 테스트 전용 팩토리
 * 외부 의존(JPA, Repository 등) 없이 LabImage 엔티티 생성 메서드만 제공합니다.
 */
public final class DomainLabImageFactory {
    private DomainLabImageFactory() {}

    public static LabImage buildValidLabImage(Lab lab, String url, ImageType type) {
        return new LabImage(lab, url, type);
    }

    public static LabImage buildValidLabImageWithId(Long id, Lab lab, String url, ImageType type) {
        LabImage img = buildValidLabImage(lab, url, type);
        ReflectionTestUtils.setField(img, "id", id);
        return img;
    }

    public static LabImage buildInvalidLabImage_NullLab(String url, ImageType type) {
        // 단순 호출로 예외 발생을 테스트 케이스에서 확인
        return new LabImage(null, url, type);
    }

    public static LabImage buildInvalidLabImage_NullOrBlankUrl(Lab lab) {
        return new LabImage(lab, "", ImageType.ADDITIONAL);
    }

    public static LabImage buildInvalidLabImage_LongUrl(Lab lab, String baseUrl) {
        StringBuilder sb = new StringBuilder(baseUrl);
        while (sb.length() <= 255) sb.append('a');
        return new LabImage(lab, sb.toString(), ImageType.REPRESENTATIVE);
    }

    public static LabImage buildInvalidLabImage_InvalidUrl(Lab lab) {
        return new LabImage(lab, "invalid-url", ImageType.ADDITIONAL);
    }

    public static LabImage buildInvalidLabImage_NullType(Lab lab, String url) {
        return new LabImage(lab, url, null);
    }

    public static LabImage buildCustomLabImage(Lab lab, String url, ImageType type) {
        return new LabImage(lab, url, type);
    }
}
