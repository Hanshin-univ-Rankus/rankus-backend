package org.univ.rankus.domain.model.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.univ.rankus.common.BaseTimeEntity;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.user.exception.UserErrorCode;
import org.univ.rankus.domain.model.user.exception.UserValidationException;

/**
 * User 엔티티 - 사용자 정보를 나타내는 도메인 모델
 */
@Getter
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 사용자 고유 ID

    @Column(nullable = false, length = 30)
    private String name; // 사용자 이름

    @Column(nullable = false, length = 100, unique = true)
    private String email; // 사용자 이메일(고유)

    // Password는 @Embeddable로 정의된 값 객체
    @Embedded
    private Password password; // 비밀번호(해시값)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // 사용자 권한(기본값: STUDENT)

    // Optional: User가 소속된 Lab이 있을 수 있으므로 ManyToOne 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    private Lab lab; // 소속 랩실(선택)

    @Column(nullable = false, length = 20, unique = true)
    private String studentNumber; // 학번

    @Column(nullable = false, length = 15)
    private String phoneNumber; // 전화번호

    @Column(nullable = false)
    private Integer grade; // 학년 (1-8)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus enrollmentStatus; // 휴학/재학 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status; // 사용자 계정 상태

    /**
     * 생성자: 필수 필드들 검증 후 세팅
     * - Role은 기본값으로 STUDENT 설정
     * - Status는 기본값으로 PENDING(이메일 인증 대기) 설정
     * Service 계층에서 Password.fromRaw(...)을 사용해 Password 객체를 생성한 후 넘겨주어야 한다.
     */
    public User(String name, String email, Password password, String studentNumber,
                String phoneNumber, Integer grade, EnrollmentStatus enrollmentStatus) {
        if (password == null) {
            throw new UserValidationException(UserErrorCode.PASSWORD_REQUIRED);
        }
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.password = password;
        this.studentNumber = validateStudentNumber(studentNumber);
        this.phoneNumber = validatePhoneNumber(phoneNumber);
        this.grade = validateGrade(grade);
        this.enrollmentStatus = validateEnrollmentStatus(enrollmentStatus);
        this.role = Role.STUDENT;  // 기본값 설정
        this.status = UserStatus.PENDING; // 기본값 설정
    }

    /**
     * 계정을 활성 상태로 변경
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }


    /**
     * 이름 유효성 검증
     */
    private String validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new UserValidationException(UserErrorCode.NAME_REQUIRED);
        }
        String trimmed = name.trim();
        if (trimmed.length() > 30) {
            throw new UserValidationException(UserErrorCode.NAME_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 이메일 유효성 검증
     * - @hs.ac.kr 도메인만 허용
     */
    private String validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new UserValidationException(UserErrorCode.EMAIL_REQUIRED);
        }
        String trimmed = email.trim().toLowerCase();

        // 기본 이메일 형식 검증
        if (!trimmed.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new UserValidationException(UserErrorCode.EMAIL_INVALID);
        }

        // @hs.ac.kr 도메인 검증
        if (!trimmed.endsWith("@hs.ac.kr")) {
            throw new UserValidationException(UserErrorCode.EMAIL_DOMAIN_NOT_ALLOWED);
        }

        // @hs.ac.kr 도메인 정확한 형식 검증
        if (!trimmed.matches("^[A-Za-z0-9._%+-]+@hs\\.ac\\.kr$")) {
            throw new UserValidationException(UserErrorCode.EMAIL_INVALID);
        }

        if (trimmed.length() > 100) {
            // 이메일 길이 초과 시에도 INVALID로 처리
            throw new UserValidationException(UserErrorCode.EMAIL_INVALID);
        }
        return trimmed;
    }

    /**
     * 로그인 시 비밀번호 일치 여부를 확인하기 위한 헬퍼 메서드
     */
    public boolean checkPassword(String rawPassword, PasswordEncoder encoder) {
        return this.password.matches(rawPassword, encoder);
    }

    /**
     * 비밀번호 변경
     *
     * @param rawNewPassword 새 비밀번호 (평문)
     */
    public void changePassword(String rawNewPassword, PasswordEncoder encoder) {
        if (!StringUtils.hasText(rawNewPassword)) {
            throw new UserValidationException(UserErrorCode.PASSWORD_REQUIRED);
        }
        this.password = Password.fromRaw(rawNewPassword, encoder);
    }

    /**
     * User가 Lab에 소속될 때 호출
     *
     * @param lab 소속될 Lab 객체
     */
    public void assignLab(Lab lab) {
        if (lab == null) {
            throw new UserValidationException(UserErrorCode.LAB_REQUIRED);
        }
        this.lab = lab;
    }

    /**
     * User 권한(role) 변경
     *
     * @param newRole 새 권한
     */
    public void changeRole(Role newRole) {
        if (newRole == null) {
            throw new UserValidationException(UserErrorCode.ROLE_REQUIRED);
        }
        this.role = newRole;
    }

    /**
     * 사용자 이름 변경
     *
     * @param newName 새 사용자 이름
     */
    public void changeName(String newName) {
        this.name = validateName(newName);
    }

    /**
     * 이메일 변경
     *
     * @param newEmail 새 이메일
     */
    public void changeEmail(String newEmail) {
        this.email = validateEmail(newEmail);
    }

    /**
     * 학번 변경
     *
     * @param newStudentNumber 새 학번
     */
    public void changeStudentNumber(String newStudentNumber) {
        this.studentNumber = validateStudentNumber(newStudentNumber);
    }

    /**
     * 전화번호 변경
     *
     * @param newPhoneNumber 새 전화번호
     */
    public void changePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = validatePhoneNumber(newPhoneNumber);
    }

    /**
     * 학년 변경
     *
     * @param newGrade 새 학년
     */
    public void changeGrade(Integer newGrade) {
        this.grade = validateGrade(newGrade);
    }

    /**
     * 재학상태 변경
     *
     * @param newEnrollmentStatus 새 재학상태
     */
    public void changeEnrollmentStatus(EnrollmentStatus newEnrollmentStatus) {
        this.enrollmentStatus = validateEnrollmentStatus(newEnrollmentStatus);
    }

    /**
     * 특정 랩실의 지원서 관리 권한을 확인
     * ADMIN은 모든 랩실의 지원서 관리 가능
     * LAB_LEADER, PROFESSOR 역할이 해당 랩실에 소속된 경우 권한 부여
     */
    public boolean canManageLabApplications(Lab lab) {
        // ADMIN은 모든 랩실 관리 가능
        if (this.role == Role.ADMIN) {
            return true;
        }

        return (this.role == Role.LAB_LEADER || this.role == Role.PROFESSOR)
                && this.lab != null && this.lab.equals(lab);
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * 특정 랩실의 공지사항 조회 권한을 확인
     * 랩실 소속 멤버(LAB_MEMBER 이상) + 모든 PROFESSOR, ADMIN
     */
    public boolean canViewLabNotices(Lab lab) {
        // ADMIN과 PROFESSOR는 모든 랩실의 공지사항 조회 가능
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실 소속 멤버(LAB_MEMBER 이상)는 해당 랩실 공지사항 조회 가능
        return this.lab != null && this.lab.equals(lab)
                && (this.role == Role.LAB_MEMBER || this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 특정 랩실의 공지사항 관리(생성/수정/삭제) 권한을 확인
     * 랩 소속 LAB_MANAGER, LAB_LEADER + 모든 PROFESSOR, ADMIN
     */
    public boolean canManageLabNotices(Lab lab) {
        // ADMIN과 PROFESSOR는 모든 랩실의 공지사항 관리 가능
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실 소속 LAB_MANAGER, LAB_LEADER는 해당 랩실 공지사항 관리 가능
        return this.lab != null && this.lab.equals(lab)
                && (this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 특정 랩실의 출석 관리(세션 생성/수정/삭제) 권한을 확인
     * 랩 소속 LAB_MANAGER, LAB_LEADER + 모든 PROFESSOR, ADMIN
     */
    public boolean canManageLabAttendance(Lab lab) {
        // ADMIN과 PROFESSOR는 모든 랩실의 출석 관리 가능
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실 소속 LAB_MANAGER, LAB_LEADER는 해당 랩실 출석 관리 가능
        return this.lab != null && this.lab.equals(lab)
                && (this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 특정 랩실의 출석 조회 권한을 확인
     * 랩실 소속 멤버(LAB_MEMBER 이상) + 모든 PROFESSOR, ADMIN
     */
    public boolean canViewLabAttendance(Lab lab) {
        // ADMIN과 PROFESSOR는 모든 랩실의 출석 조회 가능
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실 소속 멤버(LAB_MEMBER 이상)는 해당 랩실 출석 조회 가능
        return this.lab != null && this.lab.equals(lab)
                && (this.role == Role.LAB_MEMBER || this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 특정 랩실의 투표 조회 권한을 확인
     * 랩실 소속 멤버(LAB_MEMBER 이상) + 모든 PROFESSOR, ADMIN
     */
    public boolean canViewVotes(Lab lab) {
        // ADMIN과 PROFESSOR는 모든 랩실의 투표 조회 가능
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실 소속 멤버(LAB_MEMBER 이상)는 해당 랩실 투표 조회 가능
        return this.lab != null && this.lab.equals(lab)
                && (this.role == Role.LAB_MEMBER || this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 특정 랩실의 투표 생성 권한을 확인
     * 랩 소속 LAB_MANAGER, LAB_LEADER + 모든 PROFESSOR, ADMIN
     */
    public boolean canCreateVotes(Lab lab) {
        // ADMIN과 PROFESSOR는 모든 랩실의 투표 생성 가능
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실 소속 LAB_MANAGER, LAB_LEADER는 해당 랩실 투표 생성 가능
        return this.lab != null && this.lab.equals(lab)
                && (this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 특정 랩실의 투표 관리(수정/삭제/결과조회) 권한을 확인
     * 랩 소속 LAB_MANAGER, LAB_LEADER + 모든 PROFESSOR, ADMIN
     */
    public boolean canManageVotes(Lab lab) {
        // ADMIN과 PROFESSOR는 모든 랩실의 투표 관리 가능
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실 소속 LAB_MANAGER, LAB_LEADER는 해당 랩실 투표 관리 가능
        return this.lab != null && this.lab.equals(lab)
                && (this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 사용자 ID 반환
     */
    public Long getUserId() {
        return this.id;
    }

    /**
     * 특정 랩실의 자료실 조회 권한을 확인
     * 랩실 소속 멤버(LAB_MEMBER 이상) + 모든 PROFESSOR, ADMIN
     */
    public boolean canViewLabResources(Lab lab) {
        if (lab == null) {
            return false;
        }

        // ADMIN과 PROFESSOR는 모든 랩실의 자료실 조회 가능
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실 소속 멤버(LAB_MEMBER 이상)는 해당 랩실 자료실 조회 가능
        return this.lab != null && this.lab.equals(lab)
                && (this.role == Role.LAB_MEMBER || this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 특정 랩실의 자료실 생성 권한을 확인
     * 랩실 소속 멤버(LAB_MEMBER 이상) + 모든 PROFESSOR, ADMIN
     */
    public boolean canCreateLabResources(Lab lab) {
        return canViewLabResources(lab); // 조회 권한과 동일
    }

    /**
     * 특정 자료의 관리(수정/삭제) 권한을 확인
     * 자료 소유자 + 랩실 관리자(LAB_MANAGER, LAB_LEADER) + 시스템 관리자(PROFESSOR, ADMIN)
     */
    public boolean canManageLabResource(org.univ.rankus.domain.model.lab.resource.LabResource resource) {
        if (resource == null) {
            return false;
        }

        // 자료 소유자
        if (resource.isUploadedBy(this)) {
            return true;
        }

        // 시스템 관리자
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실 관리자
        return this.lab != null && resource.getLab() != null && this.lab.equals(resource.getLab())
                && (this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 특정 자료의 다운로드 권한을 확인
     * 공개 자료: 랩실 멤버 이상 + 시스템 관리자
     * 비공개 자료: 자료 소유자 + 랩실 관리자 + 시스템 관리자
     */
    public boolean canDownloadLabResource(org.univ.rankus.domain.model.lab.resource.LabResource resource) {
        if (resource == null) {
            return false;
        }

        // 시스템 관리자는 모든 자료 다운로드 가능
        if (this.role == Role.ADMIN || this.role == Role.PROFESSOR) {
            return true;
        }

        // 랩실에 소속되지 않은 경우 또는 자료의 랩실 정보가 없는 경우 다운로드 불가
        if (this.lab == null || resource.getLab() == null || !this.lab.equals(resource.getLab())) {
            return false;
        }

        // 랩실 소속이 아닌 경우 다운로드 불가
        if (!(this.role == Role.LAB_MEMBER || this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER)) {
            return false;
        }

        // 공개 자료는 랩실 멤버 모두 다운로드 가능
        if (resource.getIsPublic()) {
            return true;
        }

        // 비공개 자료는 소유자 또는 랩실 관리자만 다운로드 가능
        return resource.isUploadedBy(this)
                || (this.role == Role.LAB_MANAGER || this.role == Role.LAB_LEADER);
    }

    /**
     * 학번 유효성 검증
     */
    private String validateStudentNumber(String studentNumber) {
        if (!StringUtils.hasText(studentNumber)) {
            throw new UserValidationException(UserErrorCode.STUDENT_NUMBER_REQUIRED);
        }
        String trimmed = studentNumber.trim();
        // 학번 형식 검증: 숫자만 허용, 8-20자리
        if (!trimmed.matches("^[0-9]{8,20}$")) {
            throw new UserValidationException(UserErrorCode.STUDENT_NUMBER_INVALID);
        }
        return trimmed;
    }

    /**
     * 전화번호 유효성 검증
     */
    private String validatePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new UserValidationException(UserErrorCode.PHONE_NUMBER_REQUIRED);
        }
        String trimmed = phoneNumber.trim();
        // 전화번호 형식 검증: 숫자, 하이픈 허용
        if (!trimmed.matches("^[0-9-]{10,15}$")) {
            throw new UserValidationException(UserErrorCode.PHONE_NUMBER_INVALID);
        }
        return trimmed;
    }

    /**
     * 학년 유효성 검증
     */
    private Integer validateGrade(Integer grade) {
        if (grade == null) {
            throw new UserValidationException(UserErrorCode.GRADE_REQUIRED);
        }
        if (grade < 1 || grade > 8) {
            throw new UserValidationException(UserErrorCode.GRADE_INVALID);
        }
        return grade;
    }

    /**
     * 재학상태 유효성 검증
     */
    private EnrollmentStatus validateEnrollmentStatus(EnrollmentStatus enrollmentStatus) {
        if (enrollmentStatus == null) {
            throw new UserValidationException(UserErrorCode.ENROLLMENT_STATUS_REQUIRED);
        }
        return enrollmentStatus;
    }
}
