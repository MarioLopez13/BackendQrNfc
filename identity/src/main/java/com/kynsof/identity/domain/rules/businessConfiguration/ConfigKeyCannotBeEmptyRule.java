package com.kynsof.identity.domain.rules.businessConfiguration;

import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.rules.BusinessRule;

/**
 * Regla de negocio: La clave de configuración no puede estar vacía
 */
public class ConfigKeyCannotBeEmptyRule extends BusinessRule {

    private final String configKey;

    public ConfigKeyCannotBeEmptyRule(String configKey) {
        super(
                DomainErrorMessage.OBJECT_NOT_NULL,
                new ErrorField("configKey", "The configuration key cannot be empty.")
        );
        this.configKey = configKey;
    }

    @Override
    public boolean isBroken() {
        return this.configKey == null || this.configKey.trim().isEmpty();
    }
}
