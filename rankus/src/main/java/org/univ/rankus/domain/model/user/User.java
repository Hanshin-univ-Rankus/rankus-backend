package org.univ.rankus.domain.model.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.univ.rankus.domain.model.lab.Lab;

import java.util.regex.Pattern;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Embedded
    private Password password;

    /**
     * 이제 Optional 관계로 변경: 소속 랩실이 없을 수 있음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")  // nullable=true 가 default
    private Lab lab;

    /**
     * 생성자: 이제 lab은 null 허용
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public User(String name, String email, String rawPassword) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 필수입니다.");
        }
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("유효한 email을 입력하세요.");
        }
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("password는 8자 이상이어야 합니다.");
        }
        this.name = name;
        this.email = email;
        this.password = Password.of(rawPassword);
        this.lab = null;  // null 허용
    }



    /** 비밀번호 검증 편의 메서드 */
    public boolean matchesPassword(String raw) {
        return this.password.matches(raw);
    }
}