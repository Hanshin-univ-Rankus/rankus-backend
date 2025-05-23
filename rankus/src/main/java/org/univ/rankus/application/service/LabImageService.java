package org.univ.rankus.application.service;

import org.springframework.stereotype.Service;
import org.univ.rankus.application.port.in.LabImageUseCase;
import org.univ.rankus.adapter.out.persistence.SpringDataLabImageRepository;
import org.univ.rankus.adapter.out.persistence.SpringDataLabRepository;
import org.univ.rankus.domain.model.lab.Lab;
import org.univ.rankus.domain.model.lab.LabImage;
import org.univ.rankus.domain.model.lab.ImageType;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LabImageService implements LabImageUseCase {

    private final SpringDataLabRepository labRepo;
    private final SpringDataLabImageRepository imageRepo;

    public LabImageService(SpringDataLabRepository labRepo,
                           SpringDataLabImageRepository imageRepo) {
        this.labRepo = labRepo;
        this.imageRepo = imageRepo;
    }

    @Override
    public LabImage registerImage(Long labId, String imageUrl, ImageType type) {
        Lab lab = labRepo.findById(labId)
                .orElseThrow(() -> new NoSuchElementException(
                        "해당 ID의 랩실을 찾을 수 없습니다: " + labId));
        LabImage img = new LabImage(lab, imageUrl, type);
        return imageRepo.save(img);
    }

    @Override
    public List<LabImage> listImages(Long labId) {
        return imageRepo.findByLabId(labId);
    }
}
