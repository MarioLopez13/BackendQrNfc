package com.smartpayut.wallet.repository;

import com.smartpayut.wallet.domain.entity.WalletMovement;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface WalletMovementRepository extends JpaRepository<WalletMovement, UUID> {
    Optional<WalletMovement> findByIdempotencyKey(String key);

    Page<WalletMovement> findByWalletId(UUID walletId, Pageable pageable);

    boolean existsByReferenceIdAndType(String referenceId, com.smartpayut.wallet.domain.enumeration.MovementType type);
}
