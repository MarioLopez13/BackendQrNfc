package com.kynsof.identity.domain.rules.businessConfiguration;

import com.kynsof.identity.domain.interfaces.service.IBusinessConfigurationService;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.rules.BusinessRule;

import java.util.UUID;

/**
 * Regla de negocio: La clave de configuración debe ser única por Business
 */
public class ConfigKeyMustBeUniqueForBusinessRule extends BusinessRule {

    private final IBusinessConfigurationService service;
    private final UUID businessId;
    private final String configKey;
    private final UUID id;

    public ConfigKeyMustBeUniqueForBusinessRule(
            IBusinessConfigurationService service,
            UUID businessId,
            String configKey,
            UUID id) {
        super(
                DomainErrorMessage.ITEM_ALREADY_EXITS,
                new ErrorField("configKey", "A configuration with this key already exists for this business.")
        );
        this.service = service;
        this.businessId = businessId;
        this.configKey = configKey;
        this.id = id;
    }

    @Override
    public boolean isBroken() {
        return this.service.countByBusinessIdAndConfigKeyAndNotId(businessId, configKey, id) > 0;
    }
}
