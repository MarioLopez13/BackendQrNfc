package com.kynsof.identity.infrastructure.repository.query.projections;

import java.util.UUID;

/**
 * Proyección para obtener datos de negocio y permisos de forma eficiente.
 * Cada fila representa un business-permission individual.
 */
public interface BusinessPermissionProjection {
    UUID getBusinessId();
    String getBusinessName();
    Double getBalance();
    UUID getIdResponsible();
    String getPermissionCode();
}
