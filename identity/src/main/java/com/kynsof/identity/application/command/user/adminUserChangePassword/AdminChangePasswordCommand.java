package com.kynsof.identity.application.command.user.adminUserChangePassword;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminChangePasswordCommand implements ICommand {
    private Boolean resul;
    private String userId;
    private final String newPassword;
    private final boolean changePassword;

    public AdminChangePasswordCommand(String userId,  String newPassword, boolean changePassword) {
        this.userId = userId;

        this.newPassword = newPassword;
        this.changePassword = changePassword;
    }


    public static AdminChangePasswordCommand fromRequest( AdminChangePasswordRequest request) {
        return new AdminChangePasswordCommand(request.getUserId(),
                request.getNewPassword(), request.isChangePassword());
    }


    @Override
    public ICommandMessage getMessage() {
        return new AdminChangePasswordMessage(resul);
    }
}
