package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.infrastructure.entities.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TwoFactorAuthReadRepository extends JpaRepository<TwoFactorAuth, UUID>, JpaSpecificationExecutor<TwoFactorAuth> {

    Optional<TwoFactorAuth> findByUserId(UUID userId);

    @Query("SELECT t.enabled FROM TwoFactorAuth t WHERE t.userId = :userId")
    Optional<Boolean> isTwoFactorEnabled(@Param("userId") UUID userId);

    boolean existsByUserIdAndEnabled(UUID userId, boolean enabled);
}
