package com.kynsof.identity.application.command.userPermisionBusiness.create;

import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.dto.PermissionDto;
import com.kynsof.identity.domain.dto.UserPermissionBusinessDto;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.identity.domain.interfaces.service.IPermissionService;
import com.kynsof.identity.domain.interfaces.service.IUserPermissionBusinessService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.identity.infrastructure.config.IdentityCacheConfig;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CreateUserPermissionBusinessCommandHandler implements ICommandHandler<CreateUserPermissionBusinessCommand> {

    private final IUserPermissionBusinessService service;
    private final IPermissionService permissionService;
    private final IBusinessService businessService;
    private final IUserSystemService userSystemService;
    private final CacheManager cacheManager;

    public CreateUserPermissionBusinessCommandHandler(IUserPermissionBusinessService service,
                                                      IPermissionService permissionService,
                                                      IBusinessService businessService,
                                                      IUserSystemService userSystemService,
                                                      CacheManager cacheManager) {
        this.service = service;
        this.permissionService = permissionService;
        this.businessService = businessService;
        this.userSystemService = userSystemService;
        this.cacheManager = cacheManager;
    }

    @Override
    public void handle(CreateUserPermissionBusinessCommand command) {
        List<UserPermissionBusinessDto> userRoleBusinessDtos = new ArrayList<>();
        UserPermissionBusinessRequest userRoleBusinessRequest = command.getPayload();
        UserSystemDto userSystemDto = this.userSystemService.findById(userRoleBusinessRequest.getUserId());
        BusinessDto businessDto = this.businessService.findById(userRoleBusinessRequest.getBusinessId());

        service.delete(userRoleBusinessRequest.getBusinessId(), userRoleBusinessRequest.getUserId());
        for (UUID role : userRoleBusinessRequest.getPermissionIds()) {
            PermissionDto permissionDto = this.permissionService.findById(role);
            userRoleBusinessDtos.add(new UserPermissionBusinessDto(UUID.randomUUID(), userSystemDto, permissionDto, businessDto));
        }

        this.service.create(userRoleBusinessDtos);

        // Invalidar caché del UserMe
        evictUserInfoCache(userSystemDto.getKeyCloakId());
    }

    private void evictUserInfoCache(UUID keyCloakId) {
        var cache = cacheManager.getCache(IdentityCacheConfig.USER_INFO_CACHE);
        if (cache != null && keyCloakId != null) {
            cache.evict(keyCloakId);
        }
    }
}