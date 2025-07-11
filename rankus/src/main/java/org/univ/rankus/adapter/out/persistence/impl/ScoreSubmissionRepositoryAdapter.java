package org.univ.rankus.adapter.out.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.univ.rankus.adapter.out.persistence.jpa.SpringDataScoreSubmissionRepository;
import org.univ.rankus.application.port.out.ScoreSubmissionRepositoryPort;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ScoreSubmissionRepositoryPort 어댑터 구현체
 * - 내부에서 SpringDataScoreSubmissionRepository를 호출하여 실제 DB 저장·조회 기능을 위임합니다.
 */
@Repository
@RequiredArgsConstructor
public class ScoreSubmissionRepositoryAdapter implements ScoreSubmissionRepositoryPort {

    private final SpringDataScoreSubmissionRepository springDataScoreSubmissionRepository;

    @Override
    public ScoreSubmission save(ScoreSubmission submission) {
        return springDataScoreSubmissionRepository.save(submission);
    }

    @Override
    public Optional<ScoreSubmission> findById(Long id) {
        return springDataScoreSubmissionRepository.findById(id);
    }

    @Override
    public Page<ScoreSubmission> findByUserId(Long userId, Pageable pageable) {
        return springDataScoreSubmissionRepository.findByUserIdOrderBySubmittedAtDesc(userId, pageable);
    }

    @Override
    public Page<ScoreSubmission> findByLabId(Long labId, Pageable pageable) {
        return springDataScoreSubmissionRepository.findByLabIdOrderBySubmittedAtDesc(labId, pageable);
    }

    @Override
    public Page<ScoreSubmission> findByStatus(SubmissionStatus status, Pageable pageable) {
        return springDataScoreSubmissionRepository.findByStatusOrderBySubmittedAtDesc(status, pageable);
    }

    @Override
    public Page<ScoreSubmission> findByLabIdAndStatus(Long labId, SubmissionStatus status, Pageable pageable) {
        return springDataScoreSubmissionRepository.findByLabIdAndStatusOrderBySubmittedAtDesc(labId, status, pageable);
    }

    @Override
    public List<ScoreSubmission> findByUserIdAndAchievementDate(Long userId, LocalDate achievementDate) {
        return springDataScoreSubmissionRepository.findByUserIdAndAchievementDateOrderBySubmittedAtDesc(userId, achievementDate);
    }

    @Override
    public List<ScoreSubmission> findByUserIdAndAchievementDateAndCategory(Long userId, LocalDate achievementDate, ScoreCategory category) {
        return springDataScoreSubmissionRepository.findByUserIdAndAchievementDateAndCategoryOrderBySubmittedAtDesc(userId, achievementDate, category);
    }

    @Override
    public List<ScoreSubmission> findByStatusAndExpiresAtBefore(SubmissionStatus status, LocalDateTime expiresAt) {
        return springDataScoreSubmissionRepository.findByStatusAndExpiresAtBeforeOrderBySubmittedAtDesc(status, expiresAt);
    }

    @Override
    public List<ScoreSubmission> findByStatusAndExpiresAtBetween(SubmissionStatus status, LocalDateTime from, LocalDateTime to) {
        return springDataScoreSubmissionRepository.findByStatusAndExpiresAtBetweenOrderBySubmittedAtDesc(status, from, to);
    }

    @Override
    public List<ScoreSubmission> findByLabIdAndStatus(Long labId, SubmissionStatus status) {
        return springDataScoreSubmissionRepository.findByLabIdAndStatusOrderBySubmittedAtDesc(labId, status);
    }

    @Override
    public List<ScoreSubmission> findByUserIdAndLabIdAndStatus(Long userId, Long labId, SubmissionStatus status) {
        return springDataScoreSubmissionRepository.findByUserIdAndLabIdAndStatusOrderBySubmittedAtDesc(userId, labId, status);
    }

    @Override
    public List<ScoreSubmission> findByUserIdAndStatus(Long userId, SubmissionStatus status) {
        return springDataScoreSubmissionRepository.findByUserIdAndStatusOrderBySubmittedAtDesc(userId, status);
    }

    @Override
    public Integer sumScoresByLabIdAndStatus(Long labId, SubmissionStatus status) {
        return springDataScoreSubmissionRepository.sumScoresByLabIdAndStatus(labId, status);
    }

    @Override
    public Integer sumScoresByUserIdAndLabIdAndStatus(Long userId, Long labId, SubmissionStatus status) {
        return springDataScoreSubmissionRepository.sumScoresByUserIdAndLabIdAndStatus(userId, labId, status);
    }

    @Override
    public Integer sumScoresByUserIdAndStatus(Long userId, SubmissionStatus status) {
        return springDataScoreSubmissionRepository.sumScoresByUserIdAndStatus(userId, status);
    }

    @Override
    public void delete(ScoreSubmission submission) {
        springDataScoreSubmissionRepository.delete(submission);
    }

    @Override
    public boolean existsByUserIdAndLabIdAndAchievementDateAndCategory(Long userId, Long labId, LocalDate achievementDate, ScoreCategory category) {
        return springDataScoreSubmissionRepository.existsByUserIdAndLabIdAndAchievementDateAndCategory(userId, labId, achievementDate, category);
    }
}