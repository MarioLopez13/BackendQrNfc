package com.kynsof.identity.application.query.businessConfiguration.getById;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Query para buscar una configuración de negocio por ID
 */
@Getter
@AllArgsConstructor
public class FindBusinessConfigurationByIdQuery implements IQuery {
    private final UUID id;
}
