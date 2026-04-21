package com.kynsof.identity.infrastructure.repository.command;

import com.kynsof.identity.infrastructure.entities.GeographicLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GeographicLocationWriteDataJPARepository extends JpaRepository<GeographicLocation, UUID> {
}
