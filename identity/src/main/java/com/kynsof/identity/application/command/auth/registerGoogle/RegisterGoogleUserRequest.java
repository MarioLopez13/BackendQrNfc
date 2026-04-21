package com.kynsof.identity.application.command.auth.registerGoogle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterGoogleUserRequest {
    private String code;
    private String redirectUri;
}
