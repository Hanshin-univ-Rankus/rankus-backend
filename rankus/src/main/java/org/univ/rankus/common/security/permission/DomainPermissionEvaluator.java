package org.univ.rankus.common.security.permission;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

import java.io.Serializable;

public interface DomainPermissionEvaluator {
    /**
     * @return this evaluator가 처리할 targetType 이름 (e.g. "LabApplication")
     */
    String targetType();

    /**
     * @param principal  Authentication#getPrincipal()
     * @param targetId   보호 대상 객체의 ID
     * @param permission 수행하려는 권한 문자열 ("cancel", "approve" 등)
     */
    default boolean hasPermission(Object principal, Serializable targetId, String permission) {
        if (principal instanceof CustomUserDetails details) {
            Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
            return hasPermission(auth, targetId, permission);
        }
        return false;
    }

    /**
     * Authentication 전체를 전달받아 권한을 평가합니다.
     * 기본 구현은 principal만 넘겨 기존 메서드로 위임합니다.
     */
    default boolean hasPermission(Authentication auth, Serializable targetId, String permission) {
        Object principal = (auth != null) ? auth.getPrincipal() : null;
        return hasPermission(principal, targetId, permission);
    }
}
