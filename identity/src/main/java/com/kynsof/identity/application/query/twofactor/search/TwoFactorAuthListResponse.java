package com.kynsof.identity.application.query.twofactor.search;

import com.kynsof.identity.domain.dto.TwoFactorAuthDto;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class TwoFactorAuthListResponse implements IResponse, Serializable {

    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userName;
    private boolean enabled;
    private boolean verified;
    private int failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime enabledAt;
    private LocalDateTime createdAt;

    public TwoFactorAuthListResponse(TwoFactorAuthDto dto) {
        this.id = dto.getId();
        this.userId = dto.getUserId();
        this.enabled = dto.isEnabled();
        this.verified = dto.isVerified();
        this.failedAttempts = dto.getFailedAttempts();
        this.lockedUntil = dto.getLockedUntil();
        this.enabledAt = dto.getEnabledAt();
        this.createdAt = dto.getCreatedAt();
    }

    public TwoFactorAuthListResponse(TwoFactorAuthDto dto, String userEmail, String userName) {
        this(dto);
        this.userEmail = userEmail;
        this.userName = userName;
    }
}
