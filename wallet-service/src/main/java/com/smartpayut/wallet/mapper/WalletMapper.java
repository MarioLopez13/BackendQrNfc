package com.smartpayut.wallet.mapper;

import com.smartpayut.wallet.domain.entity.Wallet;
import com.smartpayut.wallet.dto.response.*;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {
    public WalletResponse wallet(Wallet w) {
        return new WalletResponse(w.getId(), w.getUserId(), w.getBalance(), w.getCurrency(), w.getStatus(),
                w.getCreatedAt(), w.getUpdatedAt());
    }

    public BalanceResponse balance(Wallet w) {
        return new BalanceResponse(w.getUserId(), w.getBalance(), w.getCurrency());
    }
}
