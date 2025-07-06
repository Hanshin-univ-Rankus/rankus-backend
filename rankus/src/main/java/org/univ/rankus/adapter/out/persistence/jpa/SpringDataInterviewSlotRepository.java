package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.univ.rankus.domain.model.interview.InterviewSlot;
import org.univ.rankus.domain.model.interview.SlotStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 순수 JPA 기반 InterviewSlot 저장소 인터페이스
 * - JpaRepository<InterviewSlot, Long>을 상속하여 기본 CRUD 메서드 제공
 * - InterviewSlot 조회·검증을 위한 커스텀 메서드 선언
 */
@Repository
public interface SpringDataInterviewSlotRepository extends JpaRepository<InterviewSlot, Long> {

    /**
     * 면접 ID로 InterviewSlot 리스트를 조회합니다.
     */
    List<InterviewSlot> findByInterviewId(Long interviewId);

    /**
     * 면접 ID와 상태로 InterviewSlot 리스트를 조회합니다.
     */
    List<InterviewSlot> findByInterviewIdAndStatus(Long interviewId, SlotStatus status);

    /**
     * 특정 면접의 예약 가능한 슬롯을 조회합니다.
     */
    @Query("SELECT s FROM InterviewSlot s WHERE s.interview.id = :interviewId AND s.status = 'AVAILABLE' AND s.currentApplicants < s.maxApplicants")
    List<InterviewSlot> findAvailableSlotsByInterviewId(@Param("interviewId") Long interviewId);

    /**
     * 상태별 InterviewSlot 리스트를 조회합니다.
     */
    List<InterviewSlot> findByStatus(SlotStatus status);

    /**
     * 시작 시간 이후의 슬롯을 조회합니다.
     */
    List<InterviewSlot> findByStartTimeAfter(LocalDateTime dateTime);

    /**
     * 시작 시간 이전의 슬롯을 조회합니다.
     */
    List<InterviewSlot> findByStartTimeBefore(LocalDateTime dateTime);

    /**
     * 특정 시간 범위에 겹치는 슬롯이 있는지 확인합니다.
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM InterviewSlot s " +
            "WHERE s.interview.id = :interviewId " +
            "AND ((s.startTime <= :endTime AND s.endTime >= :startTime))")
    boolean existsByInterviewIdAndTimeRange(@Param("interviewId") Long interviewId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 면접 ID로 모든 슬롯을 삭제합니다.
     */
    void deleteByInterviewId(Long interviewId);

    /**
     * 특정 면접의 슬롯 개수를 조회합니다.
     */
    @Query("SELECT COUNT(s) FROM InterviewSlot s WHERE s.interview.id = :interviewId")
    long countByInterviewId(@Param("interviewId") Long interviewId);

    /**
     * 특정 면접의 예약된 슬롯 개수를 조회합니다.
     */
    @Query("SELECT COUNT(s) FROM InterviewSlot s WHERE s.interview.id = :interviewId AND s.currentApplicants > 0")
    long countReservedSlotsByInterviewId(@Param("interviewId") Long interviewId);

    /**
     * 시간 범위와 상태로 슬롯을 조회합니다.
     */
    @Query("SELECT s FROM InterviewSlot s WHERE s.interview.id = :interviewId " +
            "AND s.startTime >= :startTime AND s.endTime <= :endTime " +
            "AND s.status = :status " +
            "ORDER BY s.startTime ASC")
    List<InterviewSlot> findByInterviewIdAndTimeRangeAndStatus(@Param("interviewId") Long interviewId,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime,
                                                               @Param("status") SlotStatus status);

    /**
     * 특정 면접의 모든 슬롯을 시간순으로 조회합니다.
     */
    @Query("SELECT s FROM InterviewSlot s WHERE s.interview.id = :interviewId ORDER BY s.startTime ASC")
    List<InterviewSlot> findByInterviewIdOrderByStartTime(@Param("interviewId") Long interviewId);
}