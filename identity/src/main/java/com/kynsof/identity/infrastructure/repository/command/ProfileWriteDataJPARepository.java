package com.kynsof.identity.infrastructure.repository.command;

import com.kynsof.identity.infrastructure.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileWriteDataJPARepository extends JpaRepository<Profile, UUID> {
}
