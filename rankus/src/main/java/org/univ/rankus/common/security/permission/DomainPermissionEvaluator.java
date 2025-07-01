package org.univ.rankus.common.security.permission;

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
    boolean hasPermission(Object principal, Serializable targetId, String permission);
}