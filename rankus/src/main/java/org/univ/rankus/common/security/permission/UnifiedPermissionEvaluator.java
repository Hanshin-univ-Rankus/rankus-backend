package org.univ.rankus.common.security.permission;


import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UnifiedPermissionEvaluator implements PermissionEvaluator {

    private final Map<String, DomainPermissionEvaluator> evaluators;

    public UnifiedPermissionEvaluator(List<DomainPermissionEvaluator> handlers) {
        this.evaluators = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        DomainPermissionEvaluator::targetType,
                        Function.identity()
                ));
    }


    @Override
    public boolean hasPermission(Authentication auth,
                                 Object targetDomainObject,
                                 Object permission) {
        return false; // 사용 안 함
    }

    @Override
    public boolean hasPermission(Authentication auth,
                                 Serializable targetId,
                                 String targetType,
                                 Object permissionObj) {
        if (!(permissionObj instanceof String)) {
            return false;
        }
        DomainPermissionEvaluator evaluator = evaluators.get(targetType);
        if (evaluator == null) {
            return false;
        }
        return evaluator.hasPermission(auth, targetId, (String) permissionObj);
    }
}
