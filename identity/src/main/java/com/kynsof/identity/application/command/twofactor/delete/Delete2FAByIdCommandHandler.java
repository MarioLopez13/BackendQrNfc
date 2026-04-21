package com.kynsof.identity.application.command.twofactor.delete;

import com.kynsof.identity.domain.interfaces.service.ITwoFactorAuthService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;

@Component
public class Delete2FAByIdCommandHandler implements ICommandHandler<Delete2FAByIdCommand> {

    private final ITwoFactorAuthService service;

    public Delete2FAByIdCommandHandler(ITwoFactorAuthService service) {
        this.service = service;
    }

    @Override
    public void handle(Delete2FAByIdCommand command) {
        service.deleteById(command.getId());
        command.setMessage(Delete2FAByIdMessage.success(command.getId()));
    }
}
