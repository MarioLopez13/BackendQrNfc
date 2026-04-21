package com.kynsof.identity.application.command.user.adminUserChangePassword;


import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminChangePasswordCommandHandler implements ICommandHandler<AdminChangePasswordCommand> {
    private final IAuthService authService;
    private final IUserSystemService userSystemService;
    public AdminChangePasswordCommandHandler(IAuthService authService, IUserSystemService userSystemService) {

        this.authService = authService;
        this.userSystemService = userSystemService;

    }

    @Override
    public void handle(AdminChangePasswordCommand command) {
        UserSystemDto userSystemDto = userSystemService.findById(UUID.fromString(command.getUserId()));
        Boolean result = authService.changePassword(userSystemDto.getKeyCloakId().toString(), command.getNewPassword(),
                false);
        command.setResul(result);
    }
}
