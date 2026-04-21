package com.kynsof.identity.application.command.twofactor.setup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Setup2FARequest {
    private UUID userId;
    private String email;
}
