package com.smartpayut.wallet.mapper;

import com.smartpayut.wallet.domain.entity.WalletMovement;
import com.smartpayut.wallet.dto.response.MovementResponse;
import org.springframework.stereotype.Component;

@Component
public class WalletMovementMapper {
    public MovementResponse response(WalletMovement m) {
        boolean credit = switch (m.getType()) {
        case TOP_UP, PAYMENT_REFUND, ADJUSTMENT_CREDIT -> true;
        default -> false;
        };
        String title = credit ? (m.getType() == com.smartpayut.wallet.domain.enumeration.MovementType.TOP_UP
                ? "Recarga de saldo" : "Crédito de saldo") : "Pago de transporte",
                subtitle = m.getDescription() == null ? "WALLET" : m.getDescription();
        return new MovementResponse(m.getId(), m.getId(), title, subtitle, title, subtitle, m.getAmount(),
                m.getCreatedAt(), m.getCreatedAt(), m.getType().name(), "Completado", m.getType(), m.getBalanceBefore(),
                m.getBalanceAfter(), m.getReferenceId(), m.getIdempotencyKey());
    }
}
