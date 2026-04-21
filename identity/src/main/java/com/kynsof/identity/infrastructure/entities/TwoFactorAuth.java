package com.kynsof.identity.infrastructure.entities;

import com.kynsof.identity.domain.dto.TwoFactorAuthDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "two_factor_auth",
        schema = "identity",
        indexes = {
                @Index(name = "idx_2fa_user_id", columnList = "user_id"),
                @Index(name = "idx_2fa_enabled", columnList = "enabled")
        }
)
public class TwoFactorAuth implements Serializable {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "secret_key", nullable = false, length = 64)
    private String secretKey;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "backup_codes", length = 500)
    private String backupCodes;

    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;

    public TwoFactorAuth(TwoFactorAuthDto dto) {
        this.id = dto.getId();
        this.userId = dto.getUserId();
        this.secretKey = dto.getSecretKey();
        this.enabled = dto.isEnabled();
        this.verified = dto.isVerified();
        this.backupCodes = dto.getBackupCodes();
        this.failedAttempts = dto.getFailedAttempts();
        this.lockedUntil = dto.getLockedUntil();
        this.enabledAt = dto.getEnabledAt();
    }

    public TwoFactorAuthDto toAggregate() {
        return new TwoFactorAuthDto(
                this.id,
                this.userId,
                this.secretKey,
                this.enabled,
                this.verified,
                this.backupCodes,
                this.failedAttempts,
                this.lockedUntil,
                this.enabledAt,
                this.createdAt
        );
    }
}
