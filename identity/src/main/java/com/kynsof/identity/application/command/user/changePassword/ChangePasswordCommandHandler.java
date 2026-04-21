package com.kynsof.identity.application.command.user.changePassword;


import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordCommandHandler implements ICommandHandler<ChangePasswordCommand> {
    private final IAuthService authService;
    private final IUserSystemService userSystemService;

    public ChangePasswordCommandHandler(IAuthService authService, IUserSystemService userSystemService) {

        this.authService = authService;
        this.userSystemService = userSystemService;
    }

    @Override
    public void handle(ChangePasswordCommand command) {
        UserSystemDto userSystemDto = userSystemService.findById(command.getId());
        Boolean result = authService.changePassword(userSystemDto.getKeyCloakId().toString(), command.getNewPassword(),false);
        command.setResul(result);
    }
}
