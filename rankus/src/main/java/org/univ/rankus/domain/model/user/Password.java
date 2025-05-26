package org.univ.rankus.domain.model.user;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Password {
    @Column(name = "password_hash", nullable = false)
    private String hash;

    private Password(String hash) {
        this.hash = hash;
    }

    /** 팩토리 메서드: 원문을 해시 */
    public static Password of(String raw) {
        String hashed = BCrypt.hashpw(raw, BCrypt.gensalt());
        return new Password(hashed);
    }

    /** 원문과 해시 매치 검증 */
    public boolean matches(String raw) {
        return BCrypt.checkpw(raw, this.hash);
    }
}
