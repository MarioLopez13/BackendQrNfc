package com.kynsof.identity.application.command.auth.emailLogin.verify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailLoginVerifyRequest {
    private String email;
    private String code;
}