package org.univ.rankus.application.port.out;

import org.univ.rankus.domain.model.user.EmailVerification;

import java.util.Optional;

/**
 * 이메일 인증 정보 영속성을 위한 리포지토리 포트
 * - EmailVerification 엔티티 CRUD 작업
 * - 이메일별 인증 정보 조회
 */
public interface EmailVerificationRepositoryPort {

    /**
     * 이메일 인증 정보 저장
     *
     * @param emailVerification 저장할 이메일 인증 정보
     * @return 저장된 이메일 인증 정보
     */
    EmailVerification save(EmailVerification emailVerification);

    /**
     * 이메일로 최신 인증 정보 조회
     *
     * @param email 이메일 주소
     * @return 해당 이메일의 최신 인증 정보
     */
    Optional<EmailVerification> findLatestByEmail(String email);

    /**
     * ID로 이메일 인증 정보 조회
     *
     * @param id 이메일 인증 정보 ID
     * @return 해당 ID의 이메일 인증 정보
     */
    Optional<EmailVerification> findById(Long id);

    /**
     * 이메일의 인증 완료된 정보 존재 여부 확인
     *
     * @param email 이메일 주소
     * @return 인증 완료된 정보 존재 여부
     */
    boolean existsVerifiedByEmail(String email);

    /**
     * 이메일 인증 정보 삭제 (테스트용)
     *
     * @param id 삭제할 이메일 인증 정보 ID
     */
    void deleteById(Long id);

    /**
     * 해당 이메일의 모든 인증 정보 삭제 (테스트용)
     *
     * @param email 이메일 주소
     */
    void deleteAllByEmail(String email);
}