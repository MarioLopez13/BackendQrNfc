package com.kynsof.identity.application.query.businessConfiguration.getByKey;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Query para buscar una configuración solo por configKey
 */
@Getter
@AllArgsConstructor
public class FindBusinessConfigurationByKeyQuery implements IQuery {
    private final String configKey;
}
