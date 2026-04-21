package com.kynsof.identity.application.command.user.adminUserChangePassword;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminChangePasswordRequest {
    private String userId;
    private String newPassword;
    private boolean changePassword;
}
