package org.univ.rankus.application.port.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.univ.rankus.domain.model.ranking.ScoreCategory;
import org.univ.rankus.domain.model.ranking.ScoreSubmission;
import org.univ.rankus.domain.model.ranking.SubmissionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ScoreSubmission 도메인 퍼시스턴스 포트 인터페이스
 * - 서비스 계층은 이 인터페이스를 통해서만 ScoreSubmission을 저장·조회·삭제합니다.
 */
public interface ScoreSubmissionRepositoryPort {

    /**
     * 새로운 ScoreSubmission을 저장 또는 업데이트합니다.
     *
     * @param submission 저장할 ScoreSubmission 엔티티
     * @return 영속화된 ScoreSubmission (저장 후 ID 포함)
     */
    ScoreSubmission save(ScoreSubmission submission);

    /**
     * ID로 ScoreSubmission을 조회합니다.
     *
     * @param id 조회할 ScoreSubmission ID
     * @return Optional.of(ScoreSubmission) 또는 Optional.empty()
     */
    Optional<ScoreSubmission> findById(Long id);

    /**
     * 특정 사용자 ID에 속한 점수 신청을 페이징하여 조회합니다.
     *
     * @param userId   조회할 사용자 ID
     * @param pageable 페이징 정보
     * @return 해당 사용자의 점수 신청 페이지
     */
    Page<ScoreSubmission> findByUserId(Long userId, Pageable pageable);

    /**
     * 특정 랩실 ID에 속한 점수 신청을 페이징하여 조회합니다.
     *
     * @param labId    조회할 랩실 ID
     * @param pageable 페이징 정보
     * @return 해당 랩실의 점수 신청 페이지
     */
    Page<ScoreSubmission> findByLabId(Long labId, Pageable pageable);

    /**
     * 특정 상태의 점수 신청을 페이징하여 조회합니다.
     *
     * @param status   조회할 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 점수 신청 페이지
     */
    Page<ScoreSubmission> findByStatus(SubmissionStatus status, Pageable pageable);

    /**
     * 특정 랩실 ID와 상태의 점수 신청을 페이징하여 조회합니다.
     *
     * @param labId    조회할 랩실 ID
     * @param status   조회할 상태
     * @param pageable 페이징 정보
     * @return 해당 랩실과 상태의 점수 신청 페이지
     */
    Page<ScoreSubmission> findByLabIdAndStatus(Long labId, SubmissionStatus status, Pageable pageable);

    /**
     * 특정 사용자의 중복 검사를 위한 점수 신청을 조회합니다.
     *
     * @param userId          조회할 사용자 ID
     * @param achievementDate 취득일자
     * @return 해당 사용자의 같은 날짜 점수 신청 리스트
     */
    List<ScoreSubmission> findByUserIdAndAchievementDate(Long userId, LocalDate achievementDate);

    /**
     * 특정 사용자의 완전 중복 검사를 위한 점수 신청을 조회합니다.
     *
     * @param userId          조회할 사용자 ID
     * @param achievementDate 취득일자
     * @param category        점수 카테고리
     * @return 해당 사용자의 같은 날짜, 같은 카테고리 점수 신청 리스트
     */
    List<ScoreSubmission> findByUserIdAndAchievementDateAndCategory(Long userId, LocalDate achievementDate,
                                                                    ScoreCategory category);

    /**
     * 특정 상태이고 만료 시간이 지난 점수 신청을 조회합니다.
     *
     * @param status    조회할 상태
     * @param expiresAt 만료 시간
     * @return 만료된 점수 신청 리스트
     */
    List<ScoreSubmission> findByStatusAndExpiresAtBefore(SubmissionStatus status, LocalDateTime expiresAt);

    /**
     * 특정 상태이고 만료 시간이 특정 시간 이내인 점수 신청을 조회합니다.
     *
     * @param status    조회할 상태
     * @param expiresAt 만료 시간
     * @return 만료 예정 점수 신청 리스트
     */
    List<ScoreSubmission> findByStatusAndExpiresAtBetween(SubmissionStatus status, LocalDateTime from, LocalDateTime to);

    /**
     * 특정 랩실 ID와 상태의 점수 신청을 조회합니다.
     *
     * @param labId  조회할 랩실 ID
     * @param status 조회할 상태
     * @return 해당 랩실과 상태의 점수 신청 리스트
     */
    List<ScoreSubmission> findByLabIdAndStatus(Long labId, SubmissionStatus status);

    /**
     * 특정 사용자 ID와 랩실 ID, 상태의 점수 신청을 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @param labId  조회할 랩실 ID
     * @param status 조회할 상태
     * @return 해당 사용자의 랩실 내 특정 상태 점수 신청 리스트
     */
    List<ScoreSubmission> findByUserIdAndLabIdAndStatus(Long userId, Long labId, SubmissionStatus status);

    /**
     * 특정 사용자 ID와 상태의 점수 신청을 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @param status 조회할 상태
     * @return 해당 사용자의 특정 상태 점수 신청 리스트
     */
    List<ScoreSubmission> findByUserIdAndStatus(Long userId, SubmissionStatus status);

    /**
     * 특정 랩실 ID와 상태의 점수 신청 점수 총합을 계산합니다.
     *
     * @param labId  조회할 랩실 ID
     * @param status 조회할 상태
     * @return 점수 총합
     */
    Integer sumScoresByLabIdAndStatus(Long labId, SubmissionStatus status);

    /**
     * 특정 사용자 ID와 랩실 ID, 상태의 점수 신청 점수 총합을 계산합니다.
     *
     * @param userId 조회할 사용자 ID
     * @param labId  조회할 랩실 ID
     * @param status 조회할 상태
     * @return 점수 총합
     */
    Integer sumScoresByUserIdAndLabIdAndStatus(Long userId, Long labId, SubmissionStatus status);

    /**
     * 특정 사용자 ID와 상태의 점수 신청 점수 총합을 계산합니다.
     *
     * @param userId 조회할 사용자 ID
     * @param status 조회할 상태
     * @return 점수 총합
     */
    Integer sumScoresByUserIdAndStatus(Long userId, SubmissionStatus status);

    /**
     * ScoreSubmission을 삭제합니다.
     *
     * @param submission 삭제할 ScoreSubmission 엔티티
     */
    void delete(ScoreSubmission submission);

    /**
     * 특정 사용자와 랩실의 중복 신청 여부를 확인합니다.
     *
     * @param userId          사용자 ID
     * @param labId           랩실 ID
     * @param achievementDate 취득일자
     * @param category        점수 카테고리
     * @return 중복 신청 여부
     */
    boolean existsByUserIdAndLabIdAndAchievementDateAndCategory(Long userId, Long labId,
                                                                LocalDate achievementDate, ScoreCategory category);
}