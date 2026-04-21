package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.users.userMe.BusinessPermissionResponse;
import com.kynsof.identity.application.query.users.userMe.UserMeResponse;
import com.kynsof.identity.domain.interfaces.service.IUserMeService;
import com.kynsof.identity.infrastructure.config.IdentityCacheConfig;
import com.kynsof.identity.infrastructure.entities.UserSystem;
import com.kynsof.identity.infrastructure.repository.query.BusinessModuleReadDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.UserPermissionBusinessReadDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.UserSystemReadDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.projections.BusinessPermissionProjection;
import com.kynsof.share.core.domain.EUserType;
import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.response.ErrorField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserMeServiceImpl implements IUserMeService {

    private static final Logger log = LoggerFactory.getLogger(UserMeServiceImpl.class);

    private final UserPermissionBusinessReadDataJPARepository userPermissionBusinessReadDataJPARepository;
    private final UserSystemReadDataJPARepository repositoryQuery;
    private final BusinessModuleReadDataJPARepository businessModuleReadDataJPARepository;

    public UserMeServiceImpl(UserPermissionBusinessReadDataJPARepository userPermissionBusinessReadDataJPARepository,
                             UserSystemReadDataJPARepository repositoryQuery,
                             BusinessModuleReadDataJPARepository businessModuleReadDataJPARepository) {
        this.userPermissionBusinessReadDataJPARepository = userPermissionBusinessReadDataJPARepository;
        this.repositoryQuery = repositoryQuery;
        this.businessModuleReadDataJPARepository = businessModuleReadDataJPARepository;
    }

    @Override
    @Cacheable(value = IdentityCacheConfig.USER_INFO_CACHE, key = "#userId", unless = "#result == null")
    public UserMeResponse getUserInfo(UUID userId) {
        long startTime = System.currentTimeMillis();

        // Query 1: Obtener usuario
        var userSystem = repositoryQuery.findByKeyCloakId(userId)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.USER_NOT_FOUND, new ErrorField("id", DomainErrorMessage.USER_NOT_FOUND.getReasonPhrase()))));

        // Query 2: Obtener permisos según tipo de usuario
        List<BusinessPermissionResponse> businessResponses;
        if (userSystem.getUserType().equals(EUserType.SUPER_ADMIN)) {
            businessResponses = getAllBusinessesWithPermissionsOptimized();
        } else {
            businessResponses = getBusinessPermissionsForUser(userSystem.getId());
        }

        UserMeResponse response = createUserMeResponse(userSystem, businessResponses);

        log.debug("getUserInfo para userId={} completado en {}ms", userId, System.currentTimeMillis() - startTime);
        return response;
    }

    @Override
    @Cacheable(value = IdentityCacheConfig.USER_INFO_CACHE, key = "#userId + '_' + #businessId", unless = "#result == null")
    public UserMeResponse getUserInfo(UUID userId, UUID businessId) {
        long startTime = System.currentTimeMillis();

        // Query 1: Obtener usuario
        var userSystem = repositoryQuery.findByKeyCloakId(userId)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.USER_NOT_FOUND, new ErrorField("id", DomainErrorMessage.USER_NOT_FOUND.getReasonPhrase()))));

        // Query 2: Obtener permisos solo del business específico
        List<BusinessPermissionResponse> businessResponses;
        if (userSystem.getUserType().equals(EUserType.SUPER_ADMIN)) {
            businessResponses = getBusinessPermissionsForSuperAdmin(businessId);
        } else {
            businessResponses = getBusinessPermissionsForUserByBusiness(userSystem.getId(), businessId);
        }

        UserMeResponse response = createUserMeResponse(userSystem, businessResponses);

        log.debug("getUserInfo para userId={} businessId={} completado en {}ms", userId, businessId, System.currentTimeMillis() - startTime);
        return response;
    }

    /**
     * Obtiene los permisos del usuario usando query optimizada con proyección.
     * Una sola query + una iteración para agregar permisos.
     */
    private List<BusinessPermissionResponse> getBusinessPermissionsForUser(UUID userId) {
        List<BusinessPermissionProjection> projections =
                userPermissionBusinessReadDataJPARepository.findBusinessPermissionsOptimized(userId);

        return aggregatePermissions(projections);
    }

    /**
     * Obtiene todos los negocios con permisos para SUPER_ADMIN usando query optimizada.
     * Solo trae negocios activos.
     */
    private List<BusinessPermissionResponse> getAllBusinessesWithPermissionsOptimized() {
        List<BusinessPermissionProjection> projections =
                businessModuleReadDataJPARepository.findAllBusinessesWithPermissionsOptimized();

        return aggregatePermissions(projections);
    }

    /**
     * Obtiene permisos de un negocio específico para SUPER_ADMIN.
     */
    private List<BusinessPermissionResponse> getBusinessPermissionsForSuperAdmin(UUID businessId) {
        List<BusinessPermissionProjection> projections =
                businessModuleReadDataJPARepository.findBusinessWithPermissionsOptimizedById(businessId);

        return aggregatePermissions(projections);
    }

    /**
     * Obtiene permisos del usuario filtrados por un negocio específico.
     */
    private List<BusinessPermissionResponse> getBusinessPermissionsForUserByBusiness(UUID userId, UUID businessId) {
        List<BusinessPermissionProjection> projections =
                userPermissionBusinessReadDataJPARepository.findBusinessPermissionsOptimizedByBusiness(userId, businessId);

        return aggregatePermissions(projections);
    }

    /**
     * Agrega proyecciones planas en BusinessPermissionResponse.
     * Una sola iteración usando LinkedHashMap para mantener el orden.
     */
    private List<BusinessPermissionResponse> aggregatePermissions(List<BusinessPermissionProjection> projections) {
        Map<UUID, BusinessPermissionResponse> responseMap = new LinkedHashMap<>();

        for (BusinessPermissionProjection projection : projections) {
            UUID businessId = projection.getBusinessId();

            responseMap.computeIfAbsent(businessId, id ->
                    new BusinessPermissionResponse(
                            id,
                            projection.getBalance(),
                            projection.getBusinessName(),
                            projection.getIdResponsible(),
                            new ArrayList<>()
                    )
            );

            // Agregar permiso si no es duplicado
            String permissionCode = projection.getPermissionCode();
            List<String> permissions = responseMap.get(businessId).getPermissions();
            if (!permissions.contains(permissionCode)) {
                permissions.add(permissionCode);
            }
        }

        return new ArrayList<>(responseMap.values());
    }

    private UserMeResponse createUserMeResponse(UserSystem userSystem, List<BusinessPermissionResponse> businessResponses) {
        return new UserMeResponse(
                userSystem.getId(),
                userSystem.getUserName(),
                userSystem.getEmail(),
                userSystem.getName(),
                userSystem.getLastName(),
                userSystem.getImage(),
                userSystem.getSelectedBusiness(),
                businessResponses
        );
    }

    /**
     * @deprecated Usar getAllBusinessesWithPermissionsOptimized() en su lugar.
     * Mantenido para compatibilidad.
     */
    @Deprecated
    public List<BusinessPermissionResponse> getAllBusinessesWithPermissions() {
        return getAllBusinessesWithPermissionsOptimized();
    }
}