package com.kynsof.identity.application.command.auth.registry;


import com.kynsof.identity.domain.dto.UserStatus;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.share.core.domain.EUserType;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistryCommandHandler implements ICommandHandler<RegistryCommand> {
    private final IAuthService authService;
    private final IUserSystemService userSystemService;

    public RegistryCommandHandler(IAuthService authService, IUserSystemService userSystemService) {
        this.authService = authService;
        this.userSystemService = userSystemService;
    }

    @Override
    public void handle(RegistryCommand command) {
        String registerUser = authService.registerUser(new UserRequest(
                command.getUsername(), command.getEmail(), command.getFirstname(),
                command.getLastname(), command.getPassword()
        ), false);
        command.setResul(registerUser);

        UserSystemDto userDto = new UserSystemDto(
                UUID.fromString(registerUser),
                command.getUsername(),
                command.getEmail(),
                command.getFirstname(),
                command.getLastname(),
                UserStatus.ACTIVE,
                ""
        );
        userDto.setKeyCloakId(UUID.fromString(registerUser));
        userDto.setUserType(EUserType.PATIENTS);

        UUID id = userSystemService.create(userDto);
    }


}
