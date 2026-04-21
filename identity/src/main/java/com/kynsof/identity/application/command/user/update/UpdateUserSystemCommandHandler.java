package com.kynsof.identity.application.command.user.update;

import com.kynsof.identity.application.command.auth.registry.UserRequest;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.identity.infrastructure.config.IdentityCacheConfig;
import com.kynsof.share.core.domain.RulesChecker;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.domain.rules.ValidateObjectNotNullRule;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserSystemCommandHandler implements ICommandHandler<UpdateUserSystemCommand> {

    private final IUserSystemService systemService;
    private final IAuthService keycloakProvider;
    private final CacheManager cacheManager;

    public UpdateUserSystemCommandHandler(IUserSystemService systemService,
                                          IAuthService keycloakProvider,
                                          CacheManager cacheManager) {
        this.systemService = systemService;
        this.keycloakProvider = keycloakProvider;
        this.cacheManager = cacheManager;
    }

    @Override
    public void handle(UpdateUserSystemCommand command) {
        // Validación inicial
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getId(), "id", "UserSystem ID cannot be null."));

        // Recuperación del usuario a actualizar
        UserSystemDto objectToUpdate = this.systemService.findById(command.getId());

        changeValueKeycloack(command, objectToUpdate);

        updateUserSystems(command, objectToUpdate);
    }

    private void updateUserSystems(UpdateUserSystemCommand command, UserSystemDto objectToUpdate) {
        if (command.getEmail() != null) {
            objectToUpdate.setEmail(command.getEmail());
        }
        if (command.getName() != null) {
            objectToUpdate.setName(command.getName());
        }
        if (command.getLastName() != null) {
            objectToUpdate.setLastName(command.getLastName());
        }
        if (command.getImage() != null) {
            objectToUpdate.setImage(command.getImage());
        }
        if (command.getUserType() != null) {
            objectToUpdate.setUserType(command.getUserType());
        }

        systemService.update(objectToUpdate);

        // Invalidar caché del UserMe
        evictUserInfoCache(objectToUpdate.getKeyCloakId());
    }

    private void evictUserInfoCache(java.util.UUID keyCloakId) {
        var cache = cacheManager.getCache(IdentityCacheConfig.USER_INFO_CACHE);
        if (cache != null && keyCloakId != null) {
            cache.evict(keyCloakId);
        }
    }

    private void changeValueKeycloack(UpdateUserSystemCommand command, UserSystemDto objectToUpdate) {
        if (!objectToUpdate.getEmail().equals(command.getEmail()) ||
                !objectToUpdate.getName().equals(command.getName()) ||
                !objectToUpdate.getLastName().equals(command.getLastName())) {

            UserRequest userRequest = new UserRequest(
                    command.getUserName(),
                    command.getEmail(),
                    command.getName(),
                    command.getLastName(),
                    ""
            );
            keycloakProvider.updateUser(objectToUpdate.getKeyCloakId().toString(), userRequest);
        }
    }
}