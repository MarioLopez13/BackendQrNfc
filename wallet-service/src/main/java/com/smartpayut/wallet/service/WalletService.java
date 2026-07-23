package com.smartpayut.wallet.service;

import com.smartpayut.wallet.domain.entity.*;
import com.smartpayut.wallet.event.*;
import com.smartpayut.wallet.exception.WalletException;
import com.smartpayut.wallet.messaging.publisher.WalletEventPublisher;
import com.smartpayut.wallet.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository wallets;
    private final ProcessedEventRepository processed;
    private final WalletEventPublisher publisher;

    public WalletService(WalletRepository w, ProcessedEventRepository p, WalletEventPublisher e) {
        wallets = w;
        processed = p;
        publisher = e;
    }

    @Transactional
    public boolean createFrom(IdentityUserCreatedEvent e) {
        if (e == null || e.eventId() == null || e.userId() == null || e.keycloakId() == null)
            throw new WalletException(HttpStatus.BAD_REQUEST, "Evento Identity incompleto");
        if (!"identity.user.created".equals(e.eventType()) || e.eventVersion() != 1)
            throw new WalletException(HttpStatus.UNPROCESSABLE_ENTITY, "Evento Identity no compatible");
        if (processed.existsById(e.eventId()))
            return false;
        if (wallets.existsByUserId(e.userId()) || wallets.existsByKeycloakId(e.keycloakId())) {
            processed.save(new ProcessedEvent(e.eventId(), e.eventType(), e.eventVersion()));
            return false;
        }
        try {
            Wallet w = wallets.saveAndFlush(new Wallet(UUID.randomUUID(), e.userId(), e.keycloakId()));
            publisher.publish(new WalletEvent(UUID.randomUUID(), "wallet.created", 1, OffsetDateTime.now(), w.getId(),
                    w.getUserId(), null, null, null, w.getBalance(), w.getCurrency(), null, null));
            processed.saveAndFlush(new ProcessedEvent(e.eventId(), e.eventType(), e.eventVersion()));
            return true;
        } catch (DataIntegrityViolationException x) {
            throw new WalletException(HttpStatus.CONFLICT, "Wallet duplicada");
        }
    }
}
