package com.kynsof.identity.application.command.twofactor.disable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Disable2FARequest {
    private UUID userId;
    private String code;
}
