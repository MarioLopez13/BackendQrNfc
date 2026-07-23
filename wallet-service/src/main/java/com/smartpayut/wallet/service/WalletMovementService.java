package com.smartpayut.wallet.service;

import com.smartpayut.wallet.domain.entity.*;
import com.smartpayut.wallet.domain.enumeration.*;
import com.smartpayut.wallet.dto.request.BalanceOperationRequest;
import com.smartpayut.wallet.dto.response.MovementResponse;
import com.smartpayut.wallet.event.WalletEvent;
import com.smartpayut.wallet.exception.WalletException;
import com.smartpayut.wallet.mapper.WalletMovementMapper;
import com.smartpayut.wallet.messaging.publisher.WalletEventPublisher;
import com.smartpayut.wallet.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class WalletMovementService {
    private final WalletRepository wallets;
    private final WalletMovementRepository movements;
    private final WalletMovementMapper mapper;
    private final WalletEventPublisher publisher;

    public WalletMovementService(WalletRepository w, WalletMovementRepository m, WalletMovementMapper map,
            WalletEventPublisher p) {
        wallets = w;
        movements = m;
        mapper = map;
        publisher = p;
    }

    @Transactional
    public MovementResponse debit(BalanceOperationRequest r) {
        MovementType type = r.movementType() == MovementType.ADJUSTMENT_DEBIT ? MovementType.ADJUSTMENT_DEBIT
                : MovementType.PAYMENT_DEBIT;
        return apply(r, type, false, "wallet.debited");
    }

    @Transactional
    public MovementResponse credit(BalanceOperationRequest r) {
        MovementType type = r.movementType() == MovementType.TOP_UP ? MovementType.TOP_UP
                : MovementType.ADJUSTMENT_CREDIT;
        return apply(r, type, true, "wallet.credited");
    }

    @Transactional
    public MovementResponse refund(BalanceOperationRequest r) {
        if (r.referenceId() == null || r.referenceId().isBlank())
            throw new WalletException(HttpStatus.BAD_REQUEST, "referenceId es obligatorio");
        if (!movements.existsByReferenceIdAndType(r.referenceId(), MovementType.PAYMENT_DEBIT))
            throw new WalletException(HttpStatus.CONFLICT, "No existe el débito original");
        return apply(r, MovementType.PAYMENT_REFUND, true, "wallet.refunded");
    }

    private MovementResponse apply(BalanceOperationRequest r, MovementType type, boolean add, String eventType) {
        if (r.amount() == null || r.amount().signum() <= 0)
            throw new WalletException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor que cero");
        var old = movements.findByIdempotencyKey(r.idempotencyKey());
        if (old.isPresent()) {
            if (!old.get().getUserId().equals(r.userId()))
                throw new WalletException(HttpStatus.CONFLICT, "idempotencyKey pertenece a otra Wallet");
            return mapper.response(old.get());
        }
        Wallet w = wallets.findByUserIdForUpdate(r.userId())
                .orElseThrow(() -> new WalletException(HttpStatus.NOT_FOUND, "Wallet no encontrada"));
        if (w.getStatus() != WalletStatus.ACTIVE)
            throw new WalletException(HttpStatus.CONFLICT, "Wallet no está activa");
        BigDecimal before = w.getBalance(), after = add ? before.add(r.amount()) : before.subtract(r.amount());
        if (after.signum() < 0)
            throw new WalletException(HttpStatus.CONFLICT, "Saldo insuficiente");
        WalletMovement movement = movements.saveAndFlush(new WalletMovement(UUID.randomUUID(), w, type, r.amount(),
                before, after, r.referenceId(), r.idempotencyKey(), r.description()));
        w.changeBalance(after);
        wallets.saveAndFlush(w);
        publisher.publish(new WalletEvent(UUID.randomUUID(), eventType, 1, OffsetDateTime.now(), w.getId(),
                w.getUserId(), movement.getId(), movement.getAmount(), before, after, w.getCurrency(), r.referenceId(),
                r.idempotencyKey()));
        return mapper.response(movement);
    }
}
