package com.smartpayut.payment.domain.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.smartpayut.payment.domain.enumeration.PaymentMethod;
import com.smartpayut.payment.domain.enumeration.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @Column(name = "user_account_id", nullable = false)
    private UUID userAccountId;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

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

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 150)
    private String idempotencyKey;

    @Column(name = "external_reference", length = 150)
    private String externalReference;

    @Column(name = "placetopay_request_id", unique = true)
    private Long placeToPayRequestId;

    @Column(name = "placetopay_process_url", length = 1000)
    private String placeToPayProcessUrl;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;

    protected Payment() {
    }

    public Payment(UUID userAccountId, UUID walletId, PaymentMethod method, BigDecimal amount,
            String idempotencyKey, String busCode, String routeName) {
        this.id = UUID.randomUUID();
        this.userAccountId = userAccountId;
        this.walletId = walletId;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.amount = amount;
        this.currency = "USD";
        this.idempotencyKey = idempotencyKey;
        this.busCode = busCode;
        this.routeName = routeName;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
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

    public void processing() {
        status = PaymentStatus.PROCESSING;
    }

    public void complete(BigDecimal previousBalance, BigDecimal updatedBalance) {
        status = PaymentStatus.COMPLETED;
        balanceBefore = previousBalance;
        balanceAfter = updatedBalance;
        completedAt = OffsetDateTime.now(ZoneOffset.UTC);
        failureReason = null;
    }

    public void fail(String reason) {
        status = PaymentStatus.FAILED;
        failureReason = reason;
    }

    public void refund() {
        status = PaymentStatus.REFUNDED;
        refundedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void assignPlaceToPaySession(long requestId, String processUrl, String reference) {
        placeToPayRequestId = requestId;
        placeToPayProcessUrl = processUrl;
        externalReference = reference;
        status = PaymentStatus.PROCESSING;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserAccountId() {
        return userAccountId;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public Long getPlaceToPayRequestId() {
        return placeToPayRequestId;
    }

    public String getPlaceToPayProcessUrl() {
        return placeToPayProcessUrl;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public OffsetDateTime getRefundedAt() {
        return refundedAt;
    }
}
