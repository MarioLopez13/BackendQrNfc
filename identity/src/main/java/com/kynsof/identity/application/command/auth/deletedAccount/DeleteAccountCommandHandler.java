package com.kynsof.identity.application.command.auth.deletedAccount;


import com.kynsof.identity.application.command.auth.autenticate.LoginRequest;
import com.kynsof.identity.application.command.auth.autenticate.TokenResponse;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteAccountCommandHandler implements ICommandHandler<DeleteAccountCommand> {
    private final IAuthService authService;
    private final IUserSystemService userSystemService;

    public DeleteAccountCommandHandler(IAuthService authService, IUserSystemService userSystemService) {

        this.authService = authService;
        this.userSystemService = userSystemService;

    }

    @Override
    public void handle(DeleteAccountCommand command) {
        TokenResponse tokenResponse = authService.authenticate(new LoginRequest(command.getEmail(), command.getPassword()));
        UserSystemDto userSystemDto = userSystemService.findByEmail(command.getEmail());
        if (userSystemDto != null) {
            Boolean result = authService.delete(userSystemDto.getKeyCloakId().toString());
            userSystemService.delete(userSystemDto);
            command.setResul(result);
        }

    }
}
