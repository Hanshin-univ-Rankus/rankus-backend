package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 순수 JPA 기반 ScoreSubmission 저장소
 * - JpaRepository<ScoreSubmission, Long>을 상속하면 기본 CRUD 메서드가 모두 제공됩니다.
 */
@Repository
public interface SpringDataScoreSubmissionRepository extends JpaRepository<ScoreSubmission, Long> {

    /**
     * 특정 사용자 ID에 속한 점수 신청을 페이징하여 조회합니다.
     */
    Page<ScoreSubmission> findByUserIdOrderBySubmittedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 랩실 ID에 속한 점수 신청을 페이징하여 조회합니다.
     */
    Page<ScoreSubmission> findByLabIdOrderBySubmittedAtDesc(Long labId, Pageable pageable);

    /**
     * 특정 상태의 점수 신청을 페이징하여 조회합니다.
     */
    Page<ScoreSubmission> findByStatusOrderBySubmittedAtDesc(SubmissionStatus status, Pageable pageable);

    /**
     * 특정 랩실 ID와 상태의 점수 신청을 페이징하여 조회합니다.
     */
    Page<ScoreSubmission> findByLabIdAndStatusOrderBySubmittedAtDesc(Long labId, SubmissionStatus status, Pageable pageable);

    /**
     * 특정 사용자의 중복 검사를 위한 점수 신청을 조회합니다.
     */
    List<ScoreSubmission> findByUserIdAndAchievementDateOrderBySubmittedAtDesc(Long userId, LocalDate achievementDate);

    /**
     * 특정 사용자의 완전 중복 검사를 위한 점수 신청을 조회합니다.
     */
    List<ScoreSubmission> findByUserIdAndAchievementDateAndCategoryOrderBySubmittedAtDesc(
            Long userId, LocalDate achievementDate, ScoreCategory category);

    /**
     * 특정 상태이고 만료 시간이 지난 점수 신청을 조회합니다.
     */
    List<ScoreSubmission> findByStatusAndExpiresAtBeforeOrderBySubmittedAtDesc(SubmissionStatus status, LocalDateTime expiresAt);

    /**
     * 특정 상태이고 만료 시간이 특정 시간 이내인 점수 신청을 조회합니다.
     */
    List<ScoreSubmission> findByStatusAndExpiresAtBetweenOrderBySubmittedAtDesc(
            SubmissionStatus status, LocalDateTime from, LocalDateTime to);

    /**
     * 특정 랩실 ID와 상태의 점수 신청을 조회합니다.
     */
    List<ScoreSubmission> findByLabIdAndStatusOrderBySubmittedAtDesc(Long labId, SubmissionStatus status);

    /**
     * 특정 사용자 ID와 랩실 ID, 상태의 점수 신청을 조회합니다.
     */
    List<ScoreSubmission> findByUserIdAndLabIdAndStatusOrderBySubmittedAtDesc(Long userId, Long labId, SubmissionStatus status);

    /**
     * 특정 사용자 ID와 상태의 점수 신청을 조회합니다.
     */
    List<ScoreSubmission> findByUserIdAndStatusOrderBySubmittedAtDesc(Long userId, SubmissionStatus status);

