package com.smartpayut.payment.client.wallet;

import java.util.UUID;

import com.smartpayut.payment.dto.wallet.WalletMovementRequest;
import com.smartpayut.payment.dto.wallet.WalletMovementResponse;
import com.smartpayut.payment.dto.wallet.WalletResponse;

public interface WalletClient {

    WalletResponse getCurrentWallet(String userBearerToken);

    WalletMovementResponse debit(WalletMovementRequest request);

    WalletMovementResponse credit(WalletMovementRequest request);

    WalletMovementResponse refund(WalletMovementRequest request);

    WalletResponse getByUserId(UUID userId);
}
