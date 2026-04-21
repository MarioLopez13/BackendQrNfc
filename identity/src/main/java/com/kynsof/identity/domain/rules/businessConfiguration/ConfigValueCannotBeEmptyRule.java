package com.kynsof.identity.domain.rules.businessConfiguration;

import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.rules.BusinessRule;

/**
 * Regla de negocio: El valor de configuración no puede estar vacío
 */
public class ConfigValueCannotBeEmptyRule extends BusinessRule {

    private final String configValue;

    public ConfigValueCannotBeEmptyRule(String configValue) {
        super(
                DomainErrorMessage.OBJECT_NOT_NULL,
                new ErrorField("configValue", "The configuration value cannot be empty.")
        );
        this.configValue = configValue;
    }

    @Override
    public boolean isBroken() {
        return this.configValue == null || this.configValue.trim().isEmpty();
    }
}
