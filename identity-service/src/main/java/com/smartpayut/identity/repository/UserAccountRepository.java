package com.smartpayut.identity.repository;

import com.smartpayut.identity.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID>, JpaSpecificationExecutor<UserAccount> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUserNameIgnoreCase(String userName);

    Optional<UserAccount> findByKeycloakId(UUID keycloakId);

    Optional<UserAccount> findByEmailIgnoreCase(String email);
}
