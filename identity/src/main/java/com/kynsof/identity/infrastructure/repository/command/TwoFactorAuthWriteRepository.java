package com.kynsof.identity.infrastructure.repository.command;

import com.kynsof.identity.infrastructure.entities.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TwoFactorAuthWriteRepository extends JpaRepository<TwoFactorAuth, UUID> {

    @Modifying
    @Query("UPDATE TwoFactorAuth t SET t.enabled = :enabled, t.enabledAt = :enabledAt, t.verified = true WHERE t.userId = :userId")
    void enableTwoFactor(@Param("userId") UUID userId, @Param("enabled") boolean enabled, @Param("enabledAt") LocalDateTime enabledAt);

    @Modifying
    @Query("UPDATE TwoFactorAuth t SET t.failedAttempts = :attempts, t.lockedUntil = :lockedUntil WHERE t.userId = :userId")
    void updateFailedAttempts(@Param("userId") UUID userId, @Param("attempts") int attempts, @Param("lockedUntil") LocalDateTime lockedUntil);

    @Modifying
    @Query("DELETE FROM TwoFactorAuth t WHERE t.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
