package com.kynsof.identity.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuthDto {

    private UUID id;
    private UUID userId;
    private String secretKey;
    private boolean enabled;
    private boolean verified;
    private String backupCodes;
    private int failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime enabledAt;
    private LocalDateTime createdAt;

    public TwoFactorAuthDto(UUID userId, String secretKey) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.secretKey = secretKey;
        this.enabled = false;
        this.verified = false;
        this.failedAttempts = 0;
    }
}