    /**
     * 특정 랩실 ID와 상태의 점수 신청 점수 총합을 계산합니다.
     */
    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN s.category = 'ACADEMIC_ACHIEVEMENT' THEN 5 " +
            "WHEN s.category = 'SCHOLARSHIP' THEN 15 " +
            "WHEN s.category = 'GPA_MAINTENANCE' THEN 10 " +
            "WHEN s.category = 'CONTEST_INTERNAL_WINNER' THEN 30 " +
            "WHEN s.category = 'CONTEST_INTERNAL_RUNNER_UP' THEN 20 " +
            "WHEN s.category = 'CONTEST_EXTERNAL_WINNER' THEN 50 " +
            "WHEN s.category = 'CONTEST_EXTERNAL_RUNNER_UP' THEN 35 " +
            "WHEN s.category = 'CERTIFICATION_NATIONAL' THEN 20 " +
            "WHEN s.category = 'CERTIFICATION_INTERNATIONAL' THEN 25 " +
            "WHEN s.category = 'CERTIFICATION_PRIVATE' THEN 10 " +
            "WHEN s.category = 'EDUCATION_COMPLETION' THEN 10 " +
            "WHEN s.category = 'SEMINAR_PARTICIPATION' THEN 5 " +
            "WHEN s.category = 'RESEARCH_SCI_PAPER' THEN 100 " +
            "WHEN s.category = 'RESEARCH_GENERAL_PAPER' THEN 50 " +
            "WHEN s.category = 'RESEARCH_PATENT' THEN 30 " +
            "WHEN s.category = 'VOLUNTEER_ACTIVITY' THEN 5 " +
            "WHEN s.category = 'EXTRACURRICULAR_ACTIVITY' THEN 10 " +
            "ELSE 0 END), 0) FROM ScoreSubmission s WHERE s.lab.id = :labId AND s.status = :status")
    Integer sumScoresByLabIdAndStatus(@Param("labId") Long labId, @Param("status") SubmissionStatus status);

    /**
     * 특정 사용자 ID와 랩실 ID, 상태의 점수 신청 점수 총합을 계산합니다.
     */
    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN s.category = 'ACADEMIC_ACHIEVEMENT' THEN 5 " +
            "WHEN s.category = 'SCHOLARSHIP' THEN 15 " +
            "WHEN s.category = 'GPA_MAINTENANCE' THEN 10 " +
            "WHEN s.category = 'CONTEST_INTERNAL_WINNER' THEN 30 " +
            "WHEN s.category = 'CONTEST_INTERNAL_RUNNER_UP' THEN 20 " +
            "WHEN s.category = 'CONTEST_EXTERNAL_WINNER' THEN 50 " +
            "WHEN s.category = 'CONTEST_EXTERNAL_RUNNER_UP' THEN 35 " +
            "WHEN s.category = 'CERTIFICATION_NATIONAL' THEN 20 " +
            "WHEN s.category = 'CERTIFICATION_INTERNATIONAL' THEN 25 " +
            "WHEN s.category = 'CERTIFICATION_PRIVATE' THEN 10 " +
            "WHEN s.category = 'EDUCATION_COMPLETION' THEN 10 " +
            "WHEN s.category = 'SEMINAR_PARTICIPATION' THEN 5 " +
            "WHEN s.category = 'RESEARCH_SCI_PAPER' THEN 100 " +
            "WHEN s.category = 'RESEARCH_GENERAL_PAPER' THEN 50 " +
            "WHEN s.category = 'RESEARCH_PATENT' THEN 30 " +
            "WHEN s.category = 'VOLUNTEER_ACTIVITY' THEN 5 " +
            "WHEN s.category = 'EXTRACURRICULAR_ACTIVITY' THEN 10 " +
            "ELSE 0 END), 0) FROM ScoreSubmission s WHERE s.user.id = :userId AND s.lab.id = :labId AND s.status = :status")
    Integer sumScoresByUserIdAndLabIdAndStatus(@Param("userId") Long userId, @Param("labId") Long labId, @Param("status") SubmissionStatus status);

    /**
     * 특정 사용자 ID와 상태의 점수 신청 점수 총합을 계산합니다.
     */
    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN s.category = 'ACADEMIC_ACHIEVEMENT' THEN 5 " +
            "WHEN s.category = 'SCHOLARSHIP' THEN 15 " +
            "WHEN s.category = 'GPA_MAINTENANCE' THEN 10 " +
            "WHEN s.category = 'CONTEST_INTERNAL_WINNER' THEN 30 " +
            "WHEN s.category = 'CONTEST_INTERNAL_RUNNER_UP' THEN 20 " +
            "WHEN s.category = 'CONTEST_EXTERNAL_WINNER' THEN 50 " +
            "WHEN s.category = 'CONTEST_EXTERNAL_RUNNER_UP' THEN 35 " +
            "WHEN s.category = 'CERTIFICATION_NATIONAL' THEN 20 " +
            "WHEN s.category = 'CERTIFICATION_INTERNATIONAL' THEN 25 " +
            "WHEN s.category = 'CERTIFICATION_PRIVATE' THEN 10 " +
            "WHEN s.category = 'EDUCATION_COMPLETION' THEN 10 " +
            "WHEN s.category = 'SEMINAR_PARTICIPATION' THEN 5 " +
            "WHEN s.category = 'RESEARCH_SCI_PAPER' THEN 100 " +
            "WHEN s.category = 'RESEARCH_GENERAL_PAPER' THEN 50 " +
            "WHEN s.category = 'RESEARCH_PATENT' THEN 30 " +
            "WHEN s.category = 'VOLUNTEER_ACTIVITY' THEN 5 " +
            "WHEN s.category = 'EXTRACURRICULAR_ACTIVITY' THEN 10 " +
            "ELSE 0 END), 0) FROM ScoreSubmission s WHERE s.user.id = :userId AND s.status = :status")
    Integer sumScoresByUserIdAndStatus(@Param("userId") Long userId, @Param("status") SubmissionStatus status);

    /**
     * 특정 사용자와 랩실의 중복 신청 여부를 확인합니다.
     */
    boolean existsByUserIdAndLabIdAndAchievementDateAndCategory(
            Long userId, Long labId, LocalDate achievementDate, ScoreCategory category);
}