package org.univ.rankus.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.univ.rankus.domain.model.user.EmailVerification;

import java.util.Optional;

/**
 * EmailVerification 엔티티를 위한 Spring Data JPA 리포지토리
 * - 이메일 인증 정보 CRUD 작업
 * - 복잡한 쿼리를 위한 @Query 어노테이션 활용
 */
public interface SpringDataEmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    /**
     * 이메일로 최신 인증 정보 조회 (생성 시간 기준 내림차순)
     *
     * @param email 이메일 주소
     * @return 해당 이메일의 최신 인증 정보
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email ORDER BY ev.createdAt DESC LIMIT 1")
    Optional<EmailVerification> findLatestByEmail(@Param("email") String email);

    /**
     * 이메일의 인증 완료된 정보 존재 여부 확인
     *
     * @param email 이메일 주소
     * @return 인증 완료된 정보 존재 여부
     */
    boolean existsByEmailAndVerifiedTrue(String email);

    /**
     * 해당 이메일의 모든 인증 정보 삭제 (테스트용)
     *
     * @param email 이메일 주소
     */
    void deleteAllByEmail(String email);

    /**
     * 해당 이메일의 인증 정보 개수 조회 (테스트/모니터링용)
     *
     * @param email 이메일 주소
     * @return 인증 정보 개수
     */
    long countByEmail(String email);

    /**
     * 인증 완료된 정보 조회
     *
     * @param email 이메일 주소
     * @return 인증 완료된 최신 정보
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.verified = true ORDER BY ev.createdAt DESC LIMIT 1")
    Optional<EmailVerification> findLatestVerifiedByEmail(@Param("email") String email);
}