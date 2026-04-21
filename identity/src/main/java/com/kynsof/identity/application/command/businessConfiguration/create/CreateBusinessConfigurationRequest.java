package com.kynsof.identity.application.command.businessConfiguration.create;

import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.identity.domain.dto.enumType.EConfigDataType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Request DTO para crear una configuración de negocio
 */
@Getter
@Setter
public class CreateBusinessConfigurationRequest {

    private UUID businessId;
    private String configKey;
    private String configValue;
    private EConfigCategory category;
    private EConfigDataType dataType;
    private String description;
    private Boolean isEncrypted;
}
