package com.kynsof.identity.application.command.twofactor.verify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Verify2FARequest {
    private UUID userId;
    private String code;
}
