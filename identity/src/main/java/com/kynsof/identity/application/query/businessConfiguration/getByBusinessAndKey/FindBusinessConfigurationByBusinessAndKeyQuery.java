package com.kynsof.identity.application.query.businessConfiguration.getByBusinessAndKey;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Query para buscar una configuración por businessId y configKey
 */
@Getter
@AllArgsConstructor
public class FindBusinessConfigurationByBusinessAndKeyQuery implements IQuery {
    private final UUID businessId;
    private final String configKey;
}
