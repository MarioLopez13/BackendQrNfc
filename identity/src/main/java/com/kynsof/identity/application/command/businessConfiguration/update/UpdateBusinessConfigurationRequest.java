package com.kynsof.identity.application.command.businessConfiguration.update;

import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.identity.domain.dto.enumType.EConfigDataType;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO para actualizar una configuración de negocio
 */
@Getter
@Setter
public class UpdateBusinessConfigurationRequest {

    private String configValue;
    private EConfigCategory category;
    private EConfigDataType dataType;
    private String description;
    private Boolean isEncrypted;
    private Boolean isActive;
}
