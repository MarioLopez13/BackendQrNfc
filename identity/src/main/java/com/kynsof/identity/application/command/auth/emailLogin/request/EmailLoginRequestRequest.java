package com.kynsof.identity.application.command.auth.emailLogin.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailLoginRequestRequest {
    private String email;
}