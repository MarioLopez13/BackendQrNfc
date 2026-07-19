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

import java.util.Objects;
import java.util.UUID;
@Component
public class UpdateUserSystemCommandHandler
        implements ICommandHandler<UpdateUserSystemCommand> {

    private final IUserSystemService systemService;
    private final IAuthService keycloakProvider;
    private final CacheManager cacheManager;

    public UpdateUserSystemCommandHandler(
            IUserSystemService systemService,
            IAuthService keycloakProvider,
            CacheManager cacheManager
    ) {
        this.systemService = systemService;
        this.keycloakProvider = keycloakProvider;
        this.cacheManager = cacheManager;
    }

    @Override
    public void handle(UpdateUserSystemCommand command) {

        RulesChecker.checkRule(
                new ValidateObjectNotNullRule<>(
                        command.getId(),
                        "id",
                        "UserSystem ID cannot be null."
                )
        );

        UserSystemDto objectToUpdate =
                systemService.findById(command.getId());

        updateKeycloakIfRequired(command, objectToUpdate);
        updateUserSystem(command, objectToUpdate);
    }

    private void updateUserSystem(
            UpdateUserSystemCommand command,
            UserSystemDto objectToUpdate
    ) {
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

        if (command.getStatus() != null) {
            objectToUpdate.setStatus(command.getStatus());
        }

        systemService.update(objectToUpdate);
        evictUserInfoCache(objectToUpdate.getKeyCloakId());
    }

    private void updateKeycloakIfRequired(
            UpdateUserSystemCommand command,
            UserSystemDto currentUser
    ) {
        boolean userNameChanged =
                command.getUserName() != null
                        && !Objects.equals(
                                currentUser.getUserName(),
                                command.getUserName()
                        );

        boolean emailChanged =
                command.getEmail() != null
                        && !Objects.equals(
                                currentUser.getEmail(),
                                command.getEmail()
                        );

        boolean nameChanged =
                command.getName() != null
                        && !Objects.equals(
                                currentUser.getName(),
                                command.getName()
                        );

        boolean lastNameChanged =
                command.getLastName() != null
                        && !Objects.equals(
                                currentUser.getLastName(),
                                command.getLastName()
                        );

        if (!userNameChanged
                && !emailChanged
                && !nameChanged
                && !lastNameChanged) {
            return;
        }

        UserRequest userRequest = new UserRequest(
                command.getUserName() != null
                        ? command.getUserName()
                        : currentUser.getUserName(),

                command.getEmail() != null
                        ? command.getEmail()
                        : currentUser.getEmail(),

                command.getName() != null
                        ? command.getName()
                        : currentUser.getName(),

                command.getLastName() != null
                        ? command.getLastName()
                        : currentUser.getLastName(),

                ""
        );

        keycloakProvider.updateUser(
                currentUser.getKeyCloakId().toString(),
                userRequest
        );
    }

    private void evictUserInfoCache(UUID keyCloakId) {
        var cache = cacheManager.getCache(
                IdentityCacheConfig.USER_INFO_CACHE
        );

        if (cache != null && keyCloakId != null) {
            cache.evict(keyCloakId);
        }
    }
}