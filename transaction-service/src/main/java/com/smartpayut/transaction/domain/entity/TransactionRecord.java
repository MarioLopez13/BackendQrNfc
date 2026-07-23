package com.smartpayut.transaction.domain.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.smartpayut.transaction.domain.enumeration.TransactionSource;
import com.smartpayut.transaction.domain.enumeration.TransactionStatus;
import com.smartpayut.transaction.domain.enumeration.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "transaction_records")
public class TransactionRecord {

    @Id
    private UUID id;

    @Column(name = "correlation_id", nullable = false, unique = true, length = 150)
    private String correlationId;

    @Column(name = "source_event_id", nullable = false, length = 150)
    private String sourceEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private TransactionSource sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "user_account_id", nullable = false)
    private UUID userAccountId;

    @Column(name = "wallet_id")
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Column(length = 30)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "bus_code", length = 100)
    private String busCode;

    @Column(name = "route_name", length = 250)
    private String routeName;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected TransactionRecord() {
    }

    public TransactionRecord(String correlationId, String sourceEventId, UUID userAccountId) {
        this.id = UUID.randomUUID();
        this.correlationId = correlationId;
        this.sourceEventId = sourceEventId;
        this.userAccountId = userAccountId;
        this.amount = BigDecimal.ZERO;
        this.currency = "USD";
        this.status = TransactionStatus.PENDING;
        this.transactionType = TransactionType.PAYMENT;
        this.sourceType = TransactionSource.PAYMENT;
        this.occurredAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void applyWallet(
            String eventId,
            UUID movementId,
            UUID walletId,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String currency,
            OffsetDateTime occurredAt) {
        sourceEventId = eventId;
        if (method == null) {
            sourceType = TransactionSource.WALLET;
        }
        if (sourceId == null) {
            sourceId = movementId;
        }
        this.walletId = walletId;
        if (method == null) {
            transactionType = type;
        }
        this.amount = amount == null ? BigDecimal.ZERO : amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.currency = currency == null ? "USD" : currency;
        if (status == TransactionStatus.PENDING) {
            status = TransactionStatus.COMPLETED;
        }
        this.occurredAt = occurredAt;
    }

    public void applyPayment(
            String eventId,
            UUID paymentId,
            UUID walletId,
            TransactionType type,
            String method,
            TransactionStatus status,
            BigDecimal amount,
            String currency,
            String busCode,
            String routeName,
            String failureReason,
            OffsetDateTime occurredAt) {
        sourceEventId = eventId;
        sourceType = TransactionSource.PAYMENT;
        sourceId = paymentId;
        this.walletId = walletId;
        transactionType = type;
        this.method = method;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.busCode = busCode;
        this.routeName = routeName;
        this.failureReason = failureReason;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public TransactionSource getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public UUID getUserAccountId() {
        return userAccountId;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getMethod() {
        return method;
    }

    public TransactionStatus getStatus() {
        return status;
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

    public String getCurrency() {
        return currency;
    }

    public String getBusCode() {
        return busCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
