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

    /**
     * 생성자: 필수 필드(name, email, Password 객체) 검증 후 세팅
     * - Role은 기본값으로 STUDENT 설정
     * Service 계층에서 Password.fromRaw(...)을 사용해 Password 객체를 생성한 후 넘겨주어야 한다.
     */
    public User(String name, String email, Password password) {
        if (password == null) {
            throw new UserValidationException(UserErrorCode.PASSWORD_REQUIRED);
        }
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.password = password;
        this.role = Role.STUDENT;  // 기본값 설정
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
     */
    private String validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new UserValidationException(UserErrorCode.EMAIL_REQUIRED);
        }
        String trimmed = email.trim();
        if (!trimmed.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
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
}
