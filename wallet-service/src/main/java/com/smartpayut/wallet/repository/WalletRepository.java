package com.smartpayut.wallet.repository;

import com.smartpayut.wallet.domain.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.userId=:userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") UUID userId);

    Optional<Wallet> findByUserId(UUID userId);

    Optional<Wallet> findByKeycloakId(UUID keycloakId);

    boolean existsByUserId(UUID id);

    boolean existsByKeycloakId(UUID id);
}
