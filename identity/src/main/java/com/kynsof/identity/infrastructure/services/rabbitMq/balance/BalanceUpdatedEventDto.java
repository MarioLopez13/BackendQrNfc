package com.kynsof.identity.infrastructure.services.rabbitMq.balance;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para recibir eventos de actualización de balance desde payment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdatedEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonAlias("merchantId")
    private UUID businessId;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String operationType;
    private String referenceType;
    private String referenceId;
    private String description;
    private String performedBy;
}
