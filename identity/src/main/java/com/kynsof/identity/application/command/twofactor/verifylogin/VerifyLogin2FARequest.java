package com.kynsof.identity.application.command.twofactor.verifylogin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyLogin2FARequest {
    private UUID userId;
    private String code;
    private boolean backupCode;
}
