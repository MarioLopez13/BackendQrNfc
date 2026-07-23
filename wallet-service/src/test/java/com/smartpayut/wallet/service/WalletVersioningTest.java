package com.smartpayut.wallet.service;

import com.smartpayut.wallet.domain.entity.Wallet;
import com.smartpayut.wallet.repository.WalletRepository;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import static org.junit.jupiter.api.Assertions.*;

class WalletVersioningTest {
    @Test
    void walletUsaOptimisticLocking() throws Exception {
        assertNotNull(Wallet.class.getDeclaredField("version").getAnnotation(Version.class));
        assertEquals(LockModeType.PESSIMISTIC_WRITE, WalletRepository.class
                .getMethod("findByUserIdForUpdate", java.util.UUID.class).getAnnotation(Lock.class).value());
    }
}
