package org.univ.rankus.common.security.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.univ.rankus.application.port.out.LabRepositoryPort;
import org.univ.rankus.application.port.out.LabResourceRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.common.security.customUser.CustomUserDetails;
import org.univ.rankus.domain.model.lab.core.Lab;
import org.univ.rankus.domain.model.lab.resource.LabResource;
import org.univ.rankus.domain.model.user.User;

/**
 * 랩실 자료 권한 처리 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LabResourcePermissionHandler {

    private final LabRepositoryPort labRepositoryPort;
    private final LabResourceRepositoryPort labResourceRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    /**
     * 랩실 자료실에 대한 권한을 확인합니다.
     *
     * @param userDetails 현재 사용자 정보
     * @param labId       랩실 ID
     * @param permission  요청한 권한 (VIEW_RESOURCES, CREATE_RESOURCES, etc.)
     * @return 권한 보유 여부
     */
    public boolean hasPermissionForLab(CustomUserDetails userDetails, Long labId, String permission) {
        if (userDetails == null || labId == null) {
            log.debug("User details or lab ID is null");
            return false;
        }

        try {
            Lab lab = labRepositoryPort.findById(labId).orElse(null);
            if (lab == null) {
                log.debug("Lab not found: {}", labId);
                return false;
            }

            User user = userRepositoryPort.findById(userDetails.getUserId()).orElse(null);
            if (user == null) {
                log.debug("User not found: {}", userDetails.getUserId());
                return false;
            }

            return switch (permission) {
                case "VIEW_RESOURCES" -> user.canViewLabResources(lab);
                case "CREATE_RESOURCES" -> user.canCreateLabResources(lab);
                default -> {
                    log.warn("Unknown permission: {}", permission);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.error("Error checking lab resource permission for user: {}, lab: {}, permission: {}",
                    userDetails.getUserId(), labId, permission, e);
            return false;
        }
    }

    /**
     * 특정 자료에 대한 권한을 확인합니다.
     *
     * @param userDetails 현재 사용자 정보
     * @param resourceId  자료 ID
     * @param permission  요청한 권한 (DOWNLOAD, MANAGE, etc.)
     * @return 권한 보유 여부
     */
    public boolean hasPermissionForResource(CustomUserDetails userDetails, Long resourceId, String permission) {
        if (userDetails == null || resourceId == null) {
            log.debug("User details or resource ID is null");
            return false;
        }

        try {
            LabResource resource = labResourceRepositoryPort.findById(resourceId).orElse(null);
            if (resource == null) {
                log.debug("Resource not found: {}", resourceId);
                return false;
            }

            User user = userRepositoryPort.findById(userDetails.getUserId()).orElse(null);
            if (user == null) {
                log.debug("User not found: {}", userDetails.getUserId());
                return false;
            }

            return switch (permission) {
                case "DOWNLOAD" -> user.canDownloadLabResource(resource);
                case "MANAGE" -> user.canManageLabResource(resource);
                case "VIEW" -> user.canViewLabResources(resource.getLab());
                default -> {
                    log.warn("Unknown permission: {}", permission);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.error("Error checking resource permission for user: {}, resource: {}, permission: {}",
                    userDetails.getUserId(), resourceId, permission, e);
            return false;
        }
    }

    /**
     * 사용자가 자료의 소유자인지 확인합니다.
     *
     * @param userDetails 현재 사용자 정보
     * @param resourceId  자료 ID
     * @return 소유자 여부
     */
    public boolean isResourceOwner(CustomUserDetails userDetails, Long resourceId) {
        if (userDetails == null || resourceId == null) {
            return false;
        }

        try {
            LabResource resource = labResourceRepositoryPort.findById(resourceId).orElse(null);
            if (resource == null) {
                log.debug("Resource not found: {}", resourceId);
                return false;
            }

            User user = userRepositoryPort.findById(userDetails.getUserId()).orElse(null);
            if (user == null) {
                log.debug("User not found: {}", userDetails.getUserId());
                return false;
            }

            return resource.isUploadedBy(user);
        } catch (Exception e) {
            log.error("Error checking resource ownership for user: {}, resource: {}",
                    userDetails.getUserId(), resourceId, e);
            return false;
        }
    }

    /**
     * 랩실 관리자 권한을 확인합니다.
     *
     * @param userDetails 현재 사용자 정보
     * @param labId       랩실 ID
     * @return 랩실 관리자 여부
     */
    public boolean isLabManager(CustomUserDetails userDetails, Long labId) {
        if (userDetails == null || labId == null) {
            return false;
        }

        try {
            Lab lab = labRepositoryPort.findById(labId).orElse(null);
            if (lab == null) {
                log.debug("Lab not found: {}", labId);
                return false;
            }

            User user = userRepositoryPort.findById(userDetails.getUserId()).orElse(null);
            if (user == null) {
                log.debug("User not found: {}", userDetails.getUserId());
                return false;
            }

            return user.canManageLabNotices(lab);
        } catch (Exception e) {
            log.error("Error checking lab manager permission for user: {}, lab: {}",
                    userDetails.getUserId(), labId, e);
            return false;
        }
    }
}