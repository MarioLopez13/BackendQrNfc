package com.smartpayut.wallet.domain.entity;

import com.smartpayut.wallet.domain.enumeration.WalletStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;
    @Column(name = "keycloak_id", nullable = false, unique = true)
    private UUID keycloakId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    @Version
    private long version;

    protected Wallet() {
    }

    public Wallet(UUID id, UUID userId, UUID keycloakId) {
        this.id = id;
        this.userId = userId;
        this.keycloakId = keycloakId;
        balance = new BigDecimal("0.00");
        currency = "USD";
        status = WalletStatus.ACTIVE;
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    public void changeBalance(BigDecimal value) {
        if (value.signum() < 0)
            throw new IllegalArgumentException("Saldo negativo");
        balance = value.setScale(2);
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getKeycloakId() {
        return keycloakId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
