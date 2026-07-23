package com.smartpayut.identity.repository;

import com.smartpayut.identity.domain.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserAccountId(UUID userAccountId);
}
