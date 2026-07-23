package com.smartpayut.wallet.domain.entity;

import com.smartpayut.wallet.domain.enumeration.MovementType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_movement")
public class WalletMovement {
    @Id
    private UUID id;
    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType type;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(name = "balance_before", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceBefore;
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;
    @Column(name = "reference_id")
    private String referenceId;
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;
    private String description;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected WalletMovement() {
    }

    public WalletMovement(UUID id, Wallet w, MovementType type, BigDecimal amount, BigDecimal before, BigDecimal after,
            String referenceId, String key, String description) {
        this.id = id;
        walletId = w.getId();
        userId = w.getUserId();
        this.type = type;
        this.amount = amount.setScale(2);
        balanceBefore = before;
        balanceAfter = after;
        this.referenceId = referenceId;
        idempotencyKey = key;
        this.description = description;
        createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public UUID getUserId() {
        return userId;
    }

    public MovementType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
