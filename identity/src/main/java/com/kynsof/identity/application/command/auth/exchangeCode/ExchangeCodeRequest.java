package com.kynsof.identity.application.command.auth.exchangeCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeCodeRequest {
    private String code;
    private String redirectUri;

}
