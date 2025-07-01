package org.univ.rankus.domain.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import org.univ.rankus.domain.model.user.exception.PasswordErrorCode;
import org.univ.rankus.domain.model.user.exception.PasswordValidationException;

import java.util.Objects;

/**
 * 비밀번호 값 객체 (해시만 저장)
 */
@Getter
@Embeddable
public class Password {

    /**
     * -- GETTER --
     * 해시값 반환 (getter)
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String hashed; // 해시된 비밀번호

    protected Password() {
        // JPA용 (리플렉션)
    }

    private Password(String hashed) {
        if (hashed == null || hashed.isBlank()) {
            throw new PasswordValidationException(PasswordErrorCode.HASH_REQUIRED);
        }
        this.hashed = hashed;// 혹은 PasswordErrorCode.HASH_REQUIRED
    }

    /**
     * 서비스 계층에서 rawPassword와 PasswordEncoder를 이용해 해시를 생성하고 Password 객체를 반환한다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param encoder     도메인 PasswordEncoder 인터페이스
     * @return Password 객체 (해시된 비밀번호만 보유)
     */
    public static Password fromRaw(String rawPassword, PasswordEncoder encoder) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new PasswordValidationException(PasswordErrorCode.PASSWORD_REQUIRED);
        }
        if (rawPassword.length() < 8) {
            throw new PasswordValidationException(PasswordErrorCode.PASSWORD_TOO_SHORT);
        }
        if (rawPassword.length() > 255) {
            throw new PasswordValidationException(PasswordErrorCode.PASSWORD_TOO_LONG);
        }
        // 향후 “너무 흔한 비밀번호”나 “특수문자 포함 여부” 같은 검증 로직을 추가할 수 있음
        String hashed = encoder.encode(rawPassword);
        return new Password(hashed);
    }

    /**
     * 입력받은 rawPassword를 내부 해시와 비교하여 일치 여부를 반환한다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param encoder     도메인 PasswordEncoder 인터페이스
     * @return 일치하면 true, 아니면 false
     */
    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        return encoder.matches(rawPassword, this.hashed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password)) return false;
        Password other = (Password) o;
        return Objects.equals(hashed, other.hashed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashed);
    }
}
