package com.kynsof.identity.infrastructure.repository.command;

import com.kynsof.identity.infrastructure.entities.BusinessConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio de escritura para BusinessConfiguration.
 * Maneja operaciones de creación, actualización y eliminación.
 */
@Repository
public interface BusinessConfigurationWriteDataJPARepository extends JpaRepository<BusinessConfiguration, UUID> {
}