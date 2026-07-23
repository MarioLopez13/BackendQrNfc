package com.smartpayut.wallet.service;

import com.smartpayut.wallet.domain.entity.Wallet;
import com.smartpayut.wallet.dto.response.*;
import com.smartpayut.wallet.exception.WalletException;
import com.smartpayut.wallet.mapper.*;
import com.smartpayut.wallet.repository.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WalletQueryService {
    private final WalletRepository wallets;
    private final WalletMovementRepository movements;
    private final WalletMapper mapper;
    private final WalletMovementMapper movementMapper;

    public WalletQueryService(WalletRepository w, WalletMovementRepository m, WalletMapper map,
            WalletMovementMapper mm) {
        wallets = w;
        movements = m;
        mapper = map;
        movementMapper = mm;
    }

    public WalletResponse me(String subject) {
        return mapper.wallet(bySubject(subject));
    }

    public BalanceResponse balance(String subject) {
        return mapper.balance(bySubject(subject));
    }

    public WalletResponse byUser(UUID userId) {
        return mapper.wallet(wallets.findByUserId(userId)
                .orElseThrow(() -> new WalletException(HttpStatus.NOT_FOUND, "Wallet no encontrada")));
    }

    public PageResponse<MovementResponse> movements(String subject, int page, int size) {
        Wallet w = bySubject(subject);
        Page<com.smartpayut.wallet.domain.entity.WalletMovement> p = movements.findByWalletId(w.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return new PageResponse<>(p.stream().map(movementMapper::response).toList(), p.getTotalElements(),
                p.getNumber(), p.getSize(), p.getTotalPages());
    }

    private Wallet bySubject(String subject) {
        try {
            return wallets.findByKeycloakId(UUID.fromString(subject))
                    .orElseThrow(() -> new WalletException(HttpStatus.NOT_FOUND, "Wallet no encontrada"));
        } catch (IllegalArgumentException e) {
            throw new WalletException(HttpStatus.UNAUTHORIZED, "JWT sin subject válido");
        }
    }
}
